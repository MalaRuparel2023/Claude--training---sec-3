package com.mr.claudetraining.domain.model

/** Meal slot a food entry belongs to. */
enum class Meal { BREAKFAST, LUNCH, DINNER, SNACK }

/** A searchable food in the shared catalog. Macros are per serving. */
data class FoodItem(
    val id: String = "",
    val name: String = "",
    val servingLabel: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0
)

/** A food logged by the user on a given day. */
data class FoodEntry(
    val id: String = "",
    val date: String = "",
    val meal: Meal = Meal.BREAKFAST,
    val name: String = "",
    val servings: Float = 1f,
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0
)

/** An exercise in the shared catalog. */
data class ExerciseItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val caloriesPerMinute: Int = 0
)

/** A workout logged by the user on a given day. */
data class WorkoutEntry(
    val id: String = "",
    val date: String = "",
    val name: String = "",
    val durationMinutes: Int = 0,
    val caloriesBurned: Int = 0
)

/** Glasses of water logged for a single day (one doc per date). */
data class WaterLog(
    val date: String = "",
    val glasses: Int = 0
)

/** A body-weight measurement. */
data class WeightEntry(
    val id: String = "",
    val date: String = "",
    val weightKg: Float = 0f
)

/** The user's daily targets. */
data class NutritionGoal(
    val calorieGoal: Int = 2000,
    val proteinGoal: Int = 120,
    val carbGoal: Int = 250,
    val fatGoal: Int = 65,
    val waterGoalGlasses: Int = 8
)

/** Aggregated nutrition for a day, derived from food entries. Display-only. */
data class DailyNutrition(
    val caloriesConsumed: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val caloriesBurned: Int = 0
) {
    /** Net calories: consumed minus burned. */
    val net: Int get() = caloriesConsumed - caloriesBurned
}