# EazyPay Production Readiness Audit

Audit date: 2026-07-02
Scope: Re-audit of the Android app currently available in `/workspace/EazyPay`, compared against the supplied EazyPay PRD v1.0 for the Babcock University pilot.

## GitHub/source sync check

I checked the local repository configuration before this re-audit. The checkout currently has no Git remote configured in `.git/config`, so there is no GitHub `origin` URL available from this environment to fetch or compare against. This report therefore audits the latest code present in the working tree and local git history. If newer commits exist on GitHub, add the remote or provide the repository URL, then rerun the audit against the fetched branch.

## Executive recommendation

The current repository should still be treated as a polished interactive prototype, not as a production-ready payment application. It demonstrates several PRD concepts in Jetpack Compose—student wallet screens, vendor terminal simulation, offline banners, pending/synced transaction labels, support chat, and demo flows—but the money movement, NFC, cryptography, authentication, backend sync, compliance controls, and operational tooling required for a real closed-loop stored-value payment system are not implemented.

Recommendation: pause any real-money pilot launch until the product is rebuilt around a secure backend ledger, real NFC token issuance/verification, encrypted local storage, server-side reconciliation, regulated payment/settlement integrations, observability, and a formal QA/security program. The existing app can be reused as a UX prototype and stakeholder demo while production foundations are built.


## Source evidence reviewed

- `app/build.gradle.kts` now uses a production-style application ID/namespace and enables release minification/resource shrinking, but release signing still depends on local/environment keystore inputs and needs a documented CI signing pipeline.
- `app/src/main/AndroidManifest.xml` now disables Android backup, and the backup/data-extraction rules exclude local preferences, databases, files, and root data from backup/transfer.
- `app/src/main/java/com/example/data/EazyPayRepository.kt` is still the main source of wallet, role, card, balance, top-up, withdrawal, and offline-sync behavior. PINs are now persisted as PBKDF2 verifiers instead of plaintext, but other wallet/payment state still uses local `SharedPreferences`, `Double`/`Float` money values, demo seed data, and simulated pending-to-synced transitions.
- `app/src/main/java/com/example/data/TransactionEntity.kt` contains only a minimal local transaction schema and does not model PRD-required transaction references, payer/payee IDs, device/terminal IDs, NFC card IDs, fees, campus IDs, idempotency keys, or server timestamps.
- `app/src/main/java/com/example/ui/EazyPayViewModel.kt` orchestrates terminal payments, support chat, PIN attempts, withdrawal requests, and demo flows locally; there is no backend client, no NFC API boundary, and no cryptographic token verification boundary.
- `app/src/main/java/com/example/ui/screens/VendorScreens.kt` describes the terminal NFC behavior as simulation, confirming that the vendor terminal remains a prototype UI rather than a real Android NFC reader/payment terminal.

## Current implementation snapshot

| Area | Current state | Production readiness |
| --- | --- | --- |
| Platform | Native Android app using Kotlin and Jetpack Compose with production-style package/application ID and release shrinking enabled. | Still prototype overall; native Android is acceptable for an Android-first pilot if backend and security foundations are completed. |
| Backend/API | No backend service exists in this repo. Retrofit/OkHttp dependencies are present but no API client or endpoints are implemented. | Not ready. Backend is mandatory for wallet authority, auth, token issuance, sync, settlement, admin, and audit. |
| Persistence | Room stores only a simple transaction table; SharedPreferences stores user, balance, role, cards, biometrics flag, vendor earnings, and a PBKDF2 PIN verifier. | Improved for PIN storage, but not ready: wallet/payment data is still local mutable state and schema lacks required models. |
| Offline payments | Simulated offline mode flips a Boolean; pending transactions are local rows updated to `Synced` after a delay. | Not ready. No encrypted queue, idempotency, conflict resolution, ledger integrity, or server reconciliation. |
| NFC | UI simulates terminal scans; no Android NFC API integration, HCE, card UID reads, or token validation. | Not ready. NFC is core to the PRD and must be implemented before pilot. |
| Security | PIN storage is improved with salted PBKDF2 verification and raw PIN state is no longer exposed; backups are disabled. | Still blocker: no hardware-backed key management, no encrypted local queue, no persisted lockout/fraud telemetry, and no backend auth/session model. |
| Admin/dashboard | Not present. | Critical blocker for onboarding, settlements, disputes, revocation, monitoring, and operations. |
| Payments/top-up/settlement | Top-ups and withdrawals mutate local balances; no Paystack/bank/virtual account/webhook integration. | Critical blocker. |
| Testing | Dependencies exist; no meaningful unit/integration/security/offline tests found in source. | Not ready. |

