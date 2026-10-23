package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeoLocation
import com.example.data.model.IndiaLocations
import com.example.ui.components.InteractiveRouteMap
import com.example.ui.components.LocationSearchDialog
import com.example.ui.components.NetworkSyncBanner
import com.example.ui.components.ReturnRouteIcon
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReturnRouteViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun LandingScreen(
    viewModel: ReturnRouteViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val journeyOffers by viewModel.journeyOffers.collectAsState()
    val sampleOffer = journeyOffers.firstOrNull()

    var searchOrigin by remember { mutableStateOf("Bengaluru") }
    var searchDestination by remember { mutableStateOf("Chennai") }
    var selectedServiceType by remember { mutableIntStateOf(0) } // 0: Rides, 1: Parcels
    var selectedDateOption by remember { mutableStateOf("Tomorrow") }

    var showOriginSearchDialog by remember { mutableStateOf(false) }
    var showDestSearchDialog by remember { mutableStateOf(false) }

    val popularCorridors = IndiaLocations.POPULAR_CORRIDORS

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("landing_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Live Corridor Network Sync Banner
        item {
            NetworkSyncBanner(
                isSyncing = uiState.isNetworkSyncing,
                lastSyncTime = uiState.lastSyncTime,
                onSyncClick = { viewModel.fetchLiveNetworkCorridors() }
            )
        }

        // 2. Modern Booking App Hero Search Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(MaterialTheme.colorScheme.outline)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Service Type Tabs (Book Rides vs Send Parcels)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedServiceType = 0 },
                            color = if (selectedServiceType == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (selectedServiceType == 0) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = if (selectedServiceType == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Book Rides",
                                    fontWeight = if (selectedServiceType == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedServiceType == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedServiceType = 1 },
                            color = if (selectedServiceType == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (selectedServiceType == 1) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = if (selectedServiceType == 1) BrandTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Send Parcels",
                                    fontWeight = if (selectedServiceType == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedServiceType == 1) BrandTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Origin Input with Location Picker Dialog trigger
                    OutlinedTextField(
                        value = searchOrigin,
                        onValueChange = { searchOrigin = it },
                        label = { Text("Leaving from (Indian city or hub)") },
                        leadingIcon = {
                            IconButton(onClick = { showOriginSearchDialog = true }) {
                                Icon(Icons.Default.TripOrigin, contentDescription = "Pick Origin", tint = StatusSuccess)
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = { showOriginSearchDialog = true }) {
                                Icon(Icons.Default.Place, contentDescription = "Select City", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_origin_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Destination Input with Location Picker Dialog trigger
                    OutlinedTextField(
                        value = searchDestination,
                        onValueChange = { searchDestination = it },
                        label = { Text("Going to (Destination city or corridor)") },
                        leadingIcon = {
                            IconButton(onClick = { showDestSearchDialog = true }) {
                                Icon(Icons.Default.PinDrop, contentDescription = "Pick Destination", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = { showDestSearchDialog = true }) {
                                Icon(Icons.Default.Place, contentDescription = "Select City", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_dest_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Corridor Suggestion Chips
                    Text(
                        text = "Popular Indian Return Corridors:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(popularCorridors.take(5)) { corridor ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        searchOrigin = corridor.origin.city
                                        searchDestination = corridor.destination.city
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${corridor.origin.city} ⇄ ${corridor.destination.city}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "-${corridor.returnSavingsPercent}%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusGreenText
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Date Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("Today", "Tomorrow", "This Weekend").forEach { dateOpt ->
                            FilterChip(
                                selected = selectedDateOption == dateOpt,
                                onClick = { selectedDateOption = dateOpt },
                                label = { Text(dateOpt, fontSize = 12.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Search / Find Action
                    Button(
                        onClick = {
                            viewModel.setSearchQuery(searchDestination)
                            if (selectedServiceType == 0) {
                                viewModel.navigateTo(ScreenDestination.RIDES)
                            } else {
                                viewModel.navigateTo(ScreenDestination.PARCELS)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("hero_search_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedServiceType == 0) "Find Matching Return Rides" else "Find Return Cargo Couriers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // 3. India Interactive Map Feature Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(ScreenDestination.INDIA_MAP) }
                    .testTag("landing_india_map_banner"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🇮🇳 Subcontinent Route Map",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StatusGreenPill
                            ) {
                                Text(
                                    text = "Sample",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = StatusGreenText,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Interactive geographic map of Indian highways & 80+ cities",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Map",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 4. Live Active Corridor Map Visualizer
        if (sampleOffer != null) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Return Corridor Map",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = { viewModel.navigateTo(ScreenDestination.INDIA_MAP) }) {
                            Text("Full Map View →", fontSize = 12.sp)
                        }
                    }

                    InteractiveRouteMap(
                        origin = sampleOffer.origin,
                        destination = sampleOffer.destination,
                        waypoints = sampleOffer.waypoints,
                        detourKm = 14.2
                    )
                }
            }
        }

        // 5. Quick Features / Options Grid
        item {
            Text(
                text = "Explore All Services & Tools",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickOptionCard(
                        title = "Passenger Rides",
                        subtitle = "Pool seats on return drives",
                        icon = Icons.Default.DirectionsCar,
                        badge = "30% Off",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.RIDES) }
                    )
                    QuickOptionCard(
                        title = "Parcel Shipping",
                        subtitle = "Fast interstate courier",
                        icon = Icons.Default.LocalShipping,
                        badge = "Secure",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.PARCELS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickOptionCard(
                        title = "🇮🇳 India Route Map",
                        subtitle = "Subcontinent highways & hubs",
                        icon = Icons.Default.Map,
                        badge = "Visualizer",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.INDIA_MAP) }
                    )
                    QuickOptionCard(
                        title = "Matching Engine",
                        subtitle = "Geospatial corridors",
                        icon = Icons.Default.Tune,
                        badge = "Core AI",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.MATCHING_ENGINE) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickOptionCard(
                        title = "Post Journey",
                        subtitle = "Monetize your empty return",
                        icon = Icons.Default.AltRoute,
                        badge = "Drivers",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.OFFERS) }
                    )
                    QuickOptionCard(
                        title = "My Bookings",
                        subtitle = "Trips & boarding codes",
                        icon = Icons.Default.ConfirmationNumber,
                        badge = "${uiState.activeRideMatches.size + 2} Trips",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.BOOKINGS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickOptionCard(
                        title = "Corridor Demand",
                        subtitle = "Surge & revenue predictions",
                        icon = Icons.Default.Analytics,
                        badge = "Insights",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.DEMAND_INSIGHTS) }
                    )
                    QuickOptionCard(
                        title = "Vehicles & Fleet",
                        subtitle = "Capacity & specs",
                        icon = Icons.Default.TimeToLeave,
                        badge = "Fleet",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.VEHICLES) }
                    )
                }
            }
        }
    }

    if (showOriginSearchDialog) {
        LocationSearchDialog(
            title = "Leaving from (Origin)",
            onDismiss = { showOriginSearchDialog = false },
            onSelectLocation = { searchOrigin = it.city }
        )
    }

    if (showDestSearchDialog) {
        LocationSearchDialog(
            title = "Going to (Destination)",
            onDismiss = { showDestSearchDialog = false },
            onSelectLocation = { searchDestination = it.city }
        )
    }
}

@Composable
fun QuickOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(MaterialTheme.colorScheme.outline)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
