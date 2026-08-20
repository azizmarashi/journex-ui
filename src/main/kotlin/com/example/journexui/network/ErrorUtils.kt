package com.example.journexui.network

import com.google.gson.JsonParser
import retrofit2.HttpException

/**
 * Extracts the backend's human-readable exception message instead of showing
 * Retrofit's generic HTTP error text. Supports common error-handler payloads
 * such as {"message":"..."}, {"error":"..."}, {"detail":"..."}, and plain text.
 */
fun extractErrorMessage(e: Throwable): String {
    if (e is HttpException) {
        val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        if (!body.isNullOrBlank()) {
            runCatching {
                val json = JsonParser.parseString(body)
                if (json.isJsonObject) {
                    val obj = json.asJsonObject
                    listOf("message", "error", "detail", "title").forEach { key ->
                        val value = obj.get(key)
                        if (value != null && !value.isJsonNull && value.isJsonPrimitive) {
                            val text = value.asString.trim()
                            if (text.isNotEmpty()) return text
                        }
                    }
                } else if (json.isJsonPrimitive) {
                    val text = json.asString.trim()
                    if (text.isNotEmpty()) return text
                }
            }
            val text = body.trim().trim('"')
            if (text.isNotEmpty()) return text
        }
        return "Request failed (${e.code()})"
    }
    return e.message?.takeIf { it.isNotBlank() } ?: "Unexpected error"
}
