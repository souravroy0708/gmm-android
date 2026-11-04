package com.gmmdistributors.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmmdistributors.app.AppController
import com.gmmdistributors.app.Tab
import com.gmmdistributors.app.api.Product
import kotlin.math.ceil

/**
 * Home / shop. Designed for a first-time rural buyer on a small phone:
 * greeting + search, three picture categories, "order again", a 2-column product grid with one
 * big button per product, and a call/WhatsApp help card. No marketing walls of text.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopTab(app: AppController, actions: PhoneActions) {
    val lang = app.language
    val bn = lang == "bn"
    val filter = app.selectedCategorySlug
    val query = app.searchQuery.trim().lowercase()
    val shown = remember(app.products, filter, query) {
        app.products.filter { p ->
            val catOk = filter == "all" || p.kind == filter || p.categorySlug == filter
            val qOk = query.isBlank() || listOf(p.name, p.nameBn, p.shortDescription, p.shortDescriptionBn, p.composition)
                .any { it.lowercase().contains(query) }
            catOk && qOk
        }
    }
    val kinds = remember(app.products) { app.products.map { it.kind }.toSet() }
    val reorder = app.reorderCandidate
    val firstName = app.me?.name?.substringBefore(' ').orEmpty()

    PullToRefreshBox(
        isRefreshing = app.refreshing,
        onRefresh = { app.refresh() },
        modifier = Modifier.fillMaxSize().background(Paper),
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Greeting + search
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Forest, Forest2)))
                    .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = if (bn) "নমস্কার${if (firstName.isNotBlank()) ", $firstName" else ""} 🙏" else "Namaste${if (firstName.isNotBlank()) ", $firstName" else ""} 🙏",
                    color = Color.White, style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = if (bn) "আজ কী লাগবে? জৈব সার, কীটনাশক ও খাঁটি খাবার, বাড়িতে পৌঁছে দেব।"
                    else "What do you need today? Organic inputs and pure food, delivered home.",
                    color = MutedOnDark, style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(6.dp))
                androidx.compose.material3.TextField(
                    value = app.searchQuery,
                    onValueChange = { app.searchQuery = it.take(40) },
                    placeholder = { Text(if (bn) "খুঁজুন: নিম, সার, চাল…" else "Search: neem, manure, rice…") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Muted) },
                    trailingIcon = {
                        if (app.searchQuery.isNotEmpty()) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Muted,
                                modifier = Modifier.clickable { app.searchQuery = "" })
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = CardBg, unfocusedContainerColor = CardBg,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                )
            }

            Column(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Offer carousel (admin: Home banners) and category circles
                if (filter == "all" && query.isBlank()) HeroCarousel(app.banners, lang) { app.openBanner(it) }
                CategoryCircles(
                    kinds = listOf("fertilizer", "pesticide", "food").filter { it in kinds },
                    selected = filter, lang = lang,
                ) { k -> app.selectedCategorySlug = if (k == filter && k != "all") "all" else k }

                if (reorder != null && filter == "all" && query.isBlank()) {
                    ReorderCard(order = reorder, lang = lang, onReorder = { app.reorder(reorder) })
                }
                if (filter == "all" && query.isBlank()) {
                    ProductRail(if (bn) "❤ আপনার পছন্দের তালিকা" else "❤ Your wishlist", app.products.filter { it.id in app.wishlist }, app)
                    ProductRail(if (bn) "⭐ সবচেয়ে বেশি বিক্রি" else "⭐ Bestsellers", app.products.filter { it.isFeatured }, app)
                }

                SectionHeader(
                    title = when (filter) {
                        "fertilizer", "pesticide", "food" -> kindLabel(filter, lang)
                        else -> if (bn) "সব পণ্য" else "All products"
                    } + " (${shown.size})",
                    action = if (filter != "all" || query.isNotBlank()) (if (bn) "সব দেখুন" else "Show all") else null,
                    onAction = { app.selectedCategorySlug = "all"; app.searchQuery = "" },
                )

                if (shown.isEmpty()) {
                    AppCard {
                        Text(if (bn) "কিছু পাওয়া যায়নি" else "Nothing found", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (bn) "অন্য নামে খুঁজুন, অথবা ফোন করে জিজ্ঞেস করুন।" else "Try another word, or call us and ask.",
                            style = MaterialTheme.typography.bodyMedium, color = Muted,
                        )
                    }
                } else {
                    shown.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            pair.forEach { p ->
                                val v = p.defaultVariant
                                ProductGridCard(
                                    product = p,
                                    lang = lang,
                                    qtyInCart = v?.let { app.cartQuantities[it.id] } ?: 0,
                                    modifier = Modifier.weight(1f),
                                    wished = p.id in app.wishlist,
                                    onWish = { app.toggleWish(p.id) },
                                    onOpen = { app.openProductSlug = p.slug },
                                    onAdd = {
                                        if (p.variants.count { it.isActive } > 1) app.packSheetSlug = p.slug  // choose size first
                                        else v?.let { app.setVariantQty(it.id, 1) }
                                    },
                                    onQty = { q -> v?.let { app.setVariantQty(it.id, q) } },
                                )
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }

                HelpStrip(
                    lang = lang,
                    phone = app.siteConfig.supportPhone,
                    showWhatsApp = app.ownerWhatsapp.isNotBlank(),
                    onCall = { actions.onCallSupport(app.siteConfig.supportPhone) },
                    onWhatsApp = {
                        actions.onSendEnquiryWhatsApp(
                            if (bn) "নমস্কার GMM, আমি অ্যাপ থেকে অর্ডার করতে চাই।" else "Namaste GMM, I want to order from the app."
                        )
                    },
                )

                ReportEntryCard(lang = lang, pending = app.pendingReports) { actions.onReportProblem("home", "") }

                // Quiet trust line instead of marketing blocks
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf(
                        if (bn) "✓ ১০০% জৈব" else "✓ 100% organic",
                        if (bn) "✓ GST ইনভয়েস" else "✓ GST invoice",
                        if (bn) "✓ বাড়িতে ডেলিভারি" else "✓ Home delivery",
                    ).forEach { Text(it, style = MaterialTheme.typography.labelMedium, color = AccentGreen) }
                }
                Spacer(Modifier.height(80.dp))  // room for the floating cart bar
            }
        }
    }
}

/**
 * Full Product Detail Screen with Interactive Crop Dosage Calculator ("How much do I need?" from `apps/catalog/models.py` CropDose).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    app: AppController,
    actions: PhoneActions,
    onBack: () -> Unit,
) {
    val lang = app.language
    var selectedVariantIdx by remember(product.id) { mutableIntStateOf(product.variants.indexOfFirst { it.inStock }.coerceAtLeast(0)) }
    val selectedVariant = product.variants.getOrElse(selectedVariantIdx) { product.variants.first() }
    val maxAllowed = minOf(10, selectedVariant.stock).coerceAtLeast(0)
    val currentCartQty = app.cartQuantities[selectedVariant.id] ?: 0
    var selectedCropIdx by remember(product.id) { mutableIntStateOf(0) }
    var areaOrTreesInput by remember(product.id) { mutableStateOf("2") }

    Box(Modifier.fillMaxSize()) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Top Back Button
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(CardBg)
                .border(1.dp, PaperLine, RoundedCornerShape(50))
                .clickable(onClick = onBack)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AccentGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (lang == "bn") "সব পণ্যে ফিরে যান" else "Back to Products", style = MaterialTheme.typography.labelLarge)
        }

        // Product Image Hero Card
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFFFF4DA), Color(0xFFF4F8EE), Color(0xFFE6EFE3))
                    )
                )
                .border(1.dp, PaperLine, RoundedCornerShape(24.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            ProductArt(
                product = product,
                modifier = Modifier.height(230.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)),
            )
        }

        // Title, Badges & Description
        AppCard {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OrganicBadge(text = kindLabel(product.kind, lang), isPesticide = product.kind == "pesticide")
                OrganicBadge(text = if (lang == "bn") "১০০% রাসায়নিকমুক্ত" else "100% Chemical-Free")
            }
            Text(product.localizedName(lang), style = MaterialTheme.typography.headlineMedium)
            Text(product.localizedDesc(lang), style = MaterialTheme.typography.bodyLarge, color = Muted)

            // Pack selector
            Text(t("Choose pack size"), style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                product.variants.forEachIndexed { idx, v ->
                    val on = idx == selectedVariantIdx
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (on) MintSoft else Paper)
                            .border(if (on) 2.dp else 1.dp, if (on) Leaf else PaperLine, RoundedCornerShape(14.dp))
                            .clickable { selectedVariantIdx = idx }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(v.localizedLabel(lang), style = MaterialTheme.typography.labelLarge, color = if (on) Forest else TextMain)
                        Text(formatRupees(v.price), style = MaterialTheme.typography.titleLarge, color = if (on) Leaf else AccentGreen)
                        Text("SKU: ${v.sku}", style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                }
            }

            if (currentCartQty > 0) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${t("In Cart")}: $currentCartQty (${formatRupees(selectedVariant.price * currentCartQty)})",
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentGreen,
                    )
                    QuantityStepper(
                        qty = currentCartQty,
                        maxQty = maxOf(maxAllowed, currentCartQty),
                        onChange = { app.setVariantQty(selectedVariant.id, it) },
                    )
                }
                BigButton(
                    text = if (lang == "bn") "কার্টে যান ও অর্ডার করুন →" else "Go to Cart & Checkout →",
                    gold = true,
                    onClick = {
                        app.openProductSlug = null
                        app.tab = Tab.Cart
                    },
                )
            } else {
                BigButton(
                    text = if (maxAllowed <= 0) (if (lang == "bn") "স্টকে নেই" else "Out of stock")
                    else "🛒 ${t("Add to Cart")} · ${formatRupees(selectedVariant.price)}",
                    enabled = maxAllowed > 0,
                    onClick = { app.setVariantQty(selectedVariant.id, 1) },
                )
            }
        }

        // Organic Composition & Benefits Card
        AppCard(title = t("Key Benefits")) {
            product.localizedBenefits(lang).forEach { b ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("✓", color = AccentGreen, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text(b, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(t("Organic Composition"), style = MaterialTheme.typography.titleMedium)
            Text(product.localizedComp(lang), style = MaterialTheme.typography.bodyMedium, color = Muted)
        }

        // Interactive Crop Dosage Calculator ("How much do I need?" powered by CropDose)
        if (product.cropDoses.isNotEmpty()) {
            val activeDose = product.cropDoses.getOrElse(selectedCropIdx) { product.cropDoses.first() }
            val inputUnits = areaOrTreesInput.toDoubleOrNull()?.coerceIn(0.1, 500.0) ?: 1.0
            val minNeeded = activeDose.doseMin * inputUnits
            val maxNeeded = (activeDose.doseMax ?: activeDose.doseMin) * inputUnits

            AppCard(title = "🧮 ${t("Crop Dosage Calculator")}") {
                Text(
                    text = if (lang == "bn") "আপনার ফসল ও জমির পরিমাণ বেছে নিন, অ্যাপটি নিজে থেকেই সঠিক জৈব মাত্রা হিসাব করে দেবে:"
                    else "Select your crop and land size (or litres of spray water) to calculate the exact organic requirement:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    product.cropDoses.forEachIndexed { idx, d ->
                        val on = idx == selectedCropIdx
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (on) Leaf else Paper)
                                .border(1.dp, if (on) Leaf else PaperLine, RoundedCornerShape(50))
                                .clickable { selectedCropIdx = idx }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = d.localizedCrop(lang),
                                color = if (on) Color.White else TextMain,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }

                val inputLabel = when (activeDose.unit) {
                    "KG_ACRE", "L_ACRE" -> if (lang == "bn") "জমির পরিমাণ (একর)" else "Farm Area (in Acres)"
                    "ML_LITRE" -> if (lang == "bn") "স্প্রে ট্যাঙ্কের জল (লিটার)" else "Spray Water Volume (in Litres)"
                    "G_TREE" -> if (lang == "bn") "গাছের সংখ্যা" else "Number of Trees"
                    else -> "Quantity"
                }

                AppField(
                    label = inputLabel,
                    value = areaOrTreesInput,
                    onChange = { areaOrTreesInput = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                    keyboard = KeyboardType.Number,
                )

                val unitWord = when (activeDose.unit) {
                    "KG_ACRE" -> if (lang == "bn") "কেজি" else "kg"
                    "L_ACRE" -> if (lang == "bn") "লিটার" else "Litres"
                    "ML_LITRE" -> if (lang == "bn") "মিলি" else "ml"
                    "G_TREE" -> if (lang == "bn") "গ্রাম" else "grams"
                    else -> ""
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MintSoft)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = if (lang == "bn") "প্রস্তাবিত জৈব মাত্রা (${activeDose.localizedCrop(lang)}):"
                        else "Recommended Organic Dose (${activeDose.localizedCrop(lang)}):",
                        style = MaterialTheme.typography.labelMedium,
                        color = Forest,
                    )
                    val rangeStr = if (activeDose.doseMax != null && activeDose.doseMax > activeDose.doseMin) {
                        "${minNeeded.toInt()} – ${ceil(maxNeeded).toInt()} $unitWord"
                    } else {
                        "${ceil(minNeeded).toInt()} $unitWord"
                    }
                    Text(
                        text = rangeStr,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Forest,
                    )
                    Text(
                        text = "${activeDose.doseMin.toInt()}${activeDose.doseMax?.let { "–${it.toInt()}" } ?: ""} ${activeDose.unitLabel(lang)} · ${activeDose.localizedNote(lang)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2B3A24),
                    )
                }
            }
        }

        // Direct WhatsApp product enquiry button
        BigButton(
            text = if (lang == "bn") "💬 এই পণ্যটি সম্পর্কে হোয়াটসঅ্যাপে জিজ্ঞাসা করুন" else "💬 Ask Owner About This Product on WhatsApp",
            whatsapp = true,
            onClick = {
                val q = "Namaste GMM Distributors, I want details/bulk price for *${product.name}* (${selectedVariant.label}, SKU: ${selectedVariant.sku})."
                actions.onSendEnquiryWhatsApp(q)
            },
        )

        Spacer(Modifier.height(24.dp))
    }
    WishHeart(on = product.id in app.wishlist, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) { app.toggleWish(product.id) }
    StickyBuyBar(
        v = selectedVariant,
        inCart = currentCartQty,
        lang = lang,
        onAdd = { app.setVariantQty(selectedVariant.id, 1) },
        onGoToBag = { app.openProductSlug = null; app.tab = Tab.Cart },
        modifier = Modifier.align(Alignment.BottomCenter),
    )
    }
}
