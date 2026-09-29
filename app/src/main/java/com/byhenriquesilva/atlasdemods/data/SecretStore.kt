package com.byhenriquesilva.atlasdemods.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.byhenriquesilva.atlasdemods.BuildConfig

/**
 * Guarda a URL base do site e a senha de admin (ADMIN_SECRET) criptografadas
 * no disco via Android Keystore. A senha NUNCA é logada nem aparece em
 * texto puro fora desta classe — só sai daqui dentro do header
 * `Authorization: Bearer <senha>` de uma chamada HTTPS.
 */
class SecretStore(context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "atlas_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, null)?.takeIf { it.isNotBlank() }
            ?: BuildConfig.DEFAULT_BASE_URL
        set(value) = prefs.edit().putString(KEY_BASE_URL, value.trim()).apply()

    var adminSecret: String
        get() = prefs.getString(KEY_ADMIN_SECRET, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ADMIN_SECRET, value).apply()

    val hasAdminSecret: Boolean get() = adminSecret.isNotBlank()

    /** Pronto pra ir direto no header Authorization. */
    fun bearerAuth(): String = "Bearer $adminSecret"

    companion object {
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_ADMIN_SECRET = "admin_secret"
    }
}
