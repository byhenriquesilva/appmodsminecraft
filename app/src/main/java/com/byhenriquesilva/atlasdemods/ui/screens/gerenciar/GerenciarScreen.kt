package com.byhenriquesilva.atlasdemods.ui.screens.gerenciar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.byhenriquesilva.atlasdemods.data.ModsRepository
import com.byhenriquesilva.atlasdemods.data.SecretStore
import com.byhenriquesilva.atlasdemods.ui.components.ModIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GerenciarScreen(repository: ModsRepository, secretStore: SecretStore, onBack: () -> Unit) {
    val viewModel: GerenciarViewModel = viewModel(
        factory = viewModelFactory { initializer { GerenciarViewModel(repository) } },
    )
    var showConfirm by remember { mutableStateOf(false) }
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar mods") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                },
                actions = {
                    if (viewModel.selectedIds.isNotEmpty()) {
                        TextButton(onClick = { showConfirm = true }) {
                            Text("Remover (${viewModel.selectedIds.size})")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (!secretStore.hasAdminSecret) {
                Surface(color = MaterialTheme.colorScheme.errorContainer) {
                    Text(
                        "Configure a senha de admin em Ajustes antes de remover mods.",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
            viewModel.deleteError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp))
            }

            when (state) {
                is GerenciarUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                is GerenciarUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = viewModel::refresh) { Text("Tentar de novo") }
                    }
                }

                is GerenciarUiState.Loaded -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(state.mods, key = { it.id }) { mod ->
                        val checked = mod.id in viewModel.selectedIds
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleSelection(mod.id) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = checked, onCheckedChange = { viewModel.toggleSelection(mod.id) })
                            Spacer(Modifier.width(6.dp))
                            ModIcon(iconUrl = mod.iconUrl, size = 36.dp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(mod.name, style = MaterialTheme.typography.bodyLarge)
                                Text("MC ${mod.mc} · ${mod.category}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("Remover ${viewModel.selectedIds.size} mod(s)?") },
            text = { Text("Isso remove o(s) mod(s) e o(s) arquivo(s) .jar do repositório. Não dá pra desfazer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        viewModel.confirmDelete()
                    },
                    enabled = !viewModel.isDeleting,
                ) { Text("Remover") }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Cancelar") } },
        )
    }

    viewModel.lastDeletedNames?.let { names ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDeletedNotice,
            confirmButton = { TextButton(onClick = viewModel::dismissDeletedNotice) { Text("OK") } },
            title = { Text("Removido(s)") },
            text = { Text(names.joinToString("\n")) },
        )
    }
}
