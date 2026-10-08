package com.comiditas.familia.domain.optimizer

/** A complete day, never a per-person patch. */
data class MealReplacementProposal(
    val replacementMealId: Long,
    val assignments: Map<Long, Long>,
    val reorganizesDay: Boolean
)

object MealReplacementPlanner {
    fun plan(members: List<Long>, meals: List<Long>, liked: Map<Long, Set<Long>>,
             draft: Map<Long, Long>, oldMealId: Long): List<MealReplacementProposal> {
        val ids = members.sorted()
        val catalog = meals.toSet()
        if (ids.size !in 1..3 || ids.distinct().size != ids.size ||
            draft.keys != ids.toSet() || oldMealId !in draft.values ||
            draft.values.toSet().size > 2 || draft.any { (member, meal) ->
                meal !in catalog || meal !in liked[member].orEmpty()
            }) return emptyList()
        val available = catalog.filter { it != oldMealId }.sorted()
        val results = linkedMapOf<List<Long>, MealReplacementProposal>()
        fun add(values: List<Long>) {
            val assignments = ids.zip(values).toMap()
            val changedRecipients = ids.filter { draft[it] == oldMealId }
            val replacement = changedRecipients.map { assignments.getValue(it) }.minOrNull()!!
            val direct = changedRecipients.all { assignments[it] == replacement } &&
                ids.filter { draft[it] != oldMealId }.all { assignments[it] == draft[it] }
            results.putIfAbsent(values, MealReplacementProposal(replacement, assignments, !direct))
        }
        // At most M(M+1)/2 meal sets, with at most 2^3 assignments per set.
        fun enumerate(set: List<Long>, index: Int, values: List<Long>) {
            if (index == ids.size) {
                add(values)
                return
            }
            for (meal in set) if (meal in liked[ids[index]].orEmpty()) {
                enumerate(set, index + 1, values + meal)
            }
        }
        available.forEachIndexed { index, meal ->
            enumerate(listOf(meal), 0, emptyList())
            for (other in available.drop(index + 1)) enumerate(listOf(meal, other), 0, emptyList())
        }
        return results.values.sortedWith(Comparator { a, b ->
            val kind = a.reorganizesDay.compareTo(b.reorganizesDay)
            if (kind != 0) kind else {
                val firstDifference = ids.firstOrNull { a.assignments[it] != b.assignments[it] }
                if (firstDifference == null) 0 else a.assignments.getValue(firstDifference)
                    .compareTo(b.assignments.getValue(firstDifference))
            }
        })
    }
}
