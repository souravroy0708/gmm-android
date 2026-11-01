package com.gmmdistributors.app

import android.content.Context

/**
 * SharedPreferences store for GMM Organic App.
 * Mirrors the Bas1Minute Prefs pattern while adding Dummy API vs Live Django Backend switching.
 */
class Prefs(context: Context) {
    private val p = context.getSharedPreferences("gmm_organic_prefs", Context.MODE_PRIVATE)

    var serverUrl: String
        get() = p.getString("server_url", null) ?: BuildConfig.DEFAULT_SERVER
        set(v) = p.edit().putString("server_url", v.trim().trimEnd('/')).apply()

    /**
     * True by default so the app works 100% standalone with the built-in Dummy API right now.
     * Switch off in Profile -> Backend & Developer Settings when the Django REST API is deployed.
     */
    var useDummyApi: Boolean
        get() = p.getBoolean("use_dummy_api", true)
        set(v) = p.edit().putBoolean("use_dummy_api", v).apply()

    var ownerWhatsapp: String
        get() = p.getString("owner_whatsapp", null)?.takeIf { it.isNotBlank() } ?: BuildConfig.OWNER_WHATSAPP
        set(v) = p.edit().putString("owner_whatsapp", v.filter { it.isDigit() }).apply()

    var supportPhone: String
        get() = p.getString("support_phone", null)?.takeIf { it.isNotBlank() } ?: BuildConfig.SUPPORT_PHONE
        set(v) = p.edit().putString("support_phone", v.trim()).apply()

    var token: String
        get() = p.getString("auth_token", "") ?: ""
        set(v) = p.edit().putString("auth_token", v.trim()).apply()

    var customerName: String
        get() = p.getString("customer_name", "") ?: ""
        set(v) = p.edit().putString("customer_name", v.trim()).apply()

    var customerPhone: String
        get() = p.getString("customer_phone", "") ?: ""
        set(v) = p.edit().putString("customer_phone", v.trim()).apply()

    var villageTown: String
        get() = p.getString("village_town", "") ?: ""
        set(v) = p.edit().putString("village_town", v.trim()).apply()

    var pincode: String
        get() = p.getString("pincode", "712601") ?: "712601"
        set(v) = p.edit().putString("pincode", v.filter { it.isDigit() }.take(6)).apply()

    var setupDone: Boolean
        get() = p.getBoolean("setup_done", false)
        set(v) = p.edit().putBoolean("setup_done", v).apply()

    /** "en" or "bn" (English / Bengali). */
    var language: String
        get() = p.getString("language", "en") ?: "en"
        set(v) = p.edit().putString("language", v).apply()

    /** "system", "light" or "dark". */
    var theme: String
        get() = p.getString("theme", "light") ?: "light"
        set(v) = p.edit().putString("theme", v).apply()

    var orderSeq: Int
        get() = p.getInt("order_seq", 1)
        set(v) = p.edit().putInt("order_seq", v).apply()

    fun wipeAll() {
        p.edit().clear().apply()
    }
}
