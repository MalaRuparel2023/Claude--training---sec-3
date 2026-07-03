package com.mr.claudetraining.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mr.claudetraining.domain.model.WorkoutIntensity
import com.mr.claudetraining.domain.model.WorkoutSession
import com.mr.claudetraining.domain.model.WorkoutType

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String,
    val intensity: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val notes: String,
    val startTime: Long,
    val endTime: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val syncedToCloud: Boolean = false
) {
    fun toDomain(): WorkoutSession = WorkoutSession(
        id = id,
        userId = userId,
        type = WorkoutType.valueOf(type),
        intensity = WorkoutIntensity.valueOf(intensity),
        durationMinutes = durationMinutes,
        caloriesBurned = caloriesBurned,
        notes = notes,
        startTime = startTime,
        endTime = endTime,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncedToCloud = syncedToCloud
    )

    companion object {
        fun fromDomain(domain: WorkoutSession): WorkoutSessionEntity = WorkoutSessionEntity(
            id = domain.id,
            userId = domain.userId,
            type = domain.type.name,
            intensity = domain.intensity.name,
            durationMinutes = domain.durationMinutes,
            caloriesBurned = domain.caloriesBurned,
            notes = domain.notes,
            startTime = domain.startTime,
            endTime = domain.endTime,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            syncedToCloud = domain.syncedToCloud
        )
    }
}
