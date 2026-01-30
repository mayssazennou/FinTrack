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
import androidx.compose.foundation.lazy.LazyRow

import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material.icons.filled.Check
import java.util.Calendar
import java.util.Date
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appmobile.data.SourceRevenu
import com.example.appmobile.viewmodel.RevenuViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val GreenPrimary = Color(0xFF4CAF50)
private val GreenDark = Color(0xFF388E3C)
private val GreenLight = Color(0xFFE8F5E9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenuScreen(
    viewModel: RevenuViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Revenus",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GreenPrimary,
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
                contentColor = GreenPrimary,
                indicator = { tabPositions ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.BottomStart)
                            .offset(x = tabPositions[selectedTab].left)
                            .width(tabPositions[selectedTab].width)
                            .height(3.dp)
                            .background(GreenPrimary)
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
                            "Graphiques",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Historique",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            when (selectedTab) {
                0 -> OverviewTab(state, viewModel)
                1 -> GraphicsTab(state, viewModel)
                2 -> HistoryTab(state)
            }
        }
    }

    if (showAddDialog) {
        AddRevenuDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { source, montant, description ->
                viewModel.addRevenu(source, montant, description)
                showAddDialog = false
            }
        )
    }

    state.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.loadRevenus() },
            title = { Text("Erreur") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.loadRevenus() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun OverviewTab(state: com.example.appmobile.viewmodel.RevenuState, viewModel: RevenuViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Carte Total
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
                                colors = listOf(GreenPrimary, GreenDark)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Total des Revenus",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            NumberFormat.getCurrencyInstance(Locale("fr", "MA"))
                                .format(state.totalRevenus),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Text(
                "Répartition par Source",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
        }

        val revenusBySource = viewModel.getRevenusBySource()
        items(revenusBySource.entries.toList()) { (source, montant) ->
            SourceCard(source, montant, state.totalRevenus)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphicsTab(state: com.example.appmobile.viewmodel.RevenuState, viewModel: RevenuViewModel) {
    // 1. État pour retenir le filtre choisi (par défaut : Ce mois)
    var selectedFilter by remember { mutableStateOf(TimeFilter.THIS_MONTH) }

    // 2. On filtre la liste à chaque fois que le filtre ou les données changent
    val filteredRevenus = remember(state.revenus, selectedFilter) {
        filterRevenusByPeriod(state.revenus, selectedFilter)
    }

    // 3. On recalcule les totaux par source uniquement sur les données filtrées
    val filteredRevenusBySource = remember(filteredRevenus) {
        filteredRevenus.groupBy { it.source }
            .mapValues { entry -> entry.value.sumOf { it.montant } }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- SECTION FILTRES ---
        item {
            Text(
                "Période",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TimeFilter.values()) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.label) },
                        leadingIcon = if (selectedFilter == filter) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenLight,
                            selectedLabelColor = GreenDark,
                            selectedLeadingIconColor = GreenDark
                        )
                    )
                }
            }
        }

        // --- SECTION GRAPHIQUE LIGNE ---
        item {
            Text(
                "Évolution (${selectedFilter.label})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (filteredRevenus.isEmpty()) {
                        Text("Aucune donnée", color = Color.Gray)
                    } else {
                        // On passe le filtre au graphique pour adapter l'axe X
                        RevenuLineChart(filteredRevenus, selectedFilter)
                    }
                }
            }
        }

        item {
            Text(
                "Répartition par Source",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (filteredRevenusBySource.values.sum() == 0.0) {
                        Text("Aucune donnée", color = Color.Gray)
                    } else {
                        RevenuPieChart(filteredRevenusBySource)
                    }
                }
            }
        }
    }
}
@Composable
fun HistoryTab(state: com.example.appmobile.viewmodel.RevenuState) {
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = GreenPrimary)
        }
    } else if (state.revenus.isEmpty()) {
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
                    "Aucun revenu enregistré",
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
            items(state.revenus.sortedByDescending { it.date }) { revenu ->
                RevenuItemCard(revenu)
            }
        }
    }
}

