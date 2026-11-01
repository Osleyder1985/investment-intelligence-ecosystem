package com.iie.android.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.iie.android.data.market.CoinMarketCapApiKeyStore
import com.iie.android.data.market.CoinMarketCapApiKeyValidator
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

@Composable
fun CoinMarketCapSettingsScreen(onCredentialSaved: () -> Unit = {}) {
    val context = LocalContext.current
    val keyStore = remember(context) { CoinMarketCapApiKeyStore(context) }
    val validator = remember { CoinMarketCapApiKeyValidator() }
    val scope = rememberCoroutineScope()
    var apiKey by remember { mutableStateOf("") }
    var hasStoredKey by remember { mutableStateOf(keyStore.hasKey()) }
    var status by remember { mutableStateOf<String?>(null) }
    var statusIsError by remember { mutableStateOf(false) }
    var isValidating by remember { mutableStateOf(false) }
    var usageSummary by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
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
                    onValueChange = { apiKey = it; status = null; statusIsError = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("CoinMarketCap API key") },
                    placeholder = { Text("Pega aquí tu API key") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        if (!isValidating) {
                            isValidating = true
                            status = "Validando credencial con CoinMarketCap…"
                            statusIsError = false
                            usageSummary = null
                            scope.launch {
                                try {
                                    // Validate first; never persist a credential that CMC rejects.
                                    val validation = validator.validate(apiKey.trim())
                                    withContext(Dispatchers.IO) { keyStore.save(apiKey) }
                                    apiKey = ""
                                    hasStoredKey = true
                                    usageSummary = formatCoinMarketCapUsage(validation)
                                    status = "Credencial válida y guardada cifrada en este dispositivo. Iniciando actualización de históricos…"
                                    onCredentialSaved()
                                } catch (error: Exception) {
                                    status = error.message ?: "No se pudo validar o guardar la credencial."
                                    statusIsError = true
                                } finally {
                                    isValidating = false
                                }
                            }
                        }
                    },
                    enabled = apiKey.isNotBlank() && !isValidating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isValidating) "Verificando…" else "Verificar y guardar API key")
                }

                Text(
                    if (hasStoredKey) "Estado: credencial almacenada localmente" else "Estado: falta configurar la API key",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasStoredKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (hasStoredKey) {
                    TextButton(
                        onClick = {
                            if (!isValidating) {
                                isValidating = true
                                status = "Comprobando credencial almacenada…"
                                statusIsError = false
                                scope.launch {
                                    try {
                                        val storedKey = withContext(Dispatchers.IO) { keyStore.read() }
                                            ?: error("No hay una credencial almacenada.")
                                        val validation = validator.validate(storedKey)
                                        usageSummary = formatCoinMarketCapUsage(validation)
                                        status = "La credencial almacenada sigue siendo válida."
                                    } catch (error: Exception) {
                                        status = error.message ?: "No se pudo validar la credencial almacenada."
                                        statusIsError = true
                                    } finally {
                                        isValidating = false
                                    }
                                }
                            }
                        },
                        enabled = !isValidating
                    ) { Text(if (isValidating) "Verificando…" else "Comprobar credencial guardada") }
                    TextButton(
                        onClick = {
                            runCatching { keyStore.delete() }
                                .onSuccess {
                                    hasStoredKey = false
                                    status = "Credencial eliminada del almacenamiento local."
                                    statusIsError = false
                                    usageSummary = null
                                }
                                .onFailure {
                                    status = it.message ?: "No se pudo eliminar la credencial."
                                    statusIsError = true
                                }
                        },
                        enabled = !isValidating
                    ) { Text("Eliminar credencial guardada") }
                }
                status?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (statusIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    "La verificación usa /v1/key/info: CMC indica que no consume créditos de datos, aunque sí ocupa una solicitud del límite por minuto. Confirma la credencial y la cuota informada, no garantiza que cada endpoint histórico esté incluido en tu plan.",
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

private fun formatCoinMarketCapUsage(validation: com.iie.android.data.market.CoinMarketCapKeyValidation): String {
    val numberFormat = java.text.NumberFormat.getIntegerInstance(java.util.Locale.US)
    val monthly = validation.monthlyCreditsLeft?.let(numberFormat::format)
    val monthlyLimit = validation.monthlyCreditLimit?.let(numberFormat::format)
    val daily = validation.dailyCreditsLeft?.let(numberFormat::format)
    val requests = validation.minuteRequestsLeft?.let(numberFormat::format)
    val rate = validation.minuteRateLimit?.let(numberFormat::format)
    return buildList {
        if (monthly != null && monthlyLimit != null) add("Créditos mensuales: $monthly / $monthlyLimit")
        if (daily != null) add("Créditos diarios restantes: $daily")
        if (requests != null && rate != null) add("Solicitudes restantes este minuto: $requests / $rate")
    }.takeIf { it.isNotEmpty() }?.joinToString(" · ")
        ?: "Credencial aceptada. CMC no devolvió el detalle de cuotas."
}
