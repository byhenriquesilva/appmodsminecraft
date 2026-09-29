package com.byhenriquesilva.atlasdemods.ui.screens.enviar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
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

private enum class EnviarModo { UNICO, LOTE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnviarScreen(repository: ModsRepository, secretStore: SecretStore, onBack: () -> Unit) {
    val viewModel: EnviarViewModel = viewModel(
        factory = viewModelFactory { initializer { EnviarViewModel(repository) } },
    )
    var modo by remember { mutableStateOf(EnviarModo.UNICO) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enviar mod") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            if (!secretStore.hasAdminSecret) {
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
                    Text(
                        "Configure a senha de admin em Ajustes antes de enviar um mod.",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = modo == EnviarModo.UNICO,
                    onClick = { modo = EnviarModo.UNICO },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text("Um mod") }
                SegmentedButton(
                    selected = modo == EnviarModo.LOTE,
                    onClick = { modo = EnviarModo.LOTE },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) { Text("Vários (últimas versões)") }
            }

            Spacer(Modifier.height(16.dp))

            if (modo == EnviarModo.UNICO) {
                SingleModForm(viewModel)
            } else {
                BulkModForm(viewModel)
            }

            viewModel.submitError?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(msg, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    viewModel.outcome?.let { outcome ->
        AlertDialog(
            onDismissRequest = viewModel::dismissOutcome,
            confirmButton = { TextButton(onClick = viewModel::dismissOutcome) { Text("OK") } },
            title = { Text("Envio concluído") },
            text = {
                when (outcome) {
                    is SubmitOutcome.Single -> Column {
                        Text("\"${outcome.modName}\" foi adicionado.")
                        Text(
                            "O mod aparece no catálogo depois que o deploy da Vercel terminar (~1 min).",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    is SubmitOutcome.Bulk -> Column {
                        outcome.results.forEach { r ->
                            val prefix = if (r.status == "ok") "✔" else "✘"
                            Text("$prefix ${r.mod?.name ?: r.modInput}${r.message?.let { " — $it" } ?: ""}")
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun SingleModForm(viewModel: EnviarViewModel) {
    OutlinedTextField(
        value = viewModel.modInput,
        onValueChange = viewModel::onModInputChange,
        label = { Text("Link ou slug do Modrinth") },
        placeholder = { Text("ex: sodium ou https://modrinth.com/mod/sodium") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(10.dp))
    Button(
        onClick = viewModel::fetchVersions,
        enabled = viewModel.modInput.isNotBlank() && viewModel.versionsState !is VersionsState.Loading,
    ) { Text("Buscar versões") }

    when (val vs = viewModel.versionsState) {
        is VersionsState.Loading -> {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator()
        }

        is VersionsState.Error -> {
            Spacer(Modifier.height(12.dp))
            Text(vs.message, color = MaterialTheme.colorScheme.error)
        }

        is VersionsState.Loaded -> {
            Spacer(Modifier.height(16.dp))
            Text("${vs.modName} — escolha a versão", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            vs.versions.forEach { v ->
                val selected = viewModel.selectedVersionId == v.id
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(selected = selected, onClick = { viewModel.onSelectVersion(v.id) })
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected, onClick = { viewModel.onSelectVersion(v.id) })
                    Column {
                        Text("${v.versionNumber} · MC ${v.gameVersion}")
                        Text(v.datePublished, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = viewModel::submitSingle,
                enabled = viewModel.selectedVersionId != null && !viewModel.isSubmitting,
            ) {
                if (viewModel.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Adicionar ao catálogo")
                }
            }
        }

        VersionsState.Idle -> Unit
    }
}

@Composable
private fun BulkModForm(viewModel: EnviarViewModel) {
    Text(
        "Um link ou slug do Modrinth por linha. Cada mod é adicionado na versão mais nova disponível.",
        style = MaterialTheme.typography.bodySmall,
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = viewModel.bulkInput,
        onValueChange = viewModel::onBulkInputChange,
        placeholder = { Text("sodium\nlithium\nhttps://modrinth.com/mod/iris") },
        modifier = Modifier.fillMaxWidth().height(160.dp),
    )
    Spacer(Modifier.height(10.dp))
    Button(
        onClick = viewModel::submitBulk,
        enabled = viewModel.bulkInput.isNotBlank() && !viewModel.isSubmitting,
    ) {
        if (viewModel.isSubmitting) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Text("Adicionar todos")
        }
    }
}