@Composable
fun SourceCard(source: String, montant: Double, total: Double) {
    val percentage = if (total > 0) (montant / total) * 100 else 0.0
    val icon = getSourceIcon(source)
    val color = getSourceColor(source)

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
            // Icône dans un cercle coloré
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
                    source,
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
fun RevenuItemCard(revenu: com.example.appmobile.data.Revenu) {
    val icon = getSourceIcon(revenu.source)
    val color = getSourceColor(revenu.source)

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
                Text(
                    revenu.source,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF2C3E50)
                )
                if (revenu.description != null) {
                    Text(
                        revenu.description,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    SimpleDateFormat("dd MMM yyyy", Locale.FRENCH).format(Date(revenu.date)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                "+${NumberFormat.getCurrencyInstance(Locale("fr", "MA")).format(revenu.montant)}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = GreenPrimary
            )
        }
    }
}

@Composable
fun RevenuLineChart(
    revenus: List<com.example.appmobile.data.Revenu>,
    timeFilter: TimeFilter
) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)

                setExtraOffsets(10f, 0f, 10f, 10f)

                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f // Important pour afficher chaque jour/heure
                xAxis.textColor = android.graphics.Color.DKGRAY

                axisLeft.setDrawGridLines(true)
                axisLeft.gridColor = android.graphics.Color.parseColor("#E0E0E0")
                axisLeft.axisMinimum = 0f

                axisRight.isEnabled = false
                legend.isEnabled = false
            }
        },
        update = { chart ->
            if (revenus.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            val entries = ArrayList<Entry>()
            val calendar = Calendar.getInstance()

            // --- 1. CALCUL DES POINTS (X, Y) ---
            if (timeFilter == TimeFilter.TODAY) {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis

                revenus.sortedBy { it.date }.forEach { revenu ->
                    // X = Minutes écoulées depuis minuit
                    val minutesFromMidnight = (revenu.date - startOfDay) / (1000f * 60f)
                    entries.add(Entry(minutesFromMidnight, revenu.montant.toFloat()))
                }

            } else {
                val groupedByDay = revenus.groupBy { revenu ->
                    calendar.timeInMillis = revenu.date
                    calendar.get(Calendar.DAY_OF_MONTH)
                }

                groupedByDay.forEach { (dayOfMonth, listRevenus) ->
                    val totalJour = listRevenus.sumOf { it.montant }.toFloat()
                    entries.add(Entry(dayOfMonth.toFloat(), totalJour))
                }

                entries.sortBy { it.x }
            }

            // --- 2. FORMATAGE DE L'AXE X ---
            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (timeFilter == TimeFilter.TODAY) {
                        val totalMinutes = value.toInt()
                        val hours = totalMinutes / 60
                        val minutes = totalMinutes % 60
                        String.format("%02d:%02d", hours, minutes)
                    } else {
                        String.format("%02d", value.toInt())
                    }
                }
            }

            // --- 3. APPARENCE DE LA LIGNE ---
            val dataSet = LineDataSet(entries, "Revenus").apply {
                color = android.graphics.Color.parseColor("#4CAF50") // Vert
                setCircleColor(android.graphics.Color.parseColor("#388E3C")) // Vert foncé
                lineWidth = 3f
                circleRadius = 5f
                setDrawCircleHole(true)
                circleHoleRadius = 2.5f
                valueTextSize = 11f
                valueTextColor = android.graphics.Color.BLACK

                // Remplissage sous la courbe
                setDrawFilled(true)
                fillColor = android.graphics.Color.parseColor("#4CAF50")
                fillAlpha = 40

                mode = LineDataSet.Mode.CUBIC_BEZIER // Ligne courbée douce
            }

            chart.data = LineData(dataSet)
            chart.notifyDataSetChanged()
            chart.invalidate()
            chart.animateX(800)
        }
    )
}

