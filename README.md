# ReturnRoute Android apps

This project builds three separately installable Android apps from shared code:

- **Passenger** — application ID `com.aistudio.returnroute.rtapp`
- **Rider/Driver** — application ID `com.aistudio.returnroute.rider`
- **Admin** — application ID `com.aistudio.returnroute.admin`

In Android Studio, select the `passenger`, `rider`, or `admin` build variant. From Gradle, build them with `:app:assemblePassengerDebug`, `:app:assembleRiderDebug`, and `:app:assembleAdminDebug`.

## Firebase Google sign-in setup

1. In one Firebase project, register all three Android application IDs above.
2. Under Firebase Authentication sign-in providers, enable **Google**, **Phone**, and **Email/Password**, then also enable **Email link (passwordless sign-in)**.
3. Set the SMS region policy to allow the countries your service supports. Add the appropriate SHA-1 and SHA-256 signing-certificate fingerprints to each registered Android app. Phone Auth uses Play Integrity when available and may fall back to reCAPTCHA.
4. Download the refreshed `google-services.json` containing all three app clients and place it at `app/google-services.json`.
5. Google uses Credential Manager and Firebase Auth; phone sign-in sends an SMS verification code; email-link sign-in sends a Firebase Hosting link that returns to the installed app. Email-link Android app-link setup also requires the app package names and signing fingerprints in Firebase, plus the Firebase Hosting domain for the project. The Android app needs the generated `default_web_client_id`, so provider sign-in cannot be tested until Firebase is configured.

All three variants can use the same Firebase project and user directory. A new account without a server-issued role claim is treated as a Passenger. The Rider app accepts `role: "driver"` or `role: "delivery_provider"`; the Admin app accepts `role: "admin"` or `admin: true`. Assign or change those claims only from a trusted server using the Firebase Admin SDK—never from client-side app code.

## Important current limitation

Google, SMS, and email-link sign-in establish accounts with Firebase, but the ride, parcel, offer, booking, and notification data is still held in the app's in-memory sample repository. Those records do **not** yet synchronize between the three apps or persist across devices. Firestore storage and restrictive Firestore Security Rules are a separate required step before the apps share operational data or handle real users.
