package com.comiditas.familia.ui.screens.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.outlined.Restaurant
import com.comiditas.familia.ui.components.FoodIconContainer
import com.comiditas.familia.ui.components.FoodSectionHeader
import com.comiditas.familia.ui.components.MemberNameBadge
import com.comiditas.familia.ui.theme.ComiditasFamiliaTheme
import com.comiditas.familia.ui.theme.FoodSpacing
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comiditas.familia.data.model.DayAssignment
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import com.comiditas.familia.domain.optimizer.MealReplacementProposal
import androidx.compose.runtime.DisposableEffect
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isPreparing by viewModel.isPreparingReplacement.collectAsStateWithLifecycle()
    val preparationError by viewModel.replacementError.collectAsStateWithLifecycle()
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingWeek by remember { mutableStateOf<LocalDate?>(null) }
    var weekHasAssignments by remember { mutableStateOf<Boolean?>(null) }
    val weekStart = uiState.weekStart
    val rangeFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es")) }

    fun checkWeek(date: LocalDate) {
        viewModel.prepareWeek(date) { weekHasAssignments = it }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            if (selectedDate == null && pendingWeek == null) viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menú semanal") },
                navigationIcon = {
                    IconButton(enabled = !uiState.isLoading && pendingWeek == null, onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            WeekMenuContent(
                state = uiState,
                onRetry = viewModel::retryWeek,
                onDateSelected = { date ->
                    if (!uiState.isLoading && selectedDate == null && pendingWeek == null) {
                        viewModel.selectDate(date)
                        selectedDate = date
                    }
                },
                header = {
                    WeekOverviewHeader(
                        weekStart = weekStart,
                        enabled = !uiState.isLoading && pendingWeek == null && selectedDate == null,
                        onPrevious = viewModel::previousWeek,
                        onNext = viewModel::nextWeek,
                        onGenerate = {
                            pendingWeek = weekStart
                            weekHasAssignments = null
                            viewModel.clearMessage()
                            checkWeek(weekStart)
                        }
                    )
                }
            )
        }
    }

    pendingWeek?.let { start ->
        AlertDialog(
            onDismissRequest = { if (!uiState.isLoading) pendingWeek = null },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = !uiState.isLoading,
                dismissOnClickOutside = !uiState.isLoading
            ),
            title = { Text(if (weekHasAssignments == true) "¿Reemplazar semana?" else "Generar semana") },
            text = {
                Column {
                    Text("${start.format(rangeFormatter)} – ${start.plusDays(6).format(rangeFormatter)}")
                    if (weekHasAssignments == true) Text("Se reemplazarán todos los planes de esta semana.")
                    uiState.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                Button(enabled = !uiState.isLoading, onClick = {
                    if (weekHasAssignments == null) checkWeek(start)
                    else viewModel.generateWeek(start, weekHasAssignments == true,
                        onSaved = { pendingWeek = null },
                        onConfirmationRequired = { weekHasAssignments = true })
                }) { Text(if (uiState.isLoading) "Guardando…" else if (weekHasAssignments == null)
                    "Reintentar" else if (weekHasAssignments == true) "Reemplazar" else "Generar") }
            },
            dismissButton = {
                TextButton(enabled = !uiState.isLoading, onClick = { pendingWeek = null }) { Text("Cancelar") }
            }
        )
    }

    selectedDate?.let { date ->
        if (!uiState.week.loaded) AlertDialog(
            onDismissRequest = { selectedDate = null },
            title = { Text("Editar día") },
            text = { Text(uiState.week.error ?: "Cargando semana…") },
            confirmButton = { if (uiState.week.error != null)
                TextButton(onClick = viewModel::retryWeek) { Text("Reintentar") } },
            dismissButton = { TextButton(onClick = { selectedDate = null }) { Text("Cerrar") } }
        ) else DayDetailDialog(
            date = date,
            members = uiState.members,
            meals = uiState.meals,
            assignments = uiState.week.assignments[date].orEmpty(),
            isLoading = uiState.isLoading,
            error = uiState.message,
            onDismiss = { if (!uiState.isLoading) selectedDate = null },
            onRandomAssign = { viewModel.assignRandomly(date) { selectedDate = null } },
            onSave = { draft -> viewModel.saveDay(date, draft) { selectedDate = null } },
            onClear = { viewModel.clearDay(date) { selectedDate = null } },
            isPreparing = isPreparing,
            preparationError = preparationError,
            onPrepare = { draft, old, ready -> viewModel.prepareReplacement(date, draft, old, ready) },
            onCancelReplacement = viewModel::cancelReplacement
        )
    }
}

