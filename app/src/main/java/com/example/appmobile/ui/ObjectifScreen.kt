package com.example.appmobile.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appmobile.data.ObjectifInvestissement
import com.example.appmobile.data.Priorite
import com.example.appmobile.data.TypeInvestissement
import com.example.appmobile.viewmodel.ObjectifViewModel
import com.example.appmobile.viewmodel.RevenuViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val PurplePrimary = Color(0xFF9C27B0)
private val PurpleDark = Color(0xFF7B1FA2)
private val PurpleLight = Color(0xFFF3E5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectifScreen(
    objectifViewModel: ObjectifViewModel = viewModel(),
    revenuViewModel: RevenuViewModel = viewModel()
) {
    val objectifState by objectifViewModel.state.collectAsState()
    val revenuState by revenuViewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Objectifs d'Investissement",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PurplePrimary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PurplePrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Ajouter",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F7FA))
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = PurplePrimary,
                indicator = { tabPositions ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.BottomStart)
                            .offset(x = tabPositions[selectedTab].left)
                            .width(tabPositions[selectedTab].width)
                            .height(3.dp)
                            .background(PurplePrimary)
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Vue d'ensemble",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Conseils",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            when (selectedTab) {
                0 -> ObjectifsOverviewTab(objectifState, objectifViewModel)
                // Ici on suppose que le state revenu contient totalRevenus.
                // Si ce n'est pas le cas, remplacez par 0.0 temporairement.
                1 -> ObjectifsConseilsTab(objectifState, 15000.0, objectifViewModel)
            }
        }
    }

    if (showAddDialog) {
        AddObjectifDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { type, description, montant, echeance, priorite ->
                objectifViewModel.addObjectif(type, description, montant, echeance, priorite)
                showAddDialog = false
            }
        )
    }

    objectifState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { objectifViewModel.loadObjectifs() },
            title = { Text("Erreur") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { objectifViewModel.loadObjectifs() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ObjectifsOverviewTab(
    state: com.example.appmobile.data.ObjectifState,
    viewModel: ObjectifViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(PurplePrimary, PurpleDark)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Progression Globale",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${String.format("%.1f", state.progressionMoyenne)}%",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Investi",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        NumberFormat.getCurrencyInstance(Locale("fr", "MA"))
                                            .format(state.totalInvesti),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Cible",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        NumberFormat.getCurrencyInstance(Locale("fr", "MA"))
                                            .format(state.totalCible),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Mes Objectifs (${state.objectifs.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
        }

        if (state.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PurplePrimary)
                }
            }
        } else if (state.objectifs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PurpleLight)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Aucun objectif défini",
                                color = PurplePrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        } else {
            items(state.objectifs) { objectif ->
                ObjectifCard(objectif, viewModel)
            }
        }
    }
}

