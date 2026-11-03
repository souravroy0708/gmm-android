package com.gmmdistributors.app.api

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Per-device signing secret encrypted via Android Keystore (AES/GCM/NoPadding).
 * Holds the sign-in token and the HMAC signing secret (same scheme as Bas1Minute).
 */
class SecretStore(context: Context) {
    private val store = context.getSharedPreferences("gmm_secure", Context.MODE_PRIVATE)
    private val alias = "gmm_api_secret_key"

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(
            KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return gen.generateKey()
    }

    private fun read(name: String): String = try {
        val stored = store.getString(name, null)
        if (stored == null) "" else {
            val bytes = Base64.decode(stored, Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
        }
    } catch (e: Exception) {
        ""
    }

    private fun write(name: String, value: String) {
        if (value.isBlank()) { store.edit().remove(name).apply(); return }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val out = cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        store.edit().putString(name, Base64.encodeToString(out, Base64.NO_WRAP)).apply()
    }

    /** Bearer token for `Authorization`, issued by /api/v1/auth/login|register. */
    var token: String
        get() = read("t")
        set(value) = write("t", value)

    /** Per-phone HMAC secret; every signed-in request is signed with it. */
    var secret: String
        get() = read("s")
        set(value) = write("s", value)

    fun clear() = store.edit().clear().apply()
}
