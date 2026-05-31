package com.tamersarioglu.openapitest.data.remote.api

import com.tamersarioglu.openapitest.data.remote.dto.ChatRequestDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("chat/completions")
    fun createChatCompletion(
        @Header("Authorization") apiKey: String,
        @Header("HTTP-Referer") referer: String,
        @Header("X-Title") appName: String,
        @Body request: ChatRequestDto,
    ): Call<ResponseBody>
}
