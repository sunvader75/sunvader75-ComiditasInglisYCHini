package com.comiditas.familia.ui.screens.members

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.ui.theme.MemberColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(
    viewModel: MembersViewModel,
    onBack: () -> Unit
) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<FamilyMember?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Miembros de la Familia") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (members.size < 6) {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir miembro")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Tienes ${members.size} miembros (máx. recomendado: 3)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(members, key = { it.id }) { member ->
                    MemberCard(
                        member = member,
                        onEdit = {
                            editingMember = member
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteMember(member) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        MemberDialog(
            member = editingMember,
            existingColors = members.map { it.color }.toSet(),
            onDismiss = {
                showDialog = false
                editingMember = null
            },
            onConfirm = { name, color ->
                if (editingMember != null) {
                    viewModel.updateMember(editingMember!!.copy(name = name, color = color))
                } else {
                    viewModel.addMember(name, color)
                }
                showDialog = false
                editingMember = null
            }
        )
    }
}

@Composable
private fun MemberCard(
    member: FamilyMember,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Color(member.color))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = member.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}

@Composable
private fun MemberDialog(
    member: FamilyMember?,
    existingColors: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf(member?.name ?: "") }
    var selectedColor by remember { mutableStateOf(member?.color ?: MemberColors.first().toArgb()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (member == null) "Nuevo Miembro" else "Editar Miembro") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )

                Text("Color:", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MemberColors.forEach { color ->
                        val colorInt = color.toArgb()
                        val isSelected = selectedColor == colorInt
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) androidx.compose.ui.graphics.Color.DarkGray else androidx.compose.ui.graphics.Color.Transparent
                                )
                                .padding(if (isSelected) 3.dp else 0.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = colorInt },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, selectedColor) },
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

private fun androidx.compose.ui.graphics.Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (this.alpha * 255).toInt(),
        (this.red * 255).toInt(),
        (this.green * 255).toInt(),
        (this.blue * 255).toInt()
    )
}
