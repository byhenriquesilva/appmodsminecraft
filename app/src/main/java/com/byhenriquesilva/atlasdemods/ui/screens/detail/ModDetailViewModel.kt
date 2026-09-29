package com.byhenriquesilva.atlasdemods.ui.screens.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byhenriquesilva.atlasdemods.data.ApiFailure
import com.byhenriquesilva.atlasdemods.data.ModsRepository
import com.byhenriquesilva.atlasdemods.network.model.Mod
import kotlinx.coroutines.launch

sealed interface ModDetailUiState {
    data object Loading : ModDetailUiState
    data class Error(val message: String) : ModDetailUiState
    data class Loaded(val mod: Mod) : ModDetailUiState
}

class ModDetailViewModel(private val repository: ModsRepository, private val modId: String) : ViewModel() {

    var uiState: ModDetailUiState by mutableStateOf(ModDetailUiState.Loading)
        private set

    init {
        load()
    }

    fun load() {
        uiState = ModDetailUiState.Loading
        viewModelScope.launch {
            uiState = try {
                ModDetailUiState.Loaded(repository.getMod(modId))
            } catch (e: ApiFailure) {
                ModDetailUiState.Error(e.message ?: "Erro ao carregar o mod.")
            }
        }
    }
}
