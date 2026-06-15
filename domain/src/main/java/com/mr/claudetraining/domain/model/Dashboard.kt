package com.mr.claudetraining.domain.model

/** A user's dashboard profile. Pure entity with no persistence/framework dependencies. */
data class UserProfile(
    val name: String = "",
    val memberSince: String = "",
    val sessions: Int = 0,
    val dayStreak: Int = 0,
    val badges: Int = 0
)

/** A user's daily session goal. */
data class DailyGoal(
    val completed: Int = 0,
    val total: Int = 0
) {
    val progress: Float get() = if (total == 0) 0f else completed.toFloat() / total
}