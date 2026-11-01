package com.gmmdistributors.app.api

import android.content.Context
import com.gmmdistributors.app.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class ApiReply(val code: Int?, val json: JSONObject?) {
    val ok: Boolean get() = code != null && code in 200..299
    val error: String? get() = json?.optString("error")?.takeIf { it.isNotBlank() }
}

/**
 * Native HTTP/JSON client + Dummy API router.
 * - When `prefs.useDummyApi == true` (default for Phase 1), all calls route through `DummyServer` in-app.
 * - When `prefs.useDummyApi == false` (Phase 2 Django integration), calls hit `${prefs.serverUrl}/api/v1/$path`
 *   with HMAC-SHA256 request signing (`X-GMM-Time`, `X-GMM-Nonce`, `X-GMM-Sign`), identical to Bas1Minute.
 */
class Api(context: Context) {
    private val ctx = context.applicationContext
    private val prefs = Prefs(ctx)
    private val secrets = SecretStore(ctx)

    suspend fun get(path: String): ApiReply = request("POST", path, JSONObject())
    suspend fun post(path: String, body: JSONObject = JSONObject()): ApiReply = request("POST", path, body)

    private fun hmac(secret: String, data: String): String {
        val mac = javax.crypto.Mac.getInstance("HmacSHA256")
        mac.init(javax.crypto.spec.SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun sha256(bytes: ByteArray): String =
        java.security.MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private suspend fun request(method: String, path: String, body: JSONObject): ApiReply = withContext(Dispatchers.IO) {
        if (prefs.useDummyApi) {
            return@withContext DummyServer.handle(ctx, path, body)
        }
        try {
            val url = URL("${prefs.serverUrl}/api/v1/$path")
            val bytes = body.toString().toByteArray(Charsets.UTF_8)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = method
                val secret = secrets.secret
                if (secret.isNotBlank()) {
                    val ts = (System.currentTimeMillis() / 1000).toString()
                    val nonce = java.util.UUID.randomUUID().toString().replace("-", "")
                    val toSign = listOf(method, url.path, ts, nonce, sha256(bytes)).joinToString("\n")
                    conn.setRequestProperty("X-GMM-Time", ts)
                    conn.setRequestProperty("X-GMM-Nonce", nonce)
                    conn.setRequestProperty("X-GMM-Sign", hmac(secret, toSign))
                }
                conn.connectTimeout = 12_000
                conn.readTimeout = 20_000
                conn.setRequestProperty("Accept", "application/json")
                if (prefs.token.isNotBlank()) conn.setRequestProperty("Authorization", "Bearer ${prefs.token}")
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.outputStream.use { it.write(bytes) }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
                val json = try { if (text.isBlank()) null else JSONObject(text) } catch (e: Exception) { null }
                ApiReply(code, json)
            } finally {
                conn.disconnect()
            }
        } catch (e: IOException) {
            ApiReply(null, null)
        }
    }
}
