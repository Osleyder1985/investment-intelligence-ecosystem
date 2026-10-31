package com.iie.android.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iie.android.data.local.DemoAssetSeeder
import com.iie.android.data.local.IIEDatabaseProvider
import com.iie.android.data.market.CoinPaprikaTickerClient
import org.json.JSONObject
import com.iie.android.data.repository.RoomAssetRepository
import com.iie.android.domain.model.Asset
import com.iie.android.domain.model.AssetDeploymentSummary
import com.iie.android.domain.model.AssetObservationProvenance
import com.iie.android.domain.quality.LocalDemoEvidenceEvaluator
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

class AssetExplorerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = IIEDatabaseProvider.get(application)
    private val repository = RoomAssetRepository(database)
    private val evidenceEvaluator = LocalDemoEvidenceEvaluator.create()
    private val coinPaprikaClient = CoinPaprikaTickerClient()

    private val _marketQuotes = MutableStateFlow<Map<String, MarketQuoteUiState>>(emptyMap())
    val marketQuotes: StateFlow<Map<String, MarketQuoteUiState>> = _marketQuotes.asStateFlow()

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

    private val _evidenceEvaluation = MutableStateFlow<EvidenceEvaluationUiState?>(null)
    val evidenceEvaluation: StateFlow<EvidenceEvaluationUiState?> = _evidenceEvaluation.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { DemoAssetSeeder(database).seed() }
                _catalogStatus.value = "Catálogo local de demostración cargado"
            } catch (error: Exception) {
                _catalogStatus.value =
                    "No se pudo inicializar el catálogo local: ${error.message ?: "error desconocido"}"
            }
        }
    }

    fun observeDeployments(assetId: String): Flow<List<AssetDeploymentSummary>> =
        repository.observeDeployments(assetId)

    fun observeProvenance(assetId: String): Flow<List<AssetObservationProvenance>> =
        repository.observeProvenance(assetId)

    fun evaluateLatestEvidence(assetId: String) {
        _evidenceEvaluation.value = EvidenceEvaluationUiState(assetId = assetId, isLoading = true)
        viewModelScope.launch {
            val state = try {
                withContext(Dispatchers.IO) {
                    val record = repository.getLatestObservationForEvaluation(
                        assetId = assetId,
                        observationType = "IDENTITY_FIXTURE"
                    )
                    if (record == null) {
                        EvidenceEvaluationUiState(
                            assetId = assetId,
                            isLoading = false,
                            message = "No existe una observación evaluable con fuente registrada."
                        )
                    } else {
                        val (observation, permittedUseStatus) = record
                        EvidenceEvaluationUiState(
                            assetId = assetId,
                            isLoading = false,
                            evaluation = evidenceEvaluator.evaluate(
                                observation = observation,
                                sourcePermittedUseStatus = permittedUseStatus,
                                intendedUse = LocalDemoEvidenceEvaluator.INTENDED_USE,
                                evaluatedAtEpochMs = System.currentTimeMillis()
                            )
                        )
                    }
                }
            } catch (error: Exception) {
                EvidenceEvaluationUiState(
                    assetId = assetId,
                    isLoading = false,
                    message = "No se pudo evaluar la evidencia: ${error.message ?: "error desconocido"}"
                )
            }
            if (_evidenceEvaluation.value?.assetId == assetId) {
                _evidenceEvaluation.value = state
            }
        }
    }


    fun loadLatestMarketQuote(assetId: String) {
        viewModelScope.launch {
            try {
                val latest = withContext(Dispatchers.IO) {
                    repository.getLatestCoinPaprikaQuote(assetId)
                }
                if (latest != null) {
                    _marketQuotes.value = _marketQuotes.value + (assetId to latest.toUiState())
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
        _marketQuotes.value = _marketQuotes.value + (assetId to
            (_marketQuotes.value[assetId] ?: MarketQuoteUiState()).copy(isLoading = true, error = null))
        viewModelScope.launch {
            try {
                val observation = withContext(Dispatchers.IO) {
                    repository.fetchAndPersistCoinPaprikaQuote(assetId, symbol, coinPaprikaClient)
                }
                val attribution = withContext(Dispatchers.IO) {
                    repository.getLatestCoinPaprikaQuote(assetId)?.second ?: "CoinPaprika"
                }
                val state = observation.toUiState(attribution)
                _marketQuotes.value = _marketQuotes.value + (assetId to state)
            } catch (error: Exception) {
                val previous = _marketQuotes.value[assetId] ?: MarketQuoteUiState()
                _marketQuotes.value = _marketQuotes.value + (assetId to previous.copy(
                    isLoading = false,
                    error = error.message ?: "No se pudo actualizar la cotización"
                ))
            }
        }
    }

    private fun Pair<com.iie.android.data.local.entity.AssetObservationEntity, String>.toUiState(): MarketQuoteUiState =
        first.toUiState(second)

    private fun com.iie.android.data.local.entity.AssetObservationEntity.toUiState(
        attribution: String
    ): MarketQuoteUiState {
        val json = JSONObject(valueJson)
        return MarketQuoteUiState(
            priceUsd = json.getDouble("price"),
            providerUpdatedAtEpochMs = json.getLong("providerUpdatedAtEpochMs"),
            retrievedAtEpochMs = retrievedAtEpochMs,
            attribution = attribution,
            providerId = json.getString("providerId"),
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
    val isLoading: Boolean = false,
    val error: String? = null
)
