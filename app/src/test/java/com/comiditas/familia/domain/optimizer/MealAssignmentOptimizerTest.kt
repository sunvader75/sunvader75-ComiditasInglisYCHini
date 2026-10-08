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

    @Test fun unknownLikedMealDoesNotGenerate() {
        assertNull(MealAssignmentOptimizer().optimize(members, meals,
            members.associate { it.id to setOf(9L) }, Random(7)))
    }

    @Test fun threeExclusiveMealsHaveNoSolution() {
        assertNull(MealAssignmentOptimizer().optimize(members, meals,
            members.associate { it.id to setOf(it.id) }, Random(7)))
    }
}
