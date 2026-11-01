package com.iie.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModelProvider
import com.iie.android.domain.model.Asset
import com.iie.android.domain.model.AssetObservationProvenance
import com.iie.android.domain.model.AssetResearchDossier
import com.iie.android.presentation.AssetExplorerViewModel
import com.iie.android.presentation.CoinMarketCapSettingsScreen
import com.iie.android.presentation.MarketQuoteUiState
import com.iie.android.presentation.MarketHistorySyncUiState
import com.iie.android.presentation.RoutescanTokenSupplyUiState
import com.iie.android.data.market.CoinPaprikaHistoricalPoint
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val explorerViewModel = ViewModelProvider(
            this, AssetExplorerViewModel.Factory(application)
        )[AssetExplorerViewModel::class.java]

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF63E6B2),
                    onPrimary = Color(0xFF06251B),
                    secondary = Color(0xFF8DA9FF),
                    background = Color(0xFF080D12),
                    surface = Color(0xFF111922),
                    surfaceVariant = Color(0xFF1A2631),
                    onSurface = Color(0xFFE8EEF4),
                    onSurfaceVariant = Color(0xFF9EACB9),
                    outline = Color(0xFF30404D),
                    error = Color(0xFFFF7777)
                )
            ) { AssetExplorerScreen(explorerViewModel) }
        }
    }
}

