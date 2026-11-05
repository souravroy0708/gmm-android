package com.gmmdistributors.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.gmmdistributors.app.api.Address
import com.gmmdistributors.app.api.Order
import org.json.JSONObject

/**
 * Built-in Android SQLite database (`gmm_organic.db`) following the exact Bas1Minute LocalDb pattern.
 * Stores:
 * - `cart`: variant_id -> quantity
 * - `addresses`: legacy table (addresses now live on the server, cached under `cache`)
 * - `orders`: offline copy of the customer's server orders
 * - `cache`: last good JSON responses from the Django server (catalogue, addresses, account, invoices)
 * - `outbox`: offline actions waiting to sync when connected to live Django backend
 */
class LocalDb private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "gmm_organic.db", null, 3) {

    companion object {
        @Volatile private var instance: LocalDb? = null

        fun get(context: Context): LocalDb =
            instance ?: synchronized(this) { instance ?: LocalDb(context).also { instance = it } }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cart (
                variant_id INTEGER PRIMARY KEY,
                quantity INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS addresses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                phone TEXT NOT NULL,
                line1 TEXT NOT NULL,
                village_town TEXT NOT NULL,
                district TEXT NOT NULL,
                state TEXT NOT NULL,
                pincode TEXT NOT NULL,
                landmark TEXT NOT NULL,
                is_default INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS orders (
                order_no TEXT PRIMARY KEY,
                status TEXT NOT NULL,
                payment_method TEXT NOT NULL,
                payment_status TEXT NOT NULL,
                utr_reference TEXT NOT NULL DEFAULT '',
                whatsapp_sent INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                payload_json TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE TABLE IF NOT EXISTS cache (key TEXT PRIMARY KEY, body TEXT NOT NULL, fetched_at INTEGER NOT NULL)")
        // v2: orders made offline, sent by SyncWorker / on reconnect. key = server idempotency key.
        db.execSQL("CREATE TABLE IF NOT EXISTS pending_orders (key TEXT PRIMARY KEY, body TEXT NOT NULL, created_at INTEGER NOT NULL)")
        // v3: problem reports written offline (screenshot kept as a file until sent)
        db.execSQL("CREATE TABLE IF NOT EXISTS pending_feedback (key TEXT PRIMARY KEY, body TEXT NOT NULL, created_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS outbox (id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT NOT NULL, body TEXT NOT NULL, created_at INTEGER NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }

    // ---------- Cart ----------

    @Synchronized
    fun cartMap(): Map<Int, Int> {
        val out = LinkedHashMap<Int, Int>()
        readableDatabase.rawQuery("SELECT variant_id, quantity FROM cart ORDER BY rowid", null).use { c ->
            while (c.moveToNext()) {
                out[c.getInt(0)] = c.getInt(1)
            }
        }
        return out
    }

    @Synchronized
    fun setCartQty(variantId: Int, qty: Int) {
        if (qty <= 0) {
            writableDatabase.delete("cart", "variant_id = ?", arrayOf(variantId.toString()))
        } else {
            writableDatabase.insertWithOnConflict(
                "cart",
                null,
                ContentValues().apply {
                    put("variant_id", variantId)
                    put("quantity", qty.coerceAtMost(10))
                },
                SQLiteDatabase.CONFLICT_REPLACE
            )
        }
    }

    @Synchronized
    fun clearCart() {
        writableDatabase.delete("cart", null, null)
    }

    // ---------- Addresses ----------

    @Synchronized
    fun addresses(): List<Address> {
        val list = ArrayList<Address>()
        readableDatabase.rawQuery(
            "SELECT id, full_name, phone, line1, village_town, district, state, pincode, landmark, is_default FROM addresses ORDER BY is_default DESC, id DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    Address(
                        id = c.getLong(0),
                        fullName = c.getString(1),
                        phone = c.getString(2),
                        line1 = c.getString(3),
                        villageTown = c.getString(4),
                        district = c.getString(5),
                        state = c.getString(6),
                        pincode = c.getString(7),
                        landmark = c.getString(8),
                        isDefault = c.getInt(9) == 1,
                    )
                )
            }
        }
        return list
    }

    @Synchronized
    fun saveAddress(a: Address): Long {
        val db = writableDatabase
        if (a.isDefault) {
            db.execSQL("UPDATE addresses SET is_default = 0")
        }
        val cv = ContentValues().apply {
            put("full_name", a.fullName.trim())
            put("phone", a.phone.trim())
            put("line1", a.line1.trim())
            put("village_town", a.villageTown.trim())
            put("district", a.district.trim())
            put("state", a.state.trim())
            put("pincode", a.pincode.trim())
            put("landmark", a.landmark.trim())
            put("is_default", if (a.isDefault) 1 else 0)
        }
        return if (a.id > 0L) {
            db.update("addresses", cv, "id = ?", arrayOf(a.id.toString()))
            a.id
        } else {
            db.insert("addresses", null, cv)
        }
    }

    @Synchronized
    fun deleteAddress(id: Long) {
        writableDatabase.delete("addresses", "id = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun setDefaultAddress(id: Long) {
        val db = writableDatabase
        db.execSQL("UPDATE addresses SET is_default = 0")
        db.execSQL("UPDATE addresses SET is_default = 1 WHERE id = ?", arrayOf(id.toString()))
    }

    // ---------- Orders ----------

    @Synchronized
    fun saveOrder(order: Order) {
        val json = order.toJson().toString()
        writableDatabase.insertWithOnConflict(
            "orders",
            null,
            ContentValues().apply {
                put("order_no", order.orderNo)
                put("status", order.status)
                put("payment_method", order.paymentMethod)
                put("payment_status", order.paymentStatus)
                put("utr_reference", order.utrReference)
                put("whatsapp_sent", if (order.whatsappSent) 1 else 0)
                put("created_at", orderSortKey(order))
                put("payload_json", json)
            },
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    private fun orderSortKey(order: Order): Long =
        runCatching { java.time.OffsetDateTime.parse(order.createdAtIso).toInstant().toEpochMilli() }
            .getOrDefault(System.currentTimeMillis())

    @Synchronized
    fun orders(): List<Order> {
        val list = ArrayList<Order>()
        readableDatabase.rawQuery("SELECT payload_json FROM orders ORDER BY created_at DESC", null).use { c ->
            while (c.moveToNext()) {
                runCatching { Order.from(JSONObject(c.getString(0))) }.getOrNull()?.let { list.add(it) }
            }
        }
        return list
    }

    @Synchronized
    fun orderByNo(orderNo: String): Order? =
        readableDatabase.rawQuery("SELECT payload_json FROM orders WHERE order_no = ?", arrayOf(orderNo)).use { c ->
            if (c.moveToFirst()) runCatching { Order.from(JSONObject(c.getString(0))) }.getOrNull() else null
        }

    // ---------- Pending (offline) orders ----------

    @Synchronized
    fun savePending(p: com.gmmdistributors.app.api.PendingOrder) {
        writableDatabase.insertWithOnConflict(
            "pending_orders", null,
            ContentValues().apply { put("key", p.key); put("body", p.toJson().toString()); put("created_at", p.createdAt) },
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    @Synchronized
    fun pendingOrders(): List<com.gmmdistributors.app.api.PendingOrder> {
        val list = ArrayList<com.gmmdistributors.app.api.PendingOrder>()
        readableDatabase.rawQuery("SELECT body FROM pending_orders ORDER BY created_at DESC", null).use { c ->
            while (c.moveToNext()) runCatching { com.gmmdistributors.app.api.PendingOrder.from(JSONObject(c.getString(0))) }.getOrNull()?.let { list.add(it) }
        }
        return list
    }

    @Synchronized
    fun deletePending(key: String) {
        writableDatabase.delete("pending_orders", "key = ?", arrayOf(key))
    }

    // ---------- Pending problem reports ----------

    @Synchronized
    fun savePendingFeedback(f: com.gmmdistributors.app.api.PendingFeedback) {
        writableDatabase.insertWithOnConflict("pending_feedback", null,
            ContentValues().apply { put("key", f.key); put("body", f.toJson().toString()); put("created_at", f.createdAt) },
            SQLiteDatabase.CONFLICT_REPLACE)
    }

    @Synchronized
    fun pendingFeedback(): List<com.gmmdistributors.app.api.PendingFeedback> {
        val list = ArrayList<com.gmmdistributors.app.api.PendingFeedback>()
        readableDatabase.rawQuery("SELECT body FROM pending_feedback ORDER BY created_at", null).use { c ->
            while (c.moveToNext()) runCatching { com.gmmdistributors.app.api.PendingFeedback.from(JSONObject(c.getString(0))) }.getOrNull()?.let { list.add(it) }
        }
        return list
    }

    @Synchronized
    fun deletePendingFeedback(key: String) {
        writableDatabase.delete("pending_feedback", "key = ?", arrayOf(key))
    }

    // ---------- Cache ----------

    @Synchronized
    fun putCache(key: String, body: String) {
        writableDatabase.insertWithOnConflict(
            "cache",
            null,
            ContentValues().apply {
                put("key", key)
                put("body", body)
                put("fetched_at", System.currentTimeMillis())
            },
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    @Synchronized
    fun cacheTime(key: String): Long? =
        readableDatabase.rawQuery("SELECT fetched_at FROM cache WHERE key = ?", arrayOf(key)).use { c ->
            if (c.moveToFirst()) c.getLong(0) else null
        }

    @Synchronized
    fun getCache(key: String): String? =
        readableDatabase.rawQuery("SELECT body FROM cache WHERE key = ?", arrayOf(key)).use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }

    /** Keeps the cached order list in step with the server (orders are never deleted there, but
     *  another account may have used this phone before). */
    @Synchronized
    fun keepOnlyOrders(orderNos: List<String>) {
        if (orderNos.isEmpty()) {
            writableDatabase.delete("orders", null, null)
            return
        }
        val marks = orderNos.joinToString(",") { "?" }
        writableDatabase.delete("orders", "order_no NOT IN ($marks)", orderNos.toTypedArray())
    }

    /** Sign-out: forget everything tied to the account but keep the public catalogue cache and cart. */
    @Synchronized
    fun wipeAccountData() {
        writableDatabase.delete("orders", null, null)
        writableDatabase.delete("addresses", null, null)
        writableDatabase.delete("outbox", null, null)
        writableDatabase.delete("cache", "key NOT IN (?, ?, ?, ?, ?)", arrayOf("catalog", "catalog_checked", "guest_address", "wishlist", "recent"))
    }

    @Synchronized
    fun wipeAll() {
        for (t in listOf("cart", "addresses", "orders", "cache", "outbox", "pending_orders", "pending_feedback")) {
            writableDatabase.delete(t, null, null)
        }
    }
}
