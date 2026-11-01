package com.gmmdistributors.app.api

import android.content.Context
import com.gmmdistributors.app.DummyCatalog
import com.gmmdistributors.app.LocalDb
import com.gmmdistributors.app.Prefs
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * In-app Dummy REST API Server that emulates the future Django `/api/v1/` endpoints.
 * Enforces the exact business rules from GMM's `apps/orders/services.py`:
 * - GST-inclusive pricing split into `taxable_value` and `tax_amount`
 * - Max 10 cart lines, max 10 quantity per variant
 * - Serviceable PIN code validation & delivery fee calculation (free delivery above Rs. 1000)
 * - Gap-free `GMM-YYMMDD-XXXX` order numbering
 * - Offline payment statuses (`AWAITING_WHATSAPP_PAYMENT`, `UTR_SUBMITTED`, `COD_DUE`)
 */
object DummyServer {

    private fun round2(v: Double): Double = (v * 100.0).roundToInt() / 100.0

    suspend fun handle(context: Context, path: String, body: JSONObject): ApiReply {
        delay(140) // realistic snappy API response
        val db = LocalDb.get(context)
        val prefs = Prefs(context)

        return when {
            path == "catalog/" -> {
                ApiReply(200, DummyCatalog.payload().toJson())
            }

            path == "orders/" -> {
                val arr = JSONArray(db.orders().map { it.toJson() })
                ApiReply(200, JSONObject().put("orders", arr))
            }

            path == "orders/place/" -> {
                placeOrder(db, prefs, body)
            }

            path == "orders/cancel/" -> {
                val orderNo = body.optString("order_no")
                val existing = db.orderByNo(orderNo)
                    ?: return ApiReply(404, JSONObject().put("error", "Order not found."))
                if (!existing.canCancel) {
                    return ApiReply(400, JSONObject().put("error", "Only PLACED orders can be cancelled."))
                }
                val updated = existing.copy(status = "CANCELLED")
                db.saveOrder(updated)
                ApiReply(200, JSONObject().put("order", updated.toJson()))
            }

            path == "orders/utr/" -> {
                val orderNo = body.optString("order_no")
                val utr = body.optString("utr").trim()
                if (utr.length < 4) {
                    return ApiReply(400, JSONObject().put("error", "Please enter a valid UPI UTR / Bank reference number."))
                }
                val existing = db.orderByNo(orderNo)
                    ?: return ApiReply(404, JSONObject().put("error", "Order not found."))
                val updated = existing.copy(
                    utrReference = utr,
                    paymentStatus = "UTR_SUBMITTED"
                )
                db.saveOrder(updated)
                ApiReply(200, JSONObject().put("order", updated.toJson()))
            }

            path == "orders/whatsapp-sent/" -> {
                val orderNo = body.optString("order_no")
                val existing = db.orderByNo(orderNo)
                    ?: return ApiReply(404, JSONObject().put("error", "Order not found."))
                val updated = existing.copy(whatsappSent = true)
                db.saveOrder(updated)
                ApiReply(200, JSONObject().put("order", updated.toJson()))
            }

            else -> ApiReply(404, JSONObject().put("error", "Unknown endpoint: /api/v1/$path"))
        }
    }

    private fun placeOrder(db: LocalDb, prefs: Prefs, body: JSONObject): ApiReply {
        val addressJson = body.optJSONObject("address")
            ?: return ApiReply(400, JSONObject().put("error", "Please select or add a delivery address."))
        val address = Address.from(addressJson)
        val paymentMethod = body.optString("payment_method", "UPI")
        val note = body.optString("note", "").trim()
        val itemsArr = body.optJSONArray("items")
            ?: return ApiReply(400, JSONObject().put("error", "Your cart is empty."))

        if (itemsArr.length() == 0) {
            return ApiReply(400, JSONObject().put("error", "Your cart is empty."))
        }
        if (itemsArr.length() > 10) {
            return ApiReply(400, JSONObject().put("error", "An order can have at most 10 different products."))
        }

        val allVariants = DummyCatalog.PRODUCTS.flatMap { p -> p.variants.map { v -> v to p } }.associateBy { it.first.id }
        val orderItems = ArrayList<OrderItem>()
        var subtotal = 0.0
        var taxTotal = 0.0

        for (i in 0 until itemsArr.length()) {
            val row = itemsArr.optJSONObject(i) ?: continue
            val variantId = row.optInt("variant_id")
            val qty = row.optInt("quantity", 0)
            if (qty <= 0 || qty > 10) {
                return ApiReply(400, JSONObject().put("error", "Quantity per item must be between 1 and 10."))
            }
            val pair = allVariants[variantId]
                ?: return ApiReply(400, JSONObject().put("error", "One of the selected packs is no longer available."))
            val (variant, product) = pair
            val lineTotal = round2(variant.price * qty)
            val taxable = if (variant.gstRate > 0.0) round2((lineTotal * 100.0) / (100.0 + variant.gstRate)) else lineTotal
            val tax = round2(lineTotal - taxable)
            subtotal = round2(subtotal + lineTotal)
            taxTotal = round2(taxTotal + tax)
            orderItems.add(
                OrderItem(
                    variantId = variant.id,
                    productName = product.name,
                    productNameBn = product.nameBn,
                    variantLabel = variant.label,
                    sku = variant.sku,
                    hsnCode = variant.hsnCode,
                    gstRate = variant.gstRate,
                    unitPrice = variant.price,
                    quantity = qty,
                    taxableValue = taxable,
                    taxAmount = tax,
                    lineTotal = lineTotal,
                )
            )
        }

        val cfg = DummyCatalog.SITE_CONFIG
        if (subtotal < cfg.minOrderValue) {
            return ApiReply(400, JSONObject().put("error", "Minimum order value is ₹${cfg.minOrderValue.toInt()}."))
        }

        val pinObj = DummyCatalog.PINS.firstOrNull { it.pincode == address.pincode }
        val baseDelivery = pinObj?.deliveryFee ?: 50.0
        val deliveryFee = if (subtotal >= cfg.freeDeliveryAbove) 0.0 else baseDelivery
        val total = round2(subtotal + deliveryFee)

        val now = LocalDateTime.now()
        val dayPart = now.format(DateTimeFormatter.ofPattern("yyMMdd", Locale.ENGLISH))
        val seq = prefs.orderSeq
        prefs.orderSeq = seq + 1
        val orderNo = "GMM-$dayPart-${"%04d".format(seq)}"
        val createdPretty = now.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH))

        val paymentStatus = if (paymentMethod == "COD") "COD_DUE" else "AWAITING_WHATSAPP_PAYMENT"
        val order = Order(
            orderNo = orderNo,
            status = "PLACED",
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            utrReference = "",
            address = address,
            items = orderItems,
            subtotal = subtotal,
            taxTotal = taxTotal,
            deliveryFee = deliveryFee,
            total = total,
            customerNote = note,
            createdAt = createdPretty,
            whatsappSent = false,
        )
        db.saveOrder(order)
        db.clearCart()
        return ApiReply(200, JSONObject().put("order", order.toJson()))
    }
}
