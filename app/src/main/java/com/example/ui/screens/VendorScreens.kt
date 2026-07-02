package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VendorUser
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.ui.EazyPayViewModel
import com.example.ui.theme.*

// --- MAIN WRAPPER WITH BOTTOM TAB NAVIGATION FOR VENDOR ---
@Composable
fun VendorMainScreen(
    viewModel: EazyPayViewModel,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("home") } // "home", "terminal", "earnings", "profile"
    val isOffline by viewModel.isOffline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var showWithdraw by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PrimaryTeal.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Nfc,
                                contentDescription = "Logo",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Connection toggle button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isOffline) Warning.copy(alpha = 0.15f) else Success.copy(alpha = 0.15f))
                            .border(
                                1.dp,
                                if (isOffline) Warning.copy(alpha = 0.3f) else Success.copy(alpha = 0.3f),
                                RoundedCornerShape(100.dp)
                            )
                            .clickable { viewModel.toggleOffline() }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isOffline) Warning else Success)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOffline) "Offline" else "Online Sync",
                            color = if (isOffline) Warning else Success,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                OfflineStatusBar(isOffline = isOffline, isSyncing = isSyncing)
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == "home",
                    onClick = { selectedTab = "home" },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Background,
                        selectedTextColor = PrimaryTeal,
                        indicatorColor = PrimaryTeal,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "terminal",
                    onClick = { selectedTab = "terminal" },
                    icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Terminal") },
                    label = { Text("POS Terminal", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Background,
                        selectedTextColor = PrimaryTeal,
                        indicatorColor = PrimaryTeal,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "earnings",
                    onClick = { selectedTab = "earnings" },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Earnings") },
                    label = { Text("Earnings", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Background,
                        selectedTextColor = PrimaryTeal,
                        indicatorColor = PrimaryTeal,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "profile",
                    onClick = { selectedTab = "profile" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Background,
                        selectedTextColor = PrimaryTeal,
                        indicatorColor = PrimaryTeal,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "home" -> VendorHomeScreen(
                    viewModel = viewModel,
                    onTerminalClick = { selectedTab = "terminal" },
                    onWithdrawClick = { showWithdraw = true },
                    onSeeAllClick = { selectedTab = "earnings" }
                )
                "terminal" -> VendorTerminalScreen(viewModel = viewModel)
                "earnings" -> VendorEarningsScreen(viewModel = viewModel)
                "profile" -> VendorProfileScreen(viewModel = viewModel, onSignOut = onSignOut)
            }

            if (showWithdraw) {
                WithdrawalModal(
                    viewModel = viewModel,
                    onDismiss = { showWithdraw = false }
                )
            }
        }
    }
}

// 1. VENDOR HOME / DASHBOARD
@Composable
fun VendorHomeScreen(
    viewModel: EazyPayViewModel,
    onTerminalClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onSeeAllClick: () -> Unit
) {
    val vendorUser by viewModel.vendor.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    var isEarningsVisible by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vendor Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Welcome back, Musa", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = vendorUser.name,
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PrimaryTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = vendorUser.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("")
                    Text(
                        text = initials,
                        color = PrimaryTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Today's Earnings Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // Blurry radial glow at top right
                            drawCircle(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(PrimaryTeal.copy(alpha = 0.08f), Color.Transparent),
                                    center = androidx.compose.ui.geometry.Offset(this@drawBehind.size.width * 0.9f, this@drawBehind.size.height * 0.1f),
                                    radius = this@drawBehind.size.width * 0.4f
                                )
                            )
                        }
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "TODAY'S EARNINGS",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isEarningsVisible) "₦${String.format("%,.2f", vendorUser.todayEarnings)}" else "₦ • • • •",
                                        color = TextPrimary,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = if (isEarningsVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Show/Hide Balance",
                                        tint = TextSecondary,
                                        modifier = Modifier
                                            .clickable { isEarningsVisible = !isEarningsVisible }
                                            .size(20.dp)
                                    )
                                }
                            }

                            // Terminal Code Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Background)
                                    .border(1.dp, Border, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "TERM-047",
                                    color = PrimaryTeal,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "10 transactions received • Last active: 8:14 am",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Button(
                            onClick = onWithdrawClick,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Withdraw earnings", color = Background, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Terminal Trigger shortcut
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = BorderStroke(1.dp, Border),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTerminalClick() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PrimaryTeal.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = "Terminal", tint = PrimaryTeal)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Launch NFC POS Terminal", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Ready to receive student payments instantly", color = TextSecondary, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "Launch", tint = TextSecondary)
                }
            }
        }

        // Today's received transactions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Ledger",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "See all",
                    color = PrimaryTeal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onSeeAllClick() }
                )
            }
        }

        // Filter and show only received payments for vendor
        val receivedList = transactions.filter { !it.isDebit }
        if (receivedList.isEmpty()) {
            item {
                Text(
                    text = "No payments received yet today.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }
        } else {
            items(receivedList.take(3)) { tx ->
                TransactionRow(
                    title = tx.title,
                    category = tx.category,
                    amount = tx.amount,
                    isDebit = tx.isDebit,
                    timestamp = tx.timestamp,
                    syncStatus = tx.syncStatus
                )
            }
        }
    }
}

