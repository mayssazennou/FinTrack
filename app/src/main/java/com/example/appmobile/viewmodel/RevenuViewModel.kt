package com.example.appmobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmobile.data.Revenu
import com.example.appmobile.data.SupabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RevenuState(
    val isLoading: Boolean = false,
    val revenus: List<Revenu> = emptyList(),
    val totalRevenus: Double = 0.0,
    val errorMessage: String? = null
)

class RevenuViewModel(
    private val repository: SupabaseRepository = SupabaseRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(RevenuState())
    val state: StateFlow<RevenuState> = _state

    init {
        loadRevenus()
    }

    fun loadRevenus() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val revenus = repository.getRevenus()
                val total = revenus.sumOf { it.montant }
                _state.value = _state.value.copy(
                    isLoading = false,
                    revenus = revenus,
                    totalRevenus = total
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur de chargement: ${e.message}"
                )
            }
        }
    }

    fun addRevenu(source: String, montant: Double, description: String? = null) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val userId = repository.getCurrentUserId() ?: ""
                val newRevenu = Revenu(
                    userId = userId,
                    source = source,
                    montant = montant,
                    description = description,
                    date = System.currentTimeMillis()
                )
                repository.addRevenu(newRevenu)
                loadRevenus() // Recharger la liste
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur d'ajout: ${e.message}"
                )
            }
        }
    }

    // Calculer les revenus par source
    fun getRevenusBySource(): Map<String, Double> {
        return _state.value.revenus
            .groupBy { it.source }
            .mapValues { entry -> entry.value.sumOf { it.montant } }
    }

    // Calculer l'évolution mensuelle
    fun getMonthlyRevenue(): Map<String, Double> {
        val monthlyData = mutableMapOf<String, Double>()
        _state.value.revenus.forEach { revenu ->
            val month = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.FRENCH)
                .format(java.util.Date(revenu.date))
            monthlyData[month] = (monthlyData[month] ?: 0.0) + revenu.montant
        }
        return monthlyData
    }
}