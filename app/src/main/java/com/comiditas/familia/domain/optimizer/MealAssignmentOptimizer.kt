package com.comiditas.familia.domain.optimizer

import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import javax.inject.Inject
import kotlin.random.Random

/**
 * Representa una asignación de comidas para un día.
 * @param assignments Mapa de mealId -> lista de memberIds que comen esa comida
 */
data class OptimizedAssignment(
    val assignments: Map<Long, List<Long>>,
    val mealDetails: Map<Long, Meal>
) {
    val numberOfMeals: Int get() = assignments.size
}

class MealAssignmentOptimizer @Inject constructor() {

    /**
     * Encuentra la asignación óptima de comidas para un día.
     * Prioridad: minimizar el número de comidas diferentes.
     *
     * @param members Lista de miembros de la familia
     * @param meals Lista de comidas disponibles
     * @param preferences Mapa de memberId -> Set de mealIds que le gustan
     * @param random Random para selección aleatoria
     * @return OptimizedAssignment o null si no hay solución
     */
    fun optimize(
        members: List<FamilyMember>,
        meals: List<Meal>,
        preferences: Map<Long, Set<Long>>,
        random: Random = Random.Default
    ): OptimizedAssignment? {
        if (members.isEmpty()) return null

        // Verificar que cada miembro tenga al menos una comida que le guste
        for (member in members) {
            val liked = preferences[member.id].orEmpty()
            if (liked.isEmpty()) return null
        }

        // Generar todas las opciones posibles ordenadas por número de comidas
        val allOptions = generateAllOptions(members)

        for (option in allOptions) {
            val validAssignments = findValidAssignments(option, meals, preferences, random)
            if (validAssignments != null) {
                val mealDetails = meals.associateBy { it.id }
                return OptimizedAssignment(validAssignments, mealDetails)
            }
        }

        return null
    }

    /**
     * Genera todas las posibles particiones de los miembros ordenadas por tamaño (menos comidas primero).
     */
    private fun generateAllOptions(members: List<FamilyMember>): List<List<List<Long>>> {
        val memberIds = members.map { it.id }
        val options = mutableListOf<List<List<Long>>>()

        // 1 comida para todos
        options.add(listOf(memberIds))

        // 2 comidas: todos los pares posibles + individual
        for (i in memberIds.indices) {
            for (j in i + 1 until memberIds.size) {
                val pair = listOf(memberIds[i], memberIds[j])
                val remaining = memberIds.filter { it != memberIds[i] && it != memberIds[j] }
                options.add(listOf(pair, remaining))
            }
        }

        // 3 comidas: cada uno individual
        options.add(memberIds.map { listOf(it) })

        return options
    }

    /**
     * Para una opción dada (lista de grupos), encuentra comidas válidas.
     * @return Map de mealId -> memberIds, o null si no es posible
     */
    private fun findValidAssignments(
        groups: List<List<Long>>,
        meals: List<Meal>,
        preferences: Map<Long, Set<Long>>,
        random: Random
    ): Map<Long, List<Long>>? {
        val result = mutableMapOf<Long, List<Long>>()
        val usedMeals = mutableSetOf<Long>()

        for (group in groups) {
            // Encontrar comidas que le gusten a TODOS en el grupo
            val likedByAll = meals.filter { meal ->
                group.all { memberId ->
                    preferences[memberId].orEmpty().contains(meal.id)
                }
            }

            if (likedByAll.isEmpty()) return null

            // Elegir una comida aleatoria que no se haya usado ya
            val available = likedByAll.filter { it.id !in usedMeals }
            val chosen = if (available.isNotEmpty()) {
                available.random(random)
            } else {
                // Si no hay disponibles nuevos, reutilizar uno (menos ideal, pero válido)
                likedByAll.random(random)
            }

            usedMeals.add(chosen.id)
            result[chosen.id] = group
        }

        return result
    }
}
