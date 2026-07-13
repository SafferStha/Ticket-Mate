package com.example.individual_project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.individual_project.repo.UserRepoImpl
import com.example.individual_project.ui.theme.IndividualProjectTheme
import com.example.individual_project.ui.theme.TmBackground
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmLightBlue
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.theme.TmSuccess
import com.example.individual_project.ui.theme.TmTextPrimary
import com.example.individual_project.ui.theme.TmTextSecondary
import com.example.individual_project.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    navController: NavController,
    email: String,
    onVerificationSuccess: () -> Unit
) {
    var otp by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var resendCountdown by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify OTP") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TmNavyBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TmBackground)
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .background(
                        color = TmBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(100.dp)
                    )
                    .padding(28.dp)
            ) {
                Text(text = "✉️", fontSize = 60.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Enter Verification Code",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TmTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "We've sent a 6-digit code to",
                fontSize = 14.sp,
                color = TmTextSecondary,
                textAlign = TextAlign.Center
            )
            Text(
                text = email,
                fontSize = 14.sp,
                color = TmBlue,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = otp,
                onValueChange = {
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        otp = it
                        otpError = ""
                    }
                },
                label = { Text("6-Digit Code") },
                isError = otpError.isNotEmpty(),
                supportingText = if (otpError.isNotEmpty()) {
                    { Text(otpError, color = TmError) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TmBlue,
                    focusedLabelColor = TmBlue,
                    cursorColor = TmBlue
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        otp.isBlank() -> otpError = "OTP is required"
                        otp.length != 6 -> otpError = "OTP must be 6 digits"
                        else -> {
                            isLoading = true
                            userViewModel.verifyOtp(email, otp) { success, message ->
                                isLoading = false
                                if (success) {
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    onVerificationSuccess()
                                } else {
                                    otpError = message
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TmBlue),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Verify",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = {
                    isResending = true
                    userViewModel.sendOtpEmail(email) { success, message, _ ->
                        isResending = false
                        if (success) {
                            resendCountdown = 30
                            Toast.makeText(context, "OTP resent successfully", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isResending && resendCountdown == 0
            ) {
                Text(
                    text = if (resendCountdown > 0) "Resend in ${resendCountdown}s" else "Didn't receive code? Resend",
                    color = if (resendCountdown > 0) TmTextSecondary else TmBlue
                )
            }
        }
    }
}

@Preview(name = "OTP Verification", showBackground = true, showSystemUi = true)
@Composable
fun OtpVerificationPreview() {
    IndividualProjectTheme {
        OtpVerificationScreen(
            navController = rememberNavController(),
            email = "user@example.com",
            onVerificationSuccess = {}
        )
    }
}
