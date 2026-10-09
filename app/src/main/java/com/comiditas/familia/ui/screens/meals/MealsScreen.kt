package com.comiditas.familia.ui.screens.meals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import com.comiditas.familia.ui.components.FoodEmptyState
import com.comiditas.familia.ui.components.FoodSectionHeader
import com.comiditas.familia.ui.components.MemberNameBadge
import com.comiditas.familia.ui.theme.ComiditasFamiliaTheme

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
                ExtendedFloatingActionButton(
                    onClick = {
                        editingMeal = null
                        viewModel.clearError()
                        showDialog = true
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Añadir comida") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            when {
                state.isLoading -> FoodEmptyState(
                    title = "Cargando comidas…",
                    message = "Preparando tus comidas familiares",
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                )
                state.meals.isEmpty() -> FoodEmptyState(
                    title = "Tu recetario familiar",
                    message = "No hay comidas aún. Pulsa + para añadir la primera.",
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                )
            }
            LazyColumn(
                modifier = if (state.isLoading || state.meals.isEmpty()) Modifier else Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = item.meal.name, style = MaterialTheme.typography.titleLarge)
            Text(
                text = if (item.likingMembers.isEmpty()) "Sin gustos asignados" else
                    "Les gusta a: ${item.likingMembers.joinToString { it.name }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            item.likingMembers.forEach { member ->
                MemberNameBadge(member.name, Color(member.color))
            }
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
        title = { FoodSectionHeader(if (meal == null) "Nueva comida" else "Editar comida") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    singleLine = true,
                    enabled = !isSaving
                )
                Text("¿A quién le gusta?", style = MaterialTheme.typography.titleSmall)
                if (members.isEmpty()) {
                    Text("No hay miembros aún. Puedes guardar la comida sin gustos asignados.")
                }
                members.forEach { member ->
                    val selected = member.id in likedIds
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (selected) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                .selectable(
                                    selected = selected,
                                    enabled = !isSaving,
                                    role = Role.Checkbox,
                                    onClick = {
                                        likedIds = if (selected) likedIds - member.id else likedIds + member.id
                                    }
                                ).padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(Modifier.size(12.dp).background(Color(member.color), CircleShape))
                            Text(member.name, modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge)
                            Checkbox(checked = selected, onCheckedChange = null, enabled = !isSaving)
                        }
                    }
                }
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, likedIds.intersect(members.map { it.id }.toSet())) },
                enabled = name.isNotBlank() && !isSaving,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(if (isSaving) "Guardando…" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving,
                modifier = Modifier.heightIn(min = 48.dp)) { Text("Cancelar") }
        }
    )
}

private val previewMembers = listOf(
    FamilyMember(1, "Ana", 0xFFE57373.toInt()),
    FamilyMember(2, "Luis Miguel", 0xFF64B5F6.toInt()),
    FamilyMember(3, "María del Carmen", 0xFF81C784.toInt())
)

@Preview(name = "Comidas · tarjetas", showBackground = true)
@Preview(name = "Comidas · compacto 2×", widthDp = 320, heightDp = 480, fontScale = 2f)
@Composable
private fun MealCardsPreview() {
    ComiditasFamiliaTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MealCard(MealWithLikes(Meal(1, "Arroz con verduras de temporada"), previewMembers), {})
                MealCard(MealWithLikes(Meal(2, "Sopa"), emptyList()), {})
            }
        }
    }
}

@Preview(name = "Editor · selección", widthDp = 360, heightDp = 640)
@Preview(name = "Editor · compacto 2×", widthDp = 320, heightDp = 480, fontScale = 2f)
@Composable
private fun MealEditorPreview() {
    ComiditasFamiliaTheme {
        MealDialog(Meal(1, "Arroz con verduras"), previewMembers, setOf(1L, 3L),
            false, null, onDismiss = {}, onConfirm = { _, _ -> })
    }
}

@Preview(name = "Editor · vacío", widthDp = 320, heightDp = 480)
@Composable
private fun EmptyMealEditorPreview() {
    ComiditasFamiliaTheme {
        MealDialog(null, emptyList(), emptySet(), false, null,
            onDismiss = {}, onConfirm = { _, _ -> })
    }
}
