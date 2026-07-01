package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EazyPayViewModel
import com.example.ui.theme.*

@Composable
fun DemoSplitScreen(
    viewModel: EazyPayViewModel,
    onBack: () -> Unit
) {
    val step by viewModel.demoStep.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()
    val studentUser by viewModel.student.collectAsState()
    val vendorUser by viewModel.vendor.collectAsState()

    // Auto-trigger demo on launch
    LaunchedEffect(Unit) {
        viewModel.startDemoFlow()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier
                                .clickable {
                                    viewModel.stopDemoFlow()
                                    onBack()
                                }
                                .padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = "Synchronized Demo",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Offline Cryptographic NFC Exchange",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Warning.copy(alpha = 0.15f))
                            .border(1.dp, Warning.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("📡 Simulation Active", color = Warning, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // STEP INDICATOR (HORIZONTAL STEPS)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val stepNames = listOf("1. Tap", "2. Read", "3. Price", "4. PIN", "5. Done")
                    stepNames.forEachIndexed { idx, name ->
                        val active = step == idx + 1
                        val completed = step > idx + 1
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (active) PrimaryTeal 
                                        else if (completed) Success.copy(alpha = 0.2f) 
                                        else Border
                                    )
                                    .border(
                                        1.dp,
                                        if (active) PrimaryTeal 
                                        else if (completed) Success 
                                        else Color.Transparent,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (completed) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Done",
                                        tint = Success,
                                        modifier = Modifier.size(12.dp)
                                    )
                                } else {
                                    Text(
                                        text = (idx + 1).toString(),
                                        color = if (active) Background else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = name,
                                color = if (active) PrimaryTeal else if (completed) TextPrimary else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // TOP HALF: STUDENT APP SIMULATION
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Background)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎓 STUDENT VIEWPHONE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("EP·0047 (Joy Adaeze)", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface)
                            .border(1.dp, Border, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (step) {
                            1 -> { // Tap to pay ring
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    NfcPulsingRing()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Student approaches merchant terminal...", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                            2 -> { // Card read success
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(Success.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Nfc, contentDescription = "Read", tint = Success, modifier = Modifier.size(32.dp))
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Contact established securely", color = Success, fontWeight = FontWeight.Bold)
                                    Text("Cryptographic exchange initialized offline", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                            3 -> { // Waiting for merchant amount input
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = PrimaryTeal, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Merchant setting transaction value...", color = TextPrimary, fontWeight = FontWeight.Bold)
                                    Text("Fee computation added locally", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                            4 -> { // Student entering PIN
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Authorize Payment PIN", color = TextPrimary, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        repeat(4) {
                                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(PrimaryTeal))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Entering 4-digit card authorize key...", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                            5 -> { // Confirmed success!
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(Success.copy(alpha = 0.15f))
                                            .border(1.dp, Success, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "OK", tint = Success)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Payment Confirmed Sent", color = Success, fontWeight = FontWeight.Bold)
                                    Text("-₦200.00 • Mama Tee's Kitchen", color = Danger, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Wallet Balance: ₦4,640.00", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Border, thickness = 1.dp)

            // BOTTOM HALF: VENDOR POS TERMINAL SIMULATION
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Background)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🛺 VENDOR POS TERMINAL", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("EP-V-001 (Musa Ibrahim)", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface)
                            .border(1.dp, Border, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (step) {
                            1 -> { // Listening
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = Warning, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("POS Terminal listening for tap...", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                            2 -> { // Read student details
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("STUDENT IDENTITY DECODED", color = PrimaryTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Joy Adaeze", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("EazyPay Card: EP-0047", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            3 -> { // Input terminal amount
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("SPECIFY SECURE AMOUNT", color = TextSecondary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("₦200.00", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                                    Text("+₦10.00 Transaction fee auto-added", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                            4 -> { // Awaiting Student PIN
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Awaiting Student PIN entry...", color = TextPrimary, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        repeat(3) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PrimaryTeal))
                                        }
                                    }
                                }
                            }
                            5 -> { // Confirmed payment receipt!
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(Success.copy(alpha = 0.15f))
                                            .border(1.dp, Success, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "OK", tint = Success)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Payment Received Confirmed", color = Success, fontWeight = FontWeight.Bold)
                                    Text("+₦200.00 • Joy Adaeze", color = Success, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Today's Earnings: ₦2,300.00", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Footer replay controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { viewModel.startDemoFlow() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = "Replay", tint = Background)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Replay Exchange Simulation", color = Background, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
