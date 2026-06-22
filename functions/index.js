const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();
const db = getFirestore();
const messaging = getMessaging();

/**
 * Reads every FCM token registered for `uid` (users/{uid}/fcmTokens/{token}) and
 * sends one data-only message to all of them.
 *
 * The Android handler (AppFirebaseMessagingService) keys off these `data` fields:
 *   type      "message" | "job_alert"  -> channel + tap destination
 *   title     status-bar title
 *   body      status-bar text
 *   channelId optional, message type only -> deep-links to that chat
 *
 * Data-only (no `notification` block) is deliberate: it guarantees onMessageReceived
 * runs in every app state, so channel routing + deep-link always apply. Stale tokens
 * (UNREGISTERED / invalid) are pruned so the collection stays clean.
 */
async function sendToUser(uid, data) {
  const snap = await db.collection("users").doc(uid).collection("fcmTokens").get();
  const tokens = snap.docs.map((d) => d.id);
  if (tokens.length === 0) return;

  const res = await messaging.sendEachForMulticast({
    tokens,
    data,
    android: { priority: "high" },
  });

  const stale = [];
  res.responses.forEach((r, i) => {
    const code = r.error?.code;
    if (
      code === "messaging/registration-token-not-registered" ||
      code === "messaging/invalid-registration-token" ||
      code === "messaging/invalid-argument"
    ) {
      stale.push(tokens[i]);
    }
  });
  await Promise.all(
    stale.map((t) =>
      db.collection("users").doc(uid).collection("fcmTokens").doc(t).delete()
    )
  );
}

/**
 * New job posted -> notify interested users with a "job_alert" push.
 *
 * Sketch targets users who saved a job whose title matches the new posting's title
 * (users/{uid}/savedJobs). Swap this query for your real interest model (saved
 * searches, tags, location radius) before production — a blanket fan-out to every
 * user does not scale.
 */
exports.onJobCreated = onDocumentCreated("jobs/{jobId}", async (event) => {
  const job = event.data?.data();
  if (!job) return;

  const title = "New job: " + (job.title || "Opportunity");
  const body = [job.company, job.location].filter(Boolean).join(" · ");

  const interested = await db
    .collectionGroup("savedJobs")
    .where("title", "==", job.title)
    .get();

  const uids = new Set(
    interested.docs
      .map((d) => d.ref.parent.parent?.id) // users/{uid}/savedJobs/{jobId} -> uid
      .filter(Boolean)
  );

  await Promise.all(
    [...uids].map((uid) =>
      sendToUser(uid, {
        type: "job_alert",
        title,
        body,
        jobId: event.params.jobId,
      })
    )
  );
});

/**
 * Chat is Stream-backed, so messages are not in Firestore. Call this from your
 * Stream `message.new` webhook (or any server) to push a "message" notification.
 *
 * In production secure this: a callable runs as the *caller*, so either restrict it
 * to authenticated app users sending on their own behalf, or move the logic behind a
 * webhook authenticated by a shared secret instead of onCall.
 */
exports.sendChatMessagePush = onCall(async (req) => {
  const { recipientUid, channelId, senderName, text } = req.data || {};
  if (!recipientUid || !channelId) {
    throw new HttpsError("invalid-argument", "recipientUid and channelId are required.");
  }
  await sendToUser(recipientUid, {
    type: "message",
    title: senderName || "New message",
    body: text || "",
    channelId,
  });
  return { ok: true };
});
