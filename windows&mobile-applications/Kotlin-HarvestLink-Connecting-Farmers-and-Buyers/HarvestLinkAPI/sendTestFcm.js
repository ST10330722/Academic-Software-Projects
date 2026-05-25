// sendTestFcm.js
const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

async function sendTest() {
  const registrationToken =
    "fdGSKJc3TJS-zEXv54OlWu:APA91bFUNQNGa4aroH3nq5cA9ECfkEhaXtaoBWjTKSrEhWFCacMToxWaw6vI4qa6umryFj1lkllcHtONu5sA9K0cLsOB9BdMJ0xOmDXvQM3ouufzkzQJJRA"; // <-- your "Got FCM token after login" value

  const message = {
    token: registrationToken,
    notification: {
      title: "HarvestLink test",
      body: "This is a test push from Node 🚜",
    },
    data: {
      type: "TEST",
    },
    android: {
      priority: "high",
    },
  };

  try {
    const response = await admin.messaging().send(message);
    console.log("✅ Successfully sent:", response);
  } catch (err) {
    console.error("❌ Error sending message:", err);
  }
}

sendTest();
