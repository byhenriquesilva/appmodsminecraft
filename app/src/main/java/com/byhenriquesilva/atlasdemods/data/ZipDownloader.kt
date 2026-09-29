package com.byhenriquesilva.atlasdemods.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.byhenriquesilva.atlasdemods.network.model.Mod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

sealed interface DownloadAllState {
    data object Idle : DownloadAllState
    data class Progress(val done: Int, val total: Int) : DownloadAllState
    data class Done(val failed: List<String>, val total: Int) : DownloadAllState
    data class Error(val message: String) : DownloadAllState
}

/**
 * Baixa os `.jar` já filtrados (por versão) e monta um único `.zip` em
 * Downloads — o mesmo que `download-all-button.tsx` faz no navegador, só que
 * aqui em Kotlin puro em vez de JSZip. Um arquivo que falhe não derruba o
 * zip inteiro, só fica de fora (e entra na lista de falhas no final).
 *
 * Observação: no site o zip usa `compression: "STORE"` (sem recomprimir, já
 * que os .jar já são comprimidos). Aqui uso DEFLATED com nível 0 — o
 * resultado é equivalente (nenhuma recompressão), mas evita ter que calcular
 * CRC32/tamanho manualmente, que o modo STORED exige.
 */
object ZipDownloader {

    private val client by lazy { OkHttpClient() }

    suspend fun downloadAllAsZip(
        context: Context,
        mods: List<Mod>,
        baseUrl: String,
        fileName: String = "atlas-de-mods.zip",
        onState: (DownloadAllState) -> Unit,
    ) = withContext(Dispatchers.IO) {
        if (mods.isEmpty()) {
            onState(DownloadAllState.Error("Nenhum mod pra baixar com esse filtro."))
            return@withContext
        }

        val tempFile = File(context.cacheDir, "download-${System.currentTimeMillis()}.zip")
        val failed = mutableListOf<String>()
        onState(DownloadAllState.Progress(0, mods.size))

        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(tempFile))).use { zip ->
                zip.setLevel(Deflater.NO_COMPRESSION)
                mods.forEachIndexed { index, mod ->
                    try {
                        val request = Request.Builder().url(mod.resolvedDownloadUrl(baseUrl)).build()
                        client.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                            zip.putNextEntry(ZipEntry(mod.file))
                            response.body?.byteStream()?.copyTo(zip)
                            zip.closeEntry()
                        }
                    } catch (_: IOException) {
                        failed += mod.file
                    }
                    onState(DownloadAllState.Progress(index + 1, mods.size))
                }
            }

            if (failed.size == mods.size) {
                tempFile.delete()
                onState(DownloadAllState.Error("Nenhum arquivo pôde ser baixado."))
                return@withContext
            }

            saveToDownloads(context, tempFile, fileName)
            onState(DownloadAllState.Done(failed, mods.size))
        } catch (e: IOException) {
            onState(DownloadAllState.Error(e.message ?: "Erro ao gerar o .zip."))
        } finally {
            tempFile.delete()
        }
    }

    /** Copia o zip temporário pra pasta pública de Downloads. */
    private fun saveToDownloads(context: Context, source: File, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/zip")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Não consegui criar o arquivo em Downloads.")
            resolver.openOutputStream(uri)?.use { out -> source.inputStream().use { it.copyTo(out) } }
                ?: throw IOException("Não consegui escrever em Downloads.")
        } else {
            // Android 8/9: escrita direta na pasta pública, exige WRITE_EXTERNAL_STORAGE
            // (pedida em runtime — ver AtlasNavHost/CatalogScreen).
            @Suppress("DEPRECATION")
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!dir.exists()) dir.mkdirs()
            val dest = File(dir, fileName)
            source.copyTo(dest, overwrite = true)
        }
    }
}
