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
    val bn = lang == "bn"
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

    val cfg = app.siteConfig
    // Members pick a saved address; guests use the details typed once on this phone.
    val selectedAddress = if (app.signedIn) app.addresses.firstOrNull { it.id == app.selectedAddressId } ?: app.addresses.firstOrNull() else app.guestAddress
    val online = !app.offline
    val pin = selectedAddress?.let { app.isPinServiceable(it.pincode) }
    val subtotal = app.cartSubtotal
    val taxTotal = app.cartTaxTotal
    val deliveryFee = selectedAddress?.let { app.deliveryFeeFor(it.pincode, subtotal) }
    val grandTotal = subtotal + (deliveryFee ?: 0.0)
    val problems = app.cartProblems
    val activeOrder = app.orders.firstOrNull { it.isActive }

    // Payment options come from Business settings in the admin.
    val codOk = cfg.codEnabled && (pin?.codAllowed ?: true) && grandTotal >= cfg.codMinOrder &&
        (cfg.codMaxOrder == null || grandTotal <= cfg.codMaxOrder!!)
    val methods = buildList {
        if (cfg.upiReady) add("UPI")
        if (cfg.bankReady) add("BANK")
        if (codOk) add("COD")
    }
    var paymentMethod by rememberSaveable { mutableStateOf("UPI") }
    if (methods.isNotEmpty() && paymentMethod !in methods) paymentMethod = methods.first()
    var customerNote by rememberSaveable { mutableStateOf("") }

    val blocker: String? = when {
        app.pendingOrders.any { it.status == "waiting" } -> if (bn) "আপনার একটি অর্ডার ফোনে সেভ আছে, ইন্টারনেট এলে পাঠানো হবে। তারপর নতুন অর্ডার করুন।"
            else "You have an order saved on this phone; it will be sent when internet is back. Order again after that."
        activeOrder != null -> if (bn) "আপনার একটি অর্ডার (${activeOrder.orderNo}) চলছে। সেটি ডেলিভারি বা বাতিল হলে নতুন অর্ডার করুন।"
            else "You already have an order in progress (${activeOrder.orderNo}). You can order again once it is delivered or cancelled."
        problems.isNotEmpty() -> if (bn) "কিছু প্যাক এখন স্টকে কম আছে। পরিমাণ কমিয়ে নিন।" else "Some packs are short in stock right now. Please reduce the quantity."
        selectedAddress == null -> if (bn) "ডেলিভারির ঠিকানা যোগ করুন।" else "Please add a delivery address."
        pin == null -> if (bn) "দুঃখিত, এই পিন কোডে (${selectedAddress.pincode}) এখনও ডেলিভারি হয় না। ফোন করে জেনে নিন।"
            else "Sorry, we do not deliver to PIN ${selectedAddress.pincode} yet. Please call us."
        subtotal < cfg.minOrderValue -> if (bn) "সর্বনিম্ন অর্ডার ${formatRupees(cfg.minOrderValue)}।" else "The minimum order value is ${formatRupees(cfg.minOrderValue)}."
        methods.isEmpty() -> if (bn) "পেমেন্টের ব্যবস্থা এখনও চালু হয়নি। আমাদের ফোন করুন।" else "Payment options are not set up yet. Please call us."
        else -> null
    }

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

        // 1. Cart lines
        AppCard(
            title = if (bn) "নির্বাচিত জৈব পণ্য (${app.cartCount} টি)" else "Selected Organic Packs (${app.cartCount})",
            trailing = {
                Text(
                    text = if (bn) "সব মুছুন" else "Clear",
                    style = MaterialTheme.typography.labelMedium,
                    color = Danger,
                    modifier = Modifier.clickable { app.clearCart() },
                )
            }
        ) {
            lines.forEachIndexed { idx, line ->
                if (idx > 0) HorizontalDivider(color = PaperLine)
                val short = line.quantity > line.variant.stock
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProductImage(
                        product = line.product,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF4F8EE))
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
                        if (short) {
                            Text(
                                if (line.variant.stock <= 0) (if (bn) "স্টকে নেই" else "Out of stock")
                                else (if (bn) "স্টকে মাত্র ${line.variant.stock} টি" else "Only ${line.variant.stock} left"),
                                style = MaterialTheme.typography.labelMedium, color = Danger,
                            )
                        }
                        Text(formatRupees(line.lineTotal), style = MaterialTheme.typography.titleMedium, color = AccentGreen)
                    }
                    QuantityStepper(
                        qty = line.quantity,
                        maxQty = minOf(10, maxOf(line.variant.stock, line.quantity)),
                        onChange = { app.setVariantQty(line.variant.id, it) },
                    )
                }
            }

            HorizontalDivider(color = PaperLine)
            BillRow(t("Items Subtotal (incl. GST)"), formatRupees(subtotal))
            BillRow(t("Included GST"), formatRupees(taxTotal), muted = true)
            BillRow(
                label = t("Delivery Fee"),
                value = when {
                    deliveryFee == null -> if (bn) "ঠিকানা বেছে নিন" else "Choose address"
                    deliveryFee <= 0.0 -> t("Free")
                    else -> formatRupees(deliveryFee)
                },
            )
            cfg.freeDeliveryAbove?.let { free ->
                if (subtotal < free) {
                    Text(
                        if (bn) "আরও ${formatRupees(free - subtotal)} কিনলে ডেলিভারি ফ্রি।" else "Add ${formatRupees(free - subtotal)} more for free delivery.",
                        style = MaterialTheme.typography.bodySmall, color = AccentGreen,
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(t("Grand Total"), style = MaterialTheme.typography.titleLarge)
                Text(formatRupees(grandTotal), style = MaterialTheme.typography.headlineMedium, color = AccentGreen)
            }
        }

        // Prices on the phone are a saved copy; the final price is confirmed when the order is sent.
        Text(
            text = when {
                app.offline -> if (bn) "📶 ইন্টারনেট নেই। অর্ডার ফোনে সেভ হবে, ইন্টারনেট এলে নিজে থেকেই চলে যাবে।"
                    else "📶 No internet. Your order will be saved and sent automatically when you are online."
                else -> if (bn) "✓ দাম ও স্টক অর্ডার পাঠানোর সময় আবার মিলিয়ে নেওয়া হয়।" else "✓ Price and stock are re-checked when you place the order."
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (app.offline) WarnInk else Muted,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(if (app.offline) WarnBg else Color.Transparent).padding(horizontal = 4.dp, vertical = 6.dp),
        )

        if (!app.signedIn) GuestDetailsCard(app)

        if (app.signedIn) {
        // 2. Delivery address (saved on the server)
        AppCard(
            title = t("1. Delivery Address"),
            trailing = {
                if (app.addresses.size < 10) {
                    SmallPill("+ ${t("Add New Address")}") {
                        app.editingAddress = null
                        app.addingAddress = true
                    }
                }
            }
        ) {
            if (app.addresses.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(WarnBg).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = if (bn) "অর্ডার ডেলিভারির জন্য আপনার নাম, মোবাইল নম্বর ও ঠিকানা যোগ করুন।"
                        else "Please add your name, mobile number and village/town delivery address.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarnInk,
                    )
                    BigButton(text = "+ ${t("Add New Address")}", onClick = { app.addingAddress = true })
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
                            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) Leaf else PaperLine, RoundedCornerShape(14.dp))
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
                            Text("${addr.fullName} · ${addr.phone}", style = MaterialTheme.typography.titleMedium, color = if (isSelected) Forest else TextMain)
                            Text(addr.formattedLine(), style = MaterialTheme.typography.bodySmall, color = if (isSelected) Color(0xFF2B3A24) else Muted)
                            Text(
                                text = if (pinInfo != null) {
                                    val fee = app.deliveryFeeFor(addr.pincode, subtotal) ?: 0.0
                                    "✓ ${pinInfo.localizedArea(lang)} · ${t("Delivery Fee")}: ${if (fee <= 0.0) t("Free") else formatRupees(fee)}"
                                } else {
                                    if (bn) "✗ এই পিন কোডে এখনও ডেলিভারি হয় না" else "✗ We do not deliver to this PIN yet"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (pinInfo != null) (if (isSelected) Leaf else AccentGreen) else Danger,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = Muted,
                            modifier = Modifier.size(20.dp).clickable {
                                app.editingAddress = addr
                                app.addingAddress = true
                            },
                        )
                    }
                }
            }
        }

        }

        // 3. Payment (no gateway: UPI app / bank transfer / cash on delivery)
        AppCard(title = if (bn) "২. পেমেন্ট পদ্ধতি" else "2. Payment Method") {
            val choices = listOf(
                Triple(
                    "UPI",
                    if (bn) "UPI (GPay, PhonePe, Paytm, BHIM)" else "UPI (GPay, PhonePe, Paytm, BHIM)",
                    if (bn) "অর্ডারের পর এক ট্যাপে আপনার UPI অ্যাপ খুলবে। পেমেন্টের পর ১২ সংখ্যার UTR নম্বর অ্যাপে দিন।"
                    else "After ordering, one tap opens your UPI app with the amount filled in. Then enter the 12-digit UTR in the app.",
                ),
                Triple(
                    "BANK",
                    if (bn) "ব্যাংক ট্রান্সফার (NEFT / IMPS)" else "Bank transfer (NEFT / IMPS)",
                    if (bn) "${cfg.bankName} অ্যাকাউন্টে পাঠান, তারপর রেফারেন্স নম্বর অ্যাপে দিন।"
                    else "Transfer to our ${cfg.bankName.ifBlank { "bank" }} account, then enter the reference number in the app.",
                ),
                Triple(
                    "COD",
                    if (bn) "ক্যাশ অন ডেলিভারি" else "Cash on delivery",
                    if (bn) "পণ্য হাতে পেয়ে নগদ দিন।" else "Pay in cash when the packs reach your door.",
                ),
            ).filter { it.first in methods }

            choices.forEach { (code, title, subtitle) ->
                val isSelected = paymentMethod == code
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) MintSoft else Paper)
                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) Leaf else PaperLine, RoundedCornerShape(14.dp))
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
            if (cfg.codEnabled && "COD" !in methods) {
                Text(
                    if (bn) "এই অর্ডারে ক্যাশ অন ডেলিভারি পাওয়া যাবে না।" else "Cash on delivery is not available for this order.",
                    style = MaterialTheme.typography.bodySmall, color = Muted,
                )
            }

            AppField(
                label = if (bn) "অর্ডার নোট বা বিশেষ নির্দেশ (ঐচ্ছিক)" else "Order Note / Crop Requirement (Optional)",
                value = customerNote,
                onChange = { customerNote = it.take(300) },
                placeholder = if (bn) "যেমন: সকাল ১০টার মধ্যে ডেলিভারি হলে ভালো হয়" else "e.g., Need for 3 acres potato cultivation",
            )

            blocker?.let {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(WarnBg).padding(12.dp)) {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = WarnInk)
                }
            }

            BigButton(
                text = when {
                    app.busy -> if (bn) "দাম মিলিয়ে অর্ডার হচ্ছে…" else "Checking prices & placing…"
                    !online -> "💾 ${if (bn) "অর্ডার সেভ করুন" else "Save order"} · ${formatRupees(grandTotal)}"
                    else -> "✔ ${if (bn) "অর্ডার করুন" else "Place order"} · ${formatRupees(grandTotal)}"
                },
                enabled = !app.busy && blocker == null,
                onClick = {
                    val address = selectedAddress ?: return@BigButton
                    app.checkout(
                        address = address,
                        paymentMethod = paymentMethod,
                        note = customerNote,
                        onPlaced = {
                            customerNote = ""
                            app.tab = Tab.Orders
                        },
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
    var fullName by rememberSaveable(existing?.id) { mutableStateOf(existing?.fullName ?: app.me?.name?.takeIf { app.signedIn } ?: "") }
    var phone by rememberSaveable(existing?.id) { mutableStateOf(existing?.phone ?: app.me?.phone?.takeIf { app.signedIn } ?: "") }
    var postOffice by rememberSaveable(existing?.id) { mutableStateOf(existing?.postOffice ?: "") }
    var line1 by rememberSaveable(existing?.id) { mutableStateOf(existing?.line1 ?: "") }
    var landmark by rememberSaveable(existing?.id) { mutableStateOf(existing?.landmark ?: "") }
    var villageTown by rememberSaveable(existing?.id) { mutableStateOf(existing?.villageTown ?: "") }
    var district by rememberSaveable(existing?.id) { mutableStateOf(existing?.district ?: "Hooghly") }
    var state by rememberSaveable(existing?.id) { mutableStateOf(existing?.state ?: "West Bengal") }
    var pincode by rememberSaveable(existing?.id) { mutableStateOf(existing?.pincode ?: "") }
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
                label = if (lang == "bn") "পোস্ট অফিস (ঐচ্ছিক)" else "Post office (Optional)",
                value = postOffice,
                onChange = { postOffice = it.take(80) },
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
                    "✓ ${matchedPin.localizedArea(lang)} · ${t("Delivery Fee")}: ${if (matchedPin.deliveryFee == 0.0) t("Free") else formatRupees(matchedPin.deliveryFee)}"
                } else if (pincode.length == 6) {
                    if (lang == "bn") "✗ এই পিন কোডে এখনও ডেলিভারি হয় না। ঠিকানা সেভ করা যাবে, কিন্তু অর্ডার করা যাবে না।"
                    else "✗ We do not deliver to PIN $pincode yet. You can save it, but orders need a served PIN."
                } else "",
                style = MaterialTheme.typography.bodySmall,
                color = if (matchedPin != null) AccentGreen else Danger,
            )

            errorMsg?.let {
                Text(it, color = Danger, style = MaterialTheme.typography.bodyMedium)
            }

            BigButton(
                text = if (app.busy) "…" else t("Save"),
                enabled = !app.busy,
                onClick = {
                    val cleanDigits = phone.filter { it.isDigit() }.takeLast(10)
                    when {
                        fullName.isBlank() -> errorMsg = "Please enter your name."
                        cleanDigits.length != 10 || cleanDigits.first() !in '6'..'9' -> errorMsg = "Please enter a valid 10-digit mobile number."
                        line1.isBlank() || villageTown.isBlank() -> errorMsg = "Please enter your address and village/town."
                        district.isBlank() -> errorMsg = "Please enter your district."
                        pincode.length != 6 || pincode.first() == '0' -> errorMsg = "Please enter a 6-digit PIN code."
                        else -> {
                            app.saveAddress(
                                Address(
                                    id = existing?.id ?: 0L,
                                    fullName = fullName.trim(),
                                    phone = cleanDigits,
                                    line1 = line1.trim(),
                                    villageTown = villageTown.trim(),
                                    postOffice = postOffice.trim(),
                                    district = district.trim().ifBlank { "Hooghly" },
                                    state = state.trim().ifBlank { "West Bengal" },
                                    pincode = pincode.trim(),
                                    landmark = landmark.trim(),
                                    isDefault = existing?.isDefault ?: true,
                                )
                            )
                        }
                    }
                },
            )

            if (existing != null && app.signedIn) {
                GhostButton(
                    text = if (lang == "bn") "ঠিকানা মুছে ফেলুন" else "Delete Address",
                    danger = true,
                    onClick = { app.deleteAddress(existing.id) },
                )
            }
        }
    }
}