## PRD fit-gap assessment

### 1. Student wallet app

**Implemented or partially implemented**

- Student home, pay, history, profile, top-up modal, support modal, transaction receipt/dispute UI concepts are present.
- Balance visibility, low-balance-style messaging, offline indicators, pending/synced labels, and quick top-up UX are represented.
- Onboarding has phone, role, OTP simulation, and PIN setup screens.

**Major gaps**

- OTP verification is simulated; no Termii/Sendchamp integration.
- PIN storage has been improved to a salted PBKDF2 verifier and legacy plaintext PINs are migrated away locally; this still needs backend-side PIN policy, fraud telemetry, and hardware-backed device-key integration.
- Biometric setting is only a Boolean preference, not backed by Android BiometricPrompt/Keystore.
- NFC card/sticker linking is text-list simulation, not a card UID/token issuance process.
- Wallet balance is local mutable state, not an authoritative server wallet.
- Top-up is local balance increment only; no virtual account, Paystack, USSD, webhook, or reconciliation.
- Transaction history stores only title/category/timestamp/amount/debit/status; it lacks transaction reference, payer/payee IDs, device ID, terminal ID, NFC card ID, fee, campus ID, server timestamps, and admin statuses.
- Offline local ledger does not prove funds availability safely across multiple terminals/cards and can be manipulated on device.

### 2. Vendor/driver app

**Implemented or partially implemented**

- Vendor dashboard, terminal flow, earnings list, and withdrawal modal concepts are present.
- Terminal scan and PIN entry flows are simulated.
- Vendor earnings update locally after simulated payments.

**Major gaps**

- Driver/vendor onboarding is not admin-approved and has no identity/KYC document handling.
- No NFC capability check or real NFC reader mode.
- No max transaction validation, amount signing, idempotency key, or signed transaction receipt.
- Withdrawal requests subtract local earnings immediately instead of creating a controlled settlement workflow.
- Vendor and student transactions are stored in one shared local table, which makes demo behavior easy but cannot represent real multi-user ledgers.

### 3. Hardware terminal

No hardware-terminal code, device management, OTA, SIM sync, tamper detection, key-store wipe, battery health, or terminal telemetry is present. This is a separate product stream and should not be assumed covered by the Android app.

### 4. Backend/API

No backend exists in this repository. This is the largest production gap. The PRD requires user, wallet, NFC token, transaction, notification, settlement, and analytics services. For a financial product, the backend ledger must be the source of truth and must enforce idempotency, double-entry accounting, transaction limits, role-based permissions, audit logs, and reconciliation.

### 5. Admin dashboard

No admin dashboard exists. The pilot cannot safely operate without admin controls for user activation, wallet adjustment, refunds, disputes, settlement approval, card revocation, and live monitoring.

### 6. Compliance and regulatory readiness

The app currently collects and displays demo personal data but does not implement consent capture, privacy policy acceptance, deletion request flow, data retention rules, audit logs, KYC tiering, pool-account reconciliation, or breach response workflows. Legal/regulatory counsel and a partner-bank/PSSP arrangement must be in place before processing real money.

## Critical blockers before any real-money pilot

