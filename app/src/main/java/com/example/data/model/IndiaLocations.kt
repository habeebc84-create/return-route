package com.example.data.model

object IndiaLocations {

    /** Resolve a user-entered city or picker label to a real catalog coordinate. */
    fun resolve(query: String): GeoLocation? {
        val cleaned = query.trim().lowercase()
            .replace("bkc", "bandra kurla complex")
            .replace("omr", "omr")
        if (cleaned.isBlank()) return null
        fun normalize(value: String) = value.lowercase().filter { it.isLetterOrDigit() }
        val target = normalize(cleaned)
        return ALL_LOCATIONS.firstOrNull { location ->
            val name = normalize(location.name)
            val address = normalize(location.address)
            val city = normalize(location.city)
            target == name || target == address || target == city ||
                name.startsWith(target) || target.startsWith(name) || target.contains(name) ||
                (target.contains(city) && (name.contains(target.removePrefix(city)) || address.contains(target.removePrefix(city))))
        } ?: ALL_LOCATIONS.firstOrNull { location ->
            normalize(location.city) == target
        }
    }

    val ALL_LOCATIONS: List<GeoLocation> = listOf(
        // --- NORTH INDIA ---
        GeoLocation("New Delhi (Connaught Place)", "Central Delhi, Rajiv Chowk", 28.6315, 77.2167, "New Delhi", "Delhi NCR"),
        GeoLocation("Delhi Kashmere Gate ISBT", "Ring Road, Kashmere Gate Bus Terminal", 28.6667, 77.2285, "Delhi", "Delhi NCR"),
        GeoLocation("Delhi IGI Airport (T3)", "Indira Gandhi International Airport, Palam", 28.5562, 77.1000, "New Delhi", "Delhi NCR"),
        GeoLocation("Gurgaon (Cyber City / DLF)", "NH-48, Sector 24-29", 28.4900, 77.0900, "Gurgaon", "Haryana"),
        GeoLocation("Gurgaon (IFFCO Chowk / Sohna Rd)", "Mehrauli-Gurgaon Rd", 28.4720, 77.0650, "Gurgaon", "Haryana"),
        GeoLocation("Noida (Sector 18 / Atta Market)", "Noida-Greater Noida Expressway", 28.5700, 77.3200, "Noida", "Uttar Pradesh"),
        GeoLocation("Greater Noida (Pari Chowk)", "Yamuna Expressway Entry", 28.4700, 77.5000, "Greater Noida", "Uttar Pradesh"),
        GeoLocation("Faridabad (Bata Chowk)", "Mathura Road (NH-19)", 28.3800, 77.3100, "Faridabad", "Haryana"),
        GeoLocation("Chandigarh (Sector 17 Plaza)", "Himalaya Marg, City Centre", 30.7333, 76.7794, "Chandigarh", "Punjab / Chandigarh"),
        GeoLocation("Chandigarh ISBT 43", "Sector 43 Bus Stand", 30.7180, 76.7450, "Chandigarh", "Punjab / Chandigarh"),
        GeoLocation("Amritsar (Golden Temple / Hall Gate)", "GT Road / Heritage Street", 31.6340, 74.8723, "Amritsar", "Punjab"),
        GeoLocation("Ludhiana (Clock Tower / GT Rd)", "Sherpur Chowk", 30.9010, 75.8573, "Ludhiana", "Punjab"),
        GeoLocation("Jalandhar (BMC Chowk / Model Town)", "GT Road Bypass", 31.3260, 75.5762, "Jalandhar", "Punjab"),
        GeoLocation("Shimla (The Mall / Ridge)", "Circular Road, ISBT Tutikandi", 31.1048, 77.1734, "Shimla", "Himachal Pradesh"),
        GeoLocation("Manali (Mall Road / Tibetan Market)", "Kullu-Manali Highway (NH-3)", 32.2396, 77.1887, "Manali", "Himachal Pradesh"),
        GeoLocation("Dharamshala (McLeod Ganj)", "Temple Road", 32.2190, 76.3234, "Dharamshala", "Himachal Pradesh"),
        GeoLocation("Srinagar (Lal Chowk / Dal Lake)", "Boulevard Road", 34.0837, 74.7973, "Srinagar", "Jammu & Kashmir"),
        GeoLocation("Jammu (Tawi Railway Station)", "Rail Head Complex", 32.7266, 74.8570, "Jammu", "Jammu & Kashmir"),
        GeoLocation("Dehradun (Clock Tower / Rajpur Rd)", "Haridwar Bypass (NH-7)", 30.3165, 78.0322, "Dehradun", "Uttarakhand"),
        GeoLocation("Haridwar (Har Ki Pauri)", "Upper Road", 29.9457, 78.1642, "Haridwar", "Uttarakhand"),
        GeoLocation("Rishikesh (Laxman Jhula)", "Badrinath Highway", 30.0869, 78.2676, "Rishikesh", "Uttarakhand"),
        GeoLocation("Jaipur (Sindhi Camp ISBT)", "Station Road", 26.9124, 75.7873, "Jaipur", "Rajasthan"),
        GeoLocation("Jaipur (Malviya Nagar / Airport)", "Jawahar Circle, Tonk Road", 26.8500, 75.8050, "Jaipur", "Rajasthan"),
        GeoLocation("Udaipur (City Palace / Sukhadia Circle)", "Fateh Sagar Lake Road", 24.5854, 73.7125, "Udaipur", "Rajasthan"),
        GeoLocation("Jodhpur (Clock Tower / Ratanada)", "High Court Road", 26.2389, 73.0243, "Jodhpur", "Rajasthan"),
        GeoLocation("Ajmer (Dargah Sharif)", "Prithviraj Marg", 26.4499, 74.6399, "Ajmer", "Rajasthan"),
        GeoLocation("Kota (Aerodrome Circle)", "Jhalawar Road", 25.1800, 75.8300, "Kota", "Rajasthan"),
        GeoLocation("Agra (Tajganj / Fatehabad Road)", "Yamuna Expressway Link", 27.1767, 78.0081, "Agra", "Uttar Pradesh"),
        GeoLocation("Mathura (Krishna Janmabhoomi)", "Delhi-Agra Highway (NH-19)", 27.4924, 77.6737, "Mathura", "Uttar Pradesh"),
        GeoLocation("Lucknow (Charbagh Railway Station)", "Station Road", 26.8322, 80.9238, "Lucknow", "Uttar Pradesh"),
        GeoLocation("Lucknow (Hazratganj / Gomti Nagar)", "Shaheed Path / Lohia Path", 26.8500, 80.9900, "Lucknow", "Uttar Pradesh"),
        GeoLocation("Kanpur (Mall Road / GT Road)", "Civil Lines, Central Station", 26.4499, 80.3319, "Kanpur", "Uttar Pradesh"),
        GeoLocation("Varanasi (Cantonment / Godowlia)", "Varanasi Cantt Station", 25.3176, 82.9739, "Varanasi", "Uttar Pradesh"),
        GeoLocation("Prayagraj (Civil Lines / Sangam)", "MG Marg", 25.4358, 81.8463, "Prayagraj", "Uttar Pradesh"),
        GeoLocation("Meerut (Begum Bridge)", "Delhi-Meerut Expressway", 28.9845, 77.7064, "Meerut", "Uttar Pradesh"),

        // --- WEST INDIA ---
        GeoLocation("Mumbai (Bandra Kurla Complex)", "BKC, Bandra East", 19.0607, 72.8656, "Mumbai", "Maharashtra"),
        GeoLocation("Mumbai (Dadar TT / CSMT)", "Eastern Express Highway, Dadar", 19.0178, 72.8478, "Mumbai", "Maharashtra"),
        GeoLocation("Mumbai (Andheri / Western Express)", "Sahar Airport Road, Andheri", 19.1136, 72.8697, "Mumbai", "Maharashtra"),
        GeoLocation("Navi Mumbai (Vashi / Belapur)", "Sion-Panvel Expressway", 19.0330, 73.0297, "Navi Mumbai", "Maharashtra"),
        GeoLocation("Thane (Teen Hath Naka)", "Eastern Express Highway", 19.2183, 72.9781, "Thane", "Maharashtra"),
        GeoLocation("Pune (Shivajinagar / Swargate)", "FC Road / JM Road", 18.5204, 73.8567, "Pune", "Maharashtra"),
        GeoLocation("Pune (Hinjewadi IT Park / Wakad)", "Mumbai-Pune Expressway Bypass", 18.5913, 73.7389, "Pune", "Maharashtra"),
        GeoLocation("Pune (Viman Nagar / Kharadi)", "Ahmednagar Highway (NH-753F)", 18.5679, 73.9143, "Pune", "Maharashtra"),
        GeoLocation("Nashik (Dwarka Circle)", "Mumbai-Agra Highway (NH-3)", 19.9975, 73.7898, "Nashik", "Maharashtra"),
        GeoLocation("Chhatrapati Sambhajinagar (Aurangabad)", "Jalna Road", 19.8762, 75.3433, "Aurangabad", "Maharashtra"),
        GeoLocation("Nagpur (Zero Mile / Sitabuldi)", "Wardha Road (NH-44)", 21.1458, 79.0882, "Nagpur", "Maharashtra"),
        GeoLocation("Kolhapur (CBS Bus Stand)", "Pune-Bangalore Highway (NH-48)", 16.7050, 74.2433, "Kolhapur", "Maharashtra"),
        GeoLocation("Solapur (Saat Rasta)", "Pune-Solapur Highway", 17.6599, 75.9064, "Solapur", "Maharashtra"),
        GeoLocation("Ahmedabad (SG Highway / Prahlad Nagar)", "Sarkhej-Gandhinagar Highway", 23.0225, 72.5714, "Ahmedabad", "Gujarat"),
        GeoLocation("Ahmedabad (Kalupur / Ashram Rd)", "Relief Road", 23.0300, 72.5800, "Ahmedabad", "Gujarat"),
        GeoLocation("Gandhinagar (Infocity / Sector 11)", "GH Road, CH Road", 23.2156, 72.6369, "Gandhinagar", "Gujarat"),
        GeoLocation("Surat (Ring Road / Athwa Gate)", "Udhana-Magdalla Road", 21.1702, 72.8311, "Surat", "Gujarat"),
        GeoLocation("Vadodara (Sayajigunj / Alkapuri)", "RC Dutt Road", 22.3072, 73.1812, "Vadodara", "Gujarat"),
        GeoLocation("Rajkot (Trikon Baug / Kalawad Rd)", "150 Feet Ring Road", 22.3039, 70.8022, "Rajkot", "Gujarat"),
        GeoLocation("Goa (Panaji / Miramar Beach)", "Dayanand Bandodkar Marg", 15.4909, 73.8278, "Panaji", "Goa"),
        GeoLocation("Goa (Madgaon Railway Station)", "Margao Central Bus Stand", 15.2700, 73.9500, "Margao", "Goa"),

        // --- SOUTH INDIA ---
        GeoLocation("Bengaluru (Kempegowda / Majestic)", "Tank Bund Road, Central Railway", 12.9767, 77.5713, "Bengaluru", "Karnataka"),
        GeoLocation("Bengaluru (Koramangala / Indiranagar)", "100 Feet Road / Intermediate Ring Rd", 12.9352, 77.6245, "Bengaluru", "Karnataka"),
        GeoLocation("Bengaluru (Electronic City Toll)", "Hosur Road (NH-44 Elevated)", 12.8399, 77.6770, "Bengaluru", "Karnataka"),
        GeoLocation("Bengaluru (Whitefield / ITPL)", "Whitefield Main Road", 12.9698, 77.7500, "Bengaluru", "Karnataka"),
        GeoLocation("Bengaluru (KIA Airport / Hebbal)", "Bellary Road (NH-44)", 13.1986, 77.7066, "Bengaluru", "Karnataka"),
        GeoLocation("Mysuru (Suburban Bus Stand)", "Bengaluru-Nilgiri Road (NH-275)", 12.2958, 76.6394, "Mysuru", "Karnataka"),
        GeoLocation("Mangaluru (State Bank / Hampankatta)", "KS Rao Road (NH-66)", 12.9141, 74.8560, "Mangaluru", "Karnataka"),
        GeoLocation("Hubballi (CBT / Chennamma Circle)", "Pune-Bengaluru Highway", 15.3647, 75.1240, "Hubballi", "Karnataka"),
        GeoLocation("Belagavi (Central Bus Stand)", "NH-48 Corridor", 15.8497, 74.4977, "Belagavi", "Karnataka"),
        GeoLocation("Chennai (Koyambedu / CMBT)", "Jawaharlal Nehru Road (Inner Ring Rd)", 13.0694, 80.1948, "Chennai", "Tamil Nadu"),
        GeoLocation("Chennai (OMR / Guindy)", "Old Mahabalipuram Road (IT Corridor)", 13.0067, 80.2025, "Chennai", "Tamil Nadu"),
        GeoLocation("Chennai (Central Railway / Egmore)", "EVR Periyar Salai", 13.0827, 80.2707, "Chennai", "Tamil Nadu"),
        GeoLocation("Coimbatore (Gandhipuram / Avinashi Rd)", "Cross Cut Road", 11.0168, 76.9558, "Coimbatore", "Tamil Nadu"),
        GeoLocation("Madurai (Periyar Bus Stand / Mattuthavani)", "West Veli Street", 9.9252, 78.1198, "Madurai", "Tamil Nadu"),
        GeoLocation("Tiruchirappalli (Central Bus Stand)", "Collector Office Road", 10.7905, 78.7047, "Tiruchirappalli", "Tamil Nadu"),
        GeoLocation("Salem (New Bus Stand)", "NH-44 Junction", 11.6643, 78.1460, "Salem", "Tamil Nadu"),
        GeoLocation("Tiruppur (Old Bus Stand)", "Kumaran Road", 11.1085, 77.3411, "Tiruppur", "Tamil Nadu"),
        GeoLocation("Vellore (Green Circle)", "NH-48 Chennai-Bengaluru Highway", 12.9165, 79.1325, "Vellore", "Tamil Nadu"),
        GeoLocation("Hyderabad (Hitec City / Madhapur)", "Cyber Towers Circle", 17.4474, 78.3762, "Hyderabad", "Telangana"),
        GeoLocation("Hyderabad (Secunderabad / Begumpet)", "SP Road", 17.4399, 78.4983, "Hyderabad", "Telangana"),
        GeoLocation("Hyderabad (Gachibowli / ORR)", "Outer Ring Road (NH-44 Exit)", 17.4401, 78.3489, "Hyderabad", "Telangana"),
        GeoLocation("Hyderabad (MGBS Imlibun)", "Chaderghat, Central Bus Station", 17.3780, 78.4840, "Hyderabad", "Telangana"),
        GeoLocation("Warangal (Hanamkonda Bus Stand)", "Kazipet Road", 17.9689, 79.5941, "Warangal", "Telangana"),
        GeoLocation("Vijayawada (Pandit Nehru PNBS)", "Bandar Road, Krishna River Bank", 16.5062, 80.6480, "Vijayawada", "Andhra Pradesh"),
        GeoLocation("Visakhapatnam (RTC Complex / Beach Rd)", "Dwaraka Nagar (NH-16)", 17.6868, 83.2185, "Visakhapatnam", "Andhra Pradesh"),
        GeoLocation("Tirupati (Central Bus Stand)", "Alipiri Bypass Road", 13.6288, 79.4192, "Tirupati", "Andhra Pradesh"),
        GeoLocation("Guntur (NTR Bus Station)", "GT Road (NH-16)", 16.3067, 80.4365, "Guntur", "Andhra Pradesh"),
        GeoLocation("Kochi (Vyttila Mobility Hub)", "SA Road / NH-66 Bypass", 9.9678, 76.3214, "Kochi", "Kerala"),
        GeoLocation("Kochi (Ernakulam South / MG Road)", "Railway Station Road", 9.9816, 76.2999, "Kochi", "Kerala"),
        GeoLocation("Thiruvananthapuram (Thampanoor)", "Central Railway & KSRTC Station", 8.5241, 76.9366, "Thiruvananthapuram", "Kerala"),
        GeoLocation("Kozhikode (Mofussil Bus Stand)", "Mavoor Road (NH-66)", 11.2588, 75.7804, "Kozhikode", "Kerala"),
        GeoLocation("Thrissur (Shaktan Thampuran Stand)", "Round South", 10.5276, 76.2144, "Thrissur", "Kerala"),
        GeoLocation("Kannur (Caltex Junction)", "Thavakkara Road", 11.8745, 75.3704, "Kannur", "Kerala"),

        // --- EAST & NORTH-EAST INDIA ---
        GeoLocation("Kolkata (Park Street / Esplanade)", "Chowringhee Road, Central Kolkata", 22.5555, 88.3510, "Kolkata", "West Bengal"),
        GeoLocation("Kolkata (Howrah Railway Station)", "Station Road, Howrah Bridge", 22.5850, 88.3426, "Kolkata", "West Bengal"),
        GeoLocation("Kolkata (Salt Lake Sector V / New Town)", "Major Arterial Road", 22.5800, 88.4500, "Kolkata", "West Bengal"),
        GeoLocation("Siliguri (Tenzing Norgay Bus Terminus)", "Hill Cart Road (NH-10)", 26.7271, 88.3953, "Siliguri", "West Bengal"),
        GeoLocation("Durgapur (City Centre)", "NH-19 Grand Trunk Road", 23.5204, 87.3119, "Durgapur", "West Bengal"),
        GeoLocation("Asansol (Bus Stand / GT Rd)", "NH-19 Highway", 23.6739, 86.9524, "Asansol", "West Bengal"),
        GeoLocation("Patna (Gandhi Maidan / Frazer Rd)", "Dak Bungalow Crossing", 25.5941, 85.1376, "Patna", "Bihar"),
        GeoLocation("Patna (Patliputra / Bailey Road)", "Bailey Road", 25.6100, 85.1100, "Patna", "Bihar"),
        GeoLocation("Gaya (Railway Station)", "Station Road (NH-83)", 24.7955, 85.0002, "Gaya", "Bihar"),
        GeoLocation("Muzaffarpur (Bairia Bus Stand)", "NH-28 Corridor", 26.1209, 85.3647, "Muzaffarpur", "Bihar"),
        GeoLocation("Ranchi (Albert Ekka Chowk)", "Main Road, Overbridge", 23.3441, 85.3096, "Ranchi", "Jharkhand"),
        GeoLocation("Jamshedpur (Bistupur / Sakchi)", "Straight Mile Road", 22.8046, 86.2029, "Jamshedpur", "Jharkhand"),
        GeoLocation("Dhanbad (Bank More / Station)", "NH-19 Link Road", 23.7957, 86.4304, "Dhanbad", "Jharkhand"),
        GeoLocation("Bhubaneswar (Master Canteen / Janpath)", "Janpath Road, Railway Station", 20.2961, 85.8245, "Bhubaneswar", "Odisha"),
        GeoLocation("Cuttack (Badambadi Bus Stand)", "Link Road (NH-16)", 20.4625, 85.8828, "Cuttack", "Odisha"),
        GeoLocation("Puri (Grand Road / Temple)", "VIP Road", 19.8135, 85.8312, "Puri", "Odisha"),
        GeoLocation("Guwahati (Paltan Bazaar / ISBT)", "GS Road / Lokhra ISBT", 26.1445, 91.7362, "Guwahati", "Assam"),
        GeoLocation("Shillong (Police Bazar)", "GS Road", 25.5788, 91.8933, "Shillong", "Meghalaya"),
        GeoLocation("Gangtok (MG Marg)", "National Highway 10", 27.3389, 88.6065, "Gangtok", "Sikkim"),
        GeoLocation("Agartala (Motor Stand)", "Akhaura Road", 23.8315, 91.2868, "Agartala", "Tripura"),

        // --- CENTRAL INDIA ---
        GeoLocation("Bhopal (MP Nagar / ISBT Habibganj)", "Hoshangabad Road", 23.2599, 77.4126, "Bhopal", "Madhya Pradesh"),
        GeoLocation("Indore (Rajwada / Vijay Nagar)", "AB Road (NH-52)", 22.7196, 75.8577, "Indore", "Madhya Pradesh"),
        GeoLocation("Indore (Bhawarkua / Chhoti Gwaltoli)", "BRTS Corridor", 22.6950, 75.8650, "Indore", "Madhya Pradesh"),
        GeoLocation("Gwalior (Morar / City Centre)", "Jhansi Road (NH-44)", 26.2183, 78.1828, "Gwalior", "Madhya Pradesh"),
        GeoLocation("Jabalpur (Civic Centre / Russell Chowk)", "Nagpur Road", 23.1815, 79.9864, "Jabalpur", "Madhya Pradesh"),
        GeoLocation("Ujjain (Mahakaleshwar / Dewas Gate)", "Indore-Ujjain Highway", 23.1765, 75.7885, "Ujjain", "Madhya Pradesh"),
        GeoLocation("Raipur (Pandri / Telibandha)", "GE Road (NH-53)", 21.2514, 81.6296, "Raipur", "Chhattisgarh"),
        GeoLocation("Bilaspur (High Court / Vyapar Vihar)", "Raipur-Bilaspur Expressway", 22.0797, 82.1409, "Bilaspur", "Chhattisgarh")
    )

