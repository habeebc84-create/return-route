package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.ReturnRouteBrandHeader

@Composable
fun LoginScreen(
    onGoogleSignIn: (Context) -> Unit,
    onSendEmailLink: (String) -> Unit,
    onStartPhoneVerification: (Context, String) -> Unit,
    onVerifyPhoneCode: (String) -> Unit,
    onCompleteEmailLink: (String) -> Unit,
    isLoading: Boolean,
    isPhoneCodeSent: Boolean,
    hasIncomingEmailLink: Boolean,
    suggestedEmail: String?,
    infoMessage: String?,
    errorMessage: String?
) {
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var smsCode by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(suggestedEmail) {
        if (email.isBlank() && !suggestedEmail.isNullOrBlank()) email = suggestedEmail
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ReturnRouteBrandHeader()
            Spacer(Modifier.height(20.dp))
            Text(
                "Sign in to ReturnRoute",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Choose Google, an email sign-in link, or an SMS code.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
            )

            Button(
                onClick = { onGoogleSignIn(context) },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text("Please wait…", modifier = Modifier.padding(start = 12.dp))
                } else {
                    Icon(Icons.Default.AccountCircle, contentDescription = null)
                    Text("Continue with Google", modifier = Modifier.padding(start = 12.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Text("Email sign-in link", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        label = { Text("Email address") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        enabled = !isLoading
                    )
                    Button(
                        onClick = { onSendEmailLink(email.trim()) },
                        enabled = !isLoading && email.contains('@'),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Send sign-in link")
                    }
                    if (hasIncomingEmailLink) {
                        Text(
                            "Email link opened. Enter the same email address, then continue.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Button(
                            onClick = { onCompleteEmailLink(email.trim()) },
                            enabled = !isLoading && email.contains('@'),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                        ) {
                            Text("Complete email sign-in")
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Text("SMS verification code", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        label = { Text("Phone number, including country code") },
                        placeholder = { Text("+91 98765 43210") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        enabled = !isLoading && !isPhoneCodeSent
                    )
                    AnimatedVisibility(
                        visible = isPhoneCodeSent,
                        enter = fadeIn(tween(160)) + expandVertically(tween(160)),
                        exit = fadeOut(tween(120)) + shrinkVertically(tween(120))
                    ) {
                        OutlinedTextField(
                            value = smsCode,
                            onValueChange = { smsCode = it.filter(Char::isDigit).take(6) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            label = { Text("6-digit SMS code") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = !isLoading
                        )
                        Button(
                            onClick = { onVerifyPhoneCode(smsCode) },
                            enabled = !isLoading && smsCode.length >= 6,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Verify code and sign in")
                        }
                    }
                    if (!isPhoneCodeSent) {
                        Button(
                            onClick = { onStartPhoneVerification(context, phoneNumber.trim()) },
                            enabled = !isLoading && phoneNumber.trim().length >= 8,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Send SMS code")
                        }
                    }
                }
            }

            if (!infoMessage.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    infoMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "New accounts start as Passengers. Rider and Administrator access must be assigned by a trusted server. SMS rates may apply.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
