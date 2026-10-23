# ReturnRoute Android apps

This project builds three separately installable Android apps from shared code:

- **Passenger** — application ID `com.aistudio.returnroute.rtapp`
- **Rider/Driver** — application ID `com.aistudio.returnroute.rider`
- **Admin** — application ID `com.aistudio.returnroute.admin`

In Android Studio, select the `passenger`, `rider`, or `admin` build variant. From Gradle, build them with `:app:assemblePassengerDebug`, `:app:assembleRiderDebug`, and `:app:assembleAdminDebug`.

## Firebase Google sign-in setup

1. In one Firebase project, register all three Android application IDs above.
2. Enable **Google** under Firebase Authentication sign-in providers.
3. Add the appropriate SHA-1 signing-certificate fingerprint(s) to each registered Android app.
4. Download the refreshed `google-services.json` containing all three app clients and place it at `app/google-services.json`.
5. The sign-in button uses Credential Manager to obtain a Google ID token, then exchanges it for a Firebase Authentication session. The Android app must have the generated `default_web_client_id` resource, so sign-in cannot be verified until the Firebase configuration is added.

All three variants can use the same Firebase project and user directory. A new account without a server-issued role claim is treated as a Passenger. The Rider app accepts `role: "driver"` or `role: "delivery_provider"`; the Admin app accepts `role: "admin"` or `admin: true`. Assign or change those claims only from a trusted server using the Firebase Admin SDK—never from client-side app code.

## Important current limitation

Google sign-in establishes the account with Firebase, but the ride, parcel, offer, booking, and notification data is still held in the app's in-memory sample repository. Those records do **not** yet synchronize between the three apps or persist across devices. Firestore storage and restrictive Firestore Security Rules are a separate required step before the apps share operational data or handle real users.
