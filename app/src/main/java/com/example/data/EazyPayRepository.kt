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
    val phone: String = "+234 801 234 5678"
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

    // Active Role state: "student" or "vendor"
    private val _currentRole = MutableStateFlow("student")
    val currentRole: StateFlow<String> = _currentRole

    // Offline state
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline

    // Syncing state
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    // Student Wallet state
    private val _student = MutableStateFlow(StudentUser())
    val student: StateFlow<StudentUser> = _student

    // Vendor Earnings state
    private val _vendor = MutableStateFlow(VendorUser())
    val vendor: StateFlow<VendorUser> = _vendor

    // PIN State
    private val _userPin = MutableStateFlow("1234") // Default mock pin
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

    fun setRole(role: String) {
        _currentRole.value = role
    }

    fun setOffline(offline: Boolean) {
        _isOffline.value = offline
    }

    suspend fun setSyncing(syncing: Boolean) {
        _isSyncing.value = syncing
    }

    fun setPin(pin: String) {
        _userPin.value = pin
    }

    suspend fun topUpWallet(amount: Double) {
        val currentStudent = _student.value
        _student.value = currentStudent.copy(balance = currentStudent.balance + amount)
        
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
            _student.value = currentStudent.copy(balance = currentStudent.balance - amount)
        } else {
            if (category != "topup") { // Received money as payment
                val currentVendor = _vendor.value
                _vendor.value = currentVendor.copy(todayEarnings = currentVendor.todayEarnings + amount)
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
            _vendor.value = currentVendor.copy(todayEarnings = currentVendor.todayEarnings - amount)
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
