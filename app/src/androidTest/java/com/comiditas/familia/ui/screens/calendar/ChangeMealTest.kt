package com.comiditas.familia.ui.screens.calendar

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.comiditas.familia.data.local.AppDatabase
import com.comiditas.familia.data.local.entity.FamilyMemberEntity
import com.comiditas.familia.data.local.entity.MealEntity
import com.comiditas.familia.data.model.DayAssignment
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import com.comiditas.familia.data.repository.*
import com.comiditas.familia.domain.optimizer.MealAssignmentOptimizer
import com.comiditas.familia.domain.usecase.AssignMealsUseCase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class ChangeMealTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var database: AppDatabase
    private lateinit var repository: DayAssignmentRepository
    private lateinit var useCase: AssignMealsUseCase
    private val date = LocalDate.parse("2025-01-05")
    private val original = listOf(DayAssignment(date.toString(), 1, 1), DayAssignment(date.toString(), 2, 1))

    @Before fun setUp() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java
        ).build()
        repository = DayAssignmentRepository(database.dayAssignmentDao())
        val preferences = MealPreferenceRepository(database.mealPreferenceDao())
        useCase = AssignMealsUseCase(repository, FamilyMemberRepository(database.familyMemberDao()),
            MealRepository(database.mealDao()), preferences, MealAssignmentOptimizer())
        for (id in 1L..2L) {
            database.familyMemberDao().insert(FamilyMemberEntity(id, "Member $id", 0))
            database.mealDao().insert(MealEntity(id, "Meal $id"))
        }
        for (id in 1L..2L) {
            for (meal in 1L..2L) preferences.setPreference(id, meal, true)
        }
        useCase.saveDay(date.toString(), original)
        compose.setContent {
            MaterialTheme {
                val preparationError = androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableStateOf<String?>(null)
                }
                DayDetailDialog(date, listOf(FamilyMember(1, "Member 1", 0), FamilyMember(2, "Member 2", 0)),
                    listOf(Meal(1, "Meal 1"), Meal(2, "Meal 2")), original,
                    onDismiss = {}, isLoading = false, error = null, onRandomAssign = {},
                    onSave = { draft -> runBlocking {
                        useCase.saveDay(date.toString(), draft.map { (member, meal) ->
                            DayAssignment(date.toString(), member, meal)
                        })
                    } }, onClear = {}, isPreparing = false, preparationError = preparationError.value,
                    onPrepare = { draft, old, ready -> runBlocking {
                        try { ready(useCase.replacementProposals(draft, old)) }
                        catch (error: com.comiditas.familia.domain.validation.ExplainedMealPlanException) {
                            preparationError.value = error.message
                        }
                    } },
                    onCancelReplacement = {})
            }
        }
    }

    @After fun tearDown() { database.close() }

    @Test fun unavailableReplacementShowsNamedExplanationWithoutWrites() {
        runBlocking {
            MealPreferenceRepository(database.mealPreferenceDao()).setPreference(1, 2, false)
        }
        compose.onNodeWithText("Cambiar").performClick()
        compose.onNodeWithText("No se puede sustituir «Meal 1»: Member 1 no tiene otra comida disponible que le guste. " +
            "Revisa las comidas y los gustos. El plan guardado no se modifica.").assertExists()
        runBlocking { assertEquals(original, repository.getByDate(date.toString()).sortedBy { it.memberId }) }
        compose.onNodeWithText("Cancelar").performClick()
        runBlocking { assertEquals(original, repository.getByDate(date.toString()).sortedBy { it.memberId }) }
    }

    @Test fun dishLevelPreviewCancelThenExplicitConfirmation() {
        compose.onNodeWithText("Cambiar").performClick()
        compose.onNodeWithText("Meal 2: Member 1, Member 2").assertExists()
        compose.onNodeWithText("Elegir").performClick()
        runBlocking { assertEquals(original, repository.getByDate(date.toString()).sortedBy { it.memberId }) }
        compose.onNodeWithText("Cancelar").performClick()
        runBlocking { assertEquals(original, repository.getByDate(date.toString()).sortedBy { it.memberId }) }
        compose.onNodeWithText("Cambiar").performClick()
        compose.onNodeWithText("Elegir").performClick()
        compose.onNodeWithText("Confirmar cambio").performClick()
        runBlocking {
            assertEquals(original.map { it.copy(mealId = 2) },
                repository.getByDate(date.toString()).sortedBy { it.memberId })
        }
    }
}
