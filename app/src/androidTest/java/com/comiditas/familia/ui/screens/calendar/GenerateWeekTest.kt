package com.comiditas.familia.ui.screens.calendar

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.comiditas.familia.data.local.AppDatabase
import com.comiditas.familia.data.local.entity.FamilyMemberEntity
import com.comiditas.familia.data.local.entity.MealEntity
import com.comiditas.familia.data.model.DayAssignment
import com.comiditas.familia.data.repository.*
import com.comiditas.familia.domain.optimizer.MealAssignmentOptimizer
import com.comiditas.familia.domain.usecase.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class GenerateWeekTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var database: AppDatabase
    private lateinit var repository: DayAssignmentRepository
    private lateinit var viewModel: CalendarViewModel
    private val store = ViewModelStore()
    private val existing = DayAssignment("2025-01-05", 1, 1)

    @Before fun setUp() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java
        ).build()
        val members = FamilyMemberRepository(database.familyMemberDao())
        val meals = MealRepository(database.mealDao())
        val preferences = MealPreferenceRepository(database.mealPreferenceDao())
        repository = DayAssignmentRepository(database.dayAssignmentDao())
        database.familyMemberDao().insert(FamilyMemberEntity(1, "Member", 0))
        database.mealDao().insert(MealEntity(1, "Meal"))
        preferences.setPreference(1, 1, true)
        repository.assign(existing.date, existing.memberId, existing.mealId)
        viewModel = CalendarViewModel(GetFamilyMembersUseCase(members), GetMealsUseCase(meals),
            AssignMealsUseCase(repository, members, meals, preferences, MealAssignmentOptimizer()))
        store.put("calendar", viewModel)
        compose.setContent { MaterialTheme { CalendarScreen(viewModel, onBack = {}) } }
        compose.runOnIdle { viewModel.showWeek(LocalDate.parse("2024-12-30")) }
    }

    @After fun tearDown() {
        compose.runOnIdle { store.clear() }
        database.close()
    }

    @Test fun editCloseReopenAndWeekNavigation() {
        val monday = "Lunes 30/12/2024"
        compose.onNodeWithText(monday).performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodes(androidx.compose.ui.test.hasText("Editor manual"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Cerrar").performClick()
        compose.onNodeWithText(monday).performClick()
        compose.onNodeWithText("Editor manual").assertExists()
        compose.onNodeWithText("Cerrar").performClick()
        compose.onNodeWithContentDescription("Semana siguiente").performClick()
        compose.onNodeWithText("06/01/2025 – 12/01/2025").assertExists()
        compose.onNodeWithContentDescription("Semana anterior").performClick()
        compose.onNodeWithText(monday).assertExists()
        runBlocking { assertEquals(listOf(existing), repository.getByDate(existing.date)) }
    }

    @Test fun explainedGenerationFailureIsShownWithoutWrites() {
        runBlocking {
            MealPreferenceRepository(database.mealPreferenceDao()).setPreference(1, 1, false)
        }
        compose.runOnIdle {
            viewModel.generateWeek(LocalDate.parse("2024-12-30"), true, {}, {})
        }
        val message = "No se puede generar la semana. Member: sin comidas disponibles que les gusten. " +
            "Añade comidas o revisa sus gustos. El plan guardado no se modifica."
        compose.waitUntil(5_000) {
            compose.onAllNodes(androidx.compose.ui.test.hasText(message)).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(message).assertExists()
        runBlocking { assertEquals(listOf(existing), repository.getByDate(existing.date)) }
    }

    @Test fun crossMonthConfirmationCanBeCancelledWithoutWrites() {
        compose.onNodeWithText("Generar semana").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodes(androidx.compose.ui.test.hasText("¿Reemplazar semana?"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Reemplazar").assertIsEnabled()
        // The pending target is frozen even if a caller changes the selected day.
        compose.runOnIdle { viewModel.selectDate(LocalDate.parse("2025-02-10")) }
        compose.onNode(
            hasText("30/12/2024 – 05/01/2025") and hasAnyAncestor(isDialog())
        ).assertExists()
        compose.onNodeWithText("Generar semana").assertIsNotEnabled()
        compose.onNodeWithText("Cancelar").performClick()
        runBlocking { assertEquals(listOf(existing), repository.getByDate(existing.date)) }
        compose.onNodeWithText("Generar semana").assertIsEnabled()
    }
}
