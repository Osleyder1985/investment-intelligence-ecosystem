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
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
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
            MaterialTheme(colorScheme = darkColorScheme(
                primary = androidx.compose.ui.graphics.Color(0xFF79D6C3),
                background = androidx.compose.ui.graphics.Color(0xFF0B1117),
                surface = androidx.compose.ui.graphics.Color(0xFF141E27),
                surfaceVariant = androidx.compose.ui.graphics.Color(0xFF1D2A35),
                secondaryContainer = androidx.compose.ui.graphics.Color(0xFF203C3A)
            )) { AssetExplorerScreen(explorerViewModel) }
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
    var selectedAsset by remember { mutableStateOf<Asset?>(null) }

    LaunchedEffect(assets) {
        assets.take(6).forEach { viewModel.loadLatestMarketQuote(it.assetId) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, top = 22.dp, end = 20.dp, bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "IIE / INTELLIGENCE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            "Market overview",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Centro de inteligencia de activos digitales",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            "LOCAL",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.fillMaxWidth().padding(18.dp)) {
                        Text(
                            "ESTADO DEL SISTEMA",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Datos bajo control. Decisiones pendientes de evidencia.",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "El catálogo y la watchlist se almacenan localmente. Las cotizaciones solo aparecen cuando se recuperan del proveedor y superan las validaciones disponibles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            catalogStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        value = assets.size.toString(),
                        label = "Activos visibles"
                    )
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        value = watchlist.size.toString(),
                        label = "En seguimiento"
                    )
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        value = marketQuotes.values.count { it.priceUsd != null }.toString(),
                        label = "Con cotización"
                    )
                }
            }
            item {
                Text(
                    "WATCHLIST & MERCADO",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            items(assets.filter { it.assetId in watchlist }.ifEmpty { assets.take(3) },
                key = { "market-${it.assetId}" }) { asset ->
                val quote = marketQuotes[asset.assetId]
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { selectedAsset = asset },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(asset.symbol, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium)
                            Text(asset.name, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                quote?.priceUsd?.let {
                                    "$" + String.format(java.util.Locale.US, "%.6f", it)
                                } ?: "Cotización no disponible",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                quote?.let { "CoinPaprika · ${it.freshnessStatus}" }
                                    ?: "Abre el activo para consultar el mercado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = { viewModel.refreshMarketQuote(asset.assetId, asset.symbol) },
                            enabled = quote?.isLoading != true
                        ) {
                            Text(if (quote?.isLoading == true) "Cargando" else "Actualizar")
                        }
                    }
                }
            }
            item {
                Text(
                    "UNIVERSO DE ACTIVOS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::updateSearchQuery,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    label = { Text("Buscar activo por nombre o símbolo") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    "Selecciona un activo para consultar identidad, redes, cotización, histórico y procedencia.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (assets.isEmpty()) {
                item {
                    Text(
                        "No hay activos que coincidan con la búsqueda.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(assets, key = { it.assetId }) { asset ->
                    AssetRow(
                        asset = asset,
                        isWatched = asset.assetId in watchlist,
                        onClick = { selectedAsset = asset },
                        onToggleWatchlist = { viewModel.toggleWatchlist(asset.assetId) }
                    )
                }
            }
            item {
                Text(
                    "TRANSPARENCIA · No se muestran puntuaciones, señales ni rentabilidades simuladas. El motor de análisis y decisión aún no está implementado.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
private fun MetricTile(modifier: Modifier = Modifier, value: String, label: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AssetRow(
    asset: Asset,
    isWatched: Boolean,
    onClick: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                Modifier.weight(1f).clickable(onClick = onClick).padding(vertical = 2.dp)
            ) {
                Text(asset.name, fontWeight = FontWeight.SemiBold)
                Text(asset.identityStatus, style = MaterialTheme.typography.bodySmall)
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
