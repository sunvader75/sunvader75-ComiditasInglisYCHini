package com.comiditas.familia.data.repository

import com.comiditas.familia.data.local.dao.FamilyMemberDao
import com.comiditas.familia.data.local.entity.FamilyMemberEntity
import com.comiditas.familia.data.model.FamilyMember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyMemberRepository @Inject constructor(
    private val dao: FamilyMemberDao
) {
    fun getAll(): Flow<List<FamilyMember>> = dao.getAll().map { list ->
        list.map { it.toModel() }
    }

    suspend fun getById(id: Long): FamilyMember? = dao.getById(id)?.toModel()

    suspend fun save(member: FamilyMember): Long {
        return if (member.id == 0L) {
            dao.insert(member.toEntity())
        } else {
            dao.update(member.toEntity())
            member.id
        }
    }

    suspend fun delete(member: FamilyMember) {
        dao.delete(member.toEntity())
    }

    suspend fun count(): Int = dao.count()

    private fun FamilyMemberEntity.toModel() = FamilyMember(
        id = id,
        name = name,
        color = color
    )

    private fun FamilyMember.toEntity() = FamilyMemberEntity(
        id = id,
        name = name,
        color = color
    )
}
