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

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario de Comidas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                onPrevious = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            WeekdayHeader()

            CalendarGrid(
                month = uiState.currentMonth,
                members = uiState.members,
                monthAssignments = uiState.monthAssignments,
                selectedDate = uiState.selectedDate,
                onDateSelected = { date ->
                    viewModel.selectDate(date)
                    selectedDate = date
                }
            )
        }
    }

    selectedDate?.let { date ->
        DayDetailDialog(
            date = date,
            members = uiState.members,
            meals = uiState.meals,
            assignments = uiState.monthAssignments[date] ?: emptyList(),
            onDismiss = { selectedDate = null },
            onRandomAssign = {
                viewModel.assignRandomly(date)
            },
            onManualAssign = { memberId, mealId ->
                viewModel.assignManually(date, memberId, mealId)
            },
            onRemoveAssignment = { memberId ->
                viewModel.removeAssignment(date, memberId)
            }
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
    onRandomAssign: () -> Unit,
    onManualAssign: (Long, Long) -> Unit,
    onRemoveAssignment: (Long) -> Unit
) {
    val assignmentMap = remember(assignments) { assignments.associateBy { it.memberId } }
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
                            val assignment = assignmentMap[member.id]
                            val meal = assignment?.let { mealMap[it.mealId] }

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

                                    if (meal != null) {
                                        TextButton(onClick = { onRemoveAssignment(member.id) }) {
                                            Text("Quitar")
                                        }
                                    } else {
                                        TextButton(onClick = { showManualAssign = member.id }) {
                                            Text("Asignar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onRandomAssign) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.size(4.dp))
                Text("Aleatorio")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )

    showManualAssign?.let { memberId ->
        ManualAssignDialog(
            memberName = members.find { it.id == memberId }?.name ?: "",
            meals = meals,
            onDismiss = { showManualAssign = null },
            onSelect = { mealId ->
                onManualAssign(memberId, mealId)
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
