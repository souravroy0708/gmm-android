package com.gmmdistributors.app.api

import org.json.JSONArray
import org.json.JSONObject

inline fun <T> JSONArray?.mapObjects(f: (JSONObject) -> T): List<T> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { optJSONObject(it)?.let(f) }
}

/** Business settings from the Django admin. No payment details are hard-coded in the app. */
data class SiteConfig(
    val businessName: String = "GMM Distributors Private Limited",
    val taglineEn: String = "100% Organic Fertilizer & Bio-Pesticide",
    val taglineBn: String = "১০০% জৈব সার ও জৈব কীটনাশক",
    val legalAddress: String = "",
    val cin: String = "",
    val gstin: String = "",
    val stateName: String = "West Bengal",
    val stateCode: String = "19",
    val supportPhone: String = "",
    val whatsappNumber: String = "",
    val supportEmail: String = "",
    val upiId: String = "",
    val upiPayeeName: String = "",
    val bankAccountName: String = "",
    val bankName: String = "",
    val bankAccountNumber: String = "",
    val bankIfsc: String = "",
    val minOrderValue: Double = 0.0,
    val freeDeliveryAbove: Double? = null,
    val codEnabled: Boolean = true,
    val codMinOrder: Double = 0.0,
    val codMaxOrder: Double? = null,
    val androidMinVersion: Int = 1,
) {
    val upiReady: Boolean get() = upiId.contains("@")
    val bankReady: Boolean get() = bankAccountNumber.isNotBlank() && bankIfsc.isNotBlank()

    fun toJson(): JSONObject = JSONObject()
        .put("business_name", businessName)
        .put("tagline_en", taglineEn)
        .put("tagline_bn", taglineBn)
        .put("legal_address", legalAddress)
        .put("cin", cin)
        .put("gstin", gstin)
        .put("state_name", stateName)
        .put("state_code", stateCode)
        .put("support_phone", supportPhone)
        .put("whatsapp_number", whatsappNumber)
        .put("support_email", supportEmail)
        .put("upi_id", upiId)
        .put("upi_payee_name", upiPayeeName)
        .put("bank_account_name", bankAccountName)
        .put("bank_name", bankName)
        .put("bank_account_number", bankAccountNumber)
        .put("bank_ifsc", bankIfsc)
        .put("min_order_value", minOrderValue)
        .put("free_delivery_above", freeDeliveryAbove ?: JSONObject.NULL)
        .put("cod_enabled", codEnabled)
        .put("cod_min_order", codMinOrder)
        .put("cod_max_order", codMaxOrder ?: JSONObject.NULL)
        .put("android_min_version", androidMinVersion)

    companion object {
        fun from(j: JSONObject?): SiteConfig {
            if (j == null) return SiteConfig()
            return SiteConfig(
                businessName = j.optString("business_name").ifBlank { "GMM Distributors Private Limited" },
                taglineEn = j.optString("tagline_en").ifBlank { "100% Organic Fertilizer & Bio-Pesticide" },
                taglineBn = j.optString("tagline_bn").ifBlank { "১০০% জৈব সার ও জৈব কীটনাশক" },
                legalAddress = j.optString("legal_address"),
                cin = j.optString("cin"),
                gstin = j.optString("gstin"),
                stateName = j.optString("state_name", "West Bengal"),
                stateCode = j.optString("state_code", "19"),
                supportPhone = j.optString("support_phone"),
                whatsappNumber = j.optString("whatsapp_number"),
                supportEmail = j.optString("support_email"),
                upiId = j.optString("upi_id"),
                upiPayeeName = j.optString("upi_payee_name"),
                bankAccountName = j.optString("bank_account_name"),
                bankName = j.optString("bank_name"),
                bankAccountNumber = j.optString("bank_account_number"),
                bankIfsc = j.optString("bank_ifsc"),
                minOrderValue = j.optDouble("min_order_value", 0.0),
                freeDeliveryAbove = j.optNullableDouble("free_delivery_above"),
                codEnabled = j.optBoolean("cod_enabled", true),
                codMinOrder = j.optDouble("cod_min_order", 0.0),
                codMaxOrder = j.optNullableDouble("cod_max_order"),
                androidMinVersion = j.optInt("android_min_version", 1),
            )
        }
    }
}

fun JSONObject.optNullableDouble(key: String): Double? =
    if (has(key) && !isNull(key)) optDouble(key).takeIf { !it.isNaN() } else null

