package com.comiditas.familia.ui.screens.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import com.comiditas.familia.domain.usecase.GetFamilyMembersUseCase
import com.comiditas.familia.domain.usecase.GetMealsUseCase
import com.comiditas.familia.domain.usecase.ManagePreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PreferencesUiState(
    val members: List<FamilyMember> = emptyList(),
    val meals: List<Meal> = emptyList(),
    val selectedMemberId: Long? = null,
    val preferences: Map<Long, Boolean> = emptyMap()
)

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    getFamilyMembersUseCase: GetFamilyMembersUseCase,
    getMealsUseCase: GetMealsUseCase,
    private val managePreferencesUseCase: ManagePreferencesUseCase
) : ViewModel() {

    private val _selectedMemberId = kotlinx.coroutines.flow.MutableStateFlow<Long?>(null)

    val uiState: StateFlow<PreferencesUiState> = combine(
        getFamilyMembersUseCase(),
        getMealsUseCase(),
        _selectedMemberId
    ) { members, meals, selectedId ->
        PreferencesUiState(
            members = members,
            meals = meals,
            selectedMemberId = selectedId ?: members.firstOrNull()?.id
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreferencesUiState())

    fun getPreferencesFlow(memberId: Long): Flow<Map<Long, Boolean>> =
        managePreferencesUseCase.getPreferences(memberId)

    fun selectMember(memberId: Long) {
        _selectedMemberId.value = memberId
    }

    fun togglePreference(memberId: Long, mealId: Long, liked: Boolean) {
        viewModelScope.launch {
            if (liked) {
                managePreferencesUseCase.setPreference(memberId, mealId, true)
            } else {
                managePreferencesUseCase.removePreference(memberId, mealId)
            }
        }
    }
}
