package com.example.appmobile.data


import com.example.appmobile.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.gotrue.auth

class SupabaseRepository {
    private val client = SupabaseClient.client

    // --- REVENUS ---
    suspend fun getRevenus(): List<Revenu> {
        return client.from("revenus").select().decodeList<Revenu>()
    }

    suspend fun addRevenu(revenu: Revenu) {
        client.from("revenus").insert(revenu)
    }

    // --- DEPENSES ---
    suspend fun getDepenses(): List<Depense> {
        // Attention : Assure-toi d'avoir créé la table "depenses" dans Supabase !
        return client.from("depenses").select().decodeList<Depense>()
    }

    suspend fun addDepense(depense: Depense) {
        client.from("depenses").insert(depense)
    }

    // Fonction pour changer le mot de passe
    suspend fun updatePassword(newPassword: String) {
        client.auth.modifyUser {
            password = newPassword
        }
    }

    // Fonction pour se déconnecter
    suspend fun signOut() {
        client.auth.signOut()
    }

    // Récupérer l'email de l'utilisateur actuel
    fun getCurrentUserEmail(): String? {
        return client.auth.currentUserOrNull()?.email
    }

    fun getCurrentUserId(): String? {

        return client.auth.currentUserOrNull()?.id
    }

    suspend fun sendPasswordResetEmail(email: String) {
        client.auth.resetPasswordForEmail(email)
    }

    // --- OBJECTIFS D'INVESTISSEMENT ---
    suspend fun getObjectifs(): List<ObjectifInvestissement> {
        return client.from("objectifs_investissement").select().decodeList<ObjectifInvestissement>()
    }

    suspend fun addObjectif(objectif: ObjectifInvestissement) {
        client.from("objectifs_investissement").insert(objectif)
    }

    suspend fun updateObjectif(objectif: ObjectifInvestissement) {
        client.from("objectifs_investissement").update(objectif) {
            filter {
                eq("id", objectif.id)
            }
        }
    }

    suspend fun deleteObjectif(id: String) {
        client.from("objectifs_investissement").delete {
            filter {
                eq("id", id)
            }
        }
    }

}

