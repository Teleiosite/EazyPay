package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Offer
import kotlinx.coroutines.delay
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
            if (isOffline) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Background)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OfflineStatusBar(isOffline = isOffline, isSyncing = isSyncing)
                }
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
                "profile" -> StudentProfileScreen(
                    viewModel = viewModel,
                    onSignOut = onSignOut,
                    onSupportClick = { showSupport = true }
                )
            }

            // Top-up Modal Bottom Sheet Overlay
            if (showTopUp) {
                TopUpModal(
                    viewModel = viewModel,
                    onDismiss = { showTopUp = false }
                )
            }

            // Full Support Hub Overlay
            if (showSupport) {
                SupportHubModal(
                    viewModel = viewModel,
                    onDismiss = { showSupport = false }
                )
            }
        }
    }
}

@Composable
fun SupportHubModal(
    viewModel: EazyPayViewModel,
    onDismiss: () -> Unit
) {
    var showChat by remember { mutableStateOf(false) }
    var chatInput by remember { mutableStateOf("") }
    val chatMessages by viewModel.chatMessages.collectAsState()
    
    val faqs = listOf(
        "What is EazyPay offline mode?" to "EazyPay stores a signed cryptographic token issued at registration. Terminals can verify this token 100% offline using a preloaded public key, allowing you to pay with zero internet access.",
        "How do I link an NFC card/sticker?" to "At the campus registration point, an EazyPay agent will tap your physical card/sticker on their administrator device. This instantly links the chip's unique serial number to your wallet account.",
        "How do I dispute a transaction?" to "Go to the History tab, tap on any transaction to view its detailed receipt, and select 'Dispute Transaction' to flag it for administrator review."
    )

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
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clickable(enabled = false) {}
                .border(1.dp, Border, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showChat) "Support Live Chat" else "Babcock Support Hub",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!showChat) {
                    // MAIN SUPPORT VIEW
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Quick Contact Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Surface),
                                border = BorderStroke(1.dp, Border),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showChat = true }
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Chat, contentDescription = "Live Chat", tint = PrimaryTeal, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Live Chat", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Simulated Agent", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Surface),
                                border = BorderStroke(1.dp, Border),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Call, contentDescription = "WhatsApp", tint = Success, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("WhatsApp", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("+234 800 EAZYPAY", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }

                        // FAQs Section
                        Text(
                            "CACHED OFFLINE FAQS",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        faqs.forEach { (q, a) ->
                            var expanded by remember { mutableStateOf(false) }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Surface),
                                border = BorderStroke(1.dp, Border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = !expanded }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(q, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        Icon(
                                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = "Expand",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    if (expanded) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(a, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // LIVE CHAT SIMULATOR
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Surface, RoundedCornerShape(12.dp))
                                .border(1.dp, Border, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            val listState = rememberLazyListState()
                            LaunchedEffect(chatMessages.size) {
                                if (chatMessages.isNotEmpty()) {
                                    listState.animateScrollToItem(chatMessages.size - 1)
                                }
                            }
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(chatMessages) { chat ->
                                    val isUser = chat.sender == "User"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(
                                                    RoundedCornerShape(
                                                        topStart = 12.dp,
                                                        topEnd = 12.dp,
                                                        bottomStart = if (isUser) 12.dp else 0.dp,
                                                        bottomEnd = if (isUser) 0.dp else 12.dp
                                                    )
                                                )
                                                .background(if (isUser) PrimaryTeal else Border)
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                                .widthIn(max = 200.dp)
                                        ) {
                                            Text(
                                                text = chat.message,
                                                color = if (isUser) Background else TextPrimary,
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = { Text("Ask Support...", color = TextMuted, fontSize = 13.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = PrimaryTeal,
                                    unfocusedBorderColor = Border,
                                    focusedContainerColor = Surface,
                                    unfocusedContainerColor = Surface
                                )
                            )
                            IconButton(
                                onClick = {
                                    if (chatInput.isNotBlank()) {
                                        viewModel.sendChatMessage(chatInput)
                                        chatInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(PrimaryTeal, RoundedCornerShape(12.dp))
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = Background)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { showChat = false }) {
                            Text("← Back to Support Hub", color = PrimaryTeal, fontSize = 12.sp)
                        }
                    }
                }
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
    val isOffline by viewModel.isOffline.collectAsState()
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

        // Low Balance Warning Banner
        if (studentUser.balance < 500.0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Warning.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Warning.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Warning, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Low Balance Alert",
                                color = Warning,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Your wallet balance is below ₦500.00. Top up now to ensure uninterrupted offline payments.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                        Button(
                            onClick = onTopUpClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Warning),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Top Up", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
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

                        // Status footer: Ready for NFC Tap & Connection status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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

                            // Connection toggle button inside Wallet Card
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
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isOffline) Warning else Success)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOffline) "Offline" else "Online Sync",
                                    color = if (isOffline) Warning else Success,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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
    var showFailureReason by remember { mutableStateOf<String?>(null) }
    
    val pinLength by viewModel.pinBuffer.map { it.length }.collectAsState(0)
    val isPinError by viewModel.pinError.collectAsState()
    val isLockedOut by viewModel.isLockedOut.collectAsState()
    val pinAttemptsRemaining by viewModel.pinAttemptsRemaining.collectAsState()

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
                    .clickable {
                        if (isLockedOut) {
                            // Already locked out
                        } else if (studentUser.balance < 10.0) {
                            showFailureReason = "Your wallet balance is below the campus minimum balance of ₦10. Please top up your wallet to transact."
                        } else if (studentUser.balance < 210.0) {
                            showFailureReason = "Insufficient funds. The standard transaction amount requires ₦210 (including service fees)."
                        } else {
                            isVerifyingPin = true
                        }
                    }
                    .padding(vertical = 16.dp)
            ) {
                NfcPulsingRing()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isLockedOut) "🔐 Device Pin Locked" else "Tap here to simulate terminal contact",
                    color = if (isLockedOut) Danger else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (isLockedOut) androidx.compose.ui.text.style.TextDecoration.None else androidx.compose.ui.text.style.TextDecoration.Underline
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
        if (isVerifyingPin && !isLockedOut) {
            PinEntryModal(
                pinLength = pinLength,
                isError = isPinError,
                attemptsRemaining = pinAttemptsRemaining,
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

        // Cooldown Lockout Screen
        if (isLockedOut) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Danger.copy(alpha = 0.15f), CircleShape)
                            .border(2.dp, Danger, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Danger, modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Security PIN Locked",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "For security reasons, your EazyPay device has been frozen due to 3 consecutive wrong PIN entries. Cooldown active.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { viewModel.resetPinAttempts() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simulate Admin Bypass Unlock", color = Background, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Payment Failure Screen
        showFailureReason?.let { reason ->
            AlertDialog(
                onDismissRequest = { showFailureReason = null },
                containerColor = Surface,
                title = { Text("Payment Blocked", color = Danger, fontWeight = FontWeight.Bold) },
                text = { Text(reason, color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = { showFailureReason = null },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                    ) {
                        Text("Okay", color = Background)
                    }
                }
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
    val disputedTransactions by viewModel.disputedTransactions.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Paid", "Received", "Pending"
    var selectedCategory by remember { mutableStateOf("All") } // "All", "Food", "Transport", "Print", "Topup"
    var selectedTransaction by remember { mutableStateOf<com.example.data.TransactionEntity?>(null) }

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

        // Category Filter
        val matchesCategory = if (selectedCategory == "All") true else tx.category.lowercase() == selectedCategory.lowercase()

        matchesSearch && matchesFilter && matchesCategory
    }

    // Analytics Calculation (e.g. Month Spend)
    val paidTransactions = transactions.filter { it.isDebit && it.category != "topup" }
    val totalSpend = paidTransactions.sumOf { it.amount }
    val foodSpend = paidTransactions.filter { it.category == "food" }.sumOf { it.amount }
    val transportSpend = paidTransactions.filter { it.category == "transport" }.sumOf { it.amount }
    val printSpend = paidTransactions.filter { it.category == "print" }.sumOf { it.amount }

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

        Spacer(modifier = Modifier.height(12.dp))

        // Spend breakdown mini card
        if (transactions.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Border),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("MONTHLY CASHLESS DISBURSEMENT", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Total: ₦${String.format("%,.2f", totalSpend)}",
                            color = PrimaryTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Segmented horizontal progress bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Border)
                    ) {
                        val scaleFactor = if (totalSpend > 0) 1.0 else 1.0
                        val fWeight = if (totalSpend > 0) (foodSpend / totalSpend).toFloat().coerceAtLeast(0.05f) else 0.4f
                        val tWeight = if (totalSpend > 0) (transportSpend / totalSpend).toFloat().coerceAtLeast(0.05f) else 0.3f
                        val pWeight = if (totalSpend > 0) (printSpend / totalSpend).toFloat().coerceAtLeast(0.05f) else 0.3f
                        
                        Box(modifier = Modifier.weight(fWeight).fillMaxHeight().background(PrimaryTeal))
                        Box(modifier = Modifier.weight(tWeight).fillMaxHeight().background(Warning))
                        Box(modifier = Modifier.weight(pWeight).fillMaxHeight().background(Danger))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Legend Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem("Food (₦${String.format("%.0f", foodSpend)})", PrimaryTeal)
                        LegendItem("Transport (₦${String.format("%.0f", transportSpend)})", Warning)
                        LegendItem("Print (₦${String.format("%.0f", printSpend)})", Danger)
                    }
                }
            }
        }

        // Status filters horizontal row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = filter,
                        color = if (active) Background else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Category filters horizontal row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val categories = listOf("All", "Food", "Transport", "Print", "Topup")
            categories.forEach { cat ->
                val active = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (active) Warning else Surface)
                        .border(1.dp, if (active) Warning else Border, RoundedCornerShape(100.dp))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (cat == "Topup") "Top-Up" else cat,
                        color = if (active) Color.Black else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                        syncStatus = tx.syncStatus,
                        onClick = { selectedTransaction = tx }
                    )
                }
            }
        }
    }

    // Receipt details drawer / modal
    selectedTransaction?.let { tx ->
        TransactionReceiptModal(
            tx = tx,
            isDisputed = disputedTransactions.contains(tx.id),
            onDispute = { viewModel.disputeTransaction(tx.id) },
            onDismiss = { selectedTransaction = null }
        )
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
fun TransactionReceiptModal(
    tx: com.example.data.TransactionEntity,
    isDisputed: Boolean,
    onDispute: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDebit = tx.isDebit
    val amountColor = if (isDebit) Danger else Success
    val prefix = if (isDebit) "-₦" else "+₦"
    var showShareMessage by remember { mutableStateOf(false) }

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
                // Ticket head
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Transaction Receipt", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Icon
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

                // Amount
                Text(
                    text = "$prefix${String.format("%,.2f", tx.amount)}",
                    color = amountColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(tx.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isDisputed) "⚠️ FLAG DISPUTED" else "Verified Offline ✓",
                    color = if (isDisputed) Warning else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Info details
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReceiptDetailRow("Transaction ID", "TXN-${tx.timestamp / 10000}")
                    ReceiptDetailRow("Timestamp", java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(tx.timestamp)))
                    ReceiptDetailRow("Category", tx.category.replaceFirstChar { it.uppercase() })
                    ReceiptDetailRow("Security Standard", "AES-256 GCM Signed Ledger")
                    ReceiptDetailRow("Status", tx.syncStatus)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            onDispute()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDisputed) Danger.copy(alpha = 0.15f) else Warning.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, if (isDisputed) Danger else Warning),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isDisputed
                    ) {
                        Text(
                            text = if (isDisputed) "Disputed" else "Dispute",
                            color = if (isDisputed) Danger else Warning,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = { showShareMessage = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Share Receipt", color = Background, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                if (showShareMessage) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Receipt cryptographic link copied to clipboard!",
                        color = Success,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiptDetailRow(label: String, valStr: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(valStr, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

// 4. STUDENT PROFILE SETTINGS SCREEN
@Composable
fun StudentProfileScreen(
    viewModel: EazyPayViewModel,
    onSignOut: () -> Unit,
    onSupportClick: () -> Unit
) {
    val studentUser by viewModel.student.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var activeModal by remember { mutableStateOf<String?>(null) } // "personal", "nfc", "biometrics", "pin", "help", "privacy"

    Box(modifier = Modifier.fillMaxSize()) {
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
                    text = "Nigeria • +234 ${studentUser.phone.removePrefix("+234").trim()}",
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                activeModal = when (item.first) {
                                    "Personal details" -> "personal"
                                    "Registered NFC cards" -> "nfc"
                                    "Biometrics lock" -> "biometrics"
                                    "Change security PIN" -> "pin"
                                    "Help Center & FAQ" -> "help"
                                    "Privacy Policy & Terms" -> "privacy"
                                    else -> null
                                }
                            },
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

        // Settings Overlay Modals
        when (activeModal) {
            "personal" -> PersonalDetailsModal(viewModel = viewModel, onDismiss = { activeModal = null })
            "nfc" -> NfcCardsModal(viewModel = viewModel, onDismiss = { activeModal = null })
            "biometrics" -> BiometricsLockModal(viewModel = viewModel, onDismiss = { activeModal = null })
            "pin" -> ChangePinModal(viewModel = viewModel, onDismiss = { activeModal = null })
            "help" -> HelpCenterModal(viewModel = viewModel, onDismiss = { activeModal = null }, onOpenChat = {
                activeModal = null
                onSupportClick()
            })
            "privacy" -> PrivacyPolicyModal(onDismiss = { activeModal = null })
        }
    }
}

@Composable
fun PersonalDetailsModal(
    viewModel: EazyPayViewModel,
    onDismiss: () -> Unit
) {
    val studentUser by viewModel.student.collectAsState()
    var name by remember { mutableStateOf(studentUser.name) }
    var email by remember { mutableStateOf(studentUser.email) }
    var phone by remember { mutableStateOf(studentUser.phone.removePrefix("+234").trim()) }
    var department by remember { mutableStateOf(studentUser.department) }
    var level by remember { mutableStateOf(studentUser.level) }

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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Personal Details", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = Border,
                        focusedContainerColor = Surface
                    )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Babcock Email") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = Border,
                        focusedContainerColor = Surface
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    prefix = { Text("+234 ", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = Border,
                        focusedContainerColor = Surface
                    )
                )

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = Border,
                        focusedContainerColor = Surface
                    )
                )

                OutlinedTextField(
                    value = level,
                    onValueChange = { level = it },
                    label = { Text("Level") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = Border,
                        focusedContainerColor = Surface
                    )
                )

                Button(
                    onClick = {
                        viewModel.updateStudentDetails(name, email, "+234 $phone", department, level)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("Save Changes", color = Background, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun NfcCardsModal(
    viewModel: EazyPayViewModel,
    onDismiss: () -> Unit
) {
    val registeredCards by viewModel.registeredCards.collectAsState()
    var isScanning by remember { mutableStateOf(false) }
    var scanStatus by remember { mutableStateOf("Place new tag against back of device") }
    var newCardName by remember { mutableStateOf("") }

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
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Registered NFC Cards", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                if (!isScanning) {
                    Text(
                        "Manage physical NFC/NFT cards, stickers or student keyfobs linked to your EazyPay account.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("LINKED CARDS & STICKERS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(registeredCards) { card ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Surface),
                                border = BorderStroke(1.dp, Border),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Icon(Icons.Default.Contactless, contentDescription = "Card", tint = PrimaryTeal, modifier = Modifier.size(20.dp))
                                        Column {
                                            Text(card, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("Active • Secure Link", color = Success, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeNfcCard(card) }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Danger, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = newCardName,
                        onValueChange = { newCardName = it },
                        placeholder = { Text("e.g. Back-up Wallet Sticker", color = TextMuted) },
                        label = { Text("Card/Sticker Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Border,
                            focusedContainerColor = Surface
                        )
                    )

                    Button(
                        onClick = {
                            if (newCardName.isNotBlank()) {
                                isScanning = true
                                scanStatus = "Searching for NFC Antenna broadcast..."
                            }
                        },
                        enabled = newCardName.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                    ) {
                        Text("Register New NFC Tag", color = Background, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Scanning Simulation
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Pulsing antenna icon
                        val infiniteTransition = rememberInfiniteTransition(label = "nfc_pulse")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "nfc_scale"
                        )

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(PrimaryTeal.copy(alpha = 0.15f))
                                .border(2.dp, PrimaryTeal, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Nfc,
                                contentDescription = "NFC Pulse",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(48.dp * scale)
                            )
                        }

                        Text(
                            text = scanStatus,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        LaunchedEffect(isScanning) {
                            delay(1800)
                            scanStatus = "NFC Tag Handshake: Verifying ECDSA key pairs..."
                            delay(1800)
                            viewModel.addNfcCard(newCardName)
                            isScanning = false
                            newCardName = ""
                        }

                        Button(
                            onClick = { isScanning = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Border),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel scan", color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BiometricsLockModal(
    viewModel: EazyPayViewModel,
    onDismiss: () -> Unit
) {
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()

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
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Biometric Security", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Fingerprint",
                    tint = PrimaryTeal,
                    modifier = Modifier.size(72.dp)
                )

                Text(
                    text = "Authenticate Offline Tap Payments with Fingerprint / Face ID",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "When enabled, EazyPay leverages Android Biometrics API to authenticate contactless transaction logs directly. This eliminates the need to input your 4-digit security PIN at the vendor terminal during rush hour.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(color = Border)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Biometric Check", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Verify with finger scan", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { viewModel.setBiometricEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Background,
                            checkedTrackColor = PrimaryTeal,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = Surface
                        )
                    )
                }

                Button(
                    onClick = onDismiss,
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

@Composable
fun ChangePinModal(
    viewModel: EazyPayViewModel,
    onDismiss: () -> Unit
) {
    val userPin by viewModel.userPin.collectAsState()
    var currentPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

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
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Change Security PIN", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Text(
                    "Your 4-digit security PIN is used offline to authorize contactless transaction tokens at Babcock terminals.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                if (successMessage.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Success.copy(alpha = 0.15f))
                            .border(1.dp, Success, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(successMessage, color = Success, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                    ) {
                        Text("Close", color = Background)
                    }
                } else {
                    if (errorMessage.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Danger.copy(alpha = 0.15f))
                                .border(1.dp, Danger, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(errorMessage, color = Danger, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    OutlinedTextField(
                        value = currentPinInput,
                        onValueChange = { if (it.length <= 4) currentPinInput = it },
                        label = { Text("Enter Current 4-Digit PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Border,
                            focusedContainerColor = Surface
                        )
                    )

                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 4) newPinInput = it },
                        label = { Text("Enter New 4-Digit PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Border,
                            focusedContainerColor = Surface
                        )
                    )

                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = { if (it.length <= 4) confirmPinInput = it },
                        label = { Text("Confirm New 4-Digit PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Border,
                            focusedContainerColor = Surface
                        )
                    )

                    Button(
                        onClick = {
                            when {
                                currentPinInput != userPin -> {
                                    errorMessage = "Current security PIN is incorrect. Try again."
                                }
                                newPinInput.length != 4 -> {
                                    errorMessage = "New PIN must be exactly 4 digits."
                                }
                                newPinInput != confirmPinInput -> {
                                    errorMessage = "PIN confirmation does not match new PIN."
                                }
                                else -> {
                                    viewModel.setPin(newPinInput)
                                    successMessage = "Your security PIN has been successfully changed offline!"
                                    errorMessage = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                    ) {
                        Text("Change security PIN", color = Background, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HelpCenterModal(
    viewModel: EazyPayViewModel,
    onDismiss: () -> Unit,
    onOpenChat: () -> Unit
) {
    val faqs = listOf(
        Pair("How does offline payment work?", "EazyPay is powered by local cryptographic secure-element simulation. During a contactless NFC tap, the student app signs a ledger token with an ECDSA secp256k1 key. The vendor terminal validates this offline signature against the Babcock registry instantly."),
        Pair("Where can I top up my EazyPay wallet?", "You can perform an instant bank transfer to your unique virtual bank details displayed on the top-up page, or visit any Student Union cafeteria terminal agent kiosk to deposit cash offline."),
        Pair("What if my physical NFC smart card/sticker is lost?", "Block it immediately from this Profile page under 'Registered NFC Cards'. This terminates its cryptographic pairing. You can then register a backup tag or purchase a sticker at the Babcock IT Support desk."),
        Pair("How are transactions synchronized?", "When either the student or vendor terminal regains cellular or Wi-Fi connectivity, the app launches an automatic background worker which synchronizes all offline ledger hashes securely with Babcock settlement servers.")
    )

    var expandedIndex by remember { mutableStateOf<Int?>(null) }

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
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Help Center & FAQ", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Text("Frequently Asked Questions", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(faqs) { idx, faq ->
                        val isExpanded = expandedIndex == idx
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            border = BorderStroke(1.dp, Border),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedIndex = if (isExpanded) null else idx }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(faq.first, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand",
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(faq.second, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Border)

                Text("Need direct assistance? Chat live with EazyPay Support specialists or reach us via WhatsApp support groups.", color = TextSecondary, fontSize = 12.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenChat,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                    ) {
                        Text("Live Support Chat", color = Background, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { /* WhatsApp simulation */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Border),
                        colors = ButtonDefaults.buttonColors(containerColor = Surface)
                    ) {
                        Text("WhatsApp Chat", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyModal(
    onDismiss: () -> Unit
) {
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
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Privacy & Terms", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("1. Cryptographic Ledger Security", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("All offline payment transaction records generated on EazyPay terminals are sealed inside local databases using AES-256-GCM hardware keys. They are synchronized with Babcock centralized systems using authenticated TLS channels.", color = TextSecondary, fontSize = 12.sp)

                    Text("2. Privacy Policies", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("EazyPay does not collect or track student physical location coordinates or background network activity. All logs represent authorized campus transaction fees.", color = TextSecondary, fontSize = 12.sp)

                    Text("3. Babcock Compliance", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("By activating and linking your student physical ID or mobile tag sticker, you authorize Babcock University to settle off-line payments from your registered student e-wallet balance.", color = TextSecondary, fontSize = 12.sp)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("Close", color = Background)
                }
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
