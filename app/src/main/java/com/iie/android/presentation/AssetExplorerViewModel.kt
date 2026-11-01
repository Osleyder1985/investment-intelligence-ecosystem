package com.iie.android.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iie.android.data.local.IIEDatabaseProvider
import com.iie.android.data.local.InitialAssetCatalog
import com.iie.android.data.local.AssetResearchDossierSnapshotCodec
import com.iie.android.data.local.entity.AssetResearchDossierSnapshotEntity
import com.iie.android.data.market.CoinPaprikaTickerClient
import com.iie.android.data.market.CoinMarketCapKeylessClient
import com.iie.android.data.market.CoinPaprikaHistoricalTickerClient
import com.iie.android.data.market.CoinMarketCapHistoricalQuotesClient
import com.iie.android.data.market.CoinMarketCapApiKeyStore
import com.iie.android.data.market.RoutescanTokenSupplyClient
import com.iie.android.data.market.CoinPaprikaHistoricalPoint
import com.iie.android.data.market.QuoteFreshnessEvaluator
import org.json.JSONObject
import com.iie.android.data.repository.RoomAssetRepository
import com.iie.android.data.repository.GlobalMarketOverviewRepository
import com.iie.android.data.repository.GlobalMarketDashboardData
import com.iie.android.domain.model.Asset
import com.iie.android.domain.model.AssetDeploymentSummary
import com.iie.android.domain.model.AssetObservationProvenance
import com.iie.android.domain.model.AssetResearchDossier
import com.iie.android.domain.model.ResearchQuoteInput
import com.iie.android.domain.model.ResearchMarketHistoryPoint
import com.iie.android.domain.model.ResearchQuoteComparisonInput
import com.iie.android.domain.model.ResearchEvidence
import com.iie.android.domain.model.ResearchFieldStatus
import com.iie.android.domain.model.TokenomicsResearchSection
import com.iie.android.domain.quality.AssetResearchDossierBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class AssetExplorerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = IIEDatabaseProvider.get(application)
    private val repository = RoomAssetRepository(database)
    private val globalMarketRepository = GlobalMarketOverviewRepository(database)
    private val _globalMarket = MutableStateFlow(GlobalMarketDashboardData(null, null))
    val globalMarket: StateFlow<GlobalMarketDashboardData> = _globalMarket.asStateFlow()
    private val coinPaprikaClient = CoinPaprikaTickerClient()
    private val coinMarketCapClient = CoinMarketCapKeylessClient()
    private val coinPaprikaHistoryClient = CoinPaprikaHistoricalTickerClient()
    private val coinMarketCapHistoryClient = CoinMarketCapHistoricalQuotesClient()
    private val coinMarketCapApiKeyStore = CoinMarketCapApiKeyStore(application)
    private val routescanTokenSupplyClient = RoutescanTokenSupplyClient()
    private val quoteFreshnessEvaluator = QuoteFreshnessEvaluator()
    private val dossierBuilder = AssetResearchDossierBuilder()
    private val _researchDossiers = MutableStateFlow<Map<String, AssetResearchDossier>>(emptyMap())
    val researchDossiers: StateFlow<Map<String, AssetResearchDossier>> = _researchDossiers.asStateFlow()

    private val _marketQuotes = MutableStateFlow<Map<String, MarketQuoteUiState>>(emptyMap())
    val marketQuotes: StateFlow<Map<String, MarketQuoteUiState>> = _marketQuotes.asStateFlow()

    private val _dashboardRefreshStatus = MutableStateFlow<String?>(null)
    val dashboardRefreshStatus: StateFlow<String?> = _dashboardRefreshStatus.asStateFlow()

    private val _coinMarketCapQuotes = MutableStateFlow<Map<String, MarketQuoteUiState>>(emptyMap())
    val coinMarketCapQuotes: StateFlow<Map<String, MarketQuoteUiState>> = _coinMarketCapQuotes.asStateFlow()

    private val _marketHistorySync = MutableStateFlow<Map<String, MarketHistorySyncUiState>>(emptyMap())
    val marketHistorySync: StateFlow<Map<String, MarketHistorySyncUiState>> = _marketHistorySync.asStateFlow()

    private val _dailyMarketHistory = MutableStateFlow<Map<String, List<CoinPaprikaHistoricalPoint>>>(emptyMap())
    val dailyMarketHistory: StateFlow<Map<String, List<CoinPaprikaHistoricalPoint>>> = _dailyMarketHistory.asStateFlow()

    private val _coinMarketCapDailyHistory = MutableStateFlow<Map<String, List<CoinPaprikaHistoricalPoint>>>(emptyMap())
    val coinMarketCapDailyHistory: StateFlow<Map<String, List<CoinPaprikaHistoricalPoint>>> = _coinMarketCapDailyHistory.asStateFlow()

    private val _routescanTokenSupplies = MutableStateFlow<Map<String, RoutescanTokenSupplyUiState>>(emptyMap())
    val routescanTokenSupplies: StateFlow<Map<String, RoutescanTokenSupplyUiState>> = _routescanTokenSupplies.asStateFlow()

    private val query = MutableStateFlow("")
    val searchQuery: StateFlow<String> = query.asStateFlow()

    val assets: StateFlow<List<Asset>> = query
        .flatMapLatest { search -> repository.observeAssets(search) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val watchlist: StateFlow<Set<String>> = repository.observeWatchlist()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _catalogStatus = MutableStateFlow("Preparando catálogo local…")
    val catalogStatus: StateFlow<String> = _catalogStatus.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { InitialAssetCatalog(database).synchronize() }
                _catalogStatus.value = "Catálogo inicial sincronizado · identidades y proveedores separados"
                _globalMarket.value = withContext(Dispatchers.IO) { globalMarketRepository.loadLatest() }
                withContext(Dispatchers.IO) { globalMarketRepository.refresh() }
                _globalMarket.value = withContext(Dispatchers.IO) { globalMarketRepository.loadLatest() }
            } catch (error: Exception) {
                _catalogStatus.value =
                    "No se pudo inicializar el catálogo local: ${error.message ?: "error desconocido"}"
            }
        }
    }

    /** Builds a dossier only from observations already persisted on this device. */
    fun generateResearchDossier(assetId: String) {
        viewModelScope.launch {
            try {
                val dossier = withContext(Dispatchers.IO) {
                    val asset = repository.observeAsset(assetId).first()
                        ?: error("El activo ya no existe en el catálogo local")
                    val deployments = repository.observeDeployments(assetId).first()
                    val provenance = repository.observeProvenance(assetId).first()
                    val quoteRecord = repository.getLatestCoinPaprikaQuote(assetId)
                    val quote = quoteRecord?.let { (observation, attribution) -> observation.toUiState(attribution) }
                    val cmcQuoteRecord = repository.getLatestCoinMarketCapQuote(assetId)
                    val cmcQuote = cmcQuoteRecord?.let { (observation, attribution) -> observation.toUiState(attribution) }
                    val tokenomicsEvidence = cmcQuoteRecord?.let { (observation, _) ->
                        val payload = JSONObject(observation.valueJson)
                        val circulating = payload.nullableNonNegativeFiniteDouble("circulatingSupply")
                        val total = payload.nullableNonNegativeFiniteDouble("totalSupply")
                        val maximum = payload.nullableNonNegativeFiniteDouble("maxSupply")
                        val marketCap = payload.nullableNonNegativeFiniteDouble("marketCapUsd")
                        val fullyDilutedMarketCap = payload.nullableNonNegativeFiniteDouble("fullyDilutedMarketCapUsd")
                        val relationIssues = com.iie.android.domain.model.ResearchEvidenceQualityGate
                            .validateSupplyRelations(circulating, total, maximum)
                        fun supplyEvidence(field: String, value: Double?): ResearchEvidence<Double> =
                            ResearchEvidence(
                                fieldKey = field,
                                value = value,
                                status = when {
                                    value == null -> ResearchFieldStatus.UNKNOWN
                                    relationIssues.isNotEmpty() -> ResearchFieldStatus.CONFLICTED
                                    else -> ResearchFieldStatus.KNOWN
                                },
                                sourceId = CoinMarketCapKeylessClient.SOURCE_ID,
                                sourceName = "CoinMarketCap",
                                sourceUrl = "https://coinmarketcap.com/",
                                licenseReference = CoinMarketCapKeylessClient.TERMS_URL,
                                permittedUseStatus = "PERSONAL_USE_ONLY",
                                observedAtEpochMs = observation.observedAtEpochMs,
                                retrievedAtEpochMs = observation.retrievedAtEpochMs,
                                qualityStatus = "PROVIDER_SCHEMA_VALIDATED",
                                notes = relationIssues
                            )
                        fun valuationEvidence(field: String, value: Double?): ResearchEvidence<Double> =
                            ResearchEvidence(
                                fieldKey = field, value = value,
                                status = if (value == null) ResearchFieldStatus.UNKNOWN else ResearchFieldStatus.KNOWN,
                                sourceId = CoinMarketCapKeylessClient.SOURCE_ID,
                                sourceName = "CoinMarketCap", sourceUrl = "https://coinmarketcap.com/",
                                licenseReference = CoinMarketCapKeylessClient.TERMS_URL,
                                permittedUseStatus = "PERSONAL_USE_ONLY",
                                observedAtEpochMs = observation.observedAtEpochMs,
                                retrievedAtEpochMs = observation.retrievedAtEpochMs,
                                qualityStatus = "PROVIDER_SCHEMA_VALIDATED"
                            )
                        val unknownNumber = ResearchEvidence<Double>(
                            fieldKey = "annual_inflation_percent", value = null,
                            status = ResearchFieldStatus.UNKNOWN,
                            sourceId = null, sourceName = null, sourceUrl = null,
                            licenseReference = null, permittedUseStatus = "UNKNOWN",
                            observedAtEpochMs = null, retrievedAtEpochMs = null,
                            qualityStatus = "NOT_PROVIDED_BY_QUOTE_ENDPOINT"
                        )
                        val unknownText = ResearchEvidence<String>(
                            fieldKey = "emission_schedule", value = null,
                            status = ResearchFieldStatus.UNKNOWN,
                            sourceId = null, sourceName = null, sourceUrl = null,
                            licenseReference = null, permittedUseStatus = "UNKNOWN",
                            observedAtEpochMs = null, retrievedAtEpochMs = null,
                            qualityStatus = "NOT_PROVIDED_BY_QUOTE_ENDPOINT"
                        )
                        TokenomicsResearchSection(
                            circulatingSupply = supplyEvidence("circulating_supply", circulating),
                            totalSupply = supplyEvidence("total_supply", total),
                            maxSupply = supplyEvidence("max_supply", maximum),
                            marketCapUsd = valuationEvidence("market_cap_usd", marketCap),
                            fullyDilutedMarketCapUsd = valuationEvidence("fully_diluted_market_cap_usd", fullyDilutedMarketCap),
                            annualInflationPercent = unknownNumber,
                            emissionSchedule = unknownText,
                            unlockEvents = emptyList()
                        )
                    }
                    val history = repository.getLatestCoinPaprikaDailyHistory(assetId, limit = 365)
                    dossierBuilder.build(
                        asset = asset,
                        deployments = deployments,
                        quote = quote?.let {
                            ResearchQuoteInput(
                                priceUsd = it.priceUsd,
                                provider = it.attribution,
                                providerUpdatedAtEpochMs = it.providerUpdatedAtEpochMs,
                                retrievedAtEpochMs = it.retrievedAtEpochMs,
                                freshnessStatus = it.freshnessStatus
                            )
                        },
                        history = history.map { ResearchMarketHistoryPoint(it.timestampEpochMs, it.priceUsd) },
                        provenance = provenance,
                        generatedAtEpochMs = System.currentTimeMillis(),
                        quoteComparison = ResearchQuoteComparisonInput(
                            primaryQuote = quote?.let { ResearchQuoteInput(it.priceUsd, it.attribution, it.providerUpdatedAtEpochMs, it.retrievedAtEpochMs, it.freshnessStatus) },
                            secondaryQuote = cmcQuote?.let { ResearchQuoteInput(it.priceUsd, it.attribution, it.providerUpdatedAtEpochMs, it.retrievedAtEpochMs, it.freshnessStatus) },
                            primaryObservationId = quoteRecord?.first?.observationId,
                            secondaryObservationId = cmcQuoteRecord?.first?.observationId
                        ),
                        tokenomicsEvidence = tokenomicsEvidence
                    )
                }
                withContext(Dispatchers.IO) {
                    val payload = AssetResearchDossierSnapshotCodec.encode(dossier)
                    database.researchDossierSnapshotDao().insert(
                        AssetResearchDossierSnapshotEntity(
                            snapshotId = "dossier-snapshot:${UUID.randomUUID()}",
                            assetId = assetId,
                            generatedAtEpochMs = dossier.generatedAtEpochMs,
                            schemaVersion = AssetResearchDossierSnapshotCodec.SCHEMA_VERSION,
                            evidenceStatus = dossier.overallEvidenceStatus,
                            payloadJson = payload,
                            payloadSha256 = AssetResearchDossierSnapshotCodec.sha256(payload)
                        )
                    )
                }
                _researchDossiers.value = _researchDossiers.value + (assetId to dossier)
            } catch (error: Exception) {
                _catalogStatus.value = "No se pudo generar el expediente: ${error.message ?: "error desconocido"}"
            }
        }
    }

    /** Restores the latest immutable local dossier snapshot after process recreation. */
    fun loadLatestResearchDossier(assetId: String) {
        viewModelScope.launch {
            try {
                val dossier = withContext(Dispatchers.IO) {
                    database.researchDossierSnapshotDao().getLatestForAsset(assetId)?.let {
                        AssetResearchDossierSnapshotCodec.decode(it)
                    }
                }
                if (dossier != null && dossier.assetId == assetId) {
                    _researchDossiers.value = _researchDossiers.value + (assetId to dossier)
                }
            } catch (error: Exception) {
                _catalogStatus.value = "No se pudo recuperar el expediente guardado: ${error.message ?: "error desconocido"}"
            }
        }
    }

    fun observeDeployments(assetId: String): Flow<List<AssetDeploymentSummary>> =
        repository.observeDeployments(assetId)

    fun observeProvenance(assetId: String): Flow<List<AssetObservationProvenance>> =
        repository.observeProvenance(assetId)

    fun loadLatestMarketQuote(assetId: String) {
        viewModelScope.launch {
            try {
                val latest = withContext(Dispatchers.IO) {
                    repository.getLatestCoinPaprikaQuote(assetId)
                }
                if (latest != null) {
                    _marketQuotes.value = _marketQuotes.value + (assetId to latest.toUiState())
                }
                val cmcLatest = withContext(Dispatchers.IO) {
                    repository.getLatestCoinMarketCapQuote(assetId)
                }
                if (cmcLatest != null) {
                    _coinMarketCapQuotes.value = _coinMarketCapQuotes.value + (assetId to cmcLatest.toUiState())
                }
            } catch (error: Exception) {
                _marketQuotes.value = _marketQuotes.value + (assetId to MarketQuoteUiState(
                    isLoading = false,
                    error = error.message ?: "No se pudo cargar la cotización local"
                ))
            }
        }
    }

    fun refreshMarketQuote(assetId: String, symbol: String) {
        markQuoteLoading(assetId)
        viewModelScope.launch { refreshMarketQuoteNow(assetId, symbol) }
    }

    fun refreshAllMarketQuotes(assets: List<Asset>) {
        if (_dashboardRefreshStatus.value?.startsWith("Actualizando") == true) return
        if (assets.isEmpty()) {
            _dashboardRefreshStatus.value = "No hay activos en el universo para actualizar."
            return
        }
        viewModelScope.launch {
            _dashboardRefreshStatus.value = "Actualizando métricas globales…"
            val globalResult = withContext(Dispatchers.IO) { globalMarketRepository.refresh() }
            _globalMarket.value = withContext(Dispatchers.IO) { globalMarketRepository.loadLatest() }
            val globalStatus = when {
                globalResult.globalUpdated && globalResult.sentimentUpdated -> "Mercado global y sentimiento actualizados"
                globalResult.globalUpdated -> "Mercado global actualizado · sentimiento no disponible"
                globalResult.sentimentUpdated -> "Sentimiento actualizado · mercado global no disponible"
                else -> "No se pudieron actualizar métricas globales; se conservan los últimos datos guardados"
            }
            _dashboardRefreshStatus.value = "Actualizando 0/${assets.size} activos…"
            assets.forEachIndexed { index, asset ->
                markQuoteLoading(asset.assetId)
                refreshMarketQuoteNow(asset.assetId, asset.symbol)
                val historicalCoverage = withContext(Dispatchers.IO) {
                    val paprikaCount = repository.getLatestCoinPaprikaDailyHistory(asset.assetId, limit = 2).size
                    val cmcCount = repository.getLatestCoinMarketCapDailyHistory(asset.assetId, limit = 2).size
                    paprikaCount >= 2 && cmcCount >= 2
                }
                if (!historicalCoverage) {
                    _dashboardRefreshStatus.value = "Actualizando · comprobando históricos independientes · ${asset.symbol.uppercase()}"
                    downloadDailyMarketHistoryNow(asset.assetId, asset.symbol)
                    loadDailyMarketHistory(asset.assetId)
                    loadCoinMarketCapDailyHistory(asset.assetId)
                }
                _dashboardRefreshStatus.value = "Actualizando ${index + 1}/${assets.size} · ${asset.symbol.uppercase()}"
            }
            _dashboardRefreshStatus.value = "Actualización finalizada · ${assets.size} activos revisados. · $globalStatus"
        }
    }

    private fun markQuoteLoading(assetId: String) {
        _marketQuotes.value = _marketQuotes.value + (assetId to
            (_marketQuotes.value[assetId] ?: MarketQuoteUiState()).copy(isLoading = true, error = null))
        _coinMarketCapQuotes.value = _coinMarketCapQuotes.value + (assetId to
            (_coinMarketCapQuotes.value[assetId] ?: MarketQuoteUiState()).copy(isLoading = true, error = null))
    }

    private suspend fun refreshMarketQuoteNow(assetId: String, symbol: String) {
        try {
            val observation = withContext(Dispatchers.IO) {
                repository.fetchAndPersistCoinPaprikaQuote(assetId, symbol, coinPaprikaClient)
            }
            val attribution = withContext(Dispatchers.IO) {
                repository.getLatestCoinPaprikaQuote(assetId)?.second ?: "CoinPaprika"
            }
            _marketQuotes.value = _marketQuotes.value + (assetId to observation.toUiState(attribution))
        } catch (error: Exception) {
            val previous = _marketQuotes.value[assetId] ?: MarketQuoteUiState()
            _marketQuotes.value = _marketQuotes.value + (assetId to previous.copy(
                isLoading = false,
                error = error.message ?: "No se pudo actualizar la cotización"
            ))
        }

        try {
            val observation = withContext(Dispatchers.IO) {
                repository.fetchAndPersistCoinMarketCapQuote(assetId, symbol, coinMarketCapClient)
            }
            val attribution = withContext(Dispatchers.IO) {
                repository.getLatestCoinMarketCapQuote(assetId)?.second ?: "CoinMarketCap"
            }
            _coinMarketCapQuotes.value = _coinMarketCapQuotes.value +
                (assetId to observation.toUiState(attribution))
        } catch (error: Exception) {
            val previous = _coinMarketCapQuotes.value[assetId] ?: MarketQuoteUiState()
            _coinMarketCapQuotes.value = _coinMarketCapQuotes.value + (assetId to previous.copy(
                isLoading = false,
                error = error.message ?: "No se pudo actualizar CoinMarketCap"
            ))
        }
    }

    fun loadLatestRoutescanTokenSupply(assetId: String, deploymentId: String) {
        viewModelScope.launch {
            try {
                val observation = withContext(Dispatchers.IO) {
                    repository.getLatestRoutescanTokenSupply(assetId, deploymentId)
                }
                if (observation != null) {
                    _routescanTokenSupplies.value = _routescanTokenSupplies.value +
                        (deploymentId to observation.toRoutescanTokenSupplyUiState())
                }
            } catch (error: Exception) {
                val previous = _routescanTokenSupplies.value[deploymentId] ?: RoutescanTokenSupplyUiState()
                _routescanTokenSupplies.value = _routescanTokenSupplies.value +
                    (deploymentId to previous.copy(error = error.message ?: "No se pudo leer el suministro on-chain"))
            }
        }
    }

    fun refreshRoutescanTokenSupply(assetId: String, deploymentId: String) {
        val previous = _routescanTokenSupplies.value[deploymentId] ?: RoutescanTokenSupplyUiState()
        _routescanTokenSupplies.value = _routescanTokenSupplies.value +
            (deploymentId to previous.copy(isLoading = true, error = null))
        viewModelScope.launch {
            try {
                val observation = withContext(Dispatchers.IO) {
                    repository.fetchAndPersistRoutescanTokenSupply(
                        assetId = assetId,
                        deploymentId = deploymentId,
                        client = routescanTokenSupplyClient
                    )
                }
                _routescanTokenSupplies.value = _routescanTokenSupplies.value +
                    (deploymentId to observation.toRoutescanTokenSupplyUiState())
            } catch (error: Exception) {
                val current = _routescanTokenSupplies.value[deploymentId] ?: RoutescanTokenSupplyUiState()
                _routescanTokenSupplies.value = _routescanTokenSupplies.value +
                    (deploymentId to current.copy(
                        isLoading = false,
                        error = error.message ?: "No se pudo consultar Routescan"
                    ))
            }
        }
    }

    private fun com.iie.android.data.local.entity.AssetObservationEntity.toRoutescanTokenSupplyUiState(): RoutescanTokenSupplyUiState {
        val payload = JSONObject(valueJson)
        return RoutescanTokenSupplyUiState(
            rawAmount = payload.getString("rawAmount"),
            decimalAmount = payload.getString("decimalAmount"),
            decimals = payload.getInt("decimals"),
            chainId = payload.getString("chainId"),
            contractAddress = payload.getString("contractAddress"),
            blockNumber = payload.getLong("blockNumber"),
            retrievedAtEpochMs = retrievedAtEpochMs,
            isLoading = false
        )
    }

    fun loadDailyMarketHistory(assetId: String) {
        viewModelScope.launch {
            try {
                val points = withContext(Dispatchers.IO) {
                    repository.getLatestCoinPaprikaDailyHistory(assetId, limit = 30)
                }
                _dailyMarketHistory.value = _dailyMarketHistory.value + (assetId to points)
            } catch (error: Exception) {
                _marketHistorySync.value = _marketHistorySync.value + (assetId to
                    (_marketHistorySync.value[assetId] ?: MarketHistorySyncUiState()).copy(
                        error = error.message ?: "No se pudo leer el histórico local"
                    ))
            }
        }
    }

    fun loadCoinMarketCapDailyHistory(assetId: String) {
        viewModelScope.launch {
            try {
                val points = withContext(Dispatchers.IO) {
                    repository.getLatestCoinMarketCapDailyHistory(assetId, limit = 370)
                }
                _coinMarketCapDailyHistory.value = _coinMarketCapDailyHistory.value + (assetId to points)
            } catch (error: Exception) {
                _marketHistorySync.value = _marketHistorySync.value + (assetId to
                    (_marketHistorySync.value[assetId] ?: MarketHistorySyncUiState()).copy(
                        coinMarketCapError = error.message ?: "No se pudo leer el histórico local de CoinMarketCap"
                    ))
            }
        }
    }

    fun downloadDailyMarketHistory(assetId: String, symbol: String) {
        val current = _marketHistorySync.value[assetId] ?: MarketHistorySyncUiState()
        if (current.isLoading) return
        _marketHistorySync.value = _marketHistorySync.value +
            (assetId to current.copy(isLoading = true, error = null, message = "Preparando descarga…"))
        viewModelScope.launch { downloadDailyMarketHistoryNow(assetId, symbol) }
    }

    private suspend fun downloadDailyMarketHistoryNow(assetId: String, symbol: String) {
        val current = _marketHistorySync.value[assetId] ?: MarketHistorySyncUiState()
        if (current.isLoading && current.message != "Preparando descarga…" && current.message != "Descargando desde el dashboard…") return
        _marketHistorySync.value = _marketHistorySync.value +
            (assetId to current.copy(isLoading = true, error = null, message = "Descargando desde el dashboard…"))
        try {
            val result = withContext(Dispatchers.IO) {
                val end = LocalDate.now(ZoneOffset.UTC)
                val start = end.minusYears(1)
                repository.downloadAndPersistCoinPaprikaDailyHistory(
                    assetId = assetId,
                    expectedSymbol = symbol,
                    start = start,
                    end = end,
                    fetcher = coinPaprikaHistoryClient
                )
            }
            loadDailyMarketHistory(assetId)
            _marketHistorySync.value = _marketHistorySync.value + (assetId to MarketHistorySyncUiState(
                isLoading = false,
                pointsReceived = result.pointsReceived,
                pointsInserted = result.pointsInserted,
                pointsAlreadyPresent = result.pointsAlreadyPresent,
                message = "Histórico diario importado: ${result.pointsInserted} nuevos; ${result.pointsAlreadyPresent} ya existían."
            ))
        } catch (error: Exception) {
            val previous = _marketHistorySync.value[assetId] ?: MarketHistorySyncUiState()
            _marketHistorySync.value = _marketHistorySync.value + (assetId to previous.copy(
                isLoading = false,
                error = error.message ?: "No se pudo descargar el histórico",
                message = "La importación de CoinPaprika no se completó."
            ))
        }

        // CMC is an independent source: never merge its observations into the
        // CoinPaprika series and never make a missing credential fail the other provider.
        try {
            val apiKey = withContext(Dispatchers.IO) { coinMarketCapApiKeyStore.read() }
            if (apiKey.isNullOrBlank()) {
                val previous = _marketHistorySync.value[assetId] ?: MarketHistorySyncUiState()
                _marketHistorySync.value = _marketHistorySync.value + (assetId to previous.copy(
                    coinMarketCapError = "API key de CoinMarketCap no configurada; histórico CMC omitido."
                ))
            } else {
                val cmcResult = withContext(Dispatchers.IO) {
                    val end = LocalDate.now(ZoneOffset.UTC)
                    repository.downloadAndPersistCoinMarketCapDailyHistory(
                        assetId = assetId,
                        expectedSymbol = symbol,
                        start = end.minusYears(1),
                        end = end,
                        apiKey = apiKey,
                        client = coinMarketCapHistoryClient
                    )
                }
                loadCoinMarketCapDailyHistory(assetId)
                val previous = _marketHistorySync.value[assetId] ?: MarketHistorySyncUiState()
                _marketHistorySync.value = _marketHistorySync.value + (assetId to previous.copy(
                    coinMarketCapPointsReceived = cmcResult.pointsReceived,
                    coinMarketCapPointsInserted = cmcResult.pointsInserted,
                    coinMarketCapPointsAlreadyPresent = cmcResult.pointsAlreadyPresent,
                    coinMarketCapError = null
                ))
            }
        } catch (error: Exception) {
            val previous = _marketHistorySync.value[assetId] ?: MarketHistorySyncUiState()
            _marketHistorySync.value = _marketHistorySync.value + (assetId to previous.copy(
                coinMarketCapError = error.message ?: "No se pudo importar el histórico de CoinMarketCap"
            ))
        }
    }

    private fun JSONObject.nullableNonNegativeFiniteDouble(key: String): Double? {
        if (!has(key) || isNull(key)) return null
        val value = optDouble(key, Double.NaN)
        return value.takeIf { it.isFinite() && it >= 0.0 }
    }

    private fun Pair<com.iie.android.data.local.entity.AssetObservationEntity, String>.toUiState(): MarketQuoteUiState =
        first.toUiState(second)

    private fun com.iie.android.data.local.entity.AssetObservationEntity.toUiState(
        attribution: String
    ): MarketQuoteUiState {
        val json = JSONObject(valueJson)
        fun optionalNonNegative(key: String): Double? =
            json.optDouble(key, Double.NaN).takeIf { it.isFinite() && it >= 0.0 }
        val providerUpdated = json.optLong("providerUpdatedAtEpochMs").takeIf { it > 0L }
        val freshness = quoteFreshnessEvaluator.evaluate(providerUpdated, System.currentTimeMillis())
        return MarketQuoteUiState(
            priceUsd = json.optDouble("price", Double.NaN).takeIf { it.isFinite() && it > 0.0 },
            providerUpdatedAtEpochMs = providerUpdated,
            retrievedAtEpochMs = retrievedAtEpochMs,
            attribution = attribution,
            providerId = json.optString("providerId").takeIf { it.isNotBlank() },
            marketCapUsd = optionalNonNegative("marketCapUsd"),
            fullyDilutedMarketCapUsd = optionalNonNegative("fullyDilutedMarketCapUsd"),
            circulatingSupply = optionalNonNegative("circulatingSupply"),
            totalSupply = optionalNonNegative("totalSupply"),
            maxSupply = optionalNonNegative("maxSupply"),
            freshnessStatus = freshness.status.name,
            freshnessReason = freshness.reason,
            isLoading = false
        )
    }

    fun updateSearchQuery(value: String) {
        query.value = value
    }

    fun toggleWatchlist(assetId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleWatchlist(assetId)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AssetExplorerViewModel::class.java)) {
                return AssetExplorerViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

data class MarketQuoteUiState(
    val priceUsd: Double? = null,
    val providerUpdatedAtEpochMs: Long? = null,
    val retrievedAtEpochMs: Long? = null,
    val attribution: String? = null,
    val providerId: String? = null,
    val marketCapUsd: Double? = null,
    val fullyDilutedMarketCapUsd: Double? = null,
    val circulatingSupply: Double? = null,
    val totalSupply: Double? = null,
    val maxSupply: Double? = null,
    val freshnessStatus: String = "UNKNOWN",
    val freshnessReason: String = "Frescura no evaluada",
    val isLoading: Boolean = false,
    val error: String? = null
)


data class RoutescanTokenSupplyUiState(
    val rawAmount: String? = null,
    val decimalAmount: String? = null,
    val decimals: Int? = null,
    val chainId: String? = null,
    val contractAddress: String? = null,
    val blockNumber: Long? = null,
    val retrievedAtEpochMs: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class MarketHistorySyncUiState(
    val isLoading: Boolean = false,
    val pointsReceived: Int = 0,
    val pointsInserted: Int = 0,
    val pointsAlreadyPresent: Int = 0,
    val message: String? = null,
    val error: String? = null,
    val coinMarketCapPointsReceived: Int = 0,
    val coinMarketCapPointsInserted: Int = 0,
    val coinMarketCapPointsAlreadyPresent: Int = 0,
    val coinMarketCapError: String? = null
)
