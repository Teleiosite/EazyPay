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
                    val displayTxRef = if (tx.txRef.isNotEmpty()) tx.txRef else "TXN-${tx.timestamp / 10000}"
                    val displayFee = if (tx.fee > 0.0) "₦${String.format("%.2f", tx.fee)}" else "Free (Zero-Fee Pilot)"
                    val displayPayer = if (tx.payerId.isNotEmpty()) tx.payerId else "EP-0047"
                    val displaySecurity = if (tx.signature.isNotEmpty()) "ECDSA-SHA256 Signed Chain" else "AES-256 Signed Ledger"

                    ReceiptDetailRow("Transaction ID", displayTxRef)
                    ReceiptDetailRow("Timestamp", java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(tx.timestamp)))
                    ReceiptDetailRow("Category", tx.category.replaceFirstChar { it.uppercase() })
                    ReceiptDetailRow("Campus Location", tx.campusId)
                    ReceiptDetailRow("Account ID", displayPayer)
                    ReceiptDetailRow("System Fee", displayFee)
                    ReceiptDetailRow("Security Standard", displaySecurity)
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
    val isLedgerSecure by viewModel.isLedgerSecure.collectAsState()
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
                    Pair("Registered NFT cards", Icons.Outlined.Nfc),
                    Pair("Biometrics lock", Icons.Outlined.Fingerprint),
                    Pair("Change security PIN", Icons.Outlined.Lock),
                    Pair("Ledger Security Audit", Icons.Outlined.Shield),
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
                                    "Registered NFC cards", "Registered NFT cards" -> "nfc"
                                    "Biometrics lock" -> "biometrics"
                                    "Change security PIN" -> "pin"
                                    "Ledger Security Audit" -> "ledger_audit"
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

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "CRYPTOGRAPHIC LEDGER TELEMETRY",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Public key row
                        val fullPubKey = viewModel.getDevicePublicKeyBase64()
                        val shortPubKey = if (fullPubKey.length > 20) {
                            fullPubKey.take(10) + "..." + fullPubKey.takeLast(10)
                        } else {
                            fullPubKey
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ECDSA OFFLINE SIGNER KEY (PUBLIC)", color = TextSecondary, fontSize = 9.sp)
                                Text(
                                    text = shortPubKey,
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = { clipboardManager.setText(AnnotatedString(fullPubKey)) },
                                colors = ButtonDefaults.buttonColors(containerColor = Border),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Copy Key", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = Border, thickness = 1.dp)

                        // Block Hash chain info
                        val ledgerColor = if (isLedgerSecure) Success else Danger
                        val ledgerText = if (isLedgerSecure) "SHA-256 Hash Chain: VERIFIED SECURE" else "LEDGER INTEGRITY BREACHED"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Ledger Integrity",
                                tint = ledgerColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("LEDGER INTEGRITY PROTOCOL", color = TextSecondary, fontSize = 9.sp)
                                Text(
                                    text = ledgerText,
                                    color = ledgerColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Divider(color = Border, thickness = 1.dp)

                        // Physical NFC info
                        val nfcAvailable = viewModel.isPhysicalNfcAvailable()
                        val nfcEnabled = viewModel.isPhysicalNfcEnabled()
                        val nfcStatusText = when {
                            !nfcAvailable -> "Hardware Not Found (Simulation Active)"
                            nfcEnabled -> "Physical NFC Hardware Active"
                            else -> "NFC Hardware Disabled"
                        }
                        val nfcColor = if (nfcAvailable && nfcEnabled) Success else PrimaryTeal

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = "NFC Hardware",
                                tint = nfcColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("PHYSICAL NFC DIAGNOSTICS", color = TextSecondary, fontSize = 9.sp)
                                Text(
                                    text = nfcStatusText,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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
            "ledger_audit" -> LedgerSecurityAuditModal(viewModel = viewModel, onDismiss = { activeModal = null })
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

                Spacer(modifier = Modifier.height(4.dp))

                val bioStatus = viewModel.getBiometricStatus()
                val bioColor = if (bioStatus.contains("Active")) Success else PrimaryTeal

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface, RoundedCornerShape(8.dp))
                        .border(1.dp, Border, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometrics Hardware",
                        tint = bioColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("PHYSICAL BIOMETRICS DIAGNOSTICS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = bioStatus,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

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
fun LedgerSecurityAuditModal(
    viewModel: EazyPayViewModel,
    onDismiss: () -> Unit
) {
    val isSecure by viewModel.isLedgerSecure.collectAsState()
    val offlineSpent by viewModel.offlineSpent.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val devicePubKey = viewModel.getDevicePublicKeyBase64()

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
                .clickable(enabled = false) {}
                .border(1.dp, Border, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ledger Security Audit", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Security Status Box
                val cardColor = if (isSecure) Success.copy(alpha = 0.1f) else Danger.copy(alpha = 0.1f)
                val borderColor = if (isSecure) Success else Danger
                val statusText = if (isSecure) "LEDGER INTEGRITY VERIFIED" else "LEDGER COMPROMISED"
                val statusDesc = if (isSecure) {
                    "All Room transaction records, block chain continuation checks, and cryptographic signatures (ECDSA-SHA256) match physical device keys perfectly."
                } else {
                    "Warning: A transaction record payload was altered directly in SQLite, causing hash-continuation check to fail. Wallet balances and tap-payments are locked."
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardColor, RoundedCornerShape(16.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isSecure) Icons.Default.Shield else Icons.Default.Warning,
                        contentDescription = "Status Icon",
                        tint = borderColor,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = statusText,
                        color = borderColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = statusDesc,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Diagnostics Details Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("SECURITY SCHEMAS & METRICS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    // Detail items
                    AuditMetricRow("Cumulative Offline Spent", "₦${String.format("%.2f", offlineSpent)} / ₦5,000.00 Max")
                    AuditMetricRow("Database Block Count", "${transactions.size} records")
                    AuditMetricRow("Block Signature Standard", "ECDSA secp256k1 (SHA256)")
                }

                // Public Key block
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("DEVICE HARDWARE PUBLIC KEY", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface, RoundedCornerShape(8.dp))
                            .border(1.dp, Border, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = devicePubKey,
                            color = PrimaryTeal,
                            fontSize = 9.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            maxLines = 3
                        )
                    }
                }

                HorizontalDivider(color = Border)

                // Simulation Controls Header
                Text(
                    text = "ATTACK & RECOVERY SIMULATION",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Attack Button
                    Button(
                        onClick = { viewModel.tamperLastTransaction() },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Danger.copy(alpha = 0.2f), contentColor = Danger),
                        border = BorderStroke(1.dp, Danger.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = "Attack", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tamper DB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Repair Button
                    Button(
                        onClick = { viewModel.repairLedgerIntegrity() },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Success.copy(alpha = 0.2f), contentColor = Success),
                        border = BorderStroke(1.dp, Success.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.Build, contentDescription = "Repair", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Repair Chain", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("Close Panel", color = Background, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AuditMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(8.dp))
            .border(1.dp, Border, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ChangePinModal(
    viewModel: EazyPayViewModel,
    onDismiss: () -> Unit
) {
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
                                !viewModel.verifyPin(currentPinInput) -> {
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
    val studentUser by viewModel.student.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var step by remember { mutableStateOf(1) } // 1: Enter Amount, 2: Gateway/Transfer, 3: Card OTP, 4: Success
    var paymentMethod by remember { mutableStateOf("card") } // "card" or "bank"
    var amountText by remember { mutableStateOf("") }
    val quickAmounts = listOf("500", "1000", "2000", "5000")

    // Card Details States
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var cardPin by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf(studentUser.name) }
    var isProcessingPayment by remember { mutableStateOf(false) }

    // OTP States
    var otpCode by remember { mutableStateOf("") }
    var otpCountdown by remember { mutableStateOf(59) }
    var otpError by remember { mutableStateOf(false) }

    // Bank Transfer States
    var isCheckingTransfer by remember { mutableStateOf(false) }
    var transferReceived by remember { mutableStateOf(false) }

    // Success state details
    var transactionRef by remember { mutableStateOf("") }

    // Helper functions for formatting
    fun detectCardBrand(num: String): String {
        val clean = num.replace(" ", "")
        return when {
            clean.startsWith("4") -> "Visa"
            clean.startsWith("5") || (clean.length >= 2 && clean.substring(0, 2).toIntOrNull() in 51..55) -> "Mastercard"
            clean.startsWith("506") || clean.startsWith("5078") || clean.startsWith("650") -> "Verve"
            else -> "Unknown"
        }
    }

    fun formatCardNumber(num: String): String {
        val clean = num.replace(" ", "").take(16)
        val sb = StringBuilder()
        for (i in clean.indices) {
            sb.append(clean[i])
            if ((i + 1) % 4 == 0 && i < clean.lastIndex) {
                sb.append(" ")
            }
        }
        return sb.toString()
    }

    fun formatExpiry(exp: String): String {
        val clean = exp.replace("/", "").take(4)
        return when {
            clean.length >= 3 -> "${clean.substring(0, 2)}/${clean.substring(2)}"
            else -> clean
        }
    }

    // OTP countdown timer
    LaunchedEffect(step) {
        if (step == 3) {
            otpCountdown = 59
            while (otpCountdown > 0) {
                delay(1000)
                otpCountdown--
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { if (!isProcessingPayment && !isCheckingTransfer && step != 4) onDismiss() },
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
                // Header (No close button if processing)
                if (step != 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (step == 1) Icons.Default.AddCard else Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = PrimaryTeal,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        if (!isProcessingPayment && !isCheckingTransfer) {
                                            if (step > 1) step-- else onDismiss()
                                        }
                                    }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = when (step) {
                                    1 -> "Top-Up Wallet"
                                    2 -> if (paymentMethod == "card") "Secure Card Checkout" else "Dynamic Bank Transfer"
                                    3 -> "Secure OTP Verification"
                                    else -> "Top-up Wallet"
                                },
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (!isProcessingPayment && !isCheckingTransfer) {
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

                // STEP 1: Enter Amount & Select Payment Mode
                if (step == 1) {
                    Text("ENTER TOP-UP AMOUNT", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { char -> char.isDigit() } },
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
                        singleLine = true,
                        placeholder = { Text("Min: ₦100", color = TextSecondary) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Amounts Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickAmounts.forEach { amt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (amountText == amt) PrimaryTeal.copy(alpha = 0.15f) else Surface)
                                    .border(
                                        1.dp,
                                        if (amountText == amt) PrimaryTeal else Border,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { amountText = amt }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("₦$amt", color = if (amountText == amt) PrimaryTeal else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("SELECT PAYMENT METHOD", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Payment Method Options
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (paymentMethod == "card") Surface else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (paymentMethod == "card") PrimaryTeal else Border,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { paymentMethod = "card" }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryTeal.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CreditCard, contentDescription = "Card", tint = PrimaryTeal, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Card Payment", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Pay securely with Visa, Mastercard, or Verve", color = TextSecondary, fontSize = 11.sp)
                            }
                            RadioButton(
                                selected = paymentMethod == "card",
                                onClick = { paymentMethod = "card" },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryTeal)
                            )
                        }

                        // Bank Transfer Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (paymentMethod == "bank") Surface else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (paymentMethod == "bank") PrimaryTeal else Border,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { paymentMethod = "bank" }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryTeal.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = "Bank", tint = PrimaryTeal, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Instant Bank Transfer", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Generate a unique virtual account to transfer Naira", color = TextSecondary, fontSize = 11.sp)
                            }
                            RadioButton(
                                selected = paymentMethod == "bank",
                                onClick = { paymentMethod = "bank" },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryTeal)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val finalAmount = amountText.toDoubleOrNull() ?: 0.0
                    Button(
                        onClick = {
                            if (finalAmount >= 100.0) {
                                step = 2
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        enabled = finalAmount >= 100.0
                    ) {
                        Text(
                            text = if (paymentMethod == "card") "Proceed to Paystack Card Gateway" else "Generate Virtual Account",
                            color = Background,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // STEP 2A: Paystack Card Gateway Form
                else if (step == 2 && paymentMethod == "card") {
                    val finalAmount = amountText.toDoubleOrNull() ?: 0.0
                    
                    Text("SECURE GATEWAY CHECKOUT", color = PrimaryTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Amount to Pay: ₦${String.format("%,.2f", finalAmount)}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Card Number Field
                    Text("CARD NUMBER", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = {
                            val clean = it.replace(" ", "").filter { char -> char.isDigit() }
                            if (clean.length <= 16) {
                                cardNumber = formatCardNumber(it)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("0000 0000 0000 0000", color = TextSecondary) },
                        trailingIcon = {
                            val brand = detectCardBrand(cardNumber)
                            if (brand != "Unknown") {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(PrimaryTeal.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(brand, color = PrimaryTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Icon(Icons.Default.CreditCard, contentDescription = "Card", tint = TextSecondary)
                            }
                        },
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

                    // Expiry and CVV Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("EXPIRY DATE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = cardExpiry,
                                onValueChange = {
                                    val clean = it.replace("/", "").filter { char -> char.isDigit() }
                                    if (clean.length <= 4) {
                                        cardExpiry = formatExpiry(it)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("MM/YY", color = TextSecondary) },
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

                        Column(modifier = Modifier.weight(1f)) {
                            Text("CVV SECURITY CODE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = cardCvv,
                                onValueChange = {
                                    val clean = it.filter { char -> char.isDigit() }
                                    if (clean.length <= 3) {
                                        cardCvv = clean
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("123", color = TextSecondary) },
                                visualTransformation = PasswordVisualTransformation(),
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
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Card PIN
                    Text("CARD 4-DIGIT PIN", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = cardPin,
                        onValueChange = {
                            val clean = it.filter { char -> char.isDigit() }
                            if (clean.length <= 4) {
                                cardPin = clean
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Enter Card ATM PIN", color = TextSecondary) },
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Paystack Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Secure", tint = Success, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Secured by Paystack | PCIDSS Level 1 Certified Gateway", color = TextSecondary, fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val isCardValid = cardNumber.replace(" ", "").length == 16 &&
                            cardExpiry.length == 5 &&
                            cardCvv.length == 3 &&
                            cardPin.length == 4

                    Button(
                        onClick = {
                            isProcessingPayment = true
                            coroutineScope.launch {
                                delay(1800) // Simulate gateway initialization
                                isProcessingPayment = false
                                step = 3 // Move to OTP
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        enabled = isCardValid && !isProcessingPayment
                    ) {
                        if (isProcessingPayment) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Background, strokeWidth = 2.dp)
                        } else {
                            Text("Pay ₦${String.format("%,.2f", finalAmount)} Securely", color = Background, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // STEP 2B: Instant Virtual Bank Transfer details
                else if (step == 2 && paymentMethod == "bank") {
                    val finalAmount = amountText.toDoubleOrNull() ?: 0.0
                    val phoneClean = studentUser.phone.replace("+234", "").replace(" ", "").trim()
                    val virtualAccount = "992" + phoneClean.takeLast(7) // Moniepoint routing prefix + phone suffix

                    Text("INSTANT NIRE WEB BANK TRANSFER", color = PrimaryTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Please transfer ₦${String.format("%,.2f", finalAmount)} to your unique Babcock-EazyPay virtual settlement account:", color = TextSecondary, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Account Container card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        border = BorderStroke(1.dp, Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("SETTLEMENT INSTITUTION", color = PrimaryTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("Moniepoint Microfinance Bank", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("ACCOUNT HOLDER", color = PrimaryTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("EazyPay Babcock - ${studentUser.name}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("VIRTUAL ACCOUNT NUMBER", color = PrimaryTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(virtualAccount, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PrimaryTeal.copy(alpha = 0.15f))
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(virtualAccount))
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Copy", color = PrimaryTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // USSD code helper for quick campus bank transfers
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        border = BorderStroke(1.dp, Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = "USSD", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Or Dial USSD Transfer Code: *402*${virtualAccount}*${amountText}#",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Active monitoring loader
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Success.copy(alpha = alpha))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Listening for real-time local credit notification...",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isCheckingTransfer = true
                            coroutineScope.launch {
                                delay(2200) // Mock lookup querying central ledger logs
                                isCheckingTransfer = false
                                transactionRef = "EP-TX-BNK" + (100000..999999).random()
                                viewModel.topUpWallet(finalAmount)
                                step = 4 // Success Screen!
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        enabled = !isCheckingTransfer
                    ) {
                        if (isCheckingTransfer) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Background, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Checking NIP Settle Ledger...", color = Background, fontSize = 14.sp)
                            }
                        } else {
                            Text("Verify Bank Deposit Status", color = Background, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // STEP 3: OTP Code Entry Panel (Card Payment Mode)
                else if (step == 3) {
                    val finalAmount = amountText.toDoubleOrNull() ?: 0.0
                    Text("3D SECURE OTP AUTHORIZATION", color = PrimaryTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("We've simulated sending a 6-digit verification OTP to registration number ${studentUser.phone.take(6)}···${studentUser.phone.takeLast(4)}", color = TextSecondary, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = {
                            if (it.filter { char -> char.isDigit() }.length <= 6) {
                                otpCode = it.filter { char -> char.isDigit() }
                                otpError = false
                            }
                        },
                        label = { Text("Enter 6-Digit OTP") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = if (otpError) Warning else PrimaryTeal,
                            unfocusedBorderColor = if (otpError) Warning else Border,
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface
                        ),
                        singleLine = true,
                        placeholder = { Text("e.g. 123456 (Use 123456 to verify)", color = TextSecondary) }
                    )

                    if (otpError) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Incorrect OTP entered. Enter code 123456 for test authorization.", color = Warning, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Countdown indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (otpCountdown > 0) "Resend SMS code in ${otpCountdown}s" else "Didn't receive SMS?",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        if (otpCountdown == 0) {
                            Text(
                                "Resend OTP",
                                color = PrimaryTeal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    otpCountdown = 59
                                    otpCode = ""
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            if (otpCode == "123456" || otpCode.length == 6) {
                                isProcessingPayment = true
                                coroutineScope.launch {
                                    delay(2000) // Authenticating with Bank 3D Secure
                                    isProcessingPayment = false
                                    transactionRef = "EP-TX-CRD" + (100000..999999).random()
                                    viewModel.topUpWallet(finalAmount)
                                    step = 4 // Success Screen!
                                }
                            } else {
                                otpError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        enabled = otpCode.length == 6 && !isProcessingPayment
                    ) {
                        if (isProcessingPayment) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Background, strokeWidth = 2.dp)
                        } else {
                            Text("Verify & Complete Top-Up", color = Background, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // STEP 4: Beautiful Checkout Success Screen
                else if (step == 4) {
                    val finalAmount = amountText.toDoubleOrNull() ?: 0.0
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Animated pulsing success tick
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(Success.copy(alpha = 0.15f))
                                .border(2.dp, Success, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Success", tint = Success, modifier = Modifier.size(48.dp))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Top-Up Successful!", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Funds loaded securely into your offline ledger balance", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Transaction details ticket
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            border = BorderStroke(1.dp, Border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("CREDITED AMOUNT", color = TextSecondary, fontSize = 11.sp)
                                    Text("₦${String.format("%,.2f", finalAmount)}", color = Success, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Divider(color = Border.copy(alpha = 0.5f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("TRANSACTION REFERENCE", color = TextSecondary, fontSize = 11.sp)
                                    Text(transactionRef, color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                                }
                                Divider(color = Border.copy(alpha = 0.5f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("GATEWAY CHANNEL", color = TextSecondary, fontSize = 11.sp)
                                    Text(if (paymentMethod == "card") "Paystack (Card)" else "NIP Instant Direct", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Divider(color = Border.copy(alpha = 0.5f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("LEDGER SYNC STATUS", color = TextSecondary, fontSize = 11.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Success))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Signed Offline Ledger", color = Success, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
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
                            Text("Close", color = Background, fontWeight = FontWeight.Bold)
                        }
                    }
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
