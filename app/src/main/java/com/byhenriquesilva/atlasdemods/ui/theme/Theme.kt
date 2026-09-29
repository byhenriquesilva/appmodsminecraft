package com.byhenriquesilva.atlasdemods.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * O site tem uma única identidade visual fixa (paleta "deepslate + tocha"),
 * sem alternância clara/escura — então o app reproduz isso como um único
 * tema, em vez de seguir o tema do sistema ou o Material You dinâmico.
 */
private val AtlasColorScheme = darkColorScheme(
    primary = AtlasTorch,
    onPrimary = AtlasStoneDeep,
    primaryContainer = AtlasMuted,
    onPrimaryContainer = AtlasTorch,
    secondary = AtlasMoss,
    onSecondary = AtlasStoneDeep,
    secondaryContainer = AtlasMuted,
    onSecondaryContainer = AtlasMoss,
    background = AtlasBackground,
    onBackground = AtlasForeground,
    surface = AtlasStone,
    onSurface = AtlasForeground,
    surfaceVariant = AtlasMuted,
    onSurfaceVariant = AtlasMutedForeground,
    outline = AtlasRule,
    outlineVariant = AtlasRule,
    error = AtlasTorch,
    onError = AtlasStoneDeep,
    errorContainer = AtlasMuted,
    onErrorContainer = AtlasTorch,
)

@Composable
fun AtlasDeModsTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AtlasColorScheme, typography = AtlasTypography, shapes = AtlasShapes, content = content)
}
