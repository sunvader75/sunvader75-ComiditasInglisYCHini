package com.comiditas.familia.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.comiditas.familia.data.local.entity.MealPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPreferenceDao {
    @Query("SELECT * FROM meal_preferences WHERE memberId = :memberId")
    fun getByMember(memberId: Long): Flow<List<MealPreferenceEntity>>

    @Query("SELECT * FROM meal_preferences WHERE mealId = :mealId")
    suspend fun getByMeal(mealId: Long): List<MealPreferenceEntity>

    @Query("SELECT * FROM meal_preferences WHERE memberId = :memberId AND liked = 1")
    suspend fun getLikedByMember(memberId: Long): List<MealPreferenceEntity>

    @Query("SELECT mealId FROM meal_preferences WHERE memberId = :memberId AND liked = 1")
    suspend fun getLikedMealIdsByMember(memberId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preference: MealPreferenceEntity)

    @Query("DELETE FROM meal_preferences WHERE memberId = :memberId AND mealId = :mealId")
    suspend fun delete(memberId: Long, mealId: Long)

    @Query("SELECT liked FROM meal_preferences WHERE memberId = :memberId AND mealId = :mealId")
    suspend fun isLiked(memberId: Long, mealId: Long): Boolean?
}
