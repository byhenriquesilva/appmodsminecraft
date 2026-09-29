package com.byhenriquesilva.atlasdemods.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.byhenriquesilva.atlasdemods.network.model.Mod
import com.byhenriquesilva.atlasdemods.ui.theme.MonoFontFamily
import com.byhenriquesilva.atlasdemods.ui.theme.PixelFontFamily

/**
 * Uma linha da lista — mesma composição do `ModRow` do site: ícone quadrado
 * (com iniciais em fonte pixel quando não há ícone), nome em fonte de
 * título, resumo, tamanho, e um botão "baixar" separado que não abre o
 * detalhe (clicar no resto da linha abre; o botão baixa direto).
 */
@Composable
fun ModCard(mod: Mod, onClick: () -> Unit, onDownloadClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ModIcon(iconUrl = mod.iconUrl, fallbackText = mod.name.take(2).uppercase(), size = 52.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    mod.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    mod.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                mod.size,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(10.dp))
            OutlinedButton(
                onClick = onDownloadClick,
                shape = RectangleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = OutlinedButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text("baixar ↓", style = MaterialTheme.typography.labelMedium)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    }
}

@Composable
fun ModIcon(iconUrl: String?, size: Dp, fallbackText: String = "?") {
    if (iconUrl.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .size(size)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                fallbackText,
                fontFamily = PixelFontFamily,
                fontSize = (size.value * 0.24f).sp,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    } else {
        AsyncImage(
            model = iconUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(RectangleShape)
                .background(MaterialTheme.colorScheme.surface),
        )
    }
}

@Composable
fun GalleryRow(images: List<String>, modifier: Modifier = Modifier) {
    if (images.isEmpty()) return
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(images) { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 220.dp, height = 130.dp)
                    .clip(RectangleShape)
                    .background(Color.Black.copy(alpha = 0.05f)),
            )
        }
    }
}
