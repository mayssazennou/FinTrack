package com.example.appmobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmobile.data.ObjectifInvestissement
import com.example.appmobile.data.ObjectifState
import com.example.appmobile.data.SupabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class ObjectifViewModel(
    private val repository: SupabaseRepository = SupabaseRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ObjectifState())
    val state: StateFlow<ObjectifState> = _state

    init {
        loadObjectifs()
    }

    fun loadObjectifs() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val objectifs = repository.getObjectifs()
                val totalInvesti = objectifs.sumOf { it.montantActuel }
                val totalCible = objectifs.sumOf { it.montantCible }
                val progression = if (totalCible > 0) {
                    ((totalInvesti / totalCible) * 100).toFloat()
                } else 0f

                _state.value = _state.value.copy(
                    isLoading = false,
                    objectifs = objectifs.sortedByDescending { it.dateCreation },
                    totalInvesti = totalInvesti,
                    totalCible = totalCible,
                    progressionMoyenne = progression
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur: ${e.message}"
                )
            }
        }
    }

    fun addObjectif(
        typeInvestissement: String,
        description: String,
        montantCible: Double,
        dateEcheance: Long,
        priorite: String
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val userId = repository.getCurrentUserId() ?: ""
                val newObjectif = ObjectifInvestissement(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    typeInvestissement = typeInvestissement.lowercase(),
                    description = description,
                    montantCible = montantCible,
                    montantActuel = 0.0,
                    dateEcheance = dateEcheance,
                    priorite = priorite.lowercase(),
                    dateCreation = System.currentTimeMillis()
                )
                repository.addObjectif(newObjectif)
                loadObjectifs()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur d'ajout: ${e.message}"
                )
            }
        }
    }

    fun updateMontantActuel(objectif: ObjectifInvestissement, nouveauMontant: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val updated = objectif.copy(montantActuel = nouveauMontant)
                repository.updateObjectif(updated)
                loadObjectifs()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur de mise à jour: ${e.message}"
                )
            }
        }
    }

    fun deleteObjectif(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                repository.deleteObjectif(id)
                loadObjectifs()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur de suppression: ${e.message}"
                )
            }
        }
    }

    // Calculer les jours restants
    fun getJoursRestants(echeance: Long): Int {
        val now = System.currentTimeMillis()
        val diff = echeance - now
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    // Générer des conseils personnalisés
    fun getConseils(objectif: ObjectifInvestissement, revenusMensuels: Double): String {
        val progression = objectif.pourcentageRealisation
        val joursRestants = getJoursRestants(objectif.dateEcheance)
        val montantRestant = objectif.montantCible - objectif.montantActuel

        return when {
            progression >= 100f -> "🎉 Objectif atteint ! Félicitations !"
            joursRestants < 0 -> "⚠️ Échéance dépassée. Revoyez votre objectif."
            joursRestants < 30 && progression < 80 -> {
                val epargneJour = if (joursRestants > 0) montantRestant / joursRestants else montantRestant
                "⏰ Plus que $joursRestants jours ! Économisez ${String.format("%.0f", epargneJour)} MAD/jour"
            }
            progression < 25 -> {
                val moisRestants = joursRestants / 30.0
                val epargneMois = if (moisRestants > 0) montantRestant / moisRestants else montantRestant
                val pourcentageRevenu = if (revenusMensuels > 0) (epargneMois / revenusMensuels) * 100 else 0.0
                "💡 Prévoyez ${String.format("%.0f", epargneMois)} MAD/mois (${String.format("%.1f", pourcentageRevenu)}% de vos revenus)"
            }
            else -> "✅ Vous progressez bien ! Continuez sur cette lancée."
        }
    }
}