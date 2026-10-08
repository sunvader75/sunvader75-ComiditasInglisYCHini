package com.comiditas.familia.domain.validation

import com.comiditas.familia.data.model.*
import org.junit.Assert.*
import org.junit.Test

class DayAssignmentValidatorTest {
    private val members = (1L..3L).map { FamilyMember(it, "Member $it", 0) }
    private val meals = (1L..3L).map { Meal(it, "Meal $it") }
    private val preferences = members.associate { it.id to setOf(1L, 2L, 3L) }
    private val rows = members.map { DayAssignment("day", it.id, 1) }
    private fun error(proposal: List<DayAssignment>, people: List<FamilyMember> = members,
                      likes: Map<Long, Set<Long>> = preferences) =
        DayAssignmentValidator.error("day", proposal, people, meals, likes)

    @Test fun commonMealIsValid() { assertNull(error(rows)) }
    @Test fun twoMealsCoverEveryone() { assertNull(error(rows.dropLast(1) + rows.last().copy(mealId = 2))) }
    @Test fun missingMemberIsRejected() { assertNotNull(error(rows.dropLast(1))) }
    @Test fun duplicateMemberIsRejected() { assertNotNull(error(listOf(rows[0], rows[0], rows[2]))) }
    @Test fun unknownMemberIsRejected() { assertNotNull(error(rows.dropLast(1) + rows.last().copy(memberId = 9))) }
    @Test fun unknownMealIsRejected() { assertNotNull(error(rows.map { it.copy(mealId = 9) })) }
    @Test fun dislikedMealIsRejected() { assertNotNull(error(rows, likes = preferences + (1L to emptySet()))) }
    @Test fun fourMembersAreRejected() { assertNotNull(error(rows, members + FamilyMember(4, "Fourth", 0))) }
    @Test fun threeMealsAreRejected() { assertNotNull(error(rows.map { it.copy(mealId = it.memberId) })) }
    @Test fun wrongDateIsRejected() { assertNotNull(error(rows.map { it.copy(date = "other") })) }
    @Test fun emptyDayIsNotComplete() { assertNotNull(error(emptyList())) }
    @Test fun oneMemberIsValid() { assertNull(error(rows.take(1), members.take(1))) }
}
