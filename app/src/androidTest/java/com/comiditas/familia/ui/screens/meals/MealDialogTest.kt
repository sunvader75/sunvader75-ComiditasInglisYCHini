package com.comiditas.familia.ui.screens.meals

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performScrollTo
import com.comiditas.familia.ui.theme.ComiditasFamiliaTheme
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MealDialogTest {
    @get:Rule val compose = createComposeRule()
    private val members = listOf(FamilyMember(1, "Ana", 0), FamilyMember(2, "Luis", 0))

    @Test fun editPreselectsLikesAndSavesNameAndSelectionTogether() {
        var saved: Pair<String, Set<Long>>? = null
        compose.setContent {
            MaterialTheme {
                MealDialog(Meal(7, "Sopa", "Legado"), members, setOf(1), false, null,
                    onDismiss = {}, onConfirm = { name, ids -> saved = name to ids })
            }
        }
        compose.onNodeWithText("Ana").assertIsSelected()
        compose.onNodeWithText("Luis").assertIsNotSelected()
        compose.onNodeWithText("Legado").assertDoesNotExist()
        compose.onNodeWithText("Descripción (opcional)").assertDoesNotExist()
        compose.onNodeWithText("Sopa").performTextReplacement("Arroz")
        compose.onNodeWithText("Ana").performClick()
        compose.onNodeWithText("Luis").performClick()
        assertNull(saved)
        compose.onNodeWithText("Guardar").performClick()
        assertEquals("Arroz" to setOf(2L), saved)
    }

    @Test fun cancelDiscardsNameAndSelectionWithoutCallingSave() {
        var saves = 0
        var dismissals = 0
        compose.setContent {
            MaterialTheme {
                MealDialog(Meal(7, "Sopa"), members, setOf(1), false, null,
                    onDismiss = { dismissals++ }, onConfirm = { _, _ -> saves++ })
            }
        }
        compose.onNodeWithText("Sopa").performTextReplacement("No guardar")
        compose.onNodeWithText("Ana").performClick()
        compose.onNodeWithText("Luis").performClick()
        compose.onNodeWithText("Cancelar").performClick()
        assertEquals(1, dismissals)
        assertEquals(0, saves)
    }

    @Test fun createWithNoMembersAllowsZeroLikesButRejectsBlankName() {
        var saved: Pair<String, Set<Long>>? = null
        compose.setContent {
            MaterialTheme {
                MealDialog(null, emptyList(), emptySet(), false, null,
                    onDismiss = {}, onConfirm = { name, ids -> saved = name to ids })
            }
        }
        compose.onNodeWithText("Guardar").assertIsNotEnabled()
        compose.onNodeWithText("Nombre *").performTextReplacement("Sopa")
        compose.onNodeWithText("Guardar").performClick()
        assertEquals("Sopa" to emptySet<Long>(), saved)
    }

    @Test fun largeFontEditorCanReachLastMemberAndSaveSelection() {
        val manyMembers = (1L..12L).map { FamilyMember(it, "Miembro $it", 0) }
        var saved: Pair<String, Set<Long>>? = null
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                ComiditasFamiliaTheme {
                    MealDialog(Meal(7, "Sopa"), manyMembers, setOf(1L), false, null,
                        onDismiss = {}, onConfirm = { name, ids -> saved = name to ids })
                }
            }
        }
        compose.onNodeWithText("Miembro 12").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText("Miembro 12").assertIsSelected()
        compose.onNodeWithText("Guardar").assertIsDisplayed().performClick()
        assertEquals("Sopa" to setOf(1L, 12L), saved)
    }

    @Test fun cardShowsLikingNamesOrZeroLikesButNeverLegacyDescription() {
        compose.setContent {
            MaterialTheme {
                androidx.compose.foundation.layout.Column {
                    MealCard(MealWithLikes(Meal(7, "Sopa", "Legado"), members), onEdit = {})
                    MealCard(MealWithLikes(Meal(8, "Arroz"), emptyList()), onEdit = {})
                }
            }
        }
        compose.onNodeWithText("Les gusta a: Ana, Luis").assertExists()
        compose.onNodeWithText("Sin gustos asignados").assertExists()
        compose.onNodeWithText("Legado").assertDoesNotExist()
    }
}
