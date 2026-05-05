package com.ths.tradingai.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object ApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    // ========== 请求/响应数据类 ==========

    data class BindRequest(
        @SerializedName("bind_code") val bindCode: String,
        @SerializedName("device_name") val deviceName: String
    )

    data class BindResponse(
        val success: Boolean,
        val token: String? = null,
        @SerializedName("device_id") val deviceId: String? = null,
        val message: String? = null
    )

    data class ApiResponse<T>(
        val success: Boolean,
        val data: T? = null,
        val message: String? = null
    )

    data class AccountData(
        val balance: Double = 0.0,
        @SerializedName("market_value") val marketValue: Double = 0.0,
        @SerializedName("total_assets") val totalAssets: Double = 0.0,
        @SerializedName("position_count") val positionCount: Int = 0,
        val positions: List<Position> = emptyList()
    )

    data class Position(
        val symbol: String = "",
        val name: String = "",
        val amount: Int = 0,
        @SerializedName("cost_price") val costPrice: Double = 0.0,
        @SerializedName("current_price") val currentPrice: Double = 0.0,
        @SerializedName("market_value") val marketValue: Double = 0.0,
        val profit: Double = 0.0
    )

    data class StatusData(
        val connected: Boolean = false,
        @SerializedName("auto_trade") val autoTrade: Boolean = false,
        @SerializedName("scheduler_enabled") val schedulerEnabled: Boolean = false,
        val balance: Double = 0.0,
        @SerializedName("total_assets") val totalAssets: Double = 0.0
    )

    data class ChatRequest(
        val message: String,
        val history: List<Map<String, String>> = emptyList()
    )

    data class ChatResponse(
        val content: String = "",
        val model: String = "",
        val traded: Boolean = false
    )

    data class Notification(
        val title: String = "",
        val message: String = "",
        val level: String = "",
        @SerializedName("created_at") val createdAt: String = ""
    )

    // ========== API 调用 ==========

    fun claimBindCode(server: String, code: String, name: String): BindResponse {
        val json = gson.toJson(BindRequest(code, name))
        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$server/api/mobile/bind/claim")
            .post(body)
            .build()
        val response = client.newCall(request).execute()
        return gson.fromJson(response.body?.string(), BindResponse::class.java)
    }

    fun getAccount(server: String, token: String): ApiResponse<AccountData> {
        val request = Request.Builder()
            .url("$server/api/mobile/account")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        val response = client.newCall(request).execute()
        return gson.fromJson(response.body?.string(), object : com.google.gson.reflect.TypeToken<ApiResponse<AccountData>>() {}.type)
    }

    fun getStatus(server: String, token: String): ApiResponse<StatusData> {
        val request = Request.Builder()
            .url("$server/api/mobile/status")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        val response = client.newCall(request).execute()
        return gson.fromJson(response.body?.string(), object : com.google.gson.reflect.TypeToken<ApiResponse<StatusData>>() {}.type)
    }

    fun chat(server: String, token: String, message: String, history: List<Map<String, String>> = emptyList()): ApiResponse<ChatResponse> {
        val json = gson.toJson(ChatRequest(message, history))
        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$server/api/mobile/ai/chat")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()
        val response = client.newCall(request).execute()
        return gson.fromJson(response.body?.string(), object : com.google.gson.reflect.TypeToken<ApiResponse<ChatResponse>>() {}.type)
    }

    fun getNotifications(server: String, token: String, limit: Int = 30): ApiResponse<List<Notification>> {
        val request = Request.Builder()
            .url("$server/api/mobile/notifications?limit=$limit")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        val response = client.newCall(request).execute()
        return gson.fromJson(response.body?.string(), object : com.google.gson.reflect.TypeToken<ApiResponse<List<Notification>>>() {}.type)
    }

    fun healthCheck(server: String): Boolean {
        return try {
            val request = Request.Builder().url("$server/api/health").get().build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
