package com.tamersarioglu.openapitest

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface OpenRouterApi {

    @POST("chat/completions")
    fun createChatCompletion(
        @Header("Authorization") apiKey: String,
        @Header("HTTP-Referer") referer: String,
        @Header("X-Title") appName: String,
        @Body request: ChatRequest
    ): Call<ResponseBody>
}
