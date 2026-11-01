package com.gmmdistributors.app.api

import android.content.Context
import com.gmmdistributors.app.DummyCatalog
import com.gmmdistributors.app.LocalDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

sealed class Res<out T> {
    data class Ok<T>(val value: T) : Res<T>()
    data class Fail(val message: String) : Res<Nothing>()
}

data class Loaded<T>(val value: T, val offline: Boolean)

class Repo(context: Context) {
    private val ctx = context.applicationContext
    private val api = Api(ctx)
    private val db = LocalDb.get(ctx)

    suspend fun loadCatalog(): Loaded<CatalogPayload> {
        val r = api.get("catalog/")
        if (r.ok && r.json != null) {
            withContext(Dispatchers.IO) { db.putCache("catalog", r.json.toString()) }
            val parsed = runCatching { CatalogPayload.from(r.json) }.getOrNull()
            if (parsed != null) return Loaded(parsed, offline = false)
        }
        val cached = withContext(Dispatchers.IO) {
            db.getCache("catalog")?.let { runCatching { CatalogPayload.from(JSONObject(it)) }.getOrNull() }
        }
        return Loaded(cached ?: DummyCatalog.payload(), offline = !r.ok)
    }

    suspend fun loadOrders(): List<Order> {
        val r = api.get("orders/")
        if (r.ok && r.json != null) {
            val arr = r.json.optJSONArray("orders")
            val list = arr.mapObjects { Order.from(it) }
            withContext(Dispatchers.IO) {
                list.forEach { db.saveOrder(it) }
            }
            return list
        }
        return withContext(Dispatchers.IO) { db.orders() }
    }

    suspend fun placeOrder(
        address: Address,
        cartLines: List<CartLine>,
        paymentMethod: String,
        note: String,
    ): Res<Order> {
        val itemsArr = JSONArray()
        cartLines.forEach { line ->
            itemsArr.put(
                JSONObject()
                    .put("variant_id", line.variant.id)
                    .put("quantity", line.quantity)
            )
        }
        val body = JSONObject()
            .put("address", address.toJson())
            .put("items", itemsArr)
            .put("payment_method", paymentMethod)
            .put("note", note)

        val r = api.post("orders/place/", body)
        return if (r.ok && r.json != null) {
            val order = Order.from(r.json.getJSONObject("order"))
            withContext(Dispatchers.IO) {
                db.saveOrder(order)
                db.clearCart()
            }
            Res.Ok(order)
        } else {
            Res.Fail(r.error ?: "Could not place order. Please check your details and try again.")
        }
    }

    suspend fun cancelOrder(orderNo: String): Res<Order> {
        val r = api.post("orders/cancel/", JSONObject().put("order_no", orderNo))
        return if (r.ok && r.json != null) {
            val order = Order.from(r.json.getJSONObject("order"))
            withContext(Dispatchers.IO) { db.saveOrder(order) }
            Res.Ok(order)
        } else {
            Res.Fail(r.error ?: "Could not cancel order.")
        }
    }

    suspend fun submitUtr(orderNo: String, utr: String): Res<Order> {
        val r = api.post("orders/utr/", JSONObject().put("order_no", orderNo).put("utr", utr))
        return if (r.ok && r.json != null) {
            val order = Order.from(r.json.getJSONObject("order"))
            withContext(Dispatchers.IO) { db.saveOrder(order) }
            Res.Ok(order)
        } else {
            Res.Fail(r.error ?: "Could not save payment reference.")
        }
    }

    suspend fun markWhatsappSent(orderNo: String): Res<Order> {
        val r = api.post("orders/whatsapp-sent/", JSONObject().put("order_no", orderNo))
        return if (r.ok && r.json != null) {
            val order = Order.from(r.json.getJSONObject("order"))
            withContext(Dispatchers.IO) { db.saveOrder(order) }
            Res.Ok(order)
        } else {
            Res.Fail(r.error ?: "Could not update order.")
        }
    }
}
