package com.byhenriquesilva.atlasdemods.ui.screens.gerenciar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byhenriquesilva.atlasdemods.data.ApiFailure
import com.byhenriquesilva.atlasdemods.data.ModsRepository
import com.byhenriquesilva.atlasdemods.network.model.Mod
import kotlinx.coroutines.launch

sealed interface GerenciarUiState {
    data object Loading : GerenciarUiState
    data class Error(val message: String) : GerenciarUiState
    data class Loaded(val mods: List<Mod>) : GerenciarUiState
}

class GerenciarViewModel(private val repository: ModsRepository) : ViewModel() {

    var uiState: GerenciarUiState by mutableStateOf(GerenciarUiState.Loading)
        private set

    var selectedIds by mutableStateOf(emptySet<String>())
        private set

    var isDeleting by mutableStateOf(false)
        private set
    var deleteError: String? by mutableStateOf(null)
        private set
    var lastDeletedNames: List<String>? by mutableStateOf(null)
        private set

    init {
        refresh()
    }

    fun refresh() {
        uiState = GerenciarUiState.Loading
        selectedIds = emptySet()
        viewModelScope.launch {
            uiState = try {
                GerenciarUiState.Loaded(repository.getCatalog().mods)
            } catch (e: ApiFailure) {
                GerenciarUiState.Error(e.message ?: "Erro ao carregar o catálogo.")
            }
        }
    }

    fun toggleSelection(id: String) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    fun clearSelection() {
        selectedIds = emptySet()
    }

    fun confirmDelete() {
        val ids = selectedIds.toList()
        if (ids.isEmpty()) return
        isDeleting = true
        deleteError = null
        viewModelScope.launch {
            try {
                val res = repository.deleteMods(ids)
                lastDeletedNames = res.removed.map { it.name }
                refresh()
            } catch (e: ApiFailure) {
                deleteError = e.message ?: "Erro ao remover os mods."
            } finally {
                isDeleting = false
            }
        }
    }

    fun dismissDeletedNotice() {
        lastDeletedNames = null
    }
}