1. **Local trust model remains unsafe**: PINs are no longer persisted as plaintext, but wallet balances and transaction state are still local and can be altered on a compromised device without backend authority.
2. **No authoritative backend ledger**: There is no server-side wallet, double-entry ledger, transaction sync endpoint, idempotency, or reconciliation.
3. **No real NFC/token system**: The app does not read/write NFC tags, issue signed tokens, verify tokens offline, or support revocation.
4. **No encrypted local queue**: Room is unencrypted and pending transactions can be altered.
5. **No real payments**: Top-up and withdrawal flows are demos, with no partner bank, Paystack, webhooks, or settlement controls.
6. **No admin operations tooling**: User activation, disputes, refunds, settlements, card revocation, device health, and fraud monitoring are missing.
7. **No meaningful automated test coverage**: Wallet edge cases, offline sync, security, and performance requirements are not covered.
8. **Release hardening incomplete**: Application ID/namespace, shrinking, and backup posture have improved, but production signing, CI provenance, environment separation, and runtime hardening remain incomplete.

## Production architecture recommendation

### Backend-first MVP foundation

Build a backend before continuing deep client work:

- NestJS or Kotlin/Spring backend with `/api/v1` versioning.
- PostgreSQL with strict migrations and a double-entry wallet ledger.
- Redis/BullMQ or equivalent for async notifications, webhooks, and settlement jobs.
- Idempotency table keyed by transaction reference/device-generated UUID.
- Role-based access control for student, vendor, agent, admin, and support roles.
- Audit-log table for every manual wallet, settlement, user status, card, and dispute action.
- Webhook ingestion for Paystack/partner-bank virtual accounts with signature verification and replay protection.
- Admin dashboard for operations from day one.

### Wallet and ledger design

- Never trust client balances as authoritative.
- Store balances as integer kobo, not floating-point `Double`.
- Use double-entry rows: debit student wallet, credit vendor wallet, credit fee revenue where applicable.
- Use database transactions and row-level locking for online commits.
- Keep offline payments in a `pending_offline_authorizations` flow until server reconciliation.
- Define hard limits for offline spend ceilings per card/device to reduce risk if a card or terminal is compromised.

### Offline/NFC payment design

The PRD's “offline debit from student and credit vendor” is feasible only with careful risk controls. Recommended pattern:

1. Student card/device holds a signed offline authorization profile: user ID, card ID, offline spend limit, token expiry, public metadata, and token version.
2. Vendor terminal has a periodically refreshed public-key bundle and revocation list.
3. Vendor terminal reads card token via NFC and validates signature/expiry/revocation locally.
4. Student enters PIN on vendor terminal; terminal verifies against a derived verifier, not a raw PIN/hash that can be trivially replayed.
5. Terminal signs the transaction with a device private key stored in Android Keystore/hardware-backed storage where available.
6. Transaction queue is encrypted and append-only locally.
7. Sync endpoint deduplicates by transaction reference and verifies terminal signature, card token, amount limit, timestamp window, and replay status.
8. Backend reconciles against authoritative wallet balance and flags exceptions for admin review.

### Mobile app hardening

- Replace SharedPreferences with encrypted storage/DataStore for non-ledger preferences; do not store raw PIN.
- Use SQLCipher/Room encryption or an approved encrypted offline queue.
- Use Android Keystore for device keys and biometric-protected credentials.
- Disable backups for sensitive data unless using an explicitly approved backup strategy.
- Enforce certificate pinning only after backend/domain operational maturity, with a safe rotation plan.
- Remove demo defaults from production builds.
- Use separate build flavors: `demo`, `staging`, `production`.

### Admin and operations

Minimum admin dashboard for pilot:

- User/vendor onboarding approval and suspension.
- Wallet view with immutable ledger and audited adjustment/refund tools.
- Transaction search by user, vendor, card, terminal, status, campus, and date.
- Offline sync exception queue.
- Settlement approval and payout status.
- Card issue/revoke/replace.
- Terminal/device health: last sync, battery, app version, pending queue count.
- CSV export for finance reconciliation.

### Compliance and governance

- Confirm CBN/PSSP/partner-bank operating model before live funds.
- Maintain a ring-fenced pool account and daily reconciliation process.
- Implement NDPA/NDPR consent, privacy policy, deletion request, data minimization, retention, and breach notification procedures.
- Define KYC tiers, limits, and blocked-user/card behavior.
- Run an external security review before pilot.