    data class CorridorPreset(
        val name: String,
        val highway: String,
        val origin: GeoLocation,
        val destination: GeoLocation,
        val waypoints: List<GeoLocation>,
        val distanceKm: Double,
        val baseFareInr: Double,
        val returnSavingsPercent: Int
    )

    val POPULAR_CORRIDORS: List<CorridorPreset> = listOf(
        CorridorPreset(
            name = "Bengaluru ⇄ Chennai Express Corridor",
            highway = "NH-48 / Hosur-Vellore Expressway",
            origin = ALL_LOCATIONS.first { it.city == "Bengaluru" && it.name.contains("Electronic City") },
            destination = ALL_LOCATIONS.first { it.city == "Chennai" && it.name.contains("Koyambedu") },
            waypoints = listOf(
                GeoLocation("Hosur SIPCOT", "NH-48", 12.7409, 77.8253, "Hosur", "Tamil Nadu"),
                GeoLocation("Krishnagiri Toll", "NH-44/48 Junction", 12.5186, 78.2137, "Krishnagiri", "Tamil Nadu"),
                GeoLocation("Vellore Green Circle", "NH-48", 12.9165, 79.1325, "Vellore", "Tamil Nadu"),
                GeoLocation("Kanchipuram Bypass", "NH-48", 12.8342, 79.7036, "Kanchipuram", "Tamil Nadu")
            ),
            distanceKm = 346.0,
            baseFareInr = 550.0,
            returnSavingsPercent = 30
        ),
        CorridorPreset(
            name = "Mumbai ⇄ Pune Expressway",
            highway = "Yashwantrao Chavan Mumbai-Pune Expressway",
            origin = ALL_LOCATIONS.first { it.city == "Mumbai" && it.name.contains("Bandra") },
            destination = ALL_LOCATIONS.first { it.city == "Pune" && it.name.contains("Hinjewadi") },
            waypoints = listOf(
                GeoLocation("Vashi Toll Naka", "Sion-Panvel Expy", 19.0620, 72.9990, "Navi Mumbai", "Maharashtra"),
                GeoLocation("Lonavala Food Mall", "Mumbai-Pune Expy", 18.7557, 73.4091, "Lonavala", "Maharashtra"),
                GeoLocation("Talegaon Toll Plaza", "Mumbai-Pune Expy", 18.7300, 73.6800, "Talegaon", "Maharashtra")
            ),
            distanceKm = 148.0,
            baseFareInr = 380.0,
            returnSavingsPercent = 35
        ),
        CorridorPreset(
            name = "Delhi ⇄ Jaipur Pink City Highway",
            highway = "NH-48 / Delhi-Jaipur Expressway",
            origin = ALL_LOCATIONS.first { it.city == "New Delhi" && it.name.contains("Connaught") },
            destination = ALL_LOCATIONS.first { it.city == "Jaipur" && it.name.contains("Sindhi") },
            waypoints = listOf(
                GeoLocation("Gurgaon Kherki Daula Toll", "NH-48", 28.4010, 76.9950, "Gurgaon", "Haryana"),
                GeoLocation("Neemrana Japanese Zone", "NH-48", 27.9868, 76.3860, "Neemrana", "Rajasthan"),
                GeoLocation("Kotputli Bypass", "NH-48", 27.7050, 76.2000, "Kotputli", "Rajasthan"),
                GeoLocation("Shahpura Toll", "NH-48", 27.3900, 75.9600, "Shahpura", "Rajasthan")
            ),
            distanceKm = 268.0,
            baseFareInr = 490.0,
            returnSavingsPercent = 25
        ),
        CorridorPreset(
            name = "Delhi ⇄ Chandigarh Grand Trunk Link",
            highway = "NH-44 (GT Road)",
            origin = ALL_LOCATIONS.first { it.city == "Delhi" && it.name.contains("Kashmere") },
            destination = ALL_LOCATIONS.first { it.city == "Chandigarh" && it.name.contains("Sector 17") },
            waypoints = listOf(
                GeoLocation("Murthal Haveli Stop", "NH-44", 29.0298, 77.0700, "Sonipat", "Haryana"),
                GeoLocation("Panipat Toll", "NH-44 Elevated", 29.3909, 76.9635, "Panipat", "Haryana"),
                GeoLocation("Karnal Lake", "NH-44", 29.6857, 76.9905, "Karnal", "Haryana"),
                GeoLocation("Ambala Cantt Junction", "NH-44", 30.3782, 76.7767, "Ambala", "Haryana")
            ),
            distanceKm = 245.0,
            baseFareInr = 420.0,
            returnSavingsPercent = 25
        ),
        CorridorPreset(
            name = "Hyderabad ⇄ Bengaluru Deccan Spine",
            highway = "NH-44 (North-South Corridor)",
            origin = ALL_LOCATIONS.first { it.city == "Hyderabad" && it.name.contains("Gachibowli") },
            destination = ALL_LOCATIONS.first { it.city == "Bengaluru" && it.name.contains("Majestic") },
            waypoints = listOf(
                GeoLocation("Kurnool Toll Plaza", "NH-44", 15.8281, 78.0373, "Kurnool", "Andhra Pradesh"),
                GeoLocation("Anantapur Bypass", "NH-44", 14.6819, 77.6006, "Anantapur", "Andhra Pradesh"),
                GeoLocation("Chikkaballapur", "NH-44", 13.4325, 77.7275, "Chikkaballapur", "Karnataka")
            ),
            distanceKm = 569.0,
            baseFareInr = 850.0,
            returnSavingsPercent = 30
        ),
        CorridorPreset(
            name = "Ahmedabad ⇄ Mumbai Western Freight Corridor",
            highway = "NH-48",
            origin = ALL_LOCATIONS.first { it.city == "Ahmedabad" && it.name.contains("SG Highway") },
            destination = ALL_LOCATIONS.first { it.city == "Mumbai" && it.name.contains("Dadar") },
            waypoints = listOf(
                GeoLocation("Vadodara Bypass", "NH-48", 22.3072, 73.1812, "Vadodara", "Gujarat"),
                GeoLocation("Bharuch Narmada Bridge", "NH-48", 21.7051, 72.9959, "Bharuch", "Gujarat"),
                GeoLocation("Surat Kamrej Toll", "NH-48", 21.2721, 72.9610, "Surat", "Gujarat"),
                GeoLocation("Vapi Industrial Area", "NH-48", 20.3718, 72.9056, "Vapi", "Gujarat")
            ),
            distanceKm = 525.0,
            baseFareInr = 780.0,
            returnSavingsPercent = 30
        ),
        CorridorPreset(
            name = "Kochi ⇄ Bengaluru Malabar Transit",
            highway = "NH-544 / NH-44 via Coimbatore & Salem",
            origin = ALL_LOCATIONS.first { it.city == "Kochi" && it.name.contains("Vyttila") },
            destination = ALL_LOCATIONS.first { it.city == "Bengaluru" && it.name.contains("Electronic City") },
            waypoints = listOf(
                GeoLocation("Thrissur Bypass", "NH-544", 10.5276, 76.2144, "Thrissur", "Kerala"),
                GeoLocation("Palakkad Gap", "NH-544", 10.7867, 76.6548, "Palakkad", "Kerala"),
                GeoLocation("Coimbatore Bypass", "L&T Tollway", 10.9600, 77.0100, "Coimbatore", "Tamil Nadu"),
                GeoLocation("Salem Junction", "NH-44", 11.6643, 78.1460, "Salem", "Tamil Nadu")
            ),
            distanceKm = 548.0,
            baseFareInr = 820.0,
            returnSavingsPercent = 28
        ),
        CorridorPreset(
            name = "Kolkata ⇄ Patna Purvanchal Link",
            highway = "NH-19 (Grand Trunk Road)",
            origin = ALL_LOCATIONS.first { it.city == "Kolkata" && it.name.contains("Park Street") },
            destination = ALL_LOCATIONS.first { it.city == "Patna" && it.name.contains("Gandhi") },
            waypoints = listOf(
                GeoLocation("Durgapur Expressway Toll", "NH-19", 23.5204, 87.3119, "Durgapur", "West Bengal"),
                GeoLocation("Asansol Bypass", "NH-19", 23.6739, 86.9524, "Asansol", "West Bengal"),
                GeoLocation("Dhanbad Gobindpur", "NH-19", 23.7957, 86.4304, "Dhanbad", "Jharkhand")
            ),
            distanceKm = 580.0,
            baseFareInr = 890.0,
            returnSavingsPercent = 25
        ),
        CorridorPreset(
            name = "Delhi ⇄ Lucknow Purvanchal Expressway",
            highway = "Yamuna & Agra-Lucknow Expressway",
            origin = ALL_LOCATIONS.first { it.city == "New Delhi" && it.name.contains("Connaught") },
            destination = ALL_LOCATIONS.first { it.city == "Lucknow" && it.name.contains("Charbagh") },
            waypoints = listOf(
                GeoLocation("Noida Expressway Toll", "Yamuna Expy", 28.5200, 77.3800, "Noida", "Uttar Pradesh"),
                GeoLocation("Mathura Cut", "Yamuna Expy", 27.5500, 77.7200, "Mathura", "Uttar Pradesh"),
                GeoLocation("Agra Ring Road Exit", "Agra-Lucknow Expy", 27.1800, 78.0800, "Agra", "Uttar Pradesh"),
                GeoLocation("Kannauj Perfume Hub", "Agra-Lucknow Expy", 27.0500, 79.9100, "Kannauj", "Uttar Pradesh")
            ),
            distanceKm = 535.0,
            baseFareInr = 750.0,
            returnSavingsPercent = 30
        )
    )

