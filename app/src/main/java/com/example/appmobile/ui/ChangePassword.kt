package com.example.appmobile.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmobile.SupabaseClient
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@Composable
fun ChangePasswordScreen(onPasswordChanged: () -> Unit) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // On observe si la session est active
    val sessionStatus by SupabaseClient.client.auth.sessionStatus.collectAsState(initial = null)

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        // Bloquer l'affichage si la session n'est pas encore chargée par le lien
        if (sessionStatus !is SessionStatus.Authenticated) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF004E92))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Vérification du lien...", color = Color.Gray)
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Nouveau mot de passe",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A2342),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        "Saisissez votre nouveau mot de passe pour sécuriser le compte.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nouveau mot de passe") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmer le mot de passe") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (newPassword.length < 6) {
                                Toast.makeText(context, "Minimum 6 caractères requis", Toast.LENGTH_SHORT).show()
                            } else if (newPassword != confirmPassword) {
                                Toast.makeText(context, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
                            } else {
                                scope.launch {
                                    isLoading = true
                                    try {
                                        SupabaseClient.client.auth.refreshCurrentSession()

                                        SupabaseClient.client.auth.updateUser {
                                            password = newPassword
                                        }

                                        Toast.makeText(context, "Mot de passe modifié avec succès !", Toast.LENGTH_LONG).show()
                                        onPasswordChanged()

                                    } catch (e: Exception) {
                                        val errorMsg = e.message ?: "Erreur inconnue"
                                        if (errorMsg.contains("missing sub claim")) {
                                            Toast.makeText(context, "Session expirée. Veuillez renvoyer un email.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Erreur: $errorMsg", Toast.LENGTH_LONG).show()
                                        }
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004E92)),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("ENREGISTRER", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}