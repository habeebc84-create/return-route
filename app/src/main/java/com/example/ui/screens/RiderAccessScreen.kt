package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.ReturnRouteViewModel

@Composable
fun RiderAccessScreen(viewModel: ReturnRouteViewModel, modifier: Modifier = Modifier) {
    val status by viewModel.riderAccessStatus.collectAsState()
    val busy by viewModel.riderAccessBusy.collectAsState()
    var note by rememberSaveable { mutableStateOf("") }
    val statusText = when (status) {
        "pending" -> "Request received — an Admin must review and verify your Rider eligibility."
        "approved" -> "Rider access approved. Sign out and sign back in to open the Rider app."
        "denied" -> "This request was not approved. Contact platform support if you need help."
        else -> null
    }

    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Become a ReturnRoute Rider", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Your Passenger account and trips stay yours. Request Rider access to publish journeys and respond to ride or parcel requests in the separate Rider app.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Admin review required", fontWeight = FontWeight.SemiBold)
                Text(
                    "An Admin grants Rider access only after checking eligibility outside the app. Never send passwords, payment details, or identity-document numbers in this message.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (statusText != null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Text(statusText, Modifier.fillMaxWidth().padding(16.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        if (status != "pending" && status != "approved") {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(500) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Optional note for the Admin") },
                minLines = 3,
                maxLines = 5,
                enabled = !busy
            )
            Button(
                onClick = { viewModel.requestRiderAccess(note) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy
            ) {
                if (busy) CircularProgressIndicator()
                else {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null)
                    Spacer(Modifier.height(0.dp))
                    Text("  Request Rider access")
                }
            }
        }
    }
}
