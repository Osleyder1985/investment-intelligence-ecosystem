package com.iie.android.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CoinMarketCapSettingsScreen() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "PROVEEDORES DE DATOS",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text("CoinMarketCap", style = MaterialTheme.typography.headlineSmall)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Conexión pública sin API key", style = MaterialTheme.typography.titleMedium)
                Text(
                    "IIE consulta el endpoint público de listados de CoinMarketCap. No necesitas " +
                        "introducir, guardar ni incluir credenciales en el APK.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Datos disponibles en esta integración: precio USD, suministro circulante, " +
                        "suministro total y máximo cuando CoinMarketCap los informa. Capitalización " +
                        "de mercado, FDV, emisiones y desbloqueos permanecen como UNKNOWN si esta " +
                        "ruta pública no los proporciona.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Fuente: CoinMarketCap · los datos conservan su fecha de actualización, " +
                        "fecha de recuperación y procedencia.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "El endpoint público puede tener cobertura y límites distintos de la API Pro. " +
                        "La aplicación no presupone acceso ilimitado ni equivalencia con endpoints autenticados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}
