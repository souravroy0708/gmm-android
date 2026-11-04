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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gmmdistributors.app.AppController
import com.gmmdistributors.app.api.KrishiTip

private val KINDS = listOf(
    Triple("order", "📦", "অর্ডারে সমস্যা" to "Order problem"),
    Triple("delivery", "🚚", "ডেলিভারি" to "Delivery"),
    Triple("product", "🏷", "দাম / পণ্য ভুল" to "Wrong price / product"),
    Triple("app", "📱", "অ্যাপ চলছে না" to "App not working"),
    Triple("idea", "💡", "পরামর্শ" to "Suggestion"),
    Triple("other", "💬", "অন্য কিছু" to "Something else"),
)

/** "Report a problem": the screen the customer was on is already attached as a screenshot. */
@Composable
fun ReportProblemScreen(app: AppController, actions: PhoneActions) {
    val bn = app.language == "bn"
    val g = app.guestAddress
    var kind by rememberSaveable { mutableStateOf(if (app.reportOrderNo.isNotBlank()) "order" else "app") }
    var text by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf(app.me?.name?.takeIf { app.signedIn } ?: g?.fullName ?: "") }
    var phone by rememberSaveable { mutableStateOf(app.me?.phone?.takeIf { app.signedIn } ?: g?.phone ?: "") }
    var keepShot by rememberSaveable { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current
    val shot = app.reportShot
    val bitmap = remember(shot) { shot?.let { runCatching { android.graphics.BitmapFactory.decodeFile(it)?.asImageBitmap() }.getOrNull() } }

    Column(
        Modifier.fillMaxSize().background(Paper).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🛟", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(if (bn) "সমস্যা জানান" else "Report a problem", style = MaterialTheme.typography.headlineMedium)
                Text(if (bn) "আমরা দেখে ফোন করে সাহায্য করব" else "We will look at it and call you back", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            Text(if (bn) "বন্ধ" else "Close", style = MaterialTheme.typography.labelLarge, color = AccentGreen,
                modifier = Modifier.clip(RoundedCornerShape(50)).clickable { app.closeReport() }.padding(10.dp))
        }

        AppCard(title = if (bn) "কী নিয়ে সমস্যা?" else "What is it about?") {
            KINDS.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { (code, icon, label) ->
                        val on = kind == code
                        Row(
                            Modifier.weight(1f).heightIn(min = 52.dp).clip(RoundedCornerShape(14.dp))
                                .background(if (on) MintSoft else Paper).border(if (on) 2.dp else 1.dp, if (on) Leaf else PaperLine, RoundedCornerShape(14.dp))
                                .clickable { kind = code }.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(icon, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.width(8.dp))
                            Text(if (bn) label.first else label.second, style = MaterialTheme.typography.labelLarge, color = if (on) Forest else TextMain)
                        }
                    }
                }
            }
            if (app.reportOrderNo.isNotBlank()) Text("${if (bn) "অর্ডার" else "Order"}: ${app.reportOrderNo}", style = MaterialTheme.typography.labelLarge, color = AccentGreen)
        }

        AppCard(title = if (bn) "কী হয়েছে?" else "What happened?") {
            OutlinedTextField(
                value = text, onValueChange = { text = it.take(2000); error = null },
                placeholder = { Text(if (bn) "যেমন: পেমেন্ট বোতাম টিপলে কিছু হচ্ছে না" else "e.g. Nothing happens when I tap Pay") },
                minLines = 4, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = CardBg, unfocusedContainerColor = CardBg,
                    focusedBorderColor = AccentGreen, unfocusedBorderColor = PaperLine),
            )
        }

        AppCard(title = if (bn) "স্ক্রিনশট" else "Screenshot") {
            if (bitmap != null && keepShot) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Image(bitmap, contentDescription = "screenshot", contentScale = ContentScale.Fit,
                        modifier = Modifier.height(260.dp).clip(RoundedCornerShape(14.dp)).border(1.dp, PaperLine, RoundedCornerShape(14.dp)))
                }
                Text(if (bn) "আপনি যে পাতায় ছিলেন তার ছবি যুক্ত হয়েছে।" else "A picture of the screen you were on is attached.",
                    style = MaterialTheme.typography.bodySmall, color = Muted)
            } else {
                Text(if (bn) "কোনো ছবি নেই।" else "No picture attached.", style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallPill(if (bn) "📷 ছবি বেছে নিন" else "📷 Choose a photo") { keepShot = true; actions.onPickReportPhoto() }
                if (bitmap != null && keepShot) SmallPill(if (bn) "✗ ছবি বাদ দিন" else "✗ Remove") { keepShot = false }
            }
        }

        AppCard(title = if (bn) "কোন নম্বরে ফোন করব?" else "How can we call you?") {
            AppField(label = if (bn) "নাম" else "Name", value = name, onChange = { name = it.take(80) })
            AppField(label = if (bn) "মোবাইল নম্বর" else "Mobile number", value = phone,
                onChange = { phone = it.filter { c -> c.isDigit() }.take(10); error = null }, keyboard = KeyboardType.Phone)
        }

        error?.let {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DangerBg).padding(12.dp)) {
                Text(it, color = Danger, style = MaterialTheme.typography.bodyMedium)
            }
        }
        BigButton(
            text = if (bn) "📨 পাঠান" else "📨 Send",
            onClick = {
                when {
                    text.trim().length < 3 && !(bitmap != null && keepShot) ->
                        error = if (bn) "সমস্যাটা কয়েক কথায় লিখুন।" else "Please write a few words about the problem."
                    phone.isNotBlank() && phone.length != 10 ->
                        error = if (bn) "সঠিক ১০ সংখ্যার মোবাইল নম্বর দিন।" else "Enter a valid 10-digit mobile number."
                    else -> { keyboard?.hide(); app.sendReport(kind, text, name, phone, keepShot) }
                }
            },
        )
        Text(
            if (bn) "📶 ইন্টারনেট না থাকলেও পাঠানো যাবে: রিপোর্ট ফোনে সেভ থাকবে, নেট এলে নিজে থেকে চলে যাবে।"
            else "📶 Works without internet: the report is saved on your phone and sent automatically when you are online.",
            style = MaterialTheme.typography.bodySmall, color = Muted,
        )
        Spacer(Modifier.height(24.dp))
    }
}

