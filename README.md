# ReturnRoute Android apps

This repository builds three separately installable Android apps from shared code:

- **Passenger** — `com.aistudio.returnroute.rtapp`
- **Rider/Driver** — `com.aistudio.returnroute.rider`
- **Admin** — `com.aistudio.returnroute.admin`

Build with `:app:assemblePassengerDebug`, `:app:assembleRiderDebug`, and `:app:assembleAdminDebug`.

## Download and install the Android apps

Download the latest public debug-preview APK from the [GitHub Releases page](https://github.com/habeebc84-create/return-route/releases/latest):

- [Passenger APK](https://github.com/habeebc84-create/return-route/releases/latest/download/ReturnRoute-Passenger-debug.apk)
- [Rider APK](https://github.com/habeebc84-create/return-route/releases/latest/download/ReturnRoute-Rider-debug.apk)
- [Admin APK](https://github.com/habeebc84-create/return-route/releases/latest/download/ReturnRoute-Admin-debug.apk)

On Android, download the APK, open it from Downloads, and approve **Install unknown apps** for the browser or file manager if prompted. Install all three to use the separate apps side by side. These are **debug preview builds**, not production-store releases. If Android reports a signature conflict when replacing an older test build, uninstall that old build first. Firebase sign-in and Admin/Rider approval require the Firebase setup described below.

## Firebase Authentication and shared project

1. Register all three Android application IDs in the **same Firebase project**.
2. Enable Email/Password, Google, Phone, and Email link sign-in providers. Phone verification is SMS OTP and may have provider/carrier costs or quotas.
3. Set the allowed SMS regions and add SHA-1/SHA-256 signing fingerprints for each app.
4. Download a `google-services.json` that includes all three Android clients and place it at `app/google-services.json`.
5. Enable Firebase App Check for all three app registrations. Release uses Play Integrity. Debug APKs use the Debug provider; register each debug token shown in Logcat in Firebase Console before using the access workflow.

Firebase Email/Password uses an email address. Phone sign-in uses SMS OTP, not a password. A custom account-ID/password or phone/password flow needs a trusted backend and is not implemented as a client-side email lookup.

## Admin-controlled Rider access

The Passenger app includes **Request Rider access**. The Admin app receives pending requests from Firestore and can grant or deny them. A grant is performed by a Firebase Callable Function with the Admin SDK; the Android client cannot write role claims. After approval, the user should sign out of Passenger, install/open Rider, and sign in again to receive the updated claim. Admin and Rider app access is still guarded by Firebase custom claims.

### Deploy the secure access workflow

Cloud Functions deployment requires a Firebase project with billing/Blaze enabled. This workflow is not a free-only Firebase feature. Firestore may have a no-cost quota, but functions deployment requires billing setup; monitor quotas and costs.

```bash
npm --prefix functions install
npm --prefix functions run build
firebase login
firebase use --add
firebase deploy --only firestore:rules,functions
```

Enable the **Play Integrity** App Check provider for each Android app in Firebase Console. For local debug builds, run each app once, copy its Firebase App Check debug token from Logcat, and register it under that app's App Check settings. Do not deploy with App Check enforcement disabled.

### Bootstrap the first Admin safely

An initial Admin custom claim must be granted from a trusted environment using Firebase Admin SDK credentials. Never put service-account credentials in the Android app or repository. With ADC or a securely managed service-account credential configured:

```bash
cd functions
FIREBASE_PROJECT_ID=your-firebase-project npm run set-admin -- FIREBASE_AUTH_UID
```

Get the UID from Firebase Authentication. The Admin user must sign out and back in to refresh the token. Later Rider approvals are handled by the callable function, which checks `admin: true` or `role: "admin"` on the caller's verified ID token.

## Important current limitations

The Rider access request, Admin review, status listener, and role grant use Firebase Functions, Firestore, Authentication, and App Check. The Rider app also has a request-board screen, and its in-memory demo view exposes active passenger/parcel samples to an authorized Rider.

However, **ride requests, parcel requests, offers, bookings, notifications, incident reports, and most dashboard metrics are still prototype-local/in-memory**. They do not yet persist or synchronize between separate app installs or devices. Do not use the current app as a production dispatch or booking service until these collections are moved to Firestore (or another authenticated backend), ownership/security rules and transaction-safe seat/cargo reservations are implemented, and end-to-end tests are run against the deployed Firebase project. The Admin dashboard labels this limitation rather than presenting hard-coded demo metrics as live data.