data class Category(
    val id: Int,
    val slug: String,
    val name: String,
    val nameBn: String,
    val sortOrder: Int,
    val kind: String, // "fertilizer" or "pesticide"
) {
    fun localizedName(lang: String): String = if (lang == "bn" && nameBn.isNotBlank()) nameBn else name

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("slug", slug)
        .put("name", name)
        .put("name_bn", nameBn)
        .put("sort_order", sortOrder)
        .put("kind", kind)

    companion object {
        fun from(j: JSONObject) = Category(
            id = j.optInt("id"),
            slug = j.optString("slug"),
            name = j.optString("name"),
            nameBn = j.optString("name_bn"),
            sortOrder = j.optInt("sort_order"),
            kind = j.optString("kind", "fertilizer"),
        )
    }
}

data class ProductVariant(
    val id: Int,
    val productId: Int,
    val label: String,
    val labelBn: String,
    val sku: String,
    val weightKg: Double,
    val price: Double,
    val gstRate: Double,
    val hsnCode: String,
    val stock: Int,
    val isActive: Boolean = true,
    /** Printed MRP from the admin (only sent when higher than the price). */
    val mrp: Double? = null,
) {
    val inStock: Boolean get() = isActive && stock > 0
    val discountPct: Int get() = mrp?.let { if (it > price) (((it - price) / it) * 100).toInt() else 0 } ?: 0
    fun localizedLabel(lang: String): String = if (lang == "bn" && labelBn.isNotBlank()) labelBn else label

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("product_id", productId)
        .put("label", label)
        .put("label_bn", labelBn)
        .put("sku", sku)
        .put("weight_kg", weightKg)
        .put("price", price)
        .put("gst_rate", gstRate)
        .put("hsn_code", hsnCode)
        .put("stock", stock)
        .put("mrp", mrp ?: JSONObject.NULL)
        .put("is_active", isActive)

    companion object {
        fun from(j: JSONObject) = ProductVariant(
            id = j.optInt("id"),
            productId = j.optInt("product_id"),
            label = j.optString("label"),
            labelBn = j.optString("label_bn"),
            sku = j.optString("sku"),
            weightKg = j.optDouble("weight_kg", 1.0),
            price = j.optDouble("price", 0.0),
            gstRate = j.optDouble("gst_rate", 5.0),
            hsnCode = j.optString("hsn_code", "3101"),
            stock = j.optInt("stock", 0),
            mrp = j.optNullableDouble("mrp"),
            isActive = j.optBoolean("is_active", true),
        )
    }
}

data class CropDose(
    val cropName: String,
    val cropNameBn: String,
    val doseMin: Double,
    val doseMax: Double?,
    val unit: String, // "KG_ACRE", "ML_LITRE", "L_ACRE", "G_TREE"
    val labelNote: String,
    val labelNoteBn: String,
) {
    fun localizedCrop(lang: String): String = if (lang == "bn" && cropNameBn.isNotBlank()) cropNameBn else cropName
    fun localizedNote(lang: String): String = if (lang == "bn" && labelNoteBn.isNotBlank()) labelNoteBn else labelNote

    fun unitLabel(lang: String): String = when (unit) {
        "KG_ACRE" -> if (lang == "bn") "কেজি / একর" else "kg / acre"
        "L_ACRE" -> if (lang == "bn") "লিটার / একর" else "L / acre"
        "ML_LITRE" -> if (lang == "bn") "মিলি / লিটার জলে" else "ml / litre water"
        "G_TREE" -> if (lang == "bn") "গ্রাম / গাছ" else "g / tree"
        else -> unit
    }

    fun toJson(): JSONObject = JSONObject()
        .put("crop_name", cropName)
        .put("crop_name_bn", cropNameBn)
        .put("dose_min", doseMin)
        .put("dose_max", doseMax)
        .put("unit", unit)
        .put("label_note", labelNote)
        .put("label_note_bn", labelNoteBn)

    companion object {
        fun from(j: JSONObject) = CropDose(
            cropName = j.optString("crop_name"),
            cropNameBn = j.optString("crop_name_bn"),
            doseMin = j.optDouble("dose_min", 1.0),
            doseMax = if (j.has("dose_max") && !j.isNull("dose_max")) j.optDouble("dose_max") else null,
            unit = j.optString("unit", "KG_ACRE"),
            labelNote = j.optString("label_note"),
            labelNoteBn = j.optString("label_note_bn"),
        )
    }
}