/** Entry card (Profile, help strip): big and friendly. */
@Composable
fun ReportEntryCard(lang: String, pending: Int, onClick: () -> Unit) {
    val bn = lang == "bn"
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(WarnBg).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("🛟", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(if (bn) "কোনো সমস্যা? আমাদের জানান" else "Something wrong? Tell us", style = MaterialTheme.typography.titleMedium, color = WarnInk)
            Text(
                if (pending > 0) (if (bn) "$pending টি রিপোর্ট নেট এলে পাঠানো হবে" else "$pending report(s) will be sent when online")
                else (if (bn) "স্ক্রিনশট সহ রিপোর্ট পাঠান" else "Send a report with a screenshot"),
                style = MaterialTheme.typography.bodySmall, color = WarnInk,
            )
        }
        Text("→", style = MaterialTheme.typography.titleLarge, color = WarnInk)
    }
}

/** Krishi tips written by GMM staff in the admin, shown on top of the Advisory tab. */
@Composable
fun KrishiTipsSection(tips: List<KrishiTip>, app: AppController) {
    if (tips.isEmpty()) return
    val lang = app.language
    AppCard(title = if (lang == "bn") "🌾 GMM-এর নতুন পরামর্শ" else "🌾 Latest advice from GMM") {
        tips.take(5).forEachIndexed { i, t ->
            if (i > 0) androidx.compose.material3.HorizontalDivider(color = PaperLine)
            Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text((if (t.pinned) "📌 " else "") + t.t(lang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (t.crop.isNotBlank()) Text(t.crop, style = MaterialTheme.typography.labelMedium, color = AccentGreen)
                Text(t.b(lang), style = MaterialTheme.typography.bodyMedium)
                val p = app.products.firstOrNull { it.slug == t.productSlug }
                if (p != null) {
                    Text("→ ${p.localizedName(lang)}", style = MaterialTheme.typography.labelLarge, color = AccentGreen,
                        modifier = Modifier.clip(RoundedCornerShape(50)).clickable { app.openProductSlug = p.slug; app.tab = com.gmmdistributors.app.Tab.Shop }.padding(vertical = 6.dp))
                }
            }
        }
    }
}
