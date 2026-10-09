package com.comiditas.familia.domain.usecase

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.comiditas.familia.data.local.AppDatabase
import com.comiditas.familia.data.local.entity.FamilyMemberEntity
import com.comiditas.familia.data.local.entity.MealEntity
import com.comiditas.familia.data.model.DayAssignment
import com.comiditas.familia.data.repository.DayAssignmentRepository
import com.comiditas.familia.data.repository.MealPreferenceRepository
import com.comiditas.familia.data.repository.MealRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaveMealWithPreferencesUseCaseTest {
    private lateinit var database: AppDatabase
    private lateinit var meals: MealRepository
    private lateinit var preferences: MealPreferenceRepository
    private lateinit var save: SaveMealWithPreferencesUseCase

    @Before fun setUp() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java
        ).build()
        meals = MealRepository(database.mealDao())
        preferences = MealPreferenceRepository(database.mealPreferenceDao())
        save = SaveMealWithPreferencesUseCase(database, meals, preferences)
        database.familyMemberDao().insert(FamilyMemberEntity(1, "Ana", 0))
        database.familyMemberDao().insert(FamilyMemberEntity(2, "Luis", 0))
    }

    @After fun tearDown() { database.close() }

    @Test fun creationSavesNameAndLikesAndAllowsZeroLikes() = runBlocking {
        val id = save(null, "  Sopa  ", setOf(1, 2))
        assertEquals("Sopa", meals.getById(id)?.name)
        assertEquals("", meals.getById(id)?.description)
        assertTrue(preferences.isLiked(1, id))
        assertTrue(preferences.isLiked(2, id))
        val zero = save(null, "Arroz", emptySet())
        assertTrue(database.mealPreferenceDao().getByMeal(zero).isEmpty())
    }

    @Test fun editPreservesIdentityLegacyDescriptionOtherLikesAndPlans() = runBlocking {
        database.mealDao().insert(MealEntity(7, "Antes", "Descripción histórica"))
        database.mealDao().insert(MealEntity(8, "Otra"))
        preferences.setPreference(1, 7, true)
        preferences.setPreference(2, 8, true)
        val plans = DayAssignmentRepository(database.dayAssignmentDao())
        plans.assign("2025-01-01", 1, 7)
        assertEquals(7L, save(7, "Después", setOf(2)))
        assertEquals(MealEntity(7, "Después", "Descripción histórica"), database.mealDao().getById(7))
        assertFalse(preferences.isLiked(1, 7))
        assertTrue(preferences.isLiked(2, 7))
        assertTrue(preferences.isLiked(2, 8))
        assertEquals(listOf(DayAssignment("2025-01-01", 1, 7)), plans.getByDate("2025-01-01"))
        save(7, "Sin gustos", emptySet())
        assertTrue(database.mealPreferenceDao().getByMeal(7).isEmpty())
    }

    @Test fun invalidMemberRollsBackBothEditAndCreation() = runBlocking {
        database.mealDao().insert(MealEntity(7, "Antes", "Legado"))
        preferences.setPreference(1, 7, true)
        for (id in listOf<Long?>(7, null)) {
            try {
                save(id, "No guardar", linkedSetOf(2, 999))
                fail("Foreign key failure expected")
            } catch (_: android.database.sqlite.SQLiteConstraintException) { }
            assertEquals(listOf(MealEntity(7, "Antes", "Legado")), database.mealDao().getAll().first())
            assertTrue(preferences.isLiked(1, 7))
            assertFalse(preferences.isLiked(2, 7))
        }
    }

    @Test fun mealCentricObservationIncludesOnlyCurrentLikes() = runBlocking {
        val id = save(null, "Sopa", setOf(1))
        assertEquals(mapOf(id to setOf(1L)), preferences.observeLikesByMeal().first())
        preferences.setPreference(2, id, false)
        assertEquals(mapOf(id to setOf(1L)), preferences.observeLikesByMeal().first())
        save(id, "Sopa", setOf(2))
        assertEquals(mapOf(id to setOf(2L)), preferences.observeLikesByMeal().first())
        database.familyMemberDao().delete(FamilyMemberEntity(2, "Luis", 0))
        assertTrue(preferences.observeLikesByMeal().first().isEmpty())
    }

    @Test fun emptyFamilyAllowsCreationAndMissingEditDoesNotCreateMeal() = runBlocking {
        database.familyMemberDao().delete(FamilyMemberEntity(1, "Ana", 0))
        database.familyMemberDao().delete(FamilyMemberEntity(2, "Luis", 0))
        val id = save(null, "Sopa", emptySet())
        try {
            save(999, "Fantasma", emptySet())
            fail("Missing meal must not be recreated")
        } catch (_: IllegalArgumentException) { }
        assertEquals(listOf(id), meals.getAll().first().map { it.id })
        try {
            save(null, "  ", emptySet())
            fail("Blank name must be rejected")
        } catch (_: IllegalArgumentException) { }
        assertEquals(1, meals.getAll().first().size)
    }
}
