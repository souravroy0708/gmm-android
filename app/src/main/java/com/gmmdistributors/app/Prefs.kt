package com.gmmdistributors.app

import android.content.Context

/**
 * SharedPreferences store for GMM Organic App.
 * Mirrors the Bas1Minute Prefs pattern. The sign-in token lives in SecretStore, not here.
 */
class Prefs(context: Context) {
    private val p = context.getSharedPreferences("gmm_organic_prefs", Context.MODE_PRIVATE)

    /** Django server. Release builds always use BuildConfig.DEFAULT_SERVER; debug builds can point
     *  at a laptop on Wi-Fi (Profile -> Developer settings), e.g. http://192.168.1.5:8000 */
    var serverUrl: String
        get() = if (BuildConfig.DEBUG) (p.getString("server_url", null)?.takeIf { it.isNotBlank() } ?: BuildConfig.DEFAULT_SERVER)
        else BuildConfig.DEFAULT_SERVER
        set(v) = p.edit().putString("server_url", v.trim().trimEnd('/')).apply()

    var ownerWhatsapp: String
        get() = p.getString("owner_whatsapp", null)?.takeIf { it.isNotBlank() } ?: BuildConfig.OWNER_WHATSAPP
        set(v) = p.edit().putString("owner_whatsapp", v.filter { it.isDigit() }).apply()

    var supportPhone: String
        get() = p.getString("support_phone", null)?.takeIf { it.isNotBlank() } ?: BuildConfig.SUPPORT_PHONE
        set(v) = p.edit().putString("support_phone", v.trim()).apply()

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

    /** "en" or "bn". Bengali by default: most customers are farmers in West Bengal (same as the website). */
    var language: String
        get() = p.getString("language", "bn") ?: "bn"
        set(v) = p.edit().putString("language", v).apply()

    /** "system", "light" or "dark". */
    var theme: String
        get() = p.getString("theme", "light") ?: "light"
        set(v) = p.edit().putString("theme", v).apply()

    /** Invite code from a Krishi Mitra link (shop.gmmdistributors.xyz/r/CODE), used on the next order. */
    var inviteCode: String
        get() = p.getString("invite_code", "") ?: ""
        set(v) = p.edit().putString("invite_code", v.trim().uppercase().take(12)).apply()

    /** Firebase token of this phone (sent to the server for notifications). */
    var pushToken: String
        get() = p.getString("push_token", "") ?: ""
        set(v) = p.edit().putString("push_token", v).apply()

    /** Offers & Krishi tips notifications (order updates always arrive). */
    var promoPush: Boolean
        get() = p.getBoolean("promo_push", true)
        set(v) = p.edit().putBoolean("promo_push", v).apply()

    /** First launch: show the big বাংলা / English picker once. */
    var onboarded: Boolean
        get() = p.getBoolean("onboarded", false)
        set(v) = p.edit().putBoolean("onboarded", v).apply()

    var onboardingNotifAsked: Boolean
        get() = p.getBoolean("onb_notif", false)
        set(v) = p.edit().putBoolean("onb_notif", v).apply()

    /** Google Play review asked once, after the first delivered order. */
    var reviewAsked: Boolean
        get() = p.getBoolean("review_asked", false)
        set(v) = p.edit().putBoolean("review_asked", v).apply()

    fun wipeAll() {
        p.edit().clear().apply()
    }
}
