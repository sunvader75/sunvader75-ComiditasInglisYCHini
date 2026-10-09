package com.comiditas.familia.ui.screens.meals

import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MealsUiStateTest {
    @Test fun likesAreMealCentricAndUnknownMembersAreNotDisplayed() {
        val ana = FamilyMember(1, "Ana", 0)
        val luis = FamilyMember(2, "Luis", 0)
        val state = buildMealsUiState(listOf(Meal(7, "Sopa"), Meal(8, "Arroz")),
            listOf(ana, luis), mapOf(7L to setOf(2L, 999L)))
        assertEquals(listOf(luis), state.meals[0].likingMembers)
        assertEquals(setOf(2L), state.meals[0].likedMemberIds)
        assertTrue(state.meals[1].likingMembers.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test fun emptyFamilyAndZeroLikesAreValid() {
        val state = buildMealsUiState(listOf(Meal(7, "Sopa")), emptyList(), emptyMap())
        assertEquals(1, state.meals.size)
        assertTrue(state.members.isEmpty())
        assertTrue(state.meals.single().likedMemberIds.isEmpty())
    }

    @Test fun preferenceAndMemberChangesProduceFreshCardState() = runBlocking {
        val meals = MutableStateFlow(listOf(Meal(7, "Sopa")))
        val members = MutableStateFlow(listOf(FamilyMember(1, "Ana", 0)))
        val likes = MutableStateFlow<Map<Long, Set<Long>>>(emptyMap())
        val state = combine(meals, members, likes, ::buildMealsUiState)
        assertTrue(state.first().meals.single().likingMembers.isEmpty())
        likes.value = mapOf(7L to setOf(1L))
        assertEquals("Ana", state.first().meals.single().likingMembers.single().name)
        members.value = listOf(FamilyMember(1, "Ana María", 0))
        assertEquals("Ana María", state.first().meals.single().likingMembers.single().name)
        members.value = emptyList()
        assertTrue(state.first().meals.single().likingMembers.isEmpty())
    }
}