data class Product(
    val id: Int,
    val slug: String,
    val categorySlug: String,
    val kind: String, // "fertilizer" or "pesticide"
    val name: String,
    val nameBn: String,
    val shortDescription: String,
    val shortDescriptionBn: String,
    val description: String,
    val descriptionBn: String,
    val composition: String,
    val compositionBn: String,
    /** Ayush medicines: licence number from the label, and a safety caution shown in a box. */
    val licenseNo: String = "",
    val caution: String = "",
    val cautionBn: String = "",
    val benefits: List<String>,
    val benefitsBn: List<String>,
    val drawableName: String,
    val imageUrl: String = "",
    val isFeatured: Boolean,
    val sortOrder: Int,
    val variants: List<ProductVariant>,
    val cropDoses: List<CropDose>,
) {
    fun localizedName(lang: String): String = if (lang == "bn" && nameBn.isNotBlank()) nameBn else name
    fun localizedShort(lang: String): String = if (lang == "bn" && shortDescriptionBn.isNotBlank()) shortDescriptionBn else shortDescription
    fun localizedDesc(lang: String): String = if (lang == "bn" && descriptionBn.isNotBlank()) descriptionBn else description
    fun localizedComp(lang: String): String = if (lang == "bn" && compositionBn.isNotBlank()) compositionBn else composition
    fun localizedBenefits(lang: String): List<String> = if (lang == "bn" && benefitsBn.isNotEmpty()) benefitsBn else benefits
    fun localizedCaution(lang: String): String = if (lang == "bn" && cautionBn.isNotBlank()) cautionBn else caution
    val isMedicine: Boolean get() = kind == "ayush"

    val defaultVariant: ProductVariant? get() = variants.firstOrNull { it.inStock } ?: variants.firstOrNull()

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("slug", slug)
        .put("category_slug", categorySlug)
        .put("kind", kind)
        .put("name", name)
        .put("name_bn", nameBn)
        .put("short_description", shortDescription)
        .put("short_description_bn", shortDescriptionBn)
        .put("description", description)
        .put("description_bn", descriptionBn)
        .put("composition", composition)
        .put("composition_bn", compositionBn)
        .put("license_no", licenseNo)
        .put("caution", caution)
        .put("caution_bn", cautionBn)
        .put("benefits", JSONArray(benefits))
        .put("benefits_bn", JSONArray(benefitsBn))
        .put("drawable_name", drawableName)
        .put("image_url", imageUrl)
        .put("is_featured", isFeatured)
        .put("sort_order", sortOrder)
        .put("variants", JSONArray(variants.map { it.toJson() }))
        .put("crop_doses", JSONArray(cropDoses.map { it.toJson() }))

    companion object {
        fun from(j: JSONObject): Product {
            val bEn = j.optJSONArray("benefits")?.let { arr -> (0 until arr.length()).map { arr.optString(it) } } ?: emptyList()
            val bBn = j.optJSONArray("benefits_bn")?.let { arr -> (0 until arr.length()).map { arr.optString(it) } } ?: emptyList()
            return Product(
                id = j.optInt("id"),
                slug = j.optString("slug"),
                categorySlug = j.optString("category_slug"),
                kind = j.optString("kind", "fertilizer"),
                name = j.optString("name"),
                nameBn = j.optString("name_bn"),
                shortDescription = j.optString("short_description"),
                shortDescriptionBn = j.optString("short_description_bn"),
                description = j.optString("description"),
                descriptionBn = j.optString("description_bn"),
                composition = j.optString("composition"),
                licenseNo = j.optString("license_no"),
                caution = j.optString("caution"),
                cautionBn = j.optString("caution_bn"),
                compositionBn = j.optString("composition_bn"),
                benefits = bEn,
                benefitsBn = bBn,
                drawableName = j.optString("drawable_name"),
                imageUrl = j.optString("image_url"),
                isFeatured = j.optBoolean("is_featured"),
                sortOrder = j.optInt("sort_order"),
                variants = j.optJSONArray("variants").mapObjects { ProductVariant.from(it) },
                cropDoses = j.optJSONArray("crop_doses").mapObjects { CropDose.from(it) },
            )
        }
    }
}

data class ServiceablePin(
    val pincode: String,
    val areaName: String,
    val areaNameBn: String,
    val deliveryFee: Double,
    val codAllowed: Boolean = true,
) {
    fun localizedArea(lang: String): String = if (lang == "bn" && areaNameBn.isNotBlank()) areaNameBn else areaName

    fun toJson(): JSONObject = JSONObject()
        .put("pincode", pincode)
        .put("area_name", areaName)
        .put("area_name_bn", areaNameBn)
        .put("delivery_fee", deliveryFee)
        .put("cod_allowed", codAllowed)

    companion object {
        fun from(j: JSONObject) = ServiceablePin(
            pincode = j.optString("pincode"),
            areaName = j.optString("area_name"),
            areaNameBn = j.optString("area_name_bn"),
            deliveryFee = j.optDouble("delivery_fee", 0.0),
            codAllowed = j.optBoolean("cod_allowed", true),
        )
    }
}

