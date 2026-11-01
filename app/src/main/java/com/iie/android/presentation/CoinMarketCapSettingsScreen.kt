package com.iie.android.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.iie.android.data.market.CoinMarketCapApiKeyStore

@Composable
fun CoinMarketCapSettingsScreen() {
    val context = LocalContext.current
    val keyStore = remember(context) { CoinMarketCapApiKeyStore(context) }
    var apiKey by remember { mutableStateOf("") }
    var hasStoredKey by remember { mutableStateOf(keyStore.hasKey()) }
    var status by remember { mutableStateOf<String?>(null) }

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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Plan Basic · histórico diario", style = MaterialTheme.typography.titleMedium)
                Text(
                    "La API autenticada permite consultar cotizaciones históricas diarias (precio, volumen y capitalización) dentro de la cobertura del plan. Esta integración no presupone acceso a velas OHLCV ni a histórico intradía.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Introduce tu API key Basic. Se cifra con AES-GCM y una clave protegida por Android Keystore. La credencial no se escribe en el código fuente ni en el repositorio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it; status = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("CoinMarketCap API key") },
                    placeholder = { Text("Pega aquí tu API key") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        runCatching { keyStore.save(apiKey) }
                            .onSuccess {
                                apiKey = ""
                                hasStoredKey = true
                                status = "API key guardada cifrada en este dispositivo."
                            }
                            .onFailure { status = it.message ?: "No se pudo guardar la API key." }
                    },
                    enabled = apiKey.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Guardar API key de forma segura") }

                Text(
                    if (hasStoredKey) "Estado: credencial almacenada localmente" else "Estado: falta configurar la API key",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasStoredKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (hasStoredKey) {
                    TextButton(
                        onClick = {
                            runCatching { keyStore.delete() }
                                .onSuccess {
                                    hasStoredKey = false
                                    status = "Credencial eliminada del almacenamiento local."
                                }
                                .onFailure { status = it.message ?: "No se pudo eliminar la credencial." }
                        }
                    ) { Text("Eliminar credencial guardada") }
                }
                status?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    "La protección local reduce la exposición en reposo, pero una API key utilizada por una aplicación cliente no puede considerarse secreta frente a un dispositivo comprometido o instrumentado.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Fuente: CoinMarketCap · los datos conservan proveedor, timestamp y fecha de recuperación.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}
