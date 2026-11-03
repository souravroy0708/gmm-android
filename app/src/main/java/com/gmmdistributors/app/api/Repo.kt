package com.gmmdistributors.app.api

import android.content.Context
import com.gmmdistributors.app.LocalDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

sealed class Res<out T> {
    data class Ok<T>(val value: T) : Res<T>()
    data class Fail(val message: String, val authExpired: Boolean = false, val offline: Boolean = false) : Res<Nothing>()
}

data class Loaded<T>(val value: T, val offline: Boolean)

/**
 * Talks to the live Django API and keeps the last good answers in the phone's SQLite cache,
 * so the shop and past orders still show when the network drops. The server is always the
 * source of truth for prices, stock, orders, payments and invoices.
 */
class Repo(context: Context) {
    private val ctx = context.applicationContext
    private val api = Api(ctx)
    private val db = LocalDb.get(ctx)
    private val secrets = SecretStore(ctx)

    val signedIn: Boolean get() = secrets.token.isNotBlank()

    private fun offlineMsg(lang: String) = if (lang == "bn") "ইন্টারনেট সংযোগ নেই। আবার চেষ্টা করুন।"
        else "No internet connection. Please try again."

    private fun <T> fail(r: ApiReply, lang: String, fallback: String): Res<T> = when {
        r.offline -> Res.Fail(offlineMsg(lang), offline = true)
        r.authExpired -> Res.Fail(if (lang == "bn") "আবার সাইন ইন করুন।" else "Please sign in again.", authExpired = true)
        else -> Res.Fail(r.error ?: fallback)
    }

    // ---------- catalogue (offline-first) ----------
    // Order of truth on the phone: SQLite snapshot (last download) -> catalogue bundled in the APK.
    // Online refresh sends the snapshot's version; an unchanged catalogue costs a ~60-byte reply.

    private fun seedCatalog(): CatalogPayload? = runCatching {
        ctx.assets.open("catalog_seed.json").bufferedReader(Charsets.UTF_8).use { CatalogPayload.from(JSONObject(it.readText())) }
    }.getOrNull()

    suspend fun cachedCatalog(): CatalogPayload? = withContext(Dispatchers.IO) {
        db.getCache("catalog")?.let { runCatching { CatalogPayload.from(JSONObject(it)) }.getOrNull() } ?: seedCatalog()
    }

    /** When the prices on this phone were last confirmed by the server (null = only the bundled copy). */
    suspend fun catalogCheckedAt(): Long? = withContext(Dispatchers.IO) { db.cacheTime("catalog_checked") }

