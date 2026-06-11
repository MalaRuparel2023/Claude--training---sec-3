package com.mr.claudetraining.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.mr.claudetraining.data.model.ExerciseItemDto
import com.mr.claudetraining.data.model.FoodEntryDto
import com.mr.claudetraining.data.model.FoodItemDto
import com.mr.claudetraining.data.model.NutritionGoalDto
import com.mr.claudetraining.data.model.WaterLogDto
import com.mr.claudetraining.data.model.WeightEntryDto
import com.mr.claudetraining.data.model.WorkoutEntryDto
import com.mr.claudetraining.data.model.toDomain
import com.mr.claudetraining.data.model.toDto
import com.mr.claudetraining.domain.model.ExerciseItem
import com.mr.claudetraining.domain.model.FoodEntry
import com.mr.claudetraining.domain.model.FoodItem
import com.mr.claudetraining.domain.model.NutritionGoal
import com.mr.claudetraining.domain.model.WaterLog
import com.mr.claudetraining.domain.model.WeightEntry
import com.mr.claudetraining.domain.model.WorkoutEntry
import com.mr.claudetraining.domain.repository.HealthRepository
import javax.inject.Inject

class HealthRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : HealthRepository {

    private val foodCatalogRef = firestore.collection("foodCatalog")
    private val exerciseCatalogRef = firestore.collection("exerciseCatalog")
    private val userRef = firestore.collection("users").document(DEMO_USER_ID)
    private val foodEntriesRef = userRef.collection("foodEntries")
    private val workoutEntriesRef = userRef.collection("workoutEntries")
    private val waterLogsRef = userRef.collection("waterLogs")
    private val weightEntriesRef = userRef.collection("weightEntries")
    private val goalRef = userRef.collection("goal").document("current")

    override fun observeFoodCatalog(): Flow<List<FoodItem>> =
        collectionFlow(foodCatalogRef.orderBy("name"), FoodItemDto::class.java) { it.toDomain() }

    override fun observeExerciseCatalog(): Flow<List<ExerciseItem>> =
        collectionFlow(exerciseCatalogRef.orderBy("name"), ExerciseItemDto::class.java) { it.toDomain() }

