package com.comiditas.familia.domain.optimizer

import org.junit.Assert.*
import org.junit.Test

class MealReplacementPlannerTest {
    private val members = listOf(1L, 2L, 3L)
    private val meals = listOf(10L, 20L, 30L, 40L)
    private val draft = linkedMapOf(1L to 10L, 2L to 10L, 3L to 20L)
    private val liked = mapOf(1L to setOf(10L, 30L), 2L to setOf(10L, 30L, 40L),
        3L to setOf(20L, 40L))
    private fun plan(preferences: Map<Long, Set<Long>> = liked) =
        MealReplacementPlanner.plan(members, meals, preferences, draft, 10)

    @Test fun directSwapPreservesRecipientsAndOtherDish() {
        val first = plan().first()
        assertEquals(mapOf(1L to 30L, 2L to 30L, 3L to 20L), first.assignments)
        assertEquals(30L, first.replacementMealId)
        assertFalse(first.reorganizesDay)
    }

    @Test fun forcedReorganizationChangesBothDishes() {
        val options = plan(liked + (2L to setOf(10L, 40L)))
        assertTrue(options.isNotEmpty())
        assertTrue(options.all { it.reorganizesDay })
        assertTrue(options.any { it.assignments == mapOf(1L to 30L, 2L to 40L, 3L to 40L) })
    }

    @Test fun everyProposalIsCompleteLikedAndExcludesOldDish() {
        assertTrue(plan().isNotEmpty())
        for (option in plan()) {
            assertEquals(members.toSet(), option.assignments.keys)
            assertFalse(option.assignments.containsValue(10L))
            assertTrue(option.assignments.values.toSet().size <= 2)
            assertTrue(option.assignments.containsValue(option.replacementMealId))
            option.assignments.forEach { (member, meal) ->
                assertTrue(meal in meals)
                assertTrue(meal in liked.getValue(member))
            }
        }
    }

    @Test fun impossibleAndNoAlternativesReturnEmpty() {
        assertTrue(plan(liked + (1L to setOf(10L))).isEmpty())
        assertTrue(MealReplacementPlanner.plan(listOf(1L), listOf(10L),
            mapOf(1L to setOf(10L)), mapOf(1L to 10L), 10).isEmpty())
    }

    @Test fun sharedAndSingleMemberPlans() {
        for (ids in listOf(listOf(1L), listOf(1L, 2L))) {
            val options = MealReplacementPlanner.plan(ids, meals,
                ids.associateWith { setOf(10L, 30L) }, ids.associateWith { 10L }, 10)
            assertEquals(1, options.size)
            assertEquals(ids.associateWith { 30L }, options.single().assignments)
            assertFalse(options.single().reorganizesDay)
        }
    }

    @Test fun duplicatesReorderedInputsAndInputImmutability() {
        val original = draft.toMap()
        val options = plan()
        assertEquals(options.size, options.map { it.assignments }.distinct().size)
        assertEquals(options, MealReplacementPlanner.plan(members.reversed(), meals.reversed() + meals,
            liked.entries.reversed().associate { it.toPair() },
            draft.entries.reversed().associate { it.toPair() }, 10))
        assertEquals(original, draft)
    }

    @Test fun invalidInitialDraftNeverInventsChanges() {
        val invalid = listOf(emptyMap(), draft - 1L, draft + (4L to 10L),
            draft + (1L to 999L), mapOf(1L to 10L, 2L to 30L, 3L to 20L))
        for (input in invalid) assertTrue(MealReplacementPlanner.plan(members, meals, liked, input, 10).isEmpty())
        assertTrue(MealReplacementPlanner.plan(members, meals, liked, draft, 999).isEmpty())
        assertTrue(MealReplacementPlanner.plan(members + 1L, meals, liked, draft, 10).isEmpty())
        assertTrue(MealReplacementPlanner.plan(emptyList(), meals, liked, emptyMap(), 10).isEmpty())
    }
}
