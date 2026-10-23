package com.example.engine

import com.example.data.model.*
import kotlin.math.*

object RouteMatchingEngine {

    private const val EARTH_RADIUS_KM = 6371.0
    private const val DEFAULT_MAX_CORRIDOR_DISTANCE_KM = 35.0

    /**
     * Calculates great-circle distance between two coordinates in kilometers using Haversine formula.
     */
    fun calculateHaversineDistanceKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Determines whether a point lies along the corridor connecting origin, waypoints, and destination.
     */
    fun minDistanceToJourneyRouteKm(
        pointLat: Double, pointLon: Double,
        journey: JourneyOffer
    ): Double {
        val routeNodes = mutableListOf<GeoLocation>()
        routeNodes.add(journey.origin)
        routeNodes.addAll(journey.waypoints)
        routeNodes.add(journey.destination)

        var minDistance = Double.MAX_VALUE
        for (i in 0 until routeNodes.size - 1) {
            val distToSegment = distanceToSegmentKm(
                pointLat, pointLon,
                routeNodes[i].latitude, routeNodes[i].longitude,
                routeNodes[i + 1].latitude, routeNodes[i + 1].longitude
            )
            if (distToSegment < minDistance) {
                minDistance = distToSegment
            }
        }
        return minDistance
    }

    private fun distanceToSegmentKm(
        pLat: Double, pLon: Double,
        aLat: Double, aLon: Double,
        bLat: Double, bLon: Double
    ): Double {
        val distA = calculateHaversineDistanceKm(pLat, pLon, aLat, aLon)
        val distB = calculateHaversineDistanceKm(pLat, pLon, bLat, bLon)
        val segmentLen = calculateHaversineDistanceKm(aLat, aLon, bLat, bLon)

        if (segmentLen < 0.1) return distA

        val crossDist = (distA + distB) - segmentLen
        val perpEstimate = sqrt(max(0.0, distA.pow(2) - (segmentLen / 2).pow(2)))
        return min(min(distA, distB), max(crossDist * 1.5, perpEstimate.coerceAtMost(min(distA, distB))))
    }

    /**
     * Evaluates a Passenger Ride request against a driver journey offer.
     */
    fun evaluateRideMatch(
        request: RideRequest,
        offer: JourneyOffer
    ): MatchCandidate {
        val reasons = mutableListOf<String>()
        val rejectionReasons = mutableListOf<String>()

        val pickupProximity = minDistanceToJourneyRouteKm(request.origin.latitude, request.origin.longitude, offer)
        val dropoffProximity = minDistanceToJourneyRouteKm(request.destination.latitude, request.destination.longitude, offer)
        val totalDetourKm = pickupProximity + dropoffProximity
        val detourMinutes = (totalDetourKm * 1.4).toInt()

        if (!offer.acceptsPassengers) {
            rejectionReasons.add("Driver has disabled passenger pooling for this trip.")
        }
        if (offer.remainingSeats < request.requiredSeats) {
            rejectionReasons.add("Insufficient seats (needs ${request.requiredSeats}, offer has ${offer.remainingSeats}).")
        }
        if (totalDetourKm > offer.maxDetourKm) {
            rejectionReasons.add("Detour of ${"%.1f".format(totalDetourKm)} km exceeds driver's max tolerance of ${offer.maxDetourKm} km.")
        }
        if (totalDetourKm > request.maxDetourToleranceKm) {
            rejectionReasons.add("Detour of ${"%.1f".format(totalDetourKm)} km exceeds passenger's limit of ${request.maxDetourToleranceKm} km.")
        }

        val priceBreakdown = PricingEngine.calculatePassengerFare(
            baseSeatFare = offer.basePricePerSeatUsd,
            seats = request.requiredSeats,
            detourKm = totalDetourKm,
            isReturnTrip = offer.isReturnTrip,
            discountPercent = offer.returnTripSavingsPercent
        )

        if (priceBreakdown.finalTotalUsd > request.maxBudgetUsd) {
            rejectionReasons.add("Estimated price ($${priceBreakdown.finalTotalUsd}) exceeds passenger budget ($${request.maxBudgetUsd}).")
        }

        var score = 100
        val detourPenalty = (totalDetourKm * 2.2).toInt()
        score -= detourPenalty.coerceAtMost(35)

        if (offer.isReturnTrip) {
            score += 15
            reasons.add("🎉 Return-Trip Opportunity: ${offer.returnTripSavingsPercent}% direct route discount applied!")
        }
        if (pickupProximity < 4.0) {
            score += 10
            reasons.add("🎯 Direct corridor pickup: within ${"%.1f".format(pickupProximity)} km of primary route.")
        } else {
            reasons.add("Pickup requires ${"%.1f".format(pickupProximity)} km route deviation.")
        }
        if (dropoffProximity < 4.0) {
            score += 10
            reasons.add("🏁 Direct corridor dropoff: within ${"%.1f".format(dropoffProximity)} km of destination.")
        }
        if (offer.driverRating >= 4.9) {
            score += 5
            reasons.add("⭐ Top-rated verified driver (${offer.driverRating}★)")
        }

        val finalScore = score.coerceIn(10, 99)
        val isEligible = rejectionReasons.isEmpty()

        return MatchCandidate(
            requestId = request.id,
            journeyOffer = offer,
            matchType = MatchType.PASSENGER_RIDE,
            compatibilityScore = if (isEligible) finalScore else (finalScore / 2).coerceAtLeast(15),
            isEligible = isEligible,
            detourDistanceKm = round(totalDetourKm * 10) / 10.0,
            detourDurationMinutes = detourMinutes,
            pickupProximityKm = round(pickupProximity * 10) / 10.0,
            dropoffProximityKm = round(dropoffProximity * 10) / 10.0,
            estimatedCostUsd = priceBreakdown.finalTotalUsd,
            reasons = reasons,
            rejectionReasons = rejectionReasons
        )
    }

