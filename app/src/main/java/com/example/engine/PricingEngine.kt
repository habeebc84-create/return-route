package com.example.engine

import com.example.data.model.ParcelCategory
import kotlin.math.roundToInt

data class PriceBreakdown(
    val baseFareUsd: Double,
    val distanceFareUsd: Double,
    val detourCompensationUsd: Double,
    val handlingFeeUsd: Double,
    val returnTripDiscountUsd: Double,
    val platformFeeUsd: Double,
    val finalTotalUsd: Double,
    val driverPayoutUsd: Double
)

object PricingEngine {

    private const val DETOUR_RATE_PER_KM = 0.65
    private const val PLATFORM_FEE_PERCENT = 0.10 // 10% marketplace commission

    fun calculatePassengerFare(
        baseSeatFare: Double,
        seats: Int,
        detourKm: Double,
        isReturnTrip: Boolean,
        discountPercent: Int
    ): PriceBreakdown {
        val base = baseSeatFare * seats
        val detourCost = detourKm * DETOUR_RATE_PER_KM
        val subtotal = base + detourCost

        val returnDiscount = if (isReturnTrip) {
            val pct = if (discountPercent > 0) discountPercent else 25
            subtotal * (pct / 100.0)
        } else 0.0

        val discountedSubtotal = subtotal - returnDiscount
        val platformFee = discountedSubtotal * PLATFORM_FEE_PERCENT
        val total = discountedSubtotal + platformFee
        val driverPayout = discountedSubtotal * 0.90 + detourCost

        return PriceBreakdown(
            baseFareUsd = roundTwoDecimals(base),
            distanceFareUsd = roundTwoDecimals(base * 0.7),
            detourCompensationUsd = roundTwoDecimals(detourCost),
            handlingFeeUsd = 0.0,
            returnTripDiscountUsd = roundTwoDecimals(returnDiscount),
            platformFeeUsd = roundTwoDecimals(platformFee),
            finalTotalUsd = roundTwoDecimals(total),
            driverPayoutUsd = roundTwoDecimals(driverPayout)
        )
    }

    fun calculateParcelFare(
        basePerKg: Double,
        weightKg: Double,
        category: ParcelCategory,
        detourKm: Double,
        isReturnTrip: Boolean,
        discountPercent: Int
    ): PriceBreakdown {
        val base = (basePerKg * weightKg).coerceAtLeast(8.0)
        val categoryHandling = when (category) {
            ParcelCategory.DOCUMENTS -> 2.50
            ParcelCategory.SMALL_ELECTRONICS -> 6.00
            ParcelCategory.FRAGILE_ITEMS -> 7.50
            ParcelCategory.CLOTHING_GOODS -> 3.00
            ParcelCategory.GENERAL_CARGO -> 4.00
        }
        val detourCost = detourKm * DETOUR_RATE_PER_KM
        val subtotal = base + categoryHandling + detourCost

        val returnDiscount = if (isReturnTrip) {
            val pct = if (discountPercent > 0) discountPercent else 20
            subtotal * (pct / 100.0)
        } else 0.0

        val discountedSubtotal = subtotal - returnDiscount
        val platformFee = discountedSubtotal * PLATFORM_FEE_PERCENT
        val total = discountedSubtotal + platformFee
        val driverPayout = discountedSubtotal * 0.88 + detourCost

        return PriceBreakdown(
            baseFareUsd = roundTwoDecimals(base),
            distanceFareUsd = roundTwoDecimals(base * 0.6),
            detourCompensationUsd = roundTwoDecimals(detourCost),
            handlingFeeUsd = roundTwoDecimals(categoryHandling),
            returnTripDiscountUsd = roundTwoDecimals(returnDiscount),
            platformFeeUsd = roundTwoDecimals(platformFee),
            finalTotalUsd = roundTwoDecimals(total),
            driverPayoutUsd = roundTwoDecimals(driverPayout)
        )
    }

    private fun roundTwoDecimals(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }
}
