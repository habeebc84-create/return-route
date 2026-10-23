package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.MatchType
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.ReturnRouteViewModel

@Composable
fun BookingsScreen(
    viewModel: ReturnRouteViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val bookings by viewModel.bookings.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Active, 2: Completed

    val filteredBookings = remember(bookings, selectedTab) {
        when (selectedTab) {
            1 -> bookings.filter { it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.REQUESTED || it.status == BookingStatus.IN_PROGRESS }
            2 -> bookings.filter { it.status == BookingStatus.COMPLETED }
            else -> bookings
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("bookings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "My Bookings & Trips",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Track confirmed rides, parcel custody, and boarding verification PINs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Tabs
        item {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All (${bookings.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Active") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Completed") }
                )
            }
        }

        if (filteredBookings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No bookings in this tab yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredBookings, key = { it.id }) { booking ->
                BookingItemCard(
                    booking = booking,
                    onStatusChange = { newStatus -> viewModel.updateBookingStatus(booking.id, newStatus) },
                    onReviewClick = { viewModel.openReviewDialog(booking) }
                )
            }
        }
    }

    // Review Dialog
    if (uiState.showReviewDialog && uiState.selectedBookingForReview != null) {
        ReviewDialog(
            booking = uiState.selectedBookingForReview!!,
            onDismiss = { viewModel.closeReviewDialog() },
            onSubmit = { rating, comment -> viewModel.submitReview(rating, comment) }
        )
    }
}

@Composable
fun BookingItemCard(
    booking: Booking,
    onStatusChange: (BookingStatus) -> Unit,
    onReviewClick: () -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (booking.requestType == MatchType.PASSENGER_RIDE) Icons.Default.DirectionsCar else Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (booking.requestType == MatchType.PASSENGER_RIDE) "Passenger Ride" else "Parcel Delivery",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                StatusBadge(status = booking.status.label)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${booking.originSummary} → ${booking.destinationSummary}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Driver: ${booking.driverName} • ${booking.departureTime}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Boarding PIN & Price Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("BOARDING PIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = booking.verificationCode,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Fare", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "₹${booking.priceUsd.toInt()}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = StatusSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Booking action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (booking.status == BookingStatus.REQUESTED) {
                    Button(
                        onClick = { onStatusChange(BookingStatus.CONFIRMED) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Confirm Booking")
                    }
                } else if (booking.status == BookingStatus.CONFIRMED) {
                    Button(
                        onClick = { onStatusChange(BookingStatus.COMPLETED) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Complete Trip")
                    }
                } else if (booking.status == BookingStatus.COMPLETED) {
                    OutlinedButton(
                        onClick = onReviewClick,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rate & Review")
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("Great return ride, arrived right on schedule!") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate & Review Trip", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Rate your experience with ${booking.driverName}:")
                Row {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Review Feedback") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(rating, comment) }) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
