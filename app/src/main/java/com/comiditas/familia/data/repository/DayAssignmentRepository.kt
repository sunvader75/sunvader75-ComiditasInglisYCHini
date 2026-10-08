package com.comiditas.familia.data.repository

import com.comiditas.familia.data.local.dao.DayAssignmentDao
import com.comiditas.familia.data.local.entity.DayAssignmentEntity
import com.comiditas.familia.data.model.DayAssignment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DayAssignmentRepository @Inject constructor(
    private val dao: DayAssignmentDao
) {
    suspend fun getByDate(date: String): List<DayAssignment> =
        dao.getByDate(date).map { it.toModel() }

    fun getByDateFlow(date: String): Flow<List<DayAssignment>> =
        dao.getByDateFlow(date).map { list -> list.map { it.toModel() } }

    fun getBetweenDates(startDate: String, endDate: String): Flow<List<DayAssignment>> =
        dao.getBetweenDatesFlow(startDate, endDate).map { list -> list.map { it.toModel() } }

    suspend fun replaceDay(date: String, assignments: List<DayAssignment>) {
        dao.replaceDay(date, assignments.map { DayAssignmentEntity(it.date, it.memberId, it.mealId) })
    }

    suspend fun hasAssignmentsBetween(startDate: String, endDate: String): Boolean =
        dao.countBetween(startDate, endDate) > 0

    suspend fun replaceWeek(startDate: String, endDate: String,
                            assignments: List<DayAssignment>, confirmed: Boolean): Boolean =
        dao.replaceWeek(startDate, endDate,
            assignments.map { DayAssignmentEntity(it.date, it.memberId, it.mealId) }, confirmed)

    suspend fun assign(date: String, memberId: Long, mealId: Long) {
        dao.insert(DayAssignmentEntity(date, memberId, mealId))
    }

    suspend fun removeAssignment(date: String, memberId: Long) {
        dao.delete(date, memberId)
    }

    suspend fun clearDate(date: String) {
        dao.deleteByDate(date)
    }

    private fun DayAssignmentEntity.toModel() = DayAssignment(
        date = date,
        memberId = memberId,
        mealId = mealId
    )
}
