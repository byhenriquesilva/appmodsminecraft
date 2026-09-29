package com.byhenriquesilva.atlasdemods.ui.screens.catalog

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byhenriquesilva.atlasdemods.data.ApiFailure
import com.byhenriquesilva.atlasdemods.data.DownloadAllState
import com.byhenriquesilva.atlasdemods.data.ModsRepository
import com.byhenriquesilva.atlasdemods.data.SecretStore
import com.byhenriquesilva.atlasdemods.data.ZipDownloader
import com.byhenriquesilva.atlasdemods.data.distinctVersionsDesc
import com.byhenriquesilva.atlasdemods.network.model.Mod
import kotlinx.coroutines.launch

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Error(val message: String) : CatalogUiState
    data class Loaded(val allMods: List<Mod>) : CatalogUiState
}

/** `null` representa "todas as versões" (o "all" do site). */
class CatalogViewModel(private val repository: ModsRepository, private val secretStore: SecretStore) : ViewModel() {

    var uiState: CatalogUiState by mutableStateOf(CatalogUiState.Loading)
        private set

    var query: String by mutableStateOf("")
        private set

    var selectedVersion: String? by mutableStateOf(null)
        private set

    var downloadAllState: DownloadAllState by mutableStateOf(DownloadAllState.Idle)
        private set

    /** Versões presentes no catálogo, da mais nova pra mais antiga — igual ao MC_VERSIONS do site. */
    val availableVersions: List<String>
        get() = (uiState as? CatalogUiState.Loaded)?.let { distinctVersionsDesc(it.allMods.map { m -> m.mc }) }.orEmpty()

    /** Só os mods da versão selecionada (ou todos) — o `modsForVersion` do site. Base do "baixar tudo". */
    val modsForVersion: List<Mod>
        get() {
            val all = (uiState as? CatalogUiState.Loaded)?.allMods.orEmpty()
            val v = selectedVersion ?: return all
            return all.filter { it.mc == v }
        }

    /** `modsForVersion` + a busca por texto — o que a lista realmente mostra. */
    val visibleMods: List<Mod>
        get() {
            val q = query.trim()
            if (q.isEmpty()) return modsForVersion
            return modsForVersion.filter {
                it.name.contains(q, ignoreCase = true) ||
                    it.category.contains(q, ignoreCase = true) ||
                    it.tags.any { tag -> tag.contains(q, ignoreCase = true) }
            }
        }

    init {
        refresh()
    }

    fun onQueryChange(value: String) {
        query = value
    }

    fun onSelectVersion(version: String?) {
        selectedVersion = version
    }

    fun refresh() {
        uiState = CatalogUiState.Loading
        viewModelScope.launch {
            uiState = try {
                val mods = repository.getCatalog().mods
                // Padrão: a versão mais nova (não "todas"), como pedido — hoje é a 26.3.
                if (selectedVersion == null) {
                    selectedVersion = distinctVersionsDesc(mods.map { it.mc }).firstOrNull()
                }
                CatalogUiState.Loaded(mods)
            } catch (e: ApiFailure) {
                CatalogUiState.Error(e.message ?: "Erro ao carregar o catálogo.")
            }
        }
    }

    fun downloadAllForCurrentFilter(context: Context) {
        val mods = modsForVersion
        viewModelScope.launch {
            ZipDownloader.downloadAllAsZip(
                context = context,
                mods = mods,
                baseUrl = secretStore.baseUrl,
            ) { state -> downloadAllState = state }
        }
    }

    fun dismissDownloadAllState() {
        downloadAllState = DownloadAllState.Idle
    }
}
