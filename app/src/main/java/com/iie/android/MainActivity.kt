package com.iie.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.iie.android.domain.model.Asset
import com.iie.android.domain.model.AssetObservationProvenance
import com.iie.android.domain.model.AssetResearchDossier
import com.iie.android.presentation.AssetExplorerViewModel
import com.iie.android.presentation.CoinMarketCapSettingsScreen
import com.iie.android.presentation.MarketQuoteUiState
import com.iie.android.presentation.MarketHistorySyncUiState
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
            MaterialTheme { AssetExplorerScreen(explorerViewModel) }
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
            bottomBar = {
                NavigationBar {
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
                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "CATÁLOGO DE ACTIVOS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            "El catálogo inicial conserva identificadores canónicos y referencias de proveedor. Los datos de mercado solo se muestran tras consultarlos; no se generan señales ni recomendaciones.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            catalogStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                when (destination) {
                    "Ajustes" -> CoinMarketCapSettingsScreen()
                    "Resumen" -> {
                        Text("VISTA GENERAL", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
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
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
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
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
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
    val researchDossiers by viewModel.researchDossiers.collectAsState()

    LaunchedEffect(asset.assetId) {
        viewModel.loadLatestResearchDossier(asset.assetId)
        viewModel.loadLatestMarketQuote(asset.assetId)
        viewModel.loadDailyMarketHistory(asset.assetId)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${asset.name} (${asset.symbol})") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("ID local: ${asset.assetId}")
                Text("Clave canónica: ${asset.canonicalKey}")
                Text("Estado de identidad: ${asset.identityStatus}")
                Spacer(Modifier.height(8.dp))
                Text("Redes y representaciones:", fontWeight = FontWeight.SemiBold)
                if (deployments.isEmpty()) {
                    Text(
                        if (asset.symbol == "USDC" || asset.symbol == "USDT") {
                            "Sin despliegue asignado: activo multi-red pendiente de resolución."
                        } else {
                            "No hay despliegues registrados para este activo."
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    deployments.forEach { deployment ->
                        Text(
                            "${deployment.networkName} · ${deployment.representationKind} · ${deployment.decimals ?: "N/D"} decimales",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Expediente de investigación", fontWeight = FontWeight.SemiBold)
                Text(
                    "Genera un resumen con las observaciones guardadas en este dispositivo. No descarga datos ni emite recomendaciones.",
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = { viewModel.generateResearchDossier(asset.assetId) }) {
                    Text("Generar expediente con datos guardados")
                }
                ResearchDossierDetails(researchDossiers[asset.assetId])

                Spacer(Modifier.height(12.dp))
                Text("Cotización de mercado · USD", fontWeight = FontWeight.SemiBold)
                MarketQuoteDetails(
                    quote = marketQuote,
                    onRefresh = { viewModel.refreshMarketQuote(asset.assetId, asset.symbol) }
                )
                Spacer(Modifier.height(8.dp))
                Text("Cotización de CoinMarketCap · USD", fontWeight = FontWeight.SemiBold)
                MarketQuoteDetails(
                    quote = coinMarketCapQuote,
                    providerName = "CoinMarketCap",
                    onRefresh = { viewModel.refreshMarketQuote(asset.assetId, asset.symbol) }
                )
                MarketQuoteComparison(
                    coinPaprika = marketQuote,
                    coinMarketCap = coinMarketCapQuote
                )

                Spacer(Modifier.height(10.dp))
                Text("Histórico diario · CoinPaprika", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Importa hasta un año de puntos diarios de precio, volumen y capitalización. No son velas OHLCV ni precios ejecutables.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    MarketHistorySyncDetails(
                        state = historySync,
                        onDownload = { viewModel.downloadDailyMarketHistory(asset.assetId, asset.symbol) }
                    )
                MarketHistoryPoints(points = dailyHistory)

                Spacer(Modifier.height(12.dp))
                Text("Procedencia de los datos", fontWeight = FontWeight.SemiBold)
                if (provenance.isEmpty()) {
                    Text(
                        "No hay observaciones de procedencia registradas.",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    provenance.forEach { observation ->
                        ProvenanceObservation(observation)
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

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
        Text("Máximo / mínimo del histórico: " + (market.periodHighUsd?.let {
            "$" + String.format(java.util.Locale.US, "%.6f", it)
        } ?: "UNKNOWN") + " / " + (market.periodLowUsd?.let {
            "$" + String.format(java.util.Locale.US, "%.6f", it)
        } ?: "UNKNOWN"))
        Text("Fundamentos: ${dossier.fundamentals} · Tokenomics: ${dossier.tokenomics}")
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
        coinPaprika.freshnessStatus != "FRESH" ||
        coinMarketCap.freshnessStatus != "FRESH"
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
