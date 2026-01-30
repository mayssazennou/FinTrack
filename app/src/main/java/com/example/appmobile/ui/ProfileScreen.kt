package com.example.appmobile.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions // <--- L'IMPORT QUI MANQUAIT
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization // <--- CELUI-CI AUSSI
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmobile.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

private val OceanBlue = Color(0xFF004E92)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onChangePasswordClick: () -> Unit
) {
    val auth = SupabaseClient.client.auth
    val currentUser = auth.currentUserOrNull()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- Gestion des données utilisateur ---
    var firstName by remember { mutableStateOf(currentUser?.userMetadata?.get("first_name")?.jsonPrimitive?.contentOrNull ?: "") }
    var lastName by remember { mutableStateOf(currentUser?.userMetadata?.get("last_name")?.jsonPrimitive?.contentOrNull ?: "") }
    val email = currentUser?.email ?: "Email non disponible"

    val initial = if (firstName.isNotEmpty()) firstName.first().uppercase() else "U"

    // État pour la boite de dialogue "Modifier Profil"
    var showEditDialog by remember { mutableStateOf(false) }

    // --- BOITE DE DIALOGUE : MODIFIER NOM/PRENOM ---
    if (showEditDialog) {
        var tempFirstName by remember { mutableStateOf(firstName) }
        var tempLastName by remember { mutableStateOf(lastName) }
        var isSaving by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Modifier mon profil", color = OceanBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = tempFirstName,
                        onValueChange = { tempFirstName = it },
                        label = { Text("Prénom") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OceanBlue, focusedLabelColor = OceanBlue)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempLastName,
                        onValueChange = { tempLastName = it },
                        label = { Text("Nom") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OceanBlue, focusedLabelColor = OceanBlue)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isSaving = true
                            try {
                                // MISE À JOUR SUPABASE
                                auth.updateUser {
                                    data = buildJsonObject {
                                        put("first_name", tempFirstName)
                                        put("last_name", tempLastName)
                                    }
                                }
                                // Mise à jour de l'affichage local
                                firstName = tempFirstName
                                lastName = tempLastName

                                Toast.makeText(context, "Profil mis à jour !", Toast.LENGTH_SHORT).show()
                                showEditDialog = false
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                    enabled = !isSaving
                ) {
                    if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    else Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Annuler", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    // --- ECRAN PRINCIPAL ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon Profil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OceanBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- CARTE UTILISATEUR ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Bouton Modifier (Crayon)
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = OceanBlue)
                    }

                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(OceanBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nom Complet
                        if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
                            Text(
                                text = "$firstName $lastName",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0A2342)
                            )
                        } else {
                            Text(
                                text = "Utilisateur",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0A2342)
                            )
                            Text(
                                text = "(Cliquez sur le crayon pour ajouter votre nom)",
                                fontSize = 12.sp,
                                color = OceanBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = email, fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Paramètres du compte",
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 8.dp),
                fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    // --- OPTION 1 : CHANGER MOT DE PASSE ---
                    ListItem(
                        headlineContent = { Text("Changer mot de passe") },
                        leadingContent = { Icon(Icons.Default.Lock, null, tint = OceanBlue) },
                        colors = ListItemDefaults.colors(containerColor = Color.White),
                        modifier = Modifier.clickable {
                            onChangePasswordClick()
                        }
                    )

                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    // --- OPTION 2 : DÉCONNEXION ---
                    ListItem(
                        headlineContent = { Text("Se déconnecter", color = Color.Red) },
                        leadingContent = { Icon(Icons.Default.Person, null, tint = Color.Red) },
                        colors = ListItemDefaults.colors(containerColor = Color.White),
                        modifier = Modifier.clickable { onLogout() }
                    )
                }
            }
        }
    }
}