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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class Asset(
    val id: String,
    val name: String,
    val symbol: String,
    val network: String,
    val description: String
)

private val demoAssets = listOf(
    Asset("bitcoin", "Bitcoin", "BTC", "Bitcoin", "Activo nativo de la red Bitcoin."),
    Asset("ethereum", "Ethereum", "ETH", "Ethereum", "Activo nativo de Ethereum."),
    Asset("solana", "Solana", "SOL", "Solana", "Activo nativo de la red Solana."),
    Asset("usd-coin", "USD Coin", "USDC", "Multi-red", "Stablecoin; sus representaciones dependen de la red.")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AssetExplorerScreen()
            }
        }
    }
}

@Composable
private fun AssetExplorerScreen() {
    var query by remember { mutableStateOf("") }
    var selectedAsset by remember { mutableStateOf<Asset?>(null) }
    val filteredAssets = demoAssets.filter {
        it.name.contains(query, ignoreCase = true) ||
            it.symbol.contains(query, ignoreCase = true)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Investment Intelligence Ecosystem",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text("Explorador de criptoactivos", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "MODO DEMOSTRACIÓN · Datos estáticos de ejemplo; no son precios ni datos de mercado en tiempo real.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar por nombre o símbolo") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${filteredAssets.size} activos de demostración",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredAssets, key = { it.id }) { asset ->
                    AssetRow(asset = asset, onClick = { selectedAsset = asset })
                }
            }
        }
    }

    selectedAsset?.let { asset ->
        AlertDialog(
            onDismissRequest = { selectedAsset = null },
            title = { Text("${asset.name} (${asset.symbol})") },
            text = {
                Column {
                    Text("Identificador: ${asset.id}")
                    Text("Red: ${asset.network}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(asset.description)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Estado de datos: DEMO — sin fuente externa conectada.")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAsset = null }) { Text("Cerrar") }
            }
        )
    }
}

@Composable
private fun AssetRow(asset: Asset, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(asset.name, fontWeight = FontWeight.SemiBold)
                Text(asset.network, style = MaterialTheme.typography.bodySmall)
            }
            Text(asset.symbol, fontWeight = FontWeight.Bold)
        }
    }
}
