package com.gmmdistributors.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gmmdistributors.app.api.Order

/** Gentle 3% shrink while pressed: the "this is tappable and I felt it" cue on cards. */
fun Modifier.pressScale(interaction: MutableInteractionSource): Modifier = composed {
    val pressed by interaction.collectIsPressedAsState()
    val s by animateFloatAsState(if (pressed) 0.97f else 1f, spring(stiffness = Spring.StiffnessMediumLow), label = "press")
    this.scale(s)
}

/** Shimmering placeholder block. */
@Composable
fun Shimmer(modifier: Modifier) {
    val t = rememberInfiniteTransition(label = "shimmer")
    val x by t.animateFloat(
        initialValue = -600f, targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart), label = "x",
    )
    val base = Paper2
    val shine = if (ThemeState.dark) Color(0xFF26352B) else Color(0xFFF3F6F1)
    Box(modifier.background(Brush.linearGradient(listOf(base, shine, base), start = Offset(x, 0f), end = Offset(x + 600f, 300f))))
}

/** Skeleton of the home screen shown on first launch while the catalogue downloads. */
@Composable
fun HomeSkeleton() {
    Column(Modifier.fillMaxSize().background(Paper).verticalScroll(rememberScrollState())) {
        Column(Modifier.fillMaxWidth().background(Forest).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.fillMaxWidth(0.55f).height(26.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.18f)))
            Box(Modifier.fillMaxWidth(0.85f).height(16.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.12f)))
            Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.9f)))
        }
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) { Shimmer(Modifier.weight(1f).height(118.dp).clip(RoundedCornerShape(20.dp))) }
            }
            Shimmer(Modifier.fillMaxWidth(0.4f).height(22.dp).clip(RoundedCornerShape(8.dp)))
            repeat(2) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(2) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Shimmer(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(20.dp)))
                            Shimmer(Modifier.fillMaxWidth(0.8f).height(16.dp).clip(RoundedCornerShape(6.dp)))
                            Shimmer(Modifier.fillMaxWidth(0.5f).height(22.dp).clip(RoundedCornerShape(6.dp)))
                            Shimmer(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(14.dp)))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-screen "order placed" moment. A first-time buyer needs certainty that the order really went
 * through, the order number, and exactly ONE next step (pay, or wait for delivery if COD).
 */
@Composable
fun OrderSuccessScreen(order: Order, lang: String, onPrimary: () -> Unit, onWhatsApp: (() -> Unit)?, onContinue: () -> Unit) {
    val bn = lang == "bn"
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(order.orderNo) { shown = true }
    val pop by animateFloatAsState(if (shown) 1f else 0.3f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow), label = "pop")
    val fade by animateFloatAsState(if (shown) 1f else 0f, tween(450, delayMillis = 150, easing = FastOutSlowInEasing), label = "fade")
    val cod = order.paymentMethod == "COD"

    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Forest, Forest2))).verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(40.dp))
        Box(Modifier.size(112.dp).scale(pop).clip(CircleShape).background(SunGold), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Forest, modifier = Modifier.size(64.dp))
        }
        Spacer(Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth().scale(0.96f + 0.04f * fade), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (bn) "অর্ডার হয়ে গেছে!" else "Order placed!", style = MaterialTheme.typography.displayMedium,
                color = Color.White.copy(alpha = fade), textAlign = TextAlign.Center)
            Text(order.orderNo, style = MaterialTheme.typography.titleLarge, color = SunGold.copy(alpha = fade))
            Text(
                "${order.items.sumOf { it.quantity }} ${if (bn) "টি প্যাক" else "packs"} · ${formatRupees(order.total)}",
                style = MaterialTheme.typography.bodyLarge, color = MutedOnDark.copy(alpha = fade),
            )
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.1f * fade)).padding(16.dp)) {
                Text(
                    if (cod) (if (bn) "আমরা অর্ডার নিশ্চিত করে পাঠিয়ে দেব। পণ্য হাতে পেয়ে নগদ দেবেন।"
                    else "We will confirm and send your order. Pay in cash when it arrives.")
                    else (if (bn) "শেষ ধাপ: ${formatRupees(order.total)} পেমেন্ট করে UTR নম্বর দিন। পেমেন্ট যাচাই হলেই আমরা পাঠিয়ে দেব।"
                    else "Last step: pay ${formatRupees(order.total)} and enter the UTR number. We dispatch as soon as it is verified."),
                    style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = fade), textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Spacer(Modifier.height(28.dp))
        BigButton(
            text = if (cod) (if (bn) "অর্ডার দেখুন" else "View order") else (if (bn) "এখনই পেমেন্ট করুন" else "Pay now"),
            gold = true, onClick = onPrimary,
        )
        Spacer(Modifier.height(10.dp))
        if (onWhatsApp != null) {
            GhostButton(text = if (bn) "💬 হোয়াটসঅ্যাপে জানান" else "💬 Tell us on WhatsApp", onDark = true, onClick = onWhatsApp)
            Spacer(Modifier.height(10.dp))
        }
        GhostButton(text = if (bn) "আরও কেনাকাটা করুন" else "Continue shopping", onDark = true, onClick = onContinue)
        Spacer(Modifier.height(40.dp))
    }
}

/** Order saved on the phone while offline: reassure, explain what happens next, offer SMS (no data needed). */
@Composable
fun QueuedOrderScreen(order: com.gmmdistributors.app.api.PendingOrder, lang: String, canSms: Boolean, onSms: () -> Unit, onDone: () -> Unit) {
    val bn = lang == "bn"
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(order.key) { shown = true }
    val pop by animateFloatAsState(if (shown) 1f else 0.3f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow), label = "pop")
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Forest, Forest2))).verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(40.dp))
        Box(Modifier.size(112.dp).scale(pop).clip(CircleShape).background(SunGold), contentAlignment = Alignment.Center) {
            Text("💾", style = MaterialTheme.typography.displayMedium)
        }
        Spacer(Modifier.height(24.dp))
        Text(if (bn) "অর্ডার ফোনে সেভ হয়েছে" else "Order saved on your phone", style = MaterialTheme.typography.displayMedium,
            color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("${order.itemsCount} ${if (bn) "টি প্যাক" else "packs"} · ${formatRupees(order.expectedTotal)}",
            style = MaterialTheme.typography.titleLarge, color = SunGold)
        Spacer(Modifier.height(14.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.1f)).padding(16.dp)) {
            Text(
                if (bn) "এখন ইন্টারনেট নেই। নেট এলেই অর্ডারটি নিজে থেকে আমাদের কাছে চলে যাবে, অ্যাপ বন্ধ থাকলেও। দাম তখন আবার মিলিয়ে দেখা হবে; বদলালে আমরা ফোন করে নেব।"
                else "There is no internet right now. The order will be sent automatically as soon as you are online, even if the app is closed. Prices are re-checked then; if anything changed we will call you first.",
                style = MaterialTheme.typography.bodyLarge, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(28.dp))
        if (canSms) {
            BigButton(text = if (bn) "✉️ এখনই SMS-এ পাঠান (নেট লাগবে না)" else "✉️ Send now by SMS (no data needed)", gold = true, onClick = onSms)
            Spacer(Modifier.height(10.dp))
        }
        GhostButton(text = if (bn) "ঠিক আছে" else "OK", onDark = true, onClick = onDone)
        Spacer(Modifier.height(40.dp))
    }
}