## Engineering remediation plan

### Phase 0: Stabilize prototype and prepare production repo (1 week)

- Rename package/namespace away from `com.example` and generated IDs.
- Add product flavors for demo/staging/production.
- Remove hard-coded demo PII from production paths.
- Disable sensitive backups and enable release minification/shrinking.
- Add CI for build, unit tests, lint, dependency review, and secret scanning.
- Document architecture decisions and threat model.

### Phase 1: Backend and ledger MVP (2-4 weeks)

- Create backend repo/service with auth, user, wallet, transaction, NFC card, vendor, settlement, and admin modules.
- Implement PostgreSQL schema for the PRD data models plus ledger/audit/idempotency tables.
- Add API contract tests and OpenAPI docs.
- Integrate OTP provider in staging.
- Integrate Paystack/partner-bank virtual account webhooks in sandbox.

### Phase 2: Secure mobile integration (2-4 weeks)

- Replace local demo repository with API-backed repository and offline queue abstractions.
- Implement PIN setup/login using server-side hashing and device-side secure verification material only where needed.
- Implement real NFC reader/card linking proof-of-concept using Android NFC APIs.
- Implement encrypted local transaction queue and sync worker.
- Add biometric login using BiometricPrompt and Keystore.

### Phase 3: Vendor terminal and offline engine (3-5 weeks)

- Implement vendor NFC reader flow, amount validation, terminal signing keys, encrypted queue, and batch sync.
- Build revocation-list/key-bundle refresh protocol.
- Implement sync conflict/error states and admin exception handling.
- Field-test with 5 drivers and controlled-value test wallets.

### Phase 4: Admin, settlement, and compliance (2-4 weeks)

- Build admin dashboard MVP.
- Implement settlement approval and payout workflow.
- Add dispute/refund workflow with audit logs.
- Add compliance exports and daily finance reconciliation.
- Complete privacy, terms, support SOPs, and incident response runbooks.

### Phase 5: QA, security, and pilot launch gate (2 weeks)

- Achieve minimum 80% backend coverage on wallet/transaction/token modules.
- Add Android unit tests for ViewModel/repository and instrumentation tests for critical flows.
- Add offline edge-case tests: airplane mode, duplicate sync, insufficient funds, PIN lockout, revoked card.
- Run load tests for target volume plus batch sync of 500 transactions.
- Complete penetration test and remediate critical/high issues.
- Launch only after sign-off from engineering, product, operations, finance, legal/regulatory, and security.

## Suggested launch gate checklist

- [ ] Partner bank/PSSP operating approval documented.
- [ ] Backend ledger deployed with database backups, monitoring, alerting, and runbooks.
- [ ] Admin dashboard supports activation, revocation, disputes, refunds, settlement, and audit logs.
- [ ] Real NFC card/sticker read/link/pay flow tested on target devices.
- [ ] Encrypted offline queue and sync deduplication tested.
- [ ] PINs are never stored or transmitted in plaintext.
- [ ] Release build uses production package, signing, shrinking, and backup/security settings.
- [ ] CI blocks failed tests/lint/security scans.
- [ ] Incident response and customer support playbooks are ready.
- [ ] Pilot wallets use strict transaction/offline limits until reconciliation is proven.

## Updated re-audit conclusion

The project has useful UX depth for demos, but the re-audit found no production-grade change to the highest-risk areas: no GitHub remote is configured in this environment for fetching external updates, no backend/API implementation is present locally, no real NFC/card-token layer is present, payment and settlement flows remain local simulations, and most wallet/payment state is still stored locally without the controls required for a financial product.

## Bottom line

Do not launch the current app for real payments. Use it as a visual and UX prototype, then build the backend, NFC/token security model, encrypted offline ledger, admin operations stack, and compliance controls required by the PRD. The shortest safe path is an Android-first pilot with a production backend and admin dashboard, controlled offline spending limits, and a staged rollout with test wallets before live student funds.
