import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore, FieldValue } from "firebase-admin/firestore";
import { CallableRequest, HttpsError, onCall } from "firebase-functions/v2/https";

initializeApp();
const db = getFirestore();
const REGION = "asia-south1";

function requireSignedIn(request: CallableRequest<Record<string, unknown>>): string {
  const uid = request.auth?.uid;
  if (!uid) throw new HttpsError("unauthenticated", "Sign in first.");
  return uid;
}

function requireAdmin(request: CallableRequest<Record<string, unknown>>): string {
  const uid = requireSignedIn(request);
  const claims = request.auth?.token;
  if (claims?.admin !== true && claims?.role !== "admin" && claims?.role !== "administrator") {
    throw new HttpsError("permission-denied", "Only an administrator can review Rider access.");
  }
  return uid;
}

export const requestRiderAccess = onCall({ region: REGION, enforceAppCheck: true }, async (request) => {
  const uid = requireSignedIn(request);
  const note = typeof request.data?.note === "string" ? request.data.note.trim().slice(0, 500) : "";
  const userRecord = await getAuth().getUser(uid);
  if (userRecord.customClaims?.admin === true || userRecord.customClaims?.role === "admin") {
    throw new HttpsError("failed-precondition", "Administrator accounts do not need Rider access.");
  }
  if (["driver", "rider", "delivery_provider"].includes(String(userRecord.customClaims?.role ?? "").toLowerCase())) {
    throw new HttpsError("already-exists", "This account already has Rider access.");
  }

  const ref = db.collection("riderAccessRequests").doc(uid);
  await db.runTransaction(async (tx) => {
    const existing = await tx.get(ref);
    const status = existing.get("status");
    if (status === "approved") throw new HttpsError("already-exists", "Rider access is already approved.");
    if (status === "pending") throw new HttpsError("already-exists", "Your Rider access request is already awaiting review.");
    tx.set(ref, {
      uid,
      displayName: userRecord.displayName ?? userRecord.email?.split("@")[0] ?? "ReturnRoute user",
      email: userRecord.email ?? "",
      note,
      status: "pending",
      createdAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    });
  });
  return { status: "pending" };
});

export const reviewRiderAccess = onCall({ region: REGION, enforceAppCheck: true }, async (request) => {
  const adminUid = requireAdmin(request);
  const uid = typeof request.data?.uid === "string" ? request.data.uid.trim() : "";
  const approved = request.data?.approved;
  const note = typeof request.data?.note === "string" ? request.data.note.trim().slice(0, 500) : "";
  if (!uid || uid === adminUid || typeof approved !== "boolean") {
    throw new HttpsError("invalid-argument", "Provide a valid user ID and approval decision.");
  }

  const ref = db.collection("riderAccessRequests").doc(uid);
  const snap = await ref.get();
  if (!snap.exists || snap.get("status") !== "pending") {
    throw new HttpsError("failed-precondition", "This Rider access request is no longer pending.");
  }
  const target = await getAuth().getUser(uid);
  const claims = { ...(target.customClaims ?? {}) };
  if (approved) {
    if (claims.admin === true || claims.role === "admin" || claims.role === "administrator") {
      throw new HttpsError("failed-precondition", "An administrator cannot be downgraded to a Rider.");
    }
    // This role is authoritative only because this operation runs with Admin SDK privileges.
    claims.role = "driver";
    delete claims.admin;
    await getAuth().setCustomUserClaims(uid, claims);
  }

  await ref.update({
    status: approved ? "approved" : "denied",
    reviewedBy: adminUid,
    reviewNote: note,
    reviewedAt: FieldValue.serverTimestamp(),
    updatedAt: FieldValue.serverTimestamp(),
  });
  await db.collection("riderAccessAudit").add({
    uid,
    actorUid: adminUid,
    action: approved ? "rider_access_granted" : "rider_access_denied",
    note,
    createdAt: FieldValue.serverTimestamp(),
  });
  return { status: approved ? "approved" : "denied" };
});
