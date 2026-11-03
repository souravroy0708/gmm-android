package com.gmmdistributors.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmmdistributors.app.AppController
import com.gmmdistributors.app.Tab
import com.gmmdistributors.app.api.Order
import com.gmmdistributors.app.api.Invoice

@Composable
fun OrdersTab(app: AppController, actions: PhoneActions) {
    val lang = app.language
    val bn = lang == "bn"
    val orders = app.orders

    if (orders.isEmpty() && app.pendingOrders.isEmpty()) {
        Column(
            Modifier.fillMaxSize().background(Paper).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(84.dp).clip(CircleShape).background(Paper2), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Inventory2, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(t("No orders placed yet"), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (bn) "আপনার অর্ডারগুলো এখানে দেখা যাবে।" else "Your organic fertilizer & bio-pesticide orders will appear here.",
                style = MaterialTheme.typography.bodyMedium, color = Muted, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            BigButton(text = t("Browse Organic Products"), onClick = { app.tab = Tab.Shop })
        }
        return
    }

    Column(
        Modifier.fillMaxSize().background(Paper).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(t("My Orders"), style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
            if (app.refreshing) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = AccentGreen)
            else Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = AccentGreen,
                modifier = Modifier.size(26.dp).clickable { app.refresh() })
        }

        // Orders saved on the phone (no internet when ordering, or the send failed)
        app.pendingOrders.forEach { p -> PendingOrderCard(p, app) }
        if (!app.signedIn && orders.isNotEmpty()) {
            Text(
                if (bn) "এই অর্ডারগুলো এই ফোনে আছে। অন্য ফোনেও দেখতে অ্যাকাউন্ট খুলুন।" else "These orders are on this phone. Create an account to see them on any phone.",
                style = MaterialTheme.typography.bodySmall, color = Muted,
                modifier = Modifier.clickable { app.requireSignIn(register = true) },
            )
        }

        orders.forEach { order ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(CardBg)
                    .border(1.dp, PaperLine, RoundedCornerShape(22.dp))
                    .clickable { app.openOrderNo = order.orderNo }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(order.orderNo, style = MaterialTheme.typography.titleLarge)
                        Text(order.createdAt, style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                    OrderStatusChip(order.status, order.statusLabel(lang))
                }
                HorizontalDivider(color = PaperLine)
                order.items.forEach { item ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.quantity} × ${item.localizedName(lang)} (${item.variantLabel})",
                            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(formatRupees(item.lineTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    PaymentChip(order, lang)
                    Text("${t("Grand Total")}: ${formatRupees(order.total)}", style = MaterialTheme.typography.titleMedium, color = AccentGreen)
                }
                if (order.canPay) {
                    SmallPill(
                        text = if (bn) "💳 পেমেন্ট করুন ও UTR দিন →" else "💳 Pay & send UTR →",
                        gold = true,
                        onClick = { app.openOrderNo = order.orderNo },
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PaymentChip(order: Order, lang: String) {
    val (bg, fg) = when (order.paymentStatus) {
        "VERIFIED", "COLLECTED" -> MintSoft to Color(0xFF14472C)
        "REJECTED" -> DangerBg to Danger
        else -> WarnBg to WarnInk
    }
    Box(Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(order.paymentStatusLabel(lang), style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

@Composable
private fun BackPill(label: String, onBack: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(CardBg)
            .border(1.dp, PaperLine, RoundedCornerShape(50))
            .clickable(onClick = onBack)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = AccentGreen, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun KV(label: String, value: String, bold: Boolean = false, big: Boolean = false, color: Color = TextMain) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = if (big) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium,
            color = if (bold || big) TextMain else Muted)
        Text(value, style = if (big) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, color = color)
    }
}

@Composable
fun OrderDetailScreen(
    order: Order,
    app: AppController,
    actions: PhoneActions,
    onBack: () -> Unit,
) {
    val lang = app.language
    val bn = lang == "bn"
    val cfg = app.siteConfig
    var utrInput by rememberSaveable(order.orderNo) { mutableStateOf("") }
    var utrError by remember { mutableStateOf<String?>(null) }
    var confirmCancel by remember { mutableStateOf(false) }
    LaunchedEffect(order.orderNo) { app.refreshOrder(order.orderNo) }  // latest status from the server

    Column(
        Modifier.fillMaxSize().background(Paper).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BackPill(if (bn) "সব অর্ডারে ফিরে যান" else "All Orders", onBack)

        // 1. Status
        AppCard(title = order.orderNo, trailing = { OrderStatusChip(order.status, order.statusLabel(lang)) }) {
            Text(if (bn) "অর্ডারের সময়: ${order.createdAt}" else "Placed on ${order.createdAt}",
                style = MaterialTheme.typography.bodySmall, color = Muted)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(order.paymentMethodLabel(lang), style = MaterialTheme.typography.titleMedium)
                PaymentChip(order, lang)
            }
            if (order.status == "CANCELLED") {
                if (order.cancelReason.isNotBlank()) Text(order.cancelReason, style = MaterialTheme.typography.bodySmall, color = Danger)
            } else {
                val steps = listOf("PLACED", "CONFIRMED", "PACKED", "DISPATCHED", "DELIVERED")
                val currentIdx = steps.indexOf(order.status).coerceAtLeast(0)
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    steps.forEachIndexed { i, step ->
                        val done = i <= currentIdx
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(if (done) Leaf else PaperLine))
                            Spacer(Modifier.height(4.dp))
                            Text(Order.statusLabelFor(step, lang), style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = if (done) AccentGreen else Muted, maxLines = 1)
                        }
                    }
                }
            }
            order.timeline.forEach { e ->
                Text("• ${Order.statusLabelFor(e.status, lang)} — ${e.at}", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }

        if (order.priceChanged && order.clientTotal != null) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(WarnBg).padding(12.dp)) {
                Text(
                    if (bn) "অর্ডারটি পরে পাঠানো হয়েছিল, তখনকার দামে মোট ${formatRupees(order.clientTotal)} থেকে ${formatRupees(order.total)} হয়েছে। আমরা নিশ্চিত করার আগে আপনাকে ফোন করব; না চাইলে বাতিল করতে পারেন।"
                    else "This order was sent later and today's total is ${formatRupees(order.total)} (you saw ${formatRupees(order.clientTotal)}). We will call you before confirming; you can cancel if you prefer.",
                    style = MaterialTheme.typography.bodyMedium, color = WarnInk,
                )
            }
        }

        // 2. Pay (UPI app / bank transfer), then send the UTR to the server
        if (order.canPay) {
            AppCard(title = if (bn) "পেমেন্ট করুন: ${formatRupees(order.total)}" else "Pay ${formatRupees(order.total)}") {
                if (order.paymentStatus == "REJECTED") {
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DangerBg).padding(10.dp)) {
                        Text(
                            (if (bn) "আগের রেফারেন্সটি মেলেনি" else "Your last reference could not be matched") +
                                (if (order.rejectReason.isNotBlank()) ": ${order.rejectReason}" else ".") +
                                (if (bn) " সঠিক UTR আবার দিন।" else " Please send the correct UTR."),
                            style = MaterialTheme.typography.bodySmall, color = Danger,
                        )
                    }
                }
                if (order.paymentMethod == "UPI" && cfg.upiReady) {
                    BigButton(
                        text = if (bn) "📲 UPI অ্যাপে ${formatRupees(order.total)} পেমেন্ট করুন" else "📲 Pay ${formatRupees(order.total)} with a UPI app",
                        gold = true,
                        onClick = { actions.onPayUpi(order) },
                    )
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MintSoft).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("UPI ID", style = MaterialTheme.typography.labelMedium, color = Forest)
                            Text(cfg.upiId, style = MaterialTheme.typography.titleLarge, color = Forest)
                            Text(cfg.upiPayeeName.ifBlank { cfg.businessName }, style = MaterialTheme.typography.bodySmall, color = Color(0xFF2B3A24))
                        }
                        SmallPill(if (bn) "কপি" else "Copy") { actions.onCopyText("UPI ID", cfg.upiId) }
                    }
                }
                if (order.paymentMethod == "BANK" || (order.paymentMethod == "UPI" && !cfg.upiReady)) {
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Paper)
                            .border(1.dp, PaperLine, RoundedCornerShape(14.dp)).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(if (bn) "ব্যাংক অ্যাকাউন্ট (NEFT / IMPS)" else "Bank account (NEFT / IMPS)", style = MaterialTheme.typography.labelMedium, color = Muted)
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = AccentGreen,
                                modifier = Modifier.size(18.dp).clickable {
                                    actions.onCopyText("Bank details",
                                        "${cfg.bankAccountName} | A/C ${cfg.bankAccountNumber} | IFSC ${cfg.bankIfsc} | ${cfg.bankName}")
                                })
                        }
                        Text(cfg.bankAccountName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(cfg.bankName, style = MaterialTheme.typography.bodySmall)
                        Text("A/C: ${cfg.bankAccountNumber}", style = MaterialTheme.typography.titleMedium, color = AccentGreen)
                        Text("IFSC: ${cfg.bankIfsc}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    if (bn) "পেমেন্টের পর UPI অ্যাপ বা ব্যাংকের মেসেজে যে রেফারেন্স (UTR) নম্বর পাবেন, সেটি নিচে লিখুন।"
                    else "After paying, type the reference (UTR) number shown in your UPI app or bank SMS.",
                    style = MaterialTheme.typography.bodySmall, color = Muted,
                )
                AppField(
                    label = if (order.paymentMethod == "UPI") (if (bn) "১২ সংখ্যার UPI রেফারেন্স (UTR)" else "12-digit UPI reference (UTR)")
                    else (if (bn) "ব্যাংক রেফারেন্স নম্বর" else "Bank transfer reference"),
                    value = utrInput,
                    onChange = { v ->
                        utrInput = if (order.paymentMethod == "UPI") v.filter { it.isDigit() }.take(12)
                        else v.filter { it.isLetterOrDigit() }.uppercase().take(30)
                        utrError = null
                    },
                    keyboard = if (order.paymentMethod == "UPI") KeyboardType.Number else KeyboardType.Ascii,
                    error = utrError,
                    placeholder = if (order.paymentMethod == "UPI") "430518920144" else "SBIN0000XXXXXX",
                )
                BigButton(
                    text = if (app.busy) "…" else (if (bn) "রেফারেন্স পাঠান" else "Send payment reference"),
                    enabled = !app.busy,
                    onClick = {
                        val okFormat = if (order.paymentMethod == "UPI") utrInput.length == 12 else utrInput.length in 6..30
                        if (!okFormat) {
                            utrError = if (order.paymentMethod == "UPI") (if (bn) "১২ সংখ্যার UTR দিন।" else "Enter the 12-digit UTR.")
                            else (if (bn) "৬-৩০ অক্ষরের রেফারেন্স দিন।" else "Enter the 6-30 character reference.")
                        } else {
                            app.submitOrderUtr(order.orderNo, utrInput) { utrInput = "" }
                        }
                    },
                )
            }
        } else if (order.paymentStatus == "PROOF_SUBMITTED") {
            AppCard {
                Text(
                    (if (bn) "আপনার পেমেন্ট রেফারেন্স ${order.utrReference} পাওয়া গেছে। যাচাই করে আমরা অর্ডার পাঠাব।"
                    else "We received your payment reference ${order.utrReference}. We will verify it and dispatch your order."),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        // 3. Items & GST
        AppCard(title = if (bn) "অর্ডারের পণ্য ও GST হিসাব" else "Order Items & GST Breakdown") {
            order.items.forEach { i ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("${i.quantity} × ${i.localizedName(lang)} (${i.variantLabel})", style = MaterialTheme.typography.titleMedium)
                        Text("HSN ${i.hsnCode} · GST ${fmtRate(i.gstRate)}% (${formatRupees(i.taxableValue)} + ${formatRupees(i.taxAmount)})",
                            style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                    Text(formatRupees(i.lineTotal), style = MaterialTheme.typography.titleMedium)
                }
            }
            HorizontalDivider(color = PaperLine)
            KV(t("Items Subtotal (incl. GST)"), formatRupees(order.subtotal), bold = true)
            KV(t("Included GST"), formatRupees(order.taxTotal))
            KV(t("Delivery Fee"), if (order.deliveryFee <= 0.0) t("Free") else formatRupees(order.deliveryFee))
            KV(t("Grand Total"), formatRupees(order.total), big = true, color = AccentGreen)
        }

        // 4. Address
        AppCard(title = t("1. Delivery Address")) {
            Text("${order.address.fullName} · ${order.address.phone}", style = MaterialTheme.typography.titleMedium)
            Text(order.address.formattedLine(), style = MaterialTheme.typography.bodyMedium, color = Muted)
            if (order.customerNote.isNotBlank()) Text("Note: ${order.customerNote}", style = MaterialTheme.typography.bodySmall, color = AccentGreen)
        }

        // 5. Invoice, WhatsApp, cancel
        if (order.invoiceAvailable) {
            BigButton(text = "🧾 ${t("View GST Invoice")}", gold = true, onClick = { app.openInvoice(order.orderNo) })
        } else if (order.status != "CANCELLED") {
            Text(if (bn) "অর্ডার নিশ্চিত হলে GST ইনভয়েস এখানে পাবেন।" else "Your GST invoice appears here once we confirm the order.",
                style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        if (order.status != "CANCELLED" && app.ownerWhatsapp.isNotBlank()) {
            GhostButton(text = if (bn) "💬 হোয়াটসঅ্যাপে আমাদের জানান" else "💬 Message us on WhatsApp", onClick = { actions.onSendOrderWhatsApp(order) })
        }
        if (order.canCancel) {
            GhostButton(text = t("Cancel Order"), danger = true, onClick = { confirmCancel = true })
        }
        Spacer(Modifier.height(24.dp))
    }

    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            title = { Text(if (bn) "অর্ডার বাতিল করবেন?" else "Cancel order ${order.orderNo}?") },
            text = {
                Text(
                    if (bn) "আপনি কি নিশ্চিত? পেমেন্ট করে থাকলে আমরা ফেরত দেওয়ার ব্যবস্থা করব।"
                    else "Are you sure? If you have already paid, we will arrange a refund."
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmCancel = false; app.cancelOrder(order.orderNo) }) {
                    Text(t("Cancel Order"), color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmCancel = false }) { Text(if (bn) "না" else "Keep order", color = TextMain) }
            },
        )
    }
}

