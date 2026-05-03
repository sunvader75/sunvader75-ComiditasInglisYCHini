package com.comiditas.familia.domain.usecase

import com.comiditas.familia.data.model.Meal
import com.comiditas.familia.data.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMealsUseCase @Inject constructor(
    private val repository: MealRepository
) {
    operator fun invoke(): Flow<List<Meal>> = repository.getAll()
}
