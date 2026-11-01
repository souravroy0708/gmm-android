package com.gmmdistributors.app.api

import org.json.JSONArray
import org.json.JSONObject

inline fun <T> JSONArray?.mapObjects(f: (JSONObject) -> T): List<T> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { optJSONObject(it)?.let(f) }
}

data class SiteConfig(
    val businessName: String = "GMM Distributors Private Limited",
    val taglineEn: String = "100% Organic Fertilizer & Bio-Pesticide · Zero Chemicals",
    val taglineBn: String = "১০০% জৈব সার ও জৈব কীটনাশক · রাসায়নিকমুক্ত চাষাবাদ",
    val legalAddress: String = "Nirmala Bhawan, Fansi Bagan, Ward No. 13, Arambagh, Hooghly, West Bengal – 712601",
    val cin: String = "U24299WB2020PTC240755",
    val gstin: String = "19AAICG0000A1Z5",
    val stateName: String = "West Bengal",
    val stateCode: String = "19",
    val supportPhone: String = "9083732101",
    val whatsappNumber: String = "919083732101",
    val supportEmail: String = "ray.chikur@gmail.com",
    val upiId: String = "gmmdistributors@sbi",
    val bankAccountName: String = "GMM Distributors Private Limited",
    val bankName: String = "State Bank of India (Arambagh Branch)",
    val bankAccountNumber: String = "39876543210",
    val bankIfsc: String = "SBIN0000014",
    val minOrderValue: Double = 150.0,
    val freeDeliveryAbove: Double = 1000.0,
    val codEnabled: Boolean = true,
) {
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
        .put("bank_account_name", bankAccountName)
        .put("bank_name", bankName)
        .put("bank_account_number", bankAccountNumber)
        .put("bank_ifsc", bankIfsc)
        .put("min_order_value", minOrderValue)
        .put("free_delivery_above", freeDeliveryAbove)
        .put("cod_enabled", codEnabled)

    companion object {
        fun from(j: JSONObject?): SiteConfig {
            if (j == null) return SiteConfig()
            return SiteConfig(
                businessName = j.optString("business_name", "GMM Distributors Private Limited"),
                taglineEn = j.optString("tagline_en", "100% Organic Fertilizer & Bio-Pesticide · Zero Chemicals"),
                taglineBn = j.optString("tagline_bn", "১০০% জৈব সার ও জৈব কীটনাশক · রাসায়নিকমুক্ত চাষাবাদ"),
                legalAddress = j.optString("legal_address", "Nirmala Bhawan, Fansi Bagan, Ward No. 13, Arambagh, Hooghly, West Bengal – 712601"),
                cin = j.optString("cin", "U24299WB2020PTC240755"),
                gstin = j.optString("gstin", "19AAICG0000A1Z5"),
                stateName = j.optString("state_name", "West Bengal"),
                stateCode = j.optString("state_code", "19"),
                supportPhone = j.optString("support_phone", "9083732101"),
                whatsappNumber = j.optString("whatsapp_number", "919083732101"),
                supportEmail = j.optString("support_email", "ray.chikur@gmail.com"),
                upiId = j.optString("upi_id", "gmmdistributors@sbi"),
                bankAccountName = j.optString("bank_account_name", "GMM Distributors Private Limited"),
                bankName = j.optString("bank_name", "State Bank of India (Arambagh Branch)"),
                bankAccountNumber = j.optString("bank_account_number", "39876543210"),
                bankIfsc = j.optString("bank_ifsc", "SBIN0000014"),
                minOrderValue = j.optDouble("min_order_value", 150.0),
                freeDeliveryAbove = j.optDouble("free_delivery_above", 1000.0),
                codEnabled = j.optBoolean("cod_enabled", true),
            )
        }
    }
}

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
) {
    val inStock: Boolean get() = isActive && stock > 0
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
    val benefits: List<String>,
    val benefitsBn: List<String>,
    val drawableName: String,
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
        .put("benefits", JSONArray(benefits))
        .put("benefits_bn", JSONArray(benefitsBn))
        .put("drawable_name", drawableName)
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
                compositionBn = j.optString("composition_bn"),
                benefits = bEn,
                benefitsBn = bBn,
                drawableName = j.optString("drawable_name", "prod_premium_jackpot"),
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
    val villageTown: String,
    val district: String = "Hooghly",
    val state: String = "West Bengal",
    val pincode: String = "712601",
    val landmark: String = "",
    val isDefault: Boolean = true,
) {
    fun formattedLine(): String = buildString {
        append(line1)
        if (landmark.isNotBlank()) append(" (Near $landmark)")
        append(", $villageTown, $district, $state – $pincode")
    }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("full_name", fullName)
        .put("phone", phone)
        .put("line1", line1)
        .put("village_town", villageTown)
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
            villageTown = j.optString("village_town"),
            district = j.optString("district", "Hooghly"),
            state = j.optString("state", "West Bengal"),
            pincode = j.optString("pincode", "712601"),
            landmark = j.optString("landmark", ""),
            isDefault = j.optBoolean("is_default", true),
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

data class Order(
    val orderNo: String,
    val status: String,         // PLACED, CONFIRMED, PACKED, DISPATCHED, DELIVERED, CANCELLED
    val paymentMethod: String,  // UPI, BANK, COD
    val paymentStatus: String,  // AWAITING_WHATSAPP_PAYMENT, UTR_SUBMITTED, VERIFIED, COD_DUE
    val utrReference: String = "",
    val address: Address,
    val items: List<OrderItem>,
    val subtotal: Double,
    val taxTotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val customerNote: String = "",
    val createdAt: String,
    val whatsappSent: Boolean = false,
) {
    val canCancel: Boolean get() = status == "PLACED"

    fun paymentMethodLabel(lang: String): String = when (paymentMethod) {
        "UPI" -> if (lang == "bn") "UPI ট্রান্সফার (হোয়াটসঅ্যাপের মাধ্যমে)" else "Manual UPI (via WhatsApp)"
        "BANK" -> if (lang == "bn") "ব্যাংক ট্রান্সফার / NEFT (হোয়াটসঅ্যাপের মাধ্যমে)" else "Manual Bank Transfer (via WhatsApp)"
        else -> if (lang == "bn") "ক্যাশ অন ডেলিভারি (COD)" else "Cash on Delivery (COD)"
    }

    fun statusLabel(lang: String): String = when (status) {
        "PLACED" -> if (lang == "bn") "অর্ডার করা হয়েছে" else "Placed"
        "CONFIRMED" -> if (lang == "bn") "নিশ্চিত হয়েছে" else "Confirmed"
        "PACKED" -> if (lang == "bn") "প্যাক করা হয়েছে" else "Packed"
        "DISPATCHED" -> if (lang == "bn") "পাঠানো হয়েছে" else "Dispatched"
        "DELIVERED" -> if (lang == "bn") "ডেলিভারি সম্পন্ন" else "Delivered"
        "CANCELLED" -> if (lang == "bn") "বাতিল" else "Cancelled"
        else -> status
    }

    fun toJson(): JSONObject = JSONObject()
        .put("order_no", orderNo)
        .put("status", status)
        .put("payment_method", paymentMethod)
        .put("payment_status", paymentStatus)
        .put("utr_reference", utrReference)
        .put("address_snapshot", address.toJson())
        .put("items", JSONArray(items.map { it.toJson() }))
        .put("subtotal", subtotal)
        .put("tax_total", taxTotal)
        .put("delivery_fee", deliveryFee)
        .put("total", total)
        .put("customer_note", customerNote)
        .put("created_at", createdAt)
        .put("whatsapp_sent", whatsappSent)

    companion object {
        fun from(j: JSONObject) = Order(
            orderNo = j.optString("order_no"),
            status = j.optString("status", "PLACED"),
            paymentMethod = j.optString("payment_method", "UPI"),
            paymentStatus = j.optString("payment_status", "AWAITING_WHATSAPP_PAYMENT"),
            utrReference = j.optString("utr_reference", ""),
            address = Address.from(j.optJSONObject("address_snapshot") ?: JSONObject()),
            items = j.optJSONArray("items").mapObjects { OrderItem.from(it) },
            subtotal = j.optDouble("subtotal", 0.0),
            taxTotal = j.optDouble("tax_total", 0.0),
            deliveryFee = j.optDouble("delivery_fee", 0.0),
            total = j.optDouble("total", 0.0),
            customerNote = j.optString("customer_note", ""),
            createdAt = j.optString("created_at", ""),
            whatsappSent = j.optBoolean("whatsapp_sent", false),
        )
    }
}

data class CatalogPayload(
    val config: SiteConfig,
    val categories: List<Category>,
    val products: List<Product>,
    val pins: List<ServiceablePin>,
) {
    fun toJson(): JSONObject = JSONObject()
        .put("site", config.toJson())
        .put("categories", JSONArray(categories.map { it.toJson() }))
        .put("products", JSONArray(products.map { it.toJson() }))
        .put("serviceable_pins", JSONArray(pins.map { it.toJson() }))

    companion object {
        fun from(j: JSONObject) = CatalogPayload(
            config = SiteConfig.from(j.optJSONObject("site")),
            categories = j.optJSONArray("categories").mapObjects { Category.from(it) },
            products = j.optJSONArray("products").mapObjects { Product.from(it) },
            pins = j.optJSONArray("serviceable_pins").mapObjects { ServiceablePin.from(it) },
        )
    }
}
