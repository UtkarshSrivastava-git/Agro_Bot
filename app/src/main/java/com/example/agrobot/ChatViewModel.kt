package com.example.agrobot

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    private val baseUrl = "https://us-central1-agro-bot-37d6e.cloudfunctions.net/api/"

    private val repo: ChatRepository by lazy {
        ChatRepository(
            api = NetworkModule.create(baseUrl),
            firestore = firestore
        )
    }

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    var userLangTag: String = "en"

    @RequiresApi(Build.VERSION_CODES.S)
    fun sendMessage(
        rawText: String,
        ttsSpeak: (String, String?) -> Unit
    ) {
        if (rawText.isBlank()) return

        _messages.value += Message(rawText, true)

        viewModelScope.launch {
            try {
                val translated = if (userLangTag != "en") {
                    try {
                        TranslatorHelper.translateText(rawText, userLangTag, "en")
                    } catch (e: Exception) {
                        rawText
                    }
                } else rawText

                val replyEn = repo.fetchReply(userId, translated, "en")

                val finalReply = if (userLangTag != "en") {
                    try {
                        TranslatorHelper.translateText(replyEn, "en", userLangTag)
                    } catch (e: Exception) {
                        replyEn
                    }
                } else replyEn

                _messages.value += Message(finalReply, false)

                ttsSpeak(finalReply, userLangTag)

            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "sendMessage failed", e)
                _messages.value += Message("Error: ${e.message}", false)
            }
        }
    }
}
