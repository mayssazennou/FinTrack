package com.example.appmobile.ui

import androidx.compose.foundation.background
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
import com.example.appmobile.data.CategorieDepense
import com.example.appmobile.data.Priorite
import com.example.appmobile.viewmodel.DepenseViewModel
import com.example.appmobile.viewmodel.RevenuViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val RedPrimary = Color(0xFFF44336)
private val RedDark = Color(0xFFD32F2F)
private val RedLight = Color(0xFFFFEBEE)
private val OrangePrimary = Color(0xFFFF9800)
private val YellowPrimary = Color(0xFFFFC107)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepenseScreen(
    depenseViewModel: DepenseViewModel = viewModel(),
    revenuViewModel: RevenuViewModel = viewModel()
) {
    val depenseState by depenseViewModel.state.collectAsState()
    val revenuState by revenuViewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showSimulatorDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dépenses",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RedPrimary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showSimulatorDialog = true }) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = "Simulateur",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = RedPrimary,
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
                contentColor = RedPrimary,
                indicator = { tabPositions ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.BottomStart)
                            .offset(x = tabPositions[selectedTab].left)
                            .width(tabPositions[selectedTab].width)
                            .height(3.dp)
                            .background(RedPrimary)
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Vue d'ensemble",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Conseils",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Historique",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            when (selectedTab) {
                0 -> OverviewTabDepense(depenseState, revenuState, depenseViewModel)
                1 -> ConseilsTabDepense(depenseState, revenuState, depenseViewModel)
                2 -> HistoryTabDepense(depenseState)
            }
        }
    }

    if (showAddDialog) {
        AddDepenseDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { label, montant, categorie, priorite, description ->
                depenseViewModel.addDepense(label, montant, categorie, priorite, description)
                showAddDialog = false
            }
        )
    }

    if (showSimulatorDialog) {
        SimulateurDialog(
            onDismiss = { showSimulatorDialog = false },
            revenus = revenuState.totalRevenus,
            depenseViewModel = depenseViewModel
        )
    }

    depenseState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { depenseViewModel.loadDepenses() },
            title = { Text("Erreur") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { depenseViewModel.loadDepenses() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun OverviewTabDepense(
    state: com.example.appmobile.viewmodel.DepenseState,
    revenuState: com.example.appmobile.viewmodel.RevenuState,
    viewModel: DepenseViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Carte Total avec gradient rouge
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
                                colors = listOf(RedPrimary, RedDark)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Total des Dépenses",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            NumberFormat.getCurrencyInstance(Locale("fr", "MA"))
                                .format(state.totalDepenses),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Budget restant
                        val budgetRestant = revenuState.totalRevenus - state.totalDepenses
                        val pourcentageUtilise = if (revenuState.totalRevenus > 0)
                            (state.totalDepenses / revenuState.totalRevenus) * 100
                        else 0.0

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Budget Restant",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                                Text(
                                    NumberFormat.getCurrencyInstance(Locale("fr", "MA"))
                                        .format(budgetRestant),
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${pourcentageUtilise.toInt()}% du budget utilisé",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Répartition par Catégorie",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
        }

        val depensesByCategorie = viewModel.getDepensesByCategorie()
        items(depensesByCategorie.entries.toList()) { (categorie, montant) ->
            CategorieCard(categorie, montant, state.totalDepenses)
        }

        item {
            Text(
                "Répartition par Priorité",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
        }

        // Grouper par priorité
        val depensesByPriorite = state.depenses.groupBy { it.priorite }
        items(listOf(3, 2, 1)) { priorite ->
            val montant = depensesByPriorite[priorite]?.sumOf { it.montant } ?: 0.0
            if (montant > 0) {
                PrioriteCard(priorite, montant, state.totalDepenses)
            }
        }
    }
}

@Composable

fun ConseilsTabDepense(
    depenseState: com.example.appmobile.viewmodel.DepenseState,
    revenuState: com.example.appmobile.viewmodel.RevenuState,
    viewModel: DepenseViewModel
) {
    val conseils = remember(depenseState, revenuState) {
        generateConseils(depenseState, revenuState, viewModel)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = OrangePrimary.copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Conseils Personnalisés",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50)
                        )
                        Text(
                            "Basés sur votre situation financière",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        items(conseils) { conseil ->
            ConseilCard(conseil)
        }

        item {
            // Objectifs recommendés
            Text(
                "Objectifs Recommandés",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
        }

        item {
            ObjectifsCard(depenseState, revenuState)
        }
    }
}

data class Conseil(
    val titre: String,
    val description: String,
    val type: ConseilType,
    val priorite: Int
)

enum class ConseilType {
    URGENT, ATTENTION, AMELIORATION, FELICITATION
}

fun generateConseils(
    depenseState: com.example.appmobile.viewmodel.DepenseState,
    revenuState: com.example.appmobile.viewmodel.RevenuState,
    viewModel: DepenseViewModel
): List<Conseil> {
    val conseils = mutableListOf<Conseil>()

    val totalDepenses = depenseState.totalDepenses
    val totalRevenus = revenuState.totalRevenus
    val budgetRestant = totalRevenus - totalDepenses
    val pourcentageUtilise = if (totalRevenus > 0) (totalDepenses / totalRevenus) * 100 else 0.0

    // Conseil 1 : Niveau de dépenses
    when {
        pourcentageUtilise > 95 -> {
            conseils.add(Conseil(
                titre = " Budget Presque Épuisé",
                description = "Vous avez utilisé ${pourcentageUtilise.toInt()}% de votre budget. Il est urgent de réduire vos dépenses pour éviter un découvert.",
                type = ConseilType.URGENT,
                priorite = 1
            ))
        }
        pourcentageUtilise > 80 -> {
            conseils.add(Conseil(
                titre = " Attention au Budget",
                description = "Vous avez dépensé ${pourcentageUtilise.toInt()}% de vos revenus. Essayez de limiter les dépenses non essentielles ce mois-ci.",
                type = ConseilType.ATTENTION,
                priorite = 2
            ))
        }
        pourcentageUtilise < 50 -> {
            conseils.add(Conseil(
                titre = " Excellente Gestion !",
                description = "Vous gérez très bien votre budget avec seulement ${pourcentageUtilise.toInt()}% utilisé. Pensez à épargner le surplus !",
                type = ConseilType.FELICITATION,
                priorite = 4
            ))
        }
    }

    // Conseil 2 : Analyse des catégories
    val depensesByCategorie = viewModel.getDepensesByCategorie()
    val categorieMax = depensesByCategorie.maxByOrNull { it.value }

    if (categorieMax != null && totalDepenses > 0) {
        val pourcentageCategorie = (categorieMax.value / totalDepenses) * 100
        if (pourcentageCategorie > 50) {
            conseils.add(Conseil(
                titre = " ${categorieMax.key} Domine",
                description = "Les dépenses en ${categorieMax.key} représentent ${pourcentageCategorie.toInt()}% de votre total. Envisagez de diversifier ou réduire cette catégorie.",
                type = ConseilType.AMELIORATION,
                priorite = 3
            ))
        }
    }

    // Conseil 3 : Dépenses prioritaires
    val depensesHautePriorite = depenseState.depenses.filter { it.priorite == 3 }
    val totalHautePriorite = depensesHautePriorite.sumOf { it.montant }

    if (totalHautePriorite > totalDepenses * 0.7) {
        conseils.add(Conseil(
            titre = " Trop de Dépenses Urgentes",
            description = "${depensesHautePriorite.size} dépenses en haute priorité représentent ${((totalHautePriorite/totalDepenses)*100).toInt()}% du total. Planifiez mieux pour éviter les urgences.",
            type = ConseilType.ATTENTION,
            priorite = 2
        ))
    }

    // Conseil 4 : Épargne recommandée
    val epargneSuggeree = totalRevenus * 0.20
    val epargnActuelle = depensesByCategorie["Épargne"] ?: 0.0

    if (epargnActuelle < epargneSuggeree) {
        conseils.add(Conseil(
            titre = " Augmentez Votre Épargne",
            description = "Vous épargnez ${NumberFormat.getCurrencyInstance(Locale("fr", "MA")).format(epargnActuelle)}. Visez 20% de vos revenus soit ${NumberFormat.getCurrencyInstance(Locale("fr", "MA")).format(epargneSuggeree)}.",
            type = ConseilType.AMELIORATION,
            priorite = 3
        ))
    } else {
        conseils.add(Conseil(
            titre = " Épargne Exemplaire",
            description = "Vous épargnez ${((epargnActuelle/totalRevenus)*100).toInt()}% de vos revenus. Continuez ainsi pour atteindre vos objectifs financiers !",
            type = ConseilType.FELICITATION,
            priorite = 4
        ))
    }

    // Conseil 5 : Prévisions futures
    if (depenseState.depenses.isNotEmpty()) {
        val moyenneDepensesParJour = totalDepenses / 30
        val joursRestants = 30 - (System.currentTimeMillis() / (1000 * 60 * 60 * 24) % 30).toInt()
        val depensesPrevisionnelles = moyenneDepensesParJour * joursRestants

        if (depensesPrevisionnelles > budgetRestant) {
            conseils.add(Conseil(
                titre = " Projection Budget",
                description = "À ce rythme, vous risquez de dépasser votre budget de ${NumberFormat.getCurrencyInstance(Locale("fr", "MA")).format(depensesPrevisionnelles - budgetRestant)}. Réduisez de ${((depensesPrevisionnelles - budgetRestant) / joursRestants).toInt()} MAD/jour.",
                type = ConseilType.ATTENTION,
                priorite = 2
            ))
        }
    }

    // Conseil 6 : Optimisation des achats
    val achats = depensesByCategorie["Achats"] ?: 0.0
    if (achats > totalDepenses * 0.4) {
        conseils.add(Conseil(
            titre = " Optimisez vos Achats",
            description = "Les achats représentent ${((achats/totalDepenses)*100).toInt()}% de vos dépenses. Comparez les prix, utilisez des promotions et achetez en gros quand possible.",
            type = ConseilType.AMELIORATION,
            priorite = 3
        ))
    }

    return conseils.sortedBy { it.priorite }
}

@Composable
fun ConseilCard(conseil: Conseil) {
    val (backgroundColor, iconColor, icon) = when (conseil.type) {
        ConseilType.URGENT -> Triple(
            Color(0xFFFFEBEE),
            RedPrimary,
            Icons.Default.Warning
        )
        ConseilType.ATTENTION -> Triple(
            Color(0xFFFFF3E0),
            OrangePrimary,
            Icons.Default.Info
        )
        ConseilType.AMELIORATION -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF2196F3),
            Icons.Default.TipsAndUpdates
        )
        ConseilType.FELICITATION -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF4CAF50),
            Icons.Default.CheckCircle
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(backgroundColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    conseil.titre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF2C3E50)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    conseil.description,
                    fontSize = 14.sp,
                    color = Color(0xFF546E7A),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun ObjectifsCard(
    depenseState: com.example.appmobile.viewmodel.DepenseState,
    revenuState: com.example.appmobile.viewmodel.RevenuState
) {
    val totalRevenus = revenuState.totalRevenus
    val totalDepenses = depenseState.totalDepenses

    val objectifs = listOf(
        Objectif(
            titre = "Épargner 20% du Revenu",
            montantCible = totalRevenus * 0.20,
            montantActuel = 0.0, // À calculer depuis les données
            icon = Icons.Default.Savings
        ),
        Objectif(
            titre = "Réduire les Achats",
            montantCible = totalDepenses * 0.25,
            montantActuel = totalDepenses * 0.40,
            icon = Icons.Default.ShoppingCart
        ),
        Objectif(
            titre = "Fonds d'Urgence",
            montantCible = totalRevenus * 3,
            montantActuel = totalRevenus * 0.5,
            icon = Icons.Default.Emergency
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            objectifs.forEach { objectif ->
                ObjectifItem(objectif)
                if (objectif != objectifs.last()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

data class Objectif(
    val titre: String,
    val montantCible: Double,
    val montantActuel: Double,
    val icon: ImageVector
)

@Composable
fun ObjectifItem(objectif: Objectif) {
    val progression = if (objectif.montantCible > 0)
        (objectif.montantActuel / objectif.montantCible) * 100
    else 0.0

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    objectif.icon,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    objectif.titre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF2C3E50)
                )
            }
            Text(
                "${progression.toInt()}%",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF2196F3)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = (progression / 100).toFloat().coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF2196F3),
            trackColor = Color(0xFFE0E0E0)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                NumberFormat.getCurrencyInstance(Locale("fr", "MA")).format(objectif.montantActuel),
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                NumberFormat.getCurrencyInstance(Locale("fr", "MA")).format(objectif.montantCible),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun HistoryTabDepense(state: com.example.appmobile.viewmodel.DepenseState) {
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = RedPrimary)
        }
    } else if (state.depenses.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Aucune dépense enregistrée",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.depenses.sortedByDescending { it.date }) { depense ->
                DepenseItemCard(depense)
            }
        }
    }
}

