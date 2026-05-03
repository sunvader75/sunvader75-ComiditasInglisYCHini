package com.comiditas.familia.ui.screens.meals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.comiditas.familia.data.model.Meal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealsScreen(
    viewModel: MealsViewModel,
    onBack: () -> Unit
) {
    val meals by viewModel.meals.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editingMeal by remember { mutableStateOf<Meal?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comidas Disponibles") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir comida")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (meals.isEmpty()) {
                Text(
                    text = "No hay comidas aún. Pulsa + para añadir la primera.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(meals, key = { it.id }) { meal ->
                    MealCard(
                        meal = meal,
                        onEdit = {
                            editingMeal = meal
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteMeal(meal) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        MealDialog(
            meal = editingMeal,
            onDismiss = {
                showDialog = false
                editingMeal = null
            },
            onConfirm = { name, description ->
                if (editingMeal != null) {
                    viewModel.updateMeal(editingMeal!!.copy(name = name, description = description))
                } else {
                    viewModel.addMeal(name, description)
                }
                showDialog = false
                editingMeal = null
            }
        )
    }
}

@Composable
private fun MealCard(
    meal: Meal,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = meal.name,
                style = MaterialTheme.typography.titleMedium
            )
            if (meal.description.isNotBlank()) {
                Text(
                    text = meal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MealDialog(
    meal: Meal?,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(meal?.name ?: "") }
    var description by remember { mutableStateOf(meal?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (meal == null) "Nueva Comida" else "Editar Comida") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre *") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, description) },
                enabled = name.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
