package com.comiditas.familia.ui.screens.calendar

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.comiditas.familia.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class WeekMenuScreenTest {
    @get:Rule val compose = createComposeRule()
    private val start = LocalDate.parse("2024-12-30")

    @Test fun dishesAndEveryRecipientAreVisibleWithoutEditing() {
        var selected: LocalDate? = null
        val state = CalendarUiState(weekStart = start,
            week = WeekObservation(start, mapOf(start to listOf(
                DayAssignment(start.toString(), 1, 1), DayAssignment(start.toString(), 2, 1),
                DayAssignment(start.toString(), 3, 2))), loaded = true),
            members = listOf(FamilyMember(1, "Ana", 0), FamilyMember(2, "Luis", 0)),
            meals = listOf(Meal(1, "Arroz"), Meal(2, "Sopa")))
        compose.setContent { MaterialTheme { WeekMenuContent(state, {}, { selected = it }) } }
        compose.onNodeWithText("Arroz").assertExists()
        compose.onNodeWithText("Ana, Luis").assertExists()
        compose.onNodeWithText("Sopa").assertExists()
        compose.onNodeWithText("Miembro #3").assertExists()
        assertEquals(null, selected)
        compose.onNodeWithText("Arroz").performClick()
        assertEquals(start, selected)
    }

    @Test fun loadingErrorRetryAndLoadedEmptyAreDistinct() {
        val state = mutableStateOf(CalendarUiState(weekStart = start, week = WeekObservation(start)))
        compose.setContent { MaterialTheme { WeekMenuContent(state.value, {
            state.value = state.value.copy(week = WeekObservation(start, loaded = true))
        }, {}) } }
        compose.onNodeWithText("Sin comidas asignadas").assertDoesNotExist()
        compose.runOnIdle { state.value = state.value.copy(week = WeekObservation(start, error = "Error de lectura")) }
        compose.onNodeWithText("Error de lectura").assertExists()
        compose.onNodeWithText("Reintentar").performClick()
        compose.onNodeWithText("Error de lectura").assertDoesNotExist()
    }
}
