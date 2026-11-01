package com.iie.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.iie.android.data.market.GlobalMarketMetrics
import com.iie.android.data.market.FearGreedIndex
import com.iie.android.data.market.HistoricalRiskAnalyzer
import com.iie.android.data.market.HistoricalProviderComparator
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val explorerViewModel = ViewModelProvider(
            this, AssetExplorerViewModel.Factory(application)
        )[AssetExplorerViewModel::class.java]

        setContent {
            IIETheme { AssetExplorerScreen(explorerViewModel) }
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
    val coinMarketCapQuotes by viewModel.coinMarketCapQuotes.collectAsState()
    val dashboardRefreshStatus by viewModel.dashboardRefreshStatus.collectAsState()
    val globalMarket by viewModel.globalMarket.collectAsState()
    val marketHistorySync by viewModel.marketHistorySync.collectAsState()
    val dailyHistoryByAsset by viewModel.dailyMarketHistory.collectAsState()
    val coinMarketCapDailyHistoryByAsset by viewModel.coinMarketCapDailyHistory.collectAsState()
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
        if (destination == "Mercados" || destination == "Resumen") {
            assets.forEach { asset ->
                viewModel.loadLatestMarketQuote(asset.assetId)
                viewModel.loadDailyMarketHistory(asset.assetId)
                viewModel.loadCoinMarketCapDailyHistory(asset.assetId)
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
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                "LOCAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "v${BuildConfig.VERSION_NAME} · #${BuildConfig.VERSION_CODE}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(22.dp))

                when (destination) {
                    "Ajustes" -> CoinMarketCapSettingsScreen(
                        onHistoricalSyncRequested = { viewModel.refreshAllMarketQuotes(assets) }
                    )
                    "Resumen" -> {
                        InvestorDashboard(
                            assets = assets,
                            watchlist = watchlist,
                            quotes = coinMarketCapQuotes,
                            history = dailyHistoryByAsset,
                            coinMarketCapHistory = coinMarketCapDailyHistoryByAsset,
                            globalMetrics = globalMarket.globalMetrics,
                            fearGreed = globalMarket.fearGreed,
                            catalogStatus = catalogStatus,
                            refreshStatus = dashboardRefreshStatus,
                            onExplore = { destination = "Mercados" },
                            onRefresh = { viewModel.refreshAllMarketQuotes(assets) },
                            onAssetClick = { selectedAsset = it },
                            onToggleWatchlist = { viewModel.toggleWatchlist(it) }
                        )
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
    asset: Asset, quote: MarketQuoteUiState?, history: List<CoinPaprikaHistoricalPoint>,
    historySync: MarketHistorySyncUiState?, isWatched: Boolean,
    onClick: () -> Unit, onToggleWatchlist: () -> Unit
) {
    val ordered = history.filter { it.priceUsd.isFinite() && it.priceUsd > 0.0 }.sortedBy { it.timestampEpochMs }
    val first = ordered.firstOrNull()
    val last = ordered.lastOrNull()
    val change = if (first != null && last != null && first.timestampEpochMs < last.timestampEpochMs)
        ((last.priceUsd / first.priceUsd) - 1.0) * 100.0 else null
    val span = if (first != null && last != null) {
        val days = ((last.timestampEpochMs - first.timestampEpochMs) / 86_400_000L).toInt()
        if (days <= 0) "menos de 1 día" else days.toString() + " días"
    } else "sin cobertura"
    val source = last?.sourceId?.removePrefix("source:")?.uppercase() ?: "no identificada"
    val freshness = when {
        quote?.isLoading == true -> "ACTUALIZANDO"
        quote?.freshnessStatus == "FRESH" -> "COTIZACIÓN RECIENTE"
        quote?.freshnessStatus != null -> quote.freshnessStatus
        else -> "SIN COTIZACIÓN"
    }
    val freshnessColor = when (quote?.freshnessStatus) {
        "FRESH" -> IIEDesign.positive
        null -> IIEDesign.neutral
        else -> IIEDesign.warning
    }
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(IIEDesign.Radius.large),
        colors = CardDefaults.cardColors(containerColor = IIEDesign.surface)) {
        Column(Modifier.fillMaxWidth().padding(IIEDesign.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(IIEDesign.Spacing.sm)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(asset.name, color = IIEDesign.textPrimary, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(asset.symbol.uppercase(), color = IIEDesign.accentSecondary,
                            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("·", color = IIEDesign.textSecondary)
                        Text(if (isWatched) "EN SEGUIMIENTO" else "NO SEGUIDO",
                            color = IIEDesign.textSecondary, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(quote?.priceUsd?.takeIf { it.isFinite() && it > 0.0 }?.let {
                        "$" + String.format(java.util.Locale.US, "%,.6f", it)
                    } ?: "—", color = IIEDesign.textPrimary, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge)
                    Surface(color = IIEDesign.surfaceElevated, shape = RoundedCornerShape(IIEDesign.Radius.pill)) {
                        Text(freshness, Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            color = freshnessColor, style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("HISTÓRICO DE PRECIOS", color = IIEDesign.textSecondary,
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(ordered.size.toString() + " observaciones · " + span,
                        color = IIEDesign.textSecondary, style = MaterialTheme.typography.labelSmall)
                }
                if (ordered.size >= 2) MarketSparkline(ordered, chartHeight = 64.dp)
                else Surface(Modifier.fillMaxWidth().height(48.dp), color = IIEDesign.surfaceElevated,
                    shape = RoundedCornerShape(IIEDesign.Radius.small)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Cobertura histórica insuficiente para calcular tendencia",
                            color = IIEDesign.textSecondary, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("VARIACIÓN DEL PERÍODO OBSERVADO", color = IIEDesign.textSecondary,
                        style = MaterialTheme.typography.labelSmall)
                    Text(change?.let { String.format(java.util.Locale.US, "%+.2f%%", it) } ?: "No calculable",
                        color = when {
                            change == null -> IIEDesign.neutral
                            change >= 0.0 -> IIEDesign.positive
                            else -> IIEDesign.negative
                        }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Fuente histórica: " + source, color = IIEDesign.textSecondary,
                        style = MaterialTheme.typography.labelSmall)
                }
                TextButton(onClick = onToggleWatchlist) {
                    Text(if (isWatched) "★ Siguiendo" else "+ Seguir",
                        color = if (isWatched) IIEDesign.accent else IIEDesign.textPrimary)
                }
            }
            if (historySync?.isLoading == true) Text("Sincronizando histórico…",
                color = IIEDesign.warning, style = MaterialTheme.typography.labelSmall)
            else if (historySync?.error != null) Text("Error al sincronizar histórico: " + historySync.error,
                color = IIEDesign.negative, style = MaterialTheme.typography.labelSmall)
            Text("Abrir expediente del activo  →", color = IIEDesign.accentSecondary,
                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun chooseCanonicalHistoricalSeries(
    coinPaprika: List<CoinPaprikaHistoricalPoint>,
    coinMarketCap: List<CoinPaprikaHistoricalPoint>
): com.iie.android.data.market.CanonicalHistoricalSeries? {
    val candidates = listOf(coinPaprika, coinMarketCap)
        .filter { it.isNotEmpty() }
    val combined = candidates.flatten()
    val priority = candidates
        .sortedWith(
            compareByDescending<List<CoinPaprikaHistoricalPoint>> { it.size }
                .thenByDescending { points -> points.maxOfOrNull { it.retrievedAtEpochMs } ?: 0L }
                .thenBy { points -> points.firstOrNull()?.sourceId.orEmpty() }
        )
        .mapNotNull { it.firstOrNull()?.sourceId }
        .distinct()
    return HistoricalRiskAnalyzer().selectCanonicalSeries(combined, priority)
}


@Composable
private fun InvestorDashboard(
    assets: List<Asset>,
    watchlist: Set<String>,
    quotes: Map<String, MarketQuoteUiState>,
    history: Map<String, List<CoinPaprikaHistoricalPoint>>,
    coinMarketCapHistory: Map<String, List<CoinPaprikaHistoricalPoint>>,
    globalMetrics: GlobalMarketMetrics?,
    fearGreed: FearGreedIndex?,
    catalogStatus: String,
    refreshStatus: String?,
    onExplore: () -> Unit,
    onRefresh: () -> Unit,
    onAssetClick: (Asset) -> Unit,
    onToggleWatchlist: (String) -> Unit
) {
    val coveredCaps = assets.mapNotNull { asset -> quotes[asset.assetId]?.marketCapUsd?.takeIf { it.isFinite() && it > 0.0 } }
    val coveredCapTotal = coveredCaps.sum()
    val capCoverage = coveredCaps.size
    val canonicalHistoryByAsset = assets.associate { asset ->
        asset.assetId to chooseCanonicalHistoricalSeries(
            history[asset.assetId].orEmpty(),
            coinMarketCapHistory[asset.assetId].orEmpty()
        )
    }
    val historyCoverage = canonicalHistoryByAsset.values.count { (it?.points?.size ?: 0) >= 2 }
    val volatilityValues = canonicalHistoryByAsset.values.mapNotNull {
        it?.let { series -> historicalVolatilityPercent(series.points) }
    }.sorted()
    val canonicalCmcCoverage = canonicalHistoryByAsset.values.count { it?.sourceId == "source:coinmarketcap" }
    val canonicalPaprikaCoverage = canonicalHistoryByAsset.values.count { it?.sourceId == "source:coinpaprika" }
    val medianVolatility = volatilityValues.takeIf { it.isNotEmpty() }?.let {
        if (it.size % 2 == 1) it[it.size / 2] else (it[it.size / 2 - 1] + it[it.size / 2]) / 2.0
    }
    val movers = assets.mapNotNull { asset ->
        val points = canonicalHistoryByAsset[asset.assetId]?.points.orEmpty().sortedBy { it.timestampEpochMs }
        if (points.size < 2 || points.first().priceUsd <= 0.0) null
        else asset to ((points.last().priceUsd / points.first().priceUsd) - 1.0) * 100.0
    }.filter { it.second.isFinite() }.sortedByDescending { kotlin.math.abs(it.second) }.take(4)
    val followedAssets = assets.filter { it.assetId in watchlist }.take(5)

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            InvestorCommandHeader(
                assetCount = assets.size,
                watchedCount = watchlist.size,
                catalogStatus = catalogStatus,
                refreshStatus = refreshStatus,
                onExplore = onExplore,
                onRefresh = onRefresh
            )
        }
        item {
            Text("MERCADO GLOBAL · COINPAPRIKA", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (globalMetrics == null) {
                EmptyState("Las métricas globales aún no están guardadas. Usa Actualizar datos para consultar la fuente.")
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("Capitalización global", formatCompactUsd(globalMetrics.marketCapUsd), Modifier.weight(1f))
                    MetricCard("Volumen 24 h", formatCompactUsd(globalMetrics.volume24hUsd), Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("Dominancia BTC", String.format(java.util.Locale.US, "%.2f%%", globalMetrics.bitcoinDominancePercent), Modifier.weight(1f))
                    MetricCard("Criptomonedas activas", String.format(java.util.Locale.US, "%,d", globalMetrics.activeCryptocurrencies), Modifier.weight(1f))
                }
                val capChange = globalMetrics.marketCapChange24hPercent
                val volumeChange = globalMetrics.volumeChange24hPercent
                Text(
                    "Cambio 24 h · capitalización: ${capChange?.let { String.format(java.util.Locale.US, "%+.2f%%", it) } ?: "N/D"} · volumen: ${volumeChange?.let { String.format(java.util.Locale.US, "%+.2f%%", it) } ?: "N/D"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "CoinPaprika · dato del proveedor: ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(globalMetrics.providerUpdatedAtEpochMs))} · recuperado: ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(globalMetrics.retrievedAtEpochMs))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            Text("SENTIMIENTO · FEAR & GREED (BITCOIN)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (fearGreed == null) {
                EmptyState("Fear & Greed todavía no está disponible. Se mostrará cuando Alternative.me responda.")
            } else {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF111B25))) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Fear & Greed · Bitcoin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(fearGreed.classification, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Fuente: Alternative.me · actualización diaria", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Fecha del índice: ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(fearGreed.providerUpdatedAtEpochMs))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${fearGreed.value}/100", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
                Text("Indicador de sentimiento orientado a Bitcoin; no es una señal autónoma de inversión. Alternative.me exige atribución visible junto al dato.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Text("PANORAMA DEL UNIVERSO", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Capitalización cubierta", formatCompactUsd(coveredCapTotal.takeIf { capCoverage > 0 }), Modifier.weight(1f))
                MetricCard("Cobertura CMC", "$capCoverage / ${assets.size}", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Volatilidad anualizada mediana · ${volatilityValues.size} activos", medianVolatility?.let { String.format(java.util.Locale.US, "%.1f%%", it) } ?: "Sin datos suficientes", Modifier.weight(1f))
                MetricCard("Históricos disponibles", "$historyCoverage / ${assets.size}", Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
            Text("Capitalización: solo activos cubiertos por CMC; no es el total del mercado. Riesgo: una serie canónica completa por activo, priorizada por cobertura y frescura; nunca se mezclan ni promedian proveedores. Fuentes canónicas: CMC $canonicalCmcCoverage · CoinPaprika $canonicalPaprikaCoverage.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            val comparator = remember { HistoricalProviderComparator() }
            val comparisons = assets.mapNotNull { asset ->
                comparator.compare(
                    history[asset.assetId].orEmpty(),
                    coinMarketCapHistory[asset.assetId].orEmpty()
                ).takeIf { it.matchedObservations > 0 }?.let { asset to it }
            }
            val paprikaQuality = assets.map { asset ->
                comparator.quality(history[asset.assetId].orEmpty(), "source:coinpaprika")
            }
            val cmcQuality = assets.map { asset ->
                comparator.quality(coinMarketCapHistory[asset.assetId].orEmpty(), "source:coinmarketcap")
            }
            val paprikaSeriesCoverage = paprikaQuality.count { it.usableTimestamps >= 2 }
            val cmcSeriesCoverage = cmcQuality.count { it.usableTimestamps >= 2 }
            fun medianRetrievalAgeHours(qualities: List<com.iie.android.data.market.HistoricalProviderSeriesQuality>): Double? {
                val now = System.currentTimeMillis()
                val ages = qualities.mapNotNull { it.latestRetrievedAtEpochMs }
                    .map { ((now - it).coerceAtLeast(0L) / 3_600_000.0) }.sorted()
                return ages.takeIf { it.isNotEmpty() }?.let {
                    if (it.size % 2 == 1) it[it.size / 2] else (it[it.size / 2 - 1] + it[it.size / 2]) / 2.0
                }
            }
            val paprikaMedianAge = medianRetrievalAgeHours(paprikaQuality)
            val cmcMedianAge = medianRetrievalAgeHours(cmcQuality)
            val ambiguousTimestamps = paprikaQuality.sumOf { it.conflictingTimestamps } +
                cmcQuality.sumOf { it.conflictingTimestamps }
            val invalidHistoryRows = paprikaQuality.sumOf { it.invalidObservations } +
                cmcQuality.sumOf { it.invalidObservations }
            val divergences = comparisons.mapNotNull { it.second.latestSymmetricDivergencePercent }.sorted()
            val medianDivergence = divergences.takeIf { it.isNotEmpty() }?.let {
                if (it.size % 2 == 1) it[it.size / 2] else (it[it.size / 2 - 1] + it[it.size / 2]) / 2.0
            }
            val highestDivergence = comparisons.maxByOrNull {
                it.second.latestSymmetricDivergencePercent ?: Double.NEGATIVE_INFINITY
            }
            Text("CALIDAD DE DATOS · CONTRASTE ENTRE PROVEEDORES", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Activos comparables", "${comparisons.size} / ${assets.size}", Modifier.weight(1f))
                MetricCard("Divergencia mediana", medianDivergence?.let {
                    String.format(java.util.Locale.US, "%.3f%%", it)
                } ?: "Sin pares exactos", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("CoinPaprika · series válidas", "$paprikaSeriesCoverage / ${assets.size}", Modifier.weight(1f))
                MetricCard("CMC · series válidas", "$cmcSeriesCoverage / ${assets.size}", Modifier.weight(1f))
            }
            Text("Recuperación mediana: CoinPaprika ${paprikaMedianAge?.let { String.format(java.util.Locale.US, "%.1f h", it) } ?: "N/D"} · CMC ${cmcMedianAge?.let { String.format(java.util.Locale.US, "%.1f h", it) } ?: "N/D"} · Filas inválidas: $invalidHistoryRows · timestamps conflictivos: $ambiguousTimestamps",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (highestDivergence != null) {
                Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111B25))) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("MAYOR DIVERGENCIA OBSERVADA", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Text("${highestDivergence.first.name} · ${String.format(java.util.Locale.US, "%.3f%%",
                            highestDivergence.second.latestSymmetricDivergencePercent ?: 0.0)}",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${highestDivergence.second.matchedObservations} timestamps UTC coincidentes",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                EmptyState("Todavía no existen timestamps UTC idénticos entre ambos proveedores. No se calcula ni se infiere una divergencia a partir de fechas aproximadas.")
            }
            Text("La comparación exige el mismo activo canónico, USD y timestamp UTC exacto. La divergencia simétrica compara las dos cotizaciones observadas; no se promedian precios ni se interpreta como oportunidad de arbitraje.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            val riskAnalyzer = remember { HistoricalRiskAnalyzer() }
            val riskByAsset = assets.mapNotNull { asset ->
                val canonical = canonicalHistoryByAsset[asset.assetId] ?: return@mapNotNull null
                val metrics = riskAnalyzer.analyze(canonical.points)
                if (metrics.observations < 2) null else asset to metrics
            }
            val volatilityCoverage = riskByAsset.count { it.second.annualizedVolatilityPercent != null }
            val tailCoverage = riskByAsset.count { it.second.historicalVar95Percent != null }
            val highestVolatility = riskByAsset.filter { it.second.annualizedVolatilityPercent != null }
                .sortedByDescending { it.second.annualizedVolatilityPercent }.take(4)
            val worstDrawdowns = riskByAsset.filter { it.second.maximumDrawdownPercent != null }
                .sortedByDescending { it.second.maximumDrawdownPercent }.take(4)
            Text("ANÁLISIS DE RIESGO · HISTÓRICO", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Volatilidad calculable", "$volatilityCoverage / ${assets.size}", Modifier.weight(1f))
                MetricCard("VaR 95% calculable", "$tailCoverage / ${assets.size}", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            if (riskByAsset.isEmpty()) {
                EmptyState("No hay histórico diario suficiente para calcular riesgo. Actualiza los datos y descarga históricos.")
            } else {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF111B25))) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("MAYOR VOLATILIDAD ANUALIZADA", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (highestVolatility.isEmpty()) Text("Muestra insuficiente: se requieren al menos 7 retornos diarios consecutivos.")
                        highestVolatility.forEach { (asset, metrics) ->
                            Row(Modifier.fillMaxWidth().clickable { onAssetClick(asset) }, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${asset.name} · ${metrics.dailyReturns} retornos", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                Text(String.format(java.util.Locale.US, "%.1f%%", metrics.annualizedVolatilityPercent), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("MAYOR CAÍDA DESDE MÁXIMOS (DRAWDOWN)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        worstDrawdowns.forEach { (asset, metrics) ->
                            Row(Modifier.fillMaxWidth().clickable { onAssetClick(asset) }, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(asset.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                Text(String.format(java.util.Locale.US, "%.1f%%", metrics.maximumDrawdownPercent), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        val representative = riskByAsset.firstOrNull { it.second.historicalVar95Percent != null }
                        Text(
                            representative?.let { (asset, metrics) ->
                                "Cola histórica 95% · ${asset.symbol.uppercase()}: VaR diario ${String.format(java.util.Locale.US, "%.2f%%", metrics.historicalVar95Percent)} · pérdida media de cola (ES) ${String.format(java.util.Locale.US, "%.2f%%", metrics.historicalExpectedShortfall95Percent)}"
                            } ?: "VaR y pérdida esperada de cola (ES) no disponibles: se requieren al menos 30 retornos diarios consecutivos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text("Volatilidad anualizada con desviación estándar muestral y √365. Drawdown: caída máxima desde un pico observado. VaR/ES: cola histórica diaria al 95%; no son pérdidas máximas ni pronósticos. Métricas por activo, no riesgo ponderado de cartera.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("RADAR DE INVESTIGACIÓN", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("${movers.size} señales", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            if (movers.isEmpty()) {
                EmptyState("Aún no hay suficientes puntos históricos para identificar movimientos. Descarga el histórico desde la ficha de cada activo.")
            } else {
                movers.forEach { (asset, change) ->
                    Card(Modifier.fillMaxWidth().padding(bottom = 7.dp).clickable { onAssetClick(asset) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF111B25))) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(asset.name, fontWeight = FontWeight.SemiBold)
                                Text("${asset.symbol.uppercase()} · período histórico disponible", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(String.format(java.util.Locale.US, "%+.2f%%", change), fontWeight = FontWeight.Bold, color = if (change >= 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Text("Los movimientos son alertas descriptivas para investigar, no señales de compra o venta.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("SEGUIMIENTO", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                TextButton(onClick = onExplore) { Text("Ver universo") }
            }
            if (followedAssets.isEmpty()) EmptyState("Todavía no sigues activos. Añádelos desde Mercados.")
            else followedAssets.forEach { asset -> AssetRow(asset, true, { onAssetClick(asset) }, { onToggleWatchlist(asset.assetId) }) }
        }
        item {
            Text("ESTADO DE LOS DATOS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF111B25))) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Capitalización con evidencia: $capCoverage de ${assets.size} activos", style = MaterialTheme.typography.bodyMedium)
                    Text("Histórico local: $historyCoverage de ${assets.size} activos", style = MaterialTheme.typography.bodyMedium)
                    Text(catalogStatus, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun historicalVolatilityPercent(points: List<CoinPaprikaHistoricalPoint>): Double? {
    val ordered = points.sortedBy { it.timestampEpochMs }
    val returns = ordered.zipWithNext { previous, next ->
        val elapsedMs = next.timestampEpochMs - previous.timestampEpochMs
        if (elapsedMs in 20L * 60L * 60L * 1000L..28L * 60L * 60L * 1000L && previous.priceUsd > 0.0) {
            next.priceUsd / previous.priceUsd - 1.0
        } else Double.NaN
    }.filter { it.isFinite() }
    if (returns.size < 7) return null
    val mean = returns.average()
    val variance = returns.sumOf { (it - mean) * (it - mean) } / (returns.size - 1)
    return (kotlin.math.sqrt(variance) * kotlin.math.sqrt(365.0) * 100.0).takeIf { it.isFinite() }
}

private fun formatCompactUsd(value: Double?): String {
    if (value == null || !value.isFinite()) return "Sin datos"
    return when {
        value >= 1_000_000_000_000.0 -> "\$" + String.format(java.util.Locale.US, "%.2f T", value / 1_000_000_000_000.0)
        value >= 1_000_000_000.0 -> "\$" + String.format(java.util.Locale.US, "%.2f B", value / 1_000_000_000.0)
        value >= 1_000_000.0 -> "\$" + String.format(java.util.Locale.US, "%.2f M", value / 1_000_000.0)
        else -> "\$" + String.format(java.util.Locale.US, "%,.0f", value)
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
        shape = RoundedCornerShape(IIEDesign.Radius.large),
        colors = CardDefaults.cardColors(containerColor = IIEDesign.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleMedium,
                    color = IIEDesign.textPrimary, fontWeight = FontWeight.Bold)
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
    val coinMarketCapQuotes by viewModel.coinMarketCapQuotes.collectAsState()
    val coinMarketCapQuote = coinMarketCapQuotes[asset.assetId]
    val historySyncStates by viewModel.marketHistorySync.collectAsState()
    val historySync = historySyncStates[asset.assetId]
    val dailyHistoryByAsset by viewModel.dailyMarketHistory.collectAsState()
    val dailyHistory = dailyHistoryByAsset[asset.assetId].orEmpty()
    val coinMarketCapDailyHistoryByAsset by viewModel.coinMarketCapDailyHistory.collectAsState()
    val coinMarketCapDailyHistory = coinMarketCapDailyHistoryByAsset[asset.assetId].orEmpty()
    val researchDossiers by viewModel.researchDossiers.collectAsState()
    val routescanTokenSupplies by viewModel.routescanTokenSupplies.collectAsState()

    LaunchedEffect(asset.assetId) {
        viewModel.loadLatestResearchDossier(asset.assetId)
        viewModel.loadLatestMarketQuote(asset.assetId)
        viewModel.loadDailyMarketHistory(asset.assetId)
        viewModel.loadCoinMarketCapDailyHistory(asset.assetId)
    }
    LaunchedEffect(deployments) {
        deployments.filter { it.representationKind == "ERC20" }.forEach { deployment ->
            viewModel.loadLatestRoutescanTokenSupply(asset.assetId, deployment.deploymentId)
        }
    }

    AlertDialog(
        modifier = Modifier.fillMaxWidth(0.96f),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = IIEDesign.background,
        titleContentColor = IIEDesign.textPrimary,
        textContentColor = IIEDesign.textSecondary,
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
            var selectedSection by remember(asset.assetId) { mutableStateOf("MERCADO") }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("CENTRO DE ANÁLISIS", color = IIEDesign.accentSecondary,
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("Evidencia disponible para ${asset.symbol.uppercase()}",
                    color = IIEDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("MERCADO", "FUNDAMENTOS", "RED").forEach { section ->
                        TextButton(onClick = { selectedSection = section }) {
                            Text(section, color = if (selectedSection == section)
                                IIEDesign.accentSecondary else IIEDesign.textSecondary,
                                fontWeight = if (selectedSection == section) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
                if (selectedSection == "MERCADO") {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = IIEDesign.surfaceElevated)
                ) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("PRECIO DE MERCADO · USD", style = MaterialTheme.typography.labelSmall,
                            color = IIEDesign.accentSecondary, fontWeight = FontWeight.Bold)
                        Text(
                            marketQuote?.priceUsd?.let { "\$" + String.format(java.util.Locale.US, "%,.4f", it) }
                                ?: "Cotización no disponible",
                            style = MaterialTheme.typography.headlineMedium,
                            color = IIEDesign.textPrimary, fontWeight = FontWeight.Bold
                        )
                        Text((if (marketQuote != null) "CoinPaprika" else "MERCADO") + " · " + (marketQuote?.freshnessStatus ?: "SIN DATOS"),
                            style = MaterialTheme.typography.labelMedium, color = IIEDesign.positive)
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
                    HistoricalProviderComparison(
                        coinPaprika = dailyHistory,
                        coinMarketCap = coinMarketCapDailyHistory
                    )
                    if (dailyHistory.isNotEmpty()) MarketHistoryPoints(points = dailyHistory)
                }

                DetailSection("Contraste de mercado", "Comparación de proveedores; no es una señal de compra o venta.") {
                    MarketQuoteComparison(coinPaprika = marketQuote, coinMarketCap = coinMarketCapQuote)
                    Text("CoinMarketCap", fontWeight = FontWeight.SemiBold)
                    MarketQuoteDetails(quote = coinMarketCapQuote, providerName = "CoinMarketCap",
                        onRefresh = { viewModel.refreshMarketQuote(asset.assetId, asset.symbol) })
                }

                }

                if (selectedSection == "FUNDAMENTOS") {
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

                }

                if (selectedSection == "RED") {
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
            "\$" + String.format(java.util.Locale.US, "%.8f", it) + " USD"
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
            "\$" + String.format(java.util.Locale.US, "%.6f", it)
        } ?: "UNKNOWN") + " / " + (market.periodLowUsd?.let {
            "\$" + String.format(java.util.Locale.US, "%.6f", it)
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
                "\$" + String.format(java.util.Locale.US, "%.8f", quote.priceUsd) + " USD",
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
    state?.coinMarketCapPointsReceived?.takeIf { it > 0 }?.let { received ->
        Text("CoinMarketCap · $received puntos recibidos · ${state.coinMarketCapPointsInserted} nuevos · ${state.coinMarketCapPointsAlreadyPresent} existentes",
            style = MaterialTheme.typography.bodySmall)
    }
    state?.coinMarketCapError?.let {
        Text("CoinMarketCap: $it", color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall)
    }
    TextButton(onClick = onDownload, enabled = state?.isLoading != true) {
        Text(if (state?.isLoading == true) "Descargando e importando…" else "Descargar último año · ambos proveedores")
    }
}


@Composable
private fun HistoricalProviderComparison(
    coinPaprika: List<CoinPaprikaHistoricalPoint>,
    coinMarketCap: List<CoinPaprikaHistoricalPoint>
) {
    val comparator = remember { HistoricalProviderComparator() }
    val result = remember(coinPaprika, coinMarketCap) {
        comparator.compare(coinPaprika, coinMarketCap)
    }
    val paprikaQuality = remember(coinPaprika) {
        comparator.quality(coinPaprika, "source:coinpaprika")
    }
    val cmcQuality = remember(coinMarketCap) {
        comparator.quality(coinMarketCap, "source:coinmarketcap")
    }
    val utcFormatter = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm 'UTC'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text("Contraste histórico independiente", fontWeight = FontWeight.SemiBold)
        Text("CoinPaprika: ${coinPaprika.size} puntos · CoinMarketCap: ${coinMarketCap.size} puntos",
            style = MaterialTheme.typography.bodySmall)
        Text("Timestamps UTC exactamente coincidentes: ${result.matchedObservations}",
            style = MaterialTheme.typography.bodySmall)
        Text(result.latestSymmetricDivergencePercent?.let {
            "Divergencia simétrica en la última observación común: ${String.format(java.util.Locale.US, "%.3f", it)}%"
        } ?: "Divergencia no calculable: no hay timestamps UTC exactos y precios válidos en ambas fuentes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        result.latestTimestampEpochMs?.let { timestamp ->
            Text("Instante UTC: ${utcFormatter.format(Date(timestamp))}",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("CoinPaprika: ${result.coinPaprikaPriceUsd} USD · CoinMarketCap: ${result.coinMarketCapPriceUsd} USD",
                style = MaterialTheme.typography.bodySmall)
        }
        Text("Calidad CoinPaprika: ${paprikaQuality.usableTimestamps} timestamps utilizables · ${paprikaQuality.invalidObservations} filas inválidas · ${paprikaQuality.conflictingTimestamps} timestamps ambiguos.")
        Text("Calidad CoinMarketCap: ${cmcQuality.usableTimestamps} timestamps utilizables · ${cmcQuality.invalidObservations} filas inválidas · ${cmcQuality.conflictingTimestamps} timestamps ambiguos.")
        Text("Última recuperación de la observación común: CoinPaprika ${result.coinPaprikaRetrievedAtEpochMs?.let { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(it)) } ?: "N/D"} · CMC ${result.coinMarketCapRetrievedAtEpochMs?.let { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(it)) } ?: "N/D"}",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Solo se emparejan el mismo activo, moneda USD y timestamp UTC exacto. Las series permanecen separadas; no se promedian ni se sustituyen precios entre proveedores.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    Text("Cotizaciones diarias observadas por el proveedor; no se deben interpretar como cierres oficiales, velas OHLCV ni precios ejecutables.",
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
