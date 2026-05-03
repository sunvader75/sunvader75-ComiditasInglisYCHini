package com.comiditas.familia.domain.usecase

import com.comiditas.familia.data.model.DayAssignment
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import com.comiditas.familia.data.repository.DayAssignmentRepository
import com.comiditas.familia.data.repository.FamilyMemberRepository
import com.comiditas.familia.data.repository.MealPreferenceRepository
import com.comiditas.familia.data.repository.MealRepository
import com.comiditas.familia.domain.optimizer.MealAssignmentOptimizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.random.Random

class AssignMealsUseCase @Inject constructor(
    private val dayAssignmentRepository: DayAssignmentRepository,
    private val memberRepository: FamilyMemberRepository,
    private val mealRepository: MealRepository,
    private val preferenceRepository: MealPreferenceRepository,
    private val optimizer: MealAssignmentOptimizer
) {
    fun getAssignments(date: String): Flow<List<DayAssignment>> =
        dayAssignmentRepository.getByDateFlow(date)

    fun getAssignmentsBetween(startDate: String, endDate: String): Flow<List<DayAssignment>> =
        dayAssignmentRepository.getBetweenDates(startDate, endDate)

    suspend fun assignManually(date: String, memberId: Long, mealId: Long) {
        dayAssignmentRepository.assign(date, memberId, mealId)
    }

    suspend fun removeAssignment(date: String, memberId: Long) {
        dayAssignmentRepository.removeAssignment(date, memberId)
    }

    suspend fun clearDay(date: String) {
        dayAssignmentRepository.clearDate(date)
    }

    /**
     * Genera una asignación aleatoria óptima para el día dado.
     * @return true si se pudo asignar, false si no hay solución posible
     */
    suspend fun assignRandomly(date: String): Boolean {
        val members = memberRepository.getAll().first()
        val meals = mealRepository.getAll().first()

        if (members.isEmpty() || meals.isEmpty()) return false

        // Obtener preferencias de todos los miembros
        val preferences = mutableMapOf<Long, Set<Long>>()
        for (member in members) {
            val likedIds = preferenceRepository.getLikedMealIdsByMember(member.id)
            preferences[member.id] = likedIds.toSet()
        }

        val assignment = optimizer.optimize(
            members = members,
            meals = meals,
            preferences = preferences,
            random = Random(System.currentTimeMillis())
        )

        if (assignment == null) return false

        // Limpiar asignaciones previas
        dayAssignmentRepository.clearDate(date)

        // Aplicar nuevas asignaciones
        for ((mealId, memberIds) in assignment.assignments) {
            for (memberId in memberIds) {
                dayAssignmentRepository.assign(date, memberId, mealId)
            }
        }

        return true
    }
}
