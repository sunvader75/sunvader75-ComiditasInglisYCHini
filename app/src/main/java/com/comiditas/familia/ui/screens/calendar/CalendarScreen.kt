package com.comiditas.familia.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comiditas.familia.data.model.DayAssignment
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingWeek by remember { mutableStateOf<LocalDate?>(null) }
    var weekHasAssignments by remember { mutableStateOf<Boolean?>(null) }
    val weekStart = com.comiditas.familia.domain.optimizer.WeekMealPlanGenerator.monday(uiState.selectedDate)
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
                title = { Text("Calendario de Comidas") },
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
            MonthSelector(
                currentMonth = uiState.currentMonth,
                onPrevious = { if (!uiState.isLoading && pendingWeek == null) viewModel.previousMonth() },
                onNext = { if (!uiState.isLoading && pendingWeek == null) viewModel.nextMonth() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                enabled = !uiState.isLoading && selectedDate == null && pendingWeek == null,
                onClick = {
                    pendingWeek = weekStart
                    weekHasAssignments = null
                    viewModel.clearMessage()
                    checkWeek(weekStart)
                }
            ) { Text("Generar semana") }
            Text("${weekStart.format(rangeFormatter)} – ${weekStart.plusDays(6).format(rangeFormatter)}")

            WeekdayHeader()

            CalendarGrid(
                month = uiState.currentMonth,
                members = uiState.members,
                monthAssignments = uiState.monthAssignments,
                selectedDate = uiState.selectedDate,
                onDateSelected = { date ->
                    if (!uiState.isLoading && pendingWeek == null) {
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
        val persisted by remember(date) { viewModel.observeDay(date) }
            .collectAsStateWithLifecycle(initialValue = null)
        if (persisted != null) DayDetailDialog(
            date = date,
            members = uiState.members,
            meals = uiState.meals,
            assignments = persisted.orEmpty(),
            isLoading = uiState.isLoading,
            error = uiState.message,
            onDismiss = { if (!uiState.isLoading) selectedDate = null },
            onRandomAssign = { viewModel.assignRandomly(date) { selectedDate = null } },
            onSave = { draft -> viewModel.saveDay(date, draft) { selectedDate = null } },
            onClear = { viewModel.clearDay(date) { selectedDate = null } }
        )
    }
}

@Composable
private fun MonthSelector(
    currentMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es"))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mes anterior")
        }

        Text(
            text = currentMonth.format(formatter).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleLarge
        )

        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Mes siguiente")
        }
    }
}

@Composable
private fun WeekdayHeader() {
    val days = listOf("L", "M", "X", "J", "V", "S", "D")
    Row(modifier = Modifier.fillMaxWidth()) {
        days.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    members: List<FamilyMember>,
    monthAssignments: Map<LocalDate, List<DayAssignment>>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val days = remember(month) {
        val firstDayOfMonth = month.atDay(1)
        val daysInMonth = month.lengthOfMonth()
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
        val offset = if (firstDayOfWeek == 0) 6 else firstDayOfWeek - 1

        val list = mutableListOf<LocalDate?>()
        repeat(offset) { list.add(null) }
        for (day in 1..daysInMonth) {
            list.add(month.atDay(day))
        }
        list
    }

    val today = remember { LocalDate.now() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(
            count = days.size,
            key = { index ->
                val date = days[index]
                if (date != null) {
                    date.toString()
                } else {
                    "padding-${month.year}-${month.monthValue}-$index"
                }
            }
        ) { index ->
            val date = days[index]
            if (date != null) {
                val assignments = monthAssignments[date] ?: emptyList()
                DayCell(
                    date = date,
                    isSelected = date == selectedDate,
                    isToday = date == today,
                    assignments = assignments,
                    members = members,
                    onClick = { onDateSelected(date) }
                )
            } else {
                Box(modifier = Modifier.aspectRatio(1f))
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    assignments: List<DayAssignment>,
    members: List<FamilyMember>,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.secondary
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            // Mostrar indicadores de asignación
            if (assignments.isNotEmpty() && members.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    assignments.forEach { assignment ->
                        val member = members.find { it.id == assignment.memberId }
                        if (member != null) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(member.color))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayDetailDialog(
    date: LocalDate,
    members: List<FamilyMember>,
    meals: List<Meal>,
    assignments: List<DayAssignment>,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    error: String?,
    onRandomAssign: () -> Unit,
    onSave: (Map<Long, Long>) -> Unit,
    onClear: () -> Unit
) {
    var draft by remember(date) { mutableStateOf(assignments.associate { it.memberId to it.mealId }) }
    val mealMap = remember(meals) { meals.associateBy { it.id } }
    var showManualAssign by remember { mutableStateOf<Long?>(null) }

    val formatter = remember { DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = date.format(formatter).replaceFirstChar { it.uppercase() }
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (members.isEmpty()) {
                    Text("No hay miembros configurados.")
                } else if (meals.isEmpty()) {
                    Text("No hay comidas configuradas.")
                } else {
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
                                        Text(if (meal != null) "Cambiar" else "Asignar")
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
                Button(enabled = !isLoading, onClick = { onSave(draft) }) {
                    Text(if (isLoading) "Guardando…" else "Guardar")
                }
                TextButton(enabled = !isLoading, onClick = onRandomAssign) { Text("Aleatorio") }
                TextButton(enabled = !isLoading, onClick = onClear) { Text("Borrar todo el plan del día") }
            }
        },
        dismissButton = {
            TextButton(enabled = !isLoading, onClick = onDismiss) { Text("Cerrar") }
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
