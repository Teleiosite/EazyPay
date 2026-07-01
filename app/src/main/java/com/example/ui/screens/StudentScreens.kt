package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Offer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.example.data.StudentUser
import com.example.data.TransactionEntity
import com.example.ui.EazyPayViewModel
import com.example.ui.theme.*

// --- MAIN WRAPPER WITH BOTTOM TAB NAVIGATION FOR STUDENT ---
@Composable
fun StudentMainScreen(
    viewModel: EazyPayViewModel,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("home") } // "home", "pay", "history", "profile"
    val isOffline by viewModel.isOffline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    // Modals
    var showTopUp by remember { mutableStateOf(false) }
    var showSupport by remember { mutableStateOf(false) }

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
                            text = "EazyPay",
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
                            text = if (isOffline) "Offline Mode" else "Online Sync",
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
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Background,
                        selectedTextColor = PrimaryTeal,
                        indicatorColor = PrimaryTeal,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "pay",
                    onClick = { selectedTab = "pay" },
                    icon = { Icon(Icons.Default.Sensors, contentDescription = "Pay") },
                    label = { Text("Tap Pay", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Background,
                        selectedTextColor = PrimaryTeal,
                        indicatorColor = PrimaryTeal,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "history",
                    onClick = { selectedTab = "history" },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History", fontSize = 11.sp) },
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
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
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
                "home" -> StudentHomeScreen(
                    viewModel = viewModel,
                    onPayClick = { selectedTab = "pay" },
                    onTopUpClick = { showTopUp = true },
                    onHistoryClick = { selectedTab = "history" },
                    onSupportClick = { showSupport = true }
                )
                "pay" -> StudentPayScreen(viewModel = viewModel)
                "history" -> StudentHistoryScreen(viewModel = viewModel)
                "profile" -> StudentProfileScreen(viewModel = viewModel, onSignOut = onSignOut)
            }

            // Top-up Modal Bottom Sheet Overlay
            if (showTopUp) {
                TopUpModal(
                    viewModel = viewModel,
                    onDismiss = { showTopUp = false }
                )
            }

            // Simple Support Dialog
            if (showSupport) {
                AlertDialog(
                    onDismissRequest = { showSupport = false },
                    containerColor = Surface,
                    title = { Text("Contact Support", color = TextPrimary) },
                    text = {
                        Text(
                            "Have an issue or dispute? Reach out to EazyPay Customer Care via WhatsApp at +234 801 234 5678 or visit the IT Support block in Admin.",
                            color = TextSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showSupport = false }) {
                            Text("Ok", color = PrimaryTeal)
                        }
                    }
                )
            }
        }
    }
}

