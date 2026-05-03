package com.comiditas.familia.ui.screens.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.domain.usecase.DeleteFamilyMemberUseCase
import com.comiditas.familia.domain.usecase.GetFamilyMembersUseCase
import com.comiditas.familia.domain.usecase.SaveFamilyMemberUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MembersViewModel @Inject constructor(
    getFamilyMembersUseCase: GetFamilyMembersUseCase,
    private val saveFamilyMemberUseCase: SaveFamilyMemberUseCase,
    private val deleteFamilyMemberUseCase: DeleteFamilyMemberUseCase
) : ViewModel() {

    val members: StateFlow<List<FamilyMember>> = getFamilyMembersUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMember(name: String, color: Int) {
        viewModelScope.launch {
            saveFamilyMemberUseCase(FamilyMember(name = name, color = color))
        }
    }

    fun updateMember(member: FamilyMember) {
        viewModelScope.launch {
            saveFamilyMemberUseCase(member)
        }
    }

    fun deleteMember(member: FamilyMember) {
        viewModelScope.launch {
            deleteFamilyMemberUseCase(member)
        }
    }
}
