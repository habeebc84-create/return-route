package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.components.LocationSearchDialog
import com.example.ui.components.MatchScoreCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.ReturnRouteViewModel
import com.example.ui.viewmodel.SortOption

@Composable
fun RidesScreen(
    viewModel: ReturnRouteViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val rideRequests by viewModel.rideRequests.collectAsState()
    val matches = uiState.activeRideMatches

    // Filters and sorting
    val filteredMatches = remember(matches, uiState.searchQuery, uiState.filterReturnTripsOnly, uiState.sortOption) {
        var list = matches
        if (uiState.searchQuery.isNotBlank()) {
            list = list.filter {
                it.journeyOffer.origin.city.contains(uiState.searchQuery, ignoreCase = true) ||
                it.journeyOffer.destination.city.contains(uiState.searchQuery, ignoreCase = true)
            }
        }
        if (uiState.filterReturnTripsOnly) {
            list = list.filter { it.journeyOffer.isReturnTrip }
        }
        when (uiState.sortOption) {
            SortOption.BEST_MATCH -> list.sortedByDescending { it.compatibilityScore }
            SortOption.LOWEST_PRICE -> list.sortedBy { it.estimatedCostUsd }
            SortOption.EARLIEST_DEPARTURE -> list
            SortOption.MINIMAL_DETOUR -> list.sortedBy { it.detourDistanceKm }
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openCreateRideDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Request Ride") },
                modifier = Modifier.testTag("post_ride_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("rides_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Passenger Return Rides",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Save up to 35% by booking seats on drivers' scheduled return journeys.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    label = { Text("Filter by destination or city") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rides_search_field"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = uiState.filterReturnTripsOnly,
                        onClick = { viewModel.toggleReturnTripsOnly(!uiState.filterReturnTripsOnly) },
                        label = { Text("Return Trips Only (-25%)") },
                        leadingIcon = {
                            if (uiState.filterReturnTripsOnly) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        },
                        modifier = Modifier.testTag("filter_return_trips_chip")
                    )

                    FilterChip(
                        selected = uiState.sortOption == SortOption.LOWEST_PRICE,
                        onClick = {
                            viewModel.setSortOption(
                                if (uiState.sortOption == SortOption.LOWEST_PRICE) SortOption.BEST_MATCH else SortOption.LOWEST_PRICE
                            )
                        },
                        label = { Text("Lowest Price") },
                        modifier = Modifier.testTag("sort_price_chip")
                    )
                }
            }

            // Match Candidates List
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
                            Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No matching rides found", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Try another city or load the bundled sample corridors to explore available routes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.fetchLiveNetworkCorridors() }) {
                                Text("Load Sample Corridors")
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

    // Create Ride Dialog
    if (uiState.showCreateRideDialog) {
        CreateRideDialog(
            onDismiss = { viewModel.closeCreateRideDialog() },
            onSubmit = { origin, dest, seats, budget, notes, isReturn ->
                viewModel.postRideRequest(origin, dest, seats, budget, notes, isReturn)
            }
        )
    }
}

@Composable
fun CreateRideDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, Int, Double, String, Boolean) -> Unit
) {
    var origin by remember { mutableStateOf("Bengaluru (Koramangala)") }
    var destination by remember { mutableStateOf("Chennai (OMR)") }
    var seats by remember { mutableIntStateOf(1) }
    var budget by remember { mutableStateOf("550") }
    var notes by remember { mutableStateOf("Flexible pickup near Hosur Road toll plaza.") }
    var isReturnTrip by remember { mutableStateOf(true) }

    var showOriginPicker by remember { mutableStateOf(false) }
    var showDestPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Passenger Ride", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = origin,
                    onValueChange = { origin = it },
                    label = { Text("Pickup Indian City / Hub") },
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Seats Required:")
                    Row {
                        (1..3).forEach { s ->
                            FilterChip(
                                selected = seats == s,
                                onClick = { seats = s },
                                label = { Text("$s") }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Max Budget (₹ INR)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes for Driver") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isReturnTrip,
                        onCheckedChange = { isReturnTrip = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Seeking Return-Trip Discount (-30%)", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val b = budget.toDoubleOrNull() ?: 550.0
                    onSubmit(origin, destination, seats, b, notes, isReturnTrip)
                },
                modifier = Modifier.testTag("submit_ride_request_button")
            ) {
                Text("Post Request")
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

