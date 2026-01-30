package com.example.appmobile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmobile.data.SupabaseRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repository = SupabaseRepository()

    var userEmail by mutableStateOf("Chargement...")
        private set

    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)
    var isError by mutableStateOf(false)

    init {
        // Charger l'email au lancement
        userEmail = repository.getCurrentUserEmail() ?: "Utilisateur inconnu"
    }

    fun updatePassword() {
        if (newPassword.isBlank() || newPassword.length < 6) {
            message = "Le mot de passe doit faire au moins 6 caractères"
            isError = true
            return
        }
        if (newPassword != confirmPassword) {
            message = "Les mots de passe ne correspondent pas"
            isError = true
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                repository.updatePassword(newPassword)
                message = "Mot de passe mis à jour avec succès !"
                isError = false
                newPassword = ""
                confirmPassword = ""
            } catch (e: Exception) {
                message = "Erreur : ${e.message}"
                isError = true
            } finally {
                isLoading = false
            }
        }
    }

    fun signOut(onSignOutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.signOut()
                onSignOutComplete()
            } catch (e: Exception) {
                message = "Erreur lors de la déconnexion"
                isError = true
            }
        }
    }
}