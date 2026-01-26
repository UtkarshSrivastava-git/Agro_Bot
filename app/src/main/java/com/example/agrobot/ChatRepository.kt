package com.example.agrobot

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class ChatRepository(private val api: ChatApi,
                     private val firestore: FirebaseFirestore)
{
    suspend fun fetchReply(userId: String, message: String, lang: String?): String {
        val resp = api.chat(ChatRequest(message = message, lang = lang, sessionId = userId))

        try {
            val coll = firestore.collection("chats").document(userId).collection("messages")
            coll.add(mapOf("text" to message, "isUser" to true, "ts" to System.currentTimeMillis()))
            coll.add(mapOf("text" to resp.reply, "isUser" to false, "ts" to System.currentTimeMillis()))
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error saving chat message to Firestore", e)
        }
        return resp.reply
    }
}