data class Address(
    val id: Long = 0L,
    val fullName: String,
    val phone: String,
    val line1: String,
    val line2: String = "",
    val villageTown: String,
    val postOffice: String = "",
    val district: String = "Hooghly",
    val state: String = "West Bengal",
    val pincode: String = "712601",
    val landmark: String = "",
    val isDefault: Boolean = true,
) {
    fun formattedLine(): String = buildString {
        append(line1)
        if (line2.isNotBlank()) append(", $line2")
        if (landmark.isNotBlank()) append(" (Near $landmark)")
        append(", $villageTown, $district, $state – $pincode")
    }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("full_name", fullName)
        .put("phone", phone)
        .put("line1", line1)
        .put("line2", line2)
        .put("village_town", villageTown)
        .put("post_office", postOffice)
        .put("district", district)
        .put("state", state)
        .put("pincode", pincode)
        .put("landmark", landmark)
        .put("is_default", isDefault)

    companion object {
        fun from(j: JSONObject) = Address(
            id = j.optLong("id", 0L),
            fullName = j.optString("full_name"),
            phone = j.optString("phone"),
            line1 = j.optString("line1"),
            line2 = j.optString("line2"),
            villageTown = j.optString("village_town"),
            postOffice = j.optString("post_office"),
            district = j.optString("district", "Hooghly"),
            state = j.optString("state", "West Bengal"),
            pincode = j.optString("pincode", "712601"),
            landmark = j.optString("landmark", ""),
            isDefault = j.optBoolean("is_default", false),
        )
    }
}

data class CartLine(
    val product: Product,
    val variant: ProductVariant,
    val quantity: Int,
) {
    val lineTotal: Double get() = variant.price * quantity
    val taxableValue: Double get() = if (variant.gstRate > 0) (lineTotal * 100.0) / (100.0 + variant.gstRate) else lineTotal
    val taxAmount: Double get() = lineTotal - taxableValue
}

data class OrderItem(
    val variantId: Int,
    val productName: String,
    val productNameBn: String,
    val variantLabel: String,
    val sku: String,
    val hsnCode: String,
    val gstRate: Double,
    val unitPrice: Double,
    val quantity: Int,
    val taxableValue: Double,
    val taxAmount: Double,
    val lineTotal: Double,
) {
    fun localizedName(lang: String): String = if (lang == "bn" && productNameBn.isNotBlank()) productNameBn else productName

    fun toJson(): JSONObject = JSONObject()
        .put("variant_id", variantId)
        .put("product_name", productName)
        .put("product_name_bn", productNameBn)
        .put("variant_label", variantLabel)
        .put("sku", sku)
        .put("hsn_code", hsnCode)
        .put("gst_rate", gstRate)
        .put("unit_price", unitPrice)
        .put("quantity", quantity)
        .put("taxable_value", taxableValue)
        .put("tax_amount", taxAmount)
        .put("line_total", lineTotal)

    companion object {
        fun from(j: JSONObject) = OrderItem(
            variantId = j.optInt("variant_id"),
            productName = j.optString("product_name"),
            productNameBn = j.optString("product_name_bn"),
            variantLabel = j.optString("variant_label"),
            sku = j.optString("sku"),
            hsnCode = j.optString("hsn_code"),
            gstRate = j.optDouble("gst_rate", 5.0),
            unitPrice = j.optDouble("unit_price"),
            quantity = j.optInt("quantity"),
            taxableValue = j.optDouble("taxable_value"),
            taxAmount = j.optDouble("tax_amount"),
            lineTotal = j.optDouble("line_total"),
        )
    }
}

data class TimelineEntry(val status: String, val at: String, val note: String)

