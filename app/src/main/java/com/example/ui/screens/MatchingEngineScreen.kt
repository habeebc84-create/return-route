package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.InteractiveRouteMap
import com.example.ui.components.ReturnRouteIcon
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.ReturnRouteViewModel

@Composable
fun MatchingEngineScreen(
    viewModel: ReturnRouteViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val offers by viewModel.journeyOffers.collectAsState()
    val sampleOffer = offers.firstOrNull()

    var detourSliderValue by remember { mutableFloatStateOf(18f) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("matching_engine_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Geospatial Route Matching Engine",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Deterministic corridor calculation linking passenger & parcel detours to active return legs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (sampleOffer != null) {
            item {
                InteractiveRouteMap(
                    origin = sampleOffer.origin,
                    destination = sampleOffer.destination,
                    waypoints = sampleOffer.waypoints,
                    detourKm = detourSliderValue.toDouble()
                )
                Spacer(modifier = Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = { viewModel.navigateTo(com.example.ui.viewmodel.ScreenDestination.INDIA_MAP) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Full 🇮🇳 India Subcontinent Highway Map", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Corridor Detour Tolerance Buffer",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Max allowable deviation from primary highway corridor: ${detourSliderValue.toInt()} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = detourSliderValue,
                        onValueChange = {
                            detourSliderValue = it
                            viewModel.setMaxDetourFilter(it.toDouble())
                        },
                        valueRange = 5f..50f,
                        steps = 8,
                        modifier = Modifier.testTag("detour_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("5 km (Strict)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("25 km (Balanced)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("50 km (Wide Corridor)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Algorithm Rules & Weightings",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Haversine formula calculates sub-second point-to-segment distance.", style = MaterialTheme.typography.bodySmall)
                    Text("• Hard eligibility constraints: active vehicle seats, payload capacity, detour cap.", style = MaterialTheme.typography.bodySmall)
                    Text("• Return-trip bonus: +15 points & 25% automated price reduction.", style = MaterialTheme.typography.bodySmall)
                    Text("• Top-rated driver bonus: +5 points for ratings >= 4.90.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
