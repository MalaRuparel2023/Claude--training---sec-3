package com.mr.claudetraining.domain.model

/**
 * Core yoga-program entity. A plain data holder with no framework dependencies — the data layer
 * is responsible for mapping persistence records (Firestore documents) onto this type.
 * Default values let the data layer reconstruct instances reflectively.
 */
data class YogaProgram(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val category: String = "",
    val level: String = "",
    val durationMinutes: Int = 0,
    val calories: Int = 0,
    val imageUrl: String = "",
    val videoUrl: String = ""
)