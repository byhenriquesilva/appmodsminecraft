package com.byhenriquesilva.atlasdemods

import android.app.Application
import com.byhenriquesilva.atlasdemods.data.ModsRepository
import com.byhenriquesilva.atlasdemods.data.SecretStore

/**
 * "DI" manual e simples de propósito — sem Hilt/Koin pra manter o esqueleto
 * fácil de ler. Se o app crescer, trocar isso por Hilt é direto.
 */
class AtlasApp : Application() {

    lateinit var secretStore: SecretStore
        private set
    lateinit var repository: ModsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        secretStore = SecretStore(this)
        repository = ModsRepository(secretStore)
    }
}
