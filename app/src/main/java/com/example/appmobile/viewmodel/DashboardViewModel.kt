package com.example.appmobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmobile.SupabaseClient
import com.example.appmobile.data.DashboardState
import com.example.appmobile.data.Depense
import com.example.appmobile.data.Revenu
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    // État initial
    private val _state = MutableStateFlow(DashboardState(isLoading = true))
    val state = _state.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // 1. Récupérer les données
                val depensesList = SupabaseClient.client.from("depenses").select().decodeList<Depense>()
                val revenusList = SupabaseClient.client.from("revenus").select().decodeList<Revenu>()

                val solde = revenusList.sumOf { it.montant } - depensesList.sumOf { it.montant }

                // 2. FUSIONNER SANS NOUVELLE CLASSE
                val combinedList = (depensesList as List<Any>) + (revenusList as List<Any>)

                // 3. TRIER
                val sortedTransactions = combinedList.sortedByDescending { item ->
                    when (item) {
                        is Depense -> item.date
                        is Revenu -> item.date
                        else -> 0L
                    }
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        depenses = depensesList,
                        transactions = sortedTransactions,
                        soldeTotal = solde
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}



