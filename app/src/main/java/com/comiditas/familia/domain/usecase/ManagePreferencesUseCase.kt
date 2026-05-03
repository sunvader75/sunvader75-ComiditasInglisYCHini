package com.comiditas.familia.domain.usecase

import com.comiditas.familia.data.repository.MealPreferenceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManagePreferencesUseCase @Inject constructor(
    private val repository: MealPreferenceRepository
) {
    fun getPreferences(memberId: Long): Flow<Map<Long, Boolean>> =
        repository.getByMember(memberId)

    suspend fun setPreference(memberId: Long, mealId: Long, liked: Boolean) {
        repository.setPreference(memberId, mealId, liked)
    }

    suspend fun removePreference(memberId: Long, mealId: Long) {
        repository.removePreference(memberId, mealId)
    }

    suspend fun isLiked(memberId: Long, mealId: Long): Boolean =
        repository.isLiked(memberId, mealId)

    suspend fun getLikedMealIds(memberId: Long): List<Long> =
        repository.getLikedMealIdsByMember(memberId)
}