fun fmtRate(rate: Double): String = if (rate % 1.0 == 0.0) rate.toInt().toString() else rate.toString()

/** GST tax invoice issued by the server (`apps/invoicing`): real number, CGST+SGST or IGST split. */
@Composable
fun InvoiceScreen(app: AppController, actions: PhoneActions, onBack: () -> Unit) {
    val lang = app.language
    val bn = lang == "bn"
    val inv: Invoice? = app.invoice

    Column(
        Modifier.fillMaxSize().background(Paper).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BackPill(if (bn) "অর্ডারে ফিরে যান" else "Back to Order", onBack)

        if (inv == null) {
            AppCard {
                val e = app.invoiceError
                if (e == null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = AccentGreen)
                        Spacer(Modifier.width(10.dp))
                        Text(if (bn) "ইনভয়েস আনা হচ্ছে…" else "Loading invoice…")
                    }
                } else {
                    Text(e, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {

        AppCard {
            Text("TAX INVOICE", style = MaterialTheme.typography.labelMedium, color = AccentGreen)
            Text(inv.seller.name, style = MaterialTheme.typography.titleLarge)
            if (inv.seller.address.isNotBlank()) Text(inv.seller.address, style = MaterialTheme.typography.bodySmall, color = Muted)
            Text(
                listOfNotNull(
                    inv.seller.gstin.takeIf { it.isNotBlank() }?.let { "GSTIN: $it" },
                    inv.seller.cin.takeIf { it.isNotBlank() }?.let { "CIN: $it" },
                    "State: ${inv.seller.state} (${inv.seller.stateCode})",
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall, color = Muted,
            )
            HorizontalDivider(color = PaperLine)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Invoice No", style = MaterialTheme.typography.bodySmall, color = Muted)
                    Text(inv.invoiceNo, style = MaterialTheme.typography.titleMedium)
                    Text(inv.issuedDate, style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Order", style = MaterialTheme.typography.bodySmall, color = Muted)
                    Text(inv.orderNo, style = MaterialTheme.typography.titleMedium)
                    Text(inv.orderDate, style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
            HorizontalDivider(color = PaperLine)
            Text("Bill to", style = MaterialTheme.typography.labelMedium, color = Muted)
            Text("${inv.buyer.fullName} (${inv.buyer.phone})", style = MaterialTheme.typography.titleMedium)
            Text(inv.buyer.formattedLine(), style = MaterialTheme.typography.bodySmall, color = Muted)
            HorizontalDivider(color = PaperLine)
            inv.items.forEachIndexed { idx, item ->
                Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${idx + 1}. ${item.productName} (${item.variantLabel})", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text(formatRupees(item.lineTotal), style = MaterialTheme.typography.titleMedium)
                    }
                    Text("HSN ${item.hsnCode} · ${item.quantity} × ${formatRupees(item.unitPrice)} · Taxable ${formatRupees(item.taxableValue)} · GST ${fmtRate(item.gstRate)}% ${formatRupees(item.taxAmount)}",
                        style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
            HorizontalDivider(color = PaperLine)
            KV("Taxable value", formatRupees(inv.taxableTotal))
            if (inv.isInterstate) KV("IGST", formatRupees(inv.igst))
            else { KV("CGST", formatRupees(inv.cgst)); KV("SGST", formatRupees(inv.sgst)) }
            KV("Delivery", if (inv.deliveryFee <= 0.0) "FREE" else formatRupees(inv.deliveryFee))
            KV("Grand total", formatRupees(inv.total), big = true, color = AccentGreen)
            Text("Prices include GST. Payment: ${inv.paymentMethodLabel} (${inv.paymentStatusLabel}).",
                style = MaterialTheme.typography.bodySmall, color = Muted)
        }

        BigButton(text = if (bn) "📄 PDF শেয়ার / সেভ করুন" else "📄 Share / save PDF", gold = true, onClick = { actions.onShareInvoicePdf(inv) })
        }
        Spacer(Modifier.height(24.dp))
    }
}


@Composable
private fun PendingOrderCard(p: com.gmmdistributors.app.api.PendingOrder, app: AppController) {
    val bn = app.language == "bn"
    val failed = p.status == "failed"
    var confirmDelete by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(if (failed) DangerBg else WarnBg).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (failed) (if (bn) "⚠ অর্ডার পাঠানো যায়নি" else "⚠ Order could not be sent")
                else (if (bn) "⏳ ইন্টারনেটের অপেক্ষায়" else "⏳ Waiting for internet"),
                style = MaterialTheme.typography.titleMedium, color = if (failed) Danger else WarnInk, modifier = Modifier.weight(1f),
            )
            Text(formatRupees(p.expectedTotal), style = MaterialTheme.typography.titleMedium, color = TextMain)
        }
        p.lines.forEach { l ->
            Text("${l.quantity} × ${l.localizedName(app.language)} (${l.label})", style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            if (failed) p.error
            else (if (bn) "ফোনে সেভ আছে। নেট এলে নিজে থেকেই পাঠানো হবে।" else "Saved on this phone. It will be sent automatically when you are online."),
            style = MaterialTheme.typography.bodySmall, color = if (failed) Danger else WarnInk,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (failed) {
                SmallPill(if (bn) "কার্টে নিয়ে ঠিক করুন" else "Fix in cart", gold = true) { app.editPending(p) }
            } else {
                SmallPill(if (bn) "এখনই পাঠান" else "Send now", gold = true) { app.retryPending() }
            }
            SmallPill(if (bn) "মুছুন" else "Delete") { confirmDelete = true }
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(if (bn) "সেভ করা অর্ডার মুছবেন?" else "Delete this saved order?") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; app.deletePending(p.key) }) { Text(if (bn) "মুছুন" else "Delete", color = Danger) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(if (bn) "না" else "Keep") } },
        )
    }
}
