package com.gmmdistributors.app.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmmdistributors.app.AppController
import com.gmmdistributors.app.R

/** First launch: two huge buttons. Picture + native script, so it works even for people who read neither well. */
@Composable
fun LanguagePickerScreen(app: AppController) {
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Forest, Forest2))).verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(32.dp))
        Image(painterResource(R.drawable.gmm_logo), contentDescription = null, contentScale = ContentScale.Crop,
            modifier = Modifier.size(120.dp).clip(CircleShape).border(3.dp, SunGold, CircleShape))
        Spacer(Modifier.height(20.dp))
        Text("GMM Organic", style = MaterialTheme.typography.displayMedium, color = Color.White)
        Text("জৈব চাষ, সুস্থ জীবন · Organic farming, healthy living", style = MaterialTheme.typography.bodyMedium,
            color = MutedOnDark, textAlign = TextAlign.Center)
        Spacer(Modifier.height(36.dp))
        Text("ভাষা বেছে নিন · Choose language", style = MaterialTheme.typography.titleLarge, color = SunGold)
        Spacer(Modifier.height(14.dp))
        LangButton("বাংলা", "Bengali", gold = true) { app.finishOnboarding("bn") }
        Spacer(Modifier.height(12.dp))
        LangButton("English", "ইংরেজি", gold = false) { app.finishOnboarding("en") }
        Spacer(Modifier.height(24.dp))
        Text("পরে প্রোফাইল থেকে বদলাতে পারবেন · You can change this later in Profile",
            style = MaterialTheme.typography.bodySmall, color = MutedOnDark, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun LangButton(big: String, small: String, gold: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().heightIn(min = 84.dp).clip(RoundedCornerShape(22.dp))
            .background(if (gold) SunGold else Color.White.copy(alpha = 0.12f))
            .border(2.dp, if (gold) SunGold else Color.White.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(big, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = if (gold) SunInk else Color.White)
        Text(small, style = MaterialTheme.typography.bodyMedium, color = if (gold) SunInk else MutedOnDark)
    }
}

/**
 * Krishi Mitra: the growth loop. Every customer gets a short code (easy to read out on the phone) and a
 * WhatsApp invite. Orders with the code are credited to them in the admin; GMM decides the reward.
 */
@Composable
fun KrishiMitraCard(app: AppController, actions: PhoneActions) {
    val bn = app.language == "bn"
    LaunchedEffect(app.me?.id) { app.loadReferrals() }
    val stats = app.referrals
    val code = stats?.code?.ifBlank { null } ?: app.myReferralCode.ifBlank { null }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFF59A23), Color(0xFFE0701A))))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(if (bn) "🤝 কৃষি মিত্র হন" else "🤝 Become a Krishi Mitra", style = MaterialTheme.typography.headlineMedium, color = SunInk)
        Text(
            if (bn) "প্রতিবেশী কৃষকদের GMM-এর কথা বলুন। আপনার কোড দিয়ে যত অর্ডার হবে, সব আপনার নামে লেখা থাকবে; GMM পুরস্কার দেবে।"
            else "Tell neighbouring farmers about GMM. Every order made with your code is credited to you, and GMM rewards you.",
            style = MaterialTheme.typography.bodyMedium, color = SunInk,
        )
        if (code != null) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(if (bn) "আপনার কোড" else "Your code", style = MaterialTheme.typography.labelMedium, color = Muted)
                    Text(code, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Forest, letterSpacing = 2.sp)
                }
                SmallPill(if (bn) "কপি" else "Copy") { actions.onCopyText("Krishi Mitra code", code) }
            }
            if (stats != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox(stats.orders.toString(), if (bn) "অর্ডার" else "Orders", Modifier.weight(1f))
                    StatBox(stats.delivered.toString(), if (bn) "পৌঁছেছে" else "Delivered", Modifier.weight(1f))
                }
                stats.recent.take(3).forEach { (name, village, status) ->
                    Text("• $name, $village · $status", style = MaterialTheme.typography.bodySmall, color = SunInk)
                }
            }
            Row(
                Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(16.dp)).background(WhatsAppGreen)
                    .clickable { actions.onShareText(app.inviteText()) },
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(if (bn) "💬 হোয়াটসঅ্যাপে আমন্ত্রণ পাঠান" else "💬 Invite on WhatsApp", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        } else {
            Text(if (bn) "ইন্টারনেট চালু করলে আপনার কোড তৈরি হবে।" else "Your code is created when you are online.",
                style = MaterialTheme.typography.bodySmall, color = SunInk)
        }
    }
}

@Composable
private fun StatBox(n: String, label: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.35f)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(n, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = SunInk)
        Text(label, style = MaterialTheme.typography.labelMedium, color = SunInk)
    }
}

/** Checkout: who told you about GMM? Pre-filled from an invite link. */
@Composable
fun InviteCodeField(app: AppController) {
    val bn = app.language == "bn"
    AppField(
        label = if (bn) "কৃষি মিত্রের কোড (ঐচ্ছিক)" else "Krishi Mitra code (optional)",
        value = app.inviteCode,
        onChange = { app.setInvite(it) },
        placeholder = "GMM…",
        hint = if (app.inviteCode.isNotBlank()) (if (bn) "✓ এই অর্ডার তাঁর নামে লেখা হবে" else "✓ This order will be credited to them") else null,
    )
}


/** Profile: notification preferences. Order updates always come; offers & tips can be switched off. */
@Composable
fun NotificationSettingsCard(app: AppController) {
    val bn = app.language == "bn"
    AppCard(title = if (bn) "🔔 নোটিফিকেশন" else "🔔 Notifications") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (bn) "অফার ও কৃষি পরামর্শ" else "Offers & Krishi tips", style = MaterialTheme.typography.titleMedium)
                Text(if (bn) "ছাড়, মরসুমের মনে করানো ও চাষের টিপস" else "Discounts, season reminders and farming tips",
                    style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            androidx.compose.material3.Switch(checked = app.promoPush, onCheckedChange = { app.changePromoPush(it) })
        }
        Text(if (bn) "অর্ডারের খবর (নিশ্চিত, পাঠানো, পৌঁছানো) সবসময় আসবে।" else "Order updates (confirmed, sent, delivered) always arrive.",
            style = MaterialTheme.typography.bodySmall, color = Muted)
    }
}
