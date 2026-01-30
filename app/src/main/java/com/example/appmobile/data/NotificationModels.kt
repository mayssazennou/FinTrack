package com.example.appmobile.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Types de notifications
enum class TypeNotification(val displayName: String) {
    RAPPEL_PAIEMENT("Rappel de paiement"),
    DEPASSEMENT_BUDGET("Dépassement de budget"),
    OBJECTIF_PROGRESSION("Progression objectif"),
    OBJECTIF_RETARD("Retard objectif"),
    OBJECTIF_ATTEINT("Objectif atteint"),
    ECHEANCE_PROCHE("Échéance proche")
}

// Modèle pour les rappels de paiement
@Serializable
data class RappelPaiement(
    val id: String = "",
    @SerialName("user_id") val userId: String? = null,
    val titre: String,
    val description: String = "",
    val montant: Double,
    @SerialName("date_paiement") val datePaiement: Long, // Date du paiement
    val recurrent: Boolean = false, // Si c'est un paiement mensuel
    @SerialName("jours_avant_rappel") val joursAvantRappel: Int = 3, // Rappel X jours avant
    val actif: Boolean = true,
    @SerialName("date_creation") val dateCreation: Long = System.currentTimeMillis()
)

// Modèle pour l'historique des notifications envoyées
@Serializable
data class NotificationHistorique(
    val id: String = "",
    @SerialName("user_id") val userId: String? = null,
    val type: String, // TypeNotification
    val titre: String,
    val message: String,
    @SerialName("date_envoi") val dateEnvoi: Long = System.currentTimeMillis(),
    val lue: Boolean = false
)

// État pour le ViewModel
data class NotificationState(
    val isLoading: Boolean = false,
    val rappels: List<RappelPaiement> = emptyList(),
    val historique: List<NotificationHistorique> = emptyList(),
    val errorMessage: String? = null,
    val notificationsActives: Boolean = true
)