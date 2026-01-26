package com.example.agrobot.ui.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrobot.DownloadState
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@RequiresApi(Build.VERSION_CODES.S)
class LanguageSelectionViewModel : ViewModel() {

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun downloadLanguageModel(langCode: String) {
        viewModelScope.launch {
            if (langCode == "en") {
                _navigationEvent.emit(langCode)
                return@launch
            }

            // Validate that the language is supported by ML Kit
            val targetLanguage = TranslateLanguage.fromLanguageTag(langCode)
            if (targetLanguage == null) {
                _downloadState.value = DownloadState.Error("Language '$langCode' is not supported by ML Kit Translation API.")
                return@launch
            }

            _downloadState.value = DownloadState.Downloading

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(targetLanguage)
                .build()
            val translator = Translation.getClient(options)
            val conditions = DownloadConditions.Builder().build()

            try {
                // Step 1: Await the download. This part is generally safe.
                translator.downloadModelIfNeeded(conditions).await()

                // Step 2: The definitive test. Use callbacks to prevent uncaught exceptions.
                translator.translate("hello")
                    .addOnSuccessListener {
                        // SUCCESS: The model is confirmed to be working.
                        translator.close()
                        viewModelScope.launch {
                            _downloadState.value = DownloadState.Success(langCode)
                            _navigationEvent.emit(langCode)
                        }
                    }
                    .addOnFailureListener { exception ->
                        // FAILURE: The model is not ready. Catch the exception and show an error.
                        translator.close()
                        _downloadState.value = DownloadState.Error("Model for '$langCode' failed to initialize. Please try again.")
                    }

            } catch (e: Exception) {
                // Catch any other exceptions during the download process itself.
                translator.close()
                _downloadState.value = DownloadState.Error("Could not download language '$langCode'. Please check your connection.")
            }
        }
    }

    fun resetDownloadState() {
        _downloadState.value = DownloadState.Idle
    }
}