data class Order(
    val orderNo: String,
    val status: String,         // PLACED, CONFIRMED, PACKED, DISPATCHED, DELIVERED, CANCELLED
    val paymentMethod: String,  // UPI, BANK, COD
    val paymentStatus: String,  // AWAITING_PROOF, PROOF_SUBMITTED, VERIFIED, REJECTED, COD_DUE, COLLECTED
    val utrReference: String = "",
    val rejectReason: String = "",
    val address: Address,
    val items: List<OrderItem>,
    val subtotal: Double,
    val taxTotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val customerNote: String = "",
    val cancelReason: String = "",
    val createdAt: String,
    val createdAtIso: String = "",
    val canCancel: Boolean = false,
    val canPay: Boolean = false,
    val invoiceAvailable: Boolean = false,
    val invoiceNo: String = "",
    val timeline: List<TimelineEntry> = emptyList(),
    val whatsappSent: Boolean = false,  // local only: has the customer sent this order on WhatsApp?
    val priceChanged: Boolean = false,  // sent later at today's price, which differs from what the phone showed
    val clientTotal: Double? = null,
    val source: String = "app",
) {
    val isActive: Boolean get() = status in setOf("PLACED", "CONFIRMED", "PACKED", "DISPATCHED")

    fun paymentMethodLabel(lang: String): String = when (paymentMethod) {
        "UPI" -> if (lang == "bn") "UPI পেমেন্ট" else "UPI payment"
        "BANK" -> if (lang == "bn") "ব্যাংক ট্রান্সফার (NEFT/IMPS)" else "Bank transfer (NEFT/IMPS)"
        else -> if (lang == "bn") "ক্যাশ অন ডেলিভারি" else "Cash on delivery"
    }

    fun paymentStatusLabel(lang: String): String = when (paymentStatus) {
        "AWAITING_PROOF" -> if (lang == "bn") "পেমেন্টের অপেক্ষায়" else "Awaiting payment"
        "PROOF_SUBMITTED" -> if (lang == "bn") "UTR জমা হয়েছে · যাচাই চলছে" else "UTR sent · being verified"
        "VERIFIED" -> if (lang == "bn") "পেমেন্ট যাচাই হয়েছে ✓" else "Payment verified ✓"
        "REJECTED" -> if (lang == "bn") "UTR মেলেনি · আবার পাঠান" else "UTR not matched · please resend"
        "COD_DUE" -> if (lang == "bn") "ডেলিভারির সময় দেবেন" else "Pay on delivery"
        "COLLECTED" -> if (lang == "bn") "নগদ পাওয়া গেছে ✓" else "Cash received ✓"
        else -> paymentStatus
    }

    fun statusLabel(lang: String): String = statusLabelFor(status, lang)

    fun toJson(): JSONObject = JSONObject()
        .put("order_no", orderNo)
        .put("status", status)
        .put("payment_method", paymentMethod)
        .put("payment_status", paymentStatus)
        .put("utr_reference", utrReference)
        .put("reject_reason", rejectReason)
        .put("address_snapshot", address.toJson())
        .put("items", JSONArray(items.map { it.toJson() }))
        .put("subtotal", subtotal)
        .put("tax_total", taxTotal)
        .put("delivery_fee", deliveryFee)
        .put("total", total)
        .put("customer_note", customerNote)
        .put("cancel_reason", cancelReason)
        .put("created_at", createdAt)
        .put("created_at_iso", createdAtIso)
        .put("can_cancel", canCancel)
        .put("can_pay", canPay)
        .put("invoice_available", invoiceAvailable)
        .put("invoice_no", invoiceNo)
        .put("timeline", JSONArray(timeline.map { JSONObject().put("status", it.status).put("at", it.at).put("note", it.note) }))
        .put("whatsapp_sent", whatsappSent)
        .put("price_changed", priceChanged)
        .put("client_total", clientTotal ?: JSONObject.NULL)
        .put("source", source)

    companion object {
        fun statusLabelFor(status: String, lang: String): String = when (status) {
            "PLACED" -> if (lang == "bn") "অর্ডার করা হয়েছে" else "Placed"
            "CONFIRMED" -> if (lang == "bn") "নিশ্চিত হয়েছে" else "Confirmed"
            "PACKED" -> if (lang == "bn") "প্যাক করা হয়েছে" else "Packed"
            "DISPATCHED" -> if (lang == "bn") "পাঠানো হয়েছে" else "Dispatched"
            "DELIVERED" -> if (lang == "bn") "ডেলিভারি সম্পন্ন" else "Delivered"
            "CANCELLED" -> if (lang == "bn") "বাতিল" else "Cancelled"
            else -> status
        }

        fun from(j: JSONObject) = Order(
            orderNo = j.optString("order_no"),
            status = j.optString("status", "PLACED"),
            paymentMethod = j.optString("payment_method", "UPI"),
            paymentStatus = j.optString("payment_status", "AWAITING_PROOF"),
            utrReference = j.optString("utr_reference", ""),
            rejectReason = j.optString("reject_reason", ""),
            address = Address.from(j.optJSONObject("address_snapshot") ?: JSONObject()),
            items = j.optJSONArray("items").mapObjects { OrderItem.from(it) },
            subtotal = j.optDouble("subtotal", 0.0),
            taxTotal = j.optDouble("tax_total", 0.0),
            deliveryFee = j.optDouble("delivery_fee", 0.0),
            total = j.optDouble("total", 0.0),
            customerNote = j.optString("customer_note", ""),
            cancelReason = j.optString("cancel_reason", ""),
            createdAt = j.optString("created_at", ""),
            createdAtIso = j.optString("created_at_iso", ""),
            canCancel = j.optBoolean("can_cancel", false),
            canPay = j.optBoolean("can_pay", false),
            invoiceAvailable = j.optBoolean("invoice_available", false),
            invoiceNo = j.optString("invoice_no", ""),
            timeline = j.optJSONArray("timeline").mapObjects { TimelineEntry(it.optString("status"), it.optString("at"), it.optString("note")) },
            whatsappSent = j.optBoolean("whatsapp_sent", false),
            priceChanged = j.optBoolean("price_changed", false),
            clientTotal = j.optNullableDouble("client_total"),
            source = j.optString("source", "app"),
        )
    }
}

