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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.iie.android.domain.model.AssetDeploymentSummary
import com.iie.android.presentation.AssetExplorerViewModel

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

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Text(
                "Investment Intelligence Ecosystem",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text("Explorador de criptoactivos", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "MODO DEMOSTRACIÓN · Datos locales de ejemplo; no son datos de mercado ni recomendaciones.",
                    Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(catalogStatus, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar por nombre o símbolo") },
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "${assets.size} activos · ${watchlist.size} en seguimiento",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))
            if (assets.isEmpty()) {
                Text("No hay activos que coincidan con la búsqueda.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(assets, key = { it.assetId }) { asset ->
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${asset.name} (${asset.symbol})") },
        text = {
            Column {
                Text("ID local: ${asset.assetId}")
                Text("Clave canónica de demostración: ${asset.canonicalKey}")
                Text("Estado de identidad: ${asset.identityStatus}")
                Spacer(Modifier.height(8.dp))
                Text("Redes y representaciones:")
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
                Spacer(Modifier.height(8.dp))
                Text("Procedencia: fixture local IIE; no verificado como fuente de producción.")
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
