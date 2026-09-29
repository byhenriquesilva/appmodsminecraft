package com.byhenriquesilva.atlasdemods.ui.screens.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.byhenriquesilva.atlasdemods.data.ModsRepository
import com.byhenriquesilva.atlasdemods.ui.components.ModCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    repository: ModsRepository,
    onOpenMod: (String) -> Unit,
    onOpenEnviar: () -> Unit,
    onOpenGerenciar: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel: CatalogViewModel = viewModel(
        factory = viewModelFactory { initializer { CatalogViewModel(repository) } },
    )
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Atlas de Mods") },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Atualizar")
                    }
                    IconButton(onClick = onOpenGerenciar) {
                        Icon(Icons.Filled.PlaylistRemove, contentDescription = "Gerenciar mods")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onOpenEnviar, icon = {
                Icon(Icons.Filled.CloudUpload, contentDescription = null)
            }, text = { Text("Enviar mod") })
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = viewModel.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por nome, categoria ou tag") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
            )

            when (state) {
                is CatalogUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }

                is CatalogUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text(state.message, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = viewModel::refresh) { Text("Tentar de novo") }
                    }
                }

                is CatalogUiState.Loaded -> {
                    val mods = viewModel.visibleMods
                    if (mods.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("Nenhum mod encontrado.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(mods, key = { it.id }) { mod ->
                                ModCard(mod = mod, onClick = { onOpenMod(mod.id) })
                            }
                            item { Spacer(Modifier.height(72.dp)) } // espaço pro FAB não cobrir o último card
                        }
                    }
                }
            }
        }
    }
}
