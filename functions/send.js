// Standalone FCM sender — runs anywhere with a service-account key, no Blaze plan.
//
// Setup:
//   cd functions && npm install
//   place your key at functions/service-account.json   (already gitignored)
//
// Usage:
//   node send.js message <uid> <channelId> "<senderName>" "<text>"
//   node send.js job <uid> "<title>" "<company>" "<location>"
//   node send.js job-fanout <jobId>     # look up interested users, push to each

const { initializeApp, cert } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

const KEY_PATH = process.env.GOOGLE_APPLICATION_CREDENTIALS || "./service-account.json";

initializeApp({ credential: cert(require(require("path").resolve(KEY_PATH))) });
const db = getFirestore();
const messaging = getMessaging();

// Reads users/{uid}/fcmTokens, sends one data-only push to all of them, prunes stale tokens.
async function sendToUser(uid, data) {
  const snap = await db.collection("users").doc(uid).collection("fcmTokens").get();
  const tokens = snap.docs.map((d) => d.id);
  if (tokens.length === 0) {
    console.log(`No tokens for ${uid}`);
    return;
  }
  const res = await messaging.sendEachForMulticast({
    tokens,
    data,
    android: { priority: "high" },
  });
  console.log(`Sent: ${res.successCount} ok, ${res.failureCount} failed`);

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
  if (stale.length) console.log(`Pruned ${stale.length} stale token(s)`);
}

async function jobFanout(jobId) {
  const jobSnap = await db.collection("jobs").doc(jobId).get();
  const job = jobSnap.data();
  if (!job) throw new Error(`Job ${jobId} not found`);

  const interested = await db
    .collectionGroup("savedJobs")
    .where("title", "==", job.title)
    .get();
  const uids = new Set(
    interested.docs.map((d) => d.ref.parent.parent?.id).filter(Boolean)
  );
  const title = "New job: " + (job.title || "Opportunity");
  const body = [job.company, job.location].filter(Boolean).join(" · ");
  await Promise.all(
    [...uids].map((uid) => sendToUser(uid, { type: "job_alert", title, body, jobId }))
  );
  console.log(`Fanned out to ${uids.size} user(s)`);
}

async function main() {
  const [cmd, ...a] = process.argv.slice(2);
  if (cmd === "message") {
    const [uid, channelId, senderName, text] = a;
    await sendToUser(uid, { type: "message", title: senderName, body: text, channelId });
  } else if (cmd === "job") {
    const [uid, title, company, location] = a;
    await sendToUser(uid, {
      type: "job_alert",
      title: "New job: " + title,
      body: [company, location].filter(Boolean).join(" · "),
    });
  } else if (cmd === "job-fanout") {
    await jobFanout(a[0]);
  } else {
    console.log("Usage: node send.js message|job|job-fanout ...");
    process.exit(1);
  }
}

main().then(() => process.exit(0)).catch((e) => {
  console.error(e);
  process.exit(1);
});
