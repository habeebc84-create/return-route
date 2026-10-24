package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.ReturnRouteViewModel

@Composable
fun SafetyLegalScreen(
    viewModel: ReturnRouteViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("safety_legal_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Safety, Trust & Legal Standards",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ensuring secure co-travel and insured interstate courier operations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            PolicyCard(
                title = "1. Mandatory PIN Verification",
                body = "Every booked passenger and parcel sender receives an encrypted 6-digit PIN. Drivers must enter or verify this PIN before the journey status transitions to Active."
            )
        }
        item {
            PolicyCard(
                title = "2. Prohibited Cargo Policy",
                body = "Hazardous chemicals, flammable substances, perishable unpacked food, and illicit items are strictly prohibited. Parcels are insured up to $1,000 against transit damage."
            )
        }
        item {
            PolicyCard(
                title = "3. Government ID Verification",
                body = "All drivers undergo motor vehicle record checks and identity document verification before being permitted to post return journeys."
            )
        }
    }
}

@Composable
private fun PolicyCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
