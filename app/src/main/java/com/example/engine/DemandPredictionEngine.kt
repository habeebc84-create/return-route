package com.example.engine

import com.example.data.model.DemandForecast

object DemandPredictionEngine {

    private val CORRIDOR_BASELINES = listOf(
        DemandForecast(
            corridorName = "Bengaluru ⇄ Chennai Express Corridor",
            originRegion = "Bengaluru (Electronic City)",
            destRegion = "Chennai (Koyambedu / OMR)",
            passengerDemandIndex = 96,
            parcelDemandIndex = 91,
            returnTripOpportunityScore = 98,
            peakHourRecommendation = "Fridays 4:00 PM – 9:00 PM & Sundays 3:00 PM – 10:00 PM",
            trend = "+28% weekend return surge; EV charging stations active along Hosur"
        ),
        DemandForecast(
            corridorName = "Mumbai ⇄ Pune Expressway",
            originRegion = "Mumbai (Bandra BKC)",
            destRegion = "Pune (Hinjewadi IT Park)",
            passengerDemandIndex = 98,
            parcelDemandIndex = 94,
            returnTripOpportunityScore = 99,
            peakHourRecommendation = "Daily 07:00 AM – 10:30 AM & 05:30 PM – 09:30 PM",
            trend = "+35% daily IT commuter & prototype electronics courier volume"
        ),
        DemandForecast(
            corridorName = "Delhi ⇄ Jaipur Pink City Highway",
            originRegion = "New Delhi (Central)",
            destRegion = "Jaipur (Sindhi Camp)",
            passengerDemandIndex = 92,
            parcelDemandIndex = 86,
            returnTripOpportunityScore = 94,
            peakHourRecommendation = "Saturday early mornings & Sunday evenings",
            trend = "+22% tourist & weekend family pooling on NH-48"
        ),
        DemandForecast(
            corridorName = "Delhi ⇄ Chandigarh Grand Trunk Link",
            originRegion = "Delhi NCR (Kashmere Gate)",
            destRegion = "Chandigarh (Sector 17)",
            passengerDemandIndex = 94,
            parcelDemandIndex = 89,
            returnTripOpportunityScore = 95,
            peakHourRecommendation = "Fridays 3:00 PM – 8:00 PM & Monday mornings",
            trend = "High return capacity: 38 empty vehicle return legs logged today"
        ),
        DemandForecast(
            corridorName = "Hyderabad ⇄ Bengaluru Deccan Spine",
            originRegion = "Hyderabad (Gachibowli / ORR)",
            destRegion = "Bengaluru (Majestic / Manyata)",
            passengerDemandIndex = 90,
            parcelDemandIndex = 85,
            returnTripOpportunityScore = 92,
            peakHourRecommendation = "Thursday night departures & Sunday afternoon returns",
            trend = "+20% inter-city tech employee & corporate cargo movement"
        ),
        DemandForecast(
            corridorName = "Ahmedabad ⇄ Mumbai Western Freight",
            originRegion = "Ahmedabad (SG Highway)",
            destRegion = "Mumbai (Dadar / Vashi)",
            passengerDemandIndex = 88,
            parcelDemandIndex = 96,
            returnTripOpportunityScore = 95,
            peakHourRecommendation = "Daily overnight commercial freight & early morning rides",
            trend = "+30% high-capacity commercial parcel courier utilization"
        ),
        DemandForecast(
            corridorName = "Delhi ⇄ Lucknow Purvanchal Expressway",
            originRegion = "New Delhi (Yamuna Expy)",
            destRegion = "Lucknow (Charbagh / Gomti Nagar)",
            passengerDemandIndex = 91,
            parcelDemandIndex = 87,
            returnTripOpportunityScore = 93,
            peakHourRecommendation = "Holiday seasons, festival weekends & Friday evenings",
            trend = "Fastest average travel time: 6.5 hours across access-controlled expressways"
        ),
        DemandForecast(
            corridorName = "Kochi ⇄ Bengaluru Malabar Transit",
            originRegion = "Kochi (Vyttila Hub)",
            destRegion = "Bengaluru (Electronic City)",
            passengerDemandIndex = 89,
            parcelDemandIndex = 83,
            returnTripOpportunityScore = 91,
            peakHourRecommendation = "Sunday night returns via Coimbatore & Salem",
            trend = "+26% student and IT professional corridor volume"
        )
    )

    fun getForecasts(): List<DemandForecast> = CORRIDOR_BASELINES

    fun getForecastForCorridor(name: String): DemandForecast? {
        return CORRIDOR_BASELINES.find { it.corridorName.contains(name, ignoreCase = true) }
            ?: CORRIDOR_BASELINES.firstOrNull()
    }
}
