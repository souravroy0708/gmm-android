package com.gmmdistributors.app

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gmmdistributors.app.api.Address
import com.gmmdistributors.app.api.CartLine
import com.gmmdistributors.app.api.Category
import com.gmmdistributors.app.api.Invoice
import com.gmmdistributors.app.api.Me
import com.gmmdistributors.app.api.Order
import com.gmmdistributors.app.api.PendingLine
import com.gmmdistributors.app.api.PendingOrder
import com.gmmdistributors.app.api.Quote
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
import java.util.UUID

enum class Tab { Shop, Advisory, Cart, Orders, Profile }

/**
 * Central state holder (same architecture as `bas1minute-android`'s AppController).
 * Everything that matters (catalogue, prices, stock, addresses, orders, payments, invoices)
 * comes from the Django backend via [Repo]; the phone only caches it for offline viewing.
 */
class AppController(private val ctx: Context, private val scope: CoroutineScope) {
    val prefs = Prefs(ctx)
    val db = LocalDb.get(ctx)
    val repo = Repo(ctx)

    var tab by mutableStateOf(Tab.Shop)
    var language by mutableStateOf(prefs.language)
    var theme by mutableStateOf(prefs.theme)
    var serverUrl by mutableStateOf(prefs.serverUrl)

    // Account
    var me by mutableStateOf<Me?>(null)
    /** A real (member) account. Guests can browse and order without one. */
    val signedIn: Boolean get() = me?.isGuest == false
    /** Name / phone / address a guest typed once; kept on the phone and sent with each order. */
    var guestAddress by mutableStateOf<Address?>(null)
    var authOpen by mutableStateOf(false)
    var authRegister by mutableStateOf(false)
    /** Sign-in / sign-up error shown inside the form (a toast would hide behind the keyboard). */
    var authError by mutableStateOf<String?>(null)
    private var afterAuth: (() -> Unit)? = null

    // Catalogue (live from /api/v1/catalog/)
    var siteConfig by mutableStateOf(SiteConfig())
    var categories by mutableStateOf<List<Category>>(emptyList())
    var products by mutableStateOf<List<Product>>(emptyList())
    var pins by mutableStateOf<List<ServiceablePin>>(emptyList())
    var catalogLoaded by mutableStateOf(false)

    // Shop filters & active product
    var selectedCategorySlug by mutableStateOf("all")
    var searchQuery by mutableStateOf("")
    var openProductSlug by mutableStateOf<String?>(null)

    // Advisory & Thought Card
    var openCropSlug by mutableStateOf<String?>(null)
    var thoughtCardOpen by mutableStateOf(false)

    // Cart & addresses
    var cartQuantities by mutableStateOf<Map<Int, Int>>(db.cartMap())
    var addresses by mutableStateOf<List<Address>>(emptyList())
    var selectedAddressId by mutableStateOf<Long?>(null)
    var addingAddress by mutableStateOf(false)
    var editingAddress by mutableStateOf<Address?>(null)
    private var checkoutKey = UUID.randomUUID().toString()

    // Orders & invoice
    var orders by mutableStateOf<List<Order>>(emptyList())
    var openOrderNo by mutableStateOf<String?>(null)
    var invoiceOrderNo by mutableStateOf<String?>(null)
    var invoice by mutableStateOf<Invoice?>(null)
    var invoiceError by mutableStateOf<String?>(null)
    var newlyPlacedOrder by mutableStateOf<Order?>(null)

    // Offline-first ordering
    var pendingOrders by mutableStateOf<List<PendingOrder>>(emptyList())
    var queuedOrder by mutableStateOf<PendingOrder?>(null)   // just saved offline -> "will send when online" screen
    var quoteReview by mutableStateOf<Quote?>(null)          // prices changed since the phone's copy -> ask once
    var pricesCheckedAt by mutableStateOf<Long?>(null)
    private var pendingCheckout: (() -> Unit)? = null

    // Profile sub-screens ("vision", "leadership", "contact", "visiting_card", "account")
    var companyPage by mutableStateOf<String?>(null)

