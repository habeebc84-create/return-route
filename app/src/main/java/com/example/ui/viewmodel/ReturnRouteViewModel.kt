package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.task.AppDatabase
import com.example.data.local.task.TaskEntity
import com.example.data.local.task.TaskRepository
import com.example.data.model.*
import com.example.data.repository.ReturnRouteRepository
import com.example.engine.DemandPredictionEngine
import com.example.engine.PricingEngine
import com.example.engine.RouteMatchingEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ScreenDestination(val title: String) {
    LANDING("ReturnRoute"),
    INDIA_MAP("India Route Map"),
    RIDES("Passenger Rides"),
    PARCELS("Parcel Deliveries"),
    OFFERS("Driver Journeys"),
    MATCHING_ENGINE("Matching Engine"),
    BOOKINGS("My Bookings"),
    TASKS("Project Tasks"),
    VEHICLES("Vehicles"),
    DEMAND_INSIGHTS("Corridor Insights"),
    NOTIFICATIONS("Notifications"),
    ADMIN("Admin Dashboard"),
    SAFETY_LEGAL("Safety & Legal")
}

enum class SortOption(val label: String) {
    BEST_MATCH("Best Match"),
    LOWEST_PRICE("Lowest Price"),
    EARLIEST_DEPARTURE("Earliest"),
    MINIMAL_DETOUR("Lowest Detour")
}

data class UiState(
    val currentScreen: ScreenDestination = ScreenDestination.LANDING,
    val isAuthenticated: Boolean = false,
    val isDarkMode: Boolean = false, // Clean modern booking app light mode by default, toggleable anytime
    val isNetworkSyncing: Boolean = false,
    val lastSyncTime: Long = System.currentTimeMillis(),
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.BEST_MATCH,
    val filterReturnTripsOnly: Boolean = false,
    val selectedVehicleCategory: VehicleCategory? = null,
    val maxDetourFilterKm: Double? = null,
    val selectedCorridorFilter: String = "All",
    val activeRideMatches: List<MatchCandidate> = emptyList(),
    val activeParcelMatches: List<MatchCandidate> = emptyList(),
    val selectedMatchCandidate: MatchCandidate? = null,
    val showBookingDialog: Boolean = false,
    val showCreateRideDialog: Boolean = false,
    val showCreateParcelDialog: Boolean = false,
    val showCreateOfferDialog: Boolean = false,
    val showCreateVehicleDialog: Boolean = false,
    val showReviewDialog: Boolean = false,
    val showReportDialog: Boolean = false,
    val selectedBookingForReview: Booking? = null,
    val snackbarMessage: String? = null
)

