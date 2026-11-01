package com.iie.android.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.iie.android.data.security.CoinMarketCapApiKeyStore
import com.iie.android.data.market.CoinMarketCapAuthenticatedClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CoinMarketCapSettingsScreen() {
    val context = LocalContext.current
    val store = remember(context) { CoinMarketCapApiKeyStore(context) }
    val scope = rememberCoroutineScope()
    var apiKey by remember { mutableStateOf("") }
    var configured by remember { mutableStateOf(store.hasKey()) }
    var busy by remember { mutableStateOf(false) }
    var testing by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PROVEEDORES DE DATOS", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary)
        Text("CoinMarketCap", style = MaterialTheme.typography.headlineSmall)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (configured) "API key guardada en este dispositivo"
                    else "No hay una API key configurada",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "La clave se cifra con AES-GCM. La clave de cifrado permanece en Android Keystore; " +
                        "la API key no se incluye en el APK ni se sincroniza con GitHub.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("API key de CoinMarketCap") },
                    placeholder = { Text("Pega aquí tu clave privada") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Button(
                    onClick = {
                        if (apiKey.isBlank() || busy) return@Button
                        busy = true
                        status = null
                        val candidate = apiKey
                        scope.launch {
                            val result = runCatching {
                                withContext(Dispatchers.IO) { store.save(candidate) }
                            }
                            busy = false
                            if (result.isSuccess) {
                                apiKey = ""
                                configured = true
                                status = "Credencial cifrada y guardada localmente."
                            } else {
                                status = "No se pudo guardar la credencial. Comprueba el almacenamiento del dispositivo."
                            }
                        }
                    },
                    enabled = apiKey.isNotBlank() && !busy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (busy) "Guardando…" else if (configured) "Actualizar API key" else "Guardar API key")
                }
                if (configured) {
                    OutlinedButton(
                        onClick = {
                            if (testing || busy) return@OutlinedButton
                            testing = true
                            status = "Comprobando conexión segura con CoinMarketCap…"
                            scope.launch {
                                val result = runCatching {
                                    withContext(Dispatchers.IO) {
                                        val credential = store.read()
                                            ?: error("No hay una API key guardada en este dispositivo.")
                                        CoinMarketCapAuthenticatedClient().fetchLatestQuote(
                                            apiKey = credential,
                                            expectedSymbol = "BTC"
                                        )
                                    }
                                }
                                testing = false
                                status = result.fold(
                                    onSuccess = { quote ->
                                        "Conexión autenticada correcta · ${quote.name} (${quote.symbol}) · USD ${quote.priceUsd} · " +
                                            "dato actualizado ${quote.providerUpdatedAtEpochMs}."
                                    },
                                    onFailure = { error ->
                                        "No se pudo validar la API key: ${error.message ?: "error de conexión"}"
                                    }
                                )
                            }
                        },
                        enabled = !busy && !testing,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (testing) "Validando…" else "Probar conexión con CoinMarketCap") }
                    OutlinedButton(
                        onClick = {
                            if (busy) return@OutlinedButton
                            busy = true
                            scope.launch {
                                val result = runCatching {
                                    withContext(Dispatchers.IO) { store.clear() }
                                }
                                busy = false
                                if (result.isSuccess) {
                                    configured = false
                                    apiKey = ""
                                    status = "API key eliminada del almacenamiento local."
                                } else {
                                    status = "No se pudo eliminar la credencial."
                                }
                            }
                        },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Eliminar API key") }
                }
                status?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "La prueba consulta BTC mediante GET /v3/cryptocurrency/quotes/latest y la cabecera " +
                "X-CMC_PRO_API_KEY. No guarda la respuesta ni muestra la clave. El acceso efectivo " +
                "depende de los permisos y límites de tu plan CoinMarketCap.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
