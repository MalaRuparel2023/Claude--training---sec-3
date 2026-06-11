package com.mr.claudetraining.data.model

import com.mr.claudetraining.domain.model.ExerciseItem
import com.mr.claudetraining.domain.model.FoodEntry
import com.mr.claudetraining.domain.model.FoodItem
import com.mr.claudetraining.domain.model.Meal
import com.mr.claudetraining.domain.model.NutritionGoal
import com.mr.claudetraining.domain.model.WaterLog
import com.mr.claudetraining.domain.model.WeightEntry
import com.mr.claudetraining.domain.model.WorkoutEntry

/** All field defaults are required for Firestore's reflective deserialization. */

data class FoodItemDto(
    val id: String = "",
    val name: String = "",
    val servingLabel: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0
)

fun FoodItemDto.toDomain() = FoodItem(id, name, servingLabel, calories, protein, carbs, fat)

data class FoodEntryDto(
    val id: String = "",
    val date: String = "",
    val meal: String = Meal.BREAKFAST.name,
    val name: String = "",
    val servings: Double = 1.0,
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0
)

fun FoodEntryDto.toDomain() = FoodEntry(
    id = id,
    date = date,
    meal = runCatching { Meal.valueOf(meal) }.getOrDefault(Meal.BREAKFAST),
    name = name,
    servings = servings.toFloat(),
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat
)

fun FoodEntry.toDto() = FoodEntryDto(
    id = id,
    date = date,
    meal = meal.name,
    name = name,
    servings = servings.toDouble(),
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat
)

data class ExerciseItemDto(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val caloriesPerMinute: Int = 0
)

fun ExerciseItemDto.toDomain() = ExerciseItem(id, name, category, caloriesPerMinute)

data class WorkoutEntryDto(
    val id: String = "",
    val date: String = "",
    val name: String = "",
    val durationMinutes: Int = 0,
    val caloriesBurned: Int = 0
)

fun WorkoutEntryDto.toDomain() = WorkoutEntry(id, date, name, durationMinutes, caloriesBurned)

fun WorkoutEntry.toDto() = WorkoutEntryDto(id, date, name, durationMinutes, caloriesBurned)

data class WaterLogDto(
    val date: String = "",
    val glasses: Int = 0
)

fun WaterLogDto.toDomain() = WaterLog(date, glasses)

data class WeightEntryDto(
    val id: String = "",
    val date: String = "",
    val weightKg: Double = 0.0
)

fun WeightEntryDto.toDomain() = WeightEntry(id, date, weightKg.toFloat())

fun WeightEntry.toDto() = WeightEntryDto(id, date, weightKg.toDouble())

data class NutritionGoalDto(
    val calorieGoal: Int = 2000,
    val proteinGoal: Int = 120,
    val carbGoal: Int = 250,
    val fatGoal: Int = 65,
    val waterGoalGlasses: Int = 8
)

fun NutritionGoalDto.toDomain() =
    NutritionGoal(calorieGoal, proteinGoal, carbGoal, fatGoal, waterGoalGlasses)