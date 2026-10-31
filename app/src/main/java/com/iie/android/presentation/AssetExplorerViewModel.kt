package com.iie.android.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iie.android.data.local.DemoAssetSeeder
import com.iie.android.data.local.IIEDatabaseProvider
import com.iie.android.data.repository.RoomAssetRepository
import com.iie.android.domain.model.Asset
import com.iie.android.domain.model.AssetDeploymentSummary
import kotlinx.coroutines.Dispatchers
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

    private val query = MutableStateFlow("")
    val searchQuery: StateFlow<String> = query.asStateFlow()

    val assets: StateFlow<List<Asset>> = query
        .flatMapLatest(repository::observeAssets)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val watchlist: StateFlow<Set<String>> = repository.observeWatchlist()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _catalogStatus = MutableStateFlow("Preparando catálogo local…")
    val catalogStatus: StateFlow<String> = _catalogStatus.asStateFlow()

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

    fun observeDeployments(assetId: String): kotlinx.coroutines.flow.Flow<List<AssetDeploymentSummary>> =
        repository.observeDeployments(assetId)

    fun updateSearchQuery(value: String) {
        query.value = value
    }

    fun toggleWatchlist(assetId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (assetId in watchlist.value) {
                repository.removeFromWatchlist(assetId)
            } else {
                repository.addToWatchlist(assetId)
            }
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
