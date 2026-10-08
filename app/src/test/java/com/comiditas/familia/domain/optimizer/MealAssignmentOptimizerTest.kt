package com.comiditas.familia.domain.optimizer

import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class MealAssignmentOptimizerTest {
    private val members = (1L..3L).map { FamilyMember(it, "Member $it", 0) }
    private val meals = (1L..3L).map { Meal(it, "Meal $it") }

    @Test fun seededGenerationIsCompleteAndDeterministic() {
        val likes = mapOf(1L to setOf(1L), 2L to setOf(1L), 3L to setOf(2L, 3L))
        repeat(100) { seed ->
            val result = MealAssignmentOptimizer().optimize(members, meals, likes, Random(seed))!!
            assertEquals(result, MealAssignmentOptimizer().optimize(members, meals, likes, Random(seed)))
            assertEquals(2, result.numberOfMeals)
            assertEquals(listOf(1L, 2L, 3L), result.assignments.values.flatten().sorted())
            result.assignments.forEach { (meal, recipients) -> recipients.forEach { assertTrue(meal in likes[it]!!) } }
        }
    }

    @Test fun commonMealIsPreferred() {
        val result = MealAssignmentOptimizer().optimize(members, meals,
            members.associate { it.id to setOf(1L, it.id) }, Random(7))!!
        assertEquals(mapOf(1L to listOf(1L, 2L, 3L)), result.assignments)
    }

    @Test fun twoPeopleCanUseTwoMeals() {
        assertEquals(2, MealAssignmentOptimizer().optimize(members.take(2), meals,
            mapOf(1L to setOf(1L), 2L to setOf(2L)), Random(7))!!.numberOfMeals)
    }

    @Test fun freshSplitBeatsRepeatedSharedAndPreservesInputs() {
        val preferences = mapOf(1L to setOf(1L, 2L), 2L to setOf(1L, 2L), 3L to setOf(1L, 3L))
        val used = mutableSetOf(1L)
        repeat(100) { seed ->
            val result = MealAssignmentOptimizer().optimize(members, meals, preferences, Random(seed), used)!!
            assertEquals(setOf(2L, 3L), result.assignments.keys)
            assertEquals(listOf(1L, 2L, 3L), result.assignments.values.flatten().sorted())
            assertEquals(result, MealAssignmentOptimizer().optimize(members, meals, preferences, Random(seed), used))
            result.assignments.forEach { (meal, recipients) ->
                recipients.forEach { assertTrue(meal in preferences.getValue(it)) }
            }
        }
        assertEquals(setOf(1L), used)
        assertEquals(setOf(1L, 2L), preferences.getValue(1L))
    }

    @Test fun minimumRepeatScoreWinsAcrossAllPartitions() {
        val preferences = mapOf(1L to setOf(1L, 2L), 2L to setOf(1L, 3L), 3L to setOf(2L, 3L))
        repeat(100) { seed ->
            val result = MealAssignmentOptimizer().optimize(members, meals, preferences, Random(seed), setOf(1L, 2L))!!
            // Two used dishes are feasible, but one used plus one fresh is better.
            assertEquals(1, result.assignments.keys.count { it in setOf(1L, 2L) })
            assertEquals(2, result.numberOfMeals)
            assertEquals(listOf(1L, 2L, 3L), result.assignments.values.flatten().sorted())
        }
    }

    @Test fun equallyFreshSharedBeatsSplitAndOverlappingRecipientsArePreserved() {
        val preferences = mapOf(1L to setOf(1L, 2L), 2L to setOf(1L, 2L), 3L to setOf(1L, 3L))
        repeat(50) { seed ->
            val result = MealAssignmentOptimizer().optimize(members, meals, preferences, Random(seed), setOf(99L))!!
            assertEquals(mapOf(1L to listOf(1L, 2L, 3L)), result.assignments)
        }
    }

    @Test fun unavoidableRepeatedSharedBeatsTwoRepeatedDishes() {
        val preferences = members.associate { it.id to setOf(1L, it.id) }
        val result = MealAssignmentOptimizer().optimize(members, meals, preferences, Random(3), setOf(1L, 2L, 3L))!!
        assertEquals(mapOf(1L to listOf(1L, 2L, 3L)), result.assignments)
    }

    @Test fun everyFeasiblePairAppearsInFiniteSeedSweep() {
        val preferences = mapOf(1L to setOf(1L, 2L), 2L to setOf(1L, 3L), 3L to setOf(2L, 3L))
        val observed = (0..99).map { seed ->
            MealAssignmentOptimizer().optimize(members, meals, preferences, Random(seed))!!.assignments.keys
        }.toSet()
        assertEquals(setOf(setOf(1L, 2L), setOf(1L, 3L), setOf(2L, 3L)), observed)
    }

    @Test fun unknownLikedMealDoesNotGenerate() {
        assertNull(MealAssignmentOptimizer().optimize(members, meals,
            members.associate { it.id to setOf(9L) }, Random(7)))
    }

    @Test fun threeExclusiveMealsHaveNoSolution() {
        assertNull(MealAssignmentOptimizer().optimize(members, meals,
            members.associate { it.id to setOf(it.id) }, Random(7)))
    }
}
