package com.gmmdistributors.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gmmdistributors.app.api.Address
import com.gmmdistributors.app.api.CartLine
import com.gmmdistributors.app.api.Category
import com.gmmdistributors.app.api.Order
import com.gmmdistributors.app.api.Product
import com.gmmdistributors.app.api.Repo
import com.gmmdistributors.app.api.Res
import com.gmmdistributors.app.api.ServiceablePin
import com.gmmdistributors.app.api.SiteConfig
import com.gmmdistributors.app.ui.formatRupees
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Tab { Shop, Advisory, Cart, Orders, Profile }

/**
 * Central state controller following the exact `AppController.kt` architecture from `bas1minute-android`.
 * Manages the 10-product organic catalog, reactive SQLite cart, delivery addresses, orders,
 * Krishi Paramarsha crop guides, Thought Card maker, and WhatsApp order message formatting.
 */
class AppController(private val ctx: Context, private val scope: CoroutineScope) {
    val prefs = Prefs(ctx)
    val db = LocalDb.get(ctx)
    val repo = Repo(ctx)

    var tab by mutableStateOf(Tab.Shop)
    var language by mutableStateOf(prefs.language)
    var theme by mutableStateOf(prefs.theme)
    var useDummyApi by mutableStateOf(prefs.useDummyApi)
    var serverUrl by mutableStateOf(prefs.serverUrl)

    // Catalog state
    var siteConfig by mutableStateOf(DummyCatalog.SITE_CONFIG)
    var categories by mutableStateOf<List<Category>>(DummyCatalog.CATEGORIES)
    var products by mutableStateOf<List<Product>>(DummyCatalog.PRODUCTS)
    var pins by mutableStateOf<List<ServiceablePin>>(DummyCatalog.PINS)

    // Shop filters & active product modal
    var selectedCategorySlug by mutableStateOf("all")
    var searchQuery by mutableStateOf("")
    var openProductSlug by mutableStateOf<String?>(null)

    // Advisory & Thought Card state
    var openCropSlug by mutableStateOf<String?>(null)
    var thoughtCardOpen by mutableStateOf(false)

    // Cart & Address state
    var cartQuantities by mutableStateOf<Map<Int, Int>>(db.cartMap())
    var addresses by mutableStateOf<List<Address>>(db.addresses())
    var selectedAddressId by mutableStateOf<Long?>(addresses.firstOrNull { it.isDefault }?.id ?: addresses.firstOrNull()?.id)
    var addingAddress by mutableStateOf(false)
    var editingAddress by mutableStateOf<Address?>(null)

    // Orders & Invoice state
    var orders by mutableStateOf<List<Order>>(db.orders())
    var openOrderNo by mutableStateOf<String?>(null)
    var invoiceOrderNo by mutableStateOf<String?>(null)
    var newlyPlacedOrder by mutableStateOf<Order?>(null)

    // Profile sub-screens ("vision", "leadership", "contact", "visiting_card")
    var companyPage by mutableStateOf<String?>(null)