@Composable
fun RevenuPieChart(revenusBySource: Map<String, Double>) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false

                // --- CONFIGURATION DU CERCLE ---
                isDrawHoleEnabled = true
                setHoleColor(android.graphics.Color.WHITE)
                holeRadius = 40f
                transparentCircleRadius = 45f
                setUsePercentValues(true)

                // --- CONFIGURATION DE LA LÉGENDE ---
                legend.isEnabled = true
                legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                legend.setDrawInside(false)
                legend.textSize = 12f
                legend.yEntrySpace = 5f

                // Animation au démarrage
                animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
            }
        },
        update = { chart ->
            if (revenusBySource.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            val entries = revenusBySource.map { (source, montant) ->
                PieEntry(montant.toFloat(), source)
            }

            val colors = ArrayList<Int>()
            for (c in com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS) colors.add(c)
            for (c in com.github.mikephil.charting.utils.ColorTemplate.JOYFUL_COLORS) colors.add(c)

            val dataSet = PieDataSet(entries, "").apply {
                setColors(colors)
                sliceSpace = 3f
                selectionShift = 5f

                valueTextSize = 14f
                valueTextColor = android.graphics.Color.BLACK
                valueTypeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            val data = PieData(dataSet).apply {
                setValueFormatter(object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return String.format("%.1f %%", value)
                    }
                })
            }

            chart.data = data

            chart.setDrawEntryLabels(false)

            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}

fun getSourceIcon(source: String): ImageVector {
    return when (source) {
        "Salaire" -> Icons.Default.Work
        "Freelance" -> Icons.Default.Computer
        "Investissements" -> Icons.Default.TrendingUp
        "Autres" -> Icons.Default.Star
        else -> Icons.Default.AttachMoney
    }
}

fun getSourceColor(source: String): Color {
    return when (source) {
        "Salaire" -> Color(0xFF4CAF50)
        "Freelance" -> Color(0xFF2196F3)
        "Investissements" -> Color(0xFFFF9800)
        "Autres" -> Color(0xFF9C27B0)
        else -> Color(0xFF607D8B)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRevenuDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, String?) -> Unit
) {
    var selectedSource by remember { mutableStateOf(SourceRevenu.SALAIRE.displayName) }
    var montant by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expandedSource by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Ajouter un Revenu",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedSource,
                    onExpandedChange = { expandedSource = it }
                ) {
                    OutlinedTextField(
                        value = selectedSource,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Source") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSource) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            focusedLabelColor = GreenPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSource,
                        onDismissRequest = { expandedSource = false }
                    ) {
                        SourceRevenu.values().forEach { source ->
                            DropdownMenuItem(
                                text = { Text(source.displayName) },
                                onClick = {
                                    selectedSource = source.displayName
                                    expandedSource = false
                                },
                                leadingIcon = {
                                    Icon(
                                        getSourceIcon(source.displayName),
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }

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
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val montantDouble = montant.toDoubleOrNull()
                    if (montantDouble != null && montantDouble > 0) {
                        onAdd(
                            selectedSource,
                            montantDouble,
                            description.ifBlank { null }
                        )
                    }
                },
                enabled = montant.toDoubleOrNull() != null && montant.toDoubleOrNull()!! > 0,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
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

// 1. Enum
enum class TimeFilter(val label: String) {
    TODAY("Aujourd'hui"),
    THIS_MONTH("Ce mois")
}

// 2. Fonction de filtrage mise à jour
fun filterRevenusByPeriod(revenus: List<com.example.appmobile.data.Revenu>, filter: TimeFilter): List<com.example.appmobile.data.Revenu> {
    val calendar = Calendar.getInstance()

    fun resetTime() {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    val startTime = when (filter) {
        TimeFilter.TODAY -> {
            resetTime()
            calendar.timeInMillis
        }
        TimeFilter.THIS_MONTH -> {
            resetTime()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.timeInMillis
        }
    }

    return revenus.filter { it.date >= startTime }
}