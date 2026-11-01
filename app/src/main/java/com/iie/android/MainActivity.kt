package com.iie.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.iie.android.domain.model.Asset
import com.iie.android.domain.model.AssetObservationProvenance
import com.iie.android.domain.model.FreshnessEvaluation
import com.iie.android.domain.model.ObservationPayloadValidation
import com.iie.android.domain.model.ObservationQualityEvaluation
import com.iie.android.presentation.AssetExplorerViewModel
import com.iie.android.presentation.EvidenceEvaluationUiState
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
    var selectedAsset by remember { mutableStateOf<Asset?>(null) }
    var destination by remember { mutableStateOf("Resumen") }

    val visibleAssets = when (destination) {
        "Seguimiento" -> assets.filter { it.assetId in watchlist }
        else -> assets
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    listOf("Resumen", "Mercados", "Seguimiento").forEach { item ->
                        NavigationBarItem(
                            selected = destination == item,
                            onClick = { destination = item },
                            icon = {
                                Text(
                                    when (item) {
                                        "Resumen" -> "⌂"
                                        "Mercados" -> "◈"
                                        else -> "☆"
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
                                else -> "Tu seguimiento"
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
                            "ENTORNO DE DEMOSTRACIÓN",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            "El catálogo incluye datos locales de demostración. Las cotizaciones solo se muestran cuando se consultan al proveedor; no se generan señales ni recomendaciones.",
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
    val evidenceState by viewModel.evidenceEvaluation.collectAsState()
    val marketQuotes by viewModel.marketQuotes.collectAsState()
    val marketQuote = marketQuotes[asset.assetId]
    val historySyncStates by viewModel.marketHistorySync.collectAsState()
    val historySync = historySyncStates[asset.assetId]
    val dailyHistoryByAsset by viewModel.dailyMarketHistory.collectAsState()
    val dailyHistory = dailyHistoryByAsset[asset.assetId].orEmpty()

    LaunchedEffect(asset.assetId) {
        viewModel.evaluateLatestEvidence(asset.assetId)
        viewModel.loadLatestMarketQuote(asset.assetId)
        viewModel.loadDailyMarketHistory(asset.assetId)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${asset.name} (${asset.symbol})") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("ID local: ${asset.assetId}")
                Text("Clave canónica de demostración: ${asset.canonicalKey}")
                Text("Estado de identidad: ${asset.identityStatus}")
                Spacer(Modifier.height(8.dp))
                Text("Redes y representaciones:", fontWeight = FontWeight.SemiBold)
                if (deployments.isEmpty()) {
                    Text(
                        if (asset.symbol == "USDC") {
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
                Text("Cotización de mercado · USD", fontWeight = FontWeight.SemiBold)
                MarketQuoteDetails(
                    quote = marketQuote,
                    onRefresh = { viewModel.refreshMarketQuote(asset.assetId, asset.symbol) }
                )

                if (asset.symbol == "USDC" || asset.symbol == "USDT") {
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
                }

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
                Text("Evaluación de evidencia", fontWeight = FontWeight.SemiBold)
                val currentEvidenceState = evidenceState?.takeIf { it.assetId == asset.assetId }
                when {
                    currentEvidenceState == null ->
                        Text("Preparando evaluación…", style = MaterialTheme.typography.bodySmall)
                    currentEvidenceState.isLoading ->
                        Text("Evaluando observación local…", style = MaterialTheme.typography.bodySmall)
                    currentEvidenceState.message != null ->
                        Text(currentEvidenceState.message, style = MaterialTheme.typography.bodySmall)
                    currentEvidenceState.evaluation != null ->
                        EvidenceEvaluationDetails(currentEvidenceState)
                    else ->
                        Text("No hay evaluación disponible.", style = MaterialTheme.typography.bodySmall)
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
private fun MarketQuoteDetails(
    quote: MarketQuoteUiState?,
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
            Text("Proveedor: CoinPaprika · ID: ${quote.providerId}", style = MaterialTheme.typography.bodySmall)
            quote.providerUpdatedAtEpochMs?.let {
                Text("Actualizado por el proveedor: ${formatter.format(Date(it))}", style = MaterialTheme.typography.bodySmall)
            }
            quote.retrievedAtEpochMs?.let {
                Text("Recuperado y guardado localmente: ${formatter.format(Date(it))}", style = MaterialTheme.typography.bodySmall)
            }
            Text(quote.attribution ?: "Powered by CoinPaprika", style = MaterialTheme.typography.bodySmall)
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
private fun MarketHistoryPoints(points: List<CoinPaprikaHistoricalPoint>) {
    val dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM)
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
private fun EvidenceEvaluationDetails(state: EvidenceEvaluationUiState) {
    val result = state.evaluation ?: return
    when (val payload = result.payload) {
        is ObservationPayloadValidation.Valid ->
            Text("Payload: VÁLIDO · contrato ${payload.observationType} v${payload.schemaVersion}",
                style = MaterialTheme.typography.bodySmall)
        is ObservationPayloadValidation.Invalid -> {
            Text("Payload: INVÁLIDO", fontWeight = FontWeight.Medium)
            payload.issues.forEach { issue ->
                Text("${issue.code} en ${issue.path}: ${issue.detail}",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        is ObservationPayloadValidation.NotEvaluated ->
            Text("Payload: NO EVALUADO · ${payload.reason}",
                style = MaterialTheme.typography.bodySmall)
    }

    Spacer(Modifier.height(6.dp))
    when (val quality = result.quality) {
        is ObservationQualityEvaluation.Evaluated -> {
            Text("Calidad: ${quality.assessment.outcome}",
                fontWeight = FontWeight.Medium)
            Text("Política: ${quality.assessment.policyId} v${quality.assessment.policyVersion} · uso: ${quality.assessment.intendedUse}",
                style = MaterialTheme.typography.bodySmall)
            quality.assessment.findings.forEach { finding ->
                Text("${finding.severity} · ${finding.code}: ${finding.detail}",
                    style = MaterialTheme.typography.bodySmall)
            }
            if (quality.assessment.intendedUse == "LOCAL_CATALOG_DISPLAY") {
                Text("Alcance: solo visualización local de datos sintéticos; no autoriza análisis de mercado.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        is ObservationQualityEvaluation.NotEvaluated ->
            Text("Calidad: NO EVALUADA · ${quality.reason}",
                style = MaterialTheme.typography.bodySmall)
    }

    Spacer(Modifier.height(6.dp))
    when (val freshness = result.freshness) {
        is FreshnessEvaluation.Evaluated ->
            Text("Frescura: ${freshness.assessment.status} · política ${freshness.assessment.policyId} v${freshness.assessment.policyVersion}",
                style = MaterialTheme.typography.bodySmall)
        is FreshnessEvaluation.NotEvaluated ->
            Text("Frescura: NO EVALUADA · ${freshness.reason}. No se ha definido una vigencia para esta fuente y finalidad.",
                style = MaterialTheme.typography.bodySmall)
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
