package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.nfc.NfcAdapter
import android.util.Base64
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope

data class StudentUser(
    val id: String = "EP-0047",
    val name: String = "Joy Adaeze",
    val balance: Double = 4850.0,
    val phone: String = "+234 801 234 5678",
    val email: String = "joy.adaeze@babcock.edu.ng",
    val department: String = "Computer Science",
    val level: String = "400 Level"
)

data class VendorUser(
    val id: String = "EP-V-001",
    val name: String = "Musa Ibrahim",
    val todayEarnings: Double = 2100.0,
    val bankName: String = "GTBank",
    val accountNumber: String = "····4521"
)

data class Offer(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String
)

class EazyPayRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.transactionDao()
    private val prefs = try {
        val masterKeyAlias = androidx.security.crypto.MasterKeys.getOrCreate(androidx.security.crypto.MasterKeys.AES256_GCM_SPEC)
        val securePrefs = androidx.security.crypto.EncryptedSharedPreferences.create(
            "eazypay_secure_prefs",
            masterKeyAlias,
            context,
            androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        
        // Migrate legacy prefs if they exist
        val legacyPrefs = context.getSharedPreferences("eazypay_prefs", Context.MODE_PRIVATE)
        if (legacyPrefs.all.isNotEmpty() && !securePrefs.contains("migrated_to_secure")) {
            val editor = securePrefs.edit()
            for ((key, value) in legacyPrefs.all) {
                when (value) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                }
            }
            editor.putBoolean("migrated_to_secure", true)
            editor.apply()
            try {
                legacyPrefs.edit().clear().apply()
            } catch (ex: Exception) {
                // Ignore clear failure
            }
        }
        securePrefs
    } catch (e: Exception) {
        android.util.Log.e("EazyPayRepository", "Failed to initialize EncryptedSharedPreferences, falling back", e)
        try {
            context.deleteSharedPreferences("eazypay_secure_prefs")
            val masterKeyAlias = androidx.security.crypto.MasterKeys.getOrCreate(androidx.security.crypto.MasterKeys.AES256_GCM_SPEC)
            androidx.security.crypto.EncryptedSharedPreferences.create(
                "eazypay_secure_prefs",
                masterKeyAlias,
                context,
                androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (ex: Exception) {
            context.getSharedPreferences("eazypay_prefs", Context.MODE_PRIVATE)
        }
    }

    // Active Role state: "student" or "vendor"
    private val _currentRole = MutableStateFlow(prefs.getString("current_role", "student") ?: "student")
    val currentRole: StateFlow<String> = _currentRole

    // Registration state
    private val _isRegistered = MutableStateFlow(prefs.getBoolean("is_registered", false))
    val isRegistered: StateFlow<Boolean> = _isRegistered

    // Biometric payment authentication state
    private val _isBiometricEnabled = MutableStateFlow(prefs.getBoolean("is_biometric_enabled", false))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled

    // Registered physical NFC smart cards/stickers
    private val _registeredCards = MutableStateFlow(
        prefs.getString("registered_cards", "Main Student ID Card,Backup Payment Sticker")?.split(",")?.filter { it.isNotEmpty() } ?: listOf("Main Student ID Card", "Backup Payment Sticker")
    )
    val registeredCards: StateFlow<List<String>> = _registeredCards

    // Offline state
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline

    // Syncing state
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    // Student Wallet state
    private val _student = MutableStateFlow(
        StudentUser(
            id = prefs.getString("student_id", "EP-0047") ?: "EP-0047",
            name = prefs.getString("student_name", "Joy Adaeze") ?: "Joy Adaeze",
            balance = prefs.getFloat("student_balance", 4850.0f).toDouble(),
            phone = prefs.getString("student_phone", "+234 801 234 5678") ?: "+234 801 234 5678",
            email = prefs.getString("student_email", "joy.adaeze@babcock.edu.ng") ?: "joy.adaeze@babcock.edu.ng",
            department = prefs.getString("student_department", "Computer Science") ?: "Computer Science",
            level = prefs.getString("student_level", "400 Level") ?: "400 Level"
        )
    )
    val student: StateFlow<StudentUser> = _student

    // Vendor Earnings state
    private val _vendor = MutableStateFlow(
        VendorUser(
            id = prefs.getString("vendor_id", "EP-V-001") ?: "EP-V-001",
            name = prefs.getString("vendor_name", "Musa Ibrahim") ?: "Musa Ibrahim",
            todayEarnings = prefs.getFloat("vendor_earnings", 2100.0f).toDouble(),
            bankName = prefs.getString("vendor_bank", "GTBank") ?: "GTBank",
            accountNumber = prefs.getString("vendor_account", "····4521") ?: "····4521"
        )
    )
    val vendor: StateFlow<VendorUser> = _vendor

    // PIN verifier state. The raw PIN is never exposed through app state or persisted.
    private val _pinHash = MutableStateFlow(ensurePinHash())

    // Offers
    val offers = listOf(
        Offer("1", "Mama Tee's Kitchen", "Get ₦50 back on 🍲 rice & swallow", "food"),
        Offer("2", "Campus Print Hub", "10 pages free on 🖨️ assignment prints", "print"),
        Offer("3", "Flash Deal", "2% airtime bonus 🛜 on instant top-up", "topup")
    )

    // Transactions list flow from database
    val transactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()

    private var cachedKeyPair: KeyPair? = null

    fun getDeviceKeyPair(): KeyPair {
        cachedKeyPair?.let { return it }
        val pubBase64 = prefs.getString("device_pub_key", null)
        val privBase64 = prefs.getString("device_priv_key", null)
        if (pubBase64 != null && privBase64 != null) {
            return try {
                val pubBytes = Base64.decode(pubBase64, Base64.NO_WRAP)
                val privBytes = Base64.decode(privBase64, Base64.NO_WRAP)
                val pub = SecurityUtils.getPublicKeyFromBytes(pubBytes)
                val priv = SecurityUtils.getPrivateKeyFromBytes(privBytes)
                val pair = KeyPair(pub, priv)
                cachedKeyPair = pair
                pair
            } catch (e: Exception) {
                generateAndSaveNewKeyPair()
            }
        } else {
            return generateAndSaveNewKeyPair()
        }
    }

    private fun generateAndSaveNewKeyPair(): KeyPair {
        val pair = SecurityUtils.generateEcKeyPair()
        prefs.edit()
            .putString("device_pub_key", Base64.encodeToString(pair.public.encoded, Base64.NO_WRAP))
            .putString("device_priv_key", Base64.encodeToString(pair.private.encoded, Base64.NO_WRAP))
            .apply()
        cachedKeyPair = pair
        return pair
    }

    fun isNfcHardwareAvailable(context: Context): Boolean {
        return NfcAdapter.getDefaultAdapter(context) != null
    }

    fun isNfcHardwareEnabled(context: Context): Boolean {
        val adapter = NfcAdapter.getDefaultAdapter(context)
        return adapter?.isEnabled == true
    }

    init {
        // Initial seeding is performed in background
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            seedInitialData()
        }

        // Automatic Network Monitoring for auto-sync of pending transactions when connection restores
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            try {
                connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        // Auto-sync when internet returns and we are not explicitly forced offline
                        if (!_isOffline.value) {
                            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                syncPending()
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                // Safe fallback for standard JVM environments or restricted permissions
            }
        }
    }

    private fun ensurePinHash(): String {
        prefs.getString("pin_hash", null)?.let { return it }

        val legacyPlaintextPin = prefs.getString("user_pin", null)
        val initialPin = legacyPlaintextPin
            ?.takeIf(SecurityUtils::isValidFourDigitPin)
            ?: "1234"
        val hashedPin = SecurityUtils.hashPin(initialPin)

        prefs.edit()
            .putString("pin_hash", hashedPin)
            .remove("user_pin")
            .apply()

        return hashedPin
    }
    fun setRegistered(registered: Boolean, phone: String? = null, role: String? = null) {
        val editor = prefs.edit().putBoolean("is_registered", registered)
        if (role != null) {
            editor.putString("current_role", role)
            _currentRole.value = role
        }
        if (phone != null) {
            val formattedPhone = "+234 " + phone.removePrefix("+234").trim()
            if (role == "vendor") {
                val currentVendor = _vendor.value
                val newVendor = currentVendor.copy(id = "EP-V-" + phone.takeLast(4))
                _vendor.value = newVendor
                editor.putString("vendor_id", newVendor.id)
            } else {
                val currentStudent = _student.value
                val newStudent = currentStudent.copy(phone = formattedPhone, id = "EP-" + phone.takeLast(4))
                _student.value = newStudent
                editor.putString("student_phone", formattedPhone)
                editor.putString("student_id", newStudent.id)
            }
        }
        editor.apply()
        _isRegistered.value = registered
    }

    fun setBiometricEnabled(enabled: Boolean) {
        _isBiometricEnabled.value = enabled
        prefs.edit().putBoolean("is_biometric_enabled", enabled).apply()
    }

    fun addNfcCard(cardName: String) {
        val current = _registeredCards.value.toMutableList()
        current.add(cardName)
        _registeredCards.value = current
        prefs.edit().putString("registered_cards", current.joinToString(",")).apply()
    }

    fun removeNfcCard(cardName: String) {
        val current = _registeredCards.value.toMutableList()
        current.remove(cardName)
        _registeredCards.value = current
        prefs.edit().putString("registered_cards", current.joinToString(",")).apply()
    }

    fun updateStudentDetails(name: String, email: String, phone: String, department: String, level: String) {
        val updated = _student.value.copy(
            name = name,
            email = email,
            phone = phone,
            department = department,
            level = level
        )
        _student.value = updated
        prefs.edit()
            .putString("student_name", name)
            .putString("student_email", email)
            .putString("student_phone", phone)
            .putString("student_department", department)
            .putString("student_level", level)
            .apply()
    }

    fun updateVendorBankDetails(bankName: String, accountNumber: String) {
        val updated = _vendor.value.copy(
            bankName = bankName,
            accountNumber = accountNumber
        )
        _vendor.value = updated
        prefs.edit()
            .putString("vendor_bank", bankName)
            .putString("vendor_account", accountNumber)
            .apply()
    }

    fun setRole(role: String) {
        _currentRole.value = role
        prefs.edit().putString("current_role", role).apply()
    }

    fun setOffline(offline: Boolean) {
        _isOffline.value = offline
    }

    suspend fun setSyncing(syncing: Boolean) {
        _isSyncing.value = syncing
    }

    fun setPin(pin: String) {
        val hashedPin = SecurityUtils.hashPin(pin)
        _pinHash.value = hashedPin
        prefs.edit()
            .putString("pin_hash", hashedPin)
            .remove("user_pin")
            .apply()
    }

    fun verifyPin(pin: String): Boolean = SecurityUtils.verifyPin(pin, _pinHash.value)
    suspend fun topUpWallet(amount: Double) {
        val currentStudent = _student.value
        val updatedStudent = currentStudent.copy(balance = currentStudent.balance + amount)
        _student.value = updatedStudent
        prefs.edit().putFloat("student_balance", updatedStudent.balance.toFloat()).apply()
        
        // Add top-up transaction
        val status = if (_isOffline.value) "Pending" else "Synced"
        addTransaction(
            title = "Wallet Top-up",
            category = "topup",
            amount = amount,
            isDebit = false,
            status = status
        )
    }

    suspend fun addTransaction(
        title: String,
        category: String,
        amount: Double,
        isDebit: Boolean,
        status: String = if (_isOffline.value) "Pending" else "Synced",
        payerId: String = if (isDebit) _student.value.id else "",
        payeeId: String = if (!isDebit) _vendor.value.id else "",
        deviceId: String = "DEV-BU-${_student.value.id.takeLast(4)}",
        nfcCardId: String = "NFC-BU-10482",
        fee: Double = if (isDebit && category != "topup") 10.0 else 0.0, // ₦10 standard fee
        campusId: String = "Babcock-Main"
    ) {
        val lastTxList = dao.getAllTransactions().first()
        val prevHash = lastTxList.firstOrNull()?.hash ?: "GENESIS"
        val timestamp = System.currentTimeMillis()
        val hash = SecurityUtils.calculateHash(prevHash, title, amount, timestamp)
        
        // Let's sign the payload: "title|amount|timestamp|isDebit"
        val payload = "$title|$amount|$timestamp|$isDebit"
        val keyPair = getDeviceKeyPair()
        val signature = SecurityUtils.signPayload(payload, keyPair.private)

        val txRef = "TXN-${category.uppercase()}-${timestamp}-${(1000..9999).random()}"
        val idempotencyKey = java.util.UUID.randomUUID().toString()

        dao.insertTransaction(
            TransactionEntity(
                title = title,
                category = category,
                timestamp = timestamp,
                amount = amount,
                isDebit = isDebit,
                syncStatus = status,
                hash = hash,
                prevHash = prevHash,
                signature = signature,
                txRef = txRef,
                payerId = payerId,
                payeeId = payeeId,
                deviceId = deviceId,
                nfcCardId = nfcCardId,
                fee = fee,
                campusId = campusId,
                idempotencyKey = idempotencyKey
            )
        )

        // Adjust student balance or vendor earnings locally
        if (isDebit) {
            val currentStudent = _student.value
            val updatedStudent = currentStudent.copy(balance = currentStudent.balance - amount)
            _student.value = updatedStudent
            prefs.edit().putFloat("student_balance", updatedStudent.balance.toFloat()).apply()
        } else {
            if (category != "topup") { // Received money as payment
                val currentVendor = _vendor.value
                val updatedVendor = currentVendor.copy(todayEarnings = currentVendor.todayEarnings + amount)
                _vendor.value = updatedVendor
                prefs.edit().putFloat("vendor_earnings", updatedVendor.todayEarnings.toFloat()).apply()
            }
        }
    }

    suspend fun performNfcPayment(vendorName: String, amount: Double, isStudentDebit: Boolean) {
        val status = if (_isOffline.value) "Pending" else "Synced"
        if (isStudentDebit) {
            // Deduct from student balance
            addTransaction(
                title = vendorName,
                category = "food", // default
                amount = amount,
                isDebit = true,
                status = status
            )
        } else {
            // Add to vendor earnings
            addTransaction(
                title = "Payment received",
                category = "food",
                amount = amount,
                isDebit = false,
                status = status
            )
        }
    }

    suspend fun triggerWithdrawal(amount: Double): Boolean {
        val currentVendor = _vendor.value
        if (currentVendor.todayEarnings >= amount) {
            val updatedVendor = currentVendor.copy(todayEarnings = currentVendor.todayEarnings - amount)
            _vendor.value = updatedVendor
            prefs.edit().putFloat("vendor_earnings", updatedVendor.todayEarnings.toFloat()).apply()
            
            // Record a withdrawal transaction as debit for vendor
            addTransaction(
                title = "Bank Withdrawal",
                category = "topup",
                amount = amount,
                isDebit = true,
                status = if (_isOffline.value) "Pending" else "Synced"
            )
            return true
        }
        return false
    }

    suspend fun verifyLocalLedgerIntegrity(): Boolean {
        val list = dao.getAllTransactions().first().reversed()
        if (list.isEmpty()) return true
        
        var expectedPrevHash = "GENESIS"
        val keyPair = getDeviceKeyPair()
        
        for (tx in list) {
            // 1. Verify prevHash matches what we expect
            if (tx.prevHash != expectedPrevHash) {
                return false
            }
            // 2. Verify hash is correct
            val calculatedHash = SecurityUtils.calculateHash(tx.prevHash, tx.title, tx.amount, tx.timestamp)
            if (tx.hash != calculatedHash) {
                return false
            }
            // 3. Verify signature
            val payload = "${tx.title}|${tx.amount}|${tx.timestamp}|${tx.isDebit}"
            val signatureValid = SecurityUtils.verifyPayload(payload, tx.signature, keyPair.public)
            if (!signatureValid) {
                return false
            }
            // 4. Move expectedPrevHash forward
            expectedPrevHash = tx.hash
        }
        return true
    }

    suspend fun tamperLastTransaction(): Boolean {
        val lastTx = dao.getLastTransaction() ?: return false
        // Maliciously alter transaction details directly in DB without correct signatures/hashes
        val tamperedTx = lastTx.copy(amount = lastTx.amount + 5000.0)
        dao.insertTransaction(tamperedTx)
        return true
    }

    suspend fun repairLedgerIntegrity(): Boolean {
        val list = dao.getAllTransactions().first().reversed()
        if (list.isEmpty()) return true
        
        var expectedPrevHash = "GENESIS"
        val keyPair = getDeviceKeyPair()
        
        for (tx in list) {
            val correctHash = SecurityUtils.calculateHash(expectedPrevHash, tx.title, tx.amount, tx.timestamp)
            val payload = "${tx.title}|${tx.amount}|${tx.timestamp}|${tx.isDebit}"
            val correctSig = SecurityUtils.signPayload(payload, keyPair.private)
            
            val repairedTx = tx.copy(
                prevHash = expectedPrevHash,
                hash = correctHash,
                signature = correctSig
            )
            dao.insertTransaction(repairedTx)
            expectedPrevHash = correctHash
        }
        return true
    }

    suspend fun getOfflineSpentCumulative(): Double {
        val allTx = dao.getAllTransactions().first()
        return allTx.filter { it.syncStatus == "Pending" && it.isDebit }.sumOf { it.amount }
    }

    fun getOfflineSpendCeiling(): Double = 5000.0

    suspend fun syncPending() {
        if (_isOffline.value) return
        _isSyncing.value = true
        // Simulate networking delay
        kotlinx.coroutines.delay(1500)
        dao.syncPendingTransactions()
        _isSyncing.value = false
    }

    private suspend fun seedInitialData() {
        // Only seed if empty
        val list = dao.getAllTransactions().first()
        if (list.isEmpty()) {
            val keyPair = getDeviceKeyPair()
            
            val t1Title = "Wallet top-up"
            val t1Amt = 2000.0
            val t1Time = System.currentTimeMillis() - 7200000
            val t1Prev = "GENESIS"
            val t1Hash = SecurityUtils.calculateHash(t1Prev, t1Title, t1Amt, t1Time)
            val t1Sig = SecurityUtils.signPayload("$t1Title|$t1Amt|$t1Time|false", keyPair.private)
            dao.insertTransaction(
                TransactionEntity(
                    title = t1Title,
                    category = "topup",
                    timestamp = t1Time,
                    amount = t1Amt,
                    isDebit = false,
                    syncStatus = "Synced",
                    hash = t1Hash,
                    prevHash = t1Prev,
                    signature = t1Sig
                )
            )
            
            val t2Title = "Mama Tee's Kitchen"
            val t2Amt = 650.0
            val t2Time = System.currentTimeMillis() - 3600000
            val t2Prev = t1Hash
            val t2Hash = SecurityUtils.calculateHash(t2Prev, t2Title, t2Amt, t2Time)
            val t2Sig = SecurityUtils.signPayload("$t2Title|$t2Amt|$t2Time|true", keyPair.private)
            dao.insertTransaction(
                TransactionEntity(
                    title = t2Title,
                    category = "food",
                    timestamp = t2Time,
                    amount = t2Amt,
                    isDebit = true,
                    syncStatus = "Synced",
                    hash = t2Hash,
                    prevHash = t2Prev,
                    signature = t2Sig
                )
            )
            
            val t3Title = "Keke transport"
            val t3Amt = 210.0
            val t3Time = System.currentTimeMillis() - 1800000
            val t3Prev = t2Hash
            val t3Hash = SecurityUtils.calculateHash(t3Prev, t3Title, t3Amt, t3Time)
            val t3Sig = SecurityUtils.signPayload("$t3Title|$t3Amt|$t3Time|true", keyPair.private)
            dao.insertTransaction(
                TransactionEntity(
                    title = t3Title,
                    category = "transport",
                    timestamp = t3Time,
                    amount = t3Amt,
                    isDebit = true,
                    syncStatus = "Synced",
                    hash = t3Hash,
                    prevHash = t3Prev,
                    signature = t3Sig
                )
            )
        }
    }
}
