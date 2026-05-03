package com.comiditas.familia.domain.usecase

import com.comiditas.familia.data.model.Meal
import com.comiditas.familia.data.repository.MealRepository
import javax.inject.Inject

class SaveMealUseCase @Inject constructor(
    private val repository: MealRepository
) {
    suspend operator fun invoke(meal: Meal): Long {
        return repository.save(meal)
    }
}
