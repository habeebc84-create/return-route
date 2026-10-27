package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.JourneyStatus
import com.example.ui.viewmodel.ReturnRouteViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun RiderRequestsScreen(viewModel: ReturnRouteViewModel, modifier: Modifier = Modifier) {
    val rides by viewModel.rideRequests.collectAsState()
    val parcels by viewModel.parcelRequests.collectAsState()
    val rider = viewModel.currentUser.value.activeRole != com.example.data.model.UserRole.PASSENGER

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Open passenger requests", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Review active requests and publish a matching journey from the Rider app. Current rows are demo samples; Passenger-created requests do not sync across devices yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = { viewModel.navigateTo(ScreenDestination.OFFERS) }) {
                Text("Open Rider journeys")
            }
        }
        item { Text("Ride requests (${rides.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
        if (rides.isEmpty()) item { EmptyRequestCard("No active passenger ride requests.") }
        items(rides.filter { it.status != JourneyStatus.CANCELLED }, key = { "ride-${it.id}" }) { request ->
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("${request.origin.city} → ${request.destination.city}", fontWeight = FontWeight.Bold)
                    Text("${request.preferredDate} • ${request.earliestDepartureTime}–${request.latestDepartureTime}")
                    Text("${request.requiredSeats} seat(s) • Budget ${request.maxBudgetUsd.toInt()}", style = MaterialTheme.typography.bodySmall)
                    if (request.notes.isNotBlank()) Text(request.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { Text("Parcel requests (${parcels.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
        if (parcels.isEmpty()) item { EmptyRequestCard("No active parcel requests.") }
        items(parcels.filter { it.status != JourneyStatus.CANCELLED }, key = { "parcel-${it.id}" }) { request ->
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("${request.pickup.city} → ${request.destination.city}", fontWeight = FontWeight.Bold)
                    Text("${request.category.label} • ${request.weightKg} kg • ${request.preferredDate}")
                    Text("Delivery by ${request.deliveryDeadline} • Budget ${request.maxBudgetUsd.toInt()}", style = MaterialTheme.typography.bodySmall)
                    if (request.handlingInstructions.isNotBlank()) Text(request.handlingInstructions, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (!rider) item { Text("Admin view: Rider-only actions are disabled here.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun EmptyRequestCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(message, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
