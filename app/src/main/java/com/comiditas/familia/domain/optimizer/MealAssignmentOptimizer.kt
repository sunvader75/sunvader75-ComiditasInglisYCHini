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
        random: Random = Random.Default,
        usedMealIds: Set<Long> = emptySet()
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
        // Keep optimal options per grouping, then choose uniformly among tied groupings.
        // Shared dishes are represented once, with every recipient, never as two map keys.
        val groupings = mutableListOf<List<Map<Long, List<Long>>>>()
        var bestRepeated = Int.MAX_VALUE
        var bestCount = Int.MAX_VALUE
        fun consider(options: List<Map<Long, List<Long>>>) {
            if (options.isEmpty()) return
            val repeated = options.first().keys.count { it in usedMealIds }
            val count = options.first().size
            if (repeated < bestRepeated || repeated == bestRepeated && count < bestCount) {
                groupings.clear()
                bestRepeated = repeated
                bestCount = count
            }
            if (repeated == bestRepeated && count == bestCount) groupings.add(options)
        }
        val shared = common(ids)
        if (shared.isNotEmpty()) {
            val score = shared.minOf { if (it in usedMealIds) 1 else 0 }
            consider(shared.filter { (if (it in usedMealIds) 1 else 0) == score }
                .map { mapOf(it to ids) })
        }

        // At most three bipartitions and O(M^2) pairs each, not meal triples.
        val splits = (1 until (1 shl ids.size)).filter { it and 1 != 0 && it != (1 shl ids.size) - 1 }
        for (mask in splits) {
            val first = ids.filterIndexed { index, _ -> mask and (1 shl index) != 0 }
            val second = ids.filter { it !in first }
            val options = mutableListOf<Map<Long, List<Long>>>()
            var bestSplitRepeated = Int.MAX_VALUE
            for (firstMeal in common(first)) {
                for (secondMeal in common(second)) {
                    // Equal IDs belong to the shared grouping, preserving all recipients.
                    if (firstMeal == secondMeal) continue
                    val repeated = listOf(firstMeal, secondMeal).count { it in usedMealIds }
                    if (repeated < bestSplitRepeated) {
                        options.clear()
                        bestSplitRepeated = repeated
                    }
                    if (repeated == bestSplitRepeated) {
                        options.add(mapOf(firstMeal to first, secondMeal to second))
                    }
                }
            }
            consider(options)
        }
        if (groupings.isEmpty()) return null
        return result(groupings.random(random).random(random))
    }
}
