package com.gmmdistributors.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gmmdistributors.app.api.Order
import com.gmmdistributors.app.api.Product

// ---------------------------------------------------------------------------------------------
// Premium design kit. Rules: one primary action per card, 48dp+ touch targets, pictures before
// words, Bengali-first labels, prices always visible, nothing hidden behind long-press or swipe.
// ---------------------------------------------------------------------------------------------

fun kindColor(kind: String): Color = when (kind) {
    "pesticide" -> KindPest
    "food" -> KindFood
    "ayush" -> KindAyush
    else -> KindFert
}

fun kindIcon(kind: String): ImageVector = when (kind) {
    "pesticide" -> Icons.Filled.Spa
    "food" -> Icons.Filled.RiceBowl
    "ayush" -> Icons.Filled.Healing
    else -> Icons.Filled.Grass
}

fun kindLabel(kind: String, lang: String): String = when (kind) {
    "pesticide" -> if (lang == "bn") "জৈব কীটনাশক" else "Bio-pesticide"
    "food" -> if (lang == "bn") "জৈব খাদ্য" else "Organic food"
    "ayush" -> if (lang == "bn") "আয়ুষ ওষুধ" else "Ayush medicine"
    else -> if (lang == "bn") "জৈব সার" else "Organic fertilizer"
}

/** Product picture with a designed fallback (category colour + icon) instead of a broken/blank image. */
@Composable
fun ProductArt(product: Product, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Fit) {
    val ctx = LocalContext.current
    val hasBundled = remember(product.drawableName) {
        product.drawableName.isNotBlank() && ctx.resources.getIdentifier(product.drawableName, "drawable", ctx.packageName) != 0
    }
    if (product.imageUrl.isNotBlank() || hasBundled) {
        ProductImage(product = product, modifier = modifier, contentScale = contentScale)
    } else {
        val c = kindColor(product.kind)
        Box(
            modifier.background(Brush.linearGradient(listOf(c.copy(alpha = 0.16f), c.copy(alpha = 0.34f)))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(kindIcon(product.kind), contentDescription = null, tint = c, modifier = Modifier.size(56.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null) {
            Text(action, style = MaterialTheme.typography.labelLarge, color = AccentGreen,
                modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onAction).padding(horizontal = 10.dp, vertical = 8.dp))
        }
    }
}

/**
 * Two-column product card. Shows only what a buyer needs to decide: photo, name, pack, price,
 * stock, and ONE big button. Pack choice and details are one tap away on the product page.
 */
@Composable
fun ProductGridCard(
    product: Product,
    lang: String,
    qtyInCart: Int,
    modifier: Modifier = Modifier,
    wished: Boolean = false,
    onWish: () -> Unit = {},
    onOpen: () -> Unit,
    onAdd: () -> Unit,
    onQty: (Int) -> Unit,
) {
    val bn = lang == "bn"
    val v = product.defaultVariant
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val press = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Column(
        modifier
            .pressScale(press)
            .shadow(3.dp, RoundedCornerShape(20.dp), clip = false, ambientColor = Forest.copy(alpha = 0.15f), spotColor = Forest.copy(alpha = 0.15f))
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .clickable(interactionSource = press, indication = androidx.compose.foundation.LocalIndication.current, onClick = onOpen),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFFF4F7EF))) {
            ProductArt(product, Modifier.fillMaxSize().padding(10.dp))
            Box(
                Modifier.align(Alignment.TopStart).padding(8.dp).clip(RoundedCornerShape(50))
                    .background(kindColor(product.kind)).padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(kindLabel(product.kind, lang), style = MaterialTheme.typography.labelMedium, color = Color.White)
            }
            WishHeart(on = wished, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp), onToggle = onWish)
            if (v != null && v.discountPct > 0) {
                Text("${v.discountPct}% OFF", style = MaterialTheme.typography.labelMedium, color = Color.White,
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp).clip(RoundedCornerShape(6.dp)).background(KindFood).padding(horizontal = 7.dp, vertical = 2.dp))
            }
        }
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(product.localizedName(lang), style = MaterialTheme.typography.titleMedium, maxLines = 2,
                overflow = TextOverflow.Ellipsis, modifier = Modifier.heightIn(min = 48.dp))
            if (product.isMedicine) {
                Text(if (bn) "⚕ আয়ুর্বেদিক · লেবেল পড়ুন" else "⚕ Ayurvedic · read the label",
                    style = MaterialTheme.typography.labelMedium, color = KindAyush, maxLines = 1)
            }
            if (v != null) {
                Text(
                    if (product.variants.size > 1) "${v.localizedLabel(lang)} · ${if (bn) "আরও ${product.variants.size - 1}টি সাইজ" else "+${product.variants.size - 1} sizes"}"
                    else v.localizedLabel(lang),
                    style = MaterialTheme.typography.bodySmall, color = Muted, maxLines = 1,
                )
                PriceBlock(v)
                Spacer(Modifier.height(2.dp))
                when {
                    !v.inStock -> Box(
                        Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(14.dp)).background(Paper2),
                        contentAlignment = Alignment.Center,
                    ) { Text(if (bn) "স্টকে নেই" else "Out of stock", style = MaterialTheme.typography.labelLarge, color = Muted) }
                    qtyInCart > 0 -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        QuantityStepper(qty = qtyInCart, maxQty = minOf(10, maxOf(v.stock, qtyInCart)), onChange = onQty)
                    }
                    else -> Row(
                        Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(14.dp)).background(Leaf)
                            .clickable(role = Role.Button) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onAdd()
                            },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (bn) "কার্টে যোগ করুন" else "Add", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }
            }
        }
    }
}

