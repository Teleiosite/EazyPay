package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SupportChatMessage(
    val sender: String, // "User" or "Agent"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class EazyPayViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EazyPayRepository(application)

    val currentRole = repository.currentRole
    val isRegistered = repository.isRegistered
    val isBiometricEnabled = repository.isBiometricEnabled
    val registeredCards = repository.registeredCards
    val isOffline = repository.isOffline
    val isSyncing = repository.isSyncing
    val student = repository.student
    val vendor = repository.vendor
    val offers = repository.offers
    val transactions = repository.transactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Interactive Demo / Payment States
    private val _demoActive = MutableStateFlow(false)
    val demoActive: StateFlow<Boolean> = _demoActive

    private val _demoStep = MutableStateFlow(1) // 1: Tap, 2: ID Read, 3: Amount, 4: PIN, 5: Confirmed
    val demoStep: StateFlow<Int> = _demoStep

    // Active Vendor Terminal State
    // 1: Waiting, 2: Card Detected, 3: Confirming student PIN, 4: Payment Received
    private val _terminalState = MutableStateFlow(1)
    val terminalState: StateFlow<Int> = _terminalState

    private val _terminalAmount = MutableStateFlow("200")
    val terminalAmount: StateFlow<String> = _terminalAmount

    private val _terminalStudent = MutableStateFlow<StudentUser?>(null)
    val terminalStudent: StateFlow<StudentUser?> = _terminalStudent

    private val _pinBuffer = MutableStateFlow("")
    val pinBuffer: StateFlow<String> = _pinBuffer

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError

    // Production-Ready PIN attempts, lockouts, support chat and dispute states
    private val _pinAttemptsRemaining = MutableStateFlow(3)
    val pinAttemptsRemaining: StateFlow<Int> = _pinAttemptsRemaining

    private val _isLockedOut = MutableStateFlow(false)
    val isLockedOut: StateFlow<Boolean> = _isLockedOut

    private val _disputedTransactions = MutableStateFlow<Set<Int>>(emptySet())
    val disputedTransactions: StateFlow<Set<Int>> = _disputedTransactions

    private val _chatMessages = MutableStateFlow<List<SupportChatMessage>>(listOf(
        SupportChatMessage("Agent", "Hello! Welcome to EazyPay Babcock Support. How can we help you today?")
    ))
    val chatMessages: StateFlow<List<SupportChatMessage>> = _chatMessages

    private var demoJob: Job? = null

    fun setRole(role: String) {
        viewModelScope.launch {
            repository.setRole(role)
        }
    }

    fun setRegistered(registered: Boolean, phone: String? = null, role: String? = null) {
        repository.setRegistered(registered, phone, role)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        repository.setBiometricEnabled(enabled)
    }

    fun addNfcCard(cardName: String) {
        repository.addNfcCard(cardName)
    }

    fun removeNfcCard(cardName: String) {
        repository.removeNfcCard(cardName)
    }

    fun updateStudentDetails(name: String, email: String, phone: String, department: String, level: String) {
        repository.updateStudentDetails(name, email, phone, department, level)
    }

    fun toggleOffline() {
        viewModelScope.launch {
            val nextOffline = !isOffline.value
            repository.setOffline(nextOffline)
            if (!nextOffline) {
                // RESTORED CONNECTION -> Auto-sync!
                repository.syncPending()
            }
        }
    }

    fun topUpWallet(amount: Double) {
        viewModelScope.launch {
            repository.topUpWallet(amount)
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            repository.setPin(pin)
        }
    }

    fun verifyPin(pin: String): Boolean = repository.verifyPin(pin)

    fun appendPinChar(char: Char, onPinComplete: () -> Unit) {
        if (_isLockedOut.value) return
        if (_pinBuffer.value.length < 4) {
            _pinBuffer.value = _pinBuffer.value + char
            if (_pinBuffer.value.length == 4) {
                verifyPinAndExecute(onPinComplete)
            }
        }
    }

    fun deletePinChar() {
        if (_isLockedOut.value) return
        if (_pinBuffer.value.isNotEmpty()) {
            _pinBuffer.value = _pinBuffer.value.dropLast(1)
        }
    }

    fun resetPinAttempts() {
        _pinAttemptsRemaining.value = 3
        _isLockedOut.value = false
        _pinBuffer.value = ""
    }

    fun disputeTransaction(id: Int) {
        _disputedTransactions.value = _disputedTransactions.value + id
    }

    fun sendChatMessage(msg: String) {
        if (msg.isBlank()) return
        viewModelScope.launch {
            _chatMessages.value = _chatMessages.value + SupportChatMessage("User", msg)
            delay(800)
            val reply = when {
                msg.contains("card", ignoreCase = true) || msg.contains("sticker", ignoreCase = true) -> 
                    "You can link your EazyPay NFC card or sticker instantly at the Babcock IT Support booth or via any registered Student Union Agent device."
                msg.contains("charge", ignoreCase = true) || msg.contains("withdraw", ignoreCase = true) -> 
                    "Withdrawals are settled directly to your linked bank account (e.g. GTBank) within 24 hours. Contact our finance line if you experience any delay."
                msg.contains("offline", ignoreCase = true) -> 
                    "Yes! EazyPay uses advanced offline signed cryptographic ledger validation, ensuring payments execute securely with absolutely zero internet connectivity."
                msg.contains("failed", ignoreCase = true) || msg.contains("dispute", ignoreCase = true) ->
                    "We apologize for the inconvenience! Tap on any transaction in your History tab, select 'Dispute Transaction' and our administrative panel will review it."
                else -> 
                    "Thank you for contacting Babcock EazyPay Support. We have logged your request. One of our agents is reviewing your ticket and will respond shortly."
            }
            _chatMessages.value = _chatMessages.value + SupportChatMessage("Agent", reply)
        }
    }

    private fun verifyPinAndExecute(onPinComplete: () -> Unit) {
        viewModelScope.launch {
            if (repository.verifyPin(_pinBuffer.value)) {
                _pinError.value = false
                _pinAttemptsRemaining.value = 3 // reset attempts
                onPinComplete()
                _pinBuffer.value = ""
            } else {
                _pinError.value = true
                val remaining = (_pinAttemptsRemaining.value - 1).coerceAtLeast(0)
                _pinAttemptsRemaining.value = remaining
                if (remaining == 0) {
                    _isLockedOut.value = true
                }
                delay(800)
                _pinBuffer.value = ""
                _pinError.value = false
            }
        }
    }

    fun setTerminalAmount(amount: String) {
        _terminalAmount.value = amount
    }

    fun triggerTerminalScan() {
        viewModelScope.launch {
            _terminalState.value = 2 // Card Detected
            _terminalStudent.value = student.value
        }
    }

    fun resetTerminal() {
        _terminalState.value = 1
        _pinBuffer.value = ""
    }

    fun chargeStudentFromTerminal(onComplete: () -> Unit) {
        viewModelScope.launch {
            _terminalState.value = 3 // Waiting for PIN
            // This is where student enters PIN
        }
    }

    fun completeTerminalPayment() {
        viewModelScope.launch {
            val amount = _terminalAmount.value.toDoubleOrNull() ?: 200.0
            
            // Deduct from student balance (representing student side)
            // And add to vendor earnings (representing vendor side)
            repository.performNfcPayment("Terminal: Musa Ibrahim", amount, isStudentDebit = true)
            repository.performNfcPayment("Payment from " + student.value.name, amount, isStudentDebit = false)
            
            _terminalState.value = 4 // Success
        }
    }

    fun withdrawFunds(amount: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.triggerWithdrawal(amount)
            onResult(success)
        }
    }

    fun startDemoFlow() {
        demoJob?.cancel()
        _demoActive.value = true
        demoJob = viewModelScope.launch {
            // STEP 1: Tap to Pay (Wait for tap)
            _demoStep.value = 1
            delay(2000)

            // STEP 2: Card / ID Read (ID detected offline)
            _demoStep.value = 2
            delay(2000)

            // STEP 3: Enter Amount
            _demoStep.value = 3
            delay(2000)

            // STEP 4: PIN Verification
            _demoStep.value = 4
            delay(2500)

            // Perform transaction in DB
            val status = if (isOffline.value) "Pending" else "Synced"
            repository.addTransaction(
                title = "Musa Ibrahim (Demo)",
                category = "food",
                amount = 200.0,
                isDebit = true,
                status = status
            )
            repository.addTransaction(
                title = "Payment from Joy (Demo)",
                category = "food",
                amount = 200.0,
                isDebit = false,
                status = status
            )

            // STEP 5: Payment Confirmed (Success Screen)
            _demoStep.value = 5
            delay(3500)

            // Exit Demo
            _demoActive.value = false
        }
    }

    fun stopDemoFlow() {
        demoJob?.cancel()
        _demoActive.value = false
    }

    fun syncAll() {
        viewModelScope.launch {
            repository.syncPending()
        }
    }
}
