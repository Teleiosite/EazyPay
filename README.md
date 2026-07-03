# EazyPay ⚡

EazyPay is an **offline-first, NFC-enabled campus cashless payment platform** designed for educational environments like Babcock University. It solves the critical challenge of network instability and connection failures in high-traffic campus ecosystems (cafeterias, print shops, transit hubs) using a secure local-ledger architecture.

---


## 📦 Downloadable Demo Build

EazyPay is currently safe to share as a **demo/prototype APK** for reviewers and stakeholders. It is **not** a live-money payment app.

To build the shareable internal demo APK from a JDK 17 Android build environment:

```bash
./scripts/build_share_apk.sh
```

The script copies the debug APK and SHA-256 checksum to:

```text
dist/EazyPay-demo-debug.apk
dist/EazyPay-demo-debug.apk.sha256
```

See [`DISTRIBUTION.md`](DISTRIBUTION.md) for release-signing instructions and distribution safety notes.

---

## 📊 Project Status & Implementation Progress

Below is the status of the EazyPay Android payment applet:

### ✅ What We Have Achieved (Completed Milestones)
*   **Secure Local Storage (AES-256)**: Replaced unencrypted shared preferences with AndroidX `EncryptedSharedPreferences`. Built an automated data migration routine to clean legacy plain-text values safely.
*   **Physical Biometrics diagnostics**: Connected native Android `BiometricManager` API to query biometric hardware availability and enrollment status, reporting status transparently in user settings.
*   **Cryptographic Ledger Integrity (SHA-256)**: Implemented an append-only transaction hash chain validator (`verifyLocalLedgerIntegrity()`). It recursively checks block continuity and validates ECDSA signatures against active device keypairs.
*   **Database Schema Hardening (Room v2)**: Re-architected and migrated the Room schema to Version 2. Standardized audit metrics like unique references (`txRef`), automatic campus fees, device/card ID binding, and UUID `idempotencyKeys`.
*   **Database Tampering & Recovery Panel**: Created an interactive developer **Ledger Security Audit** panel. Users can trigger an intentional database injection attack ("Tamper DB") to watch the system fail and lock terminal payments, then trigger a cryptographic "Repair Chain" to rebuild block trust and unlock payment operations.
*   **Offline Spending Limit (₦5,000 Cap)**: Implemented cumulative offline spending limits inside the payment loop. Contactless tap payments are securely blocked once the threshold is exceeded until the device reconnects and syncs.
*   **Rich Auditable Receipts**: Updated student and vendor receipt modal screens to present cryptographic hashes, signatures, location tags, and terminal references.

### 📋 What Remains (Production Release Roadmap)
*   **Authoritative Backend Service**: Move from purely local-state settlement to a centralized relational database double-entry bookkeeping backend acting as the supreme source of truth.
*   **Host Card Emulation (HCE)**: Transition from mock NFC terminal handshakes to authentic Android HCE and ISO 14443-4 physical contactless protocols.
*   **SQLCipher Database Encryption**: Secure the Room SQLite database file at-rest using SQLCipher rather than standard Android database stores.
*   **Payment Gateway APIs**: Integrate actual card and bank transfer processors (e.g., Paystack) to authorize and move real-world funds.
*   **Administrative Operations Web Console**: Build an operations dashboard to configure terminals, issue hardware cards, manage disputes, and track real-time network health.

---

## 🔒 Production Hardening & Recent Engineering Updates

To transition EazyPay from a high-fidelity visual prototype to a secure, resilient, and enterprise-grade payment system, we have completed the following critical security and architectural implementations:

### 1. Secure Local Storage Engine (AES-256 Encryption)
*   **Encrypted SharedPreferences**: Replaced legacy plain-text preferences with `androidx.security:security-crypto` (EncryptedSharedPreferences). This ensures sensitive offline parameters, device keys, user roles, and biometric preferences are encrypted at rest on the physical disk using AES-256-SIV (for keys) and AES-256-GCM (for values).
*   **Automatic Data Migration**: Implemented a resilient migration wrapper. On app startup, the repository checks for existing plain-text keys, transfers them securely to the encrypted engine, and purges the legacy store to prevent credential exposure.

### 2. Physical Biometrics API Integration
*   **Native Diagnostics**: Integrated the Android `BiometricManager` API to perform real-time security capability checks. The app now directly Queries the system to determine hardware availability and fingerprint enrollment status.
*   **Dynamic Visual Feedback**: Injected these physical diagnostics into the Student Profile's Security panel. If biometric hardware is found, it reports "Hardware Active & Enrolled"; otherwise, it gracefully displays an explanatory "Hardware Not Found (Simulation Active)" badge.
*   **Permissions**: Registered the mandatory `USE_BIOMETRIC` permission inside the `AndroidManifest.xml` to grant native hardware access.

### 3. SHA-256 Cryptographic Ledger Integrity Check
*   **ECDSA-SHA256 Blockchain Verification**: Built a complete, recursive local hash-chain verification protocol (`verifyLocalLedgerIntegrity()`). The validator walks the entire Room transaction table from genesis to verify:
    1.  **Block Continuity**: Current transaction `prevHash` exactly matches the parent block's `hash`.
    2.  **Payload Authenticity**: Re-calculates and asserts the SHA-256 digest of transaction parameters.
    3.  **Digital Signatures**: Validates the cryptographic ECDSA signature generated by the device's private-public key pair against the structured payload (`title|amount|timestamp|isDebit`).
