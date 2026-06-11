package com.mr.claudetraining.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.mr.claudetraining.domain.model.DailyNutrition
import com.mr.claudetraining.domain.model.ExerciseItem
import com.mr.claudetraining.domain.model.FoodEntry
import com.mr.claudetraining.domain.model.FoodItem
import com.mr.claudetraining.domain.model.Meal
import com.mr.claudetraining.domain.model.NutritionGoal
import com.mr.claudetraining.domain.model.WaterLog
import com.mr.claudetraining.domain.model.WeightEntry
import com.mr.claudetraining.domain.model.WorkoutEntry
import com.mr.claudetraining.domain.repository.HealthRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.roundToInt

data class HealthUiState(
    val date: String = "",
    val goal: NutritionGoal = NutritionGoal(),
    val foodEntries: List<FoodEntry> = emptyList(),
    val workoutEntries: List<WorkoutEntry> = emptyList(),
    val water: WaterLog = WaterLog(),
    val weightEntries: List<WeightEntry> = emptyList(),
    val foodCatalog: List<FoodItem> = emptyList(),
    val exerciseCatalog: List<ExerciseItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val nutrition: DailyNutrition
        get() = DailyNutrition(
            caloriesConsumed = foodEntries.sumOf { it.calories },
            protein = foodEntries.sumOf { it.protein },
            carbs = foodEntries.sumOf { it.carbs },
            fat = foodEntries.sumOf { it.fat },
            caloriesBurned = workoutEntries.sumOf { it.caloriesBurned }
        )

    fun entriesFor(meal: Meal): List<FoodEntry> = foodEntries.filter { it.meal == meal }
}

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val repository: HealthRepository
) : ViewModel() {

    private val today: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    private val _uiState = MutableStateFlow(HealthUiState(date = today))
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            runCatching { repository.seedCatalogsIfEmpty() }

            launch {
                repository.observeFoodCatalog().catch { }
                    .collect { _uiState.value = _uiState.value.copy(foodCatalog = it) }
            }
            launch {
                repository.observeExerciseCatalog().catch { }
                    .collect { _uiState.value = _uiState.value.copy(exerciseCatalog = it) }
            }
            launch {
                repository.observeGoal().catch { }
                    .collect { _uiState.value = _uiState.value.copy(goal = it) }
            }
            launch {
                repository.observeFoodEntries(today)
                    .catch { e -> setError(e) }
                    .collect {
                        _uiState.value = _uiState.value.copy(
                            foodEntries = it, isLoading = false, error = null
                        )
                    }
            }
            launch {
                repository.observeWorkoutEntries(today).catch { }
                    .collect { _uiState.value = _uiState.value.copy(workoutEntries = it) }
            }
            launch {
                repository.observeWater(today).catch { }
                    .collect { _uiState.value = _uiState.value.copy(water = it) }
            }
            launch {
                repository.observeWeightEntries().catch { }
                    .collect { _uiState.value = _uiState.value.copy(weightEntries = it) }
            }
        }
    }

    fun addFood(item: FoodItem, meal: Meal, servings: Float) {
        viewModelScope.launch {
            runCatching {
                repository.addFoodEntry(
                    FoodEntry(
                        date = today,
                        meal = meal,
                        name = item.name,
                        servings = servings,
                        calories = (item.calories * servings).roundToInt(),
                        protein = (item.protein * servings).roundToInt(),
                        carbs = (item.carbs * servings).roundToInt(),
                        fat = (item.fat * servings).roundToInt()
                    )
                )
            }.onFailure { setError(it) }
        }
    }

    fun deleteFood(id: String) {
        viewModelScope.launch { runCatching { repository.deleteFoodEntry(id) } }
    }

    fun addWorkout(item: ExerciseItem, minutes: Int) {
        viewModelScope.launch {
            runCatching {
                repository.addWorkoutEntry(
                    WorkoutEntry(
                        date = today,
                        name = item.name,
                        durationMinutes = minutes,
                        caloriesBurned = item.caloriesPerMinute * minutes
                    )
                )
            }.onFailure { setError(it) }
        }
    }

    fun deleteWorkout(id: String) {
        viewModelScope.launch { runCatching { repository.deleteWorkoutEntry(id) } }
    }

    fun changeWater(delta: Int) {
        val next = (_uiState.value.water.glasses + delta).coerceAtLeast(0)
        viewModelScope.launch { runCatching { repository.setWater(today, next) } }
    }

    fun addWeight(weightKg: Float) {
        viewModelScope.launch {
            runCatching {
                repository.addWeightEntry(WeightEntry(date = today, weightKg = weightKg))
            }.onFailure { setError(it) }
        }
    }

    fun deleteWeight(id: String) {
        viewModelScope.launch { runCatching { repository.deleteWeightEntry(id) } }
    }

    private fun setError(e: Throwable) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = e.message ?: "Something went wrong"
        )
    }
}