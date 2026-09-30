package com.byhenriquesilva.atlasdemods.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.byhenriquesilva.atlasdemods.data.ModsRepository
import com.byhenriquesilva.atlasdemods.data.SecretStore
import com.byhenriquesilva.atlasdemods.ui.components.GhostButton
import com.byhenriquesilva.atlasdemods.ui.components.PrimaryStepButton
import com.byhenriquesilva.atlasdemods.ui.components.RuleBox
import com.byhenriquesilva.atlasdemods.ui.components.UnderlineField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(repository: ModsRepository, secretStore: SecretStore, onBack: () -> Unit) {
    val viewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory { initializer { SettingsViewModel(secretStore, repository) } },
    )
    var secretVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ajustes", style = MaterialTheme.typography.labelMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row {
                Text("Ajustes", style = MaterialTheme.typography.headlineLarge)
                Text(".", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(24.dp))

            UnderlineField(
                value = viewModel.baseUrl,
                onValueChange = viewModel::onBaseUrlChange,
                label = "url do site (vercel)",
                placeholder = "https://seu-site.vercel.app",
            )
            Spacer(Modifier.height(20.dp))

            Column {
                UnderlineField(
                    value = viewModel.adminSecret,
                    onValueChange = viewModel::onAdminSecretChange,
                    label = "senha de admin (admin_secret)",
                    visualTransformation = if (secretVisible) {
                        androidx.compose.ui.text.input.VisualTransformation.None
                    } else {
                        androidx.compose.ui.text.input.PasswordVisualTransformation()
                    },
                    trailing = {
                        IconButton(onClick = { secretVisible = !secretVisible }) {
                            Icon(
                                if (secretVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (secretVisible) "Ocultar senha" else "Mostrar senha",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                )
            }
            Text(
                "Fica salva só neste aparelho, criptografada via Android Keystore. É a mesma senha que você usa no site.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryStepButton(text = "salvar", onClick = viewModel::save)
                GhostButton(
                    text = if (viewModel.testState is TestState.Loading) "testando…" else "testar conexão",
                    onClick = viewModel::testConnection,
                    enabled = viewModel.testState !is TestState.Loading,
                )
            }

            Spacer(Modifier.height(16.dp))
            when (val ts = viewModel.testState) {
                is TestState.Success -> RuleBox {
                    Text(
                        "Conectado! ${ts.count} mods no catálogo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                is TestState.Failure -> RuleBox {
                    Text(ts.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                else -> Unit
            }
        }
    }
}
