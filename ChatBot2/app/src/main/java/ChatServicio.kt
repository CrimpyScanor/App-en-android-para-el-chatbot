package com.example.chatbot


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class ChatRequest(val message: String)
data class ChatResponse(val response: String)

interface ChatService {
    @POST("/chat")
    fun sendMessage(@Body request: ChatRequest): Call<ChatResponse>
}
