package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.MatchType
import com.example.data.model.UserRole
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReturnRouteViewModel
import com.example.ui.viewmodel.ScreenDestination
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = applicationContext as Application
            val viewModel: ReturnRouteViewModel = viewModel(
                factory = ReturnRouteViewModel.provideFactory(app)
            )
            val uiState by viewModel.uiState.collectAsState()

            ReturnRouteTheme(darkTheme = uiState.isDarkMode) {
                ReturnRouteApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnRouteApp(
    viewModel: ReturnRouteViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotifsCount = notifications.count { !it.isRead }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showPersonaSwitcher by remember { mutableStateOf(false) }

    // Handle back button: if not on Landing screen, navigate back to Landing
    BackHandler(enabled = uiState.currentScreen != ScreenDestination.LANDING) {
        viewModel.navigateTo(ScreenDestination.LANDING)
    }

    // Launch snackbar when message is posted
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(300.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    ReturnRouteBrandHeader()
                    Spacer(modifier = Modifier.height(18.dp))

                    // Persona Card in Drawer
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPersonaSwitcher = true }
                            .testTag("drawer_persona_card"),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser.avatarInitials,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = currentUser.activeRole.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Switch", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Drawer Navigation Items
                    DrawerNavItem(
                        icon = Icons.Default.Home,
                        title = "Home & Overview",
                        selected = uiState.currentScreen == ScreenDestination.LANDING,
                        onClick = {
                            viewModel.navigateTo(ScreenDestination.LANDING)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerNavItem(
                        icon = Icons.Default.Map,
                        title = "🇮🇳 India Route Map",
                        selected = uiState.currentScreen == ScreenDestination.INDIA_MAP,
                        onClick = {
                            viewModel.navigateTo(ScreenDestination.INDIA_MAP)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerNavItem(
                        icon = Icons.Default.DirectionsCar,
                        title = "Passenger Rides",
                        selected = uiState.currentScreen == ScreenDestination.RIDES,
                        onClick = {
                            viewModel.navigateTo(ScreenDestination.RIDES)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerNavItem(
                        icon = Icons.Default.LocalShipping,
                        title = "Parcel Deliveries",
                        selected = uiState.currentScreen == ScreenDestination.PARCELS,
                        onClick = {
                            viewModel.navigateTo(ScreenDestination.PARCELS)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerNavItem(
                        icon = Icons.Default.AltRoute,
                        title = "Driver Journeys",
                        selected = uiState.currentScreen == ScreenDestination.OFFERS,
                        onClick = {
                            viewModel.navigateTo(ScreenDestination.OFFERS)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerNavItem(
                        icon = Icons.Default.Tune,
                        title = "Route Matching Engine",
                        selected = uiState.currentScreen == ScreenDestination.MATCHING_ENGINE,
                        badge = "Core",
                        onClick = {
                            viewModel.navigateTo(ScreenDestination.MATCHING_ENGINE)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerNavItem(
                        icon = Icons.Default.ConfirmationNumber,
                        title = "My Bookings & Trips",
                        selected = uiState.currentScreen == ScreenDestination.BOOKINGS,
                        onClick = {
                            viewModel.navigateTo(ScreenDestination.BOOKINGS)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerNavItem(
                        icon = Icons.Default.TaskAlt,
                        title = "Project Tasks",
                        badge = "Room DB",
                        selected = uiState.currentScreen == ScreenDestination.TASKS,
                        onClick = {
                            viewModel.navigateTo(ScreenDestination.TASKS)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerNavItem(
                        icon = Icons.Default.TimeToLeave,
                        title = "Vehicles & Fleet",
                        selected = uiState.currentScreen == ScreenDestination.VEHICLES,
                        onClick = {
                            viewModel.navigateTo(ScreenDestination.VEHICLES)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerNavItem(
                        icon = Icons.Default.Analytics,
                        title = "Corridor Demand Insights",
                        selected = uiState.currentScreen == ScreenDestination.DEMAND_INSIGHTS,
                        onClick = {
                            viewModel.navigateTo(ScreenDestination.DEMAND_INSIGHTS)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerNavItem(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "Admin Dashboard",
                        selected = uiState.currentScreen == ScreenDestination.ADMIN,
                        onClick = {
                            viewModel.navigateTo(ScreenDestination.ADMIN)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerNavItem(
                        icon = Icons.Default.Shield,
                        title = "Safety & Legal",
                        selected = uiState.currentScreen == ScreenDestination.SAFETY_LEGAL,
                        onClick = {
                            viewModel.navigateTo(ScreenDestination.SAFETY_LEGAL)
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("app_root"),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        ReturnRouteBrandHeader(showTagline = false)
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("menu_drawer_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Navigation Menu", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    actions = {
                        // Live Network Fetch from Internet Button
                        IconButton(
                            onClick = { viewModel.fetchLiveNetworkCorridors() },
                            modifier = Modifier.testTag("topbar_fetch_internet_button")
                        ) {
                            if (uiState.isNetworkSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = "Load Sample Corridors",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Theme Mode Toggle (Light Booking App vs Midnight Dark)
                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Persona role pill
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showPersonaSwitcher = true }
                                .testTag("persona_switcher_badge"),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(StatusSuccess)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentUser.name.substringBefore(" "),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Map Shortcut Icon
                        IconButton(
                            onClick = { viewModel.navigateTo(ScreenDestination.INDIA_MAP) },
                            modifier = Modifier.testTag("top_india_map_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "India Map",
                                tint = if (uiState.currentScreen == ScreenDestination.INDIA_MAP) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Theme Toggle (Light / Dark)
                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Notification icon with badge
                        IconButton(
                            onClick = { viewModel.navigateTo(ScreenDestination.NOTIFICATIONS) },
                            modifier = Modifier.testTag("notifications_icon_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifsCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ) {
                                            Text("$unreadNotifsCount", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = uiState.currentScreen == ScreenDestination.LANDING,
                        onClick = { viewModel.navigateTo(ScreenDestination.LANDING) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = uiState.currentScreen == ScreenDestination.RIDES,
                        onClick = { viewModel.navigateTo(ScreenDestination.RIDES) },
                        icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Rides") },
                        label = { Text("Rides") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_rides")
                    )
                    NavigationBarItem(
                        selected = uiState.currentScreen == ScreenDestination.PARCELS,
                        onClick = { viewModel.navigateTo(ScreenDestination.PARCELS) },
                        icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Parcels") },
                        label = { Text("Parcels") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandTeal,
                            selectedTextColor = BrandTeal,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.testTag("nav_parcels")
                    )
                    NavigationBarItem(
                        selected = uiState.currentScreen == ScreenDestination.OFFERS,
                        onClick = { viewModel.navigateTo(ScreenDestination.OFFERS) },
                        icon = { Icon(Icons.Default.AltRoute, contentDescription = "Journeys") },
                        label = { Text("Journeys") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_offers")
                    )
                    NavigationBarItem(
                        selected = uiState.currentScreen == ScreenDestination.BOOKINGS,
                        onClick = { viewModel.navigateTo(ScreenDestination.BOOKINGS) },
                        icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = "Bookings") },
                        label = { Text("Bookings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_bookings")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (uiState.currentScreen) {
                    ScreenDestination.LANDING -> LandingScreen(viewModel = viewModel)
                    ScreenDestination.INDIA_MAP -> IndiaMapScreen(viewModel = viewModel)
                    ScreenDestination.RIDES -> RidesScreen(viewModel = viewModel)
                    ScreenDestination.PARCELS -> ParcelsScreen(viewModel = viewModel)
                    ScreenDestination.OFFERS -> OffersScreen(viewModel = viewModel)
                    ScreenDestination.MATCHING_ENGINE -> MatchingEngineScreen(viewModel = viewModel)
                    ScreenDestination.BOOKINGS -> BookingsScreen(viewModel = viewModel)
                    ScreenDestination.TASKS -> TasksScreen(viewModel = viewModel)
                    ScreenDestination.VEHICLES -> VehiclesScreen(viewModel = viewModel)
                    ScreenDestination.DEMAND_INSIGHTS -> DemandInsightsScreen(viewModel = viewModel)
                    ScreenDestination.NOTIFICATIONS -> NotificationsScreen(viewModel = viewModel)
                    ScreenDestination.ADMIN -> AdminScreen(viewModel = viewModel)
                    ScreenDestination.SAFETY_LEGAL -> SafetyLegalScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Persona Switcher Dialog
    if (showPersonaSwitcher) {
        PersonaSwitcherDialog(
            currentUser = currentUser,
            allUsers = viewModel.repository.sampleUsers,
            onSelectUser = { userId ->
                viewModel.switchUser(userId)
                showPersonaSwitcher = false
            },
            onSelectRole = { role ->
                viewModel.switchRole(role)
                showPersonaSwitcher = false
            },
            onDismiss = { showPersonaSwitcher = false }
        )
    }

    // Booking Confirmation Request Modal
    if (uiState.showBookingDialog && uiState.selectedMatchCandidate != null) {
        BookingConfirmationDialog(
            candidate = uiState.selectedMatchCandidate!!,
            onDismiss = { viewModel.closeBookingDialog() },
            onConfirm = { seats, weight ->
                viewModel.requestBookingFromMatch(uiState.selectedMatchCandidate!!, seats, weight)
            }
        )
    }
}

@Composable
fun DrawerNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    selected: Boolean,
    badge: String? = null,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (badge != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        selected = selected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            unselectedContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
fun PersonaSwitcherDialog(
    currentUser: com.example.data.model.UserProfile,
    allUsers: List<com.example.data.model.UserProfile>,
    onSelectUser: (String) -> Unit,
    onSelectRole: (UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Switch User Persona & Role", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Experience ReturnRoute across all verified platform roles:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                allUsers.forEach { user ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectUser(user.id) },
                        color = if (user.id == currentUser.id) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = SolidColor(if (user.id == currentUser.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user.avatarInitials, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                Text("${user.activeRole.label} • ★ ${user.rating} (${user.completedTrips} trips)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (user.id == currentUser.id) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
fun BookingConfirmationDialog(
    candidate: com.example.data.model.MatchCandidate,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double) -> Unit
) {
    val offer = candidate.journeyOffer
    var seats by remember { mutableIntStateOf(1) }
    var parcelWeight by remember { mutableStateOf("4.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (candidate.matchType == MatchType.PASSENGER_RIDE) "Confirm Ride Booking" else "Confirm Parcel Delivery",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Driver: ${offer.driverName} (${offer.vehicleDescription})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${offer.origin.city} → ${offer.destination.city} • ${offer.departureTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (candidate.matchType == MatchType.PASSENGER_RIDE) {
                    Text("Select Seats to Reserve (Max: ${offer.remainingSeats}):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..offer.remainingSeats.coerceAtMost(4)).forEach { num ->
                            FilterChip(
                                selected = seats == num,
                                onClick = { seats = num },
                                label = { Text("$num Seat(s)") }
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = parcelWeight,
                        onValueChange = { parcelWeight = it },
                        label = { Text("Confirmed Cargo Weight (kg)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estimated Total Fare", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "₹${candidate.estimatedCostUsd.toInt()}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(seats, parcelWeight.toDoubleOrNull() ?: 3.0)
                },
                modifier = Modifier.testTag("confirm_booking_submit_button")
            ) {
                Text("Confirm & Reserve")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
