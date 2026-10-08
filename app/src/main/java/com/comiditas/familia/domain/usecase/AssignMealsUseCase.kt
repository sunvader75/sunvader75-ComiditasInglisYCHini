package com.comiditas.familia.domain.usecase

import com.comiditas.familia.data.model.DayAssignment
import com.comiditas.familia.data.repository.DayAssignmentRepository
import com.comiditas.familia.data.repository.FamilyMemberRepository
import com.comiditas.familia.data.repository.MealPreferenceRepository
import com.comiditas.familia.data.repository.MealRepository
import com.comiditas.familia.domain.optimizer.MealAssignmentOptimizer
import com.comiditas.familia.domain.validation.DayAssignmentValidator
import com.comiditas.familia.domain.validation.MealPlanDiagnostics
import com.comiditas.familia.domain.validation.ExplainedMealPlanException
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
    fun getAssignments(date: String): Flow<List<DayAssignment>> = dayAssignmentRepository.getByDateFlow(date)
    fun getAssignmentsBetween(startDate: String, endDate: String): Flow<List<DayAssignment>> =
        dayAssignmentRepository.getBetweenDates(startDate, endDate)

    enum class WeekResult { SAVED, CONFIRMATION_REQUIRED, IMPOSSIBLE }

    suspend fun weekHasAssignments(selectedDate: java.time.LocalDate): Boolean {
        val start = com.comiditas.familia.domain.optimizer.WeekMealPlanGenerator.monday(selectedDate)
        return dayAssignmentRepository.hasAssignmentsBetween(start.toString(), start.plusDays(6).toString())
    }

    suspend fun generateWeek(selectedDate: java.time.LocalDate, confirmed: Boolean,
                             random: Random = Random.Default): WeekResult {
        val members = memberRepository.getAll().first()
        val meals = mealRepository.getAll().first()
        val preferences = members.associate { it.id to preferenceRepository.getLikedMealIdsByMember(it.id).toSet() }
        MealPlanDiagnostics.diagnose(members, meals, preferences)?.let {
            throw ExplainedMealPlanException("No se puede generar la semana. " +
                MealPlanDiagnostics.message(it, members) + " El plan guardado no se modifica.")
        }
        val rows = com.comiditas.familia.domain.optimizer.WeekMealPlanGenerator(optimizer)
            .generate(selectedDate, members, meals, preferences, random) ?: return WeekResult.IMPOSSIBLE
        val start = com.comiditas.familia.domain.optimizer.WeekMealPlanGenerator.monday(selectedDate)
        return if (dayAssignmentRepository.replaceWeek(start.toString(), start.plusDays(6).toString(), rows, confirmed))
            WeekResult.SAVED else WeekResult.CONFIRMATION_REQUIRED
    }

    suspend fun assignManually(date: String, memberId: Long, mealId: Long) {
        val current = dayAssignmentRepository.getByDate(date)
        saveDay(date, current.filterNot { it.memberId == memberId } + DayAssignment(date, memberId, mealId))
    }

    suspend fun removeAssignment(date: String, memberId: Long) {
        saveDay(date, dayAssignmentRepository.getByDate(date).filterNot { it.memberId == memberId })
    }

    suspend fun saveDay(date: String, assignments: List<DayAssignment>) {
        val members = memberRepository.getAll().first()
        val meals = mealRepository.getAll().first()
        val preferences = members.associate { it.id to preferenceRepository.getLikedMealIdsByMember(it.id).toSet() }
        val error = DayAssignmentValidator.error(date, assignments, members, meals, preferences)
        require(error == null) { error ?: "Plan inválido" }
        dayAssignmentRepository.replaceDay(date, assignments)
    }

    suspend fun replacementProposals(draft: Map<Long, Long>, oldMealId: Long):
        List<com.comiditas.familia.domain.optimizer.MealReplacementProposal> {
        val members = memberRepository.getAll().first()
        val meals = mealRepository.getAll().first()
        val preferences = members.associate { it.id to preferenceRepository.getLikedMealIdsByMember(it.id).toSet() }
        val error = DayAssignmentValidator.error("draft", draft.map { (member, meal) ->
            DayAssignment("draft", member, meal)
        }, members, meals, preferences)
        if (error != null) throw ExplainedMealPlanException(
            "$error Revisa el borrador antes de sustituir. El plan guardado no se modifica.")
        if (oldMealId !in draft.values) throw ExplainedMealPlanException(
            "La comida que quieres sustituir no está en el borrador. El plan guardado no se modifica.")
        val oldMeal = meals.first { it.id == oldMealId }
        MealPlanDiagnostics.diagnose(members, meals.filterNot { it.id == oldMealId }, preferences)?.let {
            throw ExplainedMealPlanException(MealPlanDiagnostics.replacementMessage(it, members, oldMeal))
        }
        return com.comiditas.familia.domain.optimizer.MealReplacementPlanner.plan(
            members.map { it.id }, meals.map { it.id }, preferences, draft, oldMealId)
    }

    suspend fun clearDay(date: String) = dayAssignmentRepository.clearDate(date)

    suspend fun assignRandomly(date: String): Boolean {
        val members = memberRepository.getAll().first()
        val meals = mealRepository.getAll().first()
        val preferences = members.associate { it.id to preferenceRepository.getLikedMealIdsByMember(it.id).toSet() }
        MealPlanDiagnostics.diagnose(members, meals, preferences)?.let {
            throw ExplainedMealPlanException("No se puede generar el día. " +
                MealPlanDiagnostics.message(it, members) + " El plan guardado no se modifica.")
        }
        val assignment = optimizer.optimize(members, meals, preferences, Random(System.currentTimeMillis())) ?: return false
        saveDay(date, assignment.assignments.flatMap { (mealId, memberIds) ->
            memberIds.map { DayAssignment(date, it, mealId) }
        })
        return true
    }
}
