package com.comiditas.familia.domain.validation

import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import com.comiditas.familia.domain.optimizer.MealAssignmentOptimizer
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class MealPlanDiagnosticsTest {
    private val members = listOf(FamilyMember(1, "Ana", 0), FamilyMember(2, "Luis", 0), FamilyMember(3, "Eva", 0))
    private val meals = (1L..3L).map { Meal(it, "Meal $it") }
    private fun reason(people: List<FamilyMember>, catalog: List<Meal>, likes: Map<Long, Set<Long>>) =
        MealPlanDiagnostics.diagnose(people, catalog, likes)?.reason

    @Test fun precedenceAndStaleLikes() {
        assertEquals(MealPlanDiagnostics.Reason.NO_MEMBERS, reason(emptyList(), emptyList(), emptyMap()))
        assertEquals(MealPlanDiagnostics.Reason.UNSUPPORTED_MEMBERS,
            reason(listOf(members[0], members[0]), emptyList(), emptyMap()))
        assertEquals(MealPlanDiagnostics.Reason.UNSUPPORTED_MEMBERS,
            reason(members + FamilyMember(4, "Joe", 0), meals, emptyMap()))
        assertEquals(MealPlanDiagnostics.Reason.EMPTY_CATALOG, reason(members, emptyList(), emptyMap()))
        val diagnostic = MealPlanDiagnostics.diagnose(members, meals, mapOf(1L to setOf(99L)))!!
        assertEquals(MealPlanDiagnostics.Reason.NO_ELIGIBLE_LIKES, diagnostic.reason)
        assertEquals(listOf(1L, 2L, 3L), diagnostic.affectedMemberIds)
    }

    @Test fun exclusiveLikesNeedSharedPair() {
        assertEquals(MealPlanDiagnostics.Reason.INCOMPATIBLE,
            reason(members, meals, members.associate { it.id to setOf(it.id) }))
        assertNull(reason(members, meals, mapOf(1L to setOf(1L), 2L to setOf(1L), 3L to setOf(3L))))
    }

    @Test fun reorderedSnapshotsHaveDeterministicNames() {
        val first = MealPlanDiagnostics.diagnose(members, meals, emptyMap())!!
        val second = MealPlanDiagnostics.diagnose(members.reversed(), meals.reversed(), emptyMap())!!
        assertEquals(first, second)
        assertEquals(MealPlanDiagnostics.message(first, members),
            MealPlanDiagnostics.message(second, members.reversed()))
        assertTrue(MealPlanDiagnostics.message(first, members).contains("Ana, Luis, Eva"))
    }

    @Test fun validDailyPlanCanHaveNoReplacement() {
        val people = members.take(1)
        val likes = mapOf(1L to setOf(1L))
        assertNull(MealPlanDiagnostics.diagnose(people, meals, likes))
        val failure = MealPlanDiagnostics.diagnose(people, meals.drop(1), likes)!!
        val text = MealPlanDiagnostics.replacementMessage(failure, people, Meal(1, "Pasta"))
        assertTrue(text.contains("No se puede sustituir «Pasta»: Ana no tiene otra comida disponible que le guste."))
        assertTrue(text.contains("El plan guardado no se modifica."))
        assertFalse(text.contains("plan es válido"))
    }

    @Test fun incompatibleMessageDoesNotBlameOnePerson() {
        val failure = MealPlanDiagnostics.diagnose(members, meals,
            members.associate { it.id to setOf(it.id) })!!
        assertTrue(failure.affectedMemberIds.isEmpty())
        val text = MealPlanDiagnostics.message(failure, members)
        assertTrue(text.contains("gustos compartidos"))
        members.forEach { assertFalse(text.contains(it.name)) }
    }

    @Test fun exhaustiveSmallMatricesAgreeWithOptimizer() {
        for (count in 1..3) for (matrix in 0 until (1 shl (count * 3))) {
            val people = members.take(count)
            val likes = people.mapIndexed { index, member ->
                member.id to meals.filterIndexed { mealIndex, _ ->
                    matrix and (1 shl (index * 3 + mealIndex)) != 0
                }.map { it.id }.toSet()
            }.toMap()
            assertEquals("count=$count matrix=$matrix",
                MealAssignmentOptimizer().optimize(people, meals, likes, Random(7)) != null,
                MealPlanDiagnostics.diagnose(people, meals, likes) == null)
        }
    }
}
