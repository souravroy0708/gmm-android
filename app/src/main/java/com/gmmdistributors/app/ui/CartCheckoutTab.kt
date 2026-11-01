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
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmmdistributors.app.AppController
import com.gmmdistributors.app.Tab
import com.gmmdistributors.app.api.Address
import com.gmmdistributors.app.api.Order

@Composable
fun CartCheckoutTab(app: AppController, actions: PhoneActions) {
    val lang = app.language
    val lines = app.cartLines

    if (lines.isEmpty()) {
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
                Icon(Icons.Filled.ShoppingBag, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(t("Your cart is empty"), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                t("Browse our 100% organic fertilizers and bio-pesticides to start your order."),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            BigButton(
                text = t("Browse Organic Products"),
                onClick = { app.tab = Tab.Shop },
            )
        }
        return
    }

    var paymentMethod by rememberSaveable { mutableStateOf("UPI") }
    var customerNote by rememberSaveable { mutableStateOf("") }
    val selectedAddress = app.addresses.firstOrNull { it.id == app.selectedAddressId } ?: app.addresses.firstOrNull()
    val subtotal = app.cartSubtotal
    val taxTotal = app.cartTaxTotal
    val deliveryFee = app.deliveryFeeFor(selectedAddress?.pincode ?: "712601", subtotal)
    val grandTotal = subtotal + deliveryFee

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(t("Your Cart"), style = MaterialTheme.typography.headlineLarge)

        // 1. Cart Line Items Card
        AppCard(
            title = if (lang == "bn") "নির্বাচিত জৈব পণ্য (${app.cartCount} টি)" else "Selected Organic Packs (${app.cartCount})",
            trailing = {
                Text(
                    text = if (lang == "bn") "সব মুছুন" else "Clear",
                    style = MaterialTheme.typography.labelMedium,
                    color = Danger,
                    modifier = Modifier.clickable { app.clearCart() },
                )
            }
        ) {
            lines.forEachIndexed { idx, line ->
                if (idx > 0) HorizontalDivider(color = PaperLine)
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DrawableImage(
                        drawableName = line.product.drawableName,
                        contentDescription = line.product.localizedName(lang),
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background( Color(0xFFF4F8EE) )
                            .border(1.dp, PaperLine, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(line.product.localizedName(lang), style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${line.variant.localizedLabel(lang)} · ${formatRupees(line.variant.price)} / pack",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                        Text(
                            formatRupees(line.lineTotal),
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentGreen,
                        )
                    }
                    QuantityStepper(
                        qty = line.quantity,
                        maxQty = 10,
                        onChange = { app.setVariantQty(line.variant.id, it) },
                    )
                }
            }

            HorizontalDivider(color = PaperLine)
            // Bill Breakdown
            BillRow(t("Items Subtotal (incl. GST)"), formatRupees(subtotal))
            BillRow(t("Included GST"), formatRupees(taxTotal), muted = true)
            BillRow(
                label = t("Delivery Fee") + (if (subtotal >= app.siteConfig.freeDeliveryAbove) " (₹1000+ Free)" else ""),
                value = if (deliveryFee <= 0.0) t("Free") else formatRupees(deliveryFee),
            )
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(t("Grand Total"), style = MaterialTheme.typography.titleLarge)
                Text(formatRupees(grandTotal), style = MaterialTheme.typography.headlineMedium, color = AccentGreen)
            }
        }

        // 2. Delivery Address Card
        AppCard(
            title = t("1. Delivery Address"),
            trailing = {
                SmallPill("+ ${t("Add New Address")}") {
                    app.editingAddress = null
                    app.addingAddress = true
                }
            }
        ) {
            if (app.addresses.isEmpty()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(WarnBg)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = if (lang == "bn") "অর্ডার ডেলিভারির জন্য আপনার নাম, মোবাইল নম্বর ও ঠিকানা যোগ করুন।"
                        else "Please add your name, mobile number, and village/town delivery address.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarnInk,
                    )
                    BigButton(
                        text = "+ ${t("Add New Address")}",
                        onClick = { app.addingAddress = true },
                    )
                }
            } else {
                app.addresses.forEach { addr ->
                    val isSelected = addr.id == selectedAddress?.id
                    val pinInfo = app.isPinServiceable(addr.pincode)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) MintSoft else Paper)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Leaf else PaperLine,
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable { app.selectedAddressId = addr.id }
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) Leaf else Muted,
                            modifier = Modifier.size(22.dp).padding(top = 2.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${addr.fullName} · ${addr.phone}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) Forest else TextMain,
                            )
                            Text(
                                addr.formattedLine(),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) Color(0xFF2B3A24) else Muted,
                            )
                            Text(
                                text = if (pinInfo != null) {
                                    "✓ ${pinInfo.localizedArea(lang)} · Delivery: ${if (pinInfo.deliveryFee == 0.0 || subtotal >= app.siteConfig.freeDeliveryAbove) "Free" else formatRupees(pinInfo.deliveryFee)}"
                                } else {
                                    "ℹ Standard WB Delivery (${formatRupees(deliveryFee)}) · Confirmed on WhatsApp"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Leaf else AccentGreen,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = Muted,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    app.editingAddress = addr
                                    app.addingAddress = true
                                },
                        )
                    }
                }
            }
        }

        // 3. Offline Payment Mode Card (No Payment Gateway — Direct WhatsApp + Manual Owner Collection)
        AppCard(title = t("2. Offline Payment Mode (No Gateway)")) {
            val paymentChoices = listOf(
                Triple(
                    "UPI",
                    if (lang == "bn") "ম্যানুয়াল UPI পেমেন্ট (হোয়াটসঅ্যাপের মাধ্যমে)" else "Manual UPI Transfer (Outside App via WhatsApp)",
                    if (lang == "bn") "অর্ডার করার পর মালিকের UPI ID (${app.siteConfig.upiId})-তে পেমেন্ট করে UTR জানান।"
                    else "Send order on WhatsApp & transfer manually to owner's UPI ID (${app.siteConfig.upiId})."
                ),
                Triple(
                    "BANK",
                    if (lang == "bn") "ব্যাংক অ্যাকাউন্ট ট্রান্সফার / NEFT (অফলাইন)" else "Manual Bank Account Transfer (Outside App)",
                    if (lang == "bn") "মালিকের SBI ব্যাংক অ্যাকাউন্টে (${app.siteConfig.bankAccountNumber}) ম্যানুয়ালি পাঠান।"
                    else "Transfer directly to GMM Distributors SBI Account (${app.siteConfig.bankAccountNumber})."
                ),
                Triple(
                    "COD",
                    if (lang == "bn") "ক্যাশ অন ডেলিভারি (পণ্য হাতে পেয়ে পেমেন্ট)" else "Cash on Delivery (Manual Collection at Doorstep)",
                    if (lang == "bn") "হোয়াটসঅ্যাপে অর্ডার নিশ্চিত করুন এবং ডেলিভারির সময় নগদ বা UPI দিন।"
                    else "Confirm order on WhatsApp and pay manually when the organic packs reach your door."
                ),
            )

            paymentChoices.forEach { (code, title, subtitle) ->
                val isSelected = paymentMethod == code
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) MintSoft else Paper)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Leaf else PaperLine,
                            shape = RoundedCornerShape(14.dp),
                        )
                        .clickable { paymentMethod = code }
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) Leaf else Muted,
                        modifier = Modifier.size(22.dp).padding(top = 2.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleMedium, color = if (isSelected) Forest else TextMain)
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = if (isSelected) Color(0xFF2B3A24) else Muted)
                    }
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Paper2)
                    .padding(12.dp)
            ) {
                Text(
                    text = t("How offline payment works: Place your order and tap 'Send on WhatsApp'. Our owner (Chikur Kumar Ray) receives your order directly on WhatsApp and shares/confirms UPI, Bank Transfer, or Cash on Delivery."),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMain,
                )
            }

            AppField(
                label = if (lang == "bn") "অর্ডার নোট বা বিশেষ নির্দেশ (ঐচ্ছিক)" else "Order Note / Crop Requirement (Optional)",
                value = customerNote,
                onChange = { customerNote = it.take(200) },
                placeholder = if (lang == "bn") "যেমন: সকাল ১০টার মধ্যে ডেলিভারি হলে ভালো হয়" else "e.g., Need for 3 acres potato cultivation",
            )

            BigButton(
                text = if (app.busy) "Placing Order…" else "✔ ${t("Place Order & Inform Owner on WhatsApp")} · ${formatRupees(grandTotal)}",
                whatsapp = true,
                enabled = !app.busy,
                onClick = {
                    if (selectedAddress == null) {
                        app.addingAddress = true
                        return@BigButton
                    }
                    app.placeOrder(
                        address = selectedAddress,
                        paymentMethod = paymentMethod,
                        note = customerNote,
                        onPlaced = { placedOrder ->
                            customerNote = ""
                            app.tab = Tab.Orders
                            // Also open WhatsApp prompt so user can tap once to launch WhatsApp
                            app.newlyPlacedOrder = placedOrder
                        }
                    )
                },
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun BillRow(label: String, value: String, muted: Boolean = false) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = if (muted) Muted else TextMain)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = if (muted) FontWeight.Normal else FontWeight.Bold, color = if (muted) Muted else TextMain)
    }
}