    // UI status
    var refreshing by mutableStateOf(false)
    var busy by mutableStateOf(false)
    var offline by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)

    val needsUpdate: Boolean get() = siteConfig.androidMinVersion > BuildConfig.VERSION_CODE

    init {
        launch {
            repo.cachedCatalog()?.let { applyCatalog(it) }  // SQLite snapshot, or the copy bundled in the APK
            pricesCheckedAt = repo.catalogCheckedAt()
            me = repo.cachedMe()
            guestAddress = repo.guestAddress()
            pendingOrders = repo.pendingOrders()
            if (pendingOrders.any { it.status == "waiting" }) SyncWorker.schedule(ctx)
            if (repo.signedIn) {
                addresses = repo.cachedAddresses()
                orders = repo.cachedOrders()
                pickAddress()
            }
        }
        refresh()
    }

    fun launch(block: suspend () -> Unit) {
        scope.launch { block() }
    }

    private fun say(en: String, bn: String) = if (language == "bn") bn else en

    private fun applyCatalog(c: com.gmmdistributors.app.api.CatalogPayload) {
        siteConfig = c.config
        categories = c.categories
        products = c.products
        pins = c.pins
        catalogLoaded = true
    }

    private fun pickAddress() {
        if (selectedAddressId == null || addresses.none { it.id == selectedAddressId }) {
            selectedAddressId = addresses.firstOrNull { it.isDefault }?.id ?: addresses.firstOrNull()?.id
        }
    }

    /** Server said the token is no longer valid (signed out elsewhere, password changed, account closed). */
    private suspend fun sessionExpired() {
        val wasMember = signedIn
        repo.clearSession()
        me = null
        addresses = emptyList()
        orders = emptyList()
        if (wasMember) message = say("Please sign in again.", "আবার সাইন ইন করুন।")
    }

    private suspend fun <T> handle(res: Res<T>, onOk: suspend (T) -> Unit): Boolean = when (res) {
        is Res.Ok -> { onOk(res.value); true }
        is Res.Fail -> {
            if (res.authExpired) sessionExpired() else message = res.message
            false
        }
    }

    fun refresh() = launch {
        if (refreshing) return@launch
        refreshing = true
        val cat = repo.loadCatalog()
        cat.value?.let { applyCatalog(it) }
        offline = cat.offline
        if (!cat.offline) pricesCheckedAt = repo.catalogCheckedAt()
        if (cat.value == null) catalogLoaded = true  // stop the spinner; screen shows the offline message
        if (!cat.offline) syncPendingNow()
        if (repo.signedIn && !cat.offline) {
            when (val r = repo.refreshMe(language)) {
                is Res.Ok -> me = r.value
                is Res.Fail -> if (r.authExpired) sessionExpired()
            }
            if (repo.signedIn) {
                (repo.loadAddresses(language) as? Res.Ok)?.let { addresses = it.value; pickAddress() }
                (repo.loadOrders(language) as? Res.Ok)?.let { orders = it.value }
            }
        }
        cartQuantities = withContext(Dispatchers.IO) { db.cartMap() }
        refreshing = false
    }

    // ---------- Account ----------

    /** Opens the sign-in screen; [then] runs after a successful sign-in (e.g. continue to checkout). */
    fun requireSignIn(register: Boolean = false, then: (() -> Unit)? = null) {
        if (signedIn) { then?.invoke(); return }
        afterAuth = then
        authRegister = register
        authError = null
        authOpen = true
    }

    private suspend fun afterSignIn(user: Me) {
        me = user
        guestAddress?.let { g -> if (addresses.isEmpty()) launch { repo.saveAddress(g.copy(id = 0L, isDefault = true), language) } }
        authOpen = false
        if (user.language != language && user.language in setOf("en", "bn")) setLanguageCode(user.language)
        (repo.loadAddresses(language) as? Res.Ok)?.let { addresses = it.value; pickAddress() }
        (repo.loadOrders(language) as? Res.Ok)?.let { orders = it.value }
        message = say("Welcome, ${user.name.substringBefore(' ')}!", "স্বাগতম, ${user.name.substringBefore(' ')}!")
        afterAuth?.invoke()
        afterAuth = null
    }

    fun login(phone: String, password: String) = launch {
        busy = true
        authError = null
        when (val r = repo.login(phone, password, language)) {
            is Res.Ok -> afterSignIn(r.value)
            is Res.Fail -> authError = r.message
        }
        busy = false
    }

    fun register(name: String, phone: String, password: String) = launch {
        busy = true
        authError = null
        when (val r = repo.register(name, phone, password, language)) {
            is Res.Ok -> afterSignIn(r.value)
            is Res.Fail -> authError = r.message
        }
        busy = false
    }

    fun updateProfile(name: String, email: String) = launch {
        busy = true
        handle(repo.updateMe(name, email, language)) {
            me = it
            message = say("Your details are saved ✓", "আপনার তথ্য সেভ হয়েছে ✓")
        }
        busy = false
    }

    fun logout() = launch {
        busy = true
        repo.logout()
        me = null
        addresses = emptyList()
        orders = emptyList()
        selectedAddressId = null
        openOrderNo = null
        invoiceOrderNo = null
        companyPage = null
        busy = false
        message = say("Signed out.", "সাইন আউট হয়েছে।")
    }

    fun deleteAccount() = launch {
        busy = true
        handle(repo.deleteAccount(language)) {
            me = null
            addresses = emptyList()
            orders = emptyList()
            companyPage = null
            message = say("Your account has been deleted.", "আপনার অ্যাকাউন্ট মুছে ফেলা হয়েছে।")
        }
        busy = false
    }

    // ---------- Computed cart helpers ----------

    private val variantLookup get() = products.flatMap { p -> p.variants.map { v -> v.id to (p to v) } }.toMap()

    val cartLines: List<CartLine>
        get() {
            val lookup = variantLookup
            return cartQuantities.mapNotNull { (variantId, qty) ->
                val pair = lookup[variantId] ?: return@mapNotNull null
                if (qty > 0) CartLine(pair.first, pair.second, qty) else null
            }
        }

    /** Packs in the cart that are sold out or below the wanted quantity right now. */
    val cartProblems: List<CartLine> get() = cartLines.filter { it.quantity > it.variant.stock }

    val cartCount: Int get() = cartLines.sumOf { it.quantity }
    val cartSubtotal: Double get() = cartLines.sumOf { it.lineTotal }
    val cartTaxTotal: Double get() = cartLines.sumOf { it.taxAmount }

    fun isPinServiceable(pincode: String): ServiceablePin? = pins.firstOrNull { it.pincode == pincode.trim() }

    /** Same rule as `place_order` on the server. Null when the PIN is not served. */
    fun deliveryFeeFor(pincode: String, subtotal: Double = cartSubtotal): Double? {
        val pin = isPinServiceable(pincode) ?: return null
        if (subtotal <= 0.0) return 0.0
        val free = siteConfig.freeDeliveryAbove
        return if (free != null && subtotal >= free) 0.0 else pin.deliveryFee
    }

    // ---------- Cart actions ----------

    fun setVariantQty(variantId: Int, newQty: Int) {
        val current = cartQuantities[variantId] ?: 0
        if (current == 0 && newQty > 0 && cartQuantities.size >= 10) {
            message = say("Your cart can hold at most 10 different products.", "কার্টে সর্বোচ্চ ১০টি আলাদা পণ্য রাখা যায়।")
            return
        }
        val stock = variantLookup[variantId]?.second?.stock ?: 0
        var clamped = newQty.coerceIn(0, 10)
        if (clamped > current && clamped > stock) {
            clamped = maxOf(stock, 0).coerceAtMost(10)
            message = if (stock <= 0) say("Sorry, this pack is out of stock.", "দুঃখিত, এই প্যাকটি স্টকে নেই।")
            else say("Only $stock left in stock.", "স্টকে মাত্র $stock টি আছে।")
            if (clamped <= current) return
        }
        db.setCartQty(variantId, clamped)
        cartQuantities = db.cartMap()
        checkoutKey = UUID.randomUUID().toString()  // a changed cart is a new order attempt
        if (clamped > current && message == null) message = say("Added to cart ✓", "কার্টে যোগ হয়েছে ✓")
    }

    fun clearCart() {
        db.clearCart()
        cartQuantities = emptyMap()
        checkoutKey = UUID.randomUUID().toString()
    }

    /** Most recent finished order whose packs are still on sale: shown as "Order again". */
    val reorderCandidate: Order?
        get() {
            val lookup = variantLookup
            return orders.firstOrNull { o ->
                !o.isActive && o.items.isNotEmpty() && o.items.any { lookup[it.variantId]?.second?.inStock == true }
            }
        }

    /** Puts the packs of a past order back in the cart (limited by today's stock) and opens the cart. */
    fun reorder(order: Order) {
        val lookup = variantLookup
        var added = 0
        var skipped = 0
        order.items.forEach { item ->
            val v = lookup[item.variantId]?.second
            val qty = minOf(item.quantity, v?.stock ?: 0, 10)
            if (v == null || qty <= 0 || (cartQuantities[v.id] == null && cartQuantities.size >= 10)) { skipped++; return@forEach }
            db.setCartQty(v.id, maxOf(qty, cartQuantities[v.id] ?: 0))
            added++
        }
        cartQuantities = db.cartMap()
        checkoutKey = UUID.randomUUID().toString()
        message = when {
            added == 0 -> say("These packs are not available right now.", "এই প্যাকগুলি এখন পাওয়া যাচ্ছে না।")
            skipped > 0 -> say("Added to cart. Some packs are not available now.", "কার্টে যোগ হয়েছে। কিছু প্যাক এখন নেই।")
            else -> say("Added to cart ✓ Check and order.", "কার্টে যোগ হয়েছে ✓ দেখে নিয়ে অর্ডার করুন।")
        }
        if (added > 0) tab = Tab.Cart
    }

    // ---------- Address actions (saved on the server) ----------

    fun saveAddress(address: Address, onSaved: () -> Unit = {}) = launch {
        if (!signedIn) {  // guest: details stay on this phone and travel with each order
            val g = address.copy(id = 0L, isDefault = true)
            repo.saveGuestAddress(g)
            guestAddress = g
            addingAddress = false
            editingAddress = null
            message = say("Delivery details saved ✓", "ডেলিভারির তথ্য সেভ হয়েছে ✓")
            onSaved()
            return@launch
        }
        busy = true
        handle(repo.saveAddress(address, language)) { (saved, all) ->
            addresses = all
            selectedAddressId = saved.id
            addingAddress = false
            editingAddress = null
            message = say("Delivery address saved ✓", "ঠিকানা সেভ হয়েছে ✓")
            onSaved()
        }
        busy = false
    }

    fun deleteAddress(id: Long) = launch {
        busy = true
        handle(repo.deleteAddress(id, language)) {
            addresses = it
            if (selectedAddressId == id) selectedAddressId = null
            pickAddress()
            addingAddress = false
            editingAddress = null
            message = say("Address removed.", "ঠিকানা মুছে ফেলা হয়েছে।")
        }
        busy = false
    }

    fun selectDefaultAddress(id: Long) = launch {
        handle(repo.setDefaultAddress(id, language)) { addresses = it; selectedAddressId = id }
    }

    // ---------- Orders ----------

    private fun isOnline(): Boolean = runCatching {
        val cm = ctx.getSystemService(android.net.ConnectivityManager::class.java)
        val caps = cm?.getNetworkCapabilities(cm.activeNetwork)
        caps?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }.getOrDefault(true)

    /**
     * Offline-first checkout.
     *  - No internet: the order is saved on the phone and sent automatically later (SyncWorker).
     *  - Internet: (silent guest session if needed) -> live quote. If prices changed or stock is short,
     *    the cart is updated and the customer confirms once; otherwise the order is placed straight away.
     */
    fun checkout(address: Address, paymentMethod: String, note: String, confirmed: Boolean = false, onPlaced: () -> Unit): Unit = launch {
        if (busy) return@launch
        val lines = cartLines
        if (lines.isEmpty()) {
            message = say("Your cart is empty.", "আপনার কার্ট খালি।")
            return@launch
        }
        val shownTotal = cartSubtotal + (deliveryFeeFor(address.pincode) ?: 0.0)
        if (!isOnline()) { queue(address, paymentMethod, note, lines, shownTotal); onPlaced(); return@launch }

        busy = true
        val session = repo.ensureSession(language)
        if (session is Res.Fail) {
            busy = false
            if (session.offline) { queue(address, paymentMethod, note, lines, shownTotal); onPlaced() } else message = session.message
            return@launch
        }
        (session as? Res.Ok)?.let { if (me == null) me = it.value }

        var expected = shownTotal
        if (!confirmed) {
            when (val q = repo.quote(lines, address.pincode, language)) {
                is Res.Fail -> if (q.offline) { busy = false; queue(address, paymentMethod, note, lines, shownTotal); onPlaced(); return@launch }
                is Res.Ok -> {
                    val quote = q.value
                    if (quote.priceChanged || quote.stockShort) {
                        applyQuote(quote)
                        busy = false
                        quoteReview = quote
                        pendingCheckout = { checkout(address, paymentMethod, note, confirmed = true, onPlaced = onPlaced) }
                        if (quote.catalogVersion.isNotBlank()) refresh()  // fresh snapshot for the whole catalogue
                        return@launch
                    }
                    if (quote.blocked) {
                        busy = false
                        message = say("This order cannot be placed: check the PIN code and minimum order.",
                            "এই অর্ডার করা যাচ্ছে না: পিন কোড ও সর্বনিম্ন অর্ডার দেখে নিন।")
                        return@launch
                    }
                    expected = quote.total
                }
            }
        }

        val placeLines = cartLines
        when (val res = repo.placeOrder(address, placeLines, paymentMethod, note, checkoutKey, expected, language)) {
            is Res.Ok -> {
                checkoutKey = UUID.randomUUID().toString()
                cartQuantities = withContext(Dispatchers.IO) { db.cartMap() }
                orders = repo.cachedOrders()
                newlyPlacedOrder = res.value
                openOrderNo = res.value.orderNo
                busy = false
                onPlaced()
                refresh()
            }
            is Res.Fail -> {
                busy = false
                when {
                    res.offline -> { queue(address, paymentMethod, note, placeLines, expected); onPlaced() }
                    res.authExpired -> { sessionExpired(); message = say("Please try again.", "আবার চেষ্টা করুন।") }
                    else -> { message = res.message; refresh() }
                }
            }
        }
    }

    /** Customer accepted the updated prices / quantities. */
    fun confirmQuote() {
        quoteReview = null
        val next = pendingCheckout
        pendingCheckout = null
        next?.invoke()
    }

    fun dismissQuote() {
        quoteReview = null
        pendingCheckout = null
    }

    /** Put today's prices into the phone's catalogue and trim the cart to today's stock. */
    private fun applyQuote(q: Quote) {
        val byId = q.lines.associateBy { it.variantId }
        products = products.map { p ->
            p.copy(variants = p.variants.map { v ->
                val l = byId[v.id] ?: return@map v
                if (!l.available) v.copy(stock = 0) else v.copy(price = l.price, stock = l.stock)
            })
        }
        q.lines.forEach { l ->
            val have = cartQuantities[l.variantId] ?: 0
            val allowed = if (!l.available) 0 else minOf(have, l.stock)
            if (allowed != have) db.setCartQty(l.variantId, allowed)
        }
        cartQuantities = db.cartMap()
        checkoutKey = UUID.randomUUID().toString()
    }

    private suspend fun queue(address: Address, paymentMethod: String, note: String, lines: List<CartLine>, total: Double) {
        val p = PendingOrder(
            key = "off-" + UUID.randomUUID().toString(),
            createdAt = System.currentTimeMillis(),
            paymentMethod = paymentMethod,
            address = address.copy(id = 0L),
            lines = lines.map { PendingLine(it.variant.id, it.product.name, it.product.nameBn, it.variant.label, it.quantity, it.variant.price) },
            expectedTotal = total,
            note = note,
        )
        repo.queueOrder(p)
        cartQuantities = emptyMap()
        checkoutKey = UUID.randomUUID().toString()
        pendingOrders = repo.pendingOrders()
        queuedOrder = p
        SyncWorker.schedule(ctx)
    }

    private var syncing = false

    /** Send orders saved offline. Called on reconnect, on refresh and from the Orders screen. */
    private suspend fun syncPendingNow(includeFailed: Boolean = false) {
        if (syncing) return
        if (repo.pendingOrders().none { it.status == "waiting" || includeFailed }) { pendingOrders = repo.pendingOrders(); return }
        syncing = true
        val r = repo.flushPending(language, includeFailed)
        syncing = false
        pendingOrders = repo.pendingOrders()
        if (me == null) me = repo.cachedMe()
        if (r.sent.isNotEmpty()) {
            orders = repo.cachedOrders()
            message = if (r.sent.size == 1) say("Your saved order ${r.sent[0].orderNo} has been sent ✓", "আপনার সেভ করা অর্ডার ${r.sent[0].orderNo} পাঠানো হয়েছে ✓")
            else say("${r.sent.size} saved orders have been sent ✓", "${r.sent.size}টি সেভ করা অর্ডার পাঠানো হয়েছে ✓")
        }
        if (r.failed > 0) message = say("A saved order could not be sent. Open Orders to fix it.", "একটি সেভ করা অর্ডার পাঠানো যায়নি। অর্ডার পাতায় দেখুন।")
        if (r.stillWaiting > 0) SyncWorker.schedule(ctx)
    }

    fun retryPending() = launch { syncPendingNow(includeFailed = true) }

    fun deletePending(key: String) = launch {
        repo.deletePending(key)
        pendingOrders = repo.pendingOrders()
        message = say("Saved order removed.", "সেভ করা অর্ডার মুছে ফেলা হয়েছে।")
    }

    /** Put a failed saved order back in the cart so the customer can fix quantities and send again. */
    fun editPending(p: PendingOrder) = launch {
        p.lines.forEach { l ->
            val stock = variantLookup[l.variantId]?.second?.stock ?: 0
            if (stock > 0) db.setCartQty(l.variantId, minOf(l.quantity, stock, 10))
        }
        cartQuantities = db.cartMap()
        repo.deletePending(p.key)
        pendingOrders = repo.pendingOrders()
        tab = Tab.Cart
    }

    /** Network came back (ConnectivityManager callback in MainActivity). */
    fun onNetworkAvailable() {
        if (offline || pendingOrders.isNotEmpty()) refresh()
    }

    /** Short order text for SMS (works without mobile data). */
    fun buildSmsOrder(p: PendingOrder): String = buildString {
        append("GMM order: ")
        append(p.lines.joinToString(", ") { "${it.quantity}x ${it.productName} ${it.label}" })
        append(". Total approx Rs ${"%.0f".format(java.util.Locale.US, p.expectedTotal)}")
        append(". ${if (p.paymentMethod == "COD") "Cash on delivery" else p.paymentMethod}")
        append(". ${p.address.fullName}, ${p.address.phone}, ${p.address.villageTown} ${p.address.pincode}")
    }

    private fun replaceOrder(o: Order) {
        orders = listOf(o) + orders.filter { it.orderNo != o.orderNo }
        orders = orders.sortedByDescending { it.createdAtIso }
    }

    fun refreshOrder(orderNo: String) = launch {
        handle(repo.refreshOrder(orderNo, language)) { replaceOrder(it) }
    }

    fun cancelOrder(orderNo: String) = launch {
        busy = true
        handle(repo.cancelOrder(orderNo, language)) {
            replaceOrder(it)
            message = say("Order $orderNo cancelled.", "অর্ডার বাতিল করা হয়েছে।")
            refresh()
        }
        busy = false
    }

    fun submitOrderUtr(orderNo: String, utr: String, onDone: (Order) -> Unit = {}) = launch {
        busy = true
        handle(repo.submitUtr(orderNo, utr.trim(), language)) {
            replaceOrder(it)
            message = say("Payment reference sent. We will verify it soon ✓", "পেমেন্ট রেফারেন্স পাঠানো হয়েছে। শীঘ্রই যাচাই করা হবে ✓")
            onDone(it)
        }
        busy = false
    }

    fun markOrderWhatsappSent(orderNo: String) = launch {
        repo.markWhatsappSent(orderNo)
        orders = orders.map { if (it.orderNo == orderNo) it.copy(whatsappSent = true) else it }
    }

    fun openInvoice(orderNo: String) {
        invoice = null
        invoiceError = null
        invoiceOrderNo = orderNo
        launch {
            when (val r = repo.loadInvoice(orderNo, language)) {
                is Res.Ok -> invoice = r.value
                is Res.Fail -> if (r.authExpired) sessionExpired() else invoiceError = r.message
            }
        }
    }

    /** `upi://pay` deep link: opens the customer's own UPI app (GPay, PhonePe, Paytm, BHIM...) with the
     *  amount filled in. No payment gateway is involved; the customer then sends the UTR. */
    fun upiPayUri(order: Order): Uri? {
        val cfg = siteConfig
        if (!cfg.upiReady) return null
        return Uri.Builder().scheme("upi").authority("pay")
            .appendQueryParameter("pa", cfg.upiId)
            .appendQueryParameter("pn", cfg.upiPayeeName.ifBlank { cfg.businessName })
            .appendQueryParameter("am", "%.2f".format(java.util.Locale.US, order.total))
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", "GMM order ${order.orderNo}")
            .build()
    }

    val ownerWhatsapp: String
        get() = siteConfig.whatsappNumber.filter { it.isDigit() }.ifBlank { prefs.ownerWhatsapp }

    /**
     * Optional WhatsApp note to the owner. The order itself is already saved on the server and
     * visible in the admin; this message is only a courtesy / faster nudge.
     */
    fun buildWhatsAppOrderMessage(order: Order, includeUtr: Boolean = true): String {
        val sb = StringBuilder()
        sb.appendLine("🌿 *NEW ORGANIC ORDER — GMM DISTRIBUTORS*")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("📦 *Order No:* ${order.orderNo}")
        sb.appendLine("🗓 *Date:* ${order.createdAt}")
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
        sb.appendLine("💳 *Payment:* ${order.paymentMethodLabel("en")} · ${order.paymentStatusLabel("en")}")
        if (includeUtr && order.utrReference.isNotBlank()) {
            sb.appendLine("✅ *UPI / Bank reference (UTR):* ${order.utrReference}")
        } else if (order.paymentMethod == "COD") {
            sb.appendLine("🚚 _Please confirm my order for cash on delivery._")
        } else {
            sb.appendLine("📲 _I will pay and send the UTR from the app._")
        }
        if (order.customerNote.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("📝 *Customer Note:* ${order.customerNote}")
        }
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.append("Sent via *GMM Organic Android App*")
        return sb.toString()
    }

    // ---------- Settings ----------

    fun toggleLanguage() = setLanguageCode(if (language == "en") "bn" else "en")

    fun setLanguageCode(code: String) {
        prefs.language = code
        language = code
        com.gmmdistributors.app.ui.Lang.current = code
        val current = me
        if (current != null && current.language != code) {
            launch { (repo.updateMe(current.name, current.email, code) as? Res.Ok)?.let { me = it.value } }
        }
    }

    fun setThemeMode(mode: String) {
        prefs.theme = mode
        theme = mode
    }

    /** Debug builds only: point the app at another Django server (e.g. a laptop on Wi-Fi). */
    fun updateServerUrl(url: String) {
        if (!BuildConfig.DEBUG) return
        prefs.serverUrl = url.ifBlank { BuildConfig.DEFAULT_SERVER }
        serverUrl = prefs.serverUrl
        message = "Server: ${prefs.serverUrl}"
        refresh()
    }
}