*   **Real-time Ledger Security Status**: Exposed an active, dynamic "Ledger Integrity Protocol" visualizer on both the Student and Vendor dashboard screens. It dynamically updates to a high-contrast teal-green color (`Success`) when the database chain is intact, or displays a red warning (`Danger`) if any data tampering is detected.

### 4. Expanded Transaction Audit & Compliance Schema
*   **Database Schema Migration (Room v2)**: Upgraded `AppDatabase` to Version 2 to support modern auditing and tracking features.
*   **Production Audit Parameters**: Expanded `TransactionEntity` with standard transaction auditing parameters:
    *   `txRef`: Unique transaction reference string tracking the category, timestamp, and randomizer.
    *   `fee`: Automatic calculation of campus payment fees (₦10 flat fee for student debit transactions).
    *   `payerId` & `payeeId`: IDs linking specific students and merchant terminals.
    *   `deviceId`: Bound physical terminal identification tag.
    *   `nfcCardId`: Card antenna hardware token.
    *   `idempotencyKey`: Unique UUID ensuring double-billing prevention and duplicate sync rejection.
    *   `campusId`: Physical station location tag (e.g., "Babcock-Main").

### 5. Interactive Database Ledger Tampering & Repair Console
*   **Security Attack Simulation**: Implemented a fully functional developer/auditor panel in the Student Settings called **Ledger Security Audit**.
*   **Malicious Tampering Tool**: Includes a "Tamper DB" trigger that intentionally mutates transaction data directly in the SQLite Room database without recomputing the cryptographic chain or re-signing, simulating an on-device data injection.
*   **Live Attack Alert**: Once tampered, the app's hash-chain checks instantly fail. The system enters a compromised state, turning the visual indicators to pulsing Danger Red and locking the active payment terminal to block subsequent merchant charges.
*   **Cryptographic Repair Tool**: Includes a "Repair Chain" trigger that processes a full block reconstruction and re-signature, restoring the green "Secure" state and unlocking terminal tap operations.

### 6. Offline Spending Limits (₦5,000 Security Ceiling)
*   **Risk Mitigation**: Enforced a cumulative ₦5,000 ceiling on offline transactions in the core payment VM loop.
*   **Real-time Spending Checks**: If a student is offline, subsequent contactless payments check the sum of all local `Pending` records. Exceeding the ₦5,000 ceiling immediately blocks payment and displays a descriptive warning: "Offline limit exceeded (Max ₦5,000). Reconnect to sync."

### 7. High-Fidelity Audit Receipts
*   **Interactive Receipts**: Enhanced the Student `TransactionReceiptModal` and Vendor `VendorSettlementReceiptModal` layouts. They now bind directly to the new database schema, rendering actual Transaction IDs, merchant terminal reference tags, system fees, campus location names, and precise signature cryptographic standards.

---

## 📋 Fit-Gap Assessment & Remaining Production Roadmap

While these updates successfully secure the local database, provide hardware cryptographic validation, and record full audit logs, the following engineering phases must be completed before launching a live-money pilot:

### 1. Authoritative Backend Ledger (Highest Priority)
*   **Current State**: The wallet balance and transaction status transitions (`Pending` to `Synced`) are managed locally.
*   **Production Requirement**: Build a centralized, robust backend service (e.g., Spring Boot or NestJS) with a PostgreSQL relational database. The backend must act as the absolute source of truth for double-entry ledger bookkeeping. Local device balances must be treated only as cached authorizations, not authoritative funds.

### 2. Real NFC & Card-Token Issuance
*   **Current State**: NFC transaction exchange, card reading, and tap handshakes are simulated inside the Compose views.
*   **Production Requirement**: Integrate Android Host Card Emulation (HCE) and physical NFC card reader APIs. Develop a secure card personalization service to write signed authorization tokens to contactless cards, allowing the terminal to verify the student's credentials completely offline.

### 3. Encrypted Local Queue & Offline Sync
*   **Current State**: Transactions are stored in a standard local SQLite database (Room).
*   **Production Requirement**: Encrypt the Room database using SQLCipher. Implement an append-only, tamper-proof offline queue that queues transactions in a secure state until a network connection is detected, at which point an idempotent sync worker processes batch settlements on the server.

### 4. Real-World Payment Gateways
*   **Current State**: Top-ups and bank withdrawals modify local float figures instantly.
*   **Production Requirement**: Integrate registered payment service providers (e.g., Paystack) for online card top-ups, secure automated virtual accounts for direct bank transfers, and real NIBSS Instant Payment (NIP) integrations to handle vendor bank withdrawals.

### 5. Admin & Operations Control Stack
*   **Current State**: No administrative dashboard is present.
*   **Production Requirement**: Build a secure web-based Admin Console. This is mandatory for operational tasks, including card issuance, lost card suspension, manual dispute resolution, settlement reconciliation reports, and terminal telemetry monitoring.

---

## 🛠️ Tech Stack & Verification Status

*   **Language**: Kotlin (100% Type-Safe)
*   **UI Framework**: Jetpack Compose (Material Design 3)
*   **Local DB**: Room Database (Schema v2)
*   **Security Library**: AndroidX Security Crypto & AndroidX Biometrics API
*   **Status**: Verified compiled. All local modules, cryptographic signers, and encrypted shared preferences are fully operational.

---

Built with ⚡ for Babcock University and Babcock Campus Cashless Operations.  
© 2026 EazyPay Platform Services. All Rights Reserved.
