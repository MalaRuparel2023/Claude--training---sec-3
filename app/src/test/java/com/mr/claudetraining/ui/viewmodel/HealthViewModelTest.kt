package com.mr.claudetraining.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.mr.claudetraining.domain.model.ExerciseItem
import com.mr.claudetraining.domain.model.FoodEntry
import com.mr.claudetraining.domain.model.FoodItem
import com.mr.claudetraining.domain.model.Meal
import com.mr.claudetraining.domain.model.NutritionGoal
import com.mr.claudetraining.domain.model.WaterLog
import com.mr.claudetraining.domain.model.WeightEntry
import com.mr.claudetraining.domain.model.WorkoutEntry
import com.mr.claudetraining.domain.repository.HealthRepository

@OptIn(ExperimentalCoroutinesApi::class)
class HealthViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository: HealthRepository = mock()

    private val foodCatalog = listOf(
        FoodItem(id = "f1", name = "Egg", calories = 70, protein = 6, carbs = 1, fat = 5),
        FoodItem(id = "f2", name = "Rice", calories = 200, protein = 4, carbs = 45, fat = 1)
    )
    private val exerciseCatalog = listOf(
        ExerciseItem(id = "e1", name = "Run", category = "Cardio", caloriesPerMinute = 10)
    )
    private val goal = NutritionGoal(calorieGoal = 1800)
    private val foodEntries = listOf(
        FoodEntry(id = "fe1", meal = Meal.BREAKFAST, name = "Egg", calories = 70, protein = 6, carbs = 1, fat = 5),
        FoodEntry(id = "fe2", meal = Meal.LUNCH, name = "Rice", calories = 200, protein = 4, carbs = 45, fat = 1),
        FoodEntry(id = "fe3", meal = Meal.BREAKFAST, name = "Egg", calories = 70, protein = 6, carbs = 1, fat = 5)
    )
    private val workoutEntries = listOf(
        WorkoutEntry(id = "w1", name = "Run", durationMinutes = 30, caloriesBurned = 300)
    )
    private val water = WaterLog(glasses = 3)
    private val weightEntries = listOf(WeightEntry(id = "we1", weightKg = 70f))

    /** Wire up every flow with a default emission so init completes cleanly. */
    private fun stubHappyPath() {
        whenever(repository.observeFoodCatalog()).thenReturn(flowOf(foodCatalog))
        whenever(repository.observeExerciseCatalog()).thenReturn(flowOf(exerciseCatalog))
        whenever(repository.observeGoal()).thenReturn(flowOf(goal))
        whenever(repository.observeFoodEntries(any())).thenReturn(flowOf(foodEntries))
        whenever(repository.observeWorkoutEntries(any())).thenReturn(flowOf(workoutEntries))
        whenever(repository.observeWater(any())).thenReturn(flowOf(water))
        whenever(repository.observeWeightEntries()).thenReturn(flowOf(weightEntries))
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = HealthViewModel(repository)

    // --- Initial load ---

    @Test
    fun `init shows loading and no error before any emission`() = runTest(dispatcher) {
        stubHappyPath()

        val vm = viewModel()

        assertTrue(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `initial load populates all state and clears loading`() = runTest(dispatcher) {
        stubHappyPath()

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(foodCatalog, state.foodCatalog)
        assertEquals(exerciseCatalog, state.exerciseCatalog)
        assertEquals(goal, state.goal)
        assertEquals(foodEntries, state.foodEntries)
        assertEquals(workoutEntries, state.workoutEntries)
        assertEquals(water, state.water)
        assertEquals(weightEntries, state.weightEntries)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `init seeds catalogs`() = runTest(dispatcher) {
        stubHappyPath()

        viewModel()
        advanceUntilIdle()

        verify(repository).seedCatalogsIfEmpty()
    }

    @Test
    fun `seeding failure does not crash and data still loads`() = runTest(dispatcher) {
        stubHappyPath()
        repository.stub {
            onBlocking { seedCatalogsIfEmpty() } doThrow RuntimeException("seed boom")
        }

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(foodEntries, state.foodEntries)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `empty food entries still clears loading without error`() = runTest(dispatcher) {
        stubHappyPath()
        whenever(repository.observeFoodEntries(any())).thenReturn(flowOf(emptyList()))

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.foodEntries.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `food entries update on each new emission`() = runTest(dispatcher) {
        stubHappyPath()
        val upstream = MutableSharedFlow<List<FoodEntry>>()
        whenever(repository.observeFoodEntries(any())).thenReturn(upstream)

        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isLoading)

        upstream.emit(listOf(foodEntries[0]))
        advanceUntilIdle()
        assertEquals(listOf(foodEntries[0]), vm.uiState.value.foodEntries)
        assertFalse(vm.uiState.value.isLoading)

        upstream.emit(foodEntries)
        advanceUntilIdle()
        assertEquals(foodEntries, vm.uiState.value.foodEntries)
    }

    // --- Error handling ---

    @Test
    fun `food entries flow failure sets error message and stops loading`() = runTest(dispatcher) {
        stubHappyPath()
        whenever(repository.observeFoodEntries(any()))
            .thenReturn(flow { throw RuntimeException("db down") })

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("db down", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `food entries flow failure without message falls back to default`() = runTest(dispatcher) {
        stubHappyPath()
        whenever(repository.observeFoodEntries(any()))
            .thenReturn(flow { throw RuntimeException() })

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals("Something went wrong", vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `failures in other flows are swallowed and food entries still load`() = runTest(dispatcher) {
        stubHappyPath()
        whenever(repository.observeFoodCatalog()).thenReturn(flow { throw RuntimeException("x") })
        whenever(repository.observeExerciseCatalog()).thenReturn(flow { throw RuntimeException("x") })
        whenever(repository.observeGoal()).thenReturn(flow { throw RuntimeException("x") })
        whenever(repository.observeWorkoutEntries(any())).thenReturn(flow { throw RuntimeException("x") })
        whenever(repository.observeWater(any())).thenReturn(flow { throw RuntimeException("x") })
        whenever(repository.observeWeightEntries()).thenReturn(flow { throw RuntimeException("x") })

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(foodEntries, state.foodEntries)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    // --- Computed state: nutrition ---

    @Test
    fun `nutrition aggregates food and workout totals`() = runTest(dispatcher) {
        stubHappyPath()

        val vm = viewModel()
        advanceUntilIdle()

        val nutrition = vm.uiState.value.nutrition
        assertEquals(340, nutrition.caloriesConsumed) // 70 + 200 + 70
        assertEquals(16, nutrition.protein)            // 6 + 4 + 6
        assertEquals(47, nutrition.carbs)              // 1 + 45 + 1
        assertEquals(11, nutrition.fat)                // 5 + 1 + 5
        assertEquals(300, nutrition.caloriesBurned)
        assertEquals(40, nutrition.net)                // 340 - 300
    }

    @Test
    fun `nutrition is zero when no entries`() = runTest(dispatcher) {
        stubHappyPath()
        whenever(repository.observeFoodEntries(any())).thenReturn(flowOf(emptyList()))
        whenever(repository.observeWorkoutEntries(any())).thenReturn(flowOf(emptyList()))

        val vm = viewModel()
        advanceUntilIdle()

        val nutrition = vm.uiState.value.nutrition
        assertEquals(0, nutrition.caloriesConsumed)
        assertEquals(0, nutrition.caloriesBurned)
        assertEquals(0, nutrition.net)
    }

    // --- Computed state: entriesFor ---

    @Test
    fun `entriesFor filters food entries by meal`() = runTest(dispatcher) {
        stubHappyPath()

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(listOf(foodEntries[0], foodEntries[2]), state.entriesFor(Meal.BREAKFAST))
        assertEquals(listOf(foodEntries[1]), state.entriesFor(Meal.LUNCH))
        assertTrue(state.entriesFor(Meal.DINNER).isEmpty())
        assertTrue(state.entriesFor(Meal.SNACK).isEmpty())
    }

    // --- addFood ---

    @Test
    fun `addFood scales macros by servings and persists entry`() = runTest(dispatcher) {
        stubHappyPath()

        val vm = viewModel()
        advanceUntilIdle()

        vm.addFood(foodCatalog[0], Meal.DINNER, 2f)
        advanceUntilIdle()

        val captor = argumentCaptor<FoodEntry>()
        verify(repository).addFoodEntry(captor.capture())
        val entry = captor.firstValue
        assertEquals(Meal.DINNER, entry.meal)
        assertEquals("Egg", entry.name)
        assertEquals(2f, entry.servings)
        assertEquals(140, entry.calories) // 70 * 2
        assertEquals(12, entry.protein)   // 6 * 2
        assertEquals(2, entry.carbs)      // 1 * 2
        assertEquals(10, entry.fat)       // 5 * 2
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `addFood rounds fractional servings`() = runTest(dispatcher) {
        stubHappyPath()

        val vm = viewModel()
        advanceUntilIdle()

        vm.addFood(foodCatalog[0], Meal.SNACK, 1.5f)
        advanceUntilIdle()

        val captor = argumentCaptor<FoodEntry>()
        verify(repository).addFoodEntry(captor.capture())
        val entry = captor.firstValue
        assertEquals(105, entry.calories) // 70 * 1.5 = 105
        assertEquals(9, entry.protein)    // 6 * 1.5 = 9
        assertEquals(2, entry.carbs)      // 1 * 1.5 = 1.5 -> 2
        assertEquals(8, entry.fat)        // 5 * 1.5 = 7.5 -> 8
    }

    @Test
    fun `addFood failure sets error`() = runTest(dispatcher) {
        stubHappyPath()
        repository.stub {
            onBlocking { addFoodEntry(any()) } doThrow RuntimeException("add food failed")
        }

        val vm = viewModel()
        advanceUntilIdle()

        vm.addFood(foodCatalog[0], Meal.BREAKFAST, 1f)
        advanceUntilIdle()

        assertEquals("add food failed", vm.uiState.value.error)
    }

    // --- deleteFood ---

    @Test
    fun `deleteFood delegates to repository`() = runTest(dispatcher) {
        stubHappyPath()

        val vm = viewModel()
        advanceUntilIdle()

        vm.deleteFood("fe1")
        advanceUntilIdle()

        verify(repository).deleteFoodEntry("fe1")
    }

    @Test
    fun `deleteFood failure is swallowed and leaves no error`() = runTest(dispatcher) {
        stubHappyPath()
        repository.stub {
            onBlocking { deleteFoodEntry(any()) } doThrow RuntimeException("del failed")
        }

        val vm = viewModel()
        advanceUntilIdle()

        vm.deleteFood("fe1")
        advanceUntilIdle()

        assertNull(vm.uiState.value.error)
    }

    // --- addWorkout ---

    @Test
    fun `addWorkout computes calories burned from duration`() = runTest(dispatcher) {
        stubHappyPath()

        val vm = viewModel()
        advanceUntilIdle()

        vm.addWorkout(exerciseCatalog[0], 20)
        advanceUntilIdle()

        val captor = argumentCaptor<WorkoutEntry>()
        verify(repository).addWorkoutEntry(captor.capture())
        val entry = captor.firstValue
        assertEquals("Run", entry.name)
        assertEquals(20, entry.durationMinutes)
        assertEquals(200, entry.caloriesBurned) // 10 * 20
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `addWorkout failure sets error`() = runTest(dispatcher) {
        stubHappyPath()
        repository.stub {
            onBlocking { addWorkoutEntry(any()) } doThrow RuntimeException("workout failed")
        }

        val vm = viewModel()
        advanceUntilIdle()

        vm.addWorkout(exerciseCatalog[0], 10)
        advanceUntilIdle()

        assertEquals("workout failed", vm.uiState.value.error)
    }

    // --- deleteWorkout ---

    @Test
    fun `deleteWorkout delegates to repository`() = runTest(dispatcher) {
        stubHappyPath()

        val vm = viewModel()
        advanceUntilIdle()

        vm.deleteWorkout("w1")
        advanceUntilIdle()

        verify(repository).deleteWorkoutEntry("w1")
    }

    @Test
    fun `deleteWorkout failure is swallowed`() = runTest(dispatcher) {
        stubHappyPath()
        repository.stub {
            onBlocking { deleteWorkoutEntry(any()) } doThrow RuntimeException("nope")
        }

        val vm = viewModel()
        advanceUntilIdle()

        vm.deleteWorkout("w1")
        advanceUntilIdle()

        assertNull(vm.uiState.value.error)
    }

    // --- changeWater ---

    @Test
    fun `changeWater adds delta to current glasses`() = runTest(dispatcher) {
        stubHappyPath() // current water = 3 glasses

        val vm = viewModel()
        advanceUntilIdle()

        vm.changeWater(2)
        advanceUntilIdle()

        verify(repository).setWater(any(), eq(5))
    }

    @Test
    fun `changeWater clamps negative result to zero`() = runTest(dispatcher) {
        stubHappyPath() // current water = 3 glasses

        val vm = viewModel()
        advanceUntilIdle()

        vm.changeWater(-10)
        advanceUntilIdle()

        verify(repository).setWater(any(), eq(0))
    }

    @Test
    fun `changeWater failure is swallowed`() = runTest(dispatcher) {
        stubHappyPath()
        repository.stub {
            onBlocking { setWater(any(), any()) } doThrow RuntimeException("water failed")
        }

        val vm = viewModel()
        advanceUntilIdle()

        vm.changeWater(1)
        advanceUntilIdle()

        assertNull(vm.uiState.value.error)
    }

    // --- addWeight ---

    @Test
    fun `addWeight persists weight entry`() = runTest(dispatcher) {
        stubHappyPath()

        val vm = viewModel()
        advanceUntilIdle()

        vm.addWeight(72.5f)
        advanceUntilIdle()

        val captor = argumentCaptor<WeightEntry>()
        verify(repository).addWeightEntry(captor.capture())
        assertEquals(72.5f, captor.firstValue.weightKg)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `addWeight failure sets error`() = runTest(dispatcher) {
        stubHappyPath()
        repository.stub {
            onBlocking { addWeightEntry(any()) } doThrow RuntimeException("weight failed")
        }

        val vm = viewModel()
        advanceUntilIdle()

        vm.addWeight(80f)
        advanceUntilIdle()

        assertEquals("weight failed", vm.uiState.value.error)
    }

    // --- deleteWeight ---

    @Test
    fun `deleteWeight delegates to repository`() = runTest(dispatcher) {
        stubHappyPath()

        val vm = viewModel()
        advanceUntilIdle()

        vm.deleteWeight("we1")
        advanceUntilIdle()

        verify(repository).deleteWeightEntry("we1")
    }

    @Test
    fun `deleteWeight failure is swallowed`() = runTest(dispatcher) {
        stubHappyPath()
        repository.stub {
            onBlocking { deleteWeightEntry(any()) } doThrow RuntimeException("nope")
        }

        val vm = viewModel()
        advanceUntilIdle()

        vm.deleteWeight("we1")
        advanceUntilIdle()

        assertNull(vm.uiState.value.error)
    }

    // --- date wiring ---

    @Test
    fun `same date is used for all date-scoped queries`() = runTest(dispatcher) {
        stubHappyPath()

        val vm = viewModel()
        advanceUntilIdle()

        val foodDate = argumentCaptor<String>()
        verify(repository).observeFoodEntries(foodDate.capture())
        val workoutDate = argumentCaptor<String>()
        verify(repository).observeWorkoutEntries(workoutDate.capture())
        val waterDate = argumentCaptor<String>()
        verify(repository).observeWater(waterDate.capture())

        assertEquals(foodDate.firstValue, workoutDate.firstValue)
        assertEquals(foodDate.firstValue, waterDate.firstValue)
        assertEquals(foodDate.firstValue, vm.uiState.value.date)
    }
}
