package com.comiditas.familia.data.repository

import com.comiditas.familia.data.local.dao.MealPreferenceDao
import com.comiditas.familia.data.local.entity.MealPreferenceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealPreferenceRepository @Inject constructor(
    private val dao: MealPreferenceDao
) {
    fun getByMember(memberId: Long): Flow<Map<Long, Boolean>> = dao.getByMember(memberId).map { list ->
        list.associate { it.mealId to it.liked }
    }

    suspend fun getLikedMealIdsByMember(memberId: Long): List<Long> =
        dao.getLikedMealIdsByMember(memberId)

    suspend fun setPreference(memberId: Long, mealId: Long, liked: Boolean) {
        dao.insert(MealPreferenceEntity(memberId, mealId, liked))
    }

    suspend fun removePreference(memberId: Long, mealId: Long) {
        dao.delete(memberId, mealId)
    }

    suspend fun isLiked(memberId: Long, mealId: Long): Boolean {
        return dao.isLiked(memberId, mealId) ?: false
    }
}
