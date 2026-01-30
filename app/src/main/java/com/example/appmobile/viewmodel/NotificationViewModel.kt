package com.example.appmobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmobile.SupabaseClient // ✅ On importe TON client existant
import com.example.appmobile.data.*
import com.example.appmobile.notifications.NotificationHelper

// 👇 CES IMPORTS SONT OBLIGATOIRES POUR CORRIGER LES ERREURS ROUGES (eq, filter, order) 👇
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.postgrest

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val notificationHelper = NotificationHelper(application)

    private val supabase = SupabaseClient.client

    private val _state = MutableStateFlow(NotificationState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                // Récupération de l'utilisateur
                val user = supabase.auth.currentUserOrNull()
                val userId = user?.id ?: return@launch

                // 1. Récupérer les rappels
                val rappels = supabase.from("rappels_paiement")
                    .select {
                        filter {
                            eq("user_id", userId) // ✅ "eq" ne sera plus rouge grâce aux imports
                        }
                    }
                    .decodeList<RappelPaiement>()

                // 2. Récupérer l'historique
                val historique = supabase.from("notification_historique")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                        order("date_envoi", order = Order.DESCENDING) // ✅ "order" ne sera plus rouge
                    }
                    .decodeList<NotificationHistorique>()

                _state.value = _state.value.copy(
                    rappels = rappels,
                    historique = historique,
                    isLoading = false
                )

                // 3. Vérifications
                checkAllNotifications(rappels)

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    private fun checkAllNotifications(rappels: List<RappelPaiement>) {
        checkRappelsPaiement(rappels)
        checkObjectifs()
    }

    private fun checkRappelsPaiement(rappels: List<RappelPaiement>) {
        val today = System.currentTimeMillis()
        val oneDayMillis = 86400000L

        rappels.filter { it.actif }.forEach { rappel ->
            val diffMillis = rappel.datePaiement - today
            val daysLeft = (diffMillis / oneDayMillis).toInt()

            if (daysLeft in 0..rappel.joursAvantRappel) {
                notificationHelper.sendRappelPaiementNotification(
                    titre = rappel.titre,
                    montant = rappel.montant,
                    joursRestants = daysLeft
                )
            }
        }
    }

    private fun checkObjectifs() {
        viewModelScope.launch {
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch

                val objectifs = supabase.from("objectifs_investissement")
                    .select {
                        filter { eq("user_id", userId) }
                    }
                    .decodeList<ObjectifInvestissement>()

                val today = System.currentTimeMillis()

                objectifs.forEach { obj ->
                    // Vérifie si la date est dépassée et que le montant n'est pas atteint
                    if (obj.dateEcheance < today && obj.montantActuel < obj.montantCible) {
                        notificationHelper.sendObjectifNotification(
                            titre = "Retard sur ${obj.typeInvestissement}",
                            message = "L'échéance est passée et l'objectif n'est pas atteint.",
                            type = "OBJECTIF_RETARD"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addRappel(rappel: RappelPaiement) {
        viewModelScope.launch {
            try {
                supabase.from("rappels_paiement").insert(rappel)
                loadData()
            } catch(e: Exception) {
                _state.value = _state.value.copy(errorMessage = "Erreur ajout rappel")
            }
        }
    }
}