    /**
     * Evaluates a Parcel Delivery request against a driver journey offer.
     */
    fun evaluateParcelMatch(
        request: ParcelRequest,
        offer: JourneyOffer
    ): MatchCandidate {
        val reasons = mutableListOf<String>()
        val rejectionReasons = mutableListOf<String>()

        val pickupProximity = minDistanceToJourneyRouteKm(request.pickup.latitude, request.pickup.longitude, offer)
        val dropoffProximity = minDistanceToJourneyRouteKm(request.destination.latitude, request.destination.longitude, offer)
        val totalDetourKm = pickupProximity + dropoffProximity
        val detourMinutes = (totalDetourKm * 1.3).toInt()

        if (!offer.acceptsParcels) {
            rejectionReasons.add("Driver does not accept cargo/parcels for this route.")
        }
        if (offer.remainingParcelCapacityKg < request.weightKg) {
            rejectionReasons.add("Payload weight (${request.weightKg} kg) exceeds remaining capacity (${offer.remainingParcelCapacityKg} kg).")
        }
        if (totalDetourKm > offer.maxDetourKm) {
            rejectionReasons.add("Detour of ${"%.1f".format(totalDetourKm)} km exceeds driver max tolerance of ${offer.maxDetourKm} km.")
        }

        val priceBreakdown = PricingEngine.calculateParcelFare(
            basePerKg = offer.basePricePerKgUsd,
            weightKg = request.weightKg,
            category = request.category,
            detourKm = totalDetourKm,
            isReturnTrip = offer.isReturnTrip,
            discountPercent = offer.returnTripSavingsPercent
        )

        if (priceBreakdown.finalTotalUsd > request.maxBudgetUsd) {
            rejectionReasons.add("Delivery cost ($${priceBreakdown.finalTotalUsd}) exceeds sender budget ($${request.maxBudgetUsd}).")
        }

        var score = 95
        val detourPenalty = (totalDetourKm * 2.0).toInt()
        score -= detourPenalty.coerceAtMost(30)

        if (offer.isReturnTrip) {
            score += 15
            reasons.add("📦 Return-Trip Cargo Optimization: 20-30% transport discount utilized.")
        }
        if (pickupProximity < 5.0) {
            reasons.add("Convenient doorstep/curbside cargo transfer along path.")
        }
        if (request.weightKg < 10.0) {
            score += 5
            reasons.add("Lightweight parcel: optimal fit for vehicle trunk space.")
        }

        val finalScore = score.coerceIn(10, 99)
        val isEligible = rejectionReasons.isEmpty()

        return MatchCandidate(
            requestId = request.id,
            journeyOffer = offer,
            matchType = MatchType.PARCEL_DELIVERY,
            compatibilityScore = if (isEligible) finalScore else (finalScore / 2).coerceAtLeast(15),
            isEligible = isEligible,
            detourDistanceKm = round(totalDetourKm * 10) / 10.0,
            detourDurationMinutes = detourMinutes,
            pickupProximityKm = round(pickupProximity * 10) / 10.0,
            dropoffProximityKm = round(dropoffProximity * 10) / 10.0,
            estimatedCostUsd = priceBreakdown.finalTotalUsd,
            reasons = reasons,
            rejectionReasons = rejectionReasons
        )
    }
}
