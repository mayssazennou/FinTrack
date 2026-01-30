package com.example.appmobile.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmobile.R
import com.example.appmobile.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    // --- ÉTATS ---
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Nouveaux états pour le nom et prénom
    var prenom by remember { mutableStateOf("") }
    var nom by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var isSignUpMode by remember { mutableStateOf(false) }
    var showConfirmationMessage by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val darkBlue = Color(0xFF0A2342)
    val oceanBlue = Color(0xFF004E92)
    val neonCyan = Color(0xFF00D2FF)
    val bgGradient = Brush.verticalGradient(colors = listOf(darkBlue, oceanBlue))

    // --- DIALOGUE MOT DE PASSE OUBLIÉ ---
    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf(email) }
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Réinitialiser le mot de passe") },
            text = {
                Column {
                    Text("Entrez votre email pour recevoir le lien :")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = resetEmail, onValueChange = { resetEmail = it }, label = { Text("Email") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            SupabaseClient.client.auth.resetPasswordForEmail(
                                email = resetEmail,
                                redirectUrl = "appmobile://auth"
                            )
                            Toast.makeText(context, "Email envoyé !", Toast.LENGTH_LONG).show()
                            showForgotPasswordDialog = false
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }) { Text("Envoyer", color = oceanBlue) }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) { Text("Annuler", color = Color.Gray) }
            },
            containerColor = Color.White
        )
    }

    // --- UI PRINCIPALE ---
    Box(
        modifier = Modifier.fillMaxSize().background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(100.dp).padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = buildAnnotatedString {
                        append("Bienvenue sur ")
                        withStyle(style = SpanStyle(color = oceanBlue, fontWeight = FontWeight.Bold)) { append("Fin") }
                        withStyle(style = SpanStyle(color = oceanBlue, fontWeight = FontWeight.Bold)) { append("Track") }
                    },
                    fontSize = 22.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = if (isSignUpMode) "Créez un compte sécurisé" else "Connectez-vous à votre espace",
                    fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 24.dp)
                )

                if (showConfirmationMessage) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("✉️ Vérifiez vos emails", fontWeight = FontWeight.Bold, color = Color(0xFF00695C))
                            Text("Lien envoyé à $email.", fontSize = 13.sp, color = Color(0xFF004D40))
                        }
                    }
                }

                // --- NOUVEAUX CHAMPS (Visibles seulement en mode Inscription) ---
                if (isSignUpMode) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = prenom,
                            onValueChange = { prenom = it },
                            label = { Text("Prénom") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = oceanBlue, focusedLabelColor = oceanBlue)
                        )
                        OutlinedTextField(
                            value = nom,
                            onValueChange = { nom = it },
                            label = { Text("Nom") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = oceanBlue, focusedLabelColor = oceanBlue)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email pro") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = oceanBlue, focusedLabelColor = oceanBlue)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = oceanBlue, focusedLabelColor = oceanBlue)
                )

                if (!isSignUpMode) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(onClick = { showForgotPasswordDialog = true }, contentPadding = PaddingValues(0.dp)) {
                            Text("Mot de passe oublié ?", color = oceanBlue, fontSize = 13.sp)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            showConfirmationMessage = false
                            try {
                                val auth = SupabaseClient.client.auth
                                if (isSignUpMode) {
                                    auth.signUpWith(Email) {
                                        this.email = email
                                        this.password = password
                                        this.data = buildJsonObject {
                                            put("first_name", prenom)
                                            put("last_name", nom)
                                        }
                                    }
                                    showConfirmationMessage = true
                                    isSignUpMode = false
                                    Toast.makeText(context, "Inscription réussie ! Vérifiez vos mails.", Toast.LENGTH_LONG).show()
                                } else {
                                    auth.signInWith(Email) {
                                        this.email = email
                                        this.password = password
                                    }
                                    Toast.makeText(context, "Connexion réussie", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004E92)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (isSignUpMode) "S'INSCRIRE" else "SE CONNECTER", fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(
                    onClick = {
                        isSignUpMode = !isSignUpMode
                        showConfirmationMessage = false
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(if (isSignUpMode) "Déjà un compte ? Se connecter" else "Pas de compte ? S'inscrire", color = oceanBlue)
                }
            }
        }
    }
}