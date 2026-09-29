package com.byhenriquesilva.atlasdemods.ui.screens.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.byhenriquesilva.atlasdemods.data.ModsRepository
import com.byhenriquesilva.atlasdemods.network.model.Mod
import com.byhenriquesilva.atlasdemods.ui.components.GalleryRow
import com.byhenriquesilva.atlasdemods.ui.components.HtmlText
import com.byhenriquesilva.atlasdemods.ui.components.ModIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModDetailScreen(repository: ModsRepository, modId: String, baseUrl: String, onBack: () -> Unit) {
    val viewModel: ModDetailViewModel = viewModel(
        factory = viewModelFactory { initializer { ModDetailViewModel(repository, modId) } },
    )
    val context = LocalContext.current
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do mod") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                },
            )
        },
    ) { padding ->
        when (state) {
            is ModDetailUiState.Loading -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }

            is ModDetailUiState.Error -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(state.message)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::load) { Text("Tentar de novo") }
                }
            }

            is ModDetailUiState.Loaded -> {
                val mod = state.mod
                Column(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        ModIcon(iconUrl = mod.iconUrl, size = 64.dp)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(mod.name, style = MaterialTheme.typography.headlineSmall)
                            Text("por ${mod.author}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    InfoRow("Versão", mod.version)
                    InfoRow("Minecraft", mod.mc)
                    InfoRow("Loader", mod.loader)
                    InfoRow("Categoria", mod.category)
                    InfoRow("Tamanho", mod.size)
                    if (mod.dependencies.isNotEmpty()) {
                        InfoRow("Dependências", mod.dependencies.joinToString(", "))
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = {
                            val url = mod.resolvedDownloadUrl(baseUrl)
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }) {
                            Icon(Icons.Filled.Download, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Baixar .jar")
                        }
                        if (!mod.modrinth.isNullOrBlank()) {
                            OutlinedButton(onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mod.modrinth)))
                            }) {
                                Icon(Icons.Filled.OpenInBrowser, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Modrinth")
                            }
                        }
                    }

                    if (mod.gallery.isNotEmpty()) {
                        Spacer(Modifier.height(20.dp))
                        Text("Galeria", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        GalleryRow(images = mod.gallery.map { it.url })
                    }

                    if (!mod.description.isNullOrBlank()) {
                        Spacer(Modifier.height(20.dp))
                        Text("Descrição", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        HtmlText(html = mod.description, modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(120.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
