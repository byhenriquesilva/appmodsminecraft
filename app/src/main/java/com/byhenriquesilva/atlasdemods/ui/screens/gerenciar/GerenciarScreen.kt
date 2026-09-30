package com.byhenriquesilva.atlasdemods.ui.screens.gerenciar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.byhenriquesilva.atlasdemods.network.model.Mod
import com.byhenriquesilva.atlasdemods.ui.components.FilterPill
import com.byhenriquesilva.atlasdemods.ui.components.GhostButton
import com.byhenriquesilva.atlasdemods.ui.components.ModIcon
import com.byhenriquesilva.atlasdemods.ui.components.PrimaryStepButton
import com.byhenriquesilva.atlasdemods.ui.components.RuleBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GerenciarScreen(repository: ModsRepository, secretStore: SecretStore, onBack: () -> Unit) {
    val viewModel: GerenciarViewModel = viewModel(
        factory = viewModelFactory { initializer { GerenciarViewModel(repository) } },
    )
    val state = viewModel.uiState
    var showBulkConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("gerenciar", style = MaterialTheme.typography.labelMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Row {
                    Text("Gerenciar mods", style = MaterialTheme.typography.headlineMedium)
                    Text(".", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                }

                if (!secretStore.hasAdminSecret) {
                    Spacer(Modifier.height(12.dp))
                    RuleBox {
                        Text(
                            "Configure a senha de admin em Ajustes antes de remover mods.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                viewModel.deleteError?.let {
                    Spacer(Modifier.height(12.dp))
                    RuleBox { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
                }
            }

            if (state is GerenciarUiState.Loaded && viewModel.availableVersions.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        FilterPill(
                            text = "todas as versões",
                            active = viewModel.selectedVersion == null,
                            onClick = { viewModel.onSelectVersion(null) },
                        )
                    }
                    items(viewModel.availableVersions) { v ->
                        FilterPill(text = v, active = viewModel.selectedVersion == v, onClick = { viewModel.onSelectVersion(v) })
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val visibleIds = viewModel.visibleMods.map { it.id }.toSet()
                    val allSelected = visibleIds.isNotEmpty() && visibleIds.all { it in viewModel.selectedIds }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.toggleAllVisible() },
                    ) {
                        Checkbox(checked = allSelected, onCheckedChange = { viewModel.toggleAllVisible() })
                        Text("selecionar todos (${visibleIds.size})", style = MaterialTheme.typography.labelMedium)
                    }
                    PrimaryStepButton(
                        text = "excluir (${viewModel.selectedIds.size})",
                        onClick = { showBulkConfirm = true },
                        enabled = viewModel.selectedIds.isNotEmpty() && !viewModel.isDeleting,
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            when (state) {
                is GerenciarUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                is GerenciarUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        GhostButton(text = "tentar de novo", onClick = viewModel::refresh)
                    }
                }

                is GerenciarUiState.Loaded -> {
                    val mods = viewModel.visibleMods
                    if (mods.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhum mod nessa versão.", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp)) {
                            items(mods, key = { it.id }) { mod ->
                                GerenciarRow(
                                    mod = mod,
                                    checked = mod.id in viewModel.selectedIds,
                                    onToggle = { viewModel.toggleSelection(mod.id) },
                                    onDelete = { viewModel.requestDeleteSingle(mod) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBulkConfirm) {
        AlertDialog(
            onDismissRequest = { showBulkConfirm = false },
            title = { Text("Remover ${viewModel.selectedIds.size} mod(s)?") },
            text = { Text("Isso remove o(s) mod(s) e o(s) arquivo(s) .jar do repositório. Não dá pra desfazer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBulkConfirm = false
                        viewModel.confirmDeleteSelected()
                    },
                    enabled = !viewModel.isDeleting,
                ) { Text("Remover") }
            },
            dismissButton = { TextButton(onClick = { showBulkConfirm = false }) { Text("Cancelar") } },
        )
    }
    ConfirmSingleDeleteDialog(viewModel)

    viewModel.lastDeletedNames?.let { names ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDeletedNotice,
            confirmButton = { TextButton(onClick = viewModel::dismissDeletedNotice) { Text("OK") } },
            title = { Text("Removido(s)") },
            text = { Text(names.joinToString("\n")) },
        )
    }
}

@Composable
private fun GerenciarRow(mod: Mod, checked: Boolean, onToggle: () -> Unit, onDelete: () -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = checked, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(4.dp))
            ModIcon(iconUrl = mod.iconUrl, fallbackText = mod.name.take(2).uppercase(), size = 36.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(mod.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                Text(
                    "${mod.mc} · ${mod.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            GhostButton(text = "excluir ✕", onClick = onDelete)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    }
}

@Composable
private fun ConfirmSingleDeleteDialog(viewModel: GerenciarViewModel) {
    val mod = viewModel.pendingSingleDelete ?: return
    AlertDialog(
        onDismissRequest = viewModel::dismissSingleDelete,
        title = { Text("Remover \"${mod.name}\"?") },
        text = { Text("Isso remove o mod e o arquivo .jar do repositório. Não dá pra desfazer.") },
        confirmButton = {
            TextButton(onClick = viewModel::confirmDeleteSingle, enabled = !viewModel.isDeleting) { Text("Remover") }
        },
        dismissButton = { TextButton(onClick = viewModel::dismissSingleDelete) { Text("Cancelar") } },
    )
}
