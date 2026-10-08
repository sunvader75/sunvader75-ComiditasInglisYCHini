package com.comiditas.familia.ui.screens.calendar

import com.comiditas.familia.data.model.DayAssignment
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import com.comiditas.familia.domain.optimizer.WeekMealPlanGenerator
import java.time.LocalDate

data class WeekMenuDish(val mealId: Long, val name: String, val recipients: List<String>)
data class WeekMenuDay(val date: LocalDate, val dishes: List<WeekMenuDish>)

object WeekMenuPresentation {
    fun start(date: LocalDate): LocalDate = WeekMealPlanGenerator.monday(date)
    fun navigate(date: LocalDate, weeks: Long): LocalDate = start(date).plusDays(weeks * 7)

    fun days(start: LocalDate, assignments: Map<LocalDate, List<DayAssignment>>,
             members: List<FamilyMember>, meals: List<Meal>): List<WeekMenuDay> {
        val memberNames = members.associate { it.id to it.name }
        val mealNames = meals.associate { it.id to it.name }
        return (0L..6L).map { offset ->
            val date = start(start).plusDays(offset)
            WeekMenuDay(date, assignments[date].orEmpty().groupBy { it.mealId }.map { (id, rows) ->
                WeekMenuDish(id, mealNames[id]?.takeIf { it.isNotBlank() } ?: "Comida #$id",
                    rows.map { it.memberId }.distinct().map { member ->
                        memberNames[member]?.takeIf { it.isNotBlank() } ?: "Miembro #$member"
                    })
            })
        }
    }
}
