package com.byhenriquesilva.atlasdemods.network.model

import kotlinx.serialization.Serializable

/**
 * Um único modelo pra tudo que a API devolve como "mod": a listagem do
 * catálogo (sem description/gallery), o detalhe completo, e o objeto
 * devolvido depois de um envio. Os campos que só existem em algumas
 * respostas ficam opcionais.
 */
@Serializable
data class Mod(
    val id: String,
    val name: String,
    val author: String,
    val version: String,
    val loader: String,
    val mc: String,
    val category: String,
    val size: String,
    val file: String,
    val summary: String,
    val short: String? = null,
    /** HTML já sanitizado no servidor — só vem no detalhe (/api/mod) e no resultado de um envio. */
    val description: String? = null,
    val dependencies: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val iconUrl: String? = null,
    val modrinth: String? = null,
    /** Presente quando o .jar é grande demais pra ficar no repositório — o download aponta pro Modrinth. */
    val externalUrl: String? = null,
    val gallery: List<GalleryImage> = emptyList(),
    /** URL de download absoluta e pronta pra usar — só vem do /api/catalog e /api/mod. */
    val downloadUrl: String? = null,
) {
    /** Fallback pra quando `downloadUrl` não veio (ex: resultado de /api/submit). */
    fun resolvedDownloadUrl(baseUrl: String): String =
        downloadUrl ?: externalUrl ?: (baseUrl.trimEnd('/') + "/mods/" + file)
}

@Serializable
data class GalleryImage(
    val url: String,
    val title: String = "",
)

@Serializable
data class VersionOption(
    val id: String,
    val versionNumber: String,
    val gameVersion: String,
    val datePublished: String,
)

@Serializable
data class CatalogResponse(
    val count: Int,
    val mods: List<Mod>,
)

@Serializable
data class VersionsResponse(
    val name: String,
    val iconUrl: String? = null,
    val versions: List<VersionOption>,
)

@Serializable
data class SubmitRequest(
    val modInput: String,
    val versionId: String,
)

@Serializable
data class SubmitResponse(
    val ok: Boolean,
    val mod: Mod,
    val commitUrl: String? = null,
    val hostedExternally: Boolean = false,
)

@Serializable
data class SubmitBulkRequest(
    val modInputs: List<String>,
)

@Serializable
data class BulkResultItem(
    val modInput: String,
    val status: String, // "ok" | "error"
    val mod: Mod? = null,
    val message: String? = null,
)

@Serializable
data class SubmitBulkResponse(
    val ok: Boolean,
    val results: List<BulkResultItem>,
    val commitUrl: String? = null,
)

@Serializable
data class DeleteRequest(
    val modIds: List<String>,
)

@Serializable
data class RemovedMod(
    val id: String,
    val name: String,
)

@Serializable
data class DeleteResponse(
    val ok: Boolean,
    val removed: List<RemovedMod> = emptyList(),
    val commitUrl: String? = null,
)

@Serializable
data class ApiErrorBody(
    val error: String,
)
