package com.byhenriquesilva.atlasdemods.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byhenriquesilva.atlasdemods.data.ApiFailure
import com.byhenriquesilva.atlasdemods.data.ModsRepository
import com.byhenriquesilva.atlasdemods.data.SecretStore
import kotlinx.coroutines.launch

sealed interface TestState {
    data object Idle : TestState
    data object Loading : TestState
    data class Success(val count: Int) : TestState
    data class Failure(val message: String) : TestState
}

class SettingsViewModel(private val secretStore: SecretStore, private val repository: ModsRepository) : ViewModel() {

    var baseUrl by mutableStateOf(secretStore.baseUrl)
        private set
    var adminSecret by mutableStateOf(secretStore.adminSecret)
        private set
    var testState: TestState by mutableStateOf(TestState.Idle)
        private set

    fun onBaseUrlChange(value: String) {
        baseUrl = value
        testState = TestState.Idle
    }

    fun onAdminSecretChange(value: String) {
        adminSecret = value
    }

    fun save() {
        secretStore.baseUrl = baseUrl.trim()
        secretStore.adminSecret = adminSecret
    }

    fun testConnection() {
        save() // testa exatamente o que será usado
        testState = TestState.Loading
        viewModelScope.launch {
            testState = try {
                TestState.Success(repository.getCatalog().count)
            } catch (e: ApiFailure) {
                TestState.Failure(e.message ?: "Falha ao conectar.")
            }
        }
    }
}