// 1. STUDENT HOME
@Composable
fun StudentHomeScreen(
    viewModel: EazyPayViewModel,
    onPayClick: () -> Unit,
    onTopUpClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSupportClick: () -> Unit
) {
    val studentUser by viewModel.student.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    var isBalanceVisible by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Greeting & Avatar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good morning 👋",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = studentUser.name,
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
                    val initials = studentUser.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("")
                    Text(
                        text = initials,
                        color = PrimaryTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Wallet Card
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
                        // Top row with Title and ID Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "WALLET BALANCE",
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
                                        text = if (isBalanceVisible) "₦${String.format("%,.2f", studentUser.balance)}" else "₦ • • • •",
                                        color = TextPrimary,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Show/Hide Balance",
                                        tint = TextSecondary,
                                        modifier = Modifier
                                            .clickable { isBalanceVisible = !isBalanceVisible }
                                            .size(20.dp)
                                    )
                                }
                            }

                            // EP Code Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Background)
                                    .border(1.dp, Border, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = studentUser.id,
                                    color = PrimaryTeal,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Status footer: Ready for NFC Tap
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pulsing green dot
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "dotAlpha"
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Success.copy(alpha = alpha))
                            )
                            Text(
                                text = "Ready for NFC Tap",
                                color = Success,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val actions = listOf(
                    Quadruple("Tap Pay", Icons.Default.Sensors, onPayClick, true),
                    Quadruple("Top Up", Icons.Default.Add, onTopUpClick, false),
                    Quadruple("History", Icons.Default.History, onHistoryClick, false),
                    Quadruple("Support", Icons.Default.HelpOutline, onSupportClick, false)
                )

                actions.forEach { action ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { action.third() }
                    ) {
                        val isPrimary = action.fourth
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isPrimary) PrimaryTeal else Surface)
                                .border(
                                    width = if (isPrimary) 0.dp else 1.dp,
                                    color = if (isPrimary) Color.Transparent else Border,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = action.second,
                                contentDescription = action.first,
                                tint = if (isPrimary) Background else TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = action.first.uppercase(),
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Horizontal Offers Section
        item {
            Column {
                Text(
                    text = "Exclusive Cashback Offers",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(viewModel.offers) { offer ->
                        CashbackOfferCard(offer = offer)
                    }
                }
            }
        }

        // Recent Transactions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "See all",
                    color = PrimaryTeal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onHistoryClick() }
                )
            }
        }

        val recentList = transactions.take(3)
        if (recentList.isEmpty()) {
            item {
                Text(
                    text = "No transactions yet. Tap Pay or Top Up to begin.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }
        } else {
            items(recentList) { tx ->
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

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun CashbackOfferCard(offer: Offer) {
    val offerIcon = when (offer.category) {
        "food" -> "🍲"
        "print" -> "🖨️"
        else -> "⚡"
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        modifier = Modifier
            .width(220.dp)
            .border(1.dp, Border, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = offerIcon, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = offer.title,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = offer.subtitle,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

// 2. STUDENT PAY SCREEN (TAP TO PAY ANIMATION & STATE ENGINE)
@Composable
fun StudentPayScreen(
    viewModel: EazyPayViewModel
) {
    val studentUser by viewModel.student.collectAsState()
    var isVerifyingPin by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    
    val pinLength by viewModel.pinBuffer.map { it.length }.collectAsState(0)
    val isPinError by viewModel.pinError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tap to Pay",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .background(PrimaryTeal.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "📶 Offline Capable — works without internet",
                        color = PrimaryTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Clickable NFC pulsing animation (triggers PIN authorize -> Success)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { isVerifyingPin = true }
                    .padding(vertical = 16.dp)
            ) {
                NfcPulsingRing()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tap here to simulate terminal contact",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            }

            // Footer parameters
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(16.dp),
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
                        Text(text = "AVAILABLE WALLET", color = TextSecondary, fontSize = 10.sp)
                        Text(
                            text = "₦${String.format("%,.2f", studentUser.balance)}",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "SERVICE FEE", color = TextSecondary, fontSize = 10.sp)
                        Text(
                            text = "₦10.00",
                            color = PrimaryTeal,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Shared PIN Entry Bottom Sheet overlay
        if (isVerifyingPin) {
            PinEntryModal(
                pinLength = pinLength,
                isError = isPinError,
                onChar = { char ->
                    viewModel.appendPinChar(char) {
                        isVerifyingPin = false
                        showSuccess = true
                        
                        // Execute payment transaction directly
                        kotlinx.coroutines.GlobalScope.launch {
                            viewModel.setTerminalAmount("210") // standard demo amount + fee
                            viewModel.completeTerminalPayment()
                        }
                    }
                },
                onDelete = { viewModel.deletePinChar() },
                onDismiss = { isVerifyingPin = false }
            )
        }

        // Success Confirmation Full-screen Overlay
        if (showSuccess) {
            PaymentSuccessModal(
                amount = 210.0,
                vendorName = "Mama Tee's Kitchen",
                onDismiss = { showSuccess = false }
            )
        }
    }
}

// 3. STUDENT TRANSACTION HISTORY LEDGER
@Composable
fun StudentHistoryScreen(
    viewModel: EazyPayViewModel
) {
    val transactions by viewModel.transactions.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Paid", "Received", "Pending"

    val filteredList = transactions.filter { tx ->
        // Search Filter
        val matchesSearch = tx.title.contains(searchQuery, ignoreCase = true) ||
                tx.category.contains(searchQuery, ignoreCase = true)
        
        // Tab Filter
        val matchesFilter = when (selectedFilter) {
            "Paid" -> tx.isDebit
            "Received" -> !tx.isDebit
            "Pending" -> tx.syncStatus == "Pending"
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Transaction Ledger",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search transactions...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
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

        Spacer(modifier = Modifier.height(16.dp))

        // Filters horizontal row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("All", "Paid", "Received", "Pending")
            filters.forEach { filter ->
                val active = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (active) PrimaryTeal else Surface)
                        .border(1.dp, if (active) PrimaryTeal else Border, RoundedCornerShape(100.dp))
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        color = if (active) Background else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transaction List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "No transactions",
                        tint = TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching records found",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
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
}

// 4. STUDENT PROFILE SETTINGS SCREEN
@Composable
fun StudentProfileScreen(
    viewModel: EazyPayViewModel,
    onSignOut: () -> Unit
) {
    val studentUser by viewModel.student.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar Header
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(PrimaryTeal.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            val initials = studentUser.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("")
            Text(
                text = initials,
                color = PrimaryTeal,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = studentUser.name,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Nigeria • +234 ${studentUser.phone}",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // Student EazyPay ID details
        Card(
            colors = CardDefaults.cardColors(containerColor = Surface),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("MY CAMPUS EAZYPAY ID", color = TextSecondary, fontSize = 9.sp)
                    Text(
                        text = studentUser.id,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { clipboardManager.setText(AnnotatedString(studentUser.id)) },
                    colors = ButtonDefaults.buttonColors(containerColor = Border),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Copy ID", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Profile lists
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "ACCOUNT CONFIGURATION",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            val settings = listOf(
                Pair("Personal details", Icons.Outlined.Person),
                Pair("Registered NFC cards", Icons.Outlined.Nfc),
                Pair("Biometrics lock", Icons.Outlined.Fingerprint),
                Pair("Change security PIN", Icons.Outlined.Lock),
                Pair("Help Center & FAQ", Icons.Outlined.HelpOutline),
                Pair("Privacy Policy & Terms", Icons.Outlined.Info)
            )

            settings.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Border)
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
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Danger.copy(alpha = 0.15f))
            ) {
                Text("Sign out account", color = Danger, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 5. TOP-UP OVERLAY MODAL
@Composable
fun TopUpModal(
    viewModel: EazyPayViewModel,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val quickAmounts = listOf("500", "1000", "2000", "5000")
    val clipboardManager = LocalClipboardManager.current

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
                    Text("Top-Up Wallet", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mock Bank Transfer Instructions
                Card(
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    border = BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("MOCK BANK TRANSFER TO LOAD FUNDS", color = PrimaryTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Bank: Access Bank PLC (EazyPay)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Account: 0148500047", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Copy",
                                color = PrimaryTeal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { clipboardManager.setText(AnnotatedString("0148500047")) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Custom amount field
                Text("ENTER AMOUNT", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
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

                Spacer(modifier = Modifier.height(12.dp))

                // Chips row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickAmounts.forEach { amount ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Surface)
                                .border(1.dp, Border, RoundedCornerShape(8.dp))
                                .clickable { amountText = amount }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("₦$amount", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.topUpWallet(amount)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    enabled = amountText.isNotBlank()
                ) {
                    Text("I've made the transfer", color = Background, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 6. PAYMENT SUCCESS FULLSCREEN SCREEN/OVERLAY
@Composable
fun PaymentSuccessModal(
    amount: Double,
    vendorName: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.size(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Large Checkmark Pulse
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Success.copy(alpha = 0.15f))
                        .border(2.dp, Success, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success check",
                        tint = Success,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Payment sent!",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "-₦${String.format("%,.2f", amount)}",
                    color = Danger,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .background(Success.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "✓ Offline Verified & Encrypted",
                        color = Success,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    border = BorderStroke(1.dp, Border),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Recipient", color = TextSecondary, fontSize = 13.sp)
                            Text(vendorName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Transaction ID", color = TextSecondary, fontSize = 13.sp)
                            Text("TXN·" + System.currentTimeMillis().toString().takeLast(6), color = TextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Status", color = TextSecondary, fontSize = 13.sp)
                            Text("Success (Queued)", color = Success, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
            ) {
                Text("Done", color = Background, fontWeight = FontWeight.Bold)
            }
        }
    }
}