class ReturnRouteViewModel constructor(
    application: Application,
    val repository: ReturnRouteRepository = ReturnRouteRepository(),
    customTaskRepository: TaskRepository? = null
) : AndroidViewModel(application) {

    constructor(application: Application) : this(application, ReturnRouteRepository(), null)

    val taskRepository: TaskRepository = customTaskRepository
        ?: TaskRepository(AppDatabase.getDatabase(application, viewModelScope).taskDao())

    val tasks: StateFlow<List<TaskEntity>> = taskRepository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val currentUser = repository.currentUser
    val journeyOffers = repository.journeyOffers
    val rideRequests = combine(repository.rideRequests, currentUser) { items, user ->
        items.filter { it.passengerId == user.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val parcelRequests = combine(repository.parcelRequests, currentUser) { items, user ->
        items.filter { it.senderId == user.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val bookings = combine(repository.bookings, currentUser) { items, user ->
        items.filter { it.bookerId == user.id || it.driverId == user.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notifications = combine(repository.notifications, currentUser) { items, user ->
        items.filter { user.activeRole == UserRole.ADMIN || it.userId == user.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val vehicles = combine(repository.vehicles, currentUser) { items, user ->
        if (user.activeRole == UserRole.ADMIN) items else items.filter { it.driverId == user.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val reviews = combine(repository.reviews, currentUser) { items, user ->
        items.filter { it.reviewerId == user.id || it.revieweeId == user.id || user.activeRole == UserRole.ADMIN }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val issueReports = combine(repository.issueReports, currentUser) { items, user ->
        if (user.activeRole == UserRole.ADMIN) items else items.filter { it.reporterId == user.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val demandForecasts = DemandPredictionEngine.getForecasts()

    init {
        refreshMatches()
    }

    fun navigateTo(screen: ScreenDestination) {
        if (!canAccess(screen, currentUser.value.activeRole)) {
            showSnackbar("That section is not available for this demo account.")
            return
        }
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun signInDemo(role: UserRole) {
        val userId = when (role) {
            UserRole.ADMIN -> "user_priya"
            UserRole.DRIVER, UserRole.DELIVERY_PROVIDER -> "user_rajesh"
            UserRole.PARCEL_SENDER -> "user_rohan"
            UserRole.PASSENGER -> "user_ananya"
        }
        repository.switchUser(userId)
        repository.switchRole(role)
        val landing = when (role) {
            UserRole.ADMIN -> ScreenDestination.ADMIN
            UserRole.DRIVER, UserRole.DELIVERY_PROVIDER -> ScreenDestination.OFFERS
            else -> ScreenDestination.LANDING
        }
        _uiState.update { it.copy(isAuthenticated = true, currentScreen = landing) }
        refreshMatches()
    }

    fun signOutDemo() {
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                currentScreen = ScreenDestination.LANDING,
                showBookingDialog = false,
                showCreateRideDialog = false,
                showCreateParcelDialog = false,
                showCreateOfferDialog = false,
                showCreateVehicleDialog = false,
                showReviewDialog = false,
                showReportDialog = false,
                selectedBookingForReview = null,
                selectedMatchCandidate = null
            )
        }
    }

    private fun canAccess(screen: ScreenDestination, role: UserRole): Boolean {
        if (role == UserRole.ADMIN) return true
        val common = setOf(
            ScreenDestination.LANDING,
            ScreenDestination.INDIA_MAP,
            ScreenDestination.BOOKINGS,
            ScreenDestination.NOTIFICATIONS,
            ScreenDestination.SAFETY_LEGAL
        )
        val userScreens = common + setOf(ScreenDestination.RIDES, ScreenDestination.PARCELS)
        val riderScreens = common + setOf(ScreenDestination.OFFERS, ScreenDestination.VEHICLES)
        return when (role) {
            UserRole.PASSENGER, UserRole.PARCEL_SENDER -> screen in userScreens
            UserRole.DRIVER, UserRole.DELIVERY_PROVIDER -> screen in riderScreens
            UserRole.ADMIN -> true
        }
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun setDarkMode(isDark: Boolean) {
        _uiState.update { it.copy(isDarkMode = isDark) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSortOption(option: SortOption) {
        _uiState.update { it.copy(sortOption = option) }
    }

    fun toggleReturnTripsOnly(enabled: Boolean) {
        _uiState.update { it.copy(filterReturnTripsOnly = enabled) }
    }

    fun setVehicleCategoryFilter(category: VehicleCategory?) {
        _uiState.update { it.copy(selectedVehicleCategory = category) }
    }

    fun setCorridorFilter(corridor: String) {
        _uiState.update { it.copy(selectedCorridorFilter = corridor) }
    }

    fun selectMatch(candidate: MatchCandidate?) {
        _uiState.update { it.copy(selectedMatchCandidate = candidate) }
    }

    fun openBookingDialog(candidate: MatchCandidate) {
        _uiState.update { it.copy(selectedMatchCandidate = candidate, showBookingDialog = true) }
    }

    fun closeBookingDialog() {
        _uiState.update { it.copy(showBookingDialog = false) }
    }

    fun openCreateRideDialog() = _uiState.update { it.copy(showCreateRideDialog = true) }
    fun closeCreateRideDialog() = _uiState.update { it.copy(showCreateRideDialog = false) }

    fun openCreateParcelDialog() = _uiState.update { it.copy(showCreateParcelDialog = true) }
    fun closeCreateParcelDialog() = _uiState.update { it.copy(showCreateParcelDialog = false) }

    fun openCreateOfferDialog() = _uiState.update { it.copy(showCreateOfferDialog = true) }
    fun closeCreateOfferDialog() = _uiState.update { it.copy(showCreateOfferDialog = false) }

    fun openCreateVehicleDialog() = _uiState.update { it.copy(showCreateVehicleDialog = true) }
    fun closeCreateVehicleDialog() = _uiState.update { it.copy(showCreateVehicleDialog = false) }

    fun openReviewDialog(booking: Booking) = _uiState.update { it.copy(selectedBookingForReview = booking, showReviewDialog = true) }
    fun closeReviewDialog() = _uiState.update { it.copy(showReviewDialog = false, selectedBookingForReview = null) }

    fun openReportDialog() = _uiState.update { it.copy(showReportDialog = true) }
    fun closeReportDialog() = _uiState.update { it.copy(showReportDialog = false) }

    fun dismissSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun setMaxDetourFilter(maxKm: Double?) {
        _uiState.update { it.copy(maxDetourFilterKm = maxKm) }
        refreshMatches()
    }

    fun fetchLiveNetworkCorridors() {
        viewModelScope.launch {
            _uiState.update { it.copy(isNetworkSyncing = true) }
            showSnackbar("Loading bundled sample corridors...")
            val result = repository.fetchLiveCorridorsFromNetwork()
            result.onSuccess { count ->
                _uiState.update {
                    it.copy(
                        isNetworkSyncing = false,
                        lastSyncTime = System.currentTimeMillis()
                    )
                }
                refreshMatches()
                showSnackbar("Loaded $count sample return-route corridors.")
            }.onFailure {
                _uiState.update { it.copy(isNetworkSyncing = false) }
                showSnackbar("Sample corridors could not be loaded. Please try again.")
            }
        }
    }

    fun refreshMatches() {
        viewModelScope.launch {
            val userId = currentUser.value.id
            val firstRide = repository.rideRequests.value.firstOrNull { it.passengerId == userId }
            val firstParcel = repository.parcelRequests.value.firstOrNull { it.senderId == userId }

            val rideCandidates = if (firstRide != null) {
                repository.journeyOffers.value.map { offer ->
                    RouteMatchingEngine.evaluateRideMatch(firstRide, offer)
                }.sortedByDescending { it.compatibilityScore }
            } else emptyList()

            val parcelCandidates = if (firstParcel != null) {
                repository.journeyOffers.value.map { offer ->
                    RouteMatchingEngine.evaluateParcelMatch(firstParcel, offer)
                }.sortedByDescending { it.compatibilityScore }
            } else emptyList()

            _uiState.update {
                it.copy(
                    activeRideMatches = rideCandidates,
                    activeParcelMatches = parcelCandidates,
                    selectedMatchCandidate = it.selectedMatchCandidate ?: rideCandidates.firstOrNull()
                )
            }
        }
    }

    fun requestBookingFromMatch(candidate: MatchCandidate, seats: Int, weightKg: Double) {
        val newBooking = repository.createBooking(candidate, seats, weightKg)
        closeBookingDialog()
        showSnackbar("Booking request sent to ${candidate.journeyOffer.driverName}!")
        navigateTo(ScreenDestination.BOOKINGS)
    }

    fun updateBookingStatus(bookingId: String, newStatus: BookingStatus) {
        repository.updateBookingStatus(bookingId, newStatus)
        showSnackbar("Booking updated to ${newStatus.label}")
    }

    fun switchUser(userId: String) {
        repository.switchUser(userId)
        showSnackbar("Switched user to ${repository.currentUser.value.name}")
        refreshMatches()
    }

    fun switchRole(role: UserRole) {
        repository.switchRole(role)
        showSnackbar("Switched active persona to ${role.label}")
    }

    fun postRideRequest(
        originName: String,
        destName: String,
        seats: Int,
        budget: Double,
        notes: String,
        isReturnTrip: Boolean
    ) {
        val origin = IndiaLocations.resolve(originName)
        val destination = IndiaLocations.resolve(destName)
        if (origin == null || destination == null) {
            showSnackbar("Choose valid Indian pickup and destination locations.")
            return
        }
        val newReq = RideRequest(
            passengerId = currentUser.value.id,
            passengerName = currentUser.value.name,
            passengerRating = currentUser.value.rating,
            origin = origin,
            destination = destination,
            preferredDate = "Tomorrow",
            earliestDepartureTime = "08:00 AM",
            latestDepartureTime = "06:00 PM",
            requiredSeats = seats,
            maxBudgetUsd = budget,
            notes = notes,
            isReturnTripRequest = isReturnTrip
        )
        repository.postRideRequest(newReq)
        closeCreateRideDialog()
        refreshMatches()
        showSnackbar("Ride request posted! Matching engine is scanning corridors...")
    }

    fun postParcelRequest(
        pickupName: String,
        destName: String,
        category: ParcelCategory,
        weightKg: Double,
        deadline: String,
        budget: Double,
        handling: String
    ) {
        val pickup = IndiaLocations.resolve(pickupName)
        val destination = IndiaLocations.resolve(destName)
        if (pickup == null || destination == null) {
            showSnackbar("Choose valid Indian pickup and destination locations.")
            return
        }
        val newReq = ParcelRequest(
            senderId = currentUser.value.id,
            senderName = currentUser.value.name,
            pickup = pickup,
            destination = destination,
            category = category,
            weightKg = weightKg,
            preferredDate = "Tomorrow",
            deliveryDeadline = deadline,
            maxBudgetUsd = budget,
            handlingInstructions = handling
        )
        repository.postParcelRequest(newReq)
        closeCreateParcelDialog()
        refreshMatches()
        showSnackbar("Parcel request published! Checking planned return trips...")
    }

    fun postJourneyOffer(
        originName: String,
        destName: String,
        departureTime: String,
        seats: Int,
        parcelKg: Double,
        maxDetourKm: Double,
        pricePerSeat: Double,
        pricePerKg: Double,
        isReturnTrip: Boolean,
        savingsPercent: Int
    ) {
        val origin = IndiaLocations.resolve(originName)
        val destination = IndiaLocations.resolve(destName)
        if (origin == null || destination == null) {
            showSnackbar("Choose valid Indian origin and destination locations.")
            return
        }
        val vehicle = repository.vehicles.value.firstOrNull()
        val offer = JourneyOffer(
            driverId = currentUser.value.id,
            driverName = currentUser.value.name,
            driverRating = currentUser.value.rating,
            vehicleId = vehicle?.id ?: "veh_tesla_y",
            vehicleDescription = vehicle?.let { "${it.make} ${it.model}" } ?: "Tesla Model Y",
            origin = origin,
            destination = destination,
            departureTime = departureTime,
            estimatedArrivalTime = "Scheduled",
            totalDistanceKm = 280.0,
            totalAvailableSeats = seats,
            remainingSeats = seats,
            totalParcelCapacityKg = parcelKg,
            remainingParcelCapacityKg = parcelKg,
            maxDetourKm = maxDetourKm,
            maxDetourMinutes = (maxDetourKm * 1.3).toInt(),
            basePricePerSeatUsd = pricePerSeat,
            basePricePerKgUsd = pricePerKg,
            isReturnTrip = isReturnTrip,
            returnTripSavingsPercent = savingsPercent,
            acceptsPassengers = seats > 0,
            acceptsParcels = parcelKg > 0
        )
        repository.postJourneyOffer(offer)
        closeCreateOfferDialog()
        refreshMatches()
        showSnackbar("Journey offer published! Corridor matching is active.")
    }

    fun addVehicle(
        make: String,
        model: String,
        year: Int,
        plate: String,
        category: VehicleCategory,
        seats: Int,
        parcelKg: Double
    ) {
        val vehicle = Vehicle(
            driverId = currentUser.value.id,
            make = make,
            model = model,
            year = year,
            licensePlate = plate,
            category = category,
            totalSeats = seats + 1,
            availableSeats = seats,
            maxParcelWeightKg = parcelKg,
            maxParcelVolumeLiters = parcelKg * 5,
            supportedCategories = ParcelCategory.entries.toList(),
            isActive = true
        )
        repository.addVehicle(vehicle)
        closeCreateVehicleDialog()
        showSnackbar("Vehicle added successfully!")
    }

    fun submitReview(rating: Int, comment: String) {
        val booking = _uiState.value.selectedBookingForReview ?: return
        val review = Review(
            bookingId = booking.id,
            reviewerId = currentUser.value.id,
            reviewerName = currentUser.value.name,
            revieweeId = booking.driverId,
            rating = rating,
            comment = comment
        )
        repository.submitReview(review)
        closeReviewDialog()
        showSnackbar("Review submitted. Thank you for your feedback! ⭐")
    }

    fun submitReport(category: String, subject: String, description: String) {
        val report = IssueReport(
            reporterId = currentUser.value.id,
            reporterName = currentUser.value.name,
            category = category,
            subject = subject,
            description = description
        )
        repository.submitReport(report)
        closeReportDialog()
        showSnackbar("Report submitted. Our safety team has received your ticket.")
    }

    fun resolveReport(reportId: String, notes: String) {
        repository.resolveReport(reportId, notes)
        showSnackbar("Issue report marked as resolved.")
    }

    fun markNotificationRead(notifId: String) = repository.markNotificationAsRead(notifId)
    fun markAllNotificationsRead() = repository.markAllNotificationsRead()

    fun createTask(
        title: String,
        description: String,
        projectName: String,
        priority: String,
        assignedTo: String
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                description = description,
                projectName = projectName,
                priority = priority,
                status = "TODO",
                assignedTo = assignedTo
            )
            taskRepository.insertTask(task)
            showSnackbar("Task saved to Room DB! 💾")
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
            showSnackbar("Task removed from local DB")
        }
    }

    private fun showSnackbar(msg: String) {
        _uiState.update { it.copy(snackbarMessage = msg) }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReturnRouteViewModel(application) as T
                }
            }
    }
}
