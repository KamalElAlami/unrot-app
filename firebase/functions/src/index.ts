import { initializeApp } from "firebase-admin/app";
import { getFirestore, FieldValue } from "firebase-admin/firestore";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { createHash } from "node:crypto";

initializeApp();
const db = getFirestore();
const games = ["COLOR_CLASH", "MEMORY_GRID", "SIGNAL_WATCH", "RULE_SHIFT", "STORY_RECALL"] as const;

export function seedForDate(date: string): number {
  const digest = createHash("sha256").update(`focus-reset:${date}`).digest();
  return digest.readUInt32BE(0);
}

export function gameSet(seed: number): string[] {
  return [...games].sort((a, b) => createHash("sha1").update(`${seed}:${a}`).digest().compare(createHash("sha1").update(`${seed}:${b}`).digest())).slice(0, 3);
}

export const getDailySeed = onCall({ enforceAppCheck: true }, async (request) => {
  const date = typeof request.data?.date === "string" ? request.data.date : new Date().toISOString().slice(0, 10);
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) throw new HttpsError("invalid-argument", "Invalid date.");
  const seed = seedForDate(date);
  const payload = { date, seed, games: gameSet(seed), expiresAt: `${date}T23:59:59.999Z` };
  await db.collection("dailySeeds").doc(date).set(payload, { merge: true });
  return payload;
});

export const validateScore = onDocumentCreated("scoreSubmissions/{runId}", async (event) => {
  const snap = event.data;
  if (!snap) return;
  const data = snap.data();
  const score = Number(data.score), mistakes = Number(data.mistakes), duration = Number(data.durationMs), seed = Number(data.seed);
  if (!Number.isInteger(score) || score < 0 || score > 100 || !Number.isInteger(mistakes) || mistakes < 0 || duration < 1_000 || duration > 600_000) {
    await snap.ref.update({ status: "rejected", rejection: "bounds" }); return;
  }
  const today = new Date().toISOString().slice(0, 10);
  if (seed !== seedForDate(today)) { await snap.ref.update({ status: "rejected", rejection: "expired_seed" }); return; }
  const boardId = `${data.squadId}_${today}`;
  const entryRef = db.collection("leaderboards").doc(boardId).collection("entries").doc(data.uid);
  await db.runTransaction(async tx => {
    const existing = await tx.get(entryRef);
    const old = existing.data();
    const better = !old || score > old.score || (score === old.score && (mistakes < old.mistakes || (mistakes === old.mistakes && duration < old.durationMs)));
    if (better) tx.set(entryRef, { uid: data.uid, score, mistakes, durationMs: duration, runId: snap.id, date: today, updatedAt: FieldValue.serverTimestamp() });
    tx.update(snap.ref, { status: "accepted", validatedAt: FieldValue.serverTimestamp() });
  });
});

export const deleteAccountData = onCall({ enforceAppCheck: true }, async request => {
  const uid = request.auth?.uid;
  if (!uid) throw new HttpsError("unauthenticated", "Sign-in required.");
  const profile = db.collection("profiles").doc(uid);
  const progress = await db.collection("challengeProgress").where("uid", "==", uid).get();
  const scores = await db.collection("scoreSubmissions").where("uid", "==", uid).get();
  const writes = [profile, ...progress.docs.map(d => d.ref), ...scores.docs.map(d => d.ref)].map(ref => db.recursiveDelete(ref));
  await Promise.all(writes);
  return { deleted: true };
});
