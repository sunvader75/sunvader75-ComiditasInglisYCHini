package com.comiditas.familia.domain.validation

import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal

/** An immutable explanation derived exclusively from an operation's snapshot. */
object MealPlanDiagnostics {
    enum class Reason { NO_MEMBERS, UNSUPPORTED_MEMBERS, EMPTY_CATALOG, NO_ELIGIBLE_LIKES, INCOMPATIBLE }
    data class Diagnostic(val reason: Reason, val affectedMemberIds: List<Long> = emptyList())

    fun diagnose(members: List<FamilyMember>, meals: List<Meal>,
                 likes: Map<Long, Set<Long>>): Diagnostic? {
        if (members.isEmpty()) return Diagnostic(Reason.NO_MEMBERS)
        if (members.size !in 1..3 || members.map { it.id }.distinct().size != members.size)
            return Diagnostic(Reason.UNSUPPORTED_MEMBERS)
        if (meals.isEmpty()) return Diagnostic(Reason.EMPTY_CATALOG)
        val catalog = meals.map { it.id }.toSet()
        val eligible = members.sortedBy { it.id }.associate { it.id to likes[it.id].orEmpty().intersect(catalog) }
        val unavailable = eligible.filterValues { it.isEmpty() }.keys.toList()
        if (unavailable.isNotEmpty()) return Diagnostic(Reason.NO_ELIGIBLE_LIKES, unavailable)
        // With three people, a shared dish for any pair plus the third person's dish suffices.
        val sets = eligible.values.toList()
        if (sets.size == 3 && sets.indices.all { a ->
                (a + 1 until sets.size).all { b -> sets[a].intersect(sets[b]).isEmpty() }
            }) return Diagnostic(Reason.INCOMPATIBLE)
        return null
    }

    fun message(diagnostic: Diagnostic, members: List<FamilyMember>): String = when (diagnostic.reason) {
        Reason.NO_MEMBERS -> "No hay personas en la familia. Añade entre una y tres personas."
        Reason.UNSUPPORTED_MEMBERS -> "El plan requiere entre 1 y 3 personas distintas. Revisa las personas de la familia."
        Reason.EMPTY_CATALOG -> "No hay comidas disponibles. Añade comidas y configura los gustos."
        Reason.NO_ELIGIBLE_LIKES -> {
            val names = members.sortedBy { it.id }.filter { it.id in diagnostic.affectedMemberIds }
                .joinToString(", ") { it.name }
            "$names: sin comidas disponibles que les gusten. Añade comidas o revisa sus gustos."
        }
        Reason.INCOMPATIBLE -> "No hay un plan de hasta dos comidas que guste a todos. Revisa las comidas y los gustos compartidos: al menos dos personas deben poder compartir una comida."
    }

    fun replacementMessage(diagnostic: Diagnostic, members: List<FamilyMember>, oldMeal: Meal): String {
        val detail = if (diagnostic.reason == Reason.NO_ELIGIBLE_LIKES) {
            val names = members.sortedBy { it.id }.filter { it.id in diagnostic.affectedMemberIds }
                .joinToString(", ") { it.name }
            if (diagnostic.affectedMemberIds.size == 1) "$names no tiene otra comida disponible que le guste."
            else "$names no tienen otra comida disponible que les guste."
        } else message(diagnostic, members)
        return "No se puede sustituir «${oldMeal.name}»: $detail Revisa las comidas y los gustos. El plan guardado no se modifica."
    }
}

class ExplainedMealPlanException(message: String) : IllegalArgumentException(message)
