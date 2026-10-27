package com.example.data.repository

import com.example.data.model.MatchType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class ReturnRouteRepositoryBookingTest {
    @Test
    fun passengerBookingConsumesSeatsButNotParcelCapacity() {
        val repository = ReturnRouteRepository()
        val offer = repository.journeyOffers.value.first { it.id == "offer_blr_maa_return" }
        val originalParcelCapacity = offer.remainingParcelCapacityKg

        val booking = repository.createBooking(
            offer = offer,
            requestId = "ride-request-test",
            type = MatchType.PASSENGER_RIDE,
            seats = 1,
            weightKg = 2.5,
            priceUsd = 550.0
        )

        assertEquals(1, booking.seatsBooked)
        assertEquals(0.0, booking.parcelWeightKg, 0.0)
        val updatedOffer = repository.journeyOffers.value.first { it.id == offer.id }
        assertEquals(offer.remainingSeats - 1, updatedOffer.remainingSeats)
        assertEquals(originalParcelCapacity, updatedOffer.remainingParcelCapacityKg, 0.0)
    }

    @Test
    fun parcelBookingConsumesCargoCapacityButNotSeats() {
        val repository = ReturnRouteRepository()
        val offer = repository.journeyOffers.value.first { it.id == "offer_mum_pun_return" }

        val booking = repository.createBooking(
            offer = offer,
            requestId = "parcel-request-test",
            type = MatchType.PARCEL_DELIVERY,
            seats = 1,
            weightKg = 3.5,
            priceUsd = 28.0
        )

        assertEquals(0, booking.seatsBooked)
        assertEquals(3.5, booking.parcelWeightKg, 0.0)
        val updatedOffer = repository.journeyOffers.value.first { it.id == offer.id }
        assertEquals(offer.remainingSeats, updatedOffer.remainingSeats)
        assertEquals(offer.remainingParcelCapacityKg - 3.5, updatedOffer.remainingParcelCapacityKg, 0.0)
    }

    @Test
    fun rejectsBookingThatExceedsCurrentSeatCapacity() {
        val repository = ReturnRouteRepository()
        val offer = repository.journeyOffers.value.first { it.id == "offer_blr_maa_return" }

        assertThrows(IllegalStateException::class.java) {
            repository.createBooking(
                offer = offer,
                requestId = "oversized-ride-test",
                type = MatchType.PASSENGER_RIDE,
                seats = offer.remainingSeats + 1,
                weightKg = 0.0,
                priceUsd = 550.0
            )
        }
    }

    @Test
    fun concurrentLocalBookingsCannotOversubscribeSeats() {
        val repository = ReturnRouteRepository()
        val offer = repository.journeyOffers.value.first { it.id == "offer_blr_maa_return" }
        val start = CountDownLatch(1)
        val successfulBookings = AtomicInteger(0)
        val executor = Executors.newFixedThreadPool(8)

        try {
            val attempts = (1..12).map { index ->
                executor.submit {
                    start.await()
                    try {
                        repository.createBooking(
                            offer = offer,
                            requestId = "concurrent-ride-$index",
                            type = MatchType.PASSENGER_RIDE,
                            seats = 1,
                            weightKg = 0.0,
                            priceUsd = 550.0
                        )
                        successfulBookings.incrementAndGet()
                    } catch (_: IllegalStateException) {
                        // Capacity has already been claimed by another attempt.
                    }
                }
            }
            start.countDown()
            attempts.forEach { it.get() }

            assertEquals(offer.remainingSeats, successfulBookings.get())
            assertEquals(0, repository.journeyOffers.value.first { it.id == offer.id }.remainingSeats)
        } finally {
            executor.shutdownNow()
        }
    }
}
