package com.byhenriquesilva.atlasdemods.ui.theme

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** O site não arredonda nada — tudo é reto, com bordas finas em --rule. */
val AtlasShapes = Shapes(
    extraSmall = RectangleShape,
    small = RectangleShape,
    medium = RectangleShape,
    large = RectangleShape,
    extraLarge = RectangleShape,
)

/**
 * Equivalente ao `@utility step-edge` do site: corta uma quina de 8px no
 * canto superior-esquerdo e no inferior-direito. É usado só no botão
 * principal de download da tela de detalhe, pra reproduzir esse detalhe.
 */
fun stepEdgeShape(notch: Dp = 8.dp) = GenericShape { size, density ->
    val n = with(density) { notch.toPx() }
    moveTo(0f, n)
    lineTo(n, n)
    lineTo(n, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height - n)
    lineTo(size.width - n, size.height - n)
    lineTo(size.width - n, size.height)
    lineTo(0f, size.height)
    close()
}