@Composable
fun AddressFormScreen(
    app: AppController,
    existing: Address?,
    onBack: () -> Unit,
) {
    val lang = app.language
    var fullName by rememberSaveable(existing?.id) { mutableStateOf(existing?.fullName ?: app.prefs.customerName) }
    var phone by rememberSaveable(existing?.id) { mutableStateOf(existing?.phone ?: app.prefs.customerPhone) }
    var line1 by rememberSaveable(existing?.id) { mutableStateOf(existing?.line1 ?: "") }
    var landmark by rememberSaveable(existing?.id) { mutableStateOf(existing?.landmark ?: "") }
    var villageTown by rememberSaveable(existing?.id) { mutableStateOf(existing?.villageTown ?: app.prefs.villageTown.ifBlank { "Arambagh" }) }
    var district by rememberSaveable(existing?.id) { mutableStateOf(existing?.district ?: "Hooghly") }
    var state by rememberSaveable(existing?.id) { mutableStateOf(existing?.state ?: "West Bengal") }
    var pincode by rememberSaveable(existing?.id) { mutableStateOf(existing?.pincode ?: app.prefs.pincode.ifBlank { "712601" }) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

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
            Text(if (lang == "bn") "ফিরে যান" else "Back", style = MaterialTheme.typography.labelLarge)
        }

        AppCard(
            title = if (existing != null) {
                if (lang == "bn") "ঠিকানা পরিবর্তন করুন" else "Edit Delivery Address"
            } else {
                if (lang == "bn") "নতুন ডেলিভারি ঠিকানা" else "Add Delivery Address"
            }
        ) {
            AppField(
                label = if (lang == "bn") "আপনার পুরো নাম *" else "Full Name *",
                value = fullName,
                onChange = { fullName = it.take(80); errorMsg = null },
                placeholder = "Ramesh Mondal",
            )
            AppField(
                label = if (lang == "bn") "মোবাইল নম্বর (১০ সংখ্যা) *" else "Mobile Number (10 digits) *",
                value = phone,
                onChange = { phone = it.filter { c -> c.isDigit() || c == '+' }.take(13); errorMsg = null },
                keyboard = KeyboardType.Phone,
                placeholder = "9876543210",
            )
            AppField(
                label = if (lang == "bn") "বাড়ি / পাড়া / রাস্তা *" else "House / Para / Road *",
                value = line1,
                onChange = { line1 = it.take(120); errorMsg = null },
                placeholder = "Ward No. 13, Fansi Bagan",
            )
            AppField(
                label = if (lang == "bn") "গ্রাম বা শহর *" else "Village / Town *",
                value = villageTown,
                onChange = { villageTown = it.take(80); errorMsg = null },
                placeholder = "Arambagh",
            )
            AppField(
                label = if (lang == "bn") "ল্যান্ডমার্ক / কাছাকাছি স্থান (ঐচ্ছিক)" else "Landmark (Optional)",
                value = landmark,
                onChange = { landmark = it.take(80) },
                placeholder = "Near Krishi Mandi / Cold Storage",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppField(
                    label = if (lang == "bn") "জেলা *" else "District *",
                    value = district,
                    onChange = { district = it.take(50) },
                    modifier = Modifier.weight(1f),
                )
                AppField(
                    label = if (lang == "bn") "পিন কোড (৬ সংখ্যা) *" else "PIN Code *",
                    value = pincode,
                    onChange = { pincode = it.filter { c -> c.isDigit() }.take(6); errorMsg = null },
                    keyboard = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
            }
            AppField(
                label = if (lang == "bn") "রাজ্য" else "State",
                value = state,
                onChange = { state = it.take(50) },
            )

            // Quick Serviceable PIN hint
            val matchedPin = app.isPinServiceable(pincode)
            Text(
                text = if (matchedPin != null) {
                    "✓ Serviceable Zone: ${matchedPin.localizedArea(lang)} (Delivery: ${if (matchedPin.deliveryFee == 0.0) "Free" else formatRupees(matchedPin.deliveryFee)})"
                } else {
                    "ℹ PIN $pincode: Delivery arranged via WhatsApp confirmation."
                },
                style = MaterialTheme.typography.bodySmall,
                color = AccentGreen,
            )

            errorMsg?.let {
                Text(it, color = Danger, style = MaterialTheme.typography.bodyMedium)
            }

            BigButton(
                text = t("Save"),
                onClick = {
                    val cleanDigits = phone.filter { it.isDigit() }.takeLast(10)
                    when {
                        fullName.isBlank() -> errorMsg = "Please enter your name."
                        cleanDigits.length != 10 -> errorMsg = "Please enter a valid 10-digit mobile number."
                        line1.isBlank() || villageTown.isBlank() -> errorMsg = "Please enter your address and village/town."
                        pincode.length != 6 -> errorMsg = "Please enter a 6-digit PIN code."
                        else -> {
                            app.saveAddress(
                                Address(
                                    id = existing?.id ?: 0L,
                                    fullName = fullName.trim(),
                                    phone = cleanDigits,
                                    line1 = line1.trim(),
                                    villageTown = villageTown.trim(),
                                    district = district.trim().ifBlank { "Hooghly" },
                                    state = state.trim().ifBlank { "West Bengal" },
                                    pincode = pincode.trim(),
                                    landmark = landmark.trim(),
                                    isDefault = true,
                                )
                            )
                        }
                    }
                },
            )

            if (existing != null) {
                GhostButton(
                    text = if (lang == "bn") "ঠিকানা মুছে ফেলুন" else "Delete Address",
                    danger = true,
                    onClick = {
                        app.deleteAddress(existing.id)
                        onBack()
                    },
                )
            }
        }
    }
}