/** The account on this phone. isGuest = created silently so people can order without signing in. */
data class Me(val id: Long, val name: String, val phone: String, val email: String, val language: String,
              val isGuest: Boolean = false, val referralCode: String = "") {
    fun toJson(): JSONObject = JSONObject().put("id", id).put("name", name).put("phone", phone).put("email", email)
        .put("language", language).put("is_guest", isGuest).put("referral_code", referralCode)

    companion object {
        fun from(j: JSONObject?): Me? = j?.let {
            Me(it.optLong("id"), it.optString("name"), it.optString("phone"), it.optString("email"),
                it.optString("language", "bn"), it.optBoolean("is_guest", false), it.optString("referral_code"))
        }
    }
}

/** Line of an order saved on the phone, frozen at the price the customer saw. */
data class PendingLine(val variantId: Int, val productName: String, val productNameBn: String, val label: String, val quantity: Int, val price: Double) {
    fun toJson(): JSONObject = JSONObject().put("variant_id", variantId).put("product_name", productName)
        .put("product_name_bn", productNameBn).put("label", label).put("quantity", quantity).put("price", price)

    fun localizedName(lang: String) = if (lang == "bn" && productNameBn.isNotBlank()) productNameBn else productName

    companion object {
        fun from(j: JSONObject) = PendingLine(j.optInt("variant_id"), j.optString("product_name"), j.optString("product_name_bn"),
            j.optString("label"), j.optInt("quantity"), j.optDouble("price", 0.0))
    }
}

/**
 * An order made while offline (or when the send failed). Stored in SQLite and sent automatically
 * when the phone is online again. [key] doubles as the server idempotency key, so a retry after a
 * dropped connection can never create a second order.
 */
data class PendingOrder(
    val key: String,
    val createdAt: Long,
    val paymentMethod: String,
    val address: Address,
    val lines: List<PendingLine>,
    val expectedTotal: Double,
    val note: String = "",
    val status: String = "waiting",  // waiting | failed
    val error: String = "",
    val referralCode: String = "",
) {
    val itemsCount: Int get() = lines.sumOf { it.quantity }

    fun toJson(): JSONObject = JSONObject().put("key", key).put("created_at", createdAt).put("payment_method", paymentMethod)
        .put("address", address.toJson()).put("lines", JSONArray(lines.map { it.toJson() })).put("expected_total", expectedTotal)
        .put("note", note).put("status", status).put("error", error).put("referral_code", referralCode)

    /** Body for /api/v1/orders/place/ */
    fun placeBody(): JSONObject {
        val items = JSONArray()
        lines.forEach { items.put(JSONObject().put("variant_id", it.variantId).put("quantity", it.quantity)) }
        val addr = address.toJson().apply { remove("id"); remove("is_default") }
        return JSONObject().put("address", addr).put("items", items).put("payment_method", paymentMethod)
            .put("note", note).put("idempotency_key", key).put("expected_total", expectedTotal).put("offline", true)
            .put("referral_code", referralCode)
    }

    companion object {
        fun from(j: JSONObject) = PendingOrder(
            key = j.optString("key"), createdAt = j.optLong("created_at"), paymentMethod = j.optString("payment_method", "COD"),
            address = Address.from(j.optJSONObject("address") ?: JSONObject()),
            lines = j.optJSONArray("lines").mapObjects { PendingLine.from(it) },
            expectedTotal = j.optDouble("expected_total", 0.0), note = j.optString("note"),
            status = j.optString("status", "waiting"), error = j.optString("error"), referralCode = j.optString("referral_code"),
        )
    }
}

