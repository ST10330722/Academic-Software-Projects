const { db } = require("../firebase");
const collection = db.collection("produce");

// GET /produce
exports.getAllProduce = async (req, res) => {
  try {
    const snapshot = await collection.get();
    const data = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    res.json(data);
  } catch (e) {
    console.error("getAllProduce error:", e);
    res.status(500).json({ error: e.message });
  }
};

// POST /produce
exports.addProduce = async (req, res) => {
  try {
    const docRef = await collection.add(req.body);
    res.status(201).json({ id: docRef.id, ...req.body });
  } catch (e) {
    console.error("addProduce error:", e);
    res.status(500).json({ error: e.message });
  }
};

// PUT /produce/:id
exports.updateProduce = async (req, res) => {
  try {
    const id = req.params.id;
    await collection.doc(id).update(req.body);
    res.json({ id, ...req.body });
  } catch (e) {
    console.error("updateProduce error:", e);
    res.status(500).json({ error: e.message });
  }
};

// DELETE /produce/:id
exports.deleteProduce = async (req, res) => {
  try {
    const id = req.params.id;
    await collection.doc(id).delete();
    res.json({ id, message: "deleted" });
  } catch (e) {
    console.error("deleteProduce error:", e);
    res.status(500).json({ error: e.message });
  }
};
