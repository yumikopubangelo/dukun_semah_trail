package com.example.dukunsemahtrail.data

import kotlinx.coroutines.flow.Flow

class TrailRepository(private val trailDao: TrailDao, private val hikeDao: HikeDao) {
    val allTrails: Flow<List<Trail>> = trailDao.getAllTrails()

    fun searchTrails(query: String): Flow<List<Trail>> {
        return trailDao.searchTrails(query)
    }

    suspend fun getTrailById(id: Long): Trail? {
        return trailDao.getTrailById(id)
    }

    suspend fun insertTrails(trails: List<Trail>) {
        trailDao.insertTrails(trails)
    }

    // Initial data seeding
    suspend fun seedInitialData() {
        val initialTrails = listOf(
            Trail(
                name = "Mount Kinabalu - Summit Trail",
                location = "Sabah, Malaysia",
                difficulty = "Hard",
                distanceKm = 8.7,
                elevationGainMeters = 2300,
                description = "The highest peak in Southeast Asia. A challenging climb with breathtaking views from Low's Peak.",
                terrainType = "Granite, Rainforest",
                safetyWarnings = "High altitude, potential for altitude sickness. Sudden weather changes."
            ),
            Trail(
                name = "Mount Rinjani - Senaru Route",
                location = "Lombok, Indonesia",
                difficulty = "Hard",
                distanceKm = 10.5,
                elevationGainMeters = 2000,
                description = "A strenuous trek to the crater rim of Mount Rinjani, offering views of the Segara Anak lake.",
                terrainType = "Volcanic ash, Forest",
                safetyWarnings = "Loose soil, steep slopes. Active volcano status."
            ),
            Trail(
                name = "Mount Semeru - Mahameru Peak",
                location = "East Java, Indonesia",
                difficulty = "Expert",
                distanceKm = 13.0,
                elevationGainMeters = 3676,
                description = "The highest volcano in Java. The final ascent to Mahameru is extremely steep and covered in volcanic sand.",
                terrainType = "Volcanic sand, Pine forest",
                safetyWarnings = "Extreme wind, volcanic gas, rockfalls. Only for experienced climbers."
            ),
            Trail(
                name = "Mount Merapi - Selo Route",
                location = "Central Java, Indonesia",
                difficulty = "Moderate",
                distanceKm = 4.5,
                elevationGainMeters = 1200,
                description = "A relatively short but steep climb to one of the most active volcanoes in the world.",
                terrainType = "Volcanic rock, Shrubs",
                safetyWarnings = "Volcanic activity monitoring required. Steep cliffs near the crater."
            ),
            Trail(
                name = "Mount Kerinci",
                location = "Jambi, Indonesia",
                difficulty = "Hard",
                distanceKm = 9.0,
                elevationGainMeters = 3805,
                description = "The highest volcano in Indonesia and the highest peak on Sumatra.",
                terrainType = "Dense rainforest, Volcanic rock",
                safetyWarnings = "Dense jungle, slippery trails, potential for mudslides."
            )
        )
        trailDao.deleteAllTrails()
        trailDao.insertTrails(initialTrails)
    }
}
