package com.comiditas.familia.ui.screens.calendar

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.performScrollTo
import com.comiditas.familia.ui.theme.ComiditasFamiliaTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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

    @Test fun compactLargeTextWeekKeepsActionsAndLastDateReachable() {
        var previous = 0
        var next = 0
        var generated = 0
        var selected: LocalDate? = null
        val state = CalendarUiState(weekStart = start,
            week = WeekObservation(start, loaded = true))
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 2f)) {
                ComiditasFamiliaTheme {
                    Box(Modifier.size(320.dp, 480.dp)) {
                        WeekMenuContent(state, {}, { selected = it }, header = {
                            WeekOverviewHeader(start, true, { previous++ }, { next++ }, { generated++ })
                        })
                    }
                }
            }
        }
        compose.onNodeWithText("Tu semana en la mesa").assertDoesNotExist()
        compose.onNodeWithText("30/12/2024 – 05/01/2025").assertDoesNotExist()
        compose.onNodeWithContentDescription("Semana anterior").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Semana siguiente").performScrollTo().performClick()
        compose.onNodeWithText("Generar semana").performScrollTo().performClick()
        compose.onNodeWithText("Domingo 05/01/2025").performScrollTo().performClick()
        assertEquals(1, previous)
        assertEquals(1, next)
        assertEquals(1, generated)
        assertEquals(start.plusDays(6), selected)
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