@Composable
fun CategorieCard(categorie: String, montant: Double, total: Double) {
    val percentage = if (total > 0) (montant / total) * 100 else 0.0
    val icon = getCategorieIcon(categorie)
    val color = getCategorieColor(categorie)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    categorie,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF2C3E50)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = (percentage / 100).toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${percentage.toInt()}%",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                NumberFormat.getCurrencyInstance(Locale("fr", "MA")).format(montant),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = color
            )
        }
    }
}

@Composable
fun PrioriteCard(priorite: Int, montant: Double, total: Double) {
    val percentage = if (total > 0) (montant / total) * 100 else 0.0
    val color = getPrioriteColor(priorite)
    val text = getPrioriteText(priorite)
    val icon = getPrioriteIcon(priorite)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Priorité $text",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF2C3E50)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = (percentage / 100).toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${percentage.toInt()}%",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                NumberFormat.getCurrencyInstance(Locale("fr", "MA")).format(montant),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = color
            )
        }
    }
}

@Composable
fun DepenseItemCard(depense: com.example.appmobile.data.Depense) {
    val icon = getCategorieIcon(depense.categorie)
    val color = getCategorieColor(depense.categorie)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (depense.priorite) {
                3 -> RedLight
                2 -> Color(0xFFFFF3E0)
                else -> Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        depense.label,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF2C3E50)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = getPrioriteColor(depense.priorite)
                    ) {
                        Text(
                            getPrioriteText(depense.priorite),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Text(
                    depense.categorie,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                if (depense.description != null) {
                    Text(
                        depense.description,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    SimpleDateFormat("dd MMM yyyy", Locale.FRENCH).format(Date(depense.date)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                "-${NumberFormat.getCurrencyInstance(Locale("fr", "MA")).format(depense.montant)}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = RedPrimary
            )
        }
    }
}

@Composable

fun SimulateurDialog(
    onDismiss: () -> Unit,
    revenus: Double,
    depenseViewModel: DepenseViewModel
) {
    var montant by remember { mutableStateOf("") }
    var simulation by remember { mutableStateOf<Map<String, Any>?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Calculate,
                    contentDescription = null,
                    tint = RedPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Simulateur d'Impact",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = montant,
                    onValueChange = { montant = it },
                    label = { Text("Montant de l'achat (MAD)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedPrimary,
                        focusedLabelColor = RedPrimary
                    )
                )

                Button(
                    onClick = {
                        val montantDouble = montant.toDoubleOrNull()
                        if (montantDouble != null && montantDouble > 0) {
                            simulation = depenseViewModel.simulateImpact(montantDouble, revenus)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = montant.toDoubleOrNull() != null && montant.toDoubleOrNull()!! > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Calculer l'Impact")
                }

                simulation?.let { sim ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = RedLight
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = RedPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Résultat de la Simulation",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RedDark
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            SimulationRow("Nouveau Total Dépenses", sim["nouveauTotal"] as Double)
                            SimulationRow("Budget Restant", sim["budgetRestant"] as Double)

                            Spacer(modifier = Modifier.height(12.dp))

                            val pourcentage = sim["pourcentageUtilise"] as Double
                            LinearProgressIndicator(
                                progress = (pourcentage / 100).toFloat(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = when {
                                    pourcentage > 90 -> RedPrimary
                                    pourcentage > 70 -> OrangePrimary
                                    else -> Color(0xFF4CAF50)
                                },
                                trackColor = Color(0xFFE0E0E0)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "${String.format("%.1f", pourcentage)}% du budget utilisé",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when {
                                    pourcentage > 90 -> RedPrimary.copy(alpha = 0.1f)
                                    pourcentage > 70 -> OrangePrimary.copy(alpha = 0.1f)
                                    else -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        when {
                                            pourcentage > 90 -> Icons.Default.Warning
                                            pourcentage > 70 -> Icons.Default.Info
                                            else -> Icons.Default.CheckCircle
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            pourcentage > 90 -> RedPrimary
                                            pourcentage > 70 -> OrangePrimary
                                            else -> Color(0xFF4CAF50)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        sim["recommandation"] as String,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF2C3E50)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Fermer", color = RedPrimary, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun SimulationRow(label: String, value: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            NumberFormat.getCurrencyInstance(Locale("fr", "MA")).format(value),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDepenseDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, String, Int, String?) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var montant by remember { mutableStateOf("") }
    var selectedCategorie by remember { mutableStateOf(CategorieDepense.ACHATS.displayName) }
    var selectedPriorite by remember { mutableStateOf(Priorite.MOYENNE) }
    var description by remember { mutableStateOf("") }
    var expandedCategorie by remember { mutableStateOf(false) }
    var expandedPriorite by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Ajouter une Dépense",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Titre") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedPrimary,
                        focusedLabelColor = RedPrimary
                    )
                )

                OutlinedTextField(
                    value = montant,
                    onValueChange = { montant = it },
                    label = { Text("Montant (MAD)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedPrimary,
                        focusedLabelColor = RedPrimary
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCategorie,
                    onExpandedChange = { expandedCategorie = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategorie,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Catégorie") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategorie) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedPrimary,
                            focusedLabelColor = RedPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategorie,
                        onDismissRequest = { expandedCategorie = false }
                    ) {
                        CategorieDepense.values().forEach { categorie ->
                            DropdownMenuItem(
                                text = { Text(categorie.displayName) },
                                onClick = {
                                    selectedCategorie = categorie.displayName
                                    expandedCategorie = false
                                },
                                leadingIcon = {
                                    Icon(
                                        getCategorieIcon(categorie.displayName),
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedPriorite,
                    onExpandedChange = { expandedPriorite = it }
                ) {
                    OutlinedTextField(
                        // On force l'affichage via la variable d'état
                        value = selectedPriorite.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priorité") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriorite) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedPrimary,
                            focusedLabelColor = RedPrimary
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedPriorite,
                        onDismissRequest = { expandedPriorite = false }
                    ) {
                        // OPTION 1 : "Faible"
                        DropdownMenuItem(
                            text = { Text("Faible") },
                            onClick = {
                                selectedPriorite = Priorite.FAIBLE
                                expandedPriorite = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                            }
                        )

                        // OPTION 2 : "Moyenne"
                        DropdownMenuItem(
                            text = { Text("Moyenne") },
                            onClick = {
                                selectedPriorite = Priorite.MOYENNE
                                expandedPriorite = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFF9800))
                            }
                        )

                        // OPTION 3 : "Haute"
                        DropdownMenuItem(
                            text = { Text("Haute") },
                            onClick = {
                                selectedPriorite = Priorite.HAUTE
                                expandedPriorite = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF44336))
                            }
                        )
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
                        focusedBorderColor = RedPrimary,
                        focusedLabelColor = RedPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val montantDouble = montant.toDoubleOrNull()
                    if (label.isNotBlank() && montantDouble != null && montantDouble > 0) {
                        onAdd(
                            label,
                            montantDouble,
                            selectedCategorie,
                            selectedPriorite.value,
                            description.ifBlank { null }
                        )
                    }
                },
                enabled = label.isNotBlank() && montant.toDoubleOrNull() != null && montant.toDoubleOrNull()!! > 0,
                colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Annuler", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

fun getCategorieIcon(categorie: String): ImageVector {
    return when (categorie) {
        "Achats" -> Icons.Default.ShoppingCart
        "Charges fixes" -> Icons.Default.Home
        "Dettes/Crédits" -> Icons.Default.CreditCard
        "Épargne" -> Icons.Default.AccountBalance
        else -> Icons.Default.Payment
    }
}

fun getCategorieColor(categorie: String): Color {
    return when (categorie) {
        "Achats" -> Color(0xFFF44336)
        "Charges fixes" -> Color(0xFFFF9800)
        "Dettes/Crédits" -> Color(0xFF9C27B0)
        "Épargne" -> Color(0xFF4CAF50)
        else -> Color(0xFF607D8B)
    }
}

fun getPrioriteColor(priorite: Int): Color {
    return when (priorite) {
        3 -> Color(0xFFF44336) // Rouge - Haute
        2 -> Color(0xFFFF9800) // Orange - Moyenne
        1 -> Color(0xFF4CAF50) // Vert - Faible
        else -> Color(0xFFFF9800) // Orange - Moyenne
    }
}

fun getPrioriteText(priorite: Int): String {
    return when (priorite) {
        3 -> "Haute"
        2 -> "Moyenne"
        1 -> "Faible"
        else-> "Moyenne"
    }
}

fun getPrioriteIcon(priorite: Int): ImageVector {
    return when (priorite) {
        3 -> Icons.Default.Warning
        2 -> Icons.Default.Info
        1 -> Icons.Default.CheckCircle
        else -> Icons.Default.Info
    }
}