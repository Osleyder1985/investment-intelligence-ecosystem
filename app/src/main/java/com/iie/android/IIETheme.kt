package com.iie.android

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Central visual language for IIE.
 *
 * Keep semantic colors here so screens communicate market state consistently
 * instead of introducing local, unrelated palettes.
 */
object IIEDesign {
    val background = Color(0xFF080D12)
    val surface = Color(0xFF111922)
    val surfaceElevated = Color(0xFF17232E)
    val surfaceStrong = Color(0xFF1D2A36)
    val border = Color(0xFF30404D)
    val textPrimary = Color(0xFFE8EEF4)
    val textSecondary = Color(0xFF9EACB9)
    val accent = Color(0xFF63E6B2)
    val accentSecondary = Color(0xFF8DA9FF)
    val positive = Color(0xFF63D9A6)
    val negative = Color(0xFFFF7777)
    val warning = Color(0xFFFFC46B)
    val neutral = Color(0xFF9EACB9)

    object Spacing {
        val xxs = 4.dp
        val xs = 8.dp
        val sm = 12.dp
        val md = 16.dp
        val lg = 20.dp
        val xl = 24.dp
        val xxl = 32.dp
    }

    object Radius {
        val small = 10.dp
        val medium = 16.dp
        val large = 22.dp
        val pill = 50.dp
    }
}

private val IIEColorScheme = darkColorScheme(
    primary = IIEDesign.accent,
    onPrimary = Color(0xFF06251B),
    secondary = IIEDesign.accentSecondary,
    background = IIEDesign.background,
    surface = IIEDesign.surface,
    surfaceVariant = IIEDesign.surfaceElevated,
    onSurface = IIEDesign.textPrimary,
    onSurfaceVariant = IIEDesign.textSecondary,
    outline = IIEDesign.border,
    error = IIEDesign.negative
)

@Composable
fun IIETheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IIEColorScheme,
        shapes = Shapes(
            small = RoundedCornerShape(IIEDesign.Radius.small),
            medium = RoundedCornerShape(IIEDesign.Radius.medium),
            large = RoundedCornerShape(IIEDesign.Radius.large)
        ),
        content = content
    )
}
