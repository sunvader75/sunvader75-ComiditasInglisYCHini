package com.comiditas.familia.domain.usecase

import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.repository.FamilyMemberRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFamilyMembersUseCase @Inject constructor(
    private val repository: FamilyMemberRepository
) {
    operator fun invoke(): Flow<List<FamilyMember>> = repository.getAll()
}
