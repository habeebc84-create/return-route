package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeoLocation
import com.example.data.model.IndiaLocations
import com.example.ui.components.LocationSearchDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReturnRouteViewModel
import com.example.ui.viewmodel.ScreenDestination
import kotlin.math.*

@Composable
fun IndiaMapScreen(
    viewModel: ReturnRouteViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedCorridorIndex by remember { mutableIntStateOf(0) }
    val popularCorridors = IndiaLocations.POPULAR_CORRIDORS

    val currentPreset = popularCorridors.getOrElse(selectedCorridorIndex) { popularCorridors[0] }
    var selectedOrigin by remember(selectedCorridorIndex) { mutableStateOf(currentPreset.origin) }
    var selectedDestination by remember(selectedCorridorIndex) { mutableStateOf(currentPreset.destination) }

    var showOriginSearch by remember { mutableStateOf(false) }
    var showDestSearch by remember { mutableStateOf(false) }

    // Distance calculation in km
    val distanceKm = remember(selectedOrigin, selectedDestination) {
        calculateHaversineDistanceKm(
            selectedOrigin.latitude, selectedOrigin.longitude,
            selectedDestination.latitude, selectedDestination.longitude
        )
    }

    val estimatedPriceInr = remember(distanceKm) {
        (distanceKm * 1.65).coerceAtLeast(350.0).roundToInt()
    }

    val returnDiscountPercent = 30
    val discountedFare = (estimatedPriceInr * (1.0 - returnDiscountPercent / 100.0)).roundToInt()

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("india_map_screen"),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🇮🇳 India Return Route Network",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Live corridors across 28 states & 80+ cities",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { viewModel.fetchLiveNetworkCorridors() },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Popular Corridor Preset Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(popularCorridors.indices.toList()) { idx ->
                        val preset = popularCorridors[idx]
                        FilterChip(
                            selected = selectedCorridorIndex == idx,
                            onClick = {
                                selectedCorridorIndex = idx
                                selectedOrigin = preset.origin
                                selectedDestination = preset.destination
                            },
                            label = {
                                Text(
                                    text = "${preset.origin.city} ⇄ ${preset.destination.city}",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedCorridorIndex == idx) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Route Search Controls
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Origin Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showOriginSearch = true }
                            .testTag("map_select_origin_btn"),
                        color = MaterialTheme.colorScheme.surface,
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusSuccess))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("From (Origin)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(selectedOrigin.city, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                            }
                        }
                    }

                    // Swap Button
                    IconButton(
                        onClick = {
                            val temp = selectedOrigin
                            selectedOrigin = selectedDestination
                            selectedDestination = temp
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Swap", tint = MaterialTheme.colorScheme.primary)
                    }

                    // Destination Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showDestSearch = true }
                            .testTag("map_select_dest_btn"),
                        color = MaterialTheme.colorScheme.surface,
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PrimaryBlue))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("To (Destination)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(selectedDestination.city, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // India Map Canvas Visualizer
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (uiState.isDarkMode) SurfaceDark else Color(0xFFF1F5F9))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            ) {
                IndiaSubcontinentMapCanvas(
                    origin = selectedOrigin,
                    destination = selectedDestination,
                    allCities = IndiaLocations.ALL_LOCATIONS,
                    isDarkMode = uiState.isDarkMode,
                    onCityClick = { city ->
                        selectedDestination = city
                    }
                )

                // Map Legend Overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusSuccess))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Origin", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PrimaryBlue))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Destination", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusAmber))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Corridor Hub", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                    }
                }

                // Interactive Hint Overlay
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = "Tap any city pin to route",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Bottom Route Fare & Booking Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${selectedOrigin.city} → ${selectedDestination.city}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = StatusGreenPill
                                ) {
                                    Text(
                                        text = "-${returnDiscountPercent}% RETURN FARE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                        color = StatusGreenText,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Highway Distance: ~${distanceKm.roundToInt()} km • ~${(distanceKm / 60.0).roundToInt()} hrs drive",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹$discountedFare",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Regular: ₹$estimatedPriceInr",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.setSearchQuery(selectedDestination.city)
                                viewModel.navigateTo(ScreenDestination.RIDES)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("book_return_ride_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Book Ride", fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = {
                                viewModel.setSearchQuery(selectedDestination.city)
                                viewModel.navigateTo(ScreenDestination.PARCELS)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("send_parcel_corridor_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Parcel", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showOriginSearch) {
        LocationSearchDialog(
            title = "Select Origin City",
            onDismiss = { showOriginSearch = false },
            onSelectLocation = { selectedOrigin = it }
        )
    }

    if (showDestSearch) {
        LocationSearchDialog(
            title = "Select Destination City",
            onDismiss = { showDestSearch = false },
            onSelectLocation = { selectedDestination = it }
        )
    }
}

@Composable
fun IndiaSubcontinentMapCanvas(
    origin: GeoLocation,
    destination: GeoLocation,
    allCities: List<GeoLocation>,
    isDarkMode: Boolean,
    onCityClick: (GeoLocation) -> Unit
) {
    // Pulse animation for vehicle moving along corridor
    val infiniteTransition = rememberInfiniteTransition(label = "vehicle_transit")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val gridColor = if (isDarkMode) BorderDark.copy(alpha = 0.25f) else Color(0xFFE2E8F0)
    val mapLandColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val coastlineColor = if (isDarkMode) ElectricCyan.copy(alpha = 0.6f) else PrimaryBlue.copy(alpha = 0.5f)
    val corridorColor = PrimaryBlue
    val goldenQuadColor = if (isDarkMode) Color(0xFF64748B).copy(alpha = 0.4f) else Color(0xFF94A3B8).copy(alpha = 0.5f)

    // Key representative major cities to pin on canvas
    val pinnedCities = remember(allCities) {
        val keyCityNames = setOf(
            "New Delhi", "Delhi", "Chandigarh", "Amritsar", "Srinagar", "Jaipur", "Lucknow", "Varanasi",
            "Mumbai", "Pune", "Ahmedabad", "Surat", "Nagpur", "Goa",
            "Bengaluru", "Chennai", "Hyderabad", "Kochi", "Coimbatore", "Visakhapatnam", "Madurai",
            "Kolkata", "Patna", "Bhubaneswar", "Guwahati", "Ranchi", "Bhopal", "Indore"
        )
        allCities.filter { it.city in keyCityNames }.distinctBy { it.city }
    }

    // Map geographic coordinates to canvas (Bounding box of India: Lat 8.0 to 37.0, Lon 68.0 to 97.0)
    fun geoToCanvas(lat: Double, lon: Double, width: Float, height: Float): Offset {
        val minLon = 68.0
        val maxLon = 95.5
        val minLat = 8.0
        val maxLat = 36.5

        val paddingX = width * 0.08f
        val paddingY = height * 0.06f
        val usableWidth = width - 2 * paddingX
        val usableHeight = height - 2 * paddingY

        val nx = ((lon - minLon) / (maxLon - minLon)).coerceIn(0.0, 1.0).toFloat()
        val ny = (1.0 - ((lat - minLat) / (maxLat - minLat))).coerceIn(0.0, 1.0).toFloat()

        return Offset(
            x = paddingX + nx * usableWidth,
            y = paddingY + ny * usableHeight
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(allCities, width, height) {
                    detectTapGestures { tapOffset ->
                        // Detect closest city within 30dp
                        var closestCity: GeoLocation? = null
                        var minDistance = 50f

                        pinnedCities.forEach { city ->
                            val pos = geoToCanvas(city.latitude, city.longitude, width, height)
                            val dist = hypot(pos.x - tapOffset.x, pos.y - tapOffset.y)
                            if (dist < minDistance) {
                                minDistance = dist
                                closestCity = city
                            }
                        }

                        closestCity?.let { onCityClick(it) }
                    }
                }
        ) {
            // 1. Subtle latitude/longitude grid lines
            val latLines = 6
            for (i in 0..latLines) {
                val y = height * (i.toFloat() / latLines)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }
            val lonLines = 6
            for (i in 0..lonLines) {
                val x = width * (i.toFloat() / lonLines)
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
            }

            // 2. Authentic India Subcontinent Outline Path
            val indiaBorderCoords = listOf(
                Pair(35.5, 74.8), // Kashmir top
                Pair(34.5, 77.5), // Ladakh
                Pair(32.0, 78.8), // Himachal / Uttarakhand
                Pair(29.8, 80.5), // Nepal border start
                Pair(27.0, 88.0), // Sikkim / Siliguri
                Pair(27.5, 93.0), // Arunachal / Assam
                Pair(25.5, 94.5), // Nagaland / Manipur
                Pair(23.5, 92.5), // Tripura / Mizoram
                Pair(25.0, 90.0), // Meghalaya
                Pair(22.0, 89.0), // Bengal delta
                Pair(19.8, 85.8), // Odisha / Puri
                Pair(17.7, 83.3), // Visakhapatnam
                Pair(13.1, 80.3), // Chennai coast
                Pair(10.0, 79.5), // Palk Strait
                Pair(8.1, 77.5),  // Kanyakumari (Southern tip)
                Pair(9.5, 76.3),  // Kochi / Kerala coast
                Pair(13.0, 74.8), // Mangaluru / Karnataka coast
                Pair(15.5, 73.8), // Goa
                Pair(19.0, 72.8), // Mumbai / Konkan
                Pair(21.0, 72.6), // Surat / Gulf of Khambhat
                Pair(21.5, 69.6), // Saurashtra / Porbandar
                Pair(23.0, 68.5), // Kutch / Dwarka
                Pair(24.5, 71.0), // Rajasthan border
                Pair(28.0, 71.5), // Thar Desert
                Pair(31.5, 74.5), // Punjab / Wagah
                Pair(34.0, 74.5)  // Kashmir Valley
            )

            val mapPath = Path().apply {
                val start = geoToCanvas(indiaBorderCoords[0].first, indiaBorderCoords[0].second, width, height)
                moveTo(start.x, start.y)
                for (i in 1 until indiaBorderCoords.size) {
                    val pt = geoToCanvas(indiaBorderCoords[i].first, indiaBorderCoords[i].second, width, height)
                    lineTo(pt.x, pt.y)
                }
                close()
            }

            // Fill subcontinent landmass
            drawPath(
                path = mapPath,
                color = mapLandColor.copy(alpha = 0.45f)
            )

            // Outline subcontinent boundary
            drawPath(
                path = mapPath,
                color = coastlineColor,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )

            // 3. Golden Quadrilateral National Highway Spine
            val goldenQuadCities = listOf(
                Pair(28.6315, 77.2167), // Delhi
                Pair(19.0607, 72.8656), // Mumbai
                Pair(12.9767, 77.5713), // Bengaluru
                Pair(13.0694, 80.1948), // Chennai
                Pair(22.5555, 88.3510), // Kolkata
                Pair(28.6315, 77.2167)  // Back to Delhi
            )

            val gqPath = Path().apply {
                val first = geoToCanvas(goldenQuadCities[0].first, goldenQuadCities[0].second, width, height)
                moveTo(first.x, first.y)
                for (i in 1 until goldenQuadCities.size) {
                    val p = geoToCanvas(goldenQuadCities[i].first, goldenQuadCities[i].second, width, height)
                    lineTo(p.x, p.y)
                }
            }
            drawPath(
                path = gqPath,
                color = goldenQuadColor,
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                )
            )

            // 4. Active Corridor Route Path (between selected Origin and Destination)
            val originPoint = geoToCanvas(origin.latitude, origin.longitude, width, height)
            val destPoint = geoToCanvas(destination.latitude, destination.longitude, width, height)

            val corridorRoutePath = Path().apply {
                moveTo(originPoint.x, originPoint.y)
                val midX = (originPoint.x + destPoint.x) / 2
                val midY = (originPoint.y + destPoint.y) / 2
                // Gentle curve for interstate highway realism
                val dx = destPoint.x - originPoint.x
                val dy = destPoint.y - originPoint.y
                val curveOffset = 18f
                val ctrlX = midX - dy * (curveOffset / (hypot(dx, dy) + 0.1f))
                val ctrlY = midY + dx * (curveOffset / (hypot(dx, dy) + 0.1f))

                quadraticTo(ctrlX, ctrlY, destPoint.x, destPoint.y)
            }

            // Glow buffer
            drawPath(
                path = corridorRoutePath,
                color = corridorColor.copy(alpha = 0.25f),
                style = Stroke(width = 10f, cap = StrokeCap.Round)
            )
            // Solid highway line
            drawPath(
                path = corridorRoutePath,
                color = corridorColor,
                style = Stroke(width = 4.5f, cap = StrokeCap.Round)
            )

            // 5. Animated moving vehicle indicator on route
            val currentVehiclePos = Offset(
                x = originPoint.x + (destPoint.x - originPoint.x) * progress,
                y = originPoint.y + (destPoint.y - originPoint.y) * progress
            )

            drawCircle(
                color = ElectricCyanGlow.copy(alpha = 0.5f),
                radius = 12f,
                center = currentVehiclePos
            )
            drawCircle(
                color = ElectricCyan,
                radius = 6f,
                center = currentVehiclePos
            )
            drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = currentVehiclePos
            )

            // 6. Draw City Pins
            pinnedCities.forEach { city ->
                val cityPos = geoToCanvas(city.latitude, city.longitude, width, height)
                val isOrigin = city.city == origin.city
                val isDest = city.city == destination.city

                when {
                    isOrigin -> {
                        drawCircle(color = StatusSuccess.copy(alpha = 0.35f), radius = 14f, center = cityPos)
                        drawCircle(color = StatusSuccess, radius = 7f, center = cityPos)
                        drawCircle(color = Color.White, radius = 3f, center = cityPos)
                    }
                    isDest -> {
                        drawCircle(color = PrimaryBlue.copy(alpha = 0.35f), radius = 14f, center = cityPos)
                        drawCircle(color = PrimaryBlue, radius = 7f, center = cityPos)
                        drawCircle(color = Color.White, radius = 3f, center = cityPos)
                    }
                    else -> {
                        drawCircle(color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B), radius = 4f, center = cityPos)
                        drawCircle(color = Color.White, radius = 1.8f, center = cityPos)
                    }
                }
            }
        }
    }
}

// Great-circle Haversine distance formula
fun calculateHaversineDistanceKm(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val r = 6371.0 // Earth radius in kilometers
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
