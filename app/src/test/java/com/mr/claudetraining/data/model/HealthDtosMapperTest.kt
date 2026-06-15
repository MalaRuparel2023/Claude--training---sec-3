package com.mr.claudetraining.data.model

import com.mr.claudetraining.domain.model.FoodEntry
import com.mr.claudetraining.domain.model.Meal
import com.mr.claudetraining.domain.model.WeightEntry
import com.mr.claudetraining.domain.model.WorkoutEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class HealthDtosMapperTest {

    @Test
    fun `FoodItemDto toDomain maps every field`() {
        val domain = FoodItemDto("f1", "Apple", "1 piece", 95, 1, 25, 0).toDomain()
        assertEquals("f1", domain.id)
        assertEquals("Apple", domain.name)
        assertEquals("1 piece", domain.servingLabel)
        assertEquals(95, domain.calories)
        assertEquals(1, domain.protein)
        assertEquals(25, domain.carbs)
        assertEquals(0, domain.fat)
    }

    @Test
    fun `FoodItemDto toDomain defaults`() {
        val domain = FoodItemDto().toDomain()
        assertEquals("", domain.id)
        assertEquals(0, domain.calories)
    }

    @Test
    fun `FoodEntryDto toDomain maps fields and converts servings to float`() {
        val domain = FoodEntryDto(
            id = "e1",
            date = "2026-06-15",
            meal = Meal.DINNER.name,
            name = "Pasta",
            servings = 2.5,
            calories = 600,
            protein = 20,
            carbs = 80,
            fat = 15
        ).toDomain()

        assertEquals("e1", domain.id)
        assertEquals("2026-06-15", domain.date)
        assertEquals(Meal.DINNER, domain.meal)
        assertEquals("Pasta", domain.name)
        assertEquals(2.5f, domain.servings, 0f)
        assertEquals(600, domain.calories)
        assertEquals(20, domain.protein)
        assertEquals(80, domain.carbs)
        assertEquals(15, domain.fat)
    }

    @Test
    fun `FoodEntryDto toDomain decodes all valid meals`() {
        Meal.values().forEach { meal ->
            assertEquals(meal, FoodEntryDto(meal = meal.name).toDomain().meal)
        }
    }

    @Test
    fun `FoodEntryDto toDomain falls back to BREAKFAST for unknown meal`() {
        assertEquals(Meal.BREAKFAST, FoodEntryDto(meal = "BRUNCH").toDomain().meal)
    }

    @Test
    fun `FoodEntryDto toDomain falls back to BREAKFAST for empty meal`() {
        assertEquals(Meal.BREAKFAST, FoodEntryDto(meal = "").toDomain().meal)
    }

    @Test
    fun `FoodEntry toDto maps fields and converts servings to double`() {
        val dto = FoodEntry(
            id = "e2",
            date = "d",
            meal = Meal.SNACK,
            name = "Nuts",
            servings = 1.5f,
            calories = 200,
            protein = 5,
            carbs = 6,
            fat = 18
        ).toDto()

        assertEquals("e2", dto.id)
        assertEquals(Meal.SNACK.name, dto.meal)
        assertEquals(1.5, dto.servings, 0.0)
        assertEquals(200, dto.calories)
    }

    @Test
    fun `FoodEntry round-trips through dto`() {
        val entry = FoodEntry(
            id = "rt",
            date = "2026-01-01",
            meal = Meal.LUNCH,
            name = "Rice",
            servings = 3f,
            calories = 300,
            protein = 7,
            carbs = 60,
            fat = 2
        )
        assertEquals(entry, entry.toDto().toDomain())
    }

    @Test
    fun `ExerciseItemDto toDomain maps every field`() {
        val domain = ExerciseItemDto("x1", "Running", "Cardio", 12).toDomain()
        assertEquals("x1", domain.id)
        assertEquals("Running", domain.name)
        assertEquals("Cardio", domain.category)
        assertEquals(12, domain.caloriesPerMinute)
    }

    @Test
    fun `WorkoutEntryDto toDomain maps every field`() {
        val domain = WorkoutEntryDto("w1", "2026-06-15", "Leg day", 45, 400).toDomain()
        assertEquals("w1", domain.id)
        assertEquals("2026-06-15", domain.date)
        assertEquals("Leg day", domain.name)
        assertEquals(45, domain.durationMinutes)
        assertEquals(400, domain.caloriesBurned)
    }

    @Test
    fun `WorkoutEntry toDto maps every field`() {
        val dto = WorkoutEntry("w2", "d", "Arms", 30, 250).toDto()
        assertEquals("w2", dto.id)
        assertEquals("Arms", dto.name)
        assertEquals(30, dto.durationMinutes)
        assertEquals(250, dto.caloriesBurned)
    }

    @Test
    fun `WorkoutEntry round-trips through dto`() {
        val entry = WorkoutEntry("rt", "d", "n", 10, 20)
        assertEquals(entry, entry.toDto().toDomain())
    }

    @Test
    fun `WaterLogDto toDomain maps every field`() {
        val domain = WaterLogDto("2026-06-15", 6).toDomain()
        assertEquals("2026-06-15", domain.date)
        assertEquals(6, domain.glasses)
    }

    @Test
    fun `WeightEntryDto toDomain maps fields and converts weight to float`() {
        val domain = WeightEntryDto("we1", "d", 72.4).toDomain()
        assertEquals("we1", domain.id)
        assertEquals("d", domain.date)
        assertEquals(72.4f, domain.weightKg, 0f)
    }

    @Test
    fun `WeightEntry toDto maps fields and converts weight to double`() {
        val dto = WeightEntry("we2", "d", 80.5f).toDto()
        assertEquals("we2", dto.id)
        assertEquals(80.5, dto.weightKg, 0.0)
    }

    @Test
    fun `WeightEntry round-trips through dto`() {
        val entry = WeightEntry("rt", "d", 65.5f)
        assertEquals(entry, entry.toDto().toDomain())
    }

    @Test
    fun `NutritionGoalDto toDomain maps every field`() {
        val domain = NutritionGoalDto(1800, 100, 200, 50, 10).toDomain()
        assertEquals(1800, domain.calorieGoal)
        assertEquals(100, domain.proteinGoal)
        assertEquals(200, domain.carbGoal)
        assertEquals(50, domain.fatGoal)
        assertEquals(10, domain.waterGoalGlasses)
    }

    @Test
    fun `NutritionGoalDto toDomain uses dto defaults`() {
        val domain = NutritionGoalDto().toDomain()
        assertEquals(2000, domain.calorieGoal)
        assertEquals(120, domain.proteinGoal)
        assertEquals(250, domain.carbGoal)
        assertEquals(65, domain.fatGoal)
        assertEquals(8, domain.waterGoalGlasses)
    }
}
