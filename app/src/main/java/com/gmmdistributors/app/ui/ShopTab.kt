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
import androidx.compose.material.icons.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShopTab(app: AppController, actions: PhoneActions) {
    val lang = app.language
    val filteredProducts = remember(app.products, app.selectedCategorySlug, app.searchQuery) {
        app.products.filter { p ->
            val catMatch = when (app.selectedCategorySlug) {
                "all" -> true
                "fertilizer" -> p.kind == "fertilizer"
                "pesticide" -> p.kind == "pesticide"
                else -> p.categorySlug == app.selectedCategorySlug
            }
            val q = app.searchQuery.trim().lowercase()
            val searchMatch = q.isBlank() ||
                p.name.lowercase().contains(q) ||
                p.nameBn.lowercase().contains(q) ||
                p.shortDescription.lowercase().contains(q) ||
                p.composition.lowercase().contains(q)
            catMatch && searchMatch
        }
    }

    PullToRefreshBox(
        isRefreshing = app.refreshing,
        onRefresh = { app.refresh() },
        modifier = Modifier.fillMaxSize().background(Paper),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // 1. Sunrise & Hills Hero Banner (from gmm/templates/shop/products.html & premium.css)
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0B2417),
                                Color(0xFF14472C),
                                Color(0xFF2E7A44),
                            )
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(1.dp, SunGold.copy(alpha = 0.7f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Verified, contentDescription = null, tint = SunGold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (lang == "bn") "১০০% জৈব সার ও জৈব কীটনাশক · রাসায়নিকমুক্ত" else "100% Organic Fertilizer & Bio-Pesticide · No Chemicals",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                Text(
                    text = t("Organic farming, a promise of good health"),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    text = t("100% Organic Fertilizers & Bio-Pesticides from GMM Distributors, delivered to your door."),
                    color = MutedOnDark,
                    style = MaterialTheme.typography.bodyMedium,
                )

                // Hero Featured Product Pod Preview
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SunGold)
                            .clickable {
                                val msg = if (lang == "bn") {
                                    "নমস্কার GMM ডিস্ট্রিবিউটরস, আমি আপনাদের জৈব সার ও জৈব কীটনাশক অর্ডার করতে চাই।"
                                } else {
                                    "Namaste GMM Distributors, I want to enquire about your 100% organic fertilizers & bio-pesticides."
                                }
                                actions.onSendEnquiryWhatsApp(msg)
                            }
                            .padding(vertical = 12.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (lang == "bn") "💬 হোয়াটসঅ্যাপে কথা বলুন" else "💬 Chat with Owner on WhatsApp",
                            color = SunInk,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            // 2. Main Body Container
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // 4 Trust Pillars (2x2 Grid)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TrustTile(
                        icon = Icons.Filled.Eco,
                        title = t("100% Organic"),
                        sub = t("No synthetic chemicals"),
                        modifier = Modifier.weight(1f),
                    )
                    TrustTile(
                        icon = Icons.Filled.SupportAgent,
                        title = t("WhatsApp Order"),
                        sub = t("Direct to owner"),
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TrustTile(
                        icon = Icons.Filled.LocalShipping,
                        title = if (lang == "bn") "₹১০০০+ ফ্রি ডেলিভারি" else "Free Delivery ₹1000+",
                        sub = t("Manual UPI / Bank") + " / COD",
                        modifier = Modifier.weight(1f),
                    )
                    TrustTile(
                        icon = Icons.Filled.ReceiptLong,
                        title = t("GST Invoice"),
                        sub = t("Proper tax bill"),
                        modifier = Modifier.weight(1f),
                    )
                }

                // Promo Banners for Krishi Paramarsha & GMM Thought Card Maker
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.horizontalGradient(listOf(Forest, Leaf)))
                        .clickable {
                            app.openCropSlug = null
                            app.thoughtCardOpen = false
                            app.tab = Tab.Advisory
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("🌾", fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = if (lang == "bn") "কৃষি পরামর্শ — ধান, আলু, পাট, সরিষা ও বেগুন" else "Krishi Paramarsha — Organic Crop Guides",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = if (lang == "bn") "মরসুম, জৈব সার ও জৈব কীটনাশক প্রয়োগের সহজ গাইড" else "Seasonal stages, organic fertilizer & bio-pesticide dosage",
                            color = MutedOnDark,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Text("→", color = SunGold, style = MaterialTheme.typography.headlineMedium)
                }

                // Search Bar
                OutlinedTextField(
                    value = app.searchQuery,
                    onValueChange = { app.searchQuery = it },
                    placeholder = { Text(t("Search organic fertilizer or bio-pesticide…")) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = AccentGreen) },
                    trailingIcon = {
                        if (app.searchQuery.isNotBlank()) {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.clickable { app.searchQuery = "" },
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = PaperLine,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Category Filter Chips
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChipPill(
                        label = if (lang == "bn") "সব পণ্য (${app.products.size})" else "All Organic (${app.products.size})",
                        selected = app.selectedCategorySlug == "all",
                        onClick = { app.selectedCategorySlug = "all" },
                    )
                    FilterChipPill(
                        label = if (lang == "bn") "🌱 জৈব সার (7)" else "🌱 Organic Fertilizers (7)",
                        selected = app.selectedCategorySlug == "fertilizer",
                        onClick = { app.selectedCategorySlug = "fertilizer" },
                    )
                    FilterChipPill(
                        label = if (lang == "bn") "🛡️ জৈব কীটনাশক (3)" else "🛡️ Bio-Pesticides (3)",
                        selected = app.selectedCategorySlug == "pesticide",
                        onClick = { app.selectedCategorySlug = "pesticide" },
                    )
                    app.categories.forEach { cat ->
                        FilterChipPill(
                            label = cat.localizedName(lang),
                            selected = app.selectedCategorySlug == cat.slug,
                            onClick = { app.selectedCategorySlug = cat.slug },
                        )
                    }
                }

                // Section Title
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = if (lang == "bn") "আমাদের ১০০% জৈব পণ্যসমূহ" else "Our 100% Organic Catalogue",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            text = if (lang == "bn") "প্যাকের মাপ বেছে কার্টে যোগ করুন (কোনো রাসায়নিক নেই)"
                            else "Choose a pack size and add to cart (Strictly Zero Chemicals)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }

                // Product Cards List
                filteredProducts.forEach { product ->
                    OrganicProductCard(
                        product = product,
                        lang = lang,
                        cartQuantities = app.cartQuantities,
                        onUpdateQty = { variantId, qty -> app.setVariantQty(variantId, qty) },
                        onOpenDetail = { app.openProductSlug = product.slug },
                    )
                }

                // How Ordering Works (3-step card from GMM)
                AppCard(title = if (lang == "bn") "কীভাবে অর্ডার ও পেমেন্ট করবেন?" else "How WhatsApp Ordering Works") {
                    HowItWorksStep(
                        num = "1",
                        title = if (lang == "bn") "জৈব সার বা কীটনাশক বেছে নিন" else "Choose Your Organic Packs",
                        desc = if (lang == "bn") "১০টি খাঁটি জৈব পণ্যের মধ্যে আপনার জমির প্রয়োজন অনুযায়ী প্যাক কার্টে রাখুন।"
                        else "Select from our 10 fixed 100% organic fertilizers & bio-pesticides.",
                    )
                    HowItWorksStep(
                        num = "2",
                        title = if (lang == "bn") "অর্ডার করুন ও হোয়াটসঅ্যাপে পাঠান" else "Place Order & Send to Owner on WhatsApp",
                        desc = if (lang == "bn") "কোনো অনলাইন পেমেন্ট গেটওয়ে নেই — এক ট্যাপে অর্ডারটি সরাসরি মালিকের হোয়াটসঅ্যাপে চলে যাবে।"
                        else "No online payment gateway needed — one tap sends your full order receipt to our owner on WhatsApp.",
                    )
                    HowItWorksStep(
                        num = "3",
                        title = if (lang == "bn") "ম্যানুয়াল UPI / ব্যাংক বা ডেলিভারিতে পেমেন্ট" else "Pay Manually via UPI, Bank or Cash on Delivery",
                        desc = if (lang == "bn") "মালিক অর্ডার নিশ্চিত করার পর ম্যানুয়ালি UPI/ব্যাংক ট্রান্সফার করুন অথবা ডেলিভারির সময় টাকা দিন।"
                        else "Owner manually collects payment via Bank Account, UPI ID, or Cash on Delivery and dispatches with a GST Invoice.",
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TrustTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    sub: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, PaperLine, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(Paper2),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(sub, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun FilterChipPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Leaf else CardBg)
            .border(1.5.dp, if (selected) Leaf else PaperLine, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else TextMain,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OrganicProductCard(
    product: Product,
    lang: String,
    cartQuantities: Map<Int, Int>,
    onUpdateQty: (Int, Int) -> Unit,
    onOpenDetail: () -> Unit,
) {
    var selectedVariantIdx by remember(product.id) { mutableIntStateOf(0) }
    val selectedVariant = product.variants.getOrElse(selectedVariantIdx) { product.variants.first() }
    val currentCartQty = cartQuantities[selectedVariant.id] ?: 0
    var addStepQty by remember(selectedVariant.id) { mutableIntStateOf(1) }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(CardBg)
            .border(1.dp, PaperLine, RoundedCornerShape(22.dp)),
    ) {
        // Media Header
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFFFFF4DA),
                            Color(0xFFF4F8EE),
                            Color(0xFFE6EFE3),
                        )
                    )
                )
                .clickable(onClick = onOpenDetail)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            DrawableImage(
                drawableName = product.drawableName,
                contentDescription = product.localizedName(lang),
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(190.dp).fillMaxWidth(),
            )

            // Top-left Featured & Kind Badges
            Row(
                Modifier.align(Alignment.TopStart),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (product.isFeatured) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Forest)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(t("Featured"), color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
                OrganicBadge(
                    text = if (product.kind == "pesticide") t("Organic Bio-Pesticide") else t("Organic Fertilizer"),
                    isPesticide = product.kind == "pesticide",
                )
            }

            // Top-right 100% Chemical-Free stamp
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.9f))
                    .border(1.dp, Leaf, RoundedCornerShape(50))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (lang == "bn") "🌿 ১০০% জৈব" else "🌿 Zero Chemical",
                    color = Forest,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                )
            }
        }

        // Body
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = product.localizedName(lang),
                style = MaterialTheme.typography.titleLarge,
                color = TextMain,
                modifier = Modifier.clickable(onClick = onOpenDetail),
            )
            Text(
                text = product.localizedShort(lang),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )

            // Pack Size Selector Chips
            Text(
                text = t("Choose pack size"),
                style = MaterialTheme.typography.labelMedium,
                color = Muted,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                product.variants.forEachIndexed { idx, v ->
                    val isSelected = idx == selectedVariantIdx
                    val vQtyInCart = cartQuantities[v.id] ?: 0
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) MintSoft else CardBg)
                            .border(
                                width = if (isSelected) 2.dp else 1.5.dp,
                                color = if (isSelected) Leaf else PaperLine,
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable { selectedVariantIdx = idx }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = v.localizedLabel(lang) + if (vQtyInCart > 0) " ($vQtyInCart)" else "",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                            color = if (isSelected) Forest else TextMain,
                        )
                        Text(
                            text = formatRupees(v.price),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) Leaf else AccentGreen,
                        )
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${t("Prices include GST")} (${selectedVariant.gstRate.toInt()}% GST · HSN ${selectedVariant.hsnCode})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
                Text(
                    text = if (lang == "bn") "স্টকে আছে (${selectedVariant.stock})" else "In Stock (${selectedVariant.stock})",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentGreen,
                )
            }

            // Add to Cart / Quantity Control Row
            if (currentCartQty > 0) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${t("In Cart")}: $currentCartQty × ${selectedVariant.localizedLabel(lang)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentGreen,
                        )
                    }
                    QuantityStepper(
                        qty = currentCartQty,
                        maxQty = 10,
                        onChange = { newQ -> onUpdateQty(selectedVariant.id, newQ) },
                    )
                }
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    QuantityStepper(
                        qty = addStepQty,
                        maxQty = 10,
                        onChange = { if (it >= 1) addStepQty = it },
                    )
                    BigButton(
                        text = "🛒 ${t("Add to Cart")} · ${formatRupees(selectedVariant.price * addStepQty)}",
                        modifier = Modifier.weight(1f),
                        onClick = { onUpdateQty(selectedVariant.id, addStepQty) },
                    )
                }
            }

            // View Detail & Crop Dosage Calculator link
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Paper2)
                    .clickable(onClick = onOpenDetail)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Calculate, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = t("View Details & Crop Dose Calculator"),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMain,
                    )
                }
                Text("→", color = AccentGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HowItWorksStep(num: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(Forest),
            contentAlignment = Alignment.Center,
        ) {
            Text(num, color = SunGold, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = Muted)
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
    var selectedVariantIdx by remember(product.id) { mutableIntStateOf(0) }
    val selectedVariant = product.variants.getOrElse(selectedVariantIdx) { product.variants.first() }
    val currentCartQty = app.cartQuantities[selectedVariant.id] ?: 0
    var selectedCropIdx by remember(product.id) { mutableIntStateOf(0) }
    var areaOrTreesInput by remember(product.id) { mutableStateOf("2") }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
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
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = AccentGreen, modifier = Modifier.size(18.dp))
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
            DrawableImage(
                drawableName = product.drawableName,
                contentDescription = product.localizedName(lang),
                modifier = Modifier.height(230.dp).fillMaxWidth(),
            )
        }

        // Title, Badges & Description
        AppCard {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OrganicBadge(
                    text = if (product.kind == "pesticide") t("Organic Bio-Pesticide") else t("Organic Fertilizer"),
                    isPesticide = product.kind == "pesticide",
                )
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
                        maxQty = 10,
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
                    text = "🛒 ${t("Add to Cart")} · ${formatRupees(selectedVariant.price)}",
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
}
