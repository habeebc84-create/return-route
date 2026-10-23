package com.example.data.repository

import com.example.data.model.*
import com.example.engine.RouteMatchingEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ReturnRouteRepository {

    val sampleUsers = listOf(
        UserProfile(
            id = "user_ananya",
            name = "Ananya Iyer",
            email = "ananya.iyer@example.com",
            avatarInitials = "AI",
            activeRole = UserRole.PASSENGER,
            supportedRoles = listOf(UserRole.PASSENGER, UserRole.PARCEL_SENDER),
            rating = 4.95,
            completedTrips = 28,
            verifiedIdentity = true
        ),
        UserProfile(
            id = "user_rajesh",
            name = "Rajesh Sharma",
            email = "rajesh.sharma@example.com",
            avatarInitials = "RS",
            activeRole = UserRole.DRIVER,
            supportedRoles = listOf(UserRole.DRIVER, UserRole.DELIVERY_PROVIDER),
            rating = 4.98,
            completedTrips = 112,
            verifiedIdentity = true
        ),
        UserProfile(
            id = "user_rohan",
            name = "Rohan Malhotra",
            email = "rohan.m@example.com",
            avatarInitials = "RM",
            activeRole = UserRole.PARCEL_SENDER,
            supportedRoles = listOf(UserRole.PARCEL_SENDER, UserRole.PASSENGER),
            rating = 4.89,
            completedTrips = 19,
            verifiedIdentity = true
        ),
        UserProfile(
            id = "user_priya",
            name = "Priya Nambiar",
            email = "priya.n@example.com",
            avatarInitials = "PN",
            activeRole = UserRole.ADMIN,
            supportedRoles = listOf(UserRole.ADMIN, UserRole.DRIVER, UserRole.PASSENGER),
            rating = 5.0,
            completedTrips = 150,
            verifiedIdentity = true
        )
    )

    private val _currentUser = MutableStateFlow(sampleUsers[0])
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _vehicles = MutableStateFlow(
        listOf(
            Vehicle(
                id = "veh_innova_hycross",
                driverId = "user_rajesh",
                make = "Toyota",
                model = "Innova Hycross Hybrid",
                year = 2024,
                licensePlate = "MH-12-RT-8820",
                category = VehicleCategory.MINIVAN,
                totalSeats = 7,
                availableSeats = 6,
                maxParcelWeightKg = 180.0,
                maxParcelVolumeLiters = 850.0,
                supportedCategories = ParcelCategory.entries.toList(),
                isActive = true
            ),
            Vehicle(
                id = "veh_nexon_ev",
                driverId = "user_vikram",
                make = "Tata",
                model = "Nexon EV Max (Empowered)",
                year = 2024,
                licensePlate = "KA-01-EV-4029",
                category = VehicleCategory.SUV,
                totalSeats = 5,
                availableSeats = 4,
                maxParcelWeightKg = 90.0,
                maxParcelVolumeLiters = 420.0,
                supportedCategories = listOf(
                    ParcelCategory.DOCUMENTS,
                    ParcelCategory.SMALL_ELECTRONICS,
                    ParcelCategory.CLOTHING_GOODS,
                    ParcelCategory.FRAGILE_ITEMS
                ),
                isActive = true
            ),
            Vehicle(
                id = "veh_xuv700",
                driverId = "user_amit",
                make = "Mahindra",
                model = "XUV700 AX7L AWD",
                year = 2023,
                licensePlate = "HR-26-DK-3341",
                category = VehicleCategory.SUV,
                totalSeats = 7,
                availableSeats = 5,
                maxParcelWeightKg = 140.0,
                maxParcelVolumeLiters = 600.0,
                supportedCategories = ParcelCategory.entries.toList(),
                isActive = true
            )
        )
    )
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _journeyOffers = MutableStateFlow(
        listOf(
            JourneyOffer(
                id = "offer_blr_maa_return",
                driverId = "user_vikram",
                driverName = "Vikramaditya Rao",
                driverRating = 4.97,
                vehicleId = "veh_nexon_ev",
                vehicleDescription = "Tata Nexon EV Max (Teal Blue) • KA-01-EV-4029",
                origin = IndiaLocations.ALL_LOCATIONS.first { it.city == "Bengaluru" && it.name.contains("Electronic City") },
                destination = IndiaLocations.ALL_LOCATIONS.first { it.city == "Chennai" && it.name.contains("Koyambedu") },
                waypoints = listOf(
                    GeoLocation("Hosur SIPCOT", "NH-48", 12.7409, 77.8253, "Hosur", "Tamil Nadu"),
                    GeoLocation("Krishnagiri Toll", "NH-44/48 Junction", 12.5186, 78.2137, "Krishnagiri", "Tamil Nadu"),
                    GeoLocation("Vellore Green Circle", "NH-48", 12.9165, 79.1325, "Vellore", "Tamil Nadu"),
                    GeoLocation("Kanchipuram Bypass", "NH-48", 12.8342, 79.7036, "Kanchipuram", "Tamil Nadu")
                ),
                departureTime = "Tomorrow • 05:00 PM",
                estimatedArrivalTime = "Tomorrow • 09:30 PM",
                totalDistanceKm = 346.0,
                totalAvailableSeats = 4,
                remainingSeats = 3,
                totalParcelCapacityKg = 90.0,
                remainingParcelCapacityKg = 75.0,
                maxDetourKm = 18.0,
                maxDetourMinutes = 25,
                basePricePerSeatUsd = 550.00, // Fare in INR
                basePricePerKgUsd = 35.00,
                isReturnTrip = true,
                returnTripSavingsPercent = 30,
                acceptsPassengers = true,
                acceptsParcels = true,
                status = JourneyStatus.SCHEDULED
            ),
            JourneyOffer(
                id = "offer_mum_pun_return",
                driverId = "user_rajesh",
                driverName = "Rajesh Sharma",
                driverRating = 4.98,
                vehicleId = "veh_innova_hycross",
                vehicleDescription = "Toyota Innova Hycross Hybrid (Silver) • MH-12-RT-8820",
                origin = IndiaLocations.ALL_LOCATIONS.first { it.city == "Mumbai" && it.name.contains("Bandra") },
                destination = IndiaLocations.ALL_LOCATIONS.first { it.city == "Pune" && it.name.contains("Hinjewadi") },
                waypoints = listOf(
                    GeoLocation("Vashi Toll Naka", "Sion-Panvel Expy", 19.0620, 72.9990, "Navi Mumbai", "Maharashtra"),
                    GeoLocation("Lonavala Food Mall", "Mumbai-Pune Expy", 18.7557, 73.4091, "Lonavala", "Maharashtra"),
                    GeoLocation("Talegaon Toll Plaza", "Mumbai-Pune Expy", 18.7300, 73.6800, "Talegaon", "Maharashtra")
                ),
                departureTime = "Tomorrow • 06:30 PM",
                estimatedArrivalTime = "Tomorrow • 09:15 PM",
                totalDistanceKm = 148.0,
                totalAvailableSeats = 6,
                remainingSeats = 4,
                totalParcelCapacityKg = 180.0,
                remainingParcelCapacityKg = 150.0,
                maxDetourKm = 15.0,
                maxDetourMinutes = 20,
                basePricePerSeatUsd = 380.00,
                basePricePerKgUsd = 28.00,
                isReturnTrip = true,
                returnTripSavingsPercent = 35,
                acceptsPassengers = true,
                acceptsParcels = true,
                status = JourneyStatus.SCHEDULED
            ),
            JourneyOffer(
                id = "offer_del_jai_return",
                driverId = "user_amit",
                driverName = "Amit Verma",
                driverRating = 4.94,
                vehicleId = "veh_xuv700",
                vehicleDescription = "Mahindra XUV700 AX7 (Midnight Black) • HR-26-DK-3341",
                origin = IndiaLocations.ALL_LOCATIONS.first { it.city == "New Delhi" && it.name.contains("Connaught") },
                destination = IndiaLocations.ALL_LOCATIONS.first { it.city == "Jaipur" && it.name.contains("Sindhi") },
                waypoints = listOf(
                    GeoLocation("Gurgaon Kherki Daula Toll", "NH-48", 28.4010, 76.9950, "Gurgaon", "Haryana"),
                    GeoLocation("Neemrana Japanese Zone", "NH-48", 27.9868, 76.3860, "Neemrana", "Rajasthan"),
                    GeoLocation("Shahpura Toll", "NH-48", 27.3900, 75.9600, "Shahpura", "Rajasthan")
                ),
                departureTime = "Friday • 07:00 AM",
                estimatedArrivalTime = "Friday • 11:30 AM",
                totalDistanceKm = 268.0,
                totalAvailableSeats = 5,
                remainingSeats = 3,
                totalParcelCapacityKg = 120.0,
                remainingParcelCapacityKg = 100.0,
                maxDetourKm = 20.0,
                maxDetourMinutes = 25,
                basePricePerSeatUsd = 490.00,
                basePricePerKgUsd = 32.00,
                isReturnTrip = true,
                returnTripSavingsPercent = 25,
                acceptsPassengers = true,
                acceptsParcels = true,
                status = JourneyStatus.SCHEDULED
            ),
            JourneyOffer(
                id = "offer_hyd_blr_return",
                driverId = "user_sneha",
                driverName = "Sneha Reddy",
                driverRating = 4.92,
                vehicleId = "veh_honda_city",
                vehicleDescription = "Honda City ZX (Golden Brown) • TS-09-EJ-5521",
                origin = IndiaLocations.ALL_LOCATIONS.first { it.city == "Hyderabad" && it.name.contains("Gachibowli") },
                destination = IndiaLocations.ALL_LOCATIONS.first { it.city == "Bengaluru" && it.name.contains("Majestic") },
                waypoints = listOf(
                    GeoLocation("Kurnool Toll Plaza", "NH-44", 15.8281, 78.0373, "Kurnool", "Andhra Pradesh"),
                    GeoLocation("Anantapur Bypass", "NH-44", 14.6819, 77.6006, "Anantapur", "Andhra Pradesh"),
                    GeoLocation("Chikkaballapur", "NH-44", 13.4325, 77.7275, "Chikkaballapur", "Karnataka")
                ),
                departureTime = "Saturday • 06:00 AM",
                estimatedArrivalTime = "Saturday • 02:30 PM",
                totalDistanceKm = 569.0,
                totalAvailableSeats = 4,
                remainingSeats = 3,
                totalParcelCapacityKg = 70.0,
                remainingParcelCapacityKg = 60.0,
                maxDetourKm = 22.0,
                maxDetourMinutes = 30,
                basePricePerSeatUsd = 850.00,
                basePricePerKgUsd = 42.00,
                isReturnTrip = true,
                returnTripSavingsPercent = 30,
                acceptsPassengers = true,
                acceptsParcels = true,
                status = JourneyStatus.SCHEDULED
            )
        )
    )
    val journeyOffers: StateFlow<List<JourneyOffer>> = _journeyOffers.asStateFlow()

    private val _rideRequests = MutableStateFlow(
        listOf(
            RideRequest(
                id = "req_ride_blr_maa",
                passengerId = "user_ananya",
                passengerName = "Ananya Iyer",
                origin = IndiaLocations.ALL_LOCATIONS.first { it.city == "Bengaluru" && it.name.contains("Koramangala") },
                destination = IndiaLocations.ALL_LOCATIONS.first { it.city == "Chennai" && it.name.contains("OMR") },
                preferredDate = "Tomorrow",
                earliestDepartureTime = "04:30 PM",
                latestDepartureTime = "06:30 PM",
                requiredSeats = 1,
                maxDetourToleranceKm = 15.0,
                maxBudgetUsd = 600.0,
                luggagePieces = 1,
                notes = "Returning to Chennai office after client meetings. Flexible pickup along Hosur Road Elevated Toll.",
                isReturnTripRequest = true,
                status = JourneyStatus.SCHEDULED
            )
        )
    )
    val rideRequests: StateFlow<List<RideRequest>> = _rideRequests.asStateFlow()

    private val _parcelRequests = MutableStateFlow(
        listOf(
            ParcelRequest(
                id = "req_parcel_mumbai_pune",
                senderId = "user_rohan",
                senderName = "Rohan Malhotra",
                pickup = IndiaLocations.ALL_LOCATIONS.first { it.city == "Mumbai" && it.name.contains("Bandra") },
                destination = IndiaLocations.ALL_LOCATIONS.first { it.city == "Pune" && it.name.contains("Viman Nagar") },
                category = ParcelCategory.SMALL_ELECTRONICS,
                weightKg = 3.5,
                preferredDate = "Tomorrow",
                deliveryDeadline = "By 09:30 PM",
                maxBudgetUsd = 400.0,
                handlingInstructions = "Hardware test dev board in cushioned box. Handle with care.",
                status = JourneyStatus.SCHEDULED
            )
        )
    )
    val parcelRequests: StateFlow<List<ParcelRequest>> = _parcelRequests.asStateFlow()

    private val _bookings = MutableStateFlow(
        listOf(
            Booking(
                id = "book_confirmed_blr_maa",
                journeyOfferId = "offer_blr_maa_return",
                journeyDescription = "Bengaluru → Chennai (NH-48 Express Corridor)",
                requestId = "req_ride_blr_maa",
                requestType = MatchType.PASSENGER_RIDE,
                bookerId = "user_ananya",
                bookerName = "Ananya Iyer",
                driverId = "user_vikram",
                driverName = "Vikramaditya Rao",
                seatsBooked = 1,
                parcelWeightKg = 0.0,
                originSummary = "Bengaluru (Electronic City Toll)",
                destinationSummary = "Chennai (OMR / Guindy)",
                departureTime = "Tomorrow • 05:00 PM",
                priceUsd = 550.00,
                status = BookingStatus.CONFIRMED,
                verificationCode = "482910"
            ),
            Booking(
                id = "book_completed_mum_pun",
                journeyOfferId = "offer_mum_pun_return",
                journeyDescription = "Mumbai → Pune Expressway Corridor",
                requestId = "req_parcel_mumbai_pune",
                requestType = MatchType.PARCEL_DELIVERY,
                bookerId = "user_rohan",
                bookerName = "Rohan Malhotra",
                driverId = "user_rajesh",
                driverName = "Rajesh Sharma",
                seatsBooked = 0,
                parcelWeightKg = 3.5,
                originSummary = "Mumbai (BKC)",
                destinationSummary = "Pune (Hinjewadi)",
                departureTime = "Yesterday • 06:00 PM",
                priceUsd = 380.00,
                status = BookingStatus.COMPLETED,
                verificationCode = "918234"
            )
        )
    )
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _notifications = MutableStateFlow(
        listOf(
            NotificationItem(
                userId = "user_ananya",
                title = "🎉 Booking Confirmed with Return Discount!",
                message = "Vikramaditya Rao accepted your ride from Bengaluru to Chennai. You saved 30% on this return corridor trip! Boarding Code: 482910",
                type = NotificationType.BOOKING_CONFIRMED,
                relatedBookingId = "book_confirmed_blr_maa"
            ),
            NotificationItem(
                userId = "user_ananya",
                title = "🇮🇳 High Return Corridor Opportunity",
                message = "5 verified drivers returning along Mumbai-Pune and Delhi-Jaipur expressways tomorrow. Return fares starting at ₹380.",
                type = NotificationType.ADMIN_NOTICE
            )
        )
    )
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _reviews = MutableStateFlow(
        listOf(
            Review(
                bookingId = "book_completed_mum_pun",
                reviewerId = "user_rohan",
                reviewerName = "Rohan Malhotra",
                revieweeId = "user_rajesh",
                rating = 5,
                comment = "Excellent return courier delivery! Rajesh dropped off the package safely in Hinjewadi right on schedule. 10/10."
            )
        )
    )
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _issueReports = MutableStateFlow(
        listOf(
            IssueReport(
                reporterId = "user_rohan",
                reporterName = "Rohan Malhotra",
                category = "Pickup Timing",
                subject = "Toll plaza traffic alert",
                description = "Khed Shivapur toll traffic delayed pickup by 15 mins. Driver informed proactively via messaging.",
                status = ReportStatus.RESOLVED,
                resolutionNotes = "Trip completed smoothly and parcel was received in pristine condition."
            )
        )
    )
    val issueReports: StateFlow<List<IssueReport>> = _issueReports.asStateFlow()

    fun switchUser(userId: String) {
        val user = sampleUsers.find { it.id == userId } ?: return
        _currentUser.value = user
    }

    fun switchRole(role: UserRole) {
        val user = _currentUser.value
        if (role in user.supportedRoles) {
            _currentUser.value = user.copy(activeRole = role)
        }
    }

    fun addVehicle(vehicle: Vehicle) {
        _vehicles.value = _vehicles.value + vehicle
    }

    fun postJourneyOffer(offer: JourneyOffer) {
        _journeyOffers.value = listOf(offer) + _journeyOffers.value
    }

    fun postRideRequest(request: RideRequest) {
        _rideRequests.value = listOf(request) + _rideRequests.value
    }

    fun postParcelRequest(request: ParcelRequest) {
        _parcelRequests.value = listOf(request) + _parcelRequests.value
    }

    fun createBooking(
        offer: JourneyOffer,
        requestId: String,
        type: MatchType,
        seats: Int,
        weightKg: Double,
        priceUsd: Double
    ): Booking {
        val verificationCode = (100000..999999).random().toString()
        val booking = Booking(
            journeyOfferId = offer.id,
            journeyDescription = "${offer.origin.city} → ${offer.destination.city}",
            requestId = requestId,
            requestType = type,
            bookerId = _currentUser.value.id,
            bookerName = _currentUser.value.name,
            driverId = offer.driverId,
            driverName = offer.driverName,
            seatsBooked = seats,
            parcelWeightKg = weightKg,
            originSummary = offer.origin.name,
            destinationSummary = offer.destination.name,
            departureTime = offer.departureTime,
            priceUsd = priceUsd,
            status = BookingStatus.CONFIRMED,
            verificationCode = verificationCode
        )

        _bookings.value = listOf(booking) + _bookings.value

        // Update offer available capacities
        _journeyOffers.value = _journeyOffers.value.map {
            if (it.id == offer.id) {
                it.copy(
                    remainingSeats = (it.remainingSeats - seats).coerceAtLeast(0),
                    remainingParcelCapacityKg = (it.remainingParcelCapacityKg - weightKg).coerceAtLeast(0.0)
                )
            } else it
        }

        addNotification(
            userId = _currentUser.value.id,
            title = "🎉 Booking Confirmed (${if (offer.isReturnTrip) "Return -${offer.returnTripSavingsPercent}%" else "Standard"})",
            message = "Your seat/cargo is confirmed with ${offer.driverName}. Verification PIN: $verificationCode",
            type = NotificationType.BOOKING_CONFIRMED,
            relatedBookingId = booking.id
        )

        return booking
    }

    fun createBooking(
        candidate: MatchCandidate,
        seats: Int,
        weightKg: Double
    ): Booking {
        return createBooking(
            offer = candidate.journeyOffer,
            requestId = candidate.requestId,
            type = candidate.matchType,
            seats = seats,
            weightKg = weightKg,
            priceUsd = candidate.estimatedCostUsd
        )
    }

    fun updateBookingStatus(bookingId: String, newStatus: BookingStatus) {
        _bookings.value = _bookings.value.map {
            if (it.id == bookingId) it.copy(status = newStatus) else it
        }
    }

    fun addReview(review: Review) {
        _reviews.value = listOf(review) + _reviews.value
    }
    fun submitReview(review: Review) = addReview(review)

    fun addIssueReport(report: IssueReport) {
        _issueReports.value = listOf(report) + _issueReports.value
    }
    fun submitReport(report: IssueReport) = addIssueReport(report)

    fun resolveReport(reportId: String, notes: String) {
        _issueReports.value = _issueReports.value.map {
            if (it.id == reportId) it.copy(status = ReportStatus.RESOLVED, resolutionNotes = notes) else it
        }
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }
    fun markAllNotificationsRead() = markAllNotificationsAsRead()

    fun findMatchesForRide(rideRequest: RideRequest): List<MatchCandidate> {
        val activeOffers = _journeyOffers.value.filter { it.status == JourneyStatus.SCHEDULED && it.acceptsPassengers }
        return activeOffers.map { offer ->
            RouteMatchingEngine.evaluateRideMatch(rideRequest, offer)
        }.filter { it.isEligible }.sortedByDescending { it.compatibilityScore }
    }

    fun findMatchesForParcel(parcelRequest: ParcelRequest): List<MatchCandidate> {
        val activeOffers = _journeyOffers.value.filter { it.status == JourneyStatus.SCHEDULED && it.acceptsParcels }
        return activeOffers.map { offer ->
            RouteMatchingEngine.evaluateParcelMatch(parcelRequest, offer)
        }.filter { it.isEligible }.sortedByDescending { it.compatibilityScore }
    }

    suspend fun fetchLiveCorridorsFromNetwork(): Result<Int> = syncNationwideOffersFromNetwork()

    // Network Sync State
    private val _isNetworkSyncing = MutableStateFlow(false)
    val isNetworkSyncing: StateFlow<Boolean> = _isNetworkSyncing.asStateFlow()

    private val _lastNetworkSyncTime = MutableStateFlow(System.currentTimeMillis())
    val lastNetworkSyncTime: StateFlow<Long> = _lastNetworkSyncTime.asStateFlow()

    suspend fun syncNationwideOffersFromNetwork(): Result<Int> {
        _isNetworkSyncing.value = true
        kotlinx.coroutines.delay(1200) // Realistic network roundtrip

        val nationwideOffers = listOf(
            JourneyOffer(
                id = "offer_del_chd_live",
                driverId = "user_gurpreet",
                driverName = "Gurpreet Singh",
                driverRating = 4.95,
                vehicleId = "veh_skoda_slavia",
                vehicleDescription = "Skoda Slavia 1.5 TSI (Candy White) • PB-10-CZ-8812",
                origin = IndiaLocations.ALL_LOCATIONS.first { it.city == "Delhi" && it.name.contains("Kashmere") },
                destination = IndiaLocations.ALL_LOCATIONS.first { it.city == "Chandigarh" && it.name.contains("Sector 17") },
                waypoints = listOf(
                    GeoLocation("Murthal Haveli Stop", "NH-44", 29.0298, 77.0700, "Sonipat", "Haryana"),
                    GeoLocation("Panipat Toll", "NH-44", 29.3909, 76.9635, "Panipat", "Haryana"),
                    GeoLocation("Ambala Cantt", "NH-44", 30.3782, 76.7767, "Ambala", "Haryana")
                ),
                departureTime = "Today • 04:30 PM",
                estimatedArrivalTime = "Today • 08:30 PM",
                totalDistanceKm = 245.0,
                totalAvailableSeats = 3,
                remainingSeats = 2,
                totalParcelCapacityKg = 60.0,
                remainingParcelCapacityKg = 50.0,
                maxDetourKm = 12.0,
                maxDetourMinutes = 18,
                basePricePerSeatUsd = 420.0,
                basePricePerKgUsd = 26.0,
                isReturnTrip = true,
                returnTripSavingsPercent = 25,
                acceptsPassengers = true,
                acceptsParcels = true,
                status = JourneyStatus.SCHEDULED
            ),
            JourneyOffer(
                id = "offer_del_lko_live",
                driverId = "user_manish",
                driverName = "Manish Tripathi",
                driverRating = 4.91,
                vehicleId = "veh_hyundai_verna",
                vehicleDescription = "Hyundai Verna Turbo (Starry Night) • UP-32-BZ-9011",
                origin = IndiaLocations.ALL_LOCATIONS.first { it.city == "New Delhi" && it.name.contains("Connaught") },
                destination = IndiaLocations.ALL_LOCATIONS.first { it.city == "Lucknow" && it.name.contains("Charbagh") },
                waypoints = listOf(
                    GeoLocation("Mathura Toll", "Yamuna Expy", 27.5500, 77.7200, "Mathura", "Uttar Pradesh"),
                    GeoLocation("Agra Ring Road", "Agra-Lucknow Expy", 27.1800, 78.0800, "Agra", "Uttar Pradesh")
                ),
                departureTime = "Tomorrow • 06:00 AM",
                estimatedArrivalTime = "Tomorrow • 01:00 PM",
                totalDistanceKm = 535.0,
                totalAvailableSeats = 4,
                remainingSeats = 3,
                totalParcelCapacityKg = 70.0,
                remainingParcelCapacityKg = 60.0,
                maxDetourKm = 15.0,
                maxDetourMinutes = 20,
                basePricePerSeatUsd = 750.0,
                basePricePerKgUsd = 38.0,
                isReturnTrip = true,
                returnTripSavingsPercent = 30,
                acceptsPassengers = true,
                acceptsParcels = true,
                status = JourneyStatus.SCHEDULED
            ),
            JourneyOffer(
                id = "offer_amd_mum_live",
                driverId = "user_paresh",
                driverName = "Paresh Patel",
                driverRating = 4.93,
                vehicleId = "veh_kia_carens",
                vehicleDescription = "Kia Carens Luxury Plus • GJ-01-RA-4491",
                origin = IndiaLocations.ALL_LOCATIONS.first { it.city == "Ahmedabad" && it.name.contains("SG Highway") },
                destination = IndiaLocations.ALL_LOCATIONS.first { it.city == "Mumbai" && it.name.contains("Dadar") },
                waypoints = listOf(
                    GeoLocation("Vadodara Bypass", "NH-48", 22.3072, 73.1812, "Vadodara", "Gujarat"),
                    GeoLocation("Surat Kamrej Toll", "NH-48", 21.2721, 72.9610, "Surat", "Gujarat")
                ),
                departureTime = "Saturday • 07:00 AM",
                estimatedArrivalTime = "Saturday • 03:30 PM",
                totalDistanceKm = 525.0,
                totalAvailableSeats = 5,
                remainingSeats = 4,
                totalParcelCapacityKg = 150.0,
                remainingParcelCapacityKg = 120.0,
                maxDetourKm = 18.0,
                maxDetourMinutes = 25,
                basePricePerSeatUsd = 780.0,
                basePricePerKgUsd = 36.0,
                isReturnTrip = true,
                returnTripSavingsPercent = 30,
                acceptsPassengers = true,
                acceptsParcels = true,
                status = JourneyStatus.SCHEDULED
            ),
            JourneyOffer(
                id = "offer_koc_blr_live",
                driverId = "user_thomas",
                driverName = "Thomas Kurian",
                driverRating = 4.96,
                vehicleId = "veh_grand_vitara",
                vehicleDescription = "Maruti Grand Vitara Hybrid • KL-07-CM-1192",
                origin = IndiaLocations.ALL_LOCATIONS.first { it.city == "Kochi" && it.name.contains("Vyttila") },
                destination = IndiaLocations.ALL_LOCATIONS.first { it.city == "Bengaluru" && it.name.contains("Electronic City") },
                waypoints = listOf(
                    GeoLocation("Thrissur Bypass", "NH-544", 10.5276, 76.2144, "Thrissur", "Kerala"),
                    GeoLocation("Coimbatore Bypass", "L&T Tollway", 10.9600, 77.0100, "Coimbatore", "Tamil Nadu"),
                    GeoLocation("Salem Junction", "NH-44", 11.6643, 78.1460, "Salem", "Tamil Nadu")
                ),
                departureTime = "Sunday • 05:30 AM",
                estimatedArrivalTime = "Sunday • 02:45 PM",
                totalDistanceKm = 548.0,
                totalAvailableSeats = 4,
                remainingSeats = 3,
                totalParcelCapacityKg = 85.0,
                remainingParcelCapacityKg = 70.0,
                maxDetourKm = 20.0,
                maxDetourMinutes = 25,
                basePricePerSeatUsd = 820.0,
                basePricePerKgUsd = 40.0,
                isReturnTrip = true,
                returnTripSavingsPercent = 28,
                acceptsPassengers = true,
                acceptsParcels = true,
                status = JourneyStatus.SCHEDULED
            )
        )

        val currentIds = _journeyOffers.value.map { it.id }.toSet()
        val newOffers = nationwideOffers.filter { it.id !in currentIds }
        _journeyOffers.value = _journeyOffers.value + newOffers

        _lastNetworkSyncTime.value = System.currentTimeMillis()
        _isNetworkSyncing.value = false

        addNotification(
            userId = _currentUser.value.id,
            title = "🇮🇳 Sample India Corridors Loaded",
            message = "Loaded bundled sample return routes across India. ${newOffers.size} sample corridor trips added.",
            type = NotificationType.SYSTEM
        )

        return Result.success(newOffers.size)
    }

    private fun addNotification(
        userId: String,
        title: String,
        message: String,
        type: NotificationType,
        relatedBookingId: String? = null
    ) {
        val item = NotificationItem(
            userId = userId,
            title = title,
            message = message,
            type = type,
            relatedBookingId = relatedBookingId
        )
        _notifications.value = listOf(item) + _notifications.value
    }
}
