package com.mr.claudetraining.data.repository

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import com.mr.claudetraining.data.model.DailyGoalDto
import com.mr.claudetraining.data.model.UserProfileDto
import com.mr.claudetraining.data.model.YogaProgramDto
import com.mr.claudetraining.data.model.toDomain
import com.mr.claudetraining.domain.model.DailyGoal
import com.mr.claudetraining.domain.model.UserProfile
import com.mr.claudetraining.domain.model.YogaProgram
import com.mr.claudetraining.domain.repository.YogaRepository
import javax.inject.Inject

class YogaRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : YogaRepository {
    private val programsRef = firestore.collection("yogaPrograms")
    private val profileRef = firestore.collection("dashboard").document("profile")
    private val dailyGoalRef = firestore.collection("dashboard").document("dailyGoal")

    override fun observePrograms(): Flow<List<YogaProgram>> = callbackFlow {
        val registration = programsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val programs = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(YogaProgramDto::class.java)?.copy(id = doc.id)?.toDomain()
            } ?: emptyList()
            trySend(programs)
        }
        awaitClose { registration.remove() }
    }

    override fun observeProfile(): Flow<UserProfile?> =
        documentFlow(profileRef, UserProfileDto::class.java).map { it?.toDomain() }

    override fun observeDailyGoal(): Flow<DailyGoal?> =
        documentFlow(dailyGoalRef, DailyGoalDto::class.java).map { it?.toDomain() }

    private fun <T> documentFlow(ref: DocumentReference, type: Class<T>): Flow<T?> = callbackFlow {
        val registration = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(type))
        }
        awaitClose { registration.remove() }
    }

    /** Populate Firestore with starter content the first time the app runs against empty collections. */
    override suspend fun seedSampleDataIfEmpty() {
        seedPrograms()
        if (!profileRef.get().await().exists()) profileRef.set(sampleProfile).await()
        if (!dailyGoalRef.get().await().exists()) dailyGoalRef.set(sampleDailyGoal).await()
    }

    private suspend fun seedPrograms() {
        if (!programsRef.limit(1).get().await().isEmpty) return

        samplePrograms.forEach { program ->
            val doc = programsRef.document()
            doc.set(program.copy(id = doc.id)).await()
        }
    }

    private val sampleProfile = UserProfileDto(
        name = "Tutorial User",
        memberSince = "Member since 2026",
        sessions = 24,
        dayStreak = 7,
        badges = 3
    )

    private val sampleDailyGoal = DailyGoalDto(completed = 2, total = 3)

    private val samplePrograms = listOf(
        YogaProgramDto(
            title = "Beginner Basics",
            subtitle = "Start your journey",
            level = "Beginner",
            durationMinutes = 15,
            imageUrl = "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=600&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"
        ),
        YogaProgramDto(
            title = "Power Flow",
            subtitle = "Build strength & stamina",
            level = "Intermediate",
            durationMinutes = 30,
            imageUrl = "https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?w=600&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        ),
        YogaProgramDto(
            title = "Relax & Restore",
            subtitle = "Wind down and unwind",
            level = "All levels",
            durationMinutes = 20,
            imageUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
        ),
        YogaProgramDto(
            title = "Sleep Yoga",
            subtitle = "Calm before bed",
            level = "Beginner",
            durationMinutes = 12,
            imageUrl = "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=600&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
        )
    )
}