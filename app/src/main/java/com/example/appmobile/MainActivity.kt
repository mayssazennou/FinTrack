package com.example.appmobile

import android.content.Intent
import android.os.Bundle
import android.Manifest // Pour la permission
import android.content.pm.PackageManager // Pour vérifier la permission
import android.os.Build // Pour vérifier la version Android
import androidx.core.app.ActivityCompat // Pour demander la permission
import androidx.core.content.ContextCompat // Pour vérifier la permission
import com.example.appmobile.ui.screens.NotificationScreen // Import de ton nouvel écran
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appmobile.ui.ChangePasswordScreen
import com.example.appmobile.ui.DashboardScreen
import com.example.appmobile.ui.LoginScreen
import com.example.appmobile.ui.ProfileScreen
import com.example.appmobile.ui.ObjectifScreen
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.handleDeeplinks
import kotlinx.coroutines.launch
import com.example.appmobile.ui.RevenuScreen
import com.example.appmobile.ui.DepenseScreen



class MainActivity : ComponentActivity() {

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSupabaseLink(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleSupabaseLink(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        val dataString = intent.data?.toString() ?: ""
        val isRecoveryLink = dataString.contains("type=recovery")

        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    val scope = rememberCoroutineScope()
                    val context = LocalContext.current

                    val sessionStatus by SupabaseClient.client.auth.sessionStatus.collectAsState(initial = null)
                    val currentSession = SupabaseClient.client.auth.currentSessionOrNull()

                    // Priorité au Deep Link recovery, sinon Dashboard si connecté, sinon Login
                    val startDestination = if (isRecoveryLink) {
                        "change_password"
                    } else if (currentSession != null) {
                        "dashboard"
                    } else {
                        "login"
                    }

                    NavHost(navController = navController, startDestination = startDestination) {

                        // --- ROUTE 1 : LOGIN ---
                        composable("login") {
                            LoginScreen(onLoginSuccess = {
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            })
                        }
                        composable("notifications") {
                            NotificationScreen()
                        }
                        composable("objectifs") {
                            ObjectifScreen()
                        }

                        // --- ROUTE 2 : DASHBOARD ---
                        composable("dashboard") {
                            DashboardScreen(
                                navController = navController,
                                onProfileClick = { navController.navigate("profile")},
                                onRevenusClick = { navController.navigate("revenus") },
                                onDepensesClick = { navController.navigate("depenses")},
                                onObjectifsClick = { navController.navigate("objectifs")}
                            )
                        }


                        // --- ROUTE 3 : PROFIL ---
                        composable("profile") {
                            ProfileScreen(
                                onBackClick = { navController.popBackStack() },
                                onLogout = {
                                    scope.launch {
                                        SupabaseClient.client.auth.signOut()
                                        navController.navigate("login") {
                                            popUpTo("dashboard") { inclusive = true }
                                        }
                                    }
                                },
                                // Navigation vers le changement de mot de passe
                                onChangePasswordClick = {
                                    navController.navigate("change_password")
                                }
                            )
                        }

                        // --- ROUTE 4 : CHANGER MOT DE PASSE ---
                        composable("change_password") {
                            ChangePasswordScreen(
                                onPasswordChanged = {
                                    // Nettoyage de l'intent pour éviter les boucles
                                    intent.data = null

                                    // Retour au Dashboard
                                    navController.navigate("dashboard") {
                                        popUpTo("change_password") { inclusive = true }
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("revenus") {
                            RevenuScreen()
                        }

                        composable("depenses") {
                            DepenseScreen()
                        }
                    }
                }
            }
        }
    }

    private fun handleSupabaseLink(intent: Intent) {
        try {
            SupabaseClient.client.handleDeeplinks(intent = intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}