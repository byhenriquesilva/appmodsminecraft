package com.byhenriquesilva.atlasdemods.data

import com.byhenriquesilva.atlasdemods.network.ApiClient
import com.byhenriquesilva.atlasdemods.network.model.*
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.HttpException

/** Erro de API já com a mensagem em português vinda do `{ "error": "..." }` do servidor. */
class ApiFailure(message: String, val httpCode: Int? = null) : Exception(message)

/**
 * Única porta de entrada pra rede no app. Cada função recria o cliente Retrofit
 * com a base URL atual (ver [ApiClient]) e traduz falhas HTTP em [ApiFailure]
 * com a mensagem que o servidor devolveu.
 */
class ModsRepository(private val secretStore: SecretStore) {

    private val errorJson = Json { ignoreUnknownKeys = true }

    private fun api() = ApiClient.create(secretStore.baseUrl)

    private suspend fun <T> call(block: suspend () -> T): T = try {
        block()
    } catch (e: HttpException) {
        throw ApiFailure(extractErrorMessage(e), e.code())
    } catch (e: java.io.IOException) {
        throw ApiFailure("Não consegui conectar ao servidor. Confira sua internet e a URL nos Ajustes.")
    }

    private fun extractErrorMessage(e: HttpException): String {
        val body: ResponseBody? = e.response()?.errorBody()
        val raw = body?.string()
        val fromJson = raw?.let {
            runCatching { errorJson.decodeFromString(ApiErrorBody.serializer(), it).error }.getOrNull()
        }
        return fromJson ?: when (e.code()) {
            401 -> "Senha incorreta."
            404 -> "Não encontrado."
            409 -> "Esse mod já está na lista."
            429 -> "Muitas tentativas erradas. Espere alguns minutos."
            else -> "Erro do servidor (HTTP ${e.code()})."
        }
    }

    suspend fun getCatalog(): CatalogResponse = call { api().getCatalog() }

    suspend fun getMod(id: String): Mod = call { api().getMod(id) }

    suspend fun getVersions(modInput: String): VersionsResponse =
        call { api().getVersions(secretStore.bearerAuth(), modInput) }

    suspend fun submit(modInput: String, versionId: String): SubmitResponse =
        call { api().submit(secretStore.bearerAuth(), SubmitRequest(modInput, versionId)) }

    suspend fun submitBulk(modInputs: List<String>): SubmitBulkResponse =
        call { api().submitBulk(secretStore.bearerAuth(), SubmitBulkRequest(modInputs)) }

    suspend fun deleteMods(modIds: List<String>): DeleteResponse =
        call { api().delete(secretStore.bearerAuth(), DeleteRequest(modIds)) }
}