    fun search(query: String, regionFilter: String = "All"): List<GeoLocation> {
        val q = query.trim().lowercase()
        var baseList = ALL_LOCATIONS

        if (regionFilter != "All") {
            baseList = when (regionFilter.lowercase()) {
                "north" -> ALL_LOCATIONS.filter { it.state.contains("Delhi") || it.state.contains("Haryana") || it.state.contains("Punjab") || it.state.contains("Himachal") || it.state.contains("Uttarakhand") || it.state.contains("Rajasthan") || it.state.contains("Uttar Pradesh") || it.state.contains("Jammu") }
                "south" -> ALL_LOCATIONS.filter { it.state.contains("Karnataka") || it.state.contains("Tamil Nadu") || it.state.contains("Telangana") || it.state.contains("Andhra Pradesh") || it.state.contains("Kerala") }
                "west" -> ALL_LOCATIONS.filter { it.state.contains("Maharashtra") || it.state.contains("Gujarat") || it.state.contains("Goa") }
                "east" -> ALL_LOCATIONS.filter { it.state.contains("Bengal") || it.state.contains("Bihar") || it.state.contains("Jharkhand") || it.state.contains("Odisha") || it.state.contains("Assam") || it.state.contains("Meghalaya") || it.state.contains("Tripura") || it.state.contains("Sikkim") }
                "central" -> ALL_LOCATIONS.filter { it.state.contains("Madhya Pradesh") || it.state.contains("Chhattisgarh") }
                "metros" -> ALL_LOCATIONS.filter { it.city in listOf("New Delhi", "Delhi", "Mumbai", "Bengaluru", "Chennai", "Kolkata", "Hyderabad", "Pune", "Ahmedabad") }
                else -> ALL_LOCATIONS
            }
        }

        if (q.isEmpty()) return baseList.take(20)

        return baseList.filter { loc ->
            loc.name.lowercase().contains(q) ||
            loc.city.lowercase().contains(q) ||
            loc.state.lowercase().contains(q) ||
            loc.address.lowercase().contains(q)
        }
    }
}
