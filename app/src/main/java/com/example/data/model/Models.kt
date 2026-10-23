package com.example.data.model

import java.util.UUID

enum class UserRole(val label: String) {
    PASSENGER("Passenger"),
    DRIVER("Driver / Traveller"),
    PARCEL_SENDER("Parcel Sender"),
    DELIVERY_PROVIDER("Delivery Provider"),
    ADMIN("Platform Admin")
}

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val avatarInitials: String,
    val activeRole: UserRole,
    val supportedRoles: List<UserRole>,
    val rating: Double = 4.9,
    val completedTrips: Int = 18,
    val verifiedIdentity: Boolean = true,
    val memberSince: String = "2024"
)

enum class VehicleCategory(val label: String, val passengerCapacity: Int, val maxCargoKg: Double) {
    SEDAN("Sedan", 3, 40.0),
    SUV("SUV / Crossover", 4, 80.0),
    HATCHBACK("Hatchback", 3, 30.0),
    MINIVAN("Minivan", 6, 120.0),
    PICKUP_TRUCK("Pickup Truck", 4, 250.0),
    CARGO_VAN("Cargo Van", 2, 600.0)
}

data class Vehicle(
    val id: String = UUID.randomUUID().toString(),
    val driverId: String,
    val make: String,
    val model: String,
    val year: Int,
    val licensePlate: String,
    val category: VehicleCategory,
    val totalSeats: Int,
    val availableSeats: Int,
    val maxParcelWeightKg: Double,
    val maxParcelVolumeLiters: Double,
    val supportedCategories: List<ParcelCategory>,
    val isActive: Boolean = true
)

data class GeoLocation(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val state: String = ""
)

data class RideRequest(
    val id: String = UUID.randomUUID().toString(),
    val passengerId: String,
    val passengerName: String,
    val passengerRating: Double = 4.9,
    val origin: GeoLocation,
    val destination: GeoLocation,
    val preferredDate: String,
    val earliestDepartureTime: String,
    val latestDepartureTime: String,
    val requiredSeats: Int = 1,
    val maxDetourToleranceKm: Double = 15.0,
    val maxBudgetUsd: Double,
    val luggagePieces: Int = 1,
    val notes: String = "",
    val isReturnTripRequest: Boolean = false,
    val status: JourneyStatus = JourneyStatus.SCHEDULED,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ParcelCategory(val label: String, val maxInsuranceUsd: Double) {
    DOCUMENTS("Documents & Contracts", 100.0),
    SMALL_ELECTRONICS("Small Electronics", 1000.0),
    CLOTHING_GOODS("Apparel & Personal Goods", 300.0),
    FRAGILE_ITEMS("Fragile Items", 500.0),
    GENERAL_CARGO("General Cargo", 400.0)
}

data class ParcelRequest(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val senderName: String,
    val pickup: GeoLocation,
    val destination: GeoLocation,
    val category: ParcelCategory,
    val weightKg: Double,
    val preferredDate: String,
    val deliveryDeadline: String,
    val maxBudgetUsd: Double,
    val handlingInstructions: String = "",
    val status: JourneyStatus = JourneyStatus.SCHEDULED,
    val createdAt: Long = System.currentTimeMillis()
)

enum class JourneyStatus(val label: String) {
    SCHEDULED("Scheduled"),
    ACTIVE("Active / On Route"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class JourneyOffer(
    val id: String = UUID.randomUUID().toString(),
    val driverId: String,
    val driverName: String,
    val driverRating: Double = 4.9,
    val vehicleId: String,
    val vehicleDescription: String,
    val origin: GeoLocation,
    val destination: GeoLocation,
    val waypoints: List<GeoLocation> = emptyList(),
    val departureTime: String,
    val estimatedArrivalTime: String,
    val totalDistanceKm: Double,
    val totalAvailableSeats: Int,
    val remainingSeats: Int,
    val totalParcelCapacityKg: Double,
    val remainingParcelCapacityKg: Double,
    val maxDetourKm: Double,
    val maxDetourMinutes: Int,
    val basePricePerSeatUsd: Double,
    val basePricePerKgUsd: Double,
    val isReturnTrip: Boolean = false,
    val returnTripSavingsPercent: Int = 0,
    val acceptsPassengers: Boolean = true,
    val acceptsParcels: Boolean = true,
    val status: JourneyStatus = JourneyStatus.SCHEDULED,
    val createdAt: Long = System.currentTimeMillis()
)

enum class MatchType {
    PASSENGER_RIDE,
    PARCEL_DELIVERY
}

data class MatchCandidate(
    val id: String = UUID.randomUUID().toString(),
    val requestId: String,
    val journeyOffer: JourneyOffer,
    val matchType: MatchType,
    val compatibilityScore: Int, // 0 - 100
    val isEligible: Boolean,
    val detourDistanceKm: Double,
    val detourDurationMinutes: Int,
    val pickupProximityKm: Double,
    val dropoffProximityKm: Double,
    val estimatedCostUsd: Double,
    val reasons: List<String>,
    val rejectionReasons: List<String> = emptyList()
)

enum class BookingStatus(val label: String) {
    REQUESTED("Requested"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    CONFIRMED("Confirmed"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    DISPUTED("Disputed")
}

data class Booking(
    val id: String = UUID.randomUUID().toString(),
    val journeyOfferId: String,
    val journeyDescription: String,
    val requestId: String,
    val requestType: MatchType,
    val bookerId: String,
    val bookerName: String,
    val driverId: String,
    val driverName: String,
    val seatsBooked: Int = 0,
    val parcelWeightKg: Double = 0.0,
    val originSummary: String,
    val destinationSummary: String,
    val departureTime: String,
    val priceUsd: Double,
    val status: BookingStatus = BookingStatus.REQUESTED,
    val verificationCode: String = (100000..999999).random().toString(),
    val createdAt: Long = System.currentTimeMillis()
)

enum class NotificationType {
    BOOKING_REQUEST,
    BOOKING_ACCEPTED,
    BOOKING_REJECTED,
    BOOKING_CONFIRMED,
    TRIP_STATUS_UPDATE,
    CANCELLED,
    ADMIN_NOTICE,
    SYSTEM
}

data class NotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val relatedBookingId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class Review(
    val id: String = UUID.randomUUID().toString(),
    val bookingId: String,
    val reviewerId: String,
    val reviewerName: String,
    val revieweeId: String,
    val rating: Int, // 1 - 5
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ReportStatus(val label: String) {
    OPEN("Open"),
    UNDER_REVIEW("Under Review"),
    RESOLVED("Resolved")
}

data class IssueReport(
    val id: String = UUID.randomUUID().toString(),
    val reporterId: String,
    val reporterName: String,
    val category: String,
    val subject: String,
    val description: String,
    val status: ReportStatus = ReportStatus.OPEN,
    val resolutionNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class DemandForecast(
    val corridorName: String,
    val originRegion: String,
    val destRegion: String,
    val passengerDemandIndex: Int, // 1 - 100
    val parcelDemandIndex: Int, // 1 - 100
    val returnTripOpportunityScore: Int, // 1 - 100
    val peakHourRecommendation: String,
    val trend: String
)
