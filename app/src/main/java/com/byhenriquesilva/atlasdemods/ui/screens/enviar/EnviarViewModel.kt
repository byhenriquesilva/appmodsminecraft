package com.byhenriquesilva.atlasdemods.ui.screens.enviar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byhenriquesilva.atlasdemods.data.ApiFailure
import com.byhenriquesilva.atlasdemods.data.ModsRepository
import com.byhenriquesilva.atlasdemods.network.model.BulkResultItem
import com.byhenriquesilva.atlasdemods.network.model.VersionOption
import kotlinx.coroutines.launch

sealed interface VersionsState {
    data object Idle : VersionsState
    data object Loading : VersionsState
    data class Error(val message: String) : VersionsState
    data class Loaded(val modName: String, val versions: List<VersionOption>) : VersionsState
}

sealed interface SubmitOutcome {
    data class Single(val modName: String, val commitUrl: String?) : SubmitOutcome
    data class Bulk(val results: List<BulkResultItem>, val commitUrl: String?) : SubmitOutcome
}

class EnviarViewModel(private val repository: ModsRepository) : ViewModel() {

    // ---- Envio de um único mod (com escolha de versão) ----
    var modInput by mutableStateOf("")
        private set
    var versionsState: VersionsState by mutableStateOf(VersionsState.Idle)
        private set
    var selectedVersionId: String? by mutableStateOf(null)
        private set

    // ---- Envio em lote (sempre a versão mais nova) ----
    var bulkInput by mutableStateOf("")
        private set

    var isSubmitting by mutableStateOf(false)
        private set
    var submitError: String? by mutableStateOf(null)
        private set
    var outcome: SubmitOutcome? by mutableStateOf(null)
        private set

    fun onModInputChange(value: String) {
        modInput = value
        versionsState = VersionsState.Idle
        selectedVersionId = null
    }

    fun onBulkInputChange(value: String) {
        bulkInput = value
    }

    fun onSelectVersion(id: String) {
        selectedVersionId = id
    }

    fun fetchVersions() {
        val input = modInput.trim()
        if (input.isEmpty()) return
        versionsState = VersionsState.Loading
        viewModelScope.launch {
            versionsState = try {
                val res = repository.getVersions(input)
                VersionsState.Loaded(res.name, res.versions)
            } catch (e: ApiFailure) {
                VersionsState.Error(e.message ?: "Erro ao buscar versões.")
            }
        }
    }

    fun submitSingle() {
        val versionId = selectedVersionId ?: return
        isSubmitting = true
        submitError = null
        viewModelScope.launch {
            try {
                val res = repository.submit(modInput.trim(), versionId)
                outcome = SubmitOutcome.Single(res.mod.name, res.commitUrl)
                modInput = ""
                versionsState = VersionsState.Idle
                selectedVersionId = null
            } catch (e: ApiFailure) {
                submitError = e.message ?: "Erro ao enviar o mod."
            } finally {
                isSubmitting = false
            }
        }
    }

    fun submitBulk() {
        val inputs = bulkInput.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (inputs.isEmpty()) return
        isSubmitting = true
        submitError = null
        viewModelScope.launch {
            try {
                val res = repository.submitBulk(inputs)
                outcome = SubmitOutcome.Bulk(res.results, res.commitUrl)
                bulkInput = ""
            } catch (e: ApiFailure) {
                submitError = e.message ?: "Erro ao enviar os mods."
            } finally {
                isSubmitting = false
            }
        }
    }

    fun dismissOutcome() {
        outcome = null
    }
}