    override fun observeGoal(): Flow<NutritionGoal> = callbackFlow {
        val reg = goalRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val goal = snapshot?.toObject(NutritionGoalDto::class.java)?.toDomain()
                ?: NutritionGoal()
            trySend(goal)
        }
        awaitClose { reg.remove() }
    }

    override fun observeFoodEntries(date: String): Flow<List<FoodEntry>> =
        collectionFlow(foodEntriesRef.whereEqualTo("date", date), FoodEntryDto::class.java) { it.toDomain() }

    override suspend fun addFoodEntry(entry: FoodEntry) {
        val doc = foodEntriesRef.document()
        doc.set(entry.copy(id = doc.id).toDto()).await()
    }

    override suspend fun deleteFoodEntry(id: String) {
        foodEntriesRef.document(id).delete().await()
    }

    override fun observeWorkoutEntries(date: String): Flow<List<WorkoutEntry>> =
        collectionFlow(workoutEntriesRef.whereEqualTo("date", date), WorkoutEntryDto::class.java) { it.toDomain() }

    override suspend fun addWorkoutEntry(entry: WorkoutEntry) {
        val doc = workoutEntriesRef.document()
        doc.set(entry.copy(id = doc.id).toDto()).await()
    }

    override suspend fun deleteWorkoutEntry(id: String) {
        workoutEntriesRef.document(id).delete().await()
    }

    override fun observeWater(date: String): Flow<WaterLog> = callbackFlow {
        val reg = waterLogsRef.document(date).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val log = snapshot?.toObject(WaterLogDto::class.java)?.toDomain()
                ?: WaterLog(date = date, glasses = 0)
            trySend(log)
        }
        awaitClose { reg.remove() }
    }

    override suspend fun setWater(date: String, glasses: Int) {
        waterLogsRef.document(date).set(WaterLogDto(date, glasses.coerceAtLeast(0))).await()
    }

    override fun observeWeightEntries(): Flow<List<WeightEntry>> =
        collectionFlow(
            weightEntriesRef.orderBy("date", Query.Direction.ASCENDING),
            WeightEntryDto::class.java
        ) { it.toDomain() }

    override suspend fun addWeightEntry(entry: WeightEntry) {
        val doc = weightEntriesRef.document()
        doc.set(entry.copy(id = doc.id).toDto()).await()
    }

    override suspend fun deleteWeightEntry(id: String) {
        weightEntriesRef.document(id).delete().await()
    }

    override suspend fun seedCatalogsIfEmpty() {
        if (foodCatalogRef.limit(1).get().await().isEmpty) {
            sampleFoods.forEach { food ->
                val doc = foodCatalogRef.document()
                doc.set(food.copy(id = doc.id)).await()
            }
        }
        if (exerciseCatalogRef.limit(1).get().await().isEmpty) {
            sampleExercises.forEach { ex ->
                val doc = exerciseCatalogRef.document()
                doc.set(ex.copy(id = doc.id)).await()
            }
        }
        if (!goalRef.get().await().exists()) goalRef.set(NutritionGoalDto()).await()
    }

    private fun <D : Any, T> collectionFlow(
        query: Query,
        type: Class<D>,
        map: (D) -> T
    ): Flow<List<T>> = callbackFlow {
        val reg = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(type)?.let(map)
            } ?: emptyList()
            trySend(items)
        }
        awaitClose { reg.remove() }
    }

    private companion object {
        const val DEMO_USER_ID = "tutorial-demi"

        val sampleFoods = listOf(
            FoodItemDto(name = "Banana", servingLabel = "1 medium", calories = 105, protein = 1, carbs = 27, fat = 0),
            FoodItemDto(name = "Boiled Egg", servingLabel = "1 large", calories = 78, protein = 6, carbs = 1, fat = 5),
            FoodItemDto(name = "Chicken Breast", servingLabel = "100 g", calories = 165, protein = 31, carbs = 0, fat = 4),
            FoodItemDto(name = "White Rice", servingLabel = "1 cup cooked", calories = 205, protein = 4, carbs = 45, fat = 0),
            FoodItemDto(name = "Roti", servingLabel = "1 piece", calories = 120, protein = 3, carbs = 18, fat = 4),
            FoodItemDto(name = "Dal (Lentils)", servingLabel = "1 cup", calories = 230, protein = 18, carbs = 40, fat = 1),
            FoodItemDto(name = "Paneer", servingLabel = "100 g", calories = 265, protein = 18, carbs = 6, fat = 20),
            FoodItemDto(name = "Greek Yogurt", servingLabel = "1 cup", calories = 100, protein = 17, carbs = 6, fat = 1),
            FoodItemDto(name = "Almonds", servingLabel = "10 pieces", calories = 70, protein = 3, carbs = 2, fat = 6),
            FoodItemDto(name = "Apple", servingLabel = "1 medium", calories = 95, protein = 0, carbs = 25, fat = 0),
            FoodItemDto(name = "Oats", servingLabel = "1/2 cup dry", calories = 150, protein = 5, carbs = 27, fat = 3),
            FoodItemDto(name = "Whey Protein", servingLabel = "1 scoop", calories = 120, protein = 24, carbs = 3, fat = 1)
        )

        val sampleExercises = listOf(
            ExerciseItemDto(name = "Running", category = "Cardio", caloriesPerMinute = 11),
            ExerciseItemDto(name = "Walking", category = "Cardio", caloriesPerMinute = 4),
            ExerciseItemDto(name = "Cycling", category = "Cardio", caloriesPerMinute = 8),
            ExerciseItemDto(name = "Swimming", category = "Cardio", caloriesPerMinute = 10),
            ExerciseItemDto(name = "Weight Training", category = "Strength", caloriesPerMinute = 6),
            ExerciseItemDto(name = "HIIT", category = "Cardio", caloriesPerMinute = 13),
            ExerciseItemDto(name = "Jump Rope", category = "Cardio", caloriesPerMinute = 12),
            ExerciseItemDto(name = "Yoga", category = "Yoga", caloriesPerMinute = 4),
            ExerciseItemDto(name = "Sun Salutation", category = "Yoga", caloriesPerMinute = 7),
            ExerciseItemDto(name = "Downward Dog", category = "Yoga", caloriesPerMinute = 4),
            ExerciseItemDto(name = "Warrior Pose", category = "Yoga", caloriesPerMinute = 5),
            ExerciseItemDto(name = "Tree Pose", category = "Yoga", caloriesPerMinute = 3),
            ExerciseItemDto(name = "Cobra Pose", category = "Yoga", caloriesPerMinute = 3),
            ExerciseItemDto(name = "Child's Pose", category = "Yoga", caloriesPerMinute = 2),
            ExerciseItemDto(name = "Bridge Pose", category = "Yoga", caloriesPerMinute = 4),
            ExerciseItemDto(name = "Triangle Pose", category = "Yoga", caloriesPerMinute = 4),
            ExerciseItemDto(name = "Plank Pose", category = "Yoga", caloriesPerMinute = 5),
            ExerciseItemDto(name = "Seated Forward Bend", category = "Yoga", caloriesPerMinute = 3),
            ExerciseItemDto(name = "Cat-Cow Stretch", category = "Yoga", caloriesPerMinute = 3),
            ExerciseItemDto(name = "Pigeon Pose", category = "Yoga", caloriesPerMinute = 4),
            ExerciseItemDto(name = "Boat Pose", category = "Yoga", caloriesPerMinute = 5),
            ExerciseItemDto(name = "Camel Pose", category = "Yoga", caloriesPerMinute = 4),
            ExerciseItemDto(name = "Lotus Pose", category = "Yoga", caloriesPerMinute = 2),
            ExerciseItemDto(name = "Chair Pose", category = "Yoga", caloriesPerMinute = 5),
            ExerciseItemDto(name = "Corpse Pose", category = "Yoga", caloriesPerMinute = 1)
        )
    }
}