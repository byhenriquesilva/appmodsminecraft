package com.byhenriquesilva.atlasdemods.ui.screens.catalog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byhenriquesilva.atlasdemods.data.ApiFailure
import com.byhenriquesilva.atlasdemods.data.ModsRepository
import com.byhenriquesilva.atlasdemods.network.model.Mod
import kotlinx.coroutines.launch

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Error(val message: String) : CatalogUiState
    data class Loaded(val allMods: List<Mod>) : CatalogUiState
}

class CatalogViewModel(private val repository: ModsRepository) : ViewModel() {

    var uiState: CatalogUiState by mutableStateOf(CatalogUiState.Loading)
        private set

    var query: String by mutableStateOf("")
        private set

    val visibleMods: List<Mod>
        get() {
            val state = uiState
            if (state !is CatalogUiState.Loaded) return emptyList()
            val q = query.trim()
            if (q.isEmpty()) return state.allMods
            return state.allMods.filter {
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

    fun refresh() {
        uiState = CatalogUiState.Loading
        viewModelScope.launch {
            uiState = try {
                CatalogUiState.Loaded(repository.getCatalog().mods)
            } catch (e: ApiFailure) {
                CatalogUiState.Error(e.message ?: "Erro ao carregar o catálogo.")
            }
        }
    }
}
