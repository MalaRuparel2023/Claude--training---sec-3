package com.mr.claudetraining.domain.repository

import kotlinx.coroutines.flow.Flow
import com.mr.claudetraining.domain.model.ExerciseItem
import com.mr.claudetraining.domain.model.FoodEntry
import com.mr.claudetraining.domain.model.FoodItem
import com.mr.claudetraining.domain.model.NutritionGoal
import com.mr.claudetraining.domain.model.WaterLog
import com.mr.claudetraining.domain.model.WeightEntry
import com.mr.claudetraining.domain.model.WorkoutEntry

interface HealthRepository {
    // Shared catalogs
    fun observeFoodCatalog(): Flow<List<FoodItem>>
    fun observeExerciseCatalog(): Flow<List<ExerciseItem>>

    // Goal
    fun observeGoal(): Flow<NutritionGoal>

    // Food diary
    fun observeFoodEntries(date: String): Flow<List<FoodEntry>>
    suspend fun addFoodEntry(entry: FoodEntry)
    suspend fun deleteFoodEntry(id: String)

    // Workouts
    fun observeWorkoutEntries(date: String): Flow<List<WorkoutEntry>>
    suspend fun addWorkoutEntry(entry: WorkoutEntry)
    suspend fun deleteWorkoutEntry(id: String)

    // Water (one doc per date)
    fun observeWater(date: String): Flow<WaterLog>
    suspend fun setWater(date: String, glasses: Int)

    // Weight
    fun observeWeightEntries(): Flow<List<WeightEntry>>
    suspend fun addWeightEntry(entry: WeightEntry)
    suspend fun deleteWeightEntry(id: String)

    /** Seed shared food + exercise catalogs and a default goal the first time. */
    suspend fun seedCatalogsIfEmpty()
}