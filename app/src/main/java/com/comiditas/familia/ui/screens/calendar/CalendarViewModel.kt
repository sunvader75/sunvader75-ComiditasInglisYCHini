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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
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
    private val _isLoading = MutableStateFlow(false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val _monthAssignments: StateFlow<Map<LocalDate, List<DayAssignment>>> = _currentMonth
        .flatMapLatest { month ->
            val startDate = month.atDay(1).format(formatter)
            val endDate = month.atEndOfMonth().format(formatter)
            assignMealsUseCase.getAssignmentsBetween(startDate, endDate)
                .map { assignments ->
                    assignments.groupBy {
                        LocalDate.parse(it.date, formatter)
                    }
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val uiState: StateFlow<CalendarUiState> = combine(
        _currentMonth,
        _selectedDate,
        getFamilyMembersUseCase(),
        getMealsUseCase(),
        combine(_monthAssignments, _message, _isLoading) { assignments, msg, loading -> Triple(assignments, msg, loading) }
    ) { month, date, members, meals, assignmentsAndMsg ->
        CalendarUiState(
            currentMonth = month,
            selectedDate = date,
            members = members,
            meals = meals,
            monthAssignments = assignmentsAndMsg.first,
            message = assignmentsAndMsg.second,
            isLoading = assignmentsAndMsg.third
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())

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

    fun observeDay(date: LocalDate) = assignMealsUseCase.getAssignments(date.format(formatter))

    fun getAssignmentsForDate(date: LocalDate): List<DayAssignment> {
        return _monthAssignments.value[date] ?: emptyList()
    }

    private fun persist(onSuccess: () -> Unit, action: suspend () -> Unit) {
        if (_isLoading.value) return
        _isLoading.value = true
        _message.value = null
        viewModelScope.launch {
            try {
                action()
                onSuccess()
            } catch (error: CancellationException) {
                throw error
            } catch (error: IllegalArgumentException) {
                _message.value = error.message ?: "El plan no es válido."
            } catch (error: Exception) {
                _message.value = "No se pudo guardar. Inténtalo de nuevo; el plan anterior se conserva."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun assignRandomly(date: LocalDate, onSuccess: () -> Unit) = persist(onSuccess) {
        require(assignMealsUseCase.assignRandomly(date.format(formatter))) {
            "No hay un plan de hasta dos comidas que guste a todos."
        }
    }

    fun saveDay(date: LocalDate, draft: Map<Long, Long>, onSuccess: () -> Unit) = persist(onSuccess) {
        assignMealsUseCase.saveDay(date.format(formatter), draft.map { (member, meal) ->
            DayAssignment(date.format(formatter), member, meal)
        })
    }

    fun clearDay(date: LocalDate, onSuccess: () -> Unit) = persist(onSuccess) {
        assignMealsUseCase.clearDay(date.format(formatter))
    }

    fun clearMessage() {
        _message.value = null
    }
}
