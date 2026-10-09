package com.comiditas.familia.ui.screens.members

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.ui.theme.ComiditasFamiliaTheme
import com.comiditas.familia.ui.theme.MemberColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class MemberDialogTest {
    @get:Rule val compose = createComposeRule()

    @Test fun editKeepsIdentityAndSavesExactNameAndChosenColorOnlyOnConfirm() {
        var saved: Pair<String, Int>? = null
        val original = FamilyMember(7, "Ana", argb(MemberColors[1]))
        compose.setContent {
            ComiditasFamiliaTheme {
                MemberDialog(original, MemberColors.map(::argb).toSet(), onDismiss = {},
                    onConfirm = { name, color -> saved = name to color })
            }
        }
        compose.onNodeWithContentDescription("Color 2").assertIsSelected()
        compose.onNodeWithText("Ana").performTextReplacement("  María  ")
        // Already-used colors remain selectable: existingColors never restricted the palette.
        compose.onNodeWithContentDescription("Color 1").performClick().assertIsSelected()
        assertNull(saved)
        compose.onNodeWithText("Guardar").performClick()
        assertEquals("  María  " to argb(MemberColors.first()), saved)
    }

    @Test fun cancelDiscardsEditsWithoutSaving() {
        var saves = 0
        var dismissals = 0
        compose.setContent {
            ComiditasFamiliaTheme {
                MemberDialog(FamilyMember(7, "Ana", argb(MemberColors.first())), emptySet(),
                    onDismiss = { dismissals++ }, onConfirm = { _, _ -> saves++ })
            }
        }
        compose.onNodeWithText("Ana").performTextReplacement("No guardar")
        compose.onNodeWithContentDescription("Color 2").performClick()
        compose.onNodeWithText("Cancelar").performClick()
        assertEquals(1, dismissals)
        assertEquals(0, saves)
    }

    @Test fun createRejectsBlankAndKeepsFirstColorDefault() {
        var saved: Pair<String, Int>? = null
        compose.setContent {
            ComiditasFamiliaTheme {
                MemberDialog(null, emptySet(), onDismiss = {},
                    onConfirm = { name, color -> saved = name to color })
            }
        }
        compose.onNodeWithText("Guardar").assertIsNotEnabled()
        compose.onNodeWithText("Nombre").performTextReplacement("   ")
        compose.onNodeWithText("Guardar").assertIsNotEnabled()
        compose.onNodeWithText("Nombre").performTextReplacement("Ana")
        compose.onNodeWithText("Guardar").performClick()
        assertEquals("Ana" to argb(MemberColors.first()), saved)
    }

    @Test fun compactLargeTextBodyScrollsToLastColorAndKeepsSaveReachable() {
        var saved: Pair<String, Int>? = null
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                ComiditasFamiliaTheme {
                    MemberDialog(FamilyMember(7, "Ana", argb(MemberColors.first())), emptySet(),
                        onDismiss = {}, onConfirm = { name, color -> saved = name to color },
                        // Constrain the actual scroll viewport, not only the font scale.
                        contentModifier = Modifier.requiredSize(width = 220.dp, height = 160.dp))
                }
            }
        }
        compose.onNodeWithContentDescription("Color ${MemberColors.size}")
            .performScrollTo().assertIsDisplayed().performClick().assertIsSelected()
        compose.onNodeWithText("Guardar").assertIsDisplayed().performClick()
        assertEquals("Ana" to argb(MemberColors.last()), saved)
    }

    private fun argb(color: Color): Int = android.graphics.Color.argb(
        (color.alpha * 255).toInt(), (color.red * 255).toInt(),
        (color.green * 255).toInt(), (color.blue * 255).toInt()
    )
}