    suspend fun loadCatalog(): Loaded<CatalogPayload?> {
        val current = cachedCatalog()
        val r = api.post("catalog/", JSONObject().put("version", current?.version ?: ""))
        if (r.ok && r.json != null) {
            if (r.json.optBoolean("unchanged") && current != null) {
                withContext(Dispatchers.IO) { db.putCache("catalog_checked", "1") }
                return Loaded(current, offline = false)
            }
            val parsed = runCatching { CatalogPayload.from(r.json) }.getOrNull()
            if (parsed != null && parsed.products.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    db.putCache("catalog", r.json.toString())
                    db.putCache("catalog_checked", "1")
                }
                return Loaded(parsed, offline = false)
            }
        }
        return Loaded(current, offline = r.offline)
    }

    /** Live price / stock / delivery check right before an order is sent. */
    suspend fun quote(lines: List<CartLine>, pincode: String, lang: String): Res<Quote> {
        val items = JSONArray()
        lines.forEach { items.put(JSONObject().put("variant_id", it.variant.id).put("quantity", it.quantity).put("price", it.variant.price)) }
        val r = api.post("quote/", JSONObject().put("items", items).put("pincode", pincode))
        if (r.ok && r.json != null) return Res.Ok(Quote.from(r.json))
        return fail(r, lang, "Could not check today's prices.")
    }

    // ---------- account ----------

    private suspend fun storeSession(j: JSONObject): Me? {
        val token = j.optString("token")
        val secret = j.optString("secret")
        if (token.isBlank() || secret.isBlank()) return null
        secrets.token = token
        secrets.secret = secret
        val me = Me.from(j.optJSONObject("me"))
        withContext(Dispatchers.IO) { me?.let { db.putCache("me", it.toJson().toString()) } }
        return me
    }

    /** Makes sure this phone has a (guest) session so it can order without a sign-in screen. */
    suspend fun ensureSession(lang: String): Res<Me> {
        if (signedIn) cachedMe()?.let { return Res.Ok(it) }
        if (signedIn) return refreshMe(lang)
        val r = api.post("auth/device/", JSONObject().put("device", deviceName()).put("app_version", appVersion()).put("language", lang))
        if (r.ok && r.json != null) storeSession(r.json)?.let { return Res.Ok(it) }
        return fail(r, lang, "Could not connect to GMM.")
    }

    suspend fun cachedMe(): Me? = withContext(Dispatchers.IO) {
        if (!signedIn) null else db.getCache("me")?.let { runCatching { Me.from(JSONObject(it)) }.getOrNull() }
    }

    suspend fun register(name: String, phone: String, password: String, lang: String): Res<Me> {
        val r = api.post("auth/register/", JSONObject().put("name", name).put("phone", phone)
            .put("password", password).put("language", lang).put("device", deviceName()).put("app_version", appVersion()))
        if (r.ok && r.json != null) storeSession(r.json)?.let { return Res.Ok(it) }
        return fail(r, lang, "Could not create your account.")
    }

    suspend fun login(phone: String, password: String, lang: String): Res<Me> {
        val r = api.post("auth/login/", JSONObject().put("phone", phone).put("password", password)
            .put("device", deviceName()).put("app_version", appVersion()))
        if (r.ok && r.json != null) storeSession(r.json)?.let { return Res.Ok(it) }
        return fail(r, lang, "Could not sign in.")
    }

    suspend fun refreshMe(lang: String): Res<Me> {
        val r = api.post("me/")
        val me = Me.from(r.json?.optJSONObject("me"))
        if (r.ok && me != null) {
            withContext(Dispatchers.IO) { db.putCache("me", me.toJson().toString()) }
            return Res.Ok(me)
        }
        return fail(r, lang, "Could not load your account.")
    }

    suspend fun updateMe(name: String, email: String, lang: String): Res<Me> {
        val r = api.post("me/", JSONObject().put("name", name).put("email", email).put("language", lang))
        val me = Me.from(r.json?.optJSONObject("me"))
        if (r.ok && me != null) {
            withContext(Dispatchers.IO) { db.putCache("me", me.toJson().toString()) }
            return Res.Ok(me)
        }
        return fail(r, lang, "Could not save your details.")
    }

    suspend fun logout() {
        if (signedIn) api.post("auth/logout/")  // best effort; the phone forgets the token either way
        clearSession()
    }

    suspend fun deleteAccount(lang: String): Res<Unit> {
        val r = api.post("me/delete/", JSONObject().put("confirm", "DELETE"))
        if (r.ok) { clearSession(); return Res.Ok(Unit) }
        return fail(r, lang, "Could not delete your account.")
    }

    suspend fun clearSession() {
        secrets.clear()
        withContext(Dispatchers.IO) { db.wipeAccountData() }
    }

    // ---------- guest details (kept on the phone; sent with each order) ----------

    suspend fun guestAddress(): Address? = withContext(Dispatchers.IO) {
        db.getCache("guest_address")?.let { runCatching { Address.from(JSONObject(it)) }.getOrNull() }
    }

    suspend fun saveGuestAddress(a: Address) = withContext(Dispatchers.IO) { db.putCache("guest_address", a.toJson().toString()) }

    // ---------- addresses ----------

    private suspend fun saveAddressCache(arr: JSONArray?): List<Address> {
        val list = arr.mapObjects { Address.from(it) }
        withContext(Dispatchers.IO) { db.putCache("addresses", (arr ?: JSONArray()).toString()) }
        return list
    }

    suspend fun cachedAddresses(): List<Address> = withContext(Dispatchers.IO) {
        db.getCache("addresses")?.let { runCatching { JSONArray(it).mapObjects { o -> Address.from(o) } }.getOrNull() } ?: emptyList()
    }

    suspend fun loadAddresses(lang: String): Res<List<Address>> {
        val r = api.post("addresses/")
        if (r.ok && r.json != null) return Res.Ok(saveAddressCache(r.json.optJSONArray("addresses")))
        return fail(r, lang, "Could not load addresses.")
    }

    suspend fun saveAddress(a: Address, lang: String): Res<Pair<Address, List<Address>>> {
        val body = a.toJson()
        if (a.id <= 0L) body.remove("id")
        val r = api.post("addresses/save/", body)
        if (r.ok && r.json != null) {
            val saved = Address.from(r.json.getJSONObject("address"))
            return Res.Ok(saved to saveAddressCache(r.json.optJSONArray("addresses")))
        }
        return fail(r, lang, "Could not save the address.")
    }

    suspend fun deleteAddress(id: Long, lang: String): Res<List<Address>> {
        val r = api.post("addresses/delete/", JSONObject().put("id", id))
        if (r.ok && r.json != null) return Res.Ok(saveAddressCache(r.json.optJSONArray("addresses")))
        return fail(r, lang, "Could not remove the address.")
    }

    suspend fun setDefaultAddress(id: Long, lang: String): Res<List<Address>> {
        val r = api.post("addresses/default/", JSONObject().put("id", id))
        if (r.ok && r.json != null) return Res.Ok(saveAddressCache(r.json.optJSONArray("addresses")))
        return fail(r, lang, "Could not update the address.")
    }

    // ---------- orders ----------

    suspend fun cachedOrders(): List<Order> = withContext(Dispatchers.IO) { db.orders() }

    /** Saves server orders, keeping the phone-only "sent on WhatsApp" flag. */
    private suspend fun keep(order: Order): Order = withContext(Dispatchers.IO) {
        val sent = db.orderByNo(order.orderNo)?.whatsappSent ?: false
        val merged = order.copy(whatsappSent = order.whatsappSent || sent)
        db.saveOrder(merged)
        merged
    }

    suspend fun loadOrders(lang: String): Res<List<Order>> {
        val r = api.post("orders/")
        if (r.ok && r.json != null) {
            val list = r.json.optJSONArray("orders").mapObjects { Order.from(it) }.map { keep(it) }
            withContext(Dispatchers.IO) { db.keepOnlyOrders(list.map { it.orderNo }) }
            return Res.Ok(list)
        }
        return fail(r, lang, "Could not load your orders.")
    }

    private suspend fun orderCall(path: String, body: JSONObject, lang: String, fallback: String): Res<Order> {
        val r = api.post(path, body)
        if (r.ok && r.json != null) {
            val o = r.json.optJSONObject("order") ?: return Res.Fail(fallback)
            return Res.Ok(keep(Order.from(o)))
        }
        return fail(r, lang, fallback)
    }

    /** Online order. Members send a saved address id; guests send the address itself. */
    suspend fun placeOrder(
        address: Address,
        cartLines: List<CartLine>,
        paymentMethod: String,
        note: String,
        idempotencyKey: String,
        expectedTotal: Double,
        lang: String,
    ): Res<Order> {
        val itemsArr = JSONArray()
        cartLines.forEach { line -> itemsArr.put(JSONObject().put("variant_id", line.variant.id).put("quantity", line.quantity)) }
        val body = JSONObject()
            .put("items", itemsArr)
            .put("payment_method", paymentMethod)
            .put("note", note)
            .put("idempotency_key", idempotencyKey)
            .put("expected_total", expectedTotal)
        if (address.id > 0L) body.put("address_id", address.id)
        else body.put("address", address.toJson().apply { remove("id"); remove("is_default") })
        val res = orderCall("orders/place/", body, lang, "Could not place the order. Please try again.")
        if (res is Res.Ok) withContext(Dispatchers.IO) { db.clearCart() }
        return res
    }

    // ---------- orders made offline ----------

    suspend fun pendingOrders(): List<PendingOrder> = withContext(Dispatchers.IO) { db.pendingOrders() }

    suspend fun queueOrder(p: PendingOrder) = withContext(Dispatchers.IO) {
        db.savePending(p)
        db.clearCart()
    }

    suspend fun deletePending(key: String) = withContext(Dispatchers.IO) { db.deletePending(key) }

    data class FlushResult(val sent: List<Order>, val failed: Int, val stillWaiting: Int)

    /**
     * Sends every waiting order. Used by the app on reconnect and by SyncWorker in the background.
     * Network problem -> keep waiting (retry later). Business problem (e.g. sold out) -> mark failed
     * with the server's message; the customer decides (edit and retry, or delete).
     */
    suspend fun flushPending(lang: String, includeFailed: Boolean = false): FlushResult {
        val queue = pendingOrders().filter { it.status == "waiting" || includeFailed }.sortedBy { it.createdAt }
        if (queue.isEmpty()) return FlushResult(emptyList(), 0, 0)
        if (ensureSession(lang) !is Res.Ok) return FlushResult(emptyList(), 0, queue.size)
        val sent = ArrayList<Order>()
        var failed = 0
        for ((i, p) in queue.withIndex()) {
            var r = api.post("orders/place/", p.placeBody())
            if (r.authExpired) {  // e.g. guest account removed on the server: start a fresh guest session once
                clearSession()
                if (ensureSession(lang) is Res.Ok) r = api.post("orders/place/", p.placeBody())
            }
            when {
                r.offline -> return FlushResult(sent, failed, queue.size - i)
                r.ok && r.json?.optJSONObject("order") != null -> {
                    sent.add(keep(Order.from(r.json.getJSONObject("order"))))
                    deletePending(p.key)
                }
                else -> {
                    failed++
                    withContext(Dispatchers.IO) { db.savePending(p.copy(status = "failed", error = r.error ?: "Could not send this order.")) }
                }
            }
        }
        return FlushResult(sent, failed, 0)
    }

    suspend fun refreshOrder(orderNo: String, lang: String): Res<Order> =
        orderCall("orders/detail/", JSONObject().put("order_no", orderNo), lang, "Could not load the order.")

    suspend fun cancelOrder(orderNo: String, lang: String): Res<Order> =
        orderCall("orders/cancel/", JSONObject().put("order_no", orderNo), lang, "Could not cancel the order.")

    suspend fun submitUtr(orderNo: String, utr: String, lang: String): Res<Order> =
        orderCall("orders/utr/", JSONObject().put("order_no", orderNo).put("utr", utr), lang, "Could not send the payment reference.")

    suspend fun markWhatsappSent(orderNo: String) = withContext(Dispatchers.IO) {
        db.orderByNo(orderNo)?.let { db.saveOrder(it.copy(whatsappSent = true)) }
    }

    suspend fun loadInvoice(orderNo: String, lang: String): Res<Invoice> {
        val r = api.post("orders/invoice/", JSONObject().put("order_no", orderNo))
        if (r.ok && r.json != null) {
            val inv = r.json.optJSONObject("invoice") ?: return Res.Fail("Invoice not available.")
            withContext(Dispatchers.IO) { db.putCache("invoice:$orderNo", inv.toString()) }
            return Res.Ok(Invoice.from(inv))
        }
        if (r.offline) {
            withContext(Dispatchers.IO) { db.getCache("invoice:$orderNo") }?.let { cached ->
                return Res.Ok(Invoice.from(JSONObject(cached)))
            }
        }
        return fail(r, lang, "Invoice not available yet.")
    }

    private fun deviceName(): String = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}".take(80)
    private fun appVersion(): String = com.gmmdistributors.app.BuildConfig.VERSION_NAME
}
