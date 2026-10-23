package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JourneyOffer
import com.example.ui.components.LocationSearchDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.StatusGreenPill
import com.example.ui.theme.StatusGreenText
import com.example.ui.viewmodel.ReturnRouteViewModel

@Composable
fun OffersScreen(
    viewModel: ReturnRouteViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val offers by viewModel.journeyOffers.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openCreateOfferDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Publish Journey") },
                modifier = Modifier.testTag("publish_journey_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("offers_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Driver Journeys & Corridors",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Monetize planned return drives by setting corridor buffers and offering pooling.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(offers, key = { it.id }) { offer ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${offer.origin.city} → ${offer.destination.city}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (offer.isReturnTrip) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = StatusGreenPill
                                ) {
                                    Text(
                                        text = "RETURN TRIP -${offer.returnTripSavingsPercent}%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = StatusGreenText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Driver: ${offer.driverName} • ${offer.vehicleDescription}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Departure: ${offer.departureTime}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Seats", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${offer.remainingSeats}/${offer.totalAvailableSeats}", fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Cargo Space", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${offer.remainingParcelCapacityKg} kg", fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Seat Price", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₹${offer.basePricePerSeatUsd.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Max Detour", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${offer.maxDetourKm} km", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showCreateOfferDialog) {
        CreateOfferDialog(
            onDismiss = { viewModel.closeCreateOfferDialog() },
            onSubmit = { orig, dest, dept, seats, kg, detour, pSeat, pKg, isReturn, savings ->
                viewModel.postJourneyOffer(orig, dest, dept, seats, kg, detour, pSeat, pKg, isReturn, savings)
            }
        )
    }
}

@Composable
fun CreateOfferDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, Int, Double, Double, Double, Double, Boolean, Int) -> Unit
) {
    var origin by remember { mutableStateOf("Bengaluru (Electronic City)") }
    var destination by remember { mutableStateOf("Chennai (Koyambedu)") }
    var departureTime by remember { mutableStateOf("Tomorrow • 05:00 PM") }
    var seats by remember { mutableIntStateOf(3) }
    var parcelKg by remember { mutableStateOf("60.0") }
    var detourKm by remember { mutableStateOf("15.0") }
    var pricePerSeat by remember { mutableStateOf("550") }
    var isReturnTrip by remember { mutableStateOf(true) }
    var savingsPercent by remember { mutableIntStateOf(30) }

    var showOriginPicker by remember { mutableStateOf(false) }
    var showDestPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Driver Journey", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = origin,
                    onValueChange = { origin = it },
                    label = { Text("Origin Indian City / Hub") },
                    trailingIcon = {
                        IconButton(onClick = { showOriginPicker = true }) {
                            Icon(Icons.Default.Place, contentDescription = "Pick City", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination Indian City / Hub") },
                    trailingIcon = {
                        IconButton(onClick = { showDestPicker = true }) {
                            Icon(Icons.Default.Place, contentDescription = "Pick City", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = departureTime,
                    onValueChange = { departureTime = it },
                    label = { Text("Departure Time") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pricePerSeat,
                    onValueChange = { pricePerSeat = it },
                    label = { Text("Price per Seat (₹ INR)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isReturnTrip, onCheckedChange = { isReturnTrip = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Designate as Return Journey (-30% discount)", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = pricePerSeat.toDoubleOrNull() ?: 550.0
                    val pkg = parcelKg.toDoubleOrNull() ?: 50.0
                    val d = detourKm.toDoubleOrNull() ?: 15.0
                    onSubmit(origin, destination, departureTime, seats, pkg, d, p, 35.0, isReturnTrip, savingsPercent)
                },
                modifier = Modifier.testTag("submit_offer_button")
            ) {
                Text("Publish Offer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showOriginPicker) {
        LocationSearchDialog(
            title = "Select Origin City",
            onDismiss = { showOriginPicker = false },
            onSelectLocation = { origin = "${it.city} (${it.name})" }
        )
    }

    if (showDestPicker) {
        LocationSearchDialog(
            title = "Select Destination City",
            onDismiss = { showDestPicker = false },
            onSelectLocation = { destination = "${it.city} (${it.name})" }
        )
    }
}

