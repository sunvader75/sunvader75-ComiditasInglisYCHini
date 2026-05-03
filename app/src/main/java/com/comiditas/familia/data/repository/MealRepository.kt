package com.comiditas.familia.data.repository

import com.comiditas.familia.data.local.dao.MealDao
import com.comiditas.familia.data.local.entity.MealEntity
import com.comiditas.familia.data.model.Meal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealRepository @Inject constructor(
    private val dao: MealDao
) {
    fun getAll(): Flow<List<Meal>> = dao.getAll().map { list ->
        list.map { it.toModel() }
    }

    suspend fun getById(id: Long): Meal? = dao.getById(id)?.toModel()

    suspend fun save(meal: Meal): Long {
        return if (meal.id == 0L) {
            dao.insert(meal.toEntity())
        } else {
            dao.update(meal.toEntity())
            meal.id
        }
    }

    suspend fun delete(meal: Meal) {
        dao.delete(meal.toEntity())
    }

    private fun MealEntity.toModel() = Meal(
        id = id,
        name = name,
        description = description
    )

    private fun Meal.toEntity() = MealEntity(
        id = id,
        name = name,
        description = description
    )
}