@Composable
fun WhatsAppOrderPromptDialog(
    order: Order,
    lang: String,
    onSendWhatsApp: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (lang == "bn") "অর্ডার তৈরি হয়েছে: ${order.orderNo}" else "Order Placed: ${order.orderNo}",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (lang == "bn") {
                        "এখন নিচের সবুজ বাটনে চাপ দিয়ে আপনার অর্ডারের সম্পূর্ণ বিবরণ মালিকের (Chikur Kumar Ray) হোয়াটসঅ্যাপে পাঠিয়ে দিন। মালিক নিজে আপনাকে পেমেন্ট ও ডেলিভারি নিশ্চিত করবেন।"
                    } else {
                        "Tap the green button below to send your complete order receipt directly to our owner (Chikur Kumar Ray) on WhatsApp. He will confirm delivery and manually collect payment via ${order.paymentMethod}."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MintSoft)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "${order.items.sumOf { it.quantity }} packs · Total: ${formatRupees(order.total)} (${order.paymentMethod})",
                        style = MaterialTheme.typography.labelLarge,
                        color = Forest,
                    )
                }
            }
        },
        confirmButton = {
            BigButton(
                text = "💬 ${t("Send Order to Owner on WhatsApp")}",
                whatsapp = true,
                onClick = onSendWhatsApp,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (lang == "bn") "পরে পাঠাব" else "View Order Details", color = Muted)
            }
        },
    )
}
