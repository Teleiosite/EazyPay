package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// 1. NFC PULSING RING ANIMATION
@Composable
fun NfcPulsingRing(
    modifier: Modifier = Modifier,
    isListening: Boolean = false,
    text: String = "Hold near terminal"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NFC Ring pulse")
    
    // Animate scale and alpha of 3 nested rings
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1"
    )
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ring 1
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale1)
                .border(2.dp, PrimaryTeal.copy(alpha = alpha1), CircleShape)
        )
        
        // Ring 2
        Box(
            modifier = Modifier
                .fillMaxSize(0.75f)
                .scale(scale2)
                .border(2.dp, PrimaryTeal.copy(alpha = alpha2), CircleShape)
        )

        // Core Circle
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimaryTeal.copy(alpha = 0.3f), Color.Transparent),
                        radius = 200f
                    )
                )
                .border(3.dp, PrimaryTeal, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Sensors else Icons.Default.Nfc,
                    contentDescription = "NFC Icon",
                    tint = PrimaryTeal,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isListening) "LISTENING" else "NFC",
                    color = PrimaryTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

// 2. OFFLINE BADGE / STATUS BANNER
@Composable
fun OfflineStatusBar(
    isOffline: Boolean,
    isSyncing: Boolean = false,
    onSyncClick: () -> Unit = {}
) {
    if (isOffline) {
        Surface(
            color = Warning.copy(alpha = 0.15f),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Warning.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Offline Mode",
                        tint = Warning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "📶 Offline Mode Active",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "Saved Cryptographically",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    } else if (isSyncing) {
        Surface(
            color = PrimaryTeal.copy(alpha = 0.15f),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PrimaryTeal.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = PrimaryTeal,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Auto-Syncing local payments...",
                    color = PrimaryTeal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// 3. TRANSACTION ITEM ROW
@Composable
fun TransactionRow(
    title: String,
    category: String,
    amount: Double,
    isDebit: Boolean,
    timestamp: Long,
    syncStatus: String
) {
    val categoryIcon = when (category) {
        "food" -> "🍲"
        "transport" -> "🛺"
        "print" -> "🖨️"
        else -> "💰"
    }

    val amountColor = if (isDebit) Danger else Success
    val prefix = if (isDebit) "-₦" else "+₦"
    
    val timeString = remember(timestamp) {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        format.format(date)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Category Icon Bubble
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = categoryIcon, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = timeString,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    if (syncStatus == "Synced") Success.copy(alpha = 0.15f) 
                                    else Warning.copy(alpha = 0.15f), 
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = syncStatus,
                                color = if (syncStatus == "Synced") Success else Warning,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Text(
                text = "$prefix${String.format("%,.2f", amount)}",
                color = amountColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 4. MOCK ANALYTICS BAR CHART (FOR VENDOR)
@Composable
fun SimpleBarChart(
    modifier: Modifier = Modifier
) {
    val barData = listOf(0.3f, 0.5f, 0.8f, 0.9f, 0.4f, 0.6f, 0.2f)
    val days = listOf("M", "T", "W", "T", "F", "S", "S")

    Column(modifier = modifier) {
        Text(
            text = "Peak Transaction Hours",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            barData.forEachIndexed { index, value ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(value)
                            .width(16.dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (index == 3) PrimaryTeal else PrimaryTeal.copy(alpha = 0.4f)
                            )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = days[index],
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// 5. PIN KEYPAD
@Composable
fun PinKeypad(
    onChar: (Char) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf('⌫', '0', '✓')
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface)
                            .clickable {
                                when (key) {
                                    '⌫' -> onDelete()
                                    '✓' -> { /* Verify/Done automatically triggers */ }
                                    else -> onChar(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == '⌫') {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Delete",
                                tint = TextPrimary
                            )
                        } else if (key == '✓') {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = PrimaryTeal
                            )
                        } else {
                            Text(
                                text = key.toString(),
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// 6. SHARED PIN ENTRY SHEET MODAL
@Composable
fun PinEntryModal(
    pinLength: Int,
    isError: Boolean,
    onChar: (Char) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Background),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Border, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .background(Border, CircleShape)
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Confirm Transaction PIN",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter your 4-digit security PIN to authorize payment",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val active = index < pinLength
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isError) Danger 
                                    else if (active) PrimaryTeal 
                                    else Border
                                )
                        )
                    }
                }

                if (isError) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Incorrect PIN! Please try again.",
                        color = Danger,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                PinKeypad(onChar = onChar, onDelete = onDelete)

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel authorize", color = Danger, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
