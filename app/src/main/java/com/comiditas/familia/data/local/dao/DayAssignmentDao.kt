package com.comiditas.familia.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.comiditas.familia.data.local.entity.DayAssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayAssignmentDao {
    @Query("SELECT * FROM day_assignments WHERE date = :date")
    suspend fun getByDate(date: String): List<DayAssignmentEntity>

    @Query("SELECT * FROM day_assignments WHERE date = :date")
    fun getByDateFlow(date: String): Flow<List<DayAssignmentEntity>>

    @Query("SELECT * FROM day_assignments WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    fun getBetweenDates(startDate: String, endDate: String): Flow<List<DayAssignmentEntity>>

    @Query("SELECT * FROM day_assignments WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    fun getBetweenDatesFlow(startDate: String, endDate: String): Flow<List<DayAssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assignment: DayAssignmentEntity)

    @Query("DELETE FROM day_assignments WHERE date = :date AND memberId = :memberId")
    suspend fun delete(date: String, memberId: Long)

    @Query("DELETE FROM day_assignments WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("SELECT * FROM day_assignments WHERE memberId = :memberId AND mealId = :mealId AND date = :date")
    suspend fun exists(memberId: Long, mealId: Long, date: String): DayAssignmentEntity?
}
