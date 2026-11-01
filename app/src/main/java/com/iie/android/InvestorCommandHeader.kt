package com.iie.android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Dashboard command header: communicates scope, user coverage and data state
 * without implying an investment recommendation or a validated signal.
 */
@Composable
fun InvestorCommandHeader(
    assetCount: Int,
    watchedCount: Int,
    catalogStatus: String,
    refreshStatus: String?,
    onExplore: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(IIEDesign.surface, RoundedCornerShape(IIEDesign.Radius.large))
            .padding(IIEDesign.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(IIEDesign.Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "INVESTMENT INTELLIGENCE",
                    color = IIEDesign.accent,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "CENTRO DE ANÁLISIS",
                    color = IIEDesign.textSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Surface(
                color = IIEDesign.surfaceElevated,
                shape = RoundedCornerShape(IIEDesign.Radius.pill)
            ) {
                Text(
                    "DATOS · EVIDENCIA",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    color = IIEDesign.accent,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            "Entiende el mercado.\nInvestiga cada activo.",
            color = IIEDesign.textPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Señales observadas, cobertura medible y procedencia visible. La aplicación no sustituye tu análisis ni convierte datos incompletos en recomendaciones.",
            color = IIEDesign.textSecondary,
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(IIEDesign.Spacing.sm)
        ) {
            DashboardKpi(
                modifier = Modifier.weight(1f),
                value = assetCount.toString(),
                label = "UNIVERSO"
            )
            DashboardKpi(
                modifier = Modifier.weight(1f),
                value = watchedCount.toString(),
                label = "SEGUIMIENTO"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(IIEDesign.Spacing.sm)
        ) {
            Button(
                onClick = onExplore,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IIEDesign.accent,
                    contentColor = IIEDesign.background
                ),
                shape = RoundedCornerShape(IIEDesign.Radius.medium)
            ) {
                Text("Explorar activos", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onRefresh,
                enabled = refreshStatus?.startsWith("Actualizando") != true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(IIEDesign.Radius.medium)
            ) {
                Text(if (refreshStatus?.startsWith("Actualizando") == true) "Actualizando…" else "Actualizar")
            }
        }

        Spacer(Modifier.height(2.dp))
        Text(
            refreshStatus ?: catalogStatus,
            color = IIEDesign.textSecondary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun DashboardKpi(
    modifier: Modifier,
    value: String,
    label: String
) {
    Column(
        modifier = modifier
            .background(IIEDesign.surfaceElevated, RoundedCornerShape(IIEDesign.Radius.medium))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            value,
            color = IIEDesign.textPrimary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            color = IIEDesign.textSecondary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
