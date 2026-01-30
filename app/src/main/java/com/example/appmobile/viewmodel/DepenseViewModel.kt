package com.example.appmobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmobile.data.Depense
import com.example.appmobile.data.SupabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DepenseState(
    val isLoading: Boolean = false,
    val depenses: List<Depense> = emptyList(),
    val totalDepenses: Double = 0.0,
    val errorMessage: String? = null,
    val budgetRestant: Double = 0.0
)

class DepenseViewModel(
    private val repository: SupabaseRepository = SupabaseRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(DepenseState())
    val state: StateFlow<DepenseState> = _state

    init {
        loadDepenses()
    }

    fun loadDepenses() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val depenses = repository.getDepenses()
                val total = depenses.sumOf { it.montant }
                _state.value = _state.value.copy(
                    isLoading = false,
                    depenses = depenses,
                    totalDepenses = total
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur de chargement: ${e.message}"
                )
            }
        }
    }

    fun addDepense(
        label: String,
        montant: Double,
        categorie: String,
        priorite: Int,
        description: String? = null
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val userId = repository.getCurrentUserId() ?: ""
                val newDepense = Depense(
                    userId = userId,
                    label = label,
                    montant = montant,
                    categorie = categorie,
                    priorite = priorite,
                    description = description,
                    date = System.currentTimeMillis()
                )
                repository.addDepense(newDepense)
                loadDepenses()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur d'ajout: ${e.message}"
                )
            }
        }
    }

    // Simuler l'impact d'un achat
    fun simulateImpact(montant: Double, revenus: Double): Map<String, Any> {
        val currentTotal = _state.value.totalDepenses
        val nouveauTotal = currentTotal + montant
        val budgetRestant = revenus - nouveauTotal
        val pourcentage = (nouveauTotal / revenus) * 100

        return mapOf(
            "nouveauTotal" to nouveauTotal,
            "budgetRestant" to budgetRestant,
            "pourcentageUtilise" to pourcentage,
            "recommandation" to when {
                pourcentage > 90 -> "⚠️ Attention ! Cet achat dépasserait 90% de votre budget"
                pourcentage > 75 -> "⚠️ Cet achat réduirait significativement votre budget"
                pourcentage > 50 -> "✓ Achat possible mais surveillez vos dépenses"
                else -> "✓ Cet achat est dans vos moyens"
            }
        )
    }

    // Calculer les dépenses par catégorie
    fun getDepensesByCategorie(): Map<String, Double> {
        return _state.value.depenses
            .groupBy { it.categorie }
            .mapValues { entry -> entry.value.sumOf { it.montant } }
    }

    // Calculer l'évolution mensuelle
    fun getMonthlyExpenses(): Map<String, Double> {
        val monthlyData = mutableMapOf<String, Double>()
        _state.value.depenses.forEach { depense ->
            val month = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.FRENCH)
                .format(java.util.Date(depense.date))
            monthlyData[month] = (monthlyData[month] ?: 0.0) + depense.montant
        }
        return monthlyData
    }

    // Obtenir les dépenses triées par priorité
    fun getDepensesByPriorite(): List<Depense> {
        return _state.value.depenses.sortedByDescending { it.priorite }
    }

    // Générer des conseils basés sur les habitudes
    fun getConseilsPersonnalises(): List<String> {
        val conseils = mutableListOf<String>()
        val depensesByCategorie = getDepensesByCategorie()

        // Analyser les catégories
        val totalDepenses = _state.value.totalDepenses

        depensesByCategorie.forEach { (categorie, montant) ->
            val pourcentage = (montant / totalDepenses) * 100
            when {
                categorie == "Achats" && pourcentage > 40 -> {
                    conseils.add("💡 Vos achats représentent ${pourcentage.toInt()}% de vos dépenses. Essayez de réduire les achats impulsifs.")
                }
                categorie == "Charges" && pourcentage > 50 -> {
                    conseils.add("⚠️ Les charges fixes sont élevées (${pourcentage.toInt()}%). Cherchez des moyens de les optimiser.")
                }
                categorie == "Épargne" && pourcentage < 10 -> {
                    conseils.add("📊 Votre épargne est faible. Essayez d'économiser au moins 10-20% de vos revenus.")
                }
            }
        }

        // Vérifier les dépenses prioritaires
        val hautePriorite = _state.value.depenses.filter { it.priorite == 3 }
        if (hautePriorite.size > _state.value.depenses.size / 2) {
            conseils.add("⚡ Trop de dépenses sont marquées comme prioritaires. Réévaluez vos priorités.")
        }

        if (conseils.isEmpty()) {
            conseils.add("✅ Vos finances semblent bien équilibrées ! Continuez ainsi.")
        }

        return conseils
    }
}