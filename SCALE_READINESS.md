# ReturnRoute scale-readiness audit

**Audit date:** 2026-10-24
**Scope:** Android source and Gradle configuration in this repository. No deployed Firebase project, backend credentials, production telemetry, or load-test environment was available.

## Verdict

**Not ready to serve one lakh (100,000) real users.** This is currently an Android prototype with Firebase Authentication code, not a multi-user production service. “100,000 registered accounts” is also different from “100,000 concurrent active users”; capacity cannot be estimated without an explicit concurrency and traffic profile, a deployed backend, and load tests.

## Verified blockers

1. **Operational data is process-local.** Rides, parcel requests, journey offers, bookings, vehicles, notifications, reviews, and reports are mutable in-memory lists in `app/src/main/java/com/example/data/repository/ReturnRouteRepository.kt`. They disappear when the process is recreated and are not shared between passengers, drivers, admins, app flavors, or devices. The Room database only stores project-task demo records.
2. **Firebase is authentication-only in this checkout.** There is no Firestore/Realtime Database integration, database schema, Firestore rules/indexes, Cloud Functions/backend service, or trusted role-provisioning code. The app can read role claims locally, but local routing/filtering is not server authorization. The Firebase Android configuration is not present here, so live provider sign-in was not verified.
3. **Booking was not a reservation.** Previously the client confirmed bookings without checking current capacity and could reduce both seat and cargo capacity for the same booking. A local fix now checks the latest in-process offer, reserves only the appropriate resource, rejects invalid/stale quantities, prevents same-process overlapping calls from oversubscribing, and catches rejections in the UI. This is a demo-process guard only; it does **not** coordinate multiple devices or replace a backend transaction.
4. **Matching scanned offers on the UI dispatcher.** Matching now runs on `Dispatchers.Default` and only the top 50 results are retained in screen state. It still evaluates the offers currently held by the client; it is not an indexed or paginated server query and cannot scale as total offers grow.
5. **Network sync is sample data, not a live data service.** The current “sync” routine delays and appends bundled example corridors. No operational network/backend fetch occurs.
6. **There is no load-test evidence.** No backend stress test, concurrency test against shared storage, latency target, monitoring, or production error/crash telemetry is configured in the repository. A local unit test can validate local logic but cannot prove 100,000-user capacity.

## Changes in this audit

- Added validation for current seat/cargo availability and valid quantities during local demo booking.
- Corrected reservation accounting: passenger bookings consume seats only; parcel bookings consume cargo capacity only.
- Serialized same-repository booking attempts and added a concurrency regression test for that single process.
- Moved route scoring away from the main/UI dispatcher and limited stored match results to 50.
- Added tests for capacity rejection and the seat/cargo accounting behavior.

These changes improve correctness and responsiveness in the prototype, but are **not a scale-enabling backend**.

## Required production work before real users

1. **Provision one Firebase project** with all three Android app registrations (`com.aistudio.returnroute.rtapp`, `com.aistudio.returnroute.rider`, and `com.aistudio.returnroute.admin`), enable the intended auth providers, register signing fingerprints, and configure App Check. Keep `google-services.json` out of public source control where project policy requires it.
2. **Create a shared operational backend.** A Firebase-based baseline could use Firestore collections for users, vehicles, offers, requests, bookings, notifications, reviews, and reports; indexed, bounded, cursor-paginated queries for each user's feeds; and geohash/corridor/time/status indexes to find a bounded set of route candidates. Do not download all users' records to each handset.
3. **Make booking authoritative and atomic.** Use a trusted callable Cloud Function or backend endpoint to verify the Firebase UID and server-issued role, validate request ownership and offer status, enforce capacity, create an idempotent reservation, and atomically decrement capacity. Generate and reveal verification secrets only to the authorized participants. Enforce state transitions and admin actions on the server, not only in Compose/ViewModel code.
4. **Deploy restrictive authorization rules.** Deny by default; bind user-owned records to `request.auth.uid`; allow admin operations only for server-issued admin claims; validate allowed fields and transitions; prevent clients from changing role, owner, price, or inventory fields directly. Add indexes for the exact query patterns.
5. **Separate fixtures from release builds.** Keep seeded names, license plates, sample PINs, and bundled “network sync” data confined to an explicit demo/debug mode.
6. **Load-test a staging deployment.** Define target registered and concurrent users, writes/second, peak matching requests, p95/p99 latency, failure rate, retry/idempotency behavior, and regional traffic. Test contention on popular offers, cold start, reconnect/offline scenarios, and security rules; monitor cost and error budgets before rollout.

## What is not claimed

This audit does not claim the app now supports 100,000 users. No live Firebase project was available to deploy or validate the backend, and no production load test was run. A Firebase project configuration and authorization to configure/deploy its backend are required to continue the end-to-end migration.
