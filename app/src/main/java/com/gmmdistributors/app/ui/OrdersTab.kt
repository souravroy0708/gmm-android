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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmmdistributors.app.AppController
import com.gmmdistributors.app.Tab
import com.gmmdistributors.app.api.Order
import com.gmmdistributors.app.api.SiteConfig

@Composable
fun OrdersTab(app: AppController, actions: PhoneActions) {
    val lang = app.language
    val orders = app.orders

    if (orders.isEmpty()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Paper)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(84.dp).clip(CircleShape).background(Paper2),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Inventory2, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(t("No orders placed yet"), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (lang == "bn") "আপনার অর্ডারগুলো এখানে দেখা যাবে এবং এক ট্যাপে হোয়াটসঅ্যাপে মালিককে পাঠানো যাবে।"
                else "Your organic fertilizer & bio-pesticide orders will appear here with instant WhatsApp owner notifications.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            BigButton(
                text = t("Browse Organic Products"),
                onClick = { app.tab = Tab.Shop },
            )
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(t("My Orders"), style = MaterialTheme.typography.headlineLarge)

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
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(order.orderNo, style = MaterialTheme.typography.titleLarge)
                        Text(order.createdAt, style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                    OrderStatusChip(order.status, order.statusLabel(lang))
                }

                HorizontalDivider(color = PaperLine)

                order.items.forEach { item ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "${item.quantity} × ${item.localizedName(lang)} (${item.variantLabel})",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(formatRupees(item.lineTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(order.paymentMethodLabel(lang), style = MaterialTheme.typography.bodySmall, color = Muted)
                    Text(
                        "${t("Grand Total")}: ${formatRupees(order.total)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentGreen,
                    )
                }

                if (order.status != "CANCELLED") {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SmallPill(
                            text = if (order.whatsappSent) "💬 ${t("Re-send Order on WhatsApp")}" else "💬 ${t("Send Order to Owner on WhatsApp")}",
                            whatsapp = true,
                            onClick = { actions.onSendOrderWhatsApp(order) },
                        )
                        SmallPill(
                            text = if (lang == "bn") "বিস্তারিত ও পেমেন্ট →" else "Details & Payment →",
                            onClick = { app.openOrderNo = order.orderNo },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
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
    val cfg = app.siteConfig
    var utrInput by rememberSaveable(order.orderNo) { mutableStateOf(order.utrReference) }
    var confirmCancel by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
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
            Text(if (lang == "bn") "সব অর্ডারে ফিরে যান" else "All Orders", style = MaterialTheme.typography.labelLarge)
        }

        // 1. Order Status & WhatsApp Action Hero Card
        AppCard(
            title = order.orderNo,
            trailing = { OrderStatusChip(order.status, order.statusLabel(lang)) }
        ) {
            Text(
                text = if (lang == "bn") "অর্ডারের সময়: ${order.createdAt}" else "Placed on ${order.createdAt}",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
            Text(
                text = "${if (lang == "bn") "পেমেন্ট পদ্ধতি" else "Payment Mode"}: ${order.paymentMethodLabel(lang)}",
                style = MaterialTheme.typography.titleMedium,
            )

            // Status progress bar (PLACED -> CONFIRMED -> PACKED -> DISPATCHED -> DELIVERED)
            if (order.status != "CANCELLED") {
                val steps = listOf("PLACED", "CONFIRMED", "PACKED", "DISPATCHED", "DELIVERED")
                val currentIdx = steps.indexOf(order.status).coerceAtLeast(0)
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    steps.forEachIndexed { i, step ->
                        val done = i <= currentIdx
                        Column(
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (done) Leaf else PaperLine)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = step.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = if (done) AccentGreen else Muted,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            if (order.status != "CANCELLED") {
                BigButton(
                    text = if (order.whatsappSent) "💬 ${t("Re-send Order on WhatsApp")}" else "💬 ${t("Send Order to Owner on WhatsApp")}",
                    whatsapp = true,
                    onClick = { actions.onSendOrderWhatsApp(order) },
                )
            }
        }

        // 2. Manual Offline Payment Box (UPI ID / Bank Account Details + UTR Reference Logger)
        if (order.status != "CANCELLED") {
            AppCard(title = t("Manual UPI / Bank Payment Details")) {
                Text(
                    text = if (lang == "bn") {
                        "এই অ্যাপে কোনো অটোমেটিক পেমেন্ট গেটওয়ে নেই। নিচের যেকোনো মাধ্যমে ম্যানুয়ালি ${formatRupees(order.total)} পেমেন্ট করে UTR নম্বরটি সেভ করুন এবং হোয়াটসঅ্যাপে মালিককে জানিয়ে দিন:"
                    } else {
                        "There is no in-app payment gateway. Please transfer ${formatRupees(order.total)} manually outside the app via UPI or Bank Transfer (or pay on delivery) and share the reference on WhatsApp:"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )

                // UPI Box
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MintSoft)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Official UPI ID", style = MaterialTheme.typography.labelMedium, color = Forest)
                        Text(cfg.upiId, style = MaterialTheme.typography.titleLarge, color = Forest)
                        Text(cfg.businessName, style = MaterialTheme.typography.bodySmall, color = Color(0xFF2B3A24))
                    }
                    SmallPill("Copy UPI") {
                        actions.onCopyText("UPI ID", cfg.upiId)
                    }
                }

                // Bank Account Box
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Paper)
                        .border(1.dp, PaperLine, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Bank Account (NEFT / IMPS)", style = MaterialTheme.typography.labelMedium, color = Muted)
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "Copy Bank Details",
                            tint = AccentGreen,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    actions.onCopyText(
                                        "Bank Details",
                                        "${cfg.bankAccountName} | A/C: ${cfg.bankAccountNumber} | IFSC: ${cfg.bankIfsc} | ${cfg.bankName}"
                                    )
                                },
                        )
                    }
                    Text("A/C Name: ${cfg.bankAccountName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("Bank: ${cfg.bankName}", style = MaterialTheme.typography.bodySmall)
                    Text("Account No: ${cfg.bankAccountNumber}", style = MaterialTheme.typography.titleMedium, color = AccentGreen)
                    Text("IFSC Code: ${cfg.bankIfsc}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }

                // UTR / Reference submission
                AppField(
                    label = if (lang == "bn") "পেমেন্ট রেফারেন্স / UTR নম্বর (পেমেন্ট করার পর)" else "UPI UTR / Bank Reference No. (After paying)",
                    value = utrInput,
                    onChange = { utrInput = it.take(40) },
                    placeholder = "e.g., 430518920144",
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BigButton(
                        text = if (lang == "bn") "UTR সেভ করুন ও হোয়াটসঅ্যাপে পাঠান" else "Save UTR & Inform Owner on WhatsApp",
                        whatsapp = true,
                        onClick = {
                            if (utrInput.isNotBlank()) {
                                app.submitOrderUtr(order.orderNo, utrInput)
                                val updatedOrder = order.copy(utrReference = utrInput.trim(), paymentStatus = "UTR_SUBMITTED")
                                actions.onSendOrderWhatsApp(updatedOrder)
                            } else {
                                actions.onSendOrderWhatsApp(order)
                            }
                        },
                    )
                }
            }
        }

        // 3. Ordered Items & GST Breakdown
        AppCard(title = if (lang == "bn") "অর্ডারের পণ্য ও GST হিসাব" else "Order Items & GST Breakdown") {
            order.items.forEach { i ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("${i.quantity} × ${i.localizedName(lang)} (${i.variantLabel})", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "SKU: ${i.sku} · HSN: ${i.hsnCode} · GST: ${i.gstRate.toInt()}% (Taxable: ${formatRupees(i.taxableValue)} + Tax: ${formatRupees(i.taxAmount)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                    Text(formatRupees(i.lineTotal), style = MaterialTheme.typography.titleMedium)
                }
            }
            HorizontalDivider(color = PaperLine)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(t("Items Subtotal (incl. GST)"), style = MaterialTheme.typography.bodyMedium)
                Text(formatRupees(order.subtotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(t("Included GST"), style = MaterialTheme.typography.bodySmall, color = Muted)
                Text(formatRupees(order.taxTotal), style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(t("Delivery Fee"), style = MaterialTheme.typography.bodyMedium)
                Text(if (order.deliveryFee <= 0.0) t("Free") else formatRupees(order.deliveryFee), style = MaterialTheme.typography.bodyMedium)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(t("Grand Total"), style = MaterialTheme.typography.titleLarge)
                Text(formatRupees(order.total), style = MaterialTheme.typography.headlineMedium, color = AccentGreen)
            }
        }

        // 4. Delivery Address Snapshot
        AppCard(title = t("1. Delivery Address")) {
            Text("${order.address.fullName} · ${order.address.phone}", style = MaterialTheme.typography.titleMedium)
            Text(order.address.formattedLine(), style = MaterialTheme.typography.bodyMedium, color = Muted)
            if (order.customerNote.isNotBlank()) {
                Text("Note: ${order.customerNote}", style = MaterialTheme.typography.bodySmall, color = AccentGreen)
            }
        }

        // 5. GST Invoice Preview & Cancel Buttons
        BigButton(
            text = "🧾 ${t("View GST Invoice")}",
            gold = true,
            onClick = { app.invoiceOrderNo = order.orderNo },
        )

        if (order.canCancel) {
            GhostButton(
                text = t("Cancel Order"),
                danger = true,
                onClick = { confirmCancel = true },
            )
        }

        Spacer(Modifier.height(24.dp))
    }

    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            title = { Text(if (lang == "bn") "অর্ডার বাতিল করবেন?" else "Cancel Order ${order.orderNo}?") },
            text = {
                Text(
                    if (lang == "bn") "আপনি কি নিশ্চিত যে এই অর্ডারটি বাতিল করতে চান?"
                    else "Are you sure you want to cancel this order?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmCancel = false
                        app.cancelOrder(order.orderNo)
                    }
                ) {
                    Text(t("Cancel Order"), color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmCancel = false }) {
                    Text(if (lang == "bn") "না" else "Keep Order", color = TextMain)
                }
            },
        )
    }
}

/**
 * Native GST Tax Invoice Preview matching GMM's `apps/invoicing` (CGST + SGST split, HSN codes, seller snapshot).
 */
@Composable
fun InvoiceScreen(
    order: Order,
    site: SiteConfig,
    lang: String,
    actions: PhoneActions,
    onBack: () -> Unit,
) {
    val invoiceNo = "GMM/2026-27/${order.orderNo.takeLast(4)}"
    val cgst = order.taxTotal / 2.0
    val sgst = order.taxTotal / 2.0

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
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
            Text(if (lang == "bn") "অর্ডারে ফিরে যান" else "Back to Order", style = MaterialTheme.typography.labelLarge)
        }

        AppCard {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("TAX INVOICE / BILL OF SUPPLY", style = MaterialTheme.typography.labelMedium, color = AccentGreen)
                    Text(site.businessName, style = MaterialTheme.typography.titleLarge)
                    Text(site.legalAddress, style = MaterialTheme.typography.bodySmall, color = Muted)
                    Text("CIN: ${site.cin} · State: ${site.stateName} (${site.stateCode})", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }

            HorizontalDivider(color = PaperLine)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Invoice No", style = MaterialTheme.typography.bodySmall, color = Muted)
                    Text(invoiceNo, style = MaterialTheme.typography.titleMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Order No & Date", style = MaterialTheme.typography.bodySmall, color = Muted)
                    Text(order.orderNo, style = MaterialTheme.typography.titleMedium)
                    Text(order.createdAt, style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }

            HorizontalDivider(color = PaperLine)

            Text("Billed & Shipped To:", style = MaterialTheme.typography.labelMedium, color = Muted)
            Text("${order.address.fullName} (${order.address.phone})", style = MaterialTheme.typography.titleMedium)
            Text(order.address.formattedLine(), style = MaterialTheme.typography.bodySmall, color = Muted)

            HorizontalDivider(color = PaperLine)

            // Line items table
            order.items.forEachIndexed { idx, item ->
                Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${idx + 1}. ${item.productName} (${item.variantLabel})", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text(formatRupees(item.lineTotal), style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        "HSN: ${item.hsnCode} | Qty: ${item.quantity} × ${formatRupees(item.unitPrice)} | Taxable: ${formatRupees(item.taxableValue)} | GST (${item.gstRate.toInt()}%): ${formatRupees(item.taxAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
            }

            HorizontalDivider(color = PaperLine)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Taxable Value", style = MaterialTheme.typography.bodySmall, color = Muted)
                Text(formatRupees(order.subtotal - order.taxTotal), style = MaterialTheme.typography.bodySmall)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("CGST (Intra-State)", style = MaterialTheme.typography.bodySmall, color = Muted)
                Text(formatRupees(cgst), style = MaterialTheme.typography.bodySmall)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SGST (Intra-State)", style = MaterialTheme.typography.bodySmall, color = Muted)
                Text(formatRupees(sgst), style = MaterialTheme.typography.bodySmall)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Delivery Charges", style = MaterialTheme.typography.bodySmall, color = Muted)
                Text(if (order.deliveryFee <= 0.0) "FREE" else formatRupees(order.deliveryFee), style = MaterialTheme.typography.bodySmall)
            }
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Invoice Total (incl. GST)", style = MaterialTheme.typography.titleLarge)
                Text(formatRupees(order.total), style = MaterialTheme.typography.headlineMedium, color = AccentGreen)
            }
        }

        BigButton(
            text = "💬 Share Invoice Summary on WhatsApp",
            whatsapp = true,
            onClick = { actions.onSendOrderWhatsApp(order) },
        )

        Spacer(Modifier.height(24.dp))
    }
}
