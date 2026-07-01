package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EazyPayViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EazyPayRepository(application)

    val currentRole = repository.currentRole
    val isOffline = repository.isOffline
    val isSyncing = repository.isSyncing
    val student = repository.student
    val vendor = repository.vendor
    val userPin = repository.userPin
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

    private var demoJob: Job? = null

    fun setRole(role: String) {
        viewModelScope.launch {
            repository.setRole(role)
        }
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

    fun appendPinChar(char: Char, onPinComplete: () -> Unit) {
        if (_pinBuffer.value.length < 4) {
            _pinBuffer.value = _pinBuffer.value + char
            if (_pinBuffer.value.length == 4) {
                verifyPinAndExecute(onPinComplete)
            }
        }
    }

    fun deletePinChar() {
        if (_pinBuffer.value.isNotEmpty()) {
            _pinBuffer.value = _pinBuffer.value.dropLast(1)
        }
    }

    private fun verifyPinAndExecute(onPinComplete: () -> Unit) {
        viewModelScope.launch {
            if (_pinBuffer.value == userPin.value) {
                _pinError.value = false
                onPinComplete()
                _pinBuffer.value = ""
            } else {
                _pinError.value = true
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
