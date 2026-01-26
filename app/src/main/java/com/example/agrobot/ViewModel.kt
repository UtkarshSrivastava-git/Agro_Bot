package com.example.agrobot

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Message(val text: String, val isUser: Boolean)

class ChatViewModel(private val repo: ChatRepository,private val userId: String) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    var userLangTag: String = "en"

    @RequiresApi(Build.VERSION_CODES.S)
    fun sendMessage(rawText: String, ttsSpeak: (String, String?)-> Unit){
        if(rawText.isBlank()) return
        _messages.value = _messages.value + Message(rawText, true)

        viewModelScope.launch{
            try {
                val translated = if (userLangTag != "en") {
                    try {
                        TranslatorHelper.translateText(rawText, userLangTag, "en")
                    } catch (e: Exception) {
                        rawText
                    }
                } else rawText

                val replyEn = repo.fetchReply(userId, translated, "en")
                val finalReply = if(userLangTag!= "en"){
                    try {
                        TranslatorHelper.translateText(replyEn,"en" , userLangTag)
                    }catch (e: Exception) { replyEn }
                }else replyEn

                _messages.value = _messages.value + Message(finalReply,false)
                ttsSpeak(finalReply,userLangTag)

            }catch (e: Exception){
                _messages.value = _messages.value + Message("Something went wrong,Try Again!",false)
            }
        }
    }
}