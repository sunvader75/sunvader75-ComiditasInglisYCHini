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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class CalendarUiState(
    val weekStart: LocalDate = WeekMenuPresentation.start(LocalDate.now()),
    val week: WeekObservation = WeekObservation(),
    val selectedDate: LocalDate = LocalDate.now(),
    val members: List<FamilyMember> = emptyList(),
    val meals: List<Meal> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null
)

data class WeekObservation(
    val start: LocalDate = WeekMenuPresentation.start(LocalDate.now()),
    val assignments: Map<LocalDate, List<DayAssignment>> = emptyMap(),
    val loaded: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    getFamilyMembersUseCase: GetFamilyMembersUseCase,
    getMealsUseCase: GetMealsUseCase,
    private val assignMealsUseCase: AssignMealsUseCase
) : ViewModel() {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val _weekStart = MutableStateFlow(WeekMenuPresentation.start(LocalDate.now()))
    private val _retry = MutableStateFlow(0)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _message = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val _week: StateFlow<WeekObservation> = combine(_weekStart, _retry) { start, _ -> start }
        .flatMapLatest { start ->
            assignMealsUseCase.getAssignmentsBetween(start.format(formatter), start.plusDays(6).format(formatter))
                .map { assignments -> WeekObservation(start, assignments.groupBy {
                    LocalDate.parse(it.date, formatter)
                }, loaded = true) }
                .onStart { emit(WeekObservation(start)) }
                .catch { error ->
                    if (error is CancellationException) throw error
                    emit(WeekObservation(start, error = "No se pudo cargar la semana. Inténtalo de nuevo."))
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeekObservation())

    val uiState: StateFlow<CalendarUiState> = combine(
        _weekStart,
        _selectedDate,
        getFamilyMembersUseCase(),
        getMealsUseCase(),
        combine(_week, _message, _isLoading) { assignments, msg, loading -> Triple(assignments, msg, loading) }
    ) { start, date, members, meals, assignmentsAndMsg ->
        CalendarUiState(
            weekStart = start,
            // Never expose the previous range while the new observer starts.
            week = assignmentsAndMsg.first.takeIf { it.start == start } ?: WeekObservation(start),
            selectedDate = date,
            members = members,
            meals = meals,
            message = assignmentsAndMsg.second,
            isLoading = assignmentsAndMsg.third
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())

    fun selectDate(date: LocalDate) {
        if (_isLoading.value) return
        _selectedDate.value = date
        _message.value = null
    }

    fun showWeek(date: LocalDate) {
        if (!_isLoading.value) _weekStart.value = WeekMenuPresentation.start(date)
    }

    fun previousWeek() = showWeek(WeekMenuPresentation.navigate(_weekStart.value, -1))
    fun nextWeek() = showWeek(WeekMenuPresentation.navigate(_weekStart.value, 1))
    fun retryWeek() { _retry.value++ }

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

    fun prepareWeek(date: LocalDate, onReady: (Boolean) -> Unit) = persist({}) {
        onReady(assignMealsUseCase.weekHasAssignments(date))
    }

    fun generateWeek(date: LocalDate, confirmed: Boolean, onSaved: () -> Unit,
                     onConfirmationRequired: () -> Unit) = persist({}) {
        when (assignMealsUseCase.generateWeek(date, confirmed)) {
            AssignMealsUseCase.WeekResult.SAVED -> onSaved()
            AssignMealsUseCase.WeekResult.CONFIRMATION_REQUIRED -> onConfirmationRequired()
            AssignMealsUseCase.WeekResult.IMPOSSIBLE -> throw IllegalArgumentException(
                "No hay un plan de hasta dos comidas que guste a todos.")
        }
    }

    private var preparationJob: kotlinx.coroutines.Job? = null
    private var preparationVersion = 0L
    private val _isPreparingReplacement = MutableStateFlow(false)
    val isPreparingReplacement: StateFlow<Boolean> = _isPreparingReplacement
    private val _replacementError = MutableStateFlow<String?>(null)
    val replacementError: StateFlow<String?> = _replacementError

    fun cancelReplacement() {
        preparationVersion++
        preparationJob?.cancel()
        _isPreparingReplacement.value = false
        _replacementError.value = null
    }

    fun prepareReplacement(date: LocalDate, draft: Map<Long, Long>, oldMealId: Long,
        onReady: (List<com.comiditas.familia.domain.optimizer.MealReplacementProposal>) -> Unit) {
        if (_isLoading.value || _isPreparingReplacement.value) return
        val frozenDraft = draft.toMap()
        val version = ++preparationVersion
        _isPreparingReplacement.value = true
        _replacementError.value = null
        preparationJob = viewModelScope.launch {
            try {
                val proposals = assignMealsUseCase.replacementProposals(frozenDraft, oldMealId)
                if (version == preparationVersion && date == _selectedDate.value) onReady(proposals)
            } catch (error: CancellationException) {
                throw error
            } catch (error: com.comiditas.familia.domain.validation.ExplainedMealPlanException) {
                if (version == preparationVersion) _replacementError.value = error.message
            } catch (error: Exception) {
                if (version == preparationVersion) _replacementError.value =
                    "No se pudieron preparar alternativas. Inténtalo de nuevo."
            } finally {
                if (version == preparationVersion) _isPreparingReplacement.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
