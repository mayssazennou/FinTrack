package com.example.appmobile.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- ENUMS POUR LES CATÉGORIES ---
enum class CategorieDepense(val displayName: String) {
    ACHATS("Achats"),
    CHARGES("Charges fixes"),
    DETTES("Dettes/Crédits"),
    EPARGNE("Épargne")
}

enum class SourceRevenu(val displayName: String) {
    SALAIRE("Salaire"),
    PROJET("Projet"),
    FREELANCE("Freelance"),
    INVESTISSEMENT("Investissement"),
    AUTRE("Autre")
}

enum class Priorite(val value: Int, val displayName: String, val libelle: Any) {
    FAIBLE(1,"Faible", libelle = "Faible" ),
    MOYENNE(2, "Moyenne", libelle = "Moyenne" ),
    HAUTE(3,"Haute", libelle ="Haute" );
    companion object {
        fun fromValue(value: Int): Priorite = values().find { it.value == value } ?: FAIBLE
    }}

// --- MODÈLE UTILISATEUR ---
@Serializable
data class UserProfile(
    val id: String? = null,
    val email: String? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null
)

// --- MODÈLE DÉPENSE ---
@Serializable
data class Depense(
    val id: Long? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("label") val label: String,
    val montant: Double,
    val categorie: String, // Achats, Charges, Dettes, Épargne
    val priorite: Int = 0, // 1=Faible, 2=Moyenne, 3=Élevée
    val description: String? = null,
    val date: Long // Timestamp (milliseconds)
)

// --- MODÈLE REVENU ---
@Serializable
data class Revenu(
    val id: Long? = null,
    @SerialName("user_id") val userId: String? = null,
    val source: String, // Salaire, Projet, Freelance, Autre
    val montant: Double,
    val description: String? = null,
    val date: Long
)

// --- ÉTAT DU DASHBOARD ---
data class DashboardState(
    val isLoading: Boolean = false,
    val depenses: List<Depense> = emptyList(),
    val transactions: List<Any> = emptyList(),
    val soldeTotal: Double = 0.0,
    val errorMessage: String? = null
)



// --- ENUMS POUR LES OBJECTIFS ---
enum class TypeInvestissement(val libelle: String) {
    EPARGNE("Épargne"),
    RETRAITE("Retraite"),
    IMMOBILIER("Immobilier"),
    ACTIONS("Actions"),
    FONDS("Fonds"),
    CRYPTO("Crypto"),
    ENTREPRENEURIAT("Entrepreneuriat"),
    VOYAGE("Voyage"),
    VOITURE("Voiture"),
    AUTRE("Autre")
}


// --- MODÈLE OBJECTIF INVESTISSEMENT ---
@Serializable
data class ObjectifInvestissement(
    val id: String = "",
    @SerialName("type_investissement")
    val typeInvestissement: String = TypeInvestissement.EPARGNE.name.lowercase(),
    val description: String = "",
    @SerialName("montant_cible")
    val montantCible: Double = 0.0,
    @SerialName("montant_actuel")
    val montantActuel: Double = 0.0,
    @SerialName("date_echeance")
    val dateEcheance: Long = System.currentTimeMillis(),
    val priorite: String = Priorite.MOYENNE.name.lowercase(),
    @SerialName("date_creation")
    val dateCreation: Long = System.currentTimeMillis(),
    @SerialName("user_id")
    val userId: String? = null
) {
    val pourcentageRealisation: Float
        get() = if (montantCible > 0) {
            ((montantActuel / montantCible) * 100).toFloat().coerceIn(0f, 100f)
        } else 0f
}

// --- ÉTAT POUR LE VIEWMODEL ---
data class ObjectifState(
    val isLoading: Boolean = false,
    val objectifs: List<ObjectifInvestissement> = emptyList(),
    val totalInvesti: Double = 0.0,
    val totalCible: Double = 0.0,
    val progressionMoyenne: Float = 0f,
    val errorMessage: String? = null
)

