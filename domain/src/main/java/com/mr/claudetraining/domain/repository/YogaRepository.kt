package com.mr.claudetraining.domain.repository

import kotlinx.coroutines.flow.Flow
import com.mr.claudetraining.domain.model.DailyGoal
import com.mr.claudetraining.domain.model.UserProfile
import com.mr.claudetraining.domain.model.YogaProgram

interface YogaRepository {
    fun observePrograms(): Flow<List<YogaProgram>>
    fun observeProfile(): Flow<UserProfile?>
    fun observeDailyGoal(): Flow<DailyGoal?>
    suspend fun seedSampleDataIfEmpty()
}