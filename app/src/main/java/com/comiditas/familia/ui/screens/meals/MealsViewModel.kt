package com.comiditas.familia.ui.screens.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import com.comiditas.familia.domain.usecase.GetFamilyMembersUseCase
import com.comiditas.familia.domain.usecase.GetMealsUseCase
import com.comiditas.familia.domain.usecase.ManagePreferencesUseCase
import com.comiditas.familia.domain.usecase.SaveMealWithPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MealWithLikes(val meal: Meal, val likingMembers: List<FamilyMember>) {
    val likedMemberIds: Set<Long> get() = likingMembers.map { it.id }.toSet()
}

data class MealsUiState(
    val meals: List<MealWithLikes> = emptyList(),
    val members: List<FamilyMember> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

internal fun buildMealsUiState(
    meals: List<Meal>,
    members: List<FamilyMember>,
    likes: Map<Long, Set<Long>>
): MealsUiState = MealsUiState(
    meals = meals.map { meal ->
        MealWithLikes(meal, members.filter { it.id in likes[meal.id].orEmpty() })
    },
    members = members,
    isLoading = false
)

@HiltViewModel
class MealsViewModel @Inject constructor(
    getMealsUseCase: GetMealsUseCase,
    getFamilyMembersUseCase: GetFamilyMembersUseCase,
    managePreferencesUseCase: ManagePreferencesUseCase,
    private val saveMeal: SaveMealWithPreferencesUseCase
) : ViewModel() {
    private val isSaving = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)
    private val content = combine(
        getMealsUseCase(), getFamilyMembersUseCase(), managePreferencesUseCase.observeLikesByMeal(),
        ::buildMealsUiState
    )

    val uiState = combine(content, isSaving, error) { state, saving, message ->
        state.copy(isSaving = saving, error = message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MealsUiState())

    fun clearError() { error.value = null }

    fun save(mealId: Long?, name: String, likedMemberIds: Set<Long>, onSaved: () -> Unit) {
        if (isSaving.value) return
        isSaving.value = true
        error.value = null
        viewModelScope.launch {
            try {
                saveMeal(mealId, name, likedMemberIds)
                onSaved()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                error.value = "No se pudo guardar la comida. Inténtalo de nuevo."
            } finally {
                isSaving.value = false
            }
        }
    }
}
