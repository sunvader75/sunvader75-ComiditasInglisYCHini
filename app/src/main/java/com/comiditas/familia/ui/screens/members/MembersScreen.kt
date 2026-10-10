package com.comiditas.familia.ui.screens.members

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.ui.components.FoodEmptyState
import com.comiditas.familia.ui.components.FoodIconContainer
import com.comiditas.familia.ui.components.FoodSectionHeader
import com.comiditas.familia.ui.theme.ComiditasFamiliaTheme
import com.comiditas.familia.ui.theme.FoodSpacing
import com.comiditas.familia.ui.theme.MemberColors

@Composable
fun MembersScreen(
    viewModel: MembersViewModel,
    onBack: () -> Unit
) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<FamilyMember?>(null) }

    MembersContent(
        members = members,
        onBack = onBack,
        onAdd = { showDialog = true },
        onEdit = { editingMember = it; showDialog = true },
        onDelete = { viewModel.deleteMember(it) }
    )

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MembersContent(
    members: List<FamilyMember>,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (FamilyMember) -> Unit,
    onDelete: (FamilyMember) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Miembros de la Familia", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (members.size < 6) {
                ExtendedFloatingActionButton(
                    onClick = onAdd,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Añadir miembro", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = FoodSpacing.screen),
            verticalArrangement = Arrangement.spacedBy(FoodSpacing.medium),
            contentPadding = PaddingValues(top = FoodSpacing.medium, bottom = 96.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(FoodSpacing.large),
                        horizontalArrangement = Arrangement.spacedBy(FoodSpacing.inset),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "${members.size} miembros en la mesa familiar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Preferencias y gustos configurados para el menú semanal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            if (members.isEmpty()) {
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        FoodEmptyState("Miembros de la Familia", "Añadir miembro", icon = Icons.Outlined.People)
                    }
                }
            }
            items(members, key = { it.id }) { member ->
                MemberCard(member, onEdit = { onEdit(member) }, onDelete = { onDelete(member) })
            }
        }
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
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(FoodSpacing.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FoodSpacing.inset)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(2.5.dp, Color(member.color), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Miembro de la familia",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MemberDialog(
    member: FamilyMember?,
    existingColors: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit,
    contentModifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(member?.name ?: "") }
    var selectedColor by remember { mutableStateOf(member?.color ?: MemberColors.first().toArgb()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { FoodIconContainer(Icons.Outlined.People) },
        title = { Text(if (member == null) "Nuevo Miembro" else "Editar Miembro") },
        text = {
            Column(
                modifier = contentModifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(FoodSpacing.medium)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Color:", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MemberColors.forEachIndexed { index, color ->
                        val colorInt = color.toArgb()
                        val isSelected = selectedColor == colorInt
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape)
                                .selectable(selected = isSelected, role = Role.RadioButton,
                                    onClick = { selectedColor = colorInt })
                                .semantics { contentDescription = "Color ${index + 1}" }
                                .border(if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(Modifier.size(32.dp).background(color, CircleShape))
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                        .background(MaterialTheme.colorScheme.surface, CircleShape).padding(2.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, selectedColor) },
                enabled = name.isNotBlank(),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(name = "Miembros · claro", showBackground = true)
@Preview(name = "Miembros · compacto 2x", showBackground = true, widthDp = 320, heightDp = 480, fontScale = 2f)
@Composable
private fun MembersPreview() {
    ComiditasFamiliaTheme {
        MembersContent(
            listOf(FamilyMember(1, "María del Carmen", MemberColors.first().toArgb()),
                FamilyMember(2, "Luis", MemberColors.last().toArgb())),
            onBack = {}, onAdd = {}, onEdit = {}, onDelete = {}
        )
    }
}

@Preview(name = "Editor de miembro · claro", showBackground = true)
@Preview(name = "Editor de miembro · compacto 2x", showBackground = true, widthDp = 320, heightDp = 480, fontScale = 2f)
@Composable
private fun MemberDialogPreview() {
    ComiditasFamiliaTheme {
        MemberDialog(null, emptySet(), onDismiss = {}, onConfirm = { _, _ -> })
    }
}

private fun androidx.compose.ui.graphics.Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (this.alpha * 255).toInt(),
        (this.red * 255).toInt(),
        (this.green * 255).toInt(),
        (this.blue * 255).toInt()
    )
}
