package com.comiditas.familia.domain.validation

import com.comiditas.familia.data.model.DayAssignment
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal

/** Pure validation shared by generation and every complete-plan write. */
object DayAssignmentValidator {
    fun error(
        date: String,
        assignments: List<DayAssignment>,
        members: List<FamilyMember>,
        meals: List<Meal>,
        preferences: Map<Long, Set<Long>>
    ): String? {
        val memberIds = members.map { it.id }.toSet()
        val mealIds = meals.map { it.id }.toSet()
        return when {
            members.size !in 1..3 || memberIds.size != members.size ->
                "El plan requiere entre 1 y 3 personas distintas."
            assignments.any { it.date != date } -> "Las asignaciones deben ser del mismo día."
            assignments.size != members.size || assignments.map { it.memberId }.toSet() != memberIds ->
                "Asigna exactamente una comida a cada persona."
            assignments.any { it.mealId !in mealIds } -> "Una comida seleccionada ya no existe."
            assignments.any { it.mealId !in preferences[it.memberId].orEmpty() } ->
                "Cada persona debe recibir una comida que le guste."
            assignments.map { it.mealId }.toSet().size > 2 -> "Elige como máximo dos comidas distintas."
            else -> null
        }
    }
}