/** Guest checkout: name, mobile and address typed once, remembered on this phone. No password. */
@Composable
fun GuestDetailsCard(app: AppController) {
    val bn = app.language == "bn"
    val g = app.guestAddress
    AppCard(title = if (bn) "১. কোথায় পাঠাব?" else "1. Where should we deliver?") {
        if (g == null) {
            Text(
                if (bn) "শুধু নাম, মোবাইল নম্বর ও ঠিকানা দিন। কোনো পাসওয়ার্ড লাগবে না।"
                else "Just your name, mobile number and address. No password needed.",
                style = MaterialTheme.typography.bodyMedium, color = Muted,
            )
            BigButton(text = if (bn) "+ ডেলিভারির তথ্য দিন" else "+ Add delivery details", onClick = { app.editingAddress = null; app.addingAddress = true })
        } else {
            val pin = app.isPinServiceable(g.pincode)
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MintSoft)
                    .border(2.dp, Leaf, RoundedCornerShape(14.dp)).padding(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("${g.fullName} · ${g.phone}", style = MaterialTheme.typography.titleMedium, color = Forest)
                    Text(g.formattedLine(), style = MaterialTheme.typography.bodySmall, color = Color(0xFF2B3A24))
                    Text(
                        if (pin != null) "✓ ${pin.localizedArea(app.language)}" else (if (bn) "✗ এই পিন কোডে এখনও ডেলিভারি হয় না" else "✗ We do not deliver to this PIN yet"),
                        style = MaterialTheme.typography.labelMedium, color = if (pin != null) Leaf else Danger, modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Leaf,
                    modifier = Modifier.size(22.dp).clickable { app.editingAddress = g; app.addingAddress = true })
            }
        }
        Text(
            if (bn) "আগে অ্যাকাউন্ট খুলেছেন? সাইন ইন করুন" else "Have an account? Sign in",
            style = MaterialTheme.typography.labelLarge, color = AccentGreen,
            modifier = Modifier.clip(RoundedCornerShape(50)).clickable { app.requireSignIn() }.padding(vertical = 8.dp),
        )
    }
}

