package com.arkamodh.authsdk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkamodh.authsdk.sdk.AuthSdk
import com.arkamodh.authsdk.sdk.model.AuthProvider
import com.arkamodh.authsdk.sdk.model.AuthUser
import kotlinx.coroutines.launch

@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    
    // Auth State Observation using SDK flow
    val authUser by remember { 
        derivedStateOf { 
            try { 
                AuthSdk.getInstance().observeAuthState() 
            } catch (e: Exception) {
                null
            }
        } 
    }?.value?.collectAsState(initial = null) ?: remember { mutableStateOf<AuthUser?>(null) }

    // UI Fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var idToken by remember { mutableStateOf<String?>(null) }

    // Color Palette
    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F0C1B), Color(0xFF201A30))
    )
    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF7F00FF), Color(0xFFE100FF))
    )
    val cardColor = Color(0xFF2D243F)
    val textFieldColor = Color(0xFF1E172C)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .safeContentPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "AuthSdk",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Firebase KMM Authentication SDK Demo",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Notifications
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33FF3333))
                        .border(1.dp, Color(0xFFFF3333), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFFF6666),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            AnimatedVisibility(
                visible = infoMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x3333FF33))
                        .border(1.dp, Color(0xFF33FF33), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = infoMessage ?: "",
                        color = Color(0xFF66FF66),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Main Content Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = cardColor,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (authUser == null) {
                        // Unauthenticated Screen
                        Text(
                            text = if (isRegisterMode) "Create Account" else "Welcome Back",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = null },
                            label = { Text("Email Address", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFE100FF),
                                unfocusedBorderColor = Color.Gray,
                                focusedContainerColor = textFieldColor,
                                unfocusedContainerColor = textFieldColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = { Text("Password", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFE100FF),
                                unfocusedBorderColor = Color.Gray,
                                focusedContainerColor = textFieldColor,
                                unfocusedContainerColor = textFieldColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        if (isLoading) {
                            CircularProgressIndicator(color = Color(0xFFE100FF))
                        } else {
                            // Action Button
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        infoMessage = null
                                        try {
                                            val sdk = AuthSdk.getInstance()
                                            if (isRegisterMode) {
                                                sdk.signUp(email, password)
                                                infoMessage = "Registered successfully!"
                                            } else {
                                                sdk.signIn(email, password)
                                                infoMessage = "Logged in successfully!"
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = e.message ?: "Authentication failed."
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(buttonGradient)
                            ) {
                                Text(
                                    text = if (isRegisterMode) "SIGN UP" else "SIGN IN",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Reset Password Text
                            if (!isRegisterMode) {
                                TextButton(
                                    onClick = {
                                        if (email.isBlank()) {
                                            errorMessage = "Please enter your email to reset password."
                                            return@TextButton
                                        }
                                        coroutineScope.launch {
                                            isLoading = true
                                            errorMessage = null
                                            infoMessage = null
                                            try {
                                                AuthSdk.getInstance().resetPassword(email)
                                                infoMessage = "Password reset email sent!"
                                            } catch (e: Exception) {
                                                errorMessage = e.message ?: "Failed to send reset email."
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    }
                                ) {
                                    Text("Forgot Password?", color = Color(0xFFE100FF))
                                }
                            }

                            // Mode Toggle
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isRegisterMode) "Already have an account?" else "Don't have an account?",
                                    color = Color.LightGray,
                                    fontSize = 14.sp
                                )
                                TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                                    Text(
                                        text = if (isRegisterMode) "Sign In" else "Sign Up",
                                        color = Color(0xFFE100FF),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Divider(color = Color(0xFF3D344F), modifier = Modifier.padding(vertical = 12.dp))

                            // Federated Login buttons
                            Text("Or connect with", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            isLoading = true
                                            errorMessage = null
                                            try {
                                                println("App.kt: Initiating AuthSdk signInWithGoogle()...")
                                                val result = AuthSdk.getInstance().signInWithGoogle()
                                                println("App.kt: Google Sign-in successful. User UID: ${result.user?.uid}, Email: ${result.user?.email}")
                                                infoMessage = "Signed in with Google!"
                                            } catch (e: Exception) {
                                                println("App.kt: Google Sign-in error: ${e.message}")
                                                e.printStackTrace()
                                                errorMessage = e.message
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                                ) {
                                    Text("Google", color = Color.White)
                                }

                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            isLoading = true
                                            errorMessage = null
                                            try {
                                                AuthSdk.getInstance().signInWithCredential(
                                                    AuthProvider.APPLE, "mock-apple-id-token"
                                                )
                                                infoMessage = "Signed in with Apple (Sandbox)!"
                                            } catch (e: Exception) {
                                                errorMessage = e.message
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).padding(start = 6.dp)
                                ) {
                                    Text("Apple", color = Color.White)
                                }
                            }
                        }
                    } else {
                        // Authenticated Screen
                        val user = authUser!!
                        Text(
                            text = "Authenticated User",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(textFieldColor)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text("UID: ${user.uid}", color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Email: ${user.email ?: "N/A"}", color = Color.LightGray, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Display Name: ${user.displayName ?: "N/A"}", color = Color.LightGray, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Verified: ${user.isEmailVerified}", color = if (user.isEmailVerified) Color(0xFF66FF66) else Color(0xFFFF6666), fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (isLoading) {
                            CircularProgressIndicator(color = Color(0xFFE100FF))
                        } else {
                            // ID Token fetcher
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        try {
                                            idToken = AuthSdk.getInstance().getIdToken(forceRefresh = true)
                                            infoMessage = "Fetched JWT ID Token successfully!"
                                        } catch (e: Exception) {
                                            errorMessage = e.message
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Get JWT ID Token")
                            }

                            // Show Token if fetched
                            AnimatedVisibility(visible = idToken != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF130E1F))
                                        .padding(12.dp)
                                ) {
                                    Text("JWT ID Token:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(idToken ?: "", color = Color(0xFFE100FF), fontSize = 11.sp, maxLines = 3)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Sign Out Button
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        idToken = null
                                        try {
                                            AuthSdk.getInstance().signOut()
                                            infoMessage = "Signed out successfully."
                                        } catch (e: Exception) {
                                            errorMessage = e.message
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5555)),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("SIGN OUT")
                            }
                        }
                    }
                }
            }
        }
    }
}