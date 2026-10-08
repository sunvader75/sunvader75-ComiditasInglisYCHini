package com.comiditas.familia.ui.screens.calendar

import com.comiditas.familia.data.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class WeekMenuPresentationTest {
    @Test fun boundariesAndNavigation() {
        listOf("2024-12-30" to "2024-12-30", "2025-01-05" to "2024-12-30",
            "2024-02-29" to "2024-02-26", "2024-03-03" to "2024-02-26").forEach { (date, monday) ->
            val start = WeekMenuPresentation.start(LocalDate.parse(date))
            assertEquals(LocalDate.parse(monday), start)
            assertEquals(start.plusDays(7), WeekMenuPresentation.navigate(start, 1))
            assertEquals(start.minusDays(7), WeekMenuPresentation.navigate(start, -1))
        }
    }
    @Test fun sevenOrderedEmptyDays() {
        val start = LocalDate.parse("2024-12-30")
        val days = WeekMenuPresentation.days(start, emptyMap(), emptyList(), emptyList())
        assertEquals((0L..6L).map(start::plusDays), days.map { it.date })
        assertTrue(days.all { it.dishes.isEmpty() })
    }
    @Test fun allDishesRecipientsAndMissingLabels() {
        val start = LocalDate.parse("2024-02-26")
        val rows = listOf(DayAssignment(start.toString(), 1, 1), DayAssignment(start.toString(), 2, 1),
            DayAssignment(start.toString(), 3, 2), DayAssignment(start.toString(), 4, 3))
        val days = WeekMenuPresentation.days(start, mapOf(start to rows),
            listOf(FamilyMember(1, "Ana", 0), FamilyMember(2, "Luis", 0), FamilyMember(3, "", 0)),
            listOf(Meal(1, "Arroz"), Meal(2, "")))
        assertEquals(listOf("Arroz", "Comida #2", "Comida #3"), days.first().dishes.map { it.name })
        assertEquals(listOf("Ana", "Luis"), days.first().dishes.first().recipients)
        assertEquals(listOf("Miembro #3"), days.first().dishes[1].recipients)
        assertEquals(listOf("Miembro #4"), days.first().dishes[2].recipients)
        assertTrue(days.drop(1).all { it.dishes.isEmpty() })
    }
}