@Composable
fun ObjectifCard(
    objectif: ObjectifInvestissement,
    viewModel: ObjectifViewModel
) {
    val progression = objectif.pourcentageRealisation
    val joursRestants = viewModel.getJoursRestants(objectif.dateEcheance)

    // CORRECTION : Conversion sécurisée locale
    val typeEnum = safeGetTypeEnum(objectif.typeInvestissement)
    val icon = getTypeIcon(typeEnum.name)
    val color = getTypeColor(typeEnum.name)

    var showUpdateDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        typeEnum.libelle, // Utilisation du libelle de l'enum
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF2C3E50)
                    )
                    if (objectif.description.isNotBlank()) {
                        Text(
                            objectif.description,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            maxLines = 1
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ajouter montant") },
                            onClick = {
                                showMenu = false
                                showUpdateDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Supprimer") },
                            onClick = {
                                showMenu = false
                                showDeleteConfirm = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF44336))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = (progression / 100f).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    NumberFormat.getCurrencyInstance(Locale("fr", "MA")).format(objectif.montantActuel),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    NumberFormat.getCurrencyInstance(Locale("fr", "MA")).format(objectif.montantCible),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        convertMillisToDate(objectif.dateEcheance),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        joursRestants < 0 -> Color(0xFFF44336)
                        joursRestants < 30 -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    }
                ) {
                    Text(
                        when {
                            joursRestants < 0 -> "Expiré"
                            else -> "$joursRestants jours"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    if (showUpdateDialog) {
        UpdateMontantDialog(
            objectif = objectif,
            onDismiss = { showUpdateDialog = false },
            onUpdate = { nouveauMontant ->
                viewModel.updateMontantActuel(objectif, nouveauMontant)
                showUpdateDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Confirmer la suppression") },
            text = { Text("Voulez-vous vraiment supprimer cet objectif ?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteObjectif(objectif.id)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }

}

@Composable
fun ObjectifsConseilsTab(
    state: com.example.appmobile.data.ObjectifState,
    totalRevenus: Double,
    viewModel: ObjectifViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFF9800).copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Conseils d'Investissement",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50)
                        )
                        Text(
                            "Stratégies personnalisées",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        items(state.objectifs) { objectif ->
            // CORRECTION : Utilisation de la conversion safe locale
            val typeEnum = safeGetTypeEnum(objectif.typeInvestissement)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PurpleLight),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        typeEnum.libelle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PurpleDark
                    )
                    if (objectif.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            objectif.description,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        viewModel.getConseils(objectif, totalRevenus),
                        fontSize = 14.sp,
                        color = Color(0xFF546E7A),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddObjectifDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Double, Long, String) -> Unit
) {
    var typeInvestissement by remember { mutableStateOf(TypeInvestissement.EPARGNE.name.lowercase()) }
    var description by remember { mutableStateOf("") }
    var montant by remember { mutableStateOf("") }
    var priorite by remember { mutableStateOf(Priorite.MOYENNE.name.lowercase()) }
    var expandedType by remember { mutableStateOf(false) }
    var expandedPriorite by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.YEAR, 1)
    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvel Objectif", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = it }
                ) {
                    OutlinedTextField(
                        value = safeGetTypeEnum(typeInvestissement).libelle,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type d'investissement") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            focusedLabelColor = PurplePrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        TypeInvestissement.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.libelle) },
                                onClick = {
                                    typeInvestissement = type.name.lowercase()
                                    expandedType = false
                                },
                                leadingIcon = {
                                    Icon(getTypeIcon(type.name.lowercase()), contentDescription = null)
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePrimary,
                        focusedLabelColor = PurplePrimary
                    )
                )

                OutlinedTextField(
                    value = montant,
                    onValueChange = { montant = it },
                    label = { Text("Montant cible (MAD)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePrimary,
                        focusedLabelColor = PurplePrimary
                    )
                )


                OutlinedTextField(
                    value = date,
                    onValueChange = { date=it },
                    label = { Text("Échéance") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePrimary,
                        focusedLabelColor = PurplePrimary
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = expandedPriorite,
                    onExpandedChange = { expandedPriorite = it }
                ) {
                    val currentPrio = Priorite.values().find { it.name.lowercase() == priorite } ?: Priorite.MOYENNE
                    OutlinedTextField(
                        value = currentPrio.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priorité") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriorite) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            focusedLabelColor = PurplePrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPriorite,
                        onDismissRequest = { expandedPriorite = false }
                    ) {
                        Priorite.values().forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.displayName) },
                                onClick = {
                                    priorite = p.name.lowercase()
                                    expandedPriorite = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dateEcheanceLong = dateToBigInt(date)
                    val montantDouble = montant.toDoubleOrNull()
                    if (montantDouble != null && montantDouble > 0 && dateEcheanceLong != null) {
                        onAdd(
                            typeInvestissement,
                            description,
                            montantDouble,
                            dateEcheanceLong,
                            priorite
                        )
                    }
                },
                enabled = montant.toDoubleOrNull() != null && montant.toDoubleOrNull()!! > 0,
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
fun convertMillisToDate(millis: Long): String {
    if (millis == 0L) return "--/--/----"

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val instant = Instant.ofEpochMilli(millis)
    val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    return date.format(formatter)
}
fun dateToBigInt(dateString: String): Long? {
    return try {

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        val localDate = LocalDate.parse(dateString, formatter)

        localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    } catch (e: Exception) {
        null
    }
}


@Composable
fun UpdateMontantDialog(
    objectif: ObjectifInvestissement,
    onDismiss: () -> Unit,
    onUpdate: (Double) -> Unit
) {
    var ajout by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un montant", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Montant actuel: ${NumberFormat.getCurrencyInstance(Locale("fr", "MA")).format(objectif.montantActuel)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = ajout,
                    onValueChange = { ajout = it },
                    label = { Text("Montant à ajouter (MAD)") },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePrimary,
                        focusedLabelColor = PurplePrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ajoutDouble = ajout.toDoubleOrNull()
                    if (ajoutDouble != null && ajoutDouble > 0) {
                        onUpdate(objectif.montantActuel + ajoutDouble)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Text("Confirmer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}


fun safeGetTypeEnum(typeString: String): TypeInvestissement {
    return try {
        TypeInvestissement.valueOf(typeString.uppercase())
    } catch (e: Exception) {
        TypeInvestissement.AUTRE
    }
}

fun getTypeIcon(type: String): ImageVector {
    return when (type.uppercase()) {
        "EPARGNE" -> Icons.Default.Savings
        "RETRAITE" -> Icons.Default.AccountBalance
        "IMMOBILIER" -> Icons.Default.Home
        "ACTIONS" -> Icons.Default.TrendingUp
        "FONDS" -> Icons.Default.AccountBalance
        "CRYPTO" -> Icons.Default.CurrencyBitcoin
        "ENTREPRENEURIAT" -> Icons.Default.Work
        "VOYAGE" -> Icons.Default.Flight
        "VOITURE" -> Icons.Default.DirectionsCar
        else -> Icons.Default.Star
    }
}

fun getTypeColor(type: String): Color {
    return when (type.uppercase()) {
        "EPARGNE" -> Color(0xFF4CAF50)
        "RETRAITE" -> Color(0xFF2196F3)
        "IMMOBILIER" -> Color(0xFFFF9800)
        "ACTIONS" -> Color(0xFF4CAF50)
        "FONDS" -> Color(0xFF2196F3)
        "CRYPTO" -> Color(0xFFFFEB3B)
        "ENTREPRENEURIAT" -> Color(0xFF9C27B0)
        "VOYAGE" -> Color(0xFF00BCD4)
        "VOITURE" -> Color(0xFFE91E63)
        else -> Color(0xFF607D8B)
    }
}