/** Shown once when the live check found new prices or less stock than the phone's saved copy. */
@Composable
fun PriceReviewDialog(app: AppController) {
    val q = app.quoteReview ?: return
    val bn = app.language == "bn"
    AlertDialog(
        onDismissRequest = { app.dismissQuote() },
        title = { Text(if (bn) "দাম বা স্টক বদলেছে" else "Prices or stock have changed") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    if (bn) "আপনার ফোনে আগের দাম ছিল। আজকের দাম ও স্টক অনুযায়ী কার্ট ঠিক করে দিয়েছি:"
                    else "Your phone had older prices. We updated your cart to today's prices and stock:",
                    style = MaterialTheme.typography.bodyMedium,
                )
                q.lines.filter { it.priceChanged || !it.available || it.stock < it.quantity }.forEach { l ->
                    val name = app.products.firstOrNull { p -> p.variants.any { it.id == l.variantId } }?.localizedName(app.language) ?: "#${l.variantId}"
                    Text(
                        "• $name: " + when {
                            !l.available -> if (bn) "এখন পাওয়া যাচ্ছে না" else "not available now"
                            l.stock < l.quantity -> if (bn) "স্টকে মাত্র ${l.stock} টি" else "only ${l.stock} left"
                            else -> "${formatRupees(l.oldPrice ?: l.price)} → ${formatRupees(l.price)}"
                        },
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    "${t("Grand Total")}: ${formatRupees(app.cartSubtotal + (q.deliveryFee ?: 0.0))}",
                    style = MaterialTheme.typography.titleMedium, color = AccentGreen,
                )
            }
        },
        confirmButton = {
            TextButton(enabled = app.cartLines.isNotEmpty(), onClick = { app.confirmQuote() }) {
                Text(if (bn) "এই দামে অর্ডার করুন" else "Order at this price", fontWeight = FontWeight.Bold, color = Leaf)
            }
        },
        dismissButton = { TextButton(onClick = { app.dismissQuote() }) { Text(if (bn) "কার্ট দেখি" else "Review cart", color = TextMain) } },
    )
}
