package com.byhenriquesilva.atlasdemods.ui.screens.catalog

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.byhenriquesilva.atlasdemods.data.DownloadAllState
import com.byhenriquesilva.atlasdemods.data.ModsRepository
import com.byhenriquesilva.atlasdemods.data.SecretStore
import com.byhenriquesilva.atlasdemods.ui.components.FilterPill
import com.byhenriquesilva.atlasdemods.ui.components.ModCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    repository: ModsRepository,
    secretStore: SecretStore,
    onOpenMod: (String) -> Unit,
    onOpenEnviar: () -> Unit,
    onOpenGerenciar: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel: CatalogViewModel = viewModel(
        factory = viewModelFactory { initializer { CatalogViewModel(repository, secretStore) } },
    )
    val context = LocalContext.current
    val state = viewModel.uiState

    // No Android 8/9 salvar em Downloads exige essa permissão em runtime; no 10+ o
    // ZipDownloader usa MediaStore e não precisa de nada disso.
    var pendingDownloadAll by remember { mutableStateOf(false) }
    val storagePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && pendingDownloadAll) viewModel.downloadAllForCurrentFilter(context)
        pendingDownloadAll = false
    }

    fun startDownloadAll() {
        val needsPermission = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            pendingDownloadAll = true
            storagePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            viewModel.downloadAllForCurrentFilter(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("ATLAS", style = androidx.compose.ui.text.TextStyle(
                            fontFamily = com.byhenriquesilva.atlasdemods.ui.theme.PixelFontFamily,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = androidx.compose.ui.unit.TextUnit(15f, androidx.compose.ui.unit.TextUnitType.Sp),
                        ))
                        Spacer(Modifier.width(8.dp))
                        Text("de mods", style = MaterialTheme.typography.labelMedium)
                    }
                },
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
            com.byhenriquesilva.atlasdemods.ui.components.UnderlineField(
                value = viewModel.query,
                onValueChange = viewModel::onQueryChange,
                label = "buscar",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Spacer(Modifier.height(4.dp))

            if (state is CatalogUiState.Loaded && viewModel.availableVersions.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
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
                        FilterPill(
                            text = v,
                            active = viewModel.selectedVersion == v,
                            onClick = { viewModel.onSelectVersion(v) },
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${viewModel.modsForVersion.size} arquivos · fabric",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    DownloadAllButton(
                        count = viewModel.modsForVersion.size,
                        state = viewModel.downloadAllState,
                        onClick = ::startDownloadAll,
                    )
                }
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            when (state) {
                is CatalogUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                is CatalogUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = viewModel::refresh) { Text("Tentar de novo") }
                    }
                }

                is CatalogUiState.Loaded -> {
                    val mods = viewModel.visibleMods
                    if (mods.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhum mod encontrado.", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                            items(mods, key = { it.id }) { mod ->
                                ModCard(
                                    mod = mod,
                                    onClick = { onOpenMod(mod.id) },
                                    onDownloadClick = {
                                        val url = mod.resolvedDownloadUrl(secretStore.baseUrl)
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    },
                                )
                            }
                            item { Spacer(Modifier.height(72.dp)) } // espaço pro FAB não cobrir o último card
                        }
                    }
                }
            }
        }
    }

    // Feedback do "baixar tudo": igual ao site, um toast simples quando termina ou dá erro.
    LaunchedEffect(viewModel.downloadAllState) {
        when (val s = viewModel.downloadAllState) {
            is DownloadAllState.Done -> {
                val msg = if (s.failed.isEmpty()) {
                    "Baixado! ${s.total} mods salvos em Downloads."
                } else {
                    "${s.total - s.failed.size} de ${s.total} baixados. Falharam: ${s.failed.joinToString(", ")}"
                }
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                viewModel.dismissDownloadAllState()
            }
            is DownloadAllState.Error -> {
                Toast.makeText(context, s.message, Toast.LENGTH_LONG).show()
                viewModel.dismissDownloadAllState()
            }
            else -> Unit
        }
    }
}

@Composable
private fun DownloadAllButton(count: Int, state: DownloadAllState, onClick: () -> Unit) {
    val loading = state is DownloadAllState.Progress
    OutlinedButton(
        onClick = onClick,
        enabled = !loading && count > 0,
        shape = androidx.compose.ui.graphics.RectangleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = OutlinedButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
    ) {
        if (loading) {
            val p = state as DownloadAllState.Progress
            Text("preparando zip… ${p.done}/${p.total}", style = MaterialTheme.typography.labelMedium)
        } else {
            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("baixar tudo ($count)", style = MaterialTheme.typography.labelMedium)
        }
    }
}