// 2. VENDOR TERMINAL (MULTI-STATE POINT OF SALE)
@Composable
fun VendorTerminalScreen(
    viewModel: EazyPayViewModel
) {
    val terminalState by viewModel.terminalState.collectAsState()
    val terminalAmount by viewModel.terminalAmount.collectAsState()
    val terminalStudent by viewModel.terminalStudent.collectAsState()
    var isNfcAntennaOn by remember { mutableStateOf(true) }
    
    val pinLength by viewModel.pinBuffer.map { it.length }.collectAsState(0)
    val isPinError by viewModel.pinError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "NFC Payment Terminal",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Secure local contactless POS",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        when (terminalState) {
            // STATE 1: WAITING FOR TAP
            1 -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    if (isNfcAntennaOn) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { viewModel.triggerTerminalScan() }
                        ) {
                            NfcPulsingRing(isListening = true)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Waiting for payment tap...",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap here to simulate Student card contact",
                                color = PrimaryTeal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(Border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Nfc, contentDescription = "Off", tint = TextSecondary, modifier = Modifier.size(48.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "NFC Receiver Antenna Off",
                            color = Danger,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enable terminal antenna below to scan cards",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Antenna toggle row
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        border = BorderStroke(1.dp, Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.SettingsInputAntenna, contentDescription = "NFC", tint = if (isNfcAntennaOn) Success else TextSecondary)
                                Column {
                                    Text("NFC Hardware Antenna", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(if (isNfcAntennaOn) "Simulating active EMV polling" else "Transceiver hardware asleep", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                            Switch(
                                checked = isNfcAntennaOn,
                                onCheckedChange = { isNfcAntennaOn = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Background,
                                    checkedTrackColor = PrimaryTeal
                                )
                            )
                        }
                    }

                    // Secure terminal Compliance Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        border = BorderStroke(1.dp, Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Lock, contentDescription = "Secure", tint = Success, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "EMV SECURE CONTACTLESS V3 ACTIVE",
                                    color = Success,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "This device complies with campus offline protocol requirements. Cryptographic signatures verify balances without internet.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // STATE 2: CARD DETECTED (ENTER AMOUNT)
            2 -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .background(Success.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "✓ Student Card Detected Offline",
                            color = Success,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = terminalStudent?.name ?: "Joy Adaeze",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${terminalStudent?.id ?: "EP-0047"} • Verification: CRYPTO",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("CHARGE AMOUNT", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = terminalAmount,
                        onValueChange = { viewModel.setTerminalAmount(it) },
                        prefix = { Text("₦", color = TextPrimary, fontWeight = FontWeight.Bold) },
                        suffix = { Text("+ ₦10 fee added", color = TextSecondary, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Border,
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface
                        ),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutButton(
                        onClick = { viewModel.resetTerminal() },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("Cancel", color = Danger, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.chargeStudentFromTerminal {} },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                    ) {
                        Text("Charge Student", color = Background, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // STATE 3: CONFIRMING STUDENT PIN
            3 -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = PrimaryTeal, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Waiting for student PIN authorization...",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Student must verify PIN on terminal or their mobile device",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 6.dp)
                    )
                }

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = { viewModel.completeTerminalPayment() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Success)
                    ) {
                        Text("Simulate Student PIN Confirmed", color = Background, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { viewModel.resetTerminal() }) {
                        Text("Aborted Transaction", color = Danger)
                    }
                }
            }

            // STATE 4: PAYMENT RECEIVED
            4 -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Success.copy(alpha = 0.15f))
                            .border(2.dp, Success, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Confirmed", tint = Success, modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Payment received!",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+ ₦${String.format("%,.2f", terminalAmount.toDoubleOrNull() ?: 200.0)}",
                        color = Success,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "From Joy Adaeze • Offline verified ✓",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = { viewModel.resetTerminal() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("Ready for next tap", color = Background, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(12.dp),
        content = content
    )
}

// 3. VENDOR EARNINGS HISTORY + PEAK CHART
@Composable
fun VendorEarningsScreen(
    viewModel: EazyPayViewModel
) {
    val transactions by viewModel.transactions.collectAsState()
    var period by remember { mutableStateOf("This Week") } // "Today", "This Week", "This Month"
    var selectedTransaction by remember { mutableStateOf<com.example.data.TransactionEntity?>(null) }

    val filteredList = transactions.filter { !it.isDebit } // Vendor only gets received earnings

    val totalAmount = filteredList.sumOf { it.amount }
    val syncedAmount = filteredList.filter { it.syncStatus == "Synced" }.sumOf { it.amount }
    val pendingAmount = filteredList.filter { it.syncStatus == "Pending" }.sumOf { it.amount }

    // Average calculations
    val avgTicket = if (filteredList.isNotEmpty()) totalAmount / filteredList.size else 350.00
    val totalPaymentsCount = filteredList.size + 14

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Earnings Analytics",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Period chips row
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Today", "This Week", "This Month").forEach { p ->
                    val active = period == p
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (active) PrimaryTeal else Surface)
                            .border(1.dp, if (active) PrimaryTeal else Border, RoundedCornerShape(100.dp))
                            .clickable { period = p }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = p,
                            color = if (active) Background else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Earnings summary metric card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = BorderStroke(1.dp, Border),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TOTAL AMOUNT EARNED", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "₦${String.format("%,.2f", totalAmount + 14850.00)}", // add seed earnings
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("TOTAL VOLUMES", color = PrimaryTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("$totalPaymentsCount transactions", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }

        // Settlement Sync Tracker Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = BorderStroke(1.dp, Border),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "OFFLINE SETTLEMENT TRACKER",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Synced Bank settlement
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Background),
                            border = BorderStroke(1.dp, Border),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Success))
                                    Text("Settled to Bank", color = TextSecondary, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("₦${String.format("%,.2f", syncedAmount + 14850.00)}", color = Success, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Pending settlement (offline)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Background),
                            border = BorderStroke(1.dp, Border),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Warning))
                                    Text("Pending Sync", color = TextSecondary, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("₦${String.format("%,.2f", pendingAmount)}", color = Warning, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ℹ️ Offline collections are stored cryptographically on the terminal and processed automatically to your linked account once connection is restored.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Terminal Averages & Peaks Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = BorderStroke(1.dp, Border),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PERFORMANCE METRICS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Avg. Ticket Value", color = TextSecondary, fontSize = 11.sp)
                            Text("₦${String.format("%.2f", avgTicket)}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Busiest Rush Hour", color = TextSecondary, fontSize = 11.sp)
                            Text("12:15 PM - 1:45 PM", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Peak Category", color = TextSecondary, fontSize = 11.sp)
                            Text("🍛 Lunch Meals", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Peak Hours Bar Chart component
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = BorderStroke(1.dp, Border),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SimpleBarChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        item {
            Text(
                text = "Terminal Collections Log",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (filteredList.isEmpty()) {
            item {
                Text(
                    text = "No recorded transactions for this period",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )
            }
        } else {
            items(filteredList) { tx ->
                TransactionRow(
                    title = tx.title,
                    category = tx.category,
                    amount = tx.amount,
                    isDebit = tx.isDebit,
                    timestamp = tx.timestamp,
                    syncStatus = tx.syncStatus,
                    onClick = { selectedTransaction = tx }
                )
            }
        }
    }

    // Settlement Receipt Details Modal
    selectedTransaction?.let { tx ->
        VendorSettlementReceiptModal(
            tx = tx,
            onDismiss = { selectedTransaction = null }
        )
    }
}

@Composable
fun VendorSettlementReceiptModal(
    tx: com.example.data.TransactionEntity,
    onDismiss: () -> Unit
) {
    val syncLabel = if (tx.syncStatus == "Synced") "Settled to Bank ✓" else "Offline Logged 📶"
    val syncColor = if (tx.syncStatus == "Synced") Success else Warning
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Background),
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .border(1.dp, Border, RoundedCornerShape(24.dp))
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Collection Receipt", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(PrimaryTeal.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = "Receipt", tint = PrimaryTeal, modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "₦${String.format("%,.2f", tx.amount)}",
                    color = Success,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(tx.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = syncLabel,
                    color = syncColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReceiptDetailRow("Crypto Signature", "ECDSA-secp256k1 Signed")
                    ReceiptDetailRow("Linked Device", "NFC SECURE-CHIP-V3")
                    ReceiptDetailRow("Collection Time", java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(tx.timestamp)))
                    ReceiptDetailRow("Campus Station", "Babcock Cafeteria A")
                    ReceiptDetailRow("Settlement Target", "Wema Bank (012****905)")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Dismiss", color = Background, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 4. VENDOR PROFILE
@Composable
fun VendorProfileScreen(
    viewModel: EazyPayViewModel,
    onSignOut: () -> Unit
) {
    val vendorUser by viewModel.vendor.collectAsState()
    var activeModal by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(PrimaryTeal.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            val initials = vendorUser.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("")
            Text(
                text = initials,
                color = PrimaryTeal,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(vendorUser.name, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Campus Registered Vendor ID: ${vendorUser.id}", color = TextSecondary, fontSize = 13.sp)
        }

        // Status metrics card
        Card(
            colors = CardDefaults.cardColors(containerColor = Surface),
            border = BorderStroke(1.dp, Border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("TERMINAL HEALTH", color = TextSecondary, fontSize = 10.sp)
                    Text("Excellent (100%)", color = Success, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("DEVICE TYPE", color = TextSecondary, fontSize = 10.sp)
                    Text("NFC Enabled Android", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Action Lists
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "TERMINAL CONFIGURATION",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            val items = listOf(
                Pair("Bank Withdrawal Details", Icons.Outlined.AccountBalance),
                Pair("Contact campus finance dept", Icons.Outlined.Business),
                Pair("Synchronize local records", Icons.Outlined.Sync),
                Pair("Terminal Passcode security", Icons.Outlined.Security),
                Pair("Legal disclosures", Icons.Outlined.PrivacyTip)
            )

            items.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    border = BorderStroke(1.dp, Border),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            activeModal = when (item.first) {
                                "Bank Withdrawal Details" -> "bank_withdrawal"
                                "Contact campus finance dept" -> "finance_contact"
                                "Synchronize local records" -> "sync_records"
                                "Terminal Passcode security" -> "passcode_sec"
                                "Legal disclosures" -> "legal"
                                else -> null
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(item.second, contentDescription = item.first, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(item.first, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Danger.copy(alpha = 0.15f))
            ) {
                Text("Sign out account", color = Danger, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal Overlays
    when (activeModal) {
        "bank_withdrawal" -> BankWithdrawalDetailsModal(viewModel = viewModel, onDismiss = { activeModal = null })
        "finance_contact" -> FinanceContactModal(onDismiss = { activeModal = null })
        "sync_records" -> SyncRecordsModal(viewModel = viewModel, onDismiss = { activeModal = null })
        "passcode_sec" -> PasscodeSecurityModal(viewModel = viewModel, onDismiss = { activeModal = null })
        "legal" -> LegalDisclosuresModal(onDismiss = { activeModal = null })
    }
}

// 5. WITHDRAWAL BANK OVERLAY MODAL
@Composable
fun WithdrawalModal(
    viewModel: EazyPayViewModel,
    onDismiss: () -> Unit
) {
    val vendorUser by viewModel.vendor.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var step by remember { mutableStateOf(1) } // 1: Form entry, 2: PIN Challenge, 3: Success Receipt
    var withdrawAmountText by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf("Guaranty Trust Bank (GTBank)") }
    var showBankSelector by remember { mutableStateOf(false) }
    var bankAccountNumber by remember { mutableStateOf(vendorUser.accountNumber) }

    // Dynamic NIP Resolver States
    var resolvedAccountName by remember { mutableStateOf("MUSA IBRAHIM") }
    var isResolvingAccount by remember { mutableStateOf(false) }

    // PIN Challenge
    var txPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var isProcessingWithdrawal by remember { mutableStateOf(false) }

    // Receipt details
    var sessionID by remember { mutableStateOf("") }
    var payoutReference by remember { mutableStateOf("") }

    val banksList = listOf(
        "Guaranty Trust Bank (GTBank)",
        "Access Bank PLC",
        "Zenith Bank PLC",
        "United Bank for Africa (UBA)",
        "First Bank of Nigeria",
        "Moniepoint MFB",
        "OPay Digital Services",
        "PalmPay",
        "Kuda Microfinance Bank"
    )

    // Trigger dynamic NIP Name Lookup simulator
    LaunchedEffect(bankAccountNumber, selectedBank) {
        if (bankAccountNumber.length == 10) {
            isResolvingAccount = true
            delay(1200) // Simulating NIP lookup query to NIBSS central switch
            resolvedAccountName = when {
                selectedBank.contains("GTBank") -> "MUSA IBRAHIM"
                selectedBank.contains("Access") -> "MUSA IBRAHIM (ACCESS)"
                selectedBank.contains("Zenith") -> "MUSA IBRAHIM (ZENITH CORP)"
                selectedBank.contains("UBA") -> "MUSA IBRAHIM (UBA MERC)"
                selectedBank.contains("First Bank") -> "MUSA IBRAHIM (FIRST)"
                selectedBank.contains("Moniepoint") -> "MUSA IBRAHIM VENTURES"
                selectedBank.contains("OPay") -> "MUSA IBRAHIM OPAY"
                selectedBank.contains("PalmPay") -> "MUSA IBRAHIM PALMPAY"
                else -> "MUSA IBRAHIM (KUDA)"
            }
            isResolvingAccount = false
        } else {
            resolvedAccountName = ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { if (!isProcessingWithdrawal && step != 3) onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Background),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(enabled = false) {}
                .border(1.dp, Border, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                if (step != 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (step == 1) Icons.Default.AccountBalance else Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = PrimaryTeal,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        if (!isProcessingWithdrawal) {
                                            if (step > 1) step-- else onDismiss()
                                        }
                                    }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (step == 1) "Withdraw Earnings" else "Authorize Settlement",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (!isProcessingWithdrawal) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier.clickable { onDismiss() }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // STEP 1: Main form with bank input, amount, fees, and NIP lookup
                if (step == 1) {
                    Text("SELECT SETTLEMENT BANK", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Simulated dropdown clicker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface)
                            .border(1.dp, Border, RoundedCornerShape(12.dp))
                            .clickable { showBankSelector = !showBankSelector }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedBank, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select", tint = PrimaryTeal)
                    }

                    // Bank Selector inline popup
                    if (showBankSelector) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            border = BorderStroke(1.dp, Border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                        ) {
                            LazyColumn {
                                items(banksList) { bankName ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedBank = bankName
                                                showBankSelector = false
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Text(bankName, color = if (selectedBank == bankName) PrimaryTeal else TextPrimary, fontSize = 13.sp, fontWeight = if (selectedBank == bankName) FontWeight.Bold else FontWeight.Normal)
                                    }
                                    Divider(color = Border.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("10-DIGIT ACCOUNT NUMBER", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = bankAccountNumber,
                        onValueChange = {
                            val clean = it.filter { char -> char.isDigit() }
                            if (clean.length <= 10) {
                                bankAccountNumber = clean
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("e.g. 0123456789", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Border,
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface
                        ),
                        singleLine = true
                    )

                    // NIP Resolved Account Name Display
                    if (isResolvingAccount) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = PrimaryTeal, strokeWidth = 1.5.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Querying NIBSS NIP Name resolution directory...", color = TextSecondary, fontSize = 11.sp)
                        }
                    } else if (resolvedAccountName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Success, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("VERIFIED NAME: $resolvedAccountName", color = Success, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("WITHDRAWAL AMOUNT", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = withdrawAmountText,
                        onValueChange = { withdrawAmountText = it.filter { char -> char.isDigit() } },
                        prefix = { Text("₦", color = TextPrimary, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Border,
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Withdrawable balance: ₦${String.format("%,.2f", vendorUser.todayEarnings)}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        if (vendorUser.todayEarnings > 0) {
                            Text(
                                "Withdraw All",
                                color = PrimaryTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    withdrawAmountText = vendorUser.todayEarnings.toInt().toString()
                                }
                            )
                        }
                    }

                    // Payout Schedule breakdown fees
                    val amtVal = withdrawAmountText.toDoubleOrNull() ?: 0.0
                    if (amtVal > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            border = BorderStroke(1.dp, Border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("SETTLEMENT FEE SCHEDULE", color = PrimaryTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("NIP Processing Charge", color = TextSecondary, fontSize = 11.sp)
                                    Text("₦10.00", color = TextPrimary, fontSize = 11.sp)
                                }
                                Divider(color = Border.copy(alpha = 0.5f))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Net Payout", color = TextSecondary, fontSize = 11.sp)
                                    Text("₦${String.format("%,.2f", if (amtVal > 10.0) amtVal - 10.0 else 0.0)}", color = Success, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val canProceed = amtVal > 0 && amtVal <= vendorUser.todayEarnings && bankAccountNumber.length == 10 && resolvedAccountName.isNotBlank()

                    Button(
                        onClick = { step = 2 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        enabled = canProceed
                    ) {
                        Text("Proceed to Authorization", color = Background, fontWeight = FontWeight.Bold)
                    }
                }

                // STEP 2: Secure PIN Challenge
                else if (step == 2) {
                    val finalAmt = withdrawAmountText.toDoubleOrNull() ?: 0.0
                    
                    Text("AUTHORIZE TRANSACTION WITH PIN", color = PrimaryTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Please input your 4-digit security transaction PIN to sign and execute this instant settlement request.", color = TextSecondary, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = txPin,
                        onValueChange = {
                            val clean = it.filter { char -> char.isDigit() }
                            if (clean.length <= 4) {
                                txPin = clean
                                pinError = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Enter 4-Digit Security PIN", color = TextSecondary) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = if (pinError) Warning else PrimaryTeal,
                            unfocusedBorderColor = if (pinError) Warning else Border,
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface
                        ),
                        singleLine = true
                    )

                    if (pinError) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Incorrect Transaction PIN. Use 1234 to authorize payment.", color = Warning, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            if (txPin == "1234" || txPin.length == 4) {
                                isProcessingWithdrawal = true
                                coroutineScope.launch {
                                    delay(2000) // Authenticating cryptographically with local keys & NIP
                                    viewModel.withdrawFunds(finalAmt) { success ->
                                        isProcessingWithdrawal = false
                                        if (success) {
                                            sessionID = "000013" + System.currentTimeMillis().toString().takeLast(10) + (1000..9999).random()
                                            payoutReference = "EP-PAY-NIP-" + (100000..999999).random()
                                            step = 3
                                        } else {
                                            pinError = true
                                        }
                                    }
                                }
                            } else {
                                pinError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        enabled = txPin.length == 4 && !isProcessingWithdrawal
                    ) {
                        if (isProcessingWithdrawal) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Background, strokeWidth = 2.dp)
                        } else {
                            Text("Sign & Authorize Settlement", color = Background, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // STEP 3: NIBSS Instant Payment Receipt
                else if (step == 3) {
                    val finalAmt = withdrawAmountText.toDoubleOrNull() ?: 0.0

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Checkmark
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Success.copy(alpha = 0.15f))
                                .border(2.dp, Success, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Success", tint = Success, modifier = Modifier.size(44.dp))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Settlement Complete!", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("NIP outbound transfer signed and settled instantly via NIBSS gateway", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Receipt Details
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            border = BorderStroke(1.dp, Border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("SETTLED AMOUNT", color = TextSecondary, fontSize = 11.sp)
                                    Text("₦${String.format("%,.2f", finalAmt)}", color = Success, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Divider(color = Border.copy(alpha = 0.5f))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("BENEFICIARY ACCOUNT", color = TextSecondary, fontSize = 11.sp)
                                    Text("$resolvedAccountName", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("BENEFICIARY BANK", color = TextSecondary, fontSize = 11.sp)
                                    Text(selectedBank, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("ACCOUNT NUMBER", color = TextSecondary, fontSize = 11.sp)
                                    Text(bankAccountNumber, color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                                Divider(color = Border.copy(alpha = 0.5f))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("NIBSS SESSION ID", color = TextSecondary, fontSize = 11.sp)
                                    Text(sessionID, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("PAYOUT REFERENCE", color = TextSecondary, fontSize = 11.sp)
                                    Text(payoutReference, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = { onDismiss() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text("Done", color = Background, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- VENDOR PROFILE CONFIGURATION MODALS ---

@Composable
fun BankWithdrawalDetailsModal(
    viewModel: EazyPayViewModel,
    onDismiss: () -> Unit
) {
    val vendorUser by viewModel.vendor.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var isEditing by remember { mutableStateOf(false) }
    
    var selectedBank by remember { mutableStateOf(vendorUser.bankName) }
    var bankAccountNumber by remember { mutableStateOf(vendorUser.accountNumber.filter { it.isDigit() }.ifBlank { "0123456789" }) }
    var showBankSelector by remember { mutableStateOf(false) }
    var resolvedAccountName by remember { mutableStateOf("MUSA IBRAHIM") }
    var isResolvingAccount by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val banksList = listOf(
        "Guaranty Trust Bank (GTBank)",
        "Access Bank PLC",
        "Zenith Bank PLC",
        "United Bank for Africa (UBA)",
        "First Bank of Nigeria",
        "Moniepoint MFB",
        "OPay Digital Services",
        "PalmPay",
        "Kuda Microfinance Bank"
    )

    LaunchedEffect(bankAccountNumber, selectedBank) {
        if (bankAccountNumber.length == 10) {
            isResolvingAccount = true
            delay(1000)
            resolvedAccountName = when {
                selectedBank.contains("GTBank") -> "MUSA IBRAHIM"
                selectedBank.contains("Access") -> "MUSA IBRAHIM (ACCESS)"
                selectedBank.contains("Zenith") -> "MUSA IBRAHIM (ZENITH CORP)"
                selectedBank.contains("UBA") -> "MUSA IBRAHIM (UBA MERC)"
                selectedBank.contains("First Bank") -> "MUSA IBRAHIM (FIRST)"
                selectedBank.contains("Moniepoint") -> "MUSA IBRAHIM VENTURES"
                selectedBank.contains("OPay") -> "MUSA IBRAHIM OPAY"
                selectedBank.contains("PalmPay") -> "MUSA IBRAHIM PALMPAY"
                else -> "MUSA IBRAHIM (KUDA)"
            }
            isResolvingAccount = false
        } else {
            resolvedAccountName = ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Background),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(enabled = false) {}
                .border(1.dp, Border, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Bank Details" else "Bank Withdrawal Details",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                if (!isEditing) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        border = BorderStroke(1.dp, Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("LINKED SETTLEMENT BANK", color = PrimaryTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Success.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = Success, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("NIP VERIFIED", color = Success, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(vendorUser.name.uppercase(java.util.Locale.getDefault()), color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(vendorUser.bankName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Acct: " + vendorUser.accountNumber,
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = PrimaryTeal,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            val rawAcct = vendorUser.accountNumber.filter { it.isDigit() }.ifBlank { "0123456789" }
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(rawAcct))
                                            coroutineScope.launch {
                                                saveSuccessMessage = "Account number copied!"
                                                delay(1500)
                                                saveSuccessMessage = ""
                                            }
                                        }
                                )
                            }
                        }
                    }

                    if (saveSuccessMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(saveSuccessMessage, color = Success, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Border)
                        ) {
                            Text("Dismiss", color = TextSecondary)
                        }
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text("Edit Details", color = Background, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text("SELECT BANK", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface)
                            .border(1.dp, Border, RoundedCornerShape(12.dp))
                            .clickable { showBankSelector = !showBankSelector }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedBank, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select", tint = PrimaryTeal)
                    }

                    if (showBankSelector) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            border = BorderStroke(1.dp, Border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp)
                        ) {
                            LazyColumn {
                                items(banksList) { bankName ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedBank = bankName
                                                showBankSelector = false
                                            }
                                            .padding(10.dp)
                                    ) {
                                        Text(bankName, color = if (selectedBank == bankName) PrimaryTeal else TextPrimary, fontSize = 12.sp, fontWeight = if (selectedBank == bankName) FontWeight.Bold else FontWeight.Normal)
                                    }
                                    Divider(color = Border.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("10-DIGIT ACCOUNT NUMBER", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = bankAccountNumber,
                        onValueChange = {
                            val clean = it.filter { char -> char.isDigit() }
                            if (clean.length <= 10) {
                                bankAccountNumber = clean
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Border,
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface
                        ),
                        singleLine = true
                    )

                    if (isResolvingAccount) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = PrimaryTeal, strokeWidth = 1.5.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verifying account via NIP switch...", color = TextSecondary, fontSize = 11.sp)
                        }
                    } else if (resolvedAccountName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Success, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("VERIFIED NAME: $resolvedAccountName", color = Success, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Border)
                        ) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Button(
                            onClick = {
                                if (bankAccountNumber.length == 10 && resolvedAccountName.isNotBlank()) {
                                    viewModel.updateVendorBankDetails(selectedBank, bankAccountNumber)
                                    isEditing = false
                                    coroutineScope.launch {
                                        saveSuccessMessage = "Bank details updated successfully!"
                                        delay(2000)
                                        saveSuccessMessage = ""
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            enabled = bankAccountNumber.length == 10 && resolvedAccountName.isNotBlank() && !isResolvingAccount
                        ) {
                            Text("Save", color = Background, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinanceContactModal(
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copyMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Background),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(enabled = false) {}
                .border(1.dp, Border, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Campus Finance Office", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "For queries regarding settlement transfers, offline trade reconciliation, and daily vendor deposits.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    border = BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ContactInfoRow(
                            label = "OFFICE LOCATION",
                            value = "Finance & Accounts Dept, Administrative Block, Wing B, Babcock University",
                            icon = Icons.Outlined.Business
                        )
                        Divider(color = Border.copy(alpha = 0.3f))
                        ContactInfoRow(
                            label = "DIRECT SETTLEMENTS LINE",
                            value = "+234 812 345 6789",
                            icon = Icons.Outlined.Phone,
                            onCopy = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("+2348123456789"))
                                coroutineScope.launch {
                                    copyMessage = "Phone number copied!"
                                    delay(1500)
                                    copyMessage = ""
                                }
                            }
                        )
                        Divider(color = Border.copy(alpha = 0.3f))
                        ContactInfoRow(
                            label = "OFFICIAL EMAIL",
                            value = "finance.settlements@babcock.edu.ng",
                            icon = Icons.Outlined.Email,
                            onCopy = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("finance.settlements@babcock.edu.ng"))
                                coroutineScope.launch {
                                    copyMessage = "Email copied!"
                                    delay(1500)
                                    copyMessage = ""
                                }
                            }
                        )
                    }
                }

                if (copyMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(copyMessage, color = Success, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", color = Background, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ContactInfoRow(
    label: String,
    value: String,
    icon: ImageVector,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(modifier = Modifier.weight(0.9f)) {
            Icon(icon, contentDescription = label, tint = PrimaryTeal, modifier = Modifier.size(18.dp).padding(top = 2.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        if (onCopy != null) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = PrimaryTeal,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onCopy() }
            )
        }
    }
}

@Composable
fun SyncRecordsModal(
    viewModel: EazyPayViewModel,
    onDismiss: () -> Unit
) {
    val isOffline by viewModel.isOffline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var syncMessage by remember { mutableStateOf("") }
    var localPendingCount by remember { mutableStateOf(0) }

    LaunchedEffect(transactions) {
        localPendingCount = transactions.count { it.syncStatus == "Pending" }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { if (!isSyncing) onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Background),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(enabled = false) {}
                .border(1.dp, Border, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Synchronize Local Records", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (!isSyncing) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.clickable { onDismiss() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(PrimaryTeal.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = PrimaryTeal,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Offline-to-Cloud synchronization ensures local cryptographic trade receipts are compiled, registered, and verified on the campus finance network.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    border = BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pending Offline Transactions", color = TextSecondary, fontSize = 12.sp)
                            Text("$localPendingCount records", color = if (localPendingCount > 0) Warning else Success, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Divider(color = Border.copy(alpha = 0.3f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Central Ledger Status", color = TextSecondary, fontSize = 12.sp)
                            Text(if (isOffline) "Disconnected (Offline)" else "Online & Operational", color = if (isOffline) Warning else Success, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Divider(color = Border.copy(alpha = 0.3f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Active Terminal Node ID", color = TextSecondary, fontSize = 12.sp)
                            Text("NODE-ECDSA-802", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                if (syncMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(syncMessage, color = Success, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isOffline) {
                            syncMessage = "Cannot sync while in Offline Mode! Turn connection ON first."
                        } else {
                            coroutineScope.launch {
                                viewModel.syncAll()
                                delay(2200)
                                syncMessage = "All offline records synced with NIBSS ✓"
                                delay(1500)
                                onDismiss()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSyncing
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Background, strokeWidth = 2.dp)
                    } else {
                        Text("Synchronize Now", color = Background, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PasscodeSecurityModal(
    viewModel: EazyPayViewModel,
    onDismiss: () -> Unit
) {
    val currentPin by viewModel.userPin.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    var oldPinText by remember { mutableStateOf("") }
    var newPinText by remember { mutableStateOf("") }
    var confirmPinText by remember { mutableStateOf("") }
    
    var pinErrorText by remember { mutableStateOf("") }
    var pinSuccessText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Background),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(enabled = false) {}
                .border(1.dp, Border, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Terminal Passcode Security", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                
                Text(
                    text = "Configure the 4-digit PIN used to authorize local offline sales settlement and terminal administration.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("CURRENT 4-DIGIT PIN", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = oldPinText,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) oldPinText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = Border,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("NEW 4-DIGIT PIN", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = newPinText,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPinText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = Border,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("CONFIRM NEW 4-DIGIT PIN", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = confirmPinText,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmPinText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = Border,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface
                    ),
                    singleLine = true
                )

                if (pinErrorText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(pinErrorText, color = Warning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (pinSuccessText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(pinSuccessText, color = Success, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (oldPinText != currentPin) {
                            pinErrorText = "Incorrect current PIN. (Try default '1234')"
                            pinSuccessText = ""
                        } else if (newPinText.length != 4) {
                            pinErrorText = "New PIN must be exactly 4 digits."
                            pinSuccessText = ""
                        } else if (newPinText != confirmPinText) {
                            pinErrorText = "Confirm PIN does not match."
                            pinSuccessText = ""
                        } else {
                            viewModel.setPin(newPinText)
                            pinErrorText = ""
                            pinSuccessText = "Security PIN updated successfully!"
                            oldPinText = ""
                            newPinText = ""
                            confirmPinText = ""
                            coroutineScope.launch {
                                delay(1500)
                                onDismiss()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = oldPinText.length == 4 && newPinText.length == 4 && confirmPinText.length == 4
                ) {
                    Text("Update security PIN", color = Background, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LegalDisclosuresModal(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Background),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.8f)
                .clickable(enabled = false) {}
                .border(1.dp, Border, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cryptographic Disclosures", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(end = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    LegalSection(
                        title = "1. Dual-Offline Framework",
                        body = "EazyPay operates using a secure dual-offline ledger protocol (BBU-DOL-V1) authorized for exclusive trade settlement on the Babcock University campus. Transactions execute securely even during total network blackouts."
                    )
                    
                    LegalSection(
                        title = "2. ECDSA Cryptographic Signing",
                        body = "All transactions are authenticated via an elliptic curve digital signature algorithm (secp256k1). When tapping a physical Smart ID Card or sticker against a registered terminal, a deterministic cryptographic proof is signed using private keys embedded within the secure enclave chip of the card."
                    )

                    LegalSection(
                        title = "3. Double Spend Mitigation",
                        body = "Local transactions contain sequential nonce chains preventing replay attacks. When a device synchronizes with central cloud database instances, any cryptographic double-spend mismatch triggers an automated audit; centralized ledger receipts logged at the Babcock Finance core always form the final trade arbiter."
                    )

                    LegalSection(
                        title = "4. Data Sovereignty & CBN Standards",
                        body = "Financial records comply with central regulatory standards for local closed-loop campus micro-payout systems. Offline transactions are stored securely using encrypted SQLite Room instances. No biometric telemetry is exported or stored outside local secure chip memories."
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Acknowledge & Close", color = Background, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LegalSection(title: String, body: String) {
    Column {
        Text(title, color = PrimaryTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(body, color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
    }
}
