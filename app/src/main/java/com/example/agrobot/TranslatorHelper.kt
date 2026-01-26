package com.example.agrobot

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

sealed class DownloadState {
    object Idle : DownloadState()
    object Downloading : DownloadState()
    data class Success(val langCode: String) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

object TranslatorHelper {

    @RequiresApi(Build.VERSION_CODES.S)
    fun downloadModel(targetLang: String): Flow<DownloadState> = callbackFlow {
        trySend(DownloadState.Downloading)

        val remoteModelManager = RemoteModelManager.getInstance()
        val conditions = DownloadConditions.Builder().build()
        val targetModel = TranslateRemoteModel.Builder(targetLang).build()

        // Definitive Strategy: Delete the model first to ensure a clean slate.
        remoteModelManager.deleteDownloadedModel(targetModel)
            .continueWithTask { remoteModelManager.download(targetModel, conditions) }
            .addOnSuccessListener {
                // After a clean download, verify it's in the set of models.
                remoteModelManager.getDownloadedModels(TranslateRemoteModel::class.java)
                    .addOnSuccessListener { downloadedModels ->
                        val isModelReady = downloadedModels.any { it.language.startsWith(targetLang) }
                        if (isModelReady) {
                            trySend(DownloadState.Success(targetLang))
                        } else {
                            trySend(DownloadState.Error("Model for '$targetLang' not found after clean download."))
                        }
                        channel.close()
                    }
                    .addOnFailureListener { exception ->
                        trySend(DownloadState.Error("Failed to verify model set for '$targetLang': ${exception.message}"))
                        channel.close(exception)
                    }
            }
            .addOnFailureListener { exception ->
                trySend(DownloadState.Error("Model download failed for '$targetLang': ${exception.message}"))
                channel.close(exception)
            }
        awaitClose { }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun getTranslator(sourceLang: String, targetLang: String): Translator {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.fromLanguageTag(sourceLang) ?: TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.fromLanguageTag(targetLang) ?: TranslateLanguage.ENGLISH)
            .build()
        return Translation.getClient(options)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    suspend fun translateText(input: String, fromLang: String, toLang: String): String {
        if (fromLang == toLang) return input

        // This function now assumes the model has been successfully downloaded
        // by the robust downloadModel flow.
        val translator = getTranslator(fromLang, toLang)
        return try {
            // No more downloadIfNeeded, we translate directly.
            translator.translate(input).await()
        } catch (e: Exception) {
            // If it fails here, we return an error message instead of crashing.
            "Translation Error: ${e.localizedMessage}"
        } finally {
            translator.close()
        }
    }
}