/** Live check from /api/v1/quote/ just before an order is sent. */
data class QuoteLine(val variantId: Int, val quantity: Int, val available: Boolean, val price: Double, val stock: Int, val priceChanged: Boolean, val oldPrice: Double?)

data class Quote(
    val lines: List<QuoteLine>,
    val subtotal: Double,
    val deliveryFee: Double?,
    val total: Double,
    val codAllowed: Boolean,
    val problemCodes: List<String>,
    val catalogVersion: String,
) {
    val priceChanged: Boolean get() = "price" in problemCodes
    val stockShort: Boolean get() = "stock" in problemCodes || "unavailable" in problemCodes
    val blocked: Boolean get() = problemCodes.any { it in setOf("pincode", "min_order", "max_qty") }

    companion object {
        fun from(j: JSONObject): Quote {
            val probs = j.optJSONArray("problems").mapObjects { it.optString("code") }
            return Quote(
                lines = j.optJSONArray("lines").mapObjects {
                    QuoteLine(it.optInt("variant_id"), it.optInt("quantity"), it.optBoolean("available"), it.optDouble("price", 0.0),
                        it.optInt("stock"), it.optBoolean("price_changed"), it.optNullableDouble("old_price"))
                },
                subtotal = j.optDouble("subtotal", 0.0),
                deliveryFee = j.optNullableDouble("delivery_fee"),
                total = j.optDouble("total", 0.0),
                codAllowed = j.optBoolean("cod_allowed", true),
                problemCodes = probs,
                catalogVersion = j.optString("catalog_version"),
            )
        }
    }
}

data class InvoiceSeller(val name: String, val gstin: String, val cin: String, val address: String, val state: String, val stateCode: String)

/** Tax invoice issued by the server (`apps/invoicing`). Numbers are real, sequential and never reused. */
data class Invoice(
    val invoiceNo: String,
    val issuedDate: String,
    val orderNo: String,
    val orderDate: String,
    val seller: InvoiceSeller,
    val buyer: Address,
    val isInterstate: Boolean,
    val items: List<OrderItem>,
    val taxableTotal: Double,
    val cgst: Double,
    val sgst: Double,
    val igst: Double,
    val taxTotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val paymentMethodLabel: String,
    val paymentStatusLabel: String,
) {
    companion object {
        fun from(j: JSONObject): Invoice {
            val s = j.optJSONObject("seller") ?: JSONObject()
            return Invoice(
                invoiceNo = j.optString("invoice_no"),
                issuedDate = j.optString("issued_date"),
                orderNo = j.optString("order_no"),
                orderDate = j.optString("order_date"),
                seller = InvoiceSeller(s.optString("name"), s.optString("gstin"), s.optString("cin"),
                    s.optString("address"), s.optString("state"), s.optString("state_code")),
                buyer = Address.from(j.optJSONObject("buyer") ?: JSONObject()),
                isInterstate = j.optBoolean("is_interstate"),
                items = j.optJSONArray("items").mapObjects { OrderItem.from(it) },
                taxableTotal = j.optDouble("taxable_total", 0.0),
                cgst = j.optDouble("cgst", 0.0),
                sgst = j.optDouble("sgst", 0.0),
                igst = j.optDouble("igst", 0.0),
                taxTotal = j.optDouble("tax_total", 0.0),
                deliveryFee = j.optDouble("delivery_fee", 0.0),
                total = j.optDouble("total", 0.0),
                paymentMethodLabel = j.optString("payment_method_label"),
                paymentStatusLabel = j.optString("payment_status_label"),
            )
        }
    }
}

data class CatalogPayload(
    val config: SiteConfig,
    val categories: List<Category>,
    val products: List<Product>,
    val pins: List<ServiceablePin>,
    val version: String = "",
    val banners: List<Banner> = emptyList(),
    val tips: List<KrishiTip> = emptyList(),
) {
    fun toJson(): JSONObject = JSONObject()
        .put("site", config.toJson())
        .put("categories", JSONArray(categories.map { it.toJson() }))
        .put("products", JSONArray(products.map { it.toJson() }))
        .put("serviceable_pins", JSONArray(pins.map { it.toJson() }))
        .put("version", version)
        .put("banners", JSONArray(banners.map { it.toJson() }))
        .put("tips", JSONArray(tips.map { it.toJson() }))

    companion object {
        fun from(j: JSONObject) = CatalogPayload(
            config = SiteConfig.from(j.optJSONObject("site")),
            categories = j.optJSONArray("categories").mapObjects { Category.from(it) },
            products = j.optJSONArray("products").mapObjects { Product.from(it) },
            pins = j.optJSONArray("serviceable_pins").mapObjects { ServiceablePin.from(it) },
            version = j.optString("version"),
            banners = j.optJSONArray("banners").mapObjects { Banner.from(it) },
            tips = j.optJSONArray("tips").mapObjects { KrishiTip.from(it) },
        )
    }
}


