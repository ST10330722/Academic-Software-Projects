
const { db, admin } = require("../firebase");

const ordersCollection = db.collection("orders");
const usersCollection = db.collection("users");

// Helper: send a notification to all tokens of a given user
async function sendNotificationToUser(userUid, notification, data = {}) {
  try {
    const tokensSnap = await usersCollection
      .doc(userUid)
      .collection("tokens")
      .get();

    if (tokensSnap.empty) {
      console.log(
        `[FCM] No tokens found for user ${userUid}, skipping notification`
      );
      return;
    }

    const tokens = tokensSnap.docs
      .map((doc) => doc.data().token)
      .filter(Boolean);

    if (tokens.length === 0) {
      console.log(
        `[FCM] Tokens collection for user ${userUid} had no token field values`
      );
      return;
    }

    console.log(`[FCM] Sending to ${tokens.length} token(s) for user ${userUid}`);

    const messages = tokens.map((token) => ({
      token,
      notification,
      data,
      android: {
        priority: "high",
      },
    }));

    const results = await Promise.all(
      messages.map((m) => admin.messaging().send(m))
    );

    console.log("[FCM] Sent messages:", results);
  } catch (e) {
    console.error("[FCM] Error sending notification:", e);
  }
}

// Optionally resolve produce name from Firestore to make nicer messages
async function getProduceNameById(produceDocId) {
  if (!produceDocId) return "your produce";

  try {
    const snap = await db.collection("produce").doc(produceDocId).get();
    if (!snap.exists) return "your produce";
    const data = snap.data() || {};
    return data.name || "your produce";
  } catch (e) {
    console.error("getProduceNameById error:", e);
    return "your produce";
  }
}

// GET /orders
exports.getAllOrders = async (req, res) => {
  try {
    const snapshot = await ordersCollection.get();
    const data = snapshot.docs.map((doc) => ({ id: doc.id, ...doc.data() }));
    res.json(data);
  } catch (e) {
    console.error("getAllOrders error:", e);
    res.status(500).json({ error: e.message });
  }
};

// POST /orders
exports.addOrder = async (req, res) => {
  try {
    const orderBody = req.body; // buyerUid, farmerUid, produceDocId, quantity, note, etc.
    const docRef = await ordersCollection.add(orderBody);
    const savedOrder = { id: docRef.id, ...orderBody };

    res.status(201).json(savedOrder);

    // ---- AFTER saving, send push notification to farmer ----
    const { farmerUid, produceDocId, quantity } = orderBody;

    if (farmerUid) {
      try {
        const produceName = await getProduceNameById(produceDocId);

        await sendNotificationToUser(
          farmerUid,
          {
            title: "New order received",
            body: `New order for ${produceName} – ${quantity || 0}`,
          },
          {
            type: "NEW_ORDER",
            role: "farmer",
            orderId: docRef.id,
          }
        );
      } catch (e) {
        console.error("[FCM] Error sending farmer notification:", e);
      }
    }
  } catch (e) {
    console.error("addOrder error:", e);
    res.status(500).json({ error: e.message });
  }
};

// PUT /orders/:id/status
exports.updateOrderStatus = async (req, res) => {
  try {
    const id = req.params.id;
    const { status } = req.body;

    // Get full order so we can know buyerUid, produceDocId
    const orderRef = ordersCollection.doc(id);
    const orderSnap = await orderRef.get();

    if (!orderSnap.exists) {
      return res.status(404).json({ error: "Order not found" });
    }

    const orderData = orderSnap.data() || {};
    await orderRef.update({ status });

    res.json({ id, status });

    // ---- AFTER updating, send push notification to buyer ----
    const { buyerUid, produceDocId } = orderData;

    if (buyerUid) {
      try {
        const produceName = await getProduceNameById(produceDocId);

        await sendNotificationToUser(
          buyerUid,
          {
            title: "Order status updated",
            body: `Your order for ${produceName} is now ${status}`,
          },
          {
            type: "STATUS_UPDATE",
            role: "buyer",
            orderId: id,
            status: status || "",
          }
        );
      } catch (e) {
        console.error("[FCM] Error sending buyer notification:", e);
      }
    }
  } catch (e) {
    console.error("updateOrderStatus error:", e);
    res.status(500).json({ error: e.message });
  }
};

// DELETE /orders/:id
exports.deleteOrder = async (req, res) => {
  try {
    const id = req.params.id;
    await ordersCollection.doc(id).delete();
    res.json({ id, message: "deleted" });
  } catch (e) {
    console.error("deleteOrder error:", e);
    res.status(500).json({ error: e.message });
  }
};
