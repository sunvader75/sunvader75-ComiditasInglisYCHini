package com.comiditas.familia.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.comiditas.familia.ui.navigation.Screen
import com.comiditas.familia.ui.theme.ComiditasFamiliaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun labeledActionsNavigateToExistingDestinations() {
        val routes = mutableListOf<String>()
        compose.setContent {
            ComiditasFamiliaTheme { HomeScreen(onNavigate = { routes.add(it) }) }
        }
        listOf(
            "Comiditas Familia",
            "A la mesa, en familia",
            "Gestiona las comidas de tu familia",
            "Prepara tu semana",
            "Primero configura los miembros y las comidas con sus gustos, y finalmente planifica en el menú semanal."
        ).forEach { copy -> compose.onNodeWithText(copy).assertDoesNotExist() }
        listOf(
            "Consulta y edita las comidas de la semana",
            "Edita las comidas y a quién le gustan",
            "Administra los 3 miembros de la familia"
        ).forEach { subtitle -> compose.onNodeWithText(subtitle).assertExists() }
        listOf(
            "Menú semanal" to Screen.Calendar.route,
            "Comidas" to Screen.Meals.route,
            "Miembros" to Screen.Members.route
        ).forEachIndexed { index, (label, route) ->
            compose.onNodeWithText(label).performScrollTo()
                .assertIsDisplayed().assertHasClickAction().performClick()
            compose.runOnIdle { assertEquals(index + 1, routes.size) }
            compose.runOnIdle { assertEquals(route, routes.last()) }
        }
    }

    @Test fun compactLargeTextKeepsEveryDestinationReachable() {
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale = 2f)) {
                ComiditasFamiliaTheme {
                    Box(Modifier.size(width = 320.dp, height = 480.dp)) {
                        HomeScreen(onNavigate = {})
                    }
                }
            }
        }
        listOf("Menú semanal", "Comidas", "Miembros").forEach { label ->
            compose.onNodeWithText(label).performScrollTo()
                .assertIsDisplayed().assertHasClickAction()
        }
    }
}
