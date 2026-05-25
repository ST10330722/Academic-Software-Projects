
const { db, admin } = require("./firebase");


async function getUserTokens(uid) {
  const snap = await db
    .collection("users")
    .doc(uid)
    .collection("tokens")
    .get();

  return snap.docs.map(doc => doc.id); 
}

/**
 * Send a notification to all of a user's devices.
 *
 * @param {string} uid - UID of recipient user
 * @param {{title: string, body: string}} notification
 * @param {object} data - custom data payload 
 */
async function sendNotificationToUser(uid, notification, data = {}) {
  const tokens = await getUserTokens(uid);

  if (!tokens.length) {
    console.log(`No tokens found for user ${uid}, skipping notification.`);
    return;
  }

  
  const dataStringified = {};
  for (const [key, value] of Object.entries(data)) {
    dataStringified[key] = String(value);
  }

  const message = {
    tokens,
    notification,
    data: dataStringified,
  };

  const response = await admin.messaging().sendMulticast(message);
  console.log(
    `Sent notification to ${tokens.length} tokens for user ${uid}. ` +
    `Success: ${response.successCount}, Failure: ${response.failureCount}`
  );
}

module.exports = {
  sendNotificationToUser,
};
