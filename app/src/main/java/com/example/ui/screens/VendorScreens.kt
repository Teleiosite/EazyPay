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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EazyPay Terminal",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
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
                    modifier = Modifier.clickable { viewModel.triggerTerminalScan() }
                ) {
                    NfcPulsingRing(isListening = true)
                    Spacer(modifier = Modifier.height(20.dp))
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
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    border = BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Terminal status: Always active",
                            color = Success,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Students can tap sticker or phone to authorize payments",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
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
    var period by remember { mutableStateOf("Today") } // "Today", "Week", "Month"

    val filteredList = transactions.filter { !it.isDebit } // Vendor only gets received earnings

    val totalAmount = filteredList.sumOf { it.amount }

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
                            "₦${String.format("%,.2f", totalAmount + 2100.0)}", // add seed earnings
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("SETTLED SUCCESS", color = Success, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${filteredList.size + 10} payments", color = TextSecondary, fontSize = 13.sp)
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
                text = "Earnings Log",
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
                    syncStatus = tx.syncStatus
                )
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
                    modifier = Modifier.fillMaxWidth()
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
}

// 5. WITHDRAWAL BANK OVERLAY MODAL
@Composable
fun WithdrawalModal(
    viewModel: EazyPayViewModel,
    onDismiss: () -> Unit
) {
    val vendorUser by viewModel.vendor.collectAsState()
    var withdrawAmountText by remember { mutableStateOf("") }
    var alertSuccess by remember { mutableStateOf(false) }

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
                .fillMaxWidth(0.9f)
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
                    Text("Withdraw Earnings", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // GTBank linked account
                Card(
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    border = BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("LINKED BANK ACCOUNT", color = PrimaryTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${vendorUser.bankName} - Musa Ibrahim",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = "Account: ${vendorUser.accountNumber}", color = TextSecondary, fontSize = 13.sp)
                        }
                        Icon(Icons.Default.AccountBalance, contentDescription = "Bank", tint = PrimaryTeal)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("WITHDRAWAL AMOUNT", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = withdrawAmountText,
                    onValueChange = { withdrawAmountText = it },
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
                Text(
                    text = "Withdrawable balance: ₦${String.format("%,.2f", vendorUser.todayEarnings)}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val amount = withdrawAmountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0 && amount <= vendorUser.todayEarnings) {
                            viewModel.withdrawFunds(amount) { success ->
                                if (success) {
                                    alertSuccess = true
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    enabled = withdrawAmountText.isNotBlank()
                ) {
                    Text("Confirm Settlement", color = Background, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (alertSuccess) {
            AlertDialog(
                onDismissRequest = {
                    alertSuccess = false
                    onDismiss()
                },
                containerColor = Surface,
                title = { Text("Settlement Initiated", color = TextPrimary) },
                text = {
                    Text(
                        "Your withdrawal request has been registered cryptographically offline. It will be credited to GTBank ····4521 within 24 hours.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        alertSuccess = false
                        onDismiss()
                    }) {
                        Text("Done", color = PrimaryTeal)
                    }
                }
            )
        }
    }
}
