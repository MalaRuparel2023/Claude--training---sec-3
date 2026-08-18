package com.mr.claudetraining.domain.model

enum class WorkoutType { YOGA, CARDIO, STRENGTH, FLEXIBILITY, PILATES, OTHER }

enum class WorkoutIntensity { LIGHT, MODERATE, HIGH, VERY_HIGH }

data class WorkoutSession(
    val id: String = "",
    val userId: String = "",
    val type: WorkoutType = WorkoutType.YOGA,
    val intensity: WorkoutIntensity = WorkoutIntensity.MODERATE,
    val durationMinutes: Int = 0,
    val caloriesBurned: Int = 0,
    val notes: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val syncedToCloud: Boolean = false
) {
    val isComplete: Boolean get() = durationMinutes > 0 && endTime > startTime
    val dateStr: String get() = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date(startTime))
}

data class WorkoutSessionFilter(
    val types: Set<WorkoutType> = emptySet(),
    val minIntensity: WorkoutIntensity? = null,
    val dateRange: Pair<Long, Long>? = null
) {
    fun matches(session: WorkoutSession): Boolean {
        if (types.isNotEmpty() && session.type !in types) return false
        if (minIntensity != null && session.intensity.ordinal < minIntensity.ordinal) return false
        if (dateRange != null) {
            if (session.startTime < dateRange.first || session.startTime > dateRange.second) return false
        }
        return true
    }

    val isEmpty: Boolean get() = this == None

    companion object {
        val None = WorkoutSessionFilter()
    }
}
