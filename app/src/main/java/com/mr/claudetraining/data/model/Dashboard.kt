package com.mr.claudetraining.data.model

import com.mr.claudetraining.domain.model.DailyGoal as DomainDailyGoal
import com.mr.claudetraining.domain.model.UserProfile as DomainUserProfile

/** Firestore `dashboard/profile` document. Defaults required for deserialization. */
data class UserProfileDto(
    val name: String = "",
    val memberSince: String = "",
    val sessions: Int = 0,
    val dayStreak: Int = 0,
    val badges: Int = 0
)

fun UserProfileDto.toDomain() = DomainUserProfile(
    name = name,
    memberSince = memberSince,
    sessions = sessions,
    dayStreak = dayStreak,
    badges = badges
)

/** Firestore `dashboard/dailyGoal` document. */
data class DailyGoalDto(
    val completed: Int = 0,
    val total: Int = 0
)

fun DailyGoalDto.toDomain() = DomainDailyGoal(
    completed = completed,
    total = total
)