/** Home carousel banner, managed in Admin > Home banners. */
data class Banner(
    val id: Int, val title: String, val titleBn: String, val subtitle: String, val subtitleBn: String,
    val button: String, val buttonBn: String, val theme: String, val imageUrl: String, val linkKind: String, val linkValue: String,
) {
    fun t(lang: String) = if (lang == "bn" && titleBn.isNotBlank()) titleBn else title
    fun sub(lang: String) = if (lang == "bn" && subtitleBn.isNotBlank()) subtitleBn else subtitle
    fun btn(lang: String) = if (lang == "bn" && buttonBn.isNotBlank()) buttonBn else button

    fun toJson(): JSONObject = JSONObject().put("id", id).put("title", title).put("title_bn", titleBn).put("subtitle", subtitle)
        .put("subtitle_bn", subtitleBn).put("button", button).put("button_bn", buttonBn).put("theme", theme)
        .put("image_url", imageUrl).put("link_kind", linkKind).put("link_value", linkValue)

    companion object {
        fun from(j: JSONObject) = Banner(
            j.optInt("id"), j.optString("title"), j.optString("title_bn"), j.optString("subtitle"), j.optString("subtitle_bn"),
            j.optString("button"), j.optString("button_bn"), j.optString("theme", "forest"), j.optString("image_url"),
            j.optString("link_kind"), j.optString("link_value"),
        )
    }
}


/** Farming advice written by GMM staff in the admin (Krishi tips). */
data class KrishiTip(val id: Int, val title: String, val titleBn: String, val body: String, val bodyBn: String,
                     val crop: String, val imageUrl: String, val productSlug: String, val pinned: Boolean, val date: String) {
    fun t(lang: String) = if (lang == "bn" && titleBn.isNotBlank()) titleBn else title
    fun b(lang: String) = if (lang == "bn" && bodyBn.isNotBlank()) bodyBn else body

    fun toJson(): JSONObject = JSONObject().put("id", id).put("title", title).put("title_bn", titleBn).put("body", body)
        .put("body_bn", bodyBn).put("crop", crop).put("image_url", imageUrl).put("product_slug", productSlug)
        .put("pinned", pinned).put("date", date)

    companion object {
        fun from(j: JSONObject) = KrishiTip(j.optInt("id"), j.optString("title"), j.optString("title_bn"), j.optString("body"),
            j.optString("body_bn"), j.optString("crop"), j.optString("image_url"), j.optString("product_slug"),
            j.optBoolean("pinned"), j.optString("date"))
    }
}

/** A problem report waiting on the phone until there is internet. The screenshot is a JPEG file on the phone. */
data class PendingFeedback(
    val key: String, val createdAt: Long, val kind: String, val message: String, val name: String, val phone: String,
    val orderNo: String, val screen: String, val imagePath: String,
) {
    fun toJson(): JSONObject = JSONObject().put("key", key).put("created_at", createdAt).put("kind", kind).put("message", message)
        .put("name", name).put("phone", phone).put("order_no", orderNo).put("screen", screen).put("image_path", imagePath)

    companion object {
        fun from(j: JSONObject) = PendingFeedback(j.optString("key"), j.optLong("created_at"), j.optString("kind", "other"),
            j.optString("message"), j.optString("name"), j.optString("phone"), j.optString("order_no"), j.optString("screen"),
            j.optString("image_path"))
    }
}


/** Krishi Mitra: my invite code and the orders placed with it. */
data class ReferralStats(val code: String, val orders: Int, val delivered: Int, val link: String, val recent: List<Triple<String, String, String>>) {
    companion object {
        fun from(j: JSONObject) = ReferralStats(
            j.optString("code"), j.optInt("orders"), j.optInt("delivered"), j.optString("link"),
            j.optJSONArray("recent").mapObjects { Triple(it.optString("name"), it.optString("village"), it.optString("status_label")) },
        )
    }
}
