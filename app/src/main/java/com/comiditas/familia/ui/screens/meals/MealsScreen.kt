package com.comiditas.familia.ui.screens.meals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealsScreen(viewModel: MealsViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editingMeal by remember { mutableStateOf<MealWithLikes?>(null) }

    fun dismiss() {
        showDialog = false
        editingMeal = null
        viewModel.clearError()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comidas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!state.isLoading) {
                FloatingActionButton(onClick = {
                    editingMeal = null
                    viewModel.clearError()
                    showDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir comida")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            when {
                state.isLoading -> Text("Cargando comidas…")
                state.meals.isEmpty() -> Text(
                    text = "No hay comidas aún. Pulsa + para añadir la primera.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.meals, key = { it.meal.id }) { item ->
                    MealCard(item, onEdit = {
                        editingMeal = item
                        viewModel.clearError()
                        showDialog = true
                    })
                }
            }
        }
    }

    if (showDialog) {
        MealDialog(
            meal = editingMeal?.meal,
            members = state.members,
            initialLikedMemberIds = editingMeal?.likedMemberIds.orEmpty(),
            isSaving = state.isSaving,
            error = state.error,
            onDismiss = { dismiss() },
            onConfirm = { name, likes ->
                viewModel.save(editingMeal?.meal?.id, name, likes, onSaved = { dismiss() })
            }
        )
    }
}

@Composable
internal fun MealCard(item: MealWithLikes, onEdit: () -> Unit) {
    Card(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(text = item.meal.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = if (item.likingMembers.isEmpty()) "Sin gustos asignados" else
                    "Les gusta a: ${item.likingMembers.joinToString { it.name }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun MealDialog(
    meal: Meal?,
    members: List<FamilyMember>,
    initialLikedMemberIds: Set<Long>,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (String, Set<Long>) -> Unit
) {
    var name by remember(meal?.id) { mutableStateOf(meal?.name.orEmpty()) }
    var likedIds by remember(meal?.id) { mutableStateOf(initialLikedMemberIds) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(if (meal == null) "Nueva comida" else "Editar comida") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre *") },
                    singleLine = true,
                    enabled = !isSaving
                )
                Text("¿A quién le gusta?", style = MaterialTheme.typography.titleSmall)
                if (members.isEmpty()) {
                    Text("No hay miembros aún. Puedes guardar la comida sin gustos asignados.")
                }
                members.forEach { member ->
                    FilterChip(
                        selected = member.id in likedIds,
                        onClick = {
                            likedIds = if (member.id in likedIds) likedIds - member.id else likedIds + member.id
                        },
                        label = { Text(member.name) },
                        enabled = !isSaving
                    )
                }
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, likedIds.intersect(members.map { it.id }.toSet())) },
                enabled = name.isNotBlank() && !isSaving
            ) {
                Text(if (isSaving) "Guardando…" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
        }
    )
}
