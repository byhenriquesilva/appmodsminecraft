package com.byhenriquesilva.atlasdemods.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.byhenriquesilva.atlasdemods.R

/**
 * As três famílias do site (src/styles.css): Bricolage Grotesque (títulos),
 * IBM Plex Mono (texto técnico/corpo — é a fonte padrão do body no site) e
 * Silkscreen (etiquetas em pixel, tipo o "ATLAS" do cabeçalho).
 */

// Fonte variável — a instância de peso é escolhida via variationSettings (suportado
// desde a API 26, que já é o minSdk do app).
val DisplayFontFamily = FontFamily(
    Font(
        R.font.bricolage_grotesque,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(500), FontVariation.width(100)),
    ),
    Font(
        R.font.bricolage_grotesque,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700), FontVariation.width(100)),
    ),
)

val MonoFontFamily = FontFamily(
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_mono_semibold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_mono_bold, FontWeight.Bold),
)

val PixelFontFamily = FontFamily(
    Font(R.font.silkscreen_regular, FontWeight.Normal),
    Font(R.font.silkscreen_bold, FontWeight.Bold),
)

/** Equivalente à utility `.label-tech` do site: mono, caixa alta, tracking largo. */
val LabelTechStyle = TextStyle(
    fontFamily = MonoFontFamily,
    fontSize = 11.sp,
    letterSpacing = 1.5.sp,
)

val AtlasTypography = Typography(
    displayLarge = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold, fontSize = 46.sp, lineHeight = 46.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 36.sp, letterSpacing = (-0.4).sp),
    displaySmall = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 30.sp),
    headlineLarge = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 30.sp),
    headlineMedium = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 26.sp),
    headlineSmall = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 24.sp, letterSpacing = (-0.2).sp),
    titleLarge = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 22.sp),
    titleMedium = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 20.sp, letterSpacing = (-0.1).sp),
    titleSmall = TextStyle(fontFamily = MonoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 16.sp),
    bodyLarge = TextStyle(fontFamily = MonoFontFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = MonoFontFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall = TextStyle(fontFamily = MonoFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = MonoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 1.sp),
    labelMedium = LabelTechStyle,
    labelSmall = TextStyle(fontFamily = MonoFontFamily, fontSize = 10.sp, letterSpacing = 1.2.sp),
)

/** Estilo de link do site (`.link-rule`): mono, sublinhado, sem negrito. */
val LinkStyle = TextStyle(
    fontFamily = MonoFontFamily,
    fontSize = 12.sp,
    letterSpacing = 1.sp,
    textDecoration = TextDecoration.Underline,
)
