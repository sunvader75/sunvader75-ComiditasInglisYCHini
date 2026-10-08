package com.comiditas.familia.domain.optimizer

import com.comiditas.familia.data.model.DayAssignment
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import java.time.LocalDate
import kotlin.random.Random

class WeekMealPlanGenerator(private val optimizer: MealAssignmentOptimizer = MealAssignmentOptimizer()) {
    companion object {
        fun monday(date: LocalDate): LocalDate = date.minusDays((date.dayOfWeek.value - 1).toLong())
    }

    fun generate(selectedDate: LocalDate, members: List<FamilyMember>, meals: List<Meal>,
                 preferences: Map<Long, Set<Long>>, random: Random): List<DayAssignment>? {
        val start = monday(selectedDate)
        val rows = mutableListOf<DayAssignment>()
        repeat(7) { offset ->
            val date = start.plusDays(offset.toLong()).toString()
            val plan = optimizer.optimize(members, meals, preferences, random) ?: return null
            val day = plan.assignments.flatMap { (meal, recipients) ->
                recipients.map { DayAssignment(date, it, meal) }
            }
            if (com.comiditas.familia.domain.validation.DayAssignmentValidator.error(
                    date, day, members, meals, preferences) != null) return null
            rows.addAll(day)
        }
        return rows
    }
}
