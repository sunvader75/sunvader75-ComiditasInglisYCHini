package com.comiditas.familia.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
            Text("${weekStart.format(rangeFormatter)} – ${weekStart.plusDays(6).format(rangeFormatter)}")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(enabled = !uiState.isLoading && pendingWeek == null && selectedDate == null,
                    onClick = viewModel::previousWeek) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Semana anterior")
                }
                Button(
                    enabled = !uiState.isLoading && selectedDate == null && pendingWeek == null,
                    onClick = {
                        pendingWeek = weekStart
                        weekHasAssignments = null
                        viewModel.clearMessage()
                        checkWeek(weekStart)
                    }
                ) { Text("Generar semana") }
                IconButton(enabled = !uiState.isLoading && pendingWeek == null && selectedDate == null,
                    onClick = viewModel::nextWeek) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Semana siguiente")
                }
            }

            WeekMenuContent(
                state = uiState,
                onRetry = viewModel::retryWeek,
                onDateSelected = { date ->
                    if (!uiState.isLoading && selectedDate == null && pendingWeek == null) {
                        viewModel.selectDate(date)
                        selectedDate = date
                    }
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
internal fun WeekMenuContent(
    state: CalendarUiState,
    onRetry: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy", Locale("es")) }
    val days = WeekMenuPresentation.days(state.weekStart, state.week.assignments, state.members, state.meals)
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.week.error != null) item {
            Text(state.week.error, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = onRetry) { Text("Reintentar") }
        }
        items(days, key = { it.date.toString() }) { day ->
            Card(onClick = { onDateSelected(day.date) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(day.date.format(formatter).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium)
                    when {
                        state.week.error != null -> Text("Menú no disponible")
                        !state.week.loaded -> Text("Cargando semana…")
                        day.dishes.isEmpty() -> Text("Sin comidas asignadas")
                        else -> day.dishes.forEach { dish ->
                            Text(dish.name, style = MaterialTheme.typography.titleSmall)
                            Text(dish.recipients.joinToString())
                        }
                    }
                }
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
        title = { Text(if (changingMeal != null) "Cambiar comida" else
            date.format(formatter).replaceFirstChar { it.uppercase() }) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (changingMeal != null) {
                    if (isPreparing) Text("Preparando alternativas…")
                    preparationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (options?.isEmpty() == true) Text("No se encontraron alternativas. Revisa las comidas y los gustos. El plan guardado no se modifica.")
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        val visible = selectedProposal?.let { listOf(it) } ?: options.orEmpty()
                        visible.forEach { proposal ->
                            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(if (proposal.reorganizesDay) "Reorganiza el plan del día" else "Sustitución directa")
                                    proposal.assignments.entries.groupBy { it.value }.forEach { (meal, recipients) ->
                                        Text("${mealMap[meal]?.name ?: meal}: " + recipients.joinToString { row ->
                                            members.find { it.id == row.key }?.name ?: row.key.toString()
                                        })
                                    }
                                    if (selectedProposal == null) TextButton(enabled = !isLoading,
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
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(mealMap.getValue(meal).name)
                                Text(recipients.joinToString { row -> members.first { it.id == row.key }.name })
                                TextButton(enabled = !isLoading, onClick = {
                                    changingMeal = meal
                                    options = null
                                    selectedProposal = null
                                    prepare(meal)
                                }) { Text("Cambiar") }
                            }
                        }
                    }
                    Text("Editor manual")
                    members.forEach { member ->
                        key(member.id) {
                            val meal = draft[member.id]?.let { mealMap[it] }

                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color(member.color))
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = member.name,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = meal?.name ?: "Sin asignar",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (meal != null)
                                                MaterialTheme.colorScheme.onSurface
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    TextButton(enabled = !isLoading, onClick = { showManualAssign = member.id }) {
                                        Text(if (meal != null) "Editar" else "Asignar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (changingMeal != null) {
                    Button(enabled = !isLoading && !isPreparing && selectedProposal != null,
                        onClick = { selectedProposal?.let { onSave(it.assignments) } }) {
                        Text(if (isLoading) "Guardando…" else "Confirmar cambio")
                    }
                    if (preparationError != null) TextButton(enabled = !isPreparing,
                        onClick = { changingMeal?.let { prepare(it) } }) { Text("Reintentar") }
                } else {
                    Button(enabled = !isLoading, onClick = { onSave(draft) }) {
                        Text(if (isLoading) "Guardando…" else "Guardar")
                    }
                    TextButton(enabled = !isLoading, onClick = onRandomAssign) { Text("Aleatorio") }
                    TextButton(enabled = !isLoading, onClick = onClear) { Text("Borrar todo el plan del día") }
                }
            }
        },
        dismissButton = {
            TextButton(enabled = !isLoading, onClick = {
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
private fun ManualAssignDialog(
    memberName: String,
    meals: List<Meal>,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asignar comida a $memberName") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                meals.forEach { meal ->
                    TextButton(
                        onClick = { onSelect(meal.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(meal.name)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
