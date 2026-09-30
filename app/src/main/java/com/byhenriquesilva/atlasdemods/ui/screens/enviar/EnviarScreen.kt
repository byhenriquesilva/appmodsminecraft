package com.byhenriquesilva.atlasdemods.ui.screens.enviar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.byhenriquesilva.atlasdemods.data.ModsRepository
import com.byhenriquesilva.atlasdemods.data.SecretStore
import com.byhenriquesilva.atlasdemods.ui.components.FilterPill
import com.byhenriquesilva.atlasdemods.ui.components.GhostButton
import com.byhenriquesilva.atlasdemods.ui.components.PrimaryStepButton
import com.byhenriquesilva.atlasdemods.ui.components.RuleBox
import com.byhenriquesilva.atlasdemods.ui.components.UnderlineField

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
                title = { Text("enviar", style = MaterialTheme.typography.labelMedium) },
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text("novo mod", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Row {
                Text("Enviar mod", style = MaterialTheme.typography.headlineLarge)
                Text(".", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Cole a URL (ou o slug) do mod no Modrinth. Com um link só, você escolhe a versão de Minecraft. Com vários, cada um usa a versão mais nova disponível.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            if (!secretStore.hasAdminSecret) {
                RuleBox {
                    Text(
                        "Configure a senha de admin em Ajustes antes de enviar um mod.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(20.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterPill(text = "um mod", active = modo == EnviarModo.UNICO, onClick = { modo = EnviarModo.UNICO })
                FilterPill(text = "vários (últimas versões)", active = modo == EnviarModo.LOTE, onClick = { modo = EnviarModo.LOTE })
            }

            Spacer(Modifier.height(20.dp))

            if (modo == EnviarModo.UNICO) {
                SingleModForm(viewModel)
            } else {
                BulkModForm(viewModel)
            }

            viewModel.submitError?.let { msg ->
                Spacer(Modifier.height(16.dp))
                RuleBox { Text(msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
            }

            viewModel.outcome?.let { outcome ->
                Spacer(Modifier.height(20.dp))
                OutcomePanel(outcome)
            }
        }
    }
}

@Composable
private fun SingleModForm(viewModel: EnviarViewModel) {
    UnderlineField(
        value = viewModel.modInput,
        onValueChange = viewModel::onModInputChange,
        label = "mod do modrinth",
        placeholder = "https://modrinth.com/mod/sodium ou sodium",
    )
    Spacer(Modifier.height(16.dp))
    PrimaryStepButton(
        text = if (viewModel.versionsState is VersionsState.Loading) "identificando…" else "buscar mod",
        onClick = viewModel::fetchVersions,
        enabled = viewModel.modInput.isNotBlank() && viewModel.versionsState !is VersionsState.Loading,
    )

    when (val vs = viewModel.versionsState) {
        is VersionsState.Error -> {
            Spacer(Modifier.height(16.dp))
            RuleBox { Text(vs.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
        }

        is VersionsState.Loaded -> {
            Spacer(Modifier.height(20.dp))
            RuleBox {
                Text(vs.modName, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                Text("versão de minecraft", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    vs.versions.forEach { v ->
                        FilterPill(
                            text = v.gameVersion,
                            active = viewModel.selectedVersionId == v.id,
                            onClick = { viewModel.onSelectVersion(v.id) },
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                PrimaryStepButton(
                    text = if (viewModel.isSubmitting) "enviando…" else "adicionar ao catálogo",
                    onClick = viewModel::submitSingle,
                    enabled = viewModel.selectedVersionId != null && !viewModel.isSubmitting,
                )
            }
        }

        VersionsState.Idle, VersionsState.Loading -> Unit
    }
}

@Composable
private fun BulkModForm(viewModel: EnviarViewModel) {
    Text(
        "Um link ou slug do Modrinth por linha.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(12.dp))
    UnderlineField(
        value = viewModel.bulkInput,
        onValueChange = viewModel::onBulkInputChange,
        label = "mods do modrinth",
        placeholder = "sodium\nlithium\nhttps://modrinth.com/mod/iris",
        singleLine = false,
        minLines = 5,
    )
    Spacer(Modifier.height(16.dp))
    PrimaryStepButton(
        text = if (viewModel.isSubmitting) "enviando…" else "enviar todos",
        onClick = viewModel::submitBulk,
        enabled = viewModel.bulkInput.isNotBlank() && !viewModel.isSubmitting,
    )
}

@Composable
private fun OutcomePanel(outcome: SubmitOutcome) {
    RuleBox {
        when (outcome) {
            is SubmitOutcome.Single -> {
                Text("adicionado com sucesso", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                Text(outcome.modName, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "O mod aparece no catálogo depois que o deploy da Vercel terminar (~1 min).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is SubmitOutcome.Bulk -> {
                val ok = outcome.results.count { it.status == "ok" }
                Text(
                    "$ok de ${outcome.results.size} adicionados",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(10.dp))
                outcome.results.forEach { r ->
                    val ok2 = r.status == "ok"
                    Text(
                        if (ok2) "✓ ${r.mod?.name ?: r.modInput}" else "✕ ${r.modInput} — ${r.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (ok2) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
            }
        }
    }
}
