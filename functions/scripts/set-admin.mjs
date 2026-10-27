import { applicationDefault, initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";

const uid = process.argv[2]?.trim();
if (!uid || !process.env.FIREBASE_PROJECT_ID) {
  console.error("Usage: FIREBASE_PROJECT_ID=your-project-id GOOGLE_APPLICATION_CREDENTIALS=/secure/path/service-account.json node scripts/set-admin.mjs <firebase-auth-uid>");
  process.exit(2);
}

initializeApp({ credential: applicationDefault(), projectId: process.env.FIREBASE_PROJECT_ID });
const auth = getAuth();
const user = await auth.getUser(uid);
const claims = { ...(user.customClaims ?? {}), admin: true, role: "admin" };
await auth.setCustomUserClaims(uid, claims);
console.log(`Admin custom claim set for UID ${uid}. Sign out and back in to refresh the ID token.`);
