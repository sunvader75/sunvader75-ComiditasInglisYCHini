package com.comiditas.familia.domain.usecase

import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.repository.FamilyMemberRepository
import javax.inject.Inject

class SaveFamilyMemberUseCase @Inject constructor(
    private val repository: FamilyMemberRepository
) {
    suspend operator fun invoke(member: FamilyMember): Long {
        return repository.save(member)
    }
}
