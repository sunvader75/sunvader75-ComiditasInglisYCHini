package com.comiditas.familia.domain.optimizer

import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

class WeekMealPlanGeneratorTest {
    private val generator = WeekMealPlanGenerator()
    private val members = (1L..3L).map { FamilyMember(it, "Member $it", 0) }
    private val meals = (1L..3L).map { Meal(it, "Meal $it") }
    private val likes = mapOf(1L to setOf(1L), 2L to setOf(1L), 3L to setOf(2L, 3L))

    @Test fun sevenCompleteLikedDaysWithAtMostTwoMeals() {
        val rows = generator.generate(LocalDate.parse("2024-02-29"), members, meals, likes, Random(4))!!
        assertEquals(21, rows.size)
        val days = rows.groupBy { it.date }
        assertEquals(7, days.size)
        days.values.forEach { day ->
            assertEquals(listOf(1L, 2L, 3L), day.map { it.memberId }.sorted())
            assertTrue(day.map { it.mealId }.distinct().size <= 2)
            day.forEach { assertTrue(it.mealId in likes.getValue(it.memberId)) }
        }
    }

    @Test fun mondaySundayAndBoundaryDatesUseContainingWeek() {
        listOf("2024-12-30" to "2024-12-30", "2025-01-05" to "2024-12-30",
            "2024-02-29" to "2024-02-26", "2024-03-03" to "2024-02-26").forEach { (selected, start) ->
            val rows = generator.generate(LocalDate.parse(selected), members, meals, likes, Random(1))!!
            assertEquals((0L..6L).map { LocalDate.parse(start).plusDays(it).toString() }, rows.map { it.date }.distinct())
        }
    }

    @Test fun fixedSeedReproducesWeek() {
        val date = LocalDate.parse("2025-01-01")
        val first = generator.generate(date, members, meals, likes, Random(17))
        assertNotNull(first)
        assertEquals(first, generator.generate(date, members, meals, likes, Random(17)))
    }

    @Test fun impossibleAndIncompleteSnapshotsFail() {
        val date = LocalDate.parse("2025-01-01")
        assertNull(generator.generate(date, members, meals, members.associate { it.id to setOf(it.id) }, Random(1)))
        assertNull(generator.generate(date, emptyList(), meals, likes, Random(1)))
        assertNull(generator.generate(date, members + FamilyMember(4, "Fourth", 0), meals, likes, Random(1)))
        assertNull(generator.generate(date, members, emptyList(), likes, Random(1)))
        assertNull(generator.generate(date, members, meals, likes - 3L, Random(1)))
        assertNull(generator.generate(date, members, meals, members.associate { it.id to setOf(99L) }, Random(1)))
    }

    @Test fun repeatedMealIsAllowedAllSevenDays() {
        val rows = generator.generate(LocalDate.parse("2025-01-01"), members.take(1), meals.take(1),
            mapOf(1L to setOf(1L)), Random(1))!!
        assertEquals(7, rows.size)
        assertTrue(rows.all { it.mealId == 1L })
    }
}