@Composable
internal fun WeekOverviewHeader(
    weekStart: LocalDate,
    enabled: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onGenerate: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FoodSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious, enabled = enabled, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Semana anterior")
        }
        Button(onClick = onGenerate, enabled = enabled,
            modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
            Text("Generar semana")
        }
        IconButton(onClick = onNext, enabled = enabled, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Semana siguiente")
        }
    }
}

@Composable
internal fun WeekMenuContent(
    state: CalendarUiState,
    onRetry: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    header: (@Composable () -> Unit)? = null
) {
    val formatter = remember { DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy", Locale("es")) }
    val days = WeekMenuPresentation.days(state.weekStart, state.week.assignments, state.members, state.meals)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(FoodSpacing.medium)
    ) {
        if (header != null) item(key = "week-header") { header() }
        if (state.week.error != null) item(key = "week-error") {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Column(Modifier.fillMaxWidth().padding(FoodSpacing.medium)) {
                    Text(state.week.error)
                    TextButton(onClick = onRetry, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Reintentar")
                    }
                }
            }
        }
        items(days, key = { it.date.toString() }) { day ->
            Card(
                onClick = { onDateSelected(day.date) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.padding(FoodSpacing.medium),
                    verticalArrangement = Arrangement.spacedBy(FoodSpacing.medium)) {
                    Text(day.date.format(formatter).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary)
                    when {
                        state.week.error != null -> Text("Menú no disponible",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        !state.week.loaded -> Text("Cargando semana…",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        day.dishes.isEmpty() -> Text("Sin comidas asignadas",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        else -> day.dishes.forEach { dish ->
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Column(Modifier.fillMaxWidth().padding(FoodSpacing.medium),
                                    verticalArrangement = Arrangement.spacedBy(FoodSpacing.small)) {
                                    Text(dish.name, style = MaterialTheme.typography.titleSmall)
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ) {
                                        Text(dish.recipients.joinToString(),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Semana · compacta", widthDp = 320, heightDp = 480, showBackground = true)
@Preview(name = "Semana · texto 2x", widthDp = 320, heightDp = 480, fontScale = 2f, showBackground = true)
@Composable
private fun WeekOverviewPreview() {
    val start = LocalDate.of(2024, 12, 30)
    val state = CalendarUiState(
        weekStart = start,
        week = WeekObservation(start, mapOf(start to listOf(
            DayAssignment(start.toString(), 1, 1), DayAssignment(start.toString(), 2, 1)
        )), loaded = true),
        members = listOf(FamilyMember(1, "Ana", 0), FamilyMember(2, "Luis", 0)),
        meals = listOf(Meal(1, "Arroz con verduras de temporada"))
    )
    ComiditasFamiliaTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().padding(FoodSpacing.screen)) {
                WeekMenuContent(state, {}, {}, header = {
                    WeekOverviewHeader(start, true, {}, {}, {})
                })
            }
        }
    }
}

@Composable
internal fun DayDetailDialog(
    date: LocalDate,
    members: List<FamilyMember>,
    meals: List<Meal>,
    assignments: List<DayAssignment>,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    error: String?,
    onRandomAssign: () -> Unit,
    onSave: (Map<Long, Long>) -> Unit,
    onClear: () -> Unit,
    isPreparing: Boolean,
    preparationError: String?,
    onPrepare: (Map<Long, Long>, Long, (List<MealReplacementProposal>) -> Unit) -> Unit,
    onCancelReplacement: () -> Unit
) {
    var draft by remember(date) { mutableStateOf(assignments.associate { it.memberId to it.mealId }) }
    val mealMap = remember(meals) { meals.associateBy { it.id } }
    var showManualAssign by remember { mutableStateOf<Long?>(null) }

    var changingMeal by remember(date) { mutableStateOf<Long?>(null) }
    var options by remember(date) { mutableStateOf<List<MealReplacementProposal>?>(null) }
    var selectedProposal by remember(date) { mutableStateOf<MealReplacementProposal?>(null) }
    var requestVersion by remember(date) { mutableStateOf(0L) }
    val complete = members.size in 1..3 && draft.keys == members.map { it.id }.toSet() &&
        draft.values.toSet().size <= 2 && draft.values.all { it in mealMap }
    fun cancelChange() {
        requestVersion++
        onCancelReplacement()
        changingMeal = null
        options = null
        selectedProposal = null
    }
    fun prepare(old: Long) {
        val frozen = draft.toMap()
        val version = ++requestVersion
        onPrepare(frozen, old) { result ->
            if (version == requestVersion && changingMeal == old && draft == frozen) options = result
        }
    }
    DisposableEffect(date) { onDispose { onCancelReplacement() } }
    val formatter = remember { DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es")) }

    if (showManualAssign == null) AlertDialog(
        onDismissRequest = { if (!isLoading) { if (changingMeal != null) cancelChange() else onDismiss() } },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = !isLoading, dismissOnClickOutside = !isLoading),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(FoodSpacing.small)) {
                FoodIconContainer(Icons.Outlined.Restaurant)
                Text(if (changingMeal != null) "Cambiar comida" else
                    date.format(formatter).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineSmall)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(FoodSpacing.medium)
            ) {
                if (changingMeal != null) {
                    if (isPreparing) Text("Preparando alternativas…")
                    preparationError?.let { DayDialogError(it) }
                    if (options?.isEmpty() == true) Text("No se encontraron alternativas. Revisa las comidas y los gustos. El plan guardado no se modifica.")
                    Column(verticalArrangement = Arrangement.spacedBy(FoodSpacing.small)) {
                        val visible = selectedProposal?.let { listOf(it) } ?: options.orEmpty()
                        visible.forEach { proposal ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(Modifier.padding(FoodSpacing.medium),
                                    verticalArrangement = Arrangement.spacedBy(FoodSpacing.small)) {
                                    Text(if (proposal.reorganizesDay) "Reorganiza el plan del día" else "Sustitución directa",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary)
                                    proposal.assignments.entries.groupBy { it.value }.forEach { (meal, recipients) ->
                                        Text("${mealMap[meal]?.name ?: meal}: " + recipients.joinToString { row ->
                                            members.find { it.id == row.key }?.name ?: row.key.toString()
                                        })
                                    }
                                    if (selectedProposal == null) OutlinedButton(enabled = !isLoading,
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        onClick = { selectedProposal = proposal; }) { Text("Elegir") }
                                }
                            }
                        }
                    }
                } else if (members.isEmpty()) {
                    Text("No hay miembros configurados.")
                } else if (meals.isEmpty()) {
                    Text("No hay comidas configuradas.")
                } else {
                    if (complete) draft.entries.groupBy { it.value }.forEach { (meal, recipients) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(Modifier.padding(FoodSpacing.medium),
                                verticalArrangement = Arrangement.spacedBy(FoodSpacing.small)) {
                                Text(mealMap.getValue(meal).name, style = MaterialTheme.typography.titleMedium)
                                recipients.forEach { row ->
                                    val member = members.first { it.id == row.key }
                                    MemberNameBadge(member.name, Color(member.color))
                                }
                                OutlinedButton(enabled = !isLoading,
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), onClick = {
                                    changingMeal = meal
                                    options = null
                                    selectedProposal = null
                                    prepare(meal)
                                }) { Text("Cambiar") }
                            }
                        }
                    }
                    FoodSectionHeader("Editor manual")
                    members.forEach { member ->
                        key(member.id) {
                            val meal = draft[member.id]?.let { mealMap[it] }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(FoodSpacing.medium),
                                    verticalArrangement = Arrangement.spacedBy(FoodSpacing.small)
                                ) {
                                    MemberNameBadge(member.name, Color(member.color))
                                    Column {
                                        Text(
                                            text = meal?.name ?: "Sin asignar",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (meal != null)
                                                MaterialTheme.colorScheme.onSurface
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    OutlinedButton(enabled = !isLoading,
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        onClick = { showManualAssign = member.id }) {
                                        Text(if (meal != null) "Editar" else "Asignar")
                                    }
                                }
                            }
                        }
                    }
                }
                error?.let { DayDialogError(it) }
                // Actions share the content scroll so compact and large-text dialogs stay reachable.
                Column(verticalArrangement = Arrangement.spacedBy(FoodSpacing.small)) {
                    if (changingMeal != null) {
                        Button(enabled = !isLoading && !isPreparing && selectedProposal != null,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            onClick = { selectedProposal?.let { onSave(it.assignments) } }) {
                            Text(if (isLoading) "Guardando…" else "Confirmar cambio")
                        }
                        if (preparationError != null) TextButton(enabled = !isPreparing,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            onClick = { changingMeal?.let { prepare(it) } }) { Text("Reintentar") }
                    } else {
                        Button(enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            onClick = { onSave(draft) }) {
                            Text(if (isLoading) "Guardando…" else "Guardar")
                        }
                        OutlinedButton(enabled = !isLoading, onClick = onRandomAssign,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Aleatorio") }
                        TextButton(enabled = !isLoading, onClick = onClear,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Borrar todo el plan del día") }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(enabled = !isLoading, modifier = Modifier.heightIn(min = 48.dp), onClick = {
                if (changingMeal != null) cancelChange() else onDismiss()
            }) { Text(if (changingMeal != null) "Cancelar" else "Cerrar") }
        }
    )

    showManualAssign?.let { memberId ->
        ManualAssignDialog(
            memberName = members.find { it.id == memberId }?.name ?: "",
            meals = meals,
            onDismiss = { showManualAssign = null },
            onSelect = { mealId ->
                draft = draft + (memberId to mealId)
                showManualAssign = null
            }
        )
    }
}

@Composable
private fun DayDialogError(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Text(message, Modifier.padding(FoodSpacing.medium),
            style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(name = "Día · compacto", widthDp = 320, heightDp = 480)
@Preview(name = "Día · compacto 2x", widthDp = 320, heightDp = 480, fontScale = 2f)
@Composable
private fun DayDetailDialogPreview() {
    val date = LocalDate.of(2025, 1, 5)
    ComiditasFamiliaTheme {
        DayDetailDialog(
            date = date,
            members = listOf(FamilyMember(1, "María del Carmen", 0xFFB76E50.toInt()),
                FamilyMember(2, "Pablo", 0xFF66794C.toInt())),
            meals = listOf(Meal(1, "Arroz con verduras de temporada")),
            assignments = listOf(DayAssignment(date.toString(), 1, 1), DayAssignment(date.toString(), 2, 1)),
            onDismiss = {}, isLoading = false, error = "No se pudo guardar. Inténtalo de nuevo.",
            onRandomAssign = {}, onSave = {}, onClear = {}, isPreparing = false,
            preparationError = null, onPrepare = { _, _, _ -> }, onCancelReplacement = {}
        )
    }
}

@Preview(name = "Asignar · compacto", widthDp = 320, heightDp = 480)
@Preview(name = "Asignar · compacto 2x", widthDp = 320, heightDp = 480, fontScale = 2f)
@Composable
private fun ManualAssignDialogPreview() {
    ComiditasFamiliaTheme {
        ManualAssignDialog("María del Carmen",
            listOf(Meal(1, "Arroz con verduras de temporada"), Meal(2, "Lentejas con calabaza"),
                Meal(3, "Ensalada de garbanzos y tomate"), Meal(4, "Pasta con verduras")), {}, {})
    }
}

@Composable
private fun ManualAssignDialog(
    memberName: String,
    meals: List<Meal>,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(FoodSpacing.small)) {
                FoodIconContainer(Icons.Outlined.Restaurant)
                Text("Asignar comida a $memberName", style = MaterialTheme.typography.headlineSmall)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(FoodSpacing.small)
            ) {
                meals.forEach { meal ->
                    OutlinedButton(
                        onClick = { onSelect(meal.id) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text(meal.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Cancelar")
            }
        }
    )
}
