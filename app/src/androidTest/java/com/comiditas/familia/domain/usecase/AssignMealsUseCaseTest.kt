package com.comiditas.familia.domain.usecase

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.comiditas.familia.data.local.AppDatabase
import com.comiditas.familia.data.local.entity.*
import com.comiditas.familia.data.model.DayAssignment
import com.comiditas.familia.data.repository.*
import com.comiditas.familia.domain.optimizer.MealAssignmentOptimizer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssignMealsUseCaseTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: DayAssignmentRepository
    private lateinit var useCase: AssignMealsUseCase
    private val original = listOf(DayAssignment("day", 1, 1), DayAssignment("day", 2, 1))

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
            preferences.setPreference(id, 1, true)
        }
        useCase.saveDay("day", original)
    }

    @After fun tearDown() { database.close() }

    @Test fun proposalsAreReadOnlyAndConfirmationReplacesOnlyTargetDay() = runBlocking {
        val preferences = MealPreferenceRepository(database.mealPreferenceDao())
        for (id in 1L..2L) preferences.setPreference(id, 2, true)
        repository.assign("neighbor", 1, 1)
        val proposal = useCase.replacementProposals(original.associate { it.memberId to it.mealId }, 1).first()
        assertEquals(original, repository.getByDate("day").sortedBy { it.memberId })
        useCase.saveDay("day", proposal.assignments.map { (member, meal) -> DayAssignment("day", member, meal) })
        assertEquals(listOf(DayAssignment("day", 1, 2), DayAssignment("day", 2, 2)),
            repository.getByDate("day").sortedBy { it.memberId })
        assertEquals(listOf(DayAssignment("neighbor", 1, 1)), repository.getByDate("neighbor"))
    }

    @Test fun freshPreferencesRejectConfirmationWithoutDeletingRows() = runBlocking {
        val preferences = MealPreferenceRepository(database.mealPreferenceDao())
        for (id in 1L..2L) preferences.setPreference(id, 2, true)
        val proposal = useCase.replacementProposals(original.associate { it.memberId to it.mealId }, 1).first()
        preferences.setPreference(2, 2, false)
        try {
            useCase.saveDay("day", proposal.assignments.map { (member, meal) -> DayAssignment("day", member, meal) })
            fail("Fresh validation must reject the confirmation")
        } catch (_: IllegalArgumentException) { }
        assertEquals(original, repository.getByDate("day").sortedBy { it.memberId })
    }

    @Test fun rejectedMutationsPreserveRows() = runBlocking {
        val changes: List<suspend () -> Unit> = listOf(
            { useCase.removeAssignment("day", 1) },
            { useCase.assignManually("day", 1, 2) },
            { useCase.saveDay("day", original.take(1)) },
            { useCase.saveDay("day", emptyList()) }
        )
        for (change in changes) {
            try { change(); fail("Invalid proposal must be rejected") } catch (_: IllegalArgumentException) { }
            assertEquals(original, repository.getByDate("day").sortedBy { it.memberId })
        }
    }

    @Test fun failedInsertRollsBackDeletedDay() = runBlocking {
        try {
            database.dayAssignmentDao().replaceDay("day", listOf(
                DayAssignmentEntity("day", 1, 2), DayAssignmentEntity("day", 999, 1)))
            fail("Foreign key failure expected")
        } catch (_: android.database.sqlite.SQLiteConstraintException) { }
        assertEquals(original, repository.getByDate("day").sortedBy { it.memberId })
    }

    private val weekStart = java.time.LocalDate.parse("2024-12-30")
    private fun weekRows() = (0L..6L).flatMap { offset ->
        original.map { it.copy(date = weekStart.plusDays(offset).toString()) }
    }
    private suspend fun storedWeek() = (0L..6L).flatMap {
        repository.getByDate(weekStart.plusDays(it).toString()).sortedBy { row -> row.memberId }
    }
    private suspend fun seedWeek() {
        assertTrue(repository.replaceWeek(weekStart.toString(), weekStart.plusDays(6).toString(), weekRows(), true))
    }

    @Test fun impossibleGenerationPreservesEntireWeek() = runBlocking {
        seedWeek()
        database.familyMemberDao().insert(FamilyMemberEntity(3, "Third", 0))
        database.mealDao().insert(MealEntity(3, "Third meal"))
        val preferences = MealPreferenceRepository(database.mealPreferenceDao())
        preferences.setPreference(2, 1, false)
        preferences.setPreference(2, 2, true)
        preferences.setPreference(3, 3, true)
        try {
            useCase.generateWeek(weekStart, true)
            fail("Explained infeasibility expected")
        } catch (error: com.comiditas.familia.domain.validation.ExplainedMealPlanException) {
            assertTrue(error.message!!.contains("gustos compartidos"))
        }
        assertEquals(weekRows(), storedWeek())
    }

    @Test fun exactWeekReplacementPreservesNeighbors() = runBlocking {
        seedWeek()
        val before = weekStart.minusDays(1).toString()
        val after = weekStart.plusDays(7).toString()
        repository.assign(before, 1, 2)
        repository.assign(after, 2, 2)
        val replacement = weekRows().map { it.copy(mealId = 2) }
        assertTrue(repository.replaceWeek(weekStart.toString(), weekStart.plusDays(6).toString(), replacement, true))
        assertEquals(replacement, storedWeek())
        assertEquals(listOf(DayAssignment(before, 1, 2)), repository.getByDate(before))
        assertEquals(listOf(DayAssignment(after, 2, 2)), repository.getByDate(after))
    }

    @Test fun lateForeignKeyFailureRollsBackEntireWeek() = runBlocking {
        seedWeek()
        val proposal = weekRows().mapIndexed { index, row ->
            if (index == 13) row.copy(mealId = 999) else row.copy(mealId = 2)
        }
        try {
            repository.replaceWeek(weekStart.toString(), weekStart.plusDays(6).toString(), proposal, true)
            fail("Foreign key failure expected")
        } catch (_: android.database.sqlite.SQLiteConstraintException) { }
        assertEquals(weekRows(), storedWeek())
    }

    @Test fun unconfirmedExistingAndRacingAssignmentsAreRefused() = runBlocking {
        assertFalse(useCase.weekHasAssignments(weekStart))
        // A writer arrives after the empty preflight, in the adjacent calendar month.
        repository.assign("2025-01-05", 1, 2)
        assertEquals(AssignMealsUseCase.WeekResult.CONFIRMATION_REQUIRED, useCase.generateWeek(weekStart, false))
        assertEquals(listOf(DayAssignment("2025-01-05", 1, 2)), storedWeek())
        assertTrue(useCase.weekHasAssignments(weekStart))
        seedWeek()
        assertEquals(AssignMealsUseCase.WeekResult.CONFIRMATION_REQUIRED, useCase.generateWeek(weekStart, false))
        assertEquals(weekRows(), storedWeek())
    }

    @Test fun malformedRangeCannotDeleteWeek() = runBlocking {
        seedWeek()
        try {
            repository.replaceWeek(weekStart.toString(), weekStart.plusDays(7).toString(), weekRows(), true)
            fail("Invalid range must be rejected")
        } catch (_: IllegalArgumentException) { }
        assertEquals(weekRows(), storedWeek())
    }

    @Test fun unavailableLikesPreserveDayAndWeek() = runBlocking {
        seedWeek()
        MealPreferenceRepository(database.mealPreferenceDao()).setPreference(1, 1, false)
        val actions: List<suspend () -> Unit> = listOf(
            { useCase.assignRandomly("day"); Unit },
            { useCase.generateWeek(weekStart, true); Unit }
        )
        for (action in actions) {
            try { action(); fail("Explained failure expected") }
            catch (error: com.comiditas.familia.domain.validation.ExplainedMealPlanException) {
                assertTrue(error.message!!.contains("Member 1"))
                assertTrue(error.message!!.contains("El plan guardado no se modifica"))
            }
            assertEquals(original, repository.getByDate("day").sortedBy { it.memberId })
            assertEquals(weekRows(), storedWeek())
        }
    }

    @Test fun replacementValidatesDraftBeforeDiagnosingAlternatives() = runBlocking {
        val validDraft = original.associate { it.memberId to it.mealId }
        val attempts = listOf(
            validDraft to "otra comida disponible",
            mapOf(1L to 1L) to "exactamente una comida",
            mapOf(1L to 2L, 2L to 1L) to "que le guste"
        )
        for ((draft, expected) in attempts) {
            val frozen = draft.toMap()
            try { useCase.replacementProposals(draft, 1); fail("Explained failure expected") }
            catch (error: com.comiditas.familia.domain.validation.ExplainedMealPlanException) {
                assertTrue(error.message!!, error.message!!.contains(expected))
            }
            assertEquals(frozen, draft)
            assertEquals(original, repository.getByDate("day").sortedBy { it.memberId })
        }
        try { useCase.replacementProposals(validDraft, 2); fail("Missing old meal expected") }
        catch (error: com.comiditas.familia.domain.validation.ExplainedMealPlanException) {
            assertTrue(error.message!!.contains("no está en el borrador"))
        }
        assertEquals(original, repository.getByDate("day").sortedBy { it.memberId })
    }

    @Test fun validReplacementAndExplicitClear() = runBlocking {
        MealPreferenceRepository(database.mealPreferenceDao()).setPreference(2, 2, true)
        val replacement = listOf(original[0], original[1].copy(mealId = 2))
        useCase.saveDay("day", replacement)
        assertEquals(replacement, repository.getByDate("day").sortedBy { it.memberId })
        useCase.clearDay("day")
        assertTrue(repository.getByDate("day").isEmpty())
    }
}
