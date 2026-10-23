package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.ParcelCategory
import com.example.ui.components.LocationSearchDialog
import com.example.ui.components.MatchScoreCard
import com.example.ui.theme.BrandTeal
import com.example.ui.viewmodel.ReturnRouteViewModel

@Composable
fun ParcelsScreen(
    viewModel: ReturnRouteViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val parcelRequests by viewModel.parcelRequests.collectAsState()
    val matches = uiState.activeParcelMatches

    var selectedCategoryFilter by remember { mutableStateOf<ParcelCategory?>(null) }

    val filteredMatches = remember(matches, selectedCategoryFilter) {
        matches
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openCreateParcelDialog() },
                containerColor = BrandTeal,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Send Parcel") },
                modifier = Modifier.testTag("send_parcel_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("parcels_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Interstate Parcel Delivery",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Utilize empty trunk capacity on returning vehicles for faster, eco-friendly transport.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Categories Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("All Cargo") }
                        )
                    }
                    items(ParcelCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat },
                            label = { Text(cat.label.substringBefore(" &")) }
                        )
                    }
                }
            }

            // Parcel Matches List
            if (filteredMatches.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No parcel matches currently available", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Load bundled sample corridors to see demo courier routes.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.fetchLiveNetworkCorridors() }) {
                                Text("Load Sample Couriers")
                            }
                        }
                    }
                }
            } else {
                items(filteredMatches, key = { it.id }) { candidate ->
                    MatchScoreCard(
                        candidate = candidate,
                        onBookClick = { viewModel.openBookingDialog(candidate) }
                    )
                }
            }
        }
    }

    // Create Parcel Dialog
    if (uiState.showCreateParcelDialog) {
        CreateParcelDialog(
            onDismiss = { viewModel.closeCreateParcelDialog() },
            onSubmit = { pickup, dest, cat, wt, dl, budget, handling ->
                viewModel.postParcelRequest(pickup, dest, cat, wt, dl, budget, handling)
            }
        )
    }
}

@Composable
fun CreateParcelDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, ParcelCategory, Double, String, Double, String) -> Unit
) {
    var pickup by remember { mutableStateOf("Mumbai (BKC)") }
    var dest by remember { mutableStateOf("Pune (Hinjewadi)") }
    var category by remember { mutableStateOf(ParcelCategory.SMALL_ELECTRONICS) }
    var weight by remember { mutableStateOf("4.0") }
    var deadline by remember { mutableStateOf("By 08:30 PM") }
    var budget by remember { mutableStateOf("400") }
    var handling by remember { mutableStateOf("Fragile electronic device, keep dry.") }

    var showPickupPicker by remember { mutableStateOf(false) }
    var showDestPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Send Parcel / Cargo", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = pickup,
                    onValueChange = { pickup = it },
                    label = { Text("Pickup Indian City / Hub") },
                    trailingIcon = {
                        IconButton(onClick = { showPickupPicker = true }) {
                            Icon(Icons.Default.Place, contentDescription = "Pick City", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dest,
                    onValueChange = { dest = it },
                    label = { Text("Dropoff Indian City / Hub") },
                    trailingIcon = {
                        IconButton(onClick = { showDestPicker = true }) {
                            Icon(Icons.Default.Place, contentDescription = "Pick City", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Cargo Weight (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Delivery Budget (₹ INR)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = handling,
                    onValueChange = { handling = it },
                    label = { Text("Handling Instructions") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weight.toDoubleOrNull() ?: 4.0
                    val b = budget.toDoubleOrNull() ?: 400.0
                    onSubmit(pickup, dest, category, w, deadline, b, handling)
                },
                modifier = Modifier.testTag("submit_parcel_request_button")
            ) {
                Text("Publish Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showPickupPicker) {
        LocationSearchDialog(
            title = "Select Pickup Location",
            onDismiss = { showPickupPicker = false },
            onSelectLocation = { pickup = "${it.city} (${it.name})" }
        )
    }

    if (showDestPicker) {
        LocationSearchDialog(
            title = "Select Dropoff Location",
            onDismiss = { showDestPicker = false },
            onSelectLocation = { dest = "${it.city} (${it.name})" }
        )
    }
}

