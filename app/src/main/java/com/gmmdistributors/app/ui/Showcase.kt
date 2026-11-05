package com.gmmdistributors.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gmmdistributors.app.AppController
import com.gmmdistributors.app.api.Banner
import com.gmmdistributors.app.api.Product
import com.gmmdistributors.app.api.ProductVariant
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------------------------
// Showcase: e-commerce grade home and buying components (carousel, circles, rails, price block,
// pack sheet, sticky buy bar). Everything still works offline from the phone's catalogue.
// ---------------------------------------------------------------------------------------------

private fun bannerBrush(theme: String): Brush = when (theme) {
    "sun" -> Brush.linearGradient(listOf(Color(0xFFF59A23), Color(0xFFE0701A)))
    "earth" -> Brush.linearGradient(listOf(Color(0xFFB4532A), Color(0xFF7A3218)))
    "neem" -> Brush.linearGradient(listOf(Color(0xFF8A6A12), Color(0xFF4F6B1F)))
    else -> Brush.linearGradient(listOf(Color(0xFF1F6B3A), Color(0xFF0F2F1E)))
}

/** Auto-sliding offer carousel (4.5 s per banner, pauses while the finger is on it). */
@Composable
fun HeroCarousel(banners: List<Banner>, lang: String, onTap: (Banner) -> Unit) {
    if (banners.isEmpty()) return
    val pager = rememberPagerState { banners.size }
    LaunchedEffect(pager.currentPage, banners.size) {
        delay(4500)
        if (!pager.isScrollInProgress && banners.size > 1) pager.animateScrollToPage((pager.currentPage + 1) % banners.size)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(state = pager, pageSpacing = 10.dp, modifier = Modifier.fillMaxWidth()) { i ->
            val b = banners[i]
            Box(
                Modifier.fillMaxWidth().height(168.dp).shadow(6.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp)).background(bannerBrush(b.theme)).clickable { onTap(b) },
            ) {
                if (b.imageUrl.isNotBlank()) BannerPhoto(b.imageUrl)
                // decorative circles for depth
                Box(Modifier.size(180.dp).align(Alignment.TopEnd).padding(start = 60.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.07f)))
                Box(Modifier.size(90.dp).align(Alignment.BottomEnd).clip(CircleShape).background(Color.White.copy(alpha = 0.06f)))
                Column(
                    Modifier.fillMaxSize().background(
                        if (b.imageUrl.isNotBlank()) Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    ).padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth(0.82f)) {
                        Text(b.t(lang), style = MaterialTheme.typography.headlineMedium, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (b.sub(lang).isNotBlank()) Text(b.sub(lang), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.88f), maxLines = 2)
                    }
                    if (b.btn(lang).isNotBlank()) {
                        Text(
                            "${b.btn(lang)} →", style = MaterialTheme.typography.labelLarge, color = SunInk,
                            modifier = Modifier.clip(RoundedCornerShape(50)).background(SunGold).padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
        if (banners.size > 1) {
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                banners.indices.forEach { i ->
                    val on = i == pager.currentPage
                    val w by animateDpAsState(if (on) 22.dp else 7.dp, label = "dot")
                    val c by animateColorAsState(if (on) Leaf else PaperLine, label = "dotc")
                    Box(Modifier.height(7.dp).width(w).clip(RoundedCornerShape(50)).background(c))
                }
            }
        }
    }
}

@Composable
private fun BannerPhoto(url: String) {
    val dir = LocalContext.current.filesDir
    val bmp by produceState(initialValue = RemoteImages.cached(url), url) { if (value == null) value = RemoteImages.load(url, dir, 1400) }
    bmp?.let { Image(bitmap = it, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
}

/** Myntra-style category circles. */
@Composable
fun CategoryCircles(kinds: List<String>, selected: String, lang: String, onPick: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        (listOf("all") + kinds).forEach { k ->
            val on = selected == k
            val c = if (k == "all") Forest else kindColor(k)
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(74.dp).clickable { onPick(k) }) {
                Box(
                    Modifier.size(66.dp).clip(CircleShape)
                        .background(if (on) c else c.copy(alpha = 0.12f))
                        .border(if (on) 3.dp else 0.dp, if (on) SunGold else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (k == "all") Icon(Icons.Filled.ShoppingBag, null, tint = if (on) Color.White else c, modifier = Modifier.size(30.dp))
                    else Icon(kindIcon(k), null, tint = if (on) Color.White else c, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    when (k) {
                        "all" -> if (lang == "bn") "সব" else "All"
                        "fertilizer" -> if (lang == "bn") "সার" else "Fertilizer"
                        "pesticide" -> if (lang == "bn") "কীটনাশক" else "Pest care"
                        "ayush" -> if (lang == "bn") "আয়ুষ" else "Ayush"
                        else -> if (lang == "bn") "খাবার" else "Food"
                    },
                    style = MaterialTheme.typography.labelLarge, color = if (on) TextMain else Muted, maxLines = 1,
                )
            }
        }
    }
}

/** Price with struck-through MRP and "% OFF" when the admin entered a higher MRP. */
@Composable
fun PriceBlock(v: ProductVariant, big: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(formatRupees(v.price), style = if (big) MaterialTheme.typography.displayMedium else MaterialTheme.typography.headlineMedium,
            color = TextMain, fontWeight = FontWeight.ExtraBold)
        val mrp = v.mrp
        if (mrp != null && v.discountPct > 0) {
            Text(formatRupees(mrp), style = MaterialTheme.typography.bodyMedium, color = Muted, textDecoration = TextDecoration.LineThrough)
            Text("${v.discountPct}% OFF", style = MaterialTheme.typography.labelLarge, color = KindFood, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun WishHeart(on: Boolean, modifier: Modifier = Modifier, onToggle: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier.size(40.dp).shadow(2.dp, CircleShape).clip(CircleShape).background(CardBg)
            .clickable { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onToggle() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(if (on) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = "Wishlist",
            tint = if (on) Color(0xFFE53958) else Muted, modifier = Modifier.size(22.dp))
    }
}

/** Horizontal rail ("Bestsellers", "Your wishlist"). */
@Composable
fun ProductRail(title: String, items: List<Product>, app: AppController) {
    if (items.isEmpty()) return
    val lang = app.language
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEach { p ->
                val v = p.defaultVariant
                Column(
                    Modifier.width(150.dp).shadow(2.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(CardBg)
                        .clickable { app.openProductSlug = p.slug },
                ) {
                    Box(Modifier.fillMaxWidth().height(130.dp).background(Color(0xFFF4F7EF))) {
                        ProductArt(p, Modifier.fillMaxSize().padding(8.dp))
                        if (v != null && v.discountPct > 0) {
                            Text("${v.discountPct}% OFF", style = MaterialTheme.typography.labelMedium, color = Color.White,
                                modifier = Modifier.align(Alignment.TopStart).padding(6.dp).clip(RoundedCornerShape(6.dp)).background(KindFood).padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Column(Modifier.padding(10.dp)) {
                        Text(p.localizedName(lang), style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (v != null) Text(formatRupees(v.price), style = MaterialTheme.typography.titleMedium, color = AccentGreen, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

/** Myntra-style "select size" sheet for products with several pack sizes. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackPickerSheet(app: AppController) {
    val p = app.packSheetSlug?.let { s -> app.products.firstOrNull { it.slug == s } } ?: return
    val lang = app.language
    val bn = lang == "bn"
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var picked by remember(p.id) { mutableIntStateOf(p.variants.indexOfFirst { it.inStock }.coerceAtLeast(0)) }
    val haptic = LocalHapticFeedback.current
    ModalBottomSheet(onDismissRequest = { app.packSheetSlug = null }, sheetState = sheet, containerColor = CardBg) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).navigationBarsPadding().padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProductArt(p, Modifier.size(64.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF4F7EF)).padding(4.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(p.localizedName(lang), style = MaterialTheme.typography.titleMedium, maxLines = 2)
                    Text(if (bn) "প্যাকের সাইজ বেছে নিন" else "Select pack size", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                p.variants.forEachIndexed { i, v ->
                    val on = i == picked
                    Column(
                        Modifier.clip(RoundedCornerShape(16.dp))
                            .background(if (on) MintSoft else Paper)
                            .border(if (on) 2.dp else 1.dp, if (on) Leaf else PaperLine, RoundedCornerShape(16.dp))
                            .clickable(enabled = v.inStock) { picked = i }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(v.localizedLabel(lang), style = MaterialTheme.typography.titleMedium, color = if (v.inStock) TextMain else Muted)
                        Text(formatRupees(v.price), style = MaterialTheme.typography.labelLarge, color = if (v.inStock) AccentGreen else Muted)
                        if (!v.inStock) Text(if (bn) "স্টকে নেই" else "Sold out", style = MaterialTheme.typography.labelMedium, color = Danger)
                        else if (v.stock < 10) Text(if (bn) "মাত্র ${v.stock} টি" else "Only ${v.stock} left", style = MaterialTheme.typography.labelMedium, color = KindFood)
                    }
                }
            }
            val v = p.variants.getOrNull(picked)
            if (v != null) PriceBlock(v)
            BigButton(
                text = if (v?.inStock == true) "${if (bn) "কার্টে যোগ করুন" else "Add to cart"}" else (if (bn) "স্টকে নেই" else "Sold out"),
                enabled = v?.inStock == true,
                onClick = {
                    if (v != null) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        app.setVariantQty(v.id, (app.cartQuantities[v.id] ?: 0) + 1)
                        app.packSheetSlug = null
                    }
                },
            )
        }
    }
}

/** Sticky bottom bar on the product page: price and the one action, always in thumb reach. */
@Composable
fun StickyBuyBar(v: ProductVariant, inCart: Int, lang: String, onAdd: () -> Unit, onGoToBag: () -> Unit, modifier: Modifier = Modifier) {
    val bn = lang == "bn"
    Row(
        modifier.fillMaxWidth().shadow(16.dp).background(CardBg).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            PriceBlock(v)
            Text(if (bn) "দাম জিএসটি সহ" else "Price incl. GST", style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Box(Modifier.weight(1.1f)) {
            when {
                !v.inStock -> BigButton(text = if (bn) "স্টকে নেই" else "Sold out", enabled = false, onClick = {})
                inCart > 0 -> BigButton(text = if (bn) "কার্টে যান ($inCart) →" else "Go to cart ($inCart) →", gold = true, onClick = onGoToBag)
                else -> BigButton(text = "${if (bn) "কার্টে যোগ" else "Add to cart"}", onClick = onAdd)
            }
        }
    }
}

/** Ayush medicines: label caution, licence number and when to see a doctor. Always visible, never hidden. */
@Composable
fun MedicineCautionCard(p: Product, lang: String) {
    val bn = lang == "bn"
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(KindAyush.copy(alpha = 0.09f))
            .border(1.5.dp, KindAyush.copy(alpha = 0.45f), RoundedCornerShape(16.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(if (bn) "⚕ ব্যবহারের আগে পড়ুন" else "⚕ Read before use", style = MaterialTheme.typography.titleMedium, color = KindAyush)
        val c = p.localizedCaution(lang)
        Text(
            c.ifBlank { if (bn) "আয়ুর্বেদিক ওষুধ। লেবেলে লেখা নিয়ম মেনে ব্যবহার করুন। শিশুদের নাগালের বাইরে রাখুন।" else "Ayurvedic medicine. Use only as written on the label. Keep away from children." },
            style = MaterialTheme.typography.bodyMedium, color = TextMain,
        )
        if (p.licenseNo.isNotBlank()) {
            Text("${if (bn) "লাইসেন্স নং" else "Licence no."}: ${p.licenseNo}", style = MaterialTheme.typography.labelLarge, color = KindAyush)
        }
        Text(
            if (bn) "সমস্যা না কমলে বা বাড়লে ডাক্তার দেখান। এটি ডাক্তারের চিকিৎসার বিকল্প নয়।"
            else "If it does not get better, or gets worse, see a doctor. This is not a substitute for medical care.",
            style = MaterialTheme.typography.bodySmall, color = Muted,
        )
    }
}

@Composable
fun ShareButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.size(40.dp).shadow(2.dp, CircleShape).clip(CircleShape).background(CardBg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Share, contentDescription = "Share", tint = AccentGreen, modifier = Modifier.size(20.dp))
    }
}
