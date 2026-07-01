# EazyPay ⚡

EazyPay is an **offline-first, NFC-enabled campus cashless payment platform** designed for educational institutions like Babcock University. It solves the critical challenge of network instability and transaction failures in crowded campus environments (cafeterias, print shops, transport hubs) by using **secure offline cryptographic ledgers** and **contactless smart verification**. 

The application utilizes local secure hardware emulation to verify balances and sign transactions completely offline, synchronizing securely with the central bank settlement system once connection is restored.

---

## 📸 Key Features

### 🧑‍🎓 Student Experience
*   **Aesthetic Smart Wallet Card**:
    *   One-tap toggle to show/hide wallet balances.
    *   Integrated **Offline vs. Online Sync** status switcher directly inside the card container.
    *   Real-time pulsing "Ready for NFC Tap" indicator simulating physical antenna readiness.
    *   **Low Balance Alert Banner**: Proactive notifications when the wallet drops below ₦500.00 to prevent offline terminal rejections.
*   **Rich Analytics & historic Ledger**:
    *   Visual horizontal progress breakdown representing monthly disbursement patterns across custom campus categories (**Food**, **Transport**, **Print**).
    *   Comprehensive filter matrix allowing categorization by status (All, Paid, Received, Pending) and category (All, Food, Transport, Print, Top-Up).
    *   **Cryptographic Transaction Receipts**: Deep-dive receipt details verifying transaction IDs, security standards, category markings, and offline authentication stamps.
    *   **Dispute Center**: Raise instant transaction flags for administrator review with interactive offline ledger marking.
*   **Babcock Support Hub**:
    *   Offline FAQ dictionary explaining EazyPay protocol details.
    *   Interactive **Live Chat Simulator** with instant simulated responses from EazyPay Customer Support.
    *   WhatsApp direct link details.
*   **Simulated Contactless Pay**:
    *   Pulsing NFC antenna simulation ring for contactless terminal handshakes.
    *   Dynamic payment checks (e.g., minimum campus balance of ₦10, transaction threshold warnings).
    *   **Cooldown Lockout Security**: Automatically freezes device payment ability after 3 consecutive incorrect PIN entries, with a simulation for Admin Bypass Unlock.

### 🏪 Vendor Terminal Experience
*   **NFC Receiver Antenna Simulator**:
    *   Allows toggling the physical NFC Antenna hardware on/off to mock real-world transceiver power states.
    *   Active EMV polling indicators ensuring secure, compliant transaction handshakes.
*   **Terminal Collections Log & Earnings**:
    *   **Settlement Sync Tracker**: Dynamic status displaying settled bank collections vs. pending sync (offline accumulated) funds.
    *   **Performance Metrics Dashboard**: Displays Average Ticket Value, Busiest Rush Hour metrics, and Peak Purchasing Categories.
    *   **Detailed Vendor Receipts**: Secure collection receipts embedded with ECDSA signatures, linked SECURE-CHIP serials, and cafeteria location markers.

---

## 🔒 Cryptographic Offline Security Protocol

EazyPay does not require active internet connection at point-of-sale terminals because it replicates physical secure-element card logic:

```
[Student Wallet]                                [Vendor Terminal]
       │                                               │
       │ ─── 1. Physical Contact Tap (NFC Emulated) ──>│
       │                                               │
       │ <── 2. Request Signed Cryptographic Token ────│
       │                                               │
       │ ─── 3. Auth with Secure 4-Digit PIN ────────>│
       │                                               │
       │ ─── 4. Transfer Signed Ledger Token ─────────>│ (Terminal verifies offline using ECDSA-secp256k1)
       │                                               │
       │ <── 5. Payment Completed (Receipt Logged) ────│
```

1.  **Signed Ledger Token**: At registration, the student device receives a central authority cryptographic token containing authorization keys, encrypted balance thresholds, and user profiles.
2.  **ECDSA secp256k1 Authentication**: When a payment occurs offline, the student app signs the transaction details using a unique private key.
3.  **Terminal-Side Public Key Verification**: The vendor terminal (which stores the Babcock Central Key registry offline) verifies the signature and records the purchase, deducting the amount from the offline balance.
4.  **AES-256 GCM Local Logs**: All pending logs on both student and vendor terminals are stored locally inside database tables using military-grade AES-256 GCM encryption parameters.

---

## 🛠️ Technical Architecture

EazyPay is built using state-of-the-art native Android and Jetpack Compose practices:

*   **UI Framework**: **Jetpack Compose** (Material Design 3) with dynamic light and dark color scheme compliance, customized layouts with generous padding, and adaptive design components.
*   **State Management**: **ViewModel** coupled with reactive **Kotlin Coroutines** and **StateFlow / Flow** streams.
*   **Local Persistence**: **Room Database** containing persistent relational tables for students, vendors, and transactions.
*   **Animations**: Custom Composable animations (pulsing NFC rings, segmented bar scales, dynamic sliding modals, state fade-ins) to enrich user delight.
*   **System Integrity**: Complete separation of status bars (`statusBarsPadding`), edge-to-edge screens (`enableEdgeToEdge()`), and clean view boundaries.

---

## 📂 Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt               # Base Activity initiating Edge-to-Edge Layout & Nav Graph
├── data/                         # Local Room Entities, DAOs, and Database migrations
├── ui/
│   ├── theme/                    # Material 3 typography, custom color schemes, shapes
│   └── screens/
│       ├── OnboardingScreens.kt  # User setup, simulated chip linking, and tutorial
│       ├── StudentScreens.kt     # Student dashboard, Support Hub, and History Ledger
│       ├── VendorScreens.kt      # Terminal scanning, earnings dashboard, and metrics
│       ├── DemoScreens.kt        # Combined system test interface & controller
│       └── Components.kt         # Reusable atomic items (Receipt modals, custom cards)
```

---

## 🚀 How to Run the App

1.  **Prerequisites**:
    *   Android Studio Ladybug (or higher)
    *   Java Development Kit (JDK) 17 or higher
    *   Android SDK Platform 34
2.  **Clone & Sync**:
    *   Open the project folder inside Android Studio.
    *   Sync Gradle files to resolve modern libraries.
3.  **Build and Run**:
    *   Connect your Android Device or spin up an Emulator.
    *   Run the project using `Shift + F10` or click the **Run** button.
4.  **Simulate Offline Handshake**:
    *   Toggle the app into **Offline Mode** via the Connection Switcher.
    *   Launch the Vendor Terminal Screen and turn on the **NFC Hardware Antenna**.
    *   Simulate a purchase to witness cryptographic verification with zero network connection!

---

## 🛡️ License

Built with ⚡ for Babcock University and Babcock Campus Cashless Operations. 
© 2026 EazyPay Platform Services. All Rights Reserved.
