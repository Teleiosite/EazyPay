package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.EazyPayViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EazyPayAppNavigator()
            }
        }
    }
}

@Composable
fun EazyPayAppNavigator() {
    val navController = rememberNavController()
    val viewModel: EazyPayViewModel = viewModel()
    
    // Store simple navigation params in memory
    var currentPhone by remember { mutableStateOf("") }
    var chosenRole by remember { mutableStateOf("student") }

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("splash") {
            SplashScreen(
                onNavigate = {
                    if (viewModel.isRegistered.value) {
                        val role = viewModel.currentRole.value
                        if (role == "student") {
                            navController.navigate("student_main") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            navController.navigate("vendor_main") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    } else {
                        navController.navigate("onboarding") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable("onboarding") {
            OnboardingScreen(
                onFinished = {
                    navController.navigate("register") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        
        composable("register") {
            RegisterScreen(
                onContinue = { phone, role ->
                    currentPhone = phone
                    chosenRole = role
                    viewModel.setRole(role)
                    navController.navigate("otp")
                },
                onWatchDemo = {
                    navController.navigate("demo_split_screen")
                },
                onQuickLogin = { role ->
                    currentPhone = if (role == "student") "8012345678" else "8011112222"
                    chosenRole = role
                    viewModel.setRole(role)
                    viewModel.setPin("1234")
                    viewModel.setRegistered(true, currentPhone, role)
                    if (role == "student") {
                        navController.navigate("student_main") {
                            popUpTo("register") { inclusive = true }
                        }
                    } else {
                        navController.navigate("vendor_main") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable("otp") {
            OtpScreen(
                phone = currentPhone,
                onVerified = {
                    navController.navigate("set_pin")
                }
            )
        }
        
        composable("set_pin") {
            SetPinScreen(
                onPinSet = { pin ->
                    viewModel.setPin(pin)
                    viewModel.setRegistered(true, currentPhone, chosenRole)
                    if (chosenRole == "student") {
                        navController.navigate("student_main") {
                            popUpTo("register") { inclusive = true }
                        }
                    } else {
                        navController.navigate("vendor_main") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable("student_main") {
            StudentMainScreen(
                viewModel = viewModel,
                onSignOut = {
                    viewModel.setRegistered(false)
                    navController.navigate("register") {
                        popUpTo("student_main") { inclusive = true }
                    }
                }
            )
        }
        
        composable("vendor_main") {
            VendorMainScreen(
                viewModel = viewModel,
                onSignOut = {
                    viewModel.setRegistered(false)
                    navController.navigate("register") {
                        popUpTo("vendor_main") { inclusive = true }
                    }
                }
            )
        }

        composable("demo_split_screen") {
            DemoSplitScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
