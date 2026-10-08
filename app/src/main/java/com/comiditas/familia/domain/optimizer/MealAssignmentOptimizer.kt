package com.comiditas.familia.domain.optimizer

import com.comiditas.familia.data.model.DayAssignment
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import com.comiditas.familia.domain.validation.DayAssignmentValidator
import javax.inject.Inject
import kotlin.random.Random

data class OptimizedAssignment(
    val assignments: Map<Long, List<Long>>,
    val mealDetails: Map<Long, Meal>
) {
    val numberOfMeals: Int get() = assignments.size
}

class MealAssignmentOptimizer @Inject constructor() {
    fun optimize(
        members: List<FamilyMember>,
        meals: List<Meal>,
        preferences: Map<Long, Set<Long>>,
        random: Random = Random.Default
    ): OptimizedAssignment? {
        if (members.size !in 1..3 || members.map { it.id }.distinct().size != members.size) return null
        val ids = members.map { it.id }
        fun common(group: List<Long>) = meals.map { it.id }.distinct().filter { meal ->
            group.all { meal in preferences[it].orEmpty() }
        }
        fun result(groups: Map<Long, List<Long>>): OptimizedAssignment? {
            val rows = groups.flatMap { (meal, recipients) -> recipients.map { DayAssignment("", it, meal) } }
            if (DayAssignmentValidator.error("", rows, members, meals, preferences) != null) return null
            return OptimizedAssignment(groups, meals.associateBy { it.id })
        }
        val shared = common(ids)
        if (shared.isNotEmpty()) return result(mapOf(shared.random(random) to ids))

        // At most three bipartitions; no Cartesian enumeration over meal triples.
        val splits = (1 until (1 shl ids.size)).filter { it and 1 != 0 && it != (1 shl ids.size) - 1 }
            .shuffled(random)
        for (mask in splits) {
            val first = ids.filterIndexed { index, _ -> mask and (1 shl index) != 0 }
            val second = ids.filter { it !in first }
            val firstMeals = common(first)
            val secondMeals = common(second)
            if (firstMeals.isNotEmpty() && secondMeals.isNotEmpty()) {
                // With no global common meal these sets are disjoint.
                return result(mapOf(firstMeals.random(random) to first, secondMeals.random(random) to second))
            }
        }
        return null
    }
}