    // UI status
    var refreshing by mutableStateOf(false)
    var busy by mutableStateOf(false)
    var offline by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)

    init {
        ensureDefaultDemoAddressIfNeeded()
        refresh()
    }

    private fun ensureDefaultDemoAddressIfNeeded() {
        if (addresses.isEmpty() && prefs.customerName.isNotBlank() && prefs.customerPhone.isNotBlank()) {
            val id = db.saveAddress(
                Address(
                    fullName = prefs.customerName,
                    phone = prefs.customerPhone,
                    line1 = prefs.villageTown.ifBlank { "Ward No. 13, Fansi Bagan" },
                    villageTown = prefs.villageTown.ifBlank { "Arambagh" },
                    district = "Hooghly",
                    state = "West Bengal",
                    pincode = prefs.pincode.ifBlank { "712601" },
                    landmark = "",
                    isDefault = true,
                )
            )
            addresses = db.addresses()
            selectedAddressId = id
        }
    }

    fun launch(block: suspend () -> Unit) {
        scope.launch { block() }
    }

    fun refresh() = launch {
        refreshing = true
        val catLoaded = repo.loadCatalog()
        siteConfig = catLoaded.value.config
        categories = catLoaded.value.categories
        products = catLoaded.value.products
        pins = catLoaded.value.pins
        offline = catLoaded.offline

        orders = repo.loadOrders()
        cartQuantities = withContext(Dispatchers.IO) { db.cartMap() }
        addresses = withContext(Dispatchers.IO) { db.addresses() }
        if (selectedAddressId == null || addresses.none { it.id == selectedAddressId }) {
            selectedAddressId = addresses.firstOrNull { it.isDefault }?.id ?: addresses.firstOrNull()?.id
        }
        refreshing = false
    }

    // ---------- Computed Cart Helpers ----------

    val cartLines: List<CartLine>
        get() {
            val variantLookup = products.flatMap { p -> p.variants.map { v -> v.id to (p to v) } }.toMap()
            return cartQuantities.mapNotNull { (variantId, qty) ->
                val pair = variantLookup[variantId] ?: return@mapNotNull null
                if (qty > 0) CartLine(pair.first, pair.second, qty) else null
            }
        }

    val cartCount: Int
        get() = cartLines.sumOf { it.quantity }

    val cartSubtotal: Double
        get() = cartLines.sumOf { it.lineTotal }

    val cartTaxTotal: Double
        get() = cartLines.sumOf { it.taxAmount }

    fun deliveryFeeFor(pincode: String, subtotal: Double = cartSubtotal): Double {
        if (subtotal <= 0.0) return 0.0
        if (subtotal >= siteConfig.freeDeliveryAbove) return 0.0
        val pin = pins.firstOrNull { it.pincode == pincode.trim() }
        return pin?.deliveryFee ?: 50.0
    }

    fun isPinServiceable(pincode: String): ServiceablePin? =
        pins.firstOrNull { it.pincode == pincode.trim() }

    // ---------- Cart Actions ----------

    fun setVariantQty(variantId: Int, newQty: Int) {
        val current = cartQuantities[variantId] ?: 0
        if (current == 0 && newQty > 0 && cartQuantities.size >= 10) {
            message = if (language == "bn") "কার্টে সর্বোচ্চ ১০টি আলাদা পণ্য রাখা যায়।"
            else "Your cart can hold at most 10 different products."
            return
        }
        val clamped = newQty.coerceIn(0, 10)
        db.setCartQty(variantId, clamped)
        cartQuantities = db.cartMap()
        if (newQty > current) {
            message = if (language == "bn") "কার্টে যোগ হয়েছে ✓" else "Added to cart ✓"
        }
    }

    fun clearCart() {
        db.clearCart()
        cartQuantities = emptyMap()
    }

    // ---------- Address Actions ----------

    fun saveAddress(address: Address) {
        val id = db.saveAddress(address)
        if (prefs.customerName.isBlank()) prefs.customerName = address.fullName
        if (prefs.customerPhone.isBlank()) prefs.customerPhone = address.phone
        prefs.villageTown = address.villageTown
        prefs.pincode = address.pincode
        addresses = db.addresses()
        selectedAddressId = id
        addingAddress = false
        editingAddress = null
        message = if (language == "bn") "ঠিকানা সেভ হয়েছে ✓" else "Delivery address saved ✓"
    }

    fun deleteAddress(id: Long) {
        db.deleteAddress(id)
        addresses = db.addresses()
        if (selectedAddressId == id) {
            selectedAddressId = addresses.firstOrNull { it.isDefault }?.id ?: addresses.firstOrNull()?.id
        }
        message = if (language == "bn") "ঠিকানা মুছে ফেলা হয়েছে।" else "Address removed."
    }

    fun selectDefaultAddress(id: Long) {
        db.setDefaultAddress(id)
        addresses = db.addresses()
        selectedAddressId = id
    }

    // ---------- Order & WhatsApp Actions ----------

    fun placeOrder(
        address: Address,
        paymentMethod: String,
        note: String,
        onPlaced: (Order) -> Unit,
    ) = launch {
        if (cartLines.isEmpty()) {
            message = if (language == "bn") "আপনার কার্ট খালি।" else "Your cart is empty."
            return@launch
        }
        busy = true
        when (val res = repo.placeOrder(address, cartLines, paymentMethod, note)) {
            is Res.Ok -> {
                cartQuantities = db.cartMap()
                orders = db.orders()
                newlyPlacedOrder = res.value
                openOrderNo = res.value.orderNo
                busy = false
                onPlaced(res.value)
            }
            is Res.Fail -> {
                busy = false
                message = res.message
            }
        }
    }

    fun cancelOrder(orderNo: String) = launch {
        busy = true
        when (val res = repo.cancelOrder(orderNo)) {
            is Res.Ok -> {
                orders = db.orders()
                message = if (language == "bn") "অর্ডার বাতিল করা হয়েছে।" else "Order $orderNo cancelled."
            }
            is Res.Fail -> message = res.message
        }
        busy = false
    }

    fun submitOrderUtr(orderNo: String, utr: String) = launch {
        busy = true
        when (val res = repo.submitUtr(orderNo, utr)) {
            is Res.Ok -> {
                orders = db.orders()
                message = if (language == "bn") "পেমেন্ট রেফারেন্স (UTR) সেভ হয়েছে ✓" else "Payment reference (UTR) saved ✓"
            }
            is Res.Fail -> message = res.message
        }
        busy = false
    }

    fun markOrderWhatsappSent(orderNo: String) = launch {
        repo.markWhatsappSent(orderNo)
        orders = db.orders()
    }

    /**
     * Builds the structured WhatsApp Order Notification message sent directly to the GMM Owner
     * (+91 9083732101, Chikur Kumar Ray) so the owner can confirm the order and manually collect
     * payment via Bank Account, UPI, or Cash on Delivery outside the app.
     */
    fun buildWhatsAppOrderMessage(order: Order, includeUtr: Boolean = true): String {
        val sb = StringBuilder()
        sb.appendLine("🌿 *NEW ORGANIC ORDER — GMM DISTRIBUTORS*")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("📦 *Order No:* ${order.orderNo}")
        sb.appendLine("🗓 *Date:* ${order.createdAt}")
        sb.appendLine("🌱 *Category:* 100% Organic Fertilizer & Bio-Pesticide (Zero Chemicals)")
        sb.appendLine()
        sb.appendLine("👤 *CUSTOMER & DELIVERY ADDRESS*")
        sb.appendLine("• *Name:* ${order.address.fullName}")
        sb.appendLine("• *Mobile:* ${order.address.phone}")
        sb.appendLine("• *Address:* ${order.address.line1}")
        if (order.address.landmark.isNotBlank()) {
            sb.appendLine("• *Landmark:* ${order.address.landmark}")
        }
        sb.appendLine("• *Village/Town:* ${order.address.villageTown}")
        sb.appendLine("• *District/State:* ${order.address.district}, ${order.address.state}")
        sb.appendLine("• *PIN Code:* ${order.address.pincode}")
        sb.appendLine()
        sb.appendLine("🛒 *ORDERED ORGANIC ITEMS*")
        order.items.forEachIndexed { idx, item ->
            sb.appendLine("${idx + 1}. *${item.productName}* (${item.variantLabel})")
            sb.appendLine("   Qty: ${item.quantity} × ${formatRupees(item.unitPrice)} = *${formatRupees(item.lineTotal)}* _(SKU: ${item.sku})_")
        }
        sb.appendLine()
        sb.appendLine("💰 *BILL SUMMARY (GST Included)*")
        sb.appendLine("• Items Subtotal: ${formatRupees(order.subtotal)}")
        sb.appendLine("• Included GST: ${formatRupees(order.taxTotal)}")
        sb.appendLine("• Delivery Fee: ${if (order.deliveryFee <= 0.0) "FREE" else formatRupees(order.deliveryFee)}")
        sb.appendLine("• *GRAND TOTAL: ${formatRupees(order.total)}*")
        sb.appendLine()
        val payModeText = when (order.paymentMethod) {
            "UPI" -> "Manual UPI Transfer (Outside App)"
            "BANK" -> "Manual Bank Transfer / NEFT (Outside App)"
            else -> "Cash on Delivery (Manual Collection)"
        }
        sb.appendLine("💳 *Preferred Payment Mode:* $payModeText")
        if (includeUtr && order.utrReference.isNotBlank()) {
            sb.appendLine("✅ *Paid Reference / UTR:* ${order.utrReference}")
        } else if (order.paymentMethod == "UPI") {
            sb.appendLine("📲 _Please confirm my order & verify manual UPI payment to ${siteConfig.upiId}_")
        } else if (order.paymentMethod == "BANK") {
            sb.appendLine("🏦 _Please confirm my order & verify manual Bank Transfer to A/C ${siteConfig.bankAccountNumber} (${siteConfig.bankIfsc})_")
        } else {
            sb.appendLine("🚚 _Please confirm my order for Cash on Delivery_")
        }
        if (order.customerNote.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("📝 *Customer Note:* ${order.customerNote}")
        }
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.append("Sent via *GMM Organic Android App*")
        return sb.toString()
    }

    // ---------- Settings Actions ----------

    fun toggleLanguage() {
        val next = if (language == "en") "bn" else "en"
        prefs.language = next
        language = next
        com.gmmdistributors.app.ui.Lang.current = next
    }

    fun setLanguageCode(code: String) {
        prefs.language = code
        language = code
        com.gmmdistributors.app.ui.Lang.current = code
    }

    fun setThemeMode(mode: String) {
        prefs.theme = mode
        theme = mode
    }

    fun updateBackendSettings(dummy: Boolean, url: String, whatsapp: String) {
        prefs.useDummyApi = dummy
        prefs.serverUrl = url.ifBlank { BuildConfig.DEFAULT_SERVER }
        prefs.ownerWhatsapp = whatsapp.ifBlank { BuildConfig.OWNER_WHATSAPP }
        useDummyApi = prefs.useDummyApi
        serverUrl = prefs.serverUrl
        message = if (dummy) "Using Built-in Dummy API Mode ✓" else "Switched to Live Django Backend (${prefs.serverUrl})"
        refresh()
    }
}
