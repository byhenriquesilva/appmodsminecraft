package com.byhenriquesilva.atlasdemods.network

import com.byhenriquesilva.atlasdemods.network.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/** Espelha 1:1 as rotas de docs/API.md do site. */
interface AtlasApi {

    @GET("api/catalog")
    suspend fun getCatalog(): CatalogResponse

    @GET("api/mod")
    suspend fun getMod(@Query("id") id: String): Mod

    @GET("api/versions")
    suspend fun getVersions(
        @Header("Authorization") auth: String,
        @Query("input") input: String,
    ): VersionsResponse

    @POST("api/submit")
    suspend fun submit(
        @Header("Authorization") auth: String,
        @Body body: SubmitRequest,
    ): SubmitResponse

    @POST("api/submit-bulk")
    suspend fun submitBulk(
        @Header("Authorization") auth: String,
        @Body body: SubmitBulkRequest,
    ): SubmitBulkResponse

    @POST("api/delete")
    suspend fun delete(
        @Header("Authorization") auth: String,
        @Body body: DeleteRequest,
    ): DeleteResponse
}
