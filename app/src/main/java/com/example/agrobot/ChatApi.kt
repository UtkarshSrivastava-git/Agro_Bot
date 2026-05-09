package com.example.agrobot

import retrofit2.http.Body
import retrofit2.http.POST

data class ChatRequest(val message: String,
                       val lang: String? = null,
                       val sessionId: String? = null)


data class ChatResponse(
    val reply: String? = null,
    val error: String? = null
)


interface ChatApi{
    @POST("Chat")
    suspend fun chat(@Body req: ChatRequest) : ChatResponse
}