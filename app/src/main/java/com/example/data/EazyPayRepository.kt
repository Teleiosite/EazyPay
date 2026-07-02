package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val prefs = context.getSharedPreferences("eazypay_prefs", Context.MODE_PRIVATE)

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

    // PIN State
    private val _userPin = MutableStateFlow(prefs.getString("user_pin", "1234") ?: "1234")
    val userPin: StateFlow<String> = _userPin

    // Offers
    val offers = listOf(
        Offer("1", "Mama Tee's Kitchen", "Get ₦50 back on 🍲 rice & swallow", "food"),
        Offer("2", "Campus Print Hub", "10 pages free on 🖨️ assignment prints", "print"),
        Offer("3", "Flash Deal", "2% airtime bonus 🛜 on instant top-up", "topup")
    )

    // Transactions list flow from database
    val transactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()

    init {
        // Initial seeding is performed in background
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            seedInitialData()
        }
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
        _userPin.value = pin
        prefs.edit().putString("user_pin", pin).apply()
    }

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
        status: String = if (_isOffline.value) "Pending" else "Synced"
    ) {
        dao.insertTransaction(
            TransactionEntity(
                title = title,
                category = category,
                timestamp = System.currentTimeMillis(),
                amount = amount,
                isDebit = isDebit,
                syncStatus = status
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
            dao.insertTransaction(
                TransactionEntity(
                    title = "Wallet top-up",
                    category = "topup",
                    timestamp = System.currentTimeMillis() - 7200000, // 2 hours ago
                    amount = 2000.0,
                    isDebit = false,
                    syncStatus = "Synced"
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    title = "Mama Tee's Kitchen",
                    category = "food",
                    timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                    amount = 650.0,
                    isDebit = true,
                    syncStatus = "Synced"
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    title = "Keke transport",
                    category = "transport",
                    timestamp = System.currentTimeMillis() - 1800000, // 30 mins ago
                    amount = 210.0,
                    isDebit = true,
                    syncStatus = "Synced"
                )
            )
        }
    }
}
