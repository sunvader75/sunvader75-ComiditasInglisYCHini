package com.comiditas.familia.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comiditas.familia.data.model.DayAssignment
import com.comiditas.familia.data.model.FamilyMember
import com.comiditas.familia.data.model.Meal
import com.comiditas.familia.domain.usecase.AssignMealsUseCase
import com.comiditas.familia.domain.usecase.GetFamilyMembersUseCase
import com.comiditas.familia.domain.usecase.GetMealsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val members: List<FamilyMember> = emptyList(),
    val meals: List<Meal> = emptyList(),
    val monthAssignments: Map<LocalDate, List<DayAssignment>> = emptyMap(),
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    getFamilyMembersUseCase: GetFamilyMembersUseCase,
    getMealsUseCase: GetMealsUseCase,
    private val assignMealsUseCase: AssignMealsUseCase
) : ViewModel() {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _message = MutableStateFlow<String?>(null)

    private val _monthAssignments = MutableStateFlow<Map<LocalDate, List<DayAssignment>>>(emptyMap())

    val uiState: StateFlow<CalendarUiState> = combine(
        _currentMonth,
        _selectedDate,
        getFamilyMembersUseCase(),
        getMealsUseCase(),
        combine(_monthAssignments, _message) { assignments, msg -> assignments to msg }
    ) { month, date, members, meals, assignmentsAndMsg ->
        CalendarUiState(
            currentMonth = month,
            selectedDate = date,
            members = members,
            meals = meals,
            monthAssignments = assignmentsAndMsg.first,
            message = assignmentsAndMsg.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())

    init {
        viewModelScope.launch {
            _currentMonth.collect { month ->
                loadMonthAssignments(month)
            }
        }
    }

    private suspend fun loadMonthAssignments(month: YearMonth) {
        val startDate = month.atDay(1).format(formatter)
        val endDate = month.atEndOfMonth().format(formatter)

        assignMealsUseCase.getAssignmentsBetween(startDate, endDate)
            .collect { assignments ->
                val map = assignments.groupBy {
                    LocalDate.parse(it.date, formatter)
                }
                _monthAssignments.value = map
            }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _message.value = null
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun getAssignmentsForDate(date: LocalDate): List<DayAssignment> {
        return _monthAssignments.value[date] ?: emptyList()
    }

    fun assignRandomly(date: LocalDate) {
        viewModelScope.launch {
            _message.value = null
            val success = assignMealsUseCase.assignRandomly(date.format(formatter))
            _message.value = if (success) {
                "Comidas asignadas correctamente"
            } else {
                "No se pudo asignar. Verifica que haya comidas y gustos configurados."
            }
            // Recargar asignaciones del mes
            loadMonthAssignments(_currentMonth.value)
        }
    }

    fun assignManually(date: LocalDate, memberId: Long, mealId: Long) {
        viewModelScope.launch {
            assignMealsUseCase.assignManually(date.format(formatter), memberId, mealId)
            loadMonthAssignments(_currentMonth.value)
        }
    }

    fun removeAssignment(date: LocalDate, memberId: Long) {
        viewModelScope.launch {
            assignMealsUseCase.removeAssignment(date.format(formatter), memberId)
            loadMonthAssignments(_currentMonth.value)
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
