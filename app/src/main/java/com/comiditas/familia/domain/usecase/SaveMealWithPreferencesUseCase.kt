package com.comiditas.familia.domain.usecase

import androidx.room.withTransaction
import com.comiditas.familia.data.local.AppDatabase
import com.comiditas.familia.data.model.Meal
import com.comiditas.familia.data.repository.MealPreferenceRepository
import com.comiditas.familia.data.repository.MealRepository
import javax.inject.Inject

class SaveMealWithPreferencesUseCase @Inject constructor(
    private val database: AppDatabase,
    private val meals: MealRepository,
    private val preferences: MealPreferenceRepository
) {
    suspend operator fun invoke(mealId: Long?, name: String, likedMemberIds: Set<Long>): Long {
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "El nombre de la comida es obligatorio." }
        return database.withTransaction {
            // Read inside the transaction: editing must retain the stored legacy description.
            val meal = if (mealId == null) Meal(name = trimmedName) else {
                requireNotNull(meals.getById(mealId)) { "La comida ya no existe." }
                    .copy(name = trimmedName)
            }
            val savedId = meals.save(meal)
            preferences.replaceLikes(savedId, likedMemberIds)
            savedId
        }
    }
}
