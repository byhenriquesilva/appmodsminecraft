package com.byhenriquesilva.atlasdemods.ui.components

import android.text.Html
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Renderiza a descrição do mod, que chega do servidor como HTML já
 * sanitizado (`marked` + `sanitize-html`, ver `modrinth.server.ts`).
 *
 * `Html.fromHtml` cobre parágrafos, negrito/itálico, links, títulos e listas
 * bem — mas ignora `<table>` e não baixa `<img>`. Pra descrição completa com
 * imagens e tabelas, a alternativa é uma WebView isolada carregando só esse
 * HTML via `loadDataWithBaseURL` (não é a mesma coisa que encapsular o site:
 * é um componente de renderização de um texto que o app já buscou). Deixei
 * essa troca fora do esqueleto de propósito — comece com isto e evolua se
 * sentir falta das tabelas/imagens inline.
 */
@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    val linkColor = MaterialTheme.colorScheme.primary.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                setTextColor(textColor)
                setLinkTextColor(linkColor)
                textSize = 14f
                setLineSpacing(4f, 1.1f)
            }
        },
        update = { view ->
            view.text = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
            view.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        },
    )
}
