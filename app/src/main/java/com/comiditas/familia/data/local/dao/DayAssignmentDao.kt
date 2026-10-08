package com.comiditas.familia.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(assignments: List<DayAssignmentEntity>)

    @Transaction
    suspend fun replaceDay(date: String, assignments: List<DayAssignmentEntity>) {
        require(assignments.all { it.date == date })
        deleteByDate(date)
        insertAll(assignments)
    }

    @Query("SELECT COUNT(*) FROM day_assignments WHERE date BETWEEN :startDate AND :endDate")
    suspend fun countBetween(startDate: String, endDate: String): Int

    @Query("DELETE FROM day_assignments WHERE date BETWEEN :startDate AND :endDate")
    suspend fun deleteBetween(startDate: String, endDate: String)

    @Transaction
    suspend fun replaceWeek(startDate: String, endDate: String,
                            assignments: List<DayAssignmentEntity>, confirmed: Boolean): Boolean {
        val start = java.time.LocalDate.parse(startDate)
        val end = java.time.LocalDate.parse(endDate)
        require(start.toString() == startDate && end.toString() == endDate)
        require(start.dayOfWeek == java.time.DayOfWeek.MONDAY && end == start.plusDays(6))
        val dates = (0L..6L).map { start.plusDays(it).toString() }.toSet()
        require(assignments.map { it.date }.toSet() == dates)
        require(assignments.map { it.date to it.memberId }.distinct().size == assignments.size)
        val days = assignments.groupBy { it.date }.values
        val members = days.first().map { it.memberId }.toSet()
        require(members.size in 1..3 && days.all { day ->
            day.map { it.memberId }.toSet() == members && day.map { it.mealId }.distinct().size <= 2
        })
        if (!confirmed && countBetween(startDate, endDate) > 0) return false
        deleteBetween(startDate, endDate)
        insertAll(assignments)
        return true
    }

    @Query("DELETE FROM day_assignments WHERE date = :date AND memberId = :memberId")
    suspend fun delete(date: String, memberId: Long)

    @Query("DELETE FROM day_assignments WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("SELECT * FROM day_assignments WHERE memberId = :memberId AND mealId = :mealId AND date = :date")
    suspend fun exists(memberId: Long, mealId: Long, date: String): DayAssignmentEntity?
}
