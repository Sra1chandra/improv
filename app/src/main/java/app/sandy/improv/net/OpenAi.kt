package app.sandy.improv.net

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

enum class SandyError { NO_KEY, KEY_REFUSED, OFFLINE, STUCK }

class SandyException(val error: SandyError) : Exception(error.name)

object OpenAi {
    fun parseText(body: String): String {
        val choices = JSONObject(body).getJSONArray("choices")
        return choices.getJSONObject(0).getJSONObject("message").getString("content").trim()
    }

    fun mapHttp(code: Int): SandyError = when (code) {
        401, 403 -> SandyError.KEY_REFUSED
        in 500..599 -> SandyError.STUCK
        else -> SandyError.STUCK
    }

    fun mapNetwork(e: Exception): SandyError {
        if (e is UnknownHostException || e is ConnectException) return SandyError.OFFLINE
        if (e is InterruptedIOException || e is SocketTimeoutException) return SandyError.STUCK
        if (e.message?.contains("timeout", ignoreCase = true) == true) return SandyError.STUCK
        return SandyError.OFFLINE
    }

    fun complete(
        key: String,
        model: String,
        messages: List<Map<String, String>>,
        client: OkHttpClient = OkHttpClient.Builder()
            .callTimeout(45, TimeUnit.SECONDS)
            .build(),
    ): String {
        if (key.isBlank()) throw SandyException(SandyError.NO_KEY)
        val arr = JSONArray()
        for (m in messages) {
            arr.put(JSONObject().put("role", m["role"]).put("content", m["content"]))
        }
        val payload = JSONObject()
            .put("model", model)
            .put("max_tokens", 80)
            .put("temperature", 0.9)
            .put("messages", arr)
        val req = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $key")
            .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        try {
            client.newCall(req).execute().use { res ->
                val body = res.body?.string().orEmpty()
                if (!res.isSuccessful) throw SandyException(mapHttp(res.code))
                return parseText(body)
            }
        } catch (e: SandyException) {
            throw e
        } catch (e: IOException) {
            throw SandyException(mapNetwork(e))
        }
    }
}
