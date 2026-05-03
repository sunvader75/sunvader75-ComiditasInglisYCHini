package com.comiditas.familia.ui.screens.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comiditas.familia.data.model.Meal
import com.comiditas.familia.domain.usecase.DeleteMealUseCase
import com.comiditas.familia.domain.usecase.GetMealsUseCase
import com.comiditas.familia.domain.usecase.SaveMealUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MealsViewModel @Inject constructor(
    getMealsUseCase: GetMealsUseCase,
    private val saveMealUseCase: SaveMealUseCase,
    private val deleteMealUseCase: DeleteMealUseCase
) : ViewModel() {

    val meals: StateFlow<List<Meal>> = getMealsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMeal(name: String, description: String = "") {
        viewModelScope.launch {
            saveMealUseCase(Meal(name = name, description = description))
        }
    }

    fun updateMeal(meal: Meal) {
        viewModelScope.launch {
            saveMealUseCase(meal)
        }
    }

    fun deleteMeal(meal: Meal) {
        viewModelScope.launch {
            deleteMealUseCase(meal)
        }
    }
}
