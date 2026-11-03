package com.gmmdistributors.app.api

import android.content.Context
import com.gmmdistributors.app.BuildConfig
import com.gmmdistributors.app.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class ApiReply(val code: Int?, val json: JSONObject?) {
    val ok: Boolean get() = code != null && code in 200..299
    val offline: Boolean get() = code == null
    val authExpired: Boolean get() = code == 401 && json?.optString("code") == "auth"
    val error: String? get() = json?.optString("error")?.takeIf { it.isNotBlank() }
}

/**
 * JSON client for the GMM Django backend (`/api/v1/`). Every call is a POST with a JSON body.
 * Signed-in calls carry `Authorization: Bearer <token>` and an HMAC-SHA256 signature
 * (`X-GMM-Time`, `X-GMM-Nonce`, `X-GMM-Sign`) over method, path, time, nonce and body hash,
 * checked by `apps/api/security.py` on the server.
 */
class Api(context: Context) {
    private val ctx = context.applicationContext
    private val prefs = Prefs(ctx)
    private val secrets = SecretStore(ctx)

    suspend fun post(path: String, body: JSONObject = JSONObject()): ApiReply = request(path, body)

    private fun hmac(secret: String, data: String): String {
        val mac = javax.crypto.Mac.getInstance("HmacSHA256")
        mac.init(javax.crypto.spec.SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun sha256(bytes: ByteArray): String =
        java.security.MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private suspend fun request(path: String, body: JSONObject): ApiReply = withContext(Dispatchers.IO) {
        try {
            val url = URL("${prefs.serverUrl}/api/v1/$path")
            val bytes = body.toString().toByteArray(Charsets.UTF_8)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "POST"
                conn.connectTimeout = 12_000
                conn.readTimeout = 25_000
                conn.useCaches = false
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.setRequestProperty("X-GMM-Lang", prefs.language)
                conn.setRequestProperty("User-Agent", "GMM-Android/${BuildConfig.VERSION_NAME}")
                val token = secrets.token
                if (token.isNotBlank()) {
                    conn.setRequestProperty("Authorization", "Bearer $token")
                    val secret = secrets.secret
                    if (secret.isNotBlank()) {
                        val ts = (System.currentTimeMillis() / 1000).toString()
                        val nonce = java.util.UUID.randomUUID().toString().replace("-", "")
                        val toSign = listOf("POST", url.path, ts, nonce, sha256(bytes)).joinToString("\n")
                        conn.setRequestProperty("X-GMM-Time", ts)
                        conn.setRequestProperty("X-GMM-Nonce", nonce)
                        conn.setRequestProperty("X-GMM-Sign", hmac(secret, toSign))
                    }
                }
                conn.doOutput = true
                conn.outputStream.use { it.write(bytes) }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
                val json = try { if (text.isBlank()) null else JSONObject(text) } catch (e: Exception) { null }
                ApiReply(code, json)
            } finally {
                conn.disconnect()
            }
        } catch (e: IOException) {
            ApiReply(null, null)
        } catch (e: IllegalArgumentException) {
            ApiReply(null, null)  // malformed server URL
        }
    }
}
