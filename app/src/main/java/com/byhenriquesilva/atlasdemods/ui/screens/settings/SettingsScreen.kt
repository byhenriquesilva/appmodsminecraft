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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.byhenriquesilva.atlasdemods.data.ModsRepository
import com.byhenriquesilva.atlasdemods.data.SecretStore

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
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = viewModel.baseUrl,
                onValueChange = viewModel::onBaseUrlChange,
                label = { Text("URL do site (Vercel)") },
                placeholder = { Text("https://seu-site.vercel.app") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = viewModel.adminSecret,
                onValueChange = viewModel::onAdminSecretChange,
                label = { Text("Senha de admin (ADMIN_SECRET)") },
                singleLine = true,
                visualTransformation = if (secretVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { secretVisible = !secretVisible }) {
                        Icon(
                            if (secretVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (secretVisible) "Ocultar senha" else "Mostrar senha",
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Fica salva só neste aparelho, criptografada via Android Keystore. É a mesma senha que você usa no site.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = viewModel::save) { Text("Salvar") }
                OutlinedButton(
                    onClick = viewModel::testConnection,
                    enabled = viewModel.testState !is TestState.Loading,
                ) { Text("Testar conexão") }
            }

            Spacer(Modifier.height(12.dp))
            when (val ts = viewModel.testState) {
                is TestState.Loading -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                is TestState.Success -> Text(
                    "Conectado! ${ts.count} mods no catálogo.",
                    color = MaterialTheme.colorScheme.primary,
                )
                is TestState.Failure -> Text(ts.message, color = MaterialTheme.colorScheme.error)
                TestState.Idle -> Unit
            }
        }
    }
}
