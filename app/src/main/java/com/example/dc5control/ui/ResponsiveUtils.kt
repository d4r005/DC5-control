package com.example.dc5control.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Clase helper para detectar el tipo de pantalla y orientación.
 * Permite adaptar layouts para teléfono, tablet, landscape y portrait.
 */
data class ScreenType(
    val isCompact: Boolean,    // Teléfono portrait (< 600dp)
    val isMedium: Boolean,     // Teléfono landscape o foldable (600-840dp)
    val isExpanded: Boolean,   // Tablet (>= 840dp)
    val isLandscape: Boolean,  // Orientación horizontal
    val isPortrait: Boolean,   // Orientación vertical
    val widthDp: Int,
    val heightDp: Int
) {
    /** Ancho máximo de contenido para evitar estiramiento en pantallas grandes */
    val contentMaxWidth: Dp
        get() = when {
            isExpanded -> 900.dp
            isMedium -> 720.dp
            else -> 0.dp  // sin límite en compact
        }

    /** Padding adaptable según el tamaño de pantalla */
    val screenPadding: Dp
        get() = when {
            isExpanded -> 32.dp
            isMedium -> 24.dp
            else -> 16.dp
        }

    /** Número de columnas para grids adaptativos */
    val gridColumns: Int
        get() = when {
            isExpanded -> 3
            isMedium -> 2
            else -> 1
        }

    /** Si hay espacio suficiente para layout de dos columnas (form+preview) */
    val useTwoColumns: Boolean
        get() = isMedium || isExpanded || (isLandscape && widthDp >= 600)

    /** Si hay espacio para layout de tabla en listas */
    val useTableLayout: Boolean
        get() = isExpanded || (isLandscape && widthDp >= 700)
}

@Composable
fun rememberScreenType(): ScreenType {
    val config = LocalConfiguration.current
    val widthDp = config.screenWidthDp
    val heightDp = config.screenHeightDp
    val isLandscape = widthDp > heightDp

    return ScreenType(
        isCompact = widthDp < 600,
        isMedium = widthDp >= 600 && widthDp < 840,
        isExpanded = widthDp >= 840,
        isLandscape = isLandscape,
        isPortrait = !isLandscape,
        widthDp = widthDp,
        heightDp = heightDp
    )
}