@Composable
private fun AssetExplorerScreen(viewModel: AssetExplorerViewModel) {
    val query by viewModel.searchQuery.collectAsState()
    val assets by viewModel.assets.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val catalogStatus by viewModel.catalogStatus.collectAsState()
    val marketQuotes by viewModel.marketQuotes.collectAsState()
    val marketHistorySync by viewModel.marketHistorySync.collectAsState()
    val dailyHistoryByAsset by viewModel.dailyMarketHistory.collectAsState()
    var selectedAsset by remember { mutableStateOf<Asset?>(null) }
    var destination by remember { mutableStateOf("Resumen") }

    val visibleAssets = when (destination) {
        "Seguimiento" -> assets.filter { it.assetId in watchlist }
        else -> assets
    }

    LaunchedEffect(destination) {
        if (destination != "Mercados" && query.isNotEmpty()) {
            viewModel.updateSearchQuery("")
        }
    }

    LaunchedEffect(destination, assets) {
        if (destination == "Mercados") {
            assets.forEach { asset ->
                viewModel.loadLatestMarketQuote(asset.assetId)
                viewModel.loadDailyMarketHistory(asset.assetId)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            containerColor = Color(0xFF080D12),
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF101820)) {
                    listOf("Resumen", "Mercados", "Seguimiento", "Ajustes").forEach { item ->
                        NavigationBarItem(
                            selected = destination == item,
                            onClick = { destination = item },
                            icon = {
                                Text(
                                    when (item) {
                                        "Resumen" -> "⌂"
                                        "Mercados" -> "◈"
                                        "Seguimiento" -> "☆"
                                        else -> "⚙"
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            label = { Text(item) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "IIE  /  INTELLIGENCE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            when (destination) {
                                "Resumen" -> "Centro de inversión"
                                "Mercados" -> "Universo de activos"
                                "Seguimiento" -> "Tu seguimiento"
                                else -> "Configuración"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            "LOCAL",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))

                when (destination) {
                    "Ajustes" -> CoinMarketCapSettingsScreen()
                    "Resumen" -> {
                        DashboardHero(
                            assetCount = assets.size,
                            watchedCount = watchlist.size,
                            catalogStatus = catalogStatus,
                            onExplore = { destination = "Mercados" }
                        )
                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(
                                label = "Activos",
                                value = assets.size.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                label = "En seguimiento",
                                value = watchlist.size.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Explorar universo", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            TextButton(onClick = { destination = "Mercados" }) {
                                Text("Ver todos")
                            }
                        }
                        if (assets.isEmpty()) {
                            EmptyState("El catálogo todavía no contiene activos.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(assets.take(6), key = { it.assetId }) { asset ->
                                    AssetRow(
                                        asset = asset,
                                        isWatched = asset.assetId in watchlist,
                                        onClick = { selectedAsset = asset },
                                        onToggleWatchlist = { viewModel.toggleWatchlist(asset.assetId) }
                                    )
                                }
                            }
                        }
                    }
                    "Mercados", "Seguimiento" -> {
                        if (destination == "Mercados") {
                            OutlinedTextField(
                                value = query,
                                onValueChange = viewModel::updateSearchQuery,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Buscar por nombre o símbolo") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                        } else {
                            Text(
                                "Activos guardados para consultar más tarde.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        Text(
                            "${visibleAssets.size} activos",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        if (visibleAssets.isEmpty()) {
                            EmptyState(
                                if (destination == "Seguimiento") {
                                    "Todavía no sigues ningún activo. Añádelo desde Mercados."
                                } else {
                                    "No hay activos que coincidan con la búsqueda."
                                }
                            )
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(visibleAssets, key = { it.assetId }) { asset ->
                                    if (destination == "Mercados") {
                                        MarketAssetRow(
                                            asset = asset,
                                            quote = marketQuotes[asset.assetId],
                                            history = dailyHistoryByAsset[asset.assetId].orEmpty(),
                                            historySync = marketHistorySync[asset.assetId],
                                            isWatched = asset.assetId in watchlist,
                                            onClick = { selectedAsset = asset },
                                            onRefresh = { viewModel.refreshMarketQuote(asset.assetId, asset.symbol) },
                                            onDownloadHistory = { viewModel.downloadDailyMarketHistory(asset.assetId, asset.symbol) },
                                            onToggleWatchlist = { viewModel.toggleWatchlist(asset.assetId) }
                                        )
                                    } else {
                                        AssetRow(
                                            asset = asset,
                                            isWatched = asset.assetId in watchlist,
                                            onClick = { selectedAsset = asset },
                                            onToggleWatchlist = { viewModel.toggleWatchlist(asset.assetId) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedAsset?.let { asset ->
        AssetDetailDialog(
            asset = asset,
            isWatched = asset.assetId in watchlist,
            viewModel = viewModel,
            onToggleWatchlist = { viewModel.toggleWatchlist(asset.assetId) },
            onDismiss = { selectedAsset = null }
        )
    }
}

@Composable
private fun MarketAssetRow(
    asset: Asset,
    quote: MarketQuoteUiState?,
    history: List<CoinPaprikaHistoricalPoint>,
    historySync: MarketHistorySyncUiState?,
    isWatched: Boolean,
    onClick: () -> Unit,
    onRefresh: () -> Unit,
    onDownloadHistory: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    val latestTwo = history.sortedByDescending { it.timestampEpochMs }.take(2)
    val dailyChange = if (latestTwo.size == 2 && latestTwo[1].priceUsd > 0.0) {
        ((latestTwo[0].priceUsd / latestTwo[1].priceUsd) - 1.0) * 100.0
    } else null
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111B25))
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f).clickable(onClick = onClick)) {
                    Text(asset.name, fontWeight = FontWeight.SemiBold)
                    Text(asset.symbol, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        quote?.priceUsd?.let {
                            "$" + String.format(java.util.Locale.US, "%.8f", it)
                        } ?: "Sin cotización",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        quote?.freshnessStatus ?: "SIN DATOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (history.size >= 2) {
                MarketSparkline(history)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    dailyChange?.let {
                        "Variación entre puntos diarios: " +
                            String.format(java.util.Locale.US, "%+.2f%%", it)
                    } ?: "Variación diaria: histórico insuficiente",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (dailyChange == null) MaterialTheme.colorScheme.onSurfaceVariant
                    else if (dailyChange >= 0.0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
                TextButton(onClick = onToggleWatchlist) {
                    Text(if (isWatched) "Siguiendo" else "+ Seguir")
                }
            }
            if (quote?.attribution != null) {
                Text(
                    "Fuente: ${quote.attribution} · Frescura: ${quote.freshnessReason}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (quote?.error != null) {
                Text(
                    "Error: ${quote.error}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Text(
                "Histórico diario: " + history.size + " puntos guardados",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            historySync?.message?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
            historySync?.error?.let {
                Text(it, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error)
            }
            TextButton(onClick = onDownloadHistory, enabled = historySync?.isLoading != true) {
                Text(if (historySync?.isLoading == true) "Descargando histórico…" else "Descargar histórico diario")
            }
            TextButton(onClick = onRefresh, enabled = quote?.isLoading != true) {
                Text(if (quote?.isLoading == true) "Actualizando…" else "Actualizar cotización")
            }
        }
    }
}

@Composable
private fun DashboardHero(
    assetCount: Int,
    watchedCount: Int,
    catalogStatus: String,
    onExplore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14252B))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "INVESTMENT INTELLIGENCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8FA99F),
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = Color(0xFF203D35),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        "PERSONAL TERMINAL",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = Color(0xFF8CE2BC),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                "Investiga con\ncriterio. Decide con datos.",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFF1F7F4),
                fontWeight = FontWeight.Bold,
                lineHeight = MaterialTheme.typography.headlineMedium.lineHeight
            )
            Text(
                "Tu espacio de análisis de mercado, fundamentos y tokenómica.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB4C6C0)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeroStat(modifier = Modifier.weight(1f), value = assetCount.toString(), label = "ACTIVOS")
                HeroStat(modifier = Modifier.weight(1f), value = watchedCount.toString(), label = "SEGUIMIENTO")
            }
            TextButton(onClick = onExplore) {
                Text("EXPLORAR MERCADOS  →", color = Color(0xFF75E0B2), fontWeight = FontWeight.Bold)
            }
            Text(
                catalogStatus,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF91A69F)
            )
        }
    }
}

@Composable
private fun HeroStat(modifier: Modifier, value: String, label: String) {
    Column(
        modifier = modifier
            .background(Color(0xFF1C3437), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = Color(0xFFF1F7F4), fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9EB7AE), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF14202B)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Text(
            message,
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AssetRow(
    asset: Asset,
    isWatched: Boolean,
    onClick: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f).clickable(onClick = onClick).padding(vertical = 2.dp)
            ) {
                Text(asset.name, fontWeight = FontWeight.SemiBold)
                Text(asset.identityStatus, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(asset.symbol, fontWeight = FontWeight.Bold)
                TextButton(onClick = onToggleWatchlist) {
                    Text(if (isWatched) "Siguiendo" else "+ Seguir")
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151F29))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFE8EEF4), fontWeight = FontWeight.Bold)
                if (description != null) {
                    Text(description, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                content()
            }
        )
    }
}

@Composable
private fun AssetDetailDialog(
    asset: Asset,
    isWatched: Boolean,
    viewModel: AssetExplorerViewModel,
    onToggleWatchlist: () -> Unit,
    onDismiss: () -> Unit
) {
    val deployments by viewModel.observeDeployments(asset.assetId)
        .collectAsState(initial = emptyList())
    val provenance by viewModel.observeProvenance(asset.assetId)
        .collectAsState(initial = emptyList())
    val marketQuotes by viewModel.marketQuotes.collectAsState()
    val marketQuote = marketQuotes[asset.assetId]
    val headlineQuote = marketQuote ?: coinMarketCapQuotes[asset.assetId]
    val coinMarketCapQuotes by viewModel.coinMarketCapQuotes.collectAsState()
    val coinMarketCapQuote = coinMarketCapQuotes[asset.assetId]
    val historySyncStates by viewModel.marketHistorySync.collectAsState()
    val historySync = historySyncStates[asset.assetId]
    val dailyHistoryByAsset by viewModel.dailyMarketHistory.collectAsState()
    val dailyHistory = dailyHistoryByAsset[asset.assetId].orEmpty()
    val researchDossiers by viewModel.researchDossiers.collectAsState()
    val routescanTokenSupplies by viewModel.routescanTokenSupplies.collectAsState()

    LaunchedEffect(asset.assetId) {
        viewModel.loadLatestResearchDossier(asset.assetId)
        viewModel.loadLatestMarketQuote(asset.assetId)
        viewModel.loadDailyMarketHistory(asset.assetId)
    }
    LaunchedEffect(deployments) {
        deployments.filter { it.representationKind == "ERC20" }.forEach { deployment ->
            viewModel.loadLatestRoutescanTokenSupply(asset.assetId, deployment.deploymentId)
        }
    }

    AlertDialog(
        modifier = Modifier.fillMaxWidth(0.96f),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = Color(0xFF111922),
        titleContentColor = Color(0xFFE8EEF4),
        textContentColor = Color(0xFFCAD4DE),
        shape = RoundedCornerShape(28.dp),
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(asset.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(asset.symbol.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            var showAudit by remember(asset.assetId) { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF172B31))
                ) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("PRECIO DE MERCADO · USD", style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9BB8AD), fontWeight = FontWeight.Bold)
                        Text(
                            marketQuote?.priceUsd?.let { "\$" + String.format(java.util.Locale.US, "%,.4f", it) }
                                ?: "Cotización no disponible",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFFF0F7F3), fontWeight = FontWeight.Bold
                        )
                        Text((headlineQuote?.let { if (marketQuote != null) "CoinPaprika" else "CoinMarketCap" } ?: "MERCADO") + " · " + (headlineQuote?.freshnessStatus ?: "SIN DATOS"),
                            style = MaterialTheme.typography.labelMedium, color = Color(0xFF8DDFB8))
                        TextButton(onClick = { viewModel.refreshMarketQuote(asset.assetId, asset.symbol) },
                            enabled = marketQuote?.isLoading != true) {
                            Text(if (marketQuote?.isLoading == true) "Actualizando…" else "Actualizar cotización")
                        }
                    }
                }

                DetailSection("Evolución del precio", "Histórico diario disponible; no representa velas OHLCV ni precios ejecutables.") {
                    MarketSparkline(dailyHistory, chartHeight = 150.dp)
                    Text(
                        if (dailyHistory.isEmpty()) "Sin histórico descargado. El gráfico aparecerá cuando haya puntos válidos."
                        else "${dailyHistory.size} puntos diarios almacenados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MarketHistorySyncDetails(state = historySync,
                        onDownload = { viewModel.downloadDailyMarketHistory(asset.assetId, asset.symbol) })
                    if (dailyHistory.isNotEmpty()) MarketHistoryPoints(points = dailyHistory)
                }

                DetailSection("Contraste de mercado", "Comparación de proveedores; no es una señal de compra o venta.") {
                    MarketQuoteComparison(coinPaprika = marketQuote, coinMarketCap = coinMarketCapQuote)
                    Text("CoinMarketCap", fontWeight = FontWeight.SemiBold)
                    MarketQuoteDetails(quote = coinMarketCapQuote, providerName = "CoinMarketCap",
                        onRefresh = { viewModel.refreshMarketQuote(asset.assetId, asset.symbol) })
                }

                DetailSection("Investigación del activo", "Expediente construido con observaciones persistidas y evidencia disponible.") {
                    ResearchDossierDetails(researchDossiers[asset.assetId])
                    TextButton(onClick = { viewModel.generateResearchDossier(asset.assetId) }) {
                        Text("Actualizar expediente")
                    }
                }

                DetailSection("Tokenómica y suministro on-chain", "Los valores se muestran solo cuando existe evidencia de proveedor o de red.") {
                    val erc20Deployments = deployments.filter { it.representationKind == "ERC20" }
                    if (erc20Deployments.isEmpty()) {
                        Text("No hay contratos EVM verificados registrados para este activo.",
                            style = MaterialTheme.typography.bodySmall)
                    } else {
                        erc20Deployments.forEach { deployment ->
                            Text(deployment.networkName, fontWeight = FontWeight.SemiBold)
                            RoutescanTokenSupplyDetails(state = routescanTokenSupplies[deployment.deploymentId],
                                onRefresh = { viewModel.refreshRoutescanTokenSupply(asset.assetId, deployment.deploymentId) })
                        }
                    }
                }

                DetailSection("Redes y representaciones", "Identidad y contratos asociados al activo.") {
                    if (deployments.isEmpty()) {
                        Text(if (asset.symbol == "USDC" || asset.symbol == "USDT")
                            "Activo multired: todavía no hay despliegues asignados."
                            else "No hay despliegues registrados.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        deployments.forEach { deployment ->
                            Text("${deployment.networkName} · ${deployment.representationKind} · ${deployment.decimals ?: "N/D"} decimales",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                TextButton(onClick = { showAudit = !showAudit }) {
                    Text(if (showAudit) "Ocultar auditoría y procedencia" else "Ver auditoría y procedencia")
                }
                if (showAudit) {
                    DetailSection("Auditoría de datos", "Identificadores, marcas temporales, validaciones y procedencia.") {
                        Text("ID local: ${asset.assetId}", style = MaterialTheme.typography.bodySmall)
                        Text("Clave canónica: ${asset.canonicalKey}", style = MaterialTheme.typography.bodySmall)
                        Text("Estado de identidad: ${asset.identityStatus}", style = MaterialTheme.typography.bodySmall)
                        if (provenance.isEmpty()) {
                            Text("No hay observaciones de procedencia registradas.", style = MaterialTheme.typography.bodySmall)
                        } else {
                            provenance.forEach { observation ->
                                ProvenanceObservation(observation)
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onToggleWatchlist) {
                Text(if (isWatched) "Quitar de seguimiento" else "Añadir a seguimiento")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}


@Composable
private fun RoutescanTokenSupplyDetails(
    state: RoutescanTokenSupplyUiState?,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text("Suministro on-chain · Routescan", fontWeight = FontWeight.SemiBold)
        if (state?.decimalAmount != null) {
            Text(
                "Total supply: ${state.decimalAmount} tokens",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Raw uint256: ${state.rawAmount} · Decimales: ${state.decimals}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Ethereum chain ID: ${state.chainId} · Bloque: ${state.blockNumber}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Contrato: ${state.contractAddress}",
                style = MaterialTheme.typography.bodySmall
            )
            state.retrievedAtEpochMs?.let {
                Text(
                    "Consultado: " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(Date(it)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Text(
                "Todavía no hay una observación on-chain guardada.",
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (state?.error != null) {
            Text(
                "Error Routescan: ${state.error}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        TextButton(
            onClick = onRefresh,
            enabled = state?.isLoading != true
        ) {
            Text(if (state?.isLoading == true) "Consultando Routescan…" else "Consultar / actualizar suministro")
        }
    }
}


@Composable
private fun ResearchDossierDetails(dossier: AssetResearchDossier?) {
    if (dossier == null) {
        Text("Todavía no se ha generado un expediente.", style = MaterialTheme.typography.bodySmall)
        return
    }
    val market = dossier.market
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text("Estado: ${dossier.overallEvidenceStatus}", fontWeight = FontWeight.SemiBold)
        Text("Identidad: ${dossier.identityStatus} · evaluación: ${dossier.identityAssessment}")
        Text("Evidencias registradas: ${dossier.evidenceCount}")
        Text("Cotización: " + (market.priceUsd?.let {
            "$" + String.format(java.util.Locale.US, "%.8f", it) + " USD"
        } ?: "UNKNOWN"))
        Text("Fuente: ${market.provider ?: "UNKNOWN"} · frescura: ${market.freshnessStatus}")
        Text("Puntos históricos: ${market.historyPointCount}")
        Text("Retorno del periodo disponible: " + (market.periodReturnPercent?.let {
            String.format(java.util.Locale.US, "%+.2f%%", it)
        } ?: "UNKNOWN"))
        Text("Contraste de proveedores: " + market.quoteComparisonStatus +
            (market.quoteDifferencePercent?.let { " · diferencia " + String.format(java.util.Locale.US, "%.3f", it) + "%" } ?: ""))
        Text("Observaciones comparadas: " +
            (market.quoteComparisonPrimaryObservationId ?: "N/D") + " / " +
            (market.quoteComparisonSecondaryObservationId ?: "N/D"))
        Text("Máximo / mínimo del histórico: " + (market.periodHighUsd?.let {
            "$" + String.format(java.util.Locale.US, "%.6f", it)
        } ?: "UNKNOWN") + " / " + (market.periodLowUsd?.let {
            "$" + String.format(java.util.Locale.US, "%.6f", it)
        } ?: "UNKNOWN"))
        Text("Fundamentos: ${dossier.fundamentals} · Tokenomics: ${dossier.tokenomics}")
        dossier.tokenomicsEvidence?.let { evidence ->
            Text("EVIDENCIA TOKENÓMICA · CoinMarketCap", fontWeight = FontWeight.SemiBold)
            listOf(
                evidence.circulatingSupply,
                evidence.totalSupply,
                evidence.maxSupply,
                evidence.marketCapUsd,
                evidence.fullyDilutedMarketCapUsd
            ).forEach { field ->
                val valueText = field.value?.let { value ->
                    when (field.fieldKey) {
                        "marketCapUsd", "fullyDilutedMarketCapUsd" ->
                            "\$" + String.format(java.util.Locale.US, "%,.2f", value) + " USD"
                        else -> String.format(java.util.Locale.US, "%,.8f", value)
                    }
                } ?: field.status.name
                Text(
                    "${field.fieldKey}: $valueText · ${field.status}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Fuente: ${field.sourceName ?: "N/D"} · Observado: " +
                        (field.observedAtEpochMs?.let {
                            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                                .format(Date(it))
                        } ?: "N/D") +
                        " · Recuperado: " +
                        (field.retrievedAtEpochMs?.let {
                            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                                .format(Date(it))
                        } ?: "N/D"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (field.notes.isNotEmpty()) {
                    Text(
                        "Validación: ${field.notes.joinToString()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Text(
                "Inflación anual, calendario de emisiones y desbloqueos: UNKNOWN; este endpoint no aporta esos datos.",
                style = MaterialTheme.typography.bodySmall
            )
        } ?: Text(
            "Suministros y emisiones: UNKNOWN · todavía no hay evidencia tokenómica de CoinMarketCap guardada.",
            style = MaterialTheme.typography.bodySmall
        )
        Text("Seguridad: ${dossier.security} · Gobernanza: ${dossier.governance}")
        Text("Redes/representaciones: ${dossier.deployments.size}")
        Text(
            "Generado: " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(Date(dossier.generatedAtEpochMs)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Cobertura parcial: UNKNOWN/NOT_ASSESSED no se infieren ni se convierten en cero. Esto no es una valoración ni una recomendación.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun MarketQuoteDetails(
    quote: MarketQuoteUiState?,
    providerName: String = "CoinPaprika",
    onRefresh: () -> Unit
) {
    val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    when {
        quote?.priceUsd != null -> {
            Text(
                "$" + String.format(java.util.Locale.US, "%.8f", quote.priceUsd) + " USD",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text("Proveedor: ${providerName} · ID: ${quote.providerId}", style = MaterialTheme.typography.bodySmall)
            quote.providerUpdatedAtEpochMs?.let {
                Text("Actualizado por el proveedor: ${formatter.format(Date(it))}", style = MaterialTheme.typography.bodySmall)
            }
            quote.retrievedAtEpochMs?.let {
                Text("Recuperado y guardado localmente: ${formatter.format(Date(it))}", style = MaterialTheme.typography.bodySmall)
            }
            Text(quote.attribution ?: "Datos proporcionados por ${providerName}", style = MaterialTheme.typography.bodySmall)
            Text("Frescura local: ${quote.freshnessStatus} · ${quote.freshnessReason}", style = MaterialTheme.typography.bodySmall)
            Text("Política operativa local: FRESH hasta 10 min; STALE hasta 60 min; EXPIRED después. No certifica exactitud del precio.", style = MaterialTheme.typography.bodySmall)
            Text("Caché local: intervalo mínimo de 10 minutos entre consultas por activo.", style = MaterialTheme.typography.bodySmall)
        }
        else -> Text("No hay cotización guardada para este activo.", style = MaterialTheme.typography.bodySmall)
    }
    if (quote?.error != null) {
        Text("Error de actualización: ${quote.error}", color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall)
    }
    TextButton(onClick = onRefresh, enabled = quote?.isLoading != true) {
        Text(if (quote?.isLoading == true) "Consultando proveedor…" else "Actualizar cotización")
    }
}


@Composable
private fun MarketQuoteComparison(
    coinPaprika: MarketQuoteUiState?,
    coinMarketCap: MarketQuoteUiState?
) {
    val paprikaPrice = coinPaprika?.priceUsd
    val cmcPrice = coinMarketCap?.priceUsd
    val paprikaTime = coinPaprika?.providerUpdatedAtEpochMs
    val cmcTime = coinMarketCap?.providerUpdatedAtEpochMs
    if (paprikaPrice == null || cmcPrice == null || paprikaTime == null || cmcTime == null) {
        Text(
            "Comparación entre proveedores: pendiente de dos cotizaciones válidas.",
            style = MaterialTheme.typography.bodySmall
        )
        return
    }
    val timeDifference = kotlin.math.abs(paprikaTime - cmcTime)
    if (timeDifference > 5 * 60 * 1000L ||
        coinPaprika?.freshnessStatus != "FRESH" ||
        coinMarketCap?.freshnessStatus != "FRESH"
    ) {
        Text(
            "Comparación: NO COMPARABLE · cotizaciones no suficientemente sincronizadas o frescas.",
            style = MaterialTheme.typography.bodySmall
        )
        return
    }
    val meanPrice = (paprikaPrice + cmcPrice) / 2.0
    val differencePercent = kotlin.math.abs(paprikaPrice - cmcPrice) / meanPrice * 100.0
    val status = if (differencePercent > 2.0) "CONFLICTED" else "CONSISTENT"
    Text(
        "Contraste CoinPaprika / CoinMarketCap: $status · diferencia ${String.format(java.util.Locale.US, "%.3f", differencePercent)}%. Umbral operativo: 2%. No se promedian precios.",
        color = if (status == "CONFLICTED") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun MarketHistorySyncDetails(
    state: MarketHistorySyncUiState?,
    onDownload: () -> Unit
) {
    state?.message?.let {
        Text(it, style = MaterialTheme.typography.bodySmall)
    }
    if (state?.error != null) {
        Text(
            "Error de histórico: ${state.error}",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
    TextButton(onClick = onDownload, enabled = state?.isLoading != true) {
        Text(if (state?.isLoading == true) "Descargando e importando…" else "Descargar último año")
    }
}


@Composable
private fun MarketSparkline(
    points: List<CoinPaprikaHistoricalPoint>,
    chartHeight: androidx.compose.ui.unit.Dp = 52.dp
) {
    val ordered = points
        .asSequence()
        .filter { it.priceUsd.isFinite() && it.priceUsd > 0.0 }
        .sortedBy { it.timestampEpochMs }
        .toList()
        .takeLast(30)
    if (ordered.size < 2) return

    val prices = ordered.map { it.priceUsd }
    val minPrice = prices.minOrNull() ?: return
    val maxPrice = prices.maxOrNull() ?: return
    val range = (maxPrice - minPrice).takeIf { it > 0.0 } ?: (maxPrice * 0.000001).coerceAtLeast(1e-12)
    val lineColor = MaterialTheme.colorScheme.primary

    Canvas(Modifier.fillMaxWidth().height(chartHeight)) {
        val path = Path()
        prices.forEachIndexed { index, price ->
            val x = index.toFloat() / (prices.size - 1).toFloat() * size.width
            val normalized = ((price - minPrice) / range).toFloat().coerceIn(0f, 1f)
            val y = size.height - normalized * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
private fun MarketHistoryPoints(points: List<CoinPaprikaHistoricalPoint>) {
    val dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM)
    Spacer(Modifier.height(8.dp))
    Text("Tendencia de precios · últimos 30 puntos diarios", fontWeight = FontWeight.Medium)
    MarketSparkline(points, chartHeight = 112.dp)
    Text("Representación de precios de cierre diarios; no son velas OHLCV ni precios ejecutables.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(8.dp))
    Text("Últimos puntos almacenados", fontWeight = FontWeight.Medium)
    if (points.isEmpty()) {
        Text("Todavía no hay puntos históricos guardados.", style = MaterialTheme.typography.bodySmall)
    } else {
        points.take(10).forEach { point ->
            val date = dateFormatter.format(Date(point.timestampEpochMs))
            val price = String.format(java.util.Locale.US, "%.8f", point.priceUsd)
            Text("$date · $price USD", style = MaterialTheme.typography.bodySmall)
            Text(
                "Vol. 24 h: ${point.volume24hUsd?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: "N/D"} · Cap.: ${point.marketCapUsd?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: "N/D"}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun ProvenanceObservation(observation: AssetObservationProvenance) {
    val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    Text("Fuente: ${observation.sourceName}", fontWeight = FontWeight.Medium)
    Text("Tipo de fuente: ${observation.providerType}", style = MaterialTheme.typography.bodySmall)
    Text("Uso permitido: ${observation.permittedUseStatus}", style = MaterialTheme.typography.bodySmall)
    Text("Observación: ${observation.observationType}", style = MaterialTheme.typography.bodySmall)
    Text(
        "Recuperado: ${formatter.format(Date(observation.retrievedAtEpochMs))}",
        style = MaterialTheme.typography.bodySmall
    )
    Text(
        "Observado: ${observation.observedAtEpochMs?.let { formatter.format(Date(it)) } ?: "No informado"}",
        style = MaterialTheme.typography.bodySmall
    )
    Text("Estado: ${observation.status} · Calidad: ${observation.quality}",
        style = MaterialTheme.typography.bodySmall)
}