/** "Order again": repeat-buying is the main habit for seasonal farm inputs. */
@Composable
fun ReorderCard(order: Order, lang: String, onReorder: () -> Unit) {
    val bn = lang == "bn"
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(MintSoft).clickable(onClick = onReorder).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(Leaf), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.History, contentDescription = null, tint = Color.White)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(if (bn) "আবার একই অর্ডার করুন" else "Order the same again", style = MaterialTheme.typography.titleMedium, color = Forest)
            Text(
                order.items.joinToString(", ") { "${it.quantity}× ${it.localizedName(lang)}" },
                style = MaterialTheme.typography.bodySmall, color = Color(0xFF2B3A24), maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        Text(if (bn) "এক ট্যাপে →" else "One tap →", style = MaterialTheme.typography.labelLarge, color = Leaf)
    }
}

/** Always-visible human help: many first-time buyers trust a call more than an app. */
@Composable
fun HelpStrip(lang: String, phone: String, showWhatsApp: Boolean, onCall: () -> Unit, onWhatsApp: () -> Unit) {
    val bn = lang == "bn"
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Forest).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(if (bn) "অর্ডার করতে অসুবিধা হচ্ছে?" else "Need help ordering?", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Text(
            if (bn) "ফোন করুন, আমরা আপনার হয়ে অর্ডার করে দেব।" else "Call us and we will place the order for you.",
            style = MaterialTheme.typography.bodyMedium, color = MutedOnDark,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (phone.isNotBlank()) {
                HelpButton(Icons.Filled.Call, if (bn) "ফোন করুন" else "Call", SunGold, SunInk, Modifier.weight(1f), onCall)
            }
            if (showWhatsApp) {
                HelpButton(Icons.AutoMirrored.Filled.Chat, "WhatsApp", WhatsAppGreen, Color.White, Modifier.weight(1f), onWhatsApp)
            }
        }
    }
}

@Composable
private fun HelpButton(icon: ImageVector, text: String, bg: Color, fg: Color, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier.height(50.dp).clip(RoundedCornerShape(14.dp)).background(bg).clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = fg)
    }
}
