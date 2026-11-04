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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gmmdistributors.app.AppController
import com.gmmdistributors.app.R

@Composable
fun ProfileTab(app: AppController, actions: PhoneActions) {
    val lang = app.language
    val bn = lang == "bn"
    var serverUrlInput by rememberSaveable(app.serverUrl) { mutableStateOf(app.serverUrl) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 1. Brand & Customer Header Card
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Forest)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.gmm_logo),
                contentDescription = "GMM Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(2.dp, SunGold, CircleShape),
            )
            Text(
                text = app.siteConfig.businessName,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = if (lang == "bn") app.siteConfig.taglineBn else app.siteConfig.taglineEn,
                color = SunGold,
                style = MaterialTheme.typography.bodySmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
                if (app.siteConfig.supportPhone.isNotBlank()) {
                    SmallPill("📞 ${if (bn) "ফোন করুন" else "Call us"}", gold = true) {
                        actions.onCallSupport(app.siteConfig.supportPhone)
                    }
                }
                if (app.ownerWhatsapp.isNotBlank()) {
                    SmallPill("💬 WhatsApp", whatsapp = true) {
                        actions.onSendEnquiryWhatsApp("Namaste GMM Distributors, I am reaching out from the GMM Organic Android App.")
                    }
                }
            }
        }

        // Account
        val me = app.me
        AppCard(title = if (bn) "আমার অ্যাকাউন্ট" else "My account") {
            if (me == null || me.isGuest) {
                Text(
                    if (bn) "সাইন ইন ছাড়াই অর্ডার করা যায়। অ্যাকাউন্ট খুললে অন্য ফোনেও আপনার অর্ডার দেখতে পাবেন; এই ফোনের অর্ডার সঙ্গে চলে যাবে।"
                    else "You can order without signing in. Create an account to see your orders on any phone; orders from this phone move with you.",
                    style = MaterialTheme.typography.bodyMedium, color = Muted,
                )
                BigButton(text = if (bn) "সাইন ইন" else "Sign in", onClick = { app.requireSignIn() })
                GhostButton(text = if (bn) "নতুন অ্যাকাউন্ট খুলুন" else "Create account", onClick = { app.requireSignIn(register = true) })
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarCircle(me.name)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(me.name, style = MaterialTheme.typography.titleMedium)
                        Text("+91 ${me.phone}", style = MaterialTheme.typography.bodySmall, color = Muted)
                        if (me.email.isNotBlank()) Text(me.email, style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = AccentGreen,
                        modifier = Modifier.size(22.dp).clickable { app.companyPage = "account" })
                }
                GhostButton(text = if (bn) "সাইন আউট" else "Sign out", onClick = { app.logout() })
            }
        }

        // 2. Language & Theme Card
        AppCard(title = t("Appearance & Language")) {
            Text(if (lang == "bn") "ভাষা (Language)" else "Language", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("en" to "English", "bn" to "বাংলা (Bengali)").forEach { (code, label) ->
                    val on = app.language == code
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (on) Leaf else Paper)
                            .border(1.dp, if (on) Leaf else PaperLine, RoundedCornerShape(50))
                            .clickable { app.setLanguageCode(code) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, color = if (on) Color.White else TextMain, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Text(if (lang == "bn") "থিম (Theme)" else "Theme", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("light" to "Light", "dark" to "Dark", "system" to "System").forEach { (mode, label) ->
                    val on = app.theme == mode
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (on) Leaf else Paper)
                            .border(1.dp, if (on) Leaf else PaperLine, RoundedCornerShape(50))
                            .clickable { app.setThemeMode(mode) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, color = if (on) Color.White else TextMain, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // 3. Saved Delivery Addresses
        if (app.signedIn) AppCard(
            title = t("Saved Delivery Addresses"),
            trailing = {
                SmallPill("+ Add") {
                    app.editingAddress = null
                    app.addingAddress = true
                }
            }
        ) {
            if (app.addresses.isEmpty()) {
                Text(
                    text = if (lang == "bn") "কোনো ঠিকানা সেভ করা নেই।" else "No delivery addresses saved yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
            } else {
                app.addresses.forEach { addr ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Paper)
                            .border(1.dp, PaperLine, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("${addr.fullName} · ${addr.phone}", style = MaterialTheme.typography.titleMedium)
                            Text(addr.formattedLine(), style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = AccentGreen,
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

        // 4. Developer settings (debug builds only; release builds always use the live server)
        if (com.gmmdistributors.app.BuildConfig.DEBUG) {
            AppCard(title = "Developer: server") {
                AppField(
                    label = "Django server URL",
                    value = serverUrlInput,
                    onChange = { serverUrlInput = it.trim() },
                    hint = "e.g. http://192.168.1.5:8000 (run: python manage.py runserver 0.0.0.0:8000)",
                )
                BigButton(text = "Save & reload", onClick = { app.updateServerUrl(serverUrlInput) })
            }
        }

        // 5. Native Company & Leadership Pages (No external browser needed, matching Bas1Minute's AboutScreens)
        AppCard(title = t("About GMM Distributors")) {
            CompanyLinkRow(
                title = if (lang == "bn") "ভিশন, মিশন ও জৈব মূল্যবোধ" else "Vision, Mission & Organic Pledge",
                subtitle = if (lang == "bn") "১০০% জৈব কৃষি ও রাসায়নিকমুক্ত চাষাবাদের অঙ্গীকার" else "Why GMM sells strictly 100% organic fertilizers & bio-pesticides",
                onClick = { app.companyPage = "vision" },
            )
            HorizontalDivider(color = PaperLine)
            CompanyLinkRow(
                title = if (lang == "bn") "পরিচালনা পর্ষদ (Board of Directors)" else "Leadership & Board of Directors",
                subtitle = "Chikur Kumar Ray (Founder & Director) · Bina Debnath (Director)",
                onClick = { app.companyPage = "leadership" },
            )
            HorizontalDivider(color = PaperLine)
            CompanyLinkRow(
                title = if (lang == "bn") "ডিজিটাল ভিজিটিং কার্ড" else "Official Digital Visiting Card",
                subtitle = "Front & Back Visiting Card of Chikur Kumar Ray",
                onClick = { app.companyPage = "visiting_card" },
            )
            HorizontalDivider(color = PaperLine)
            CompanyLinkRow(
                title = if (lang == "bn") "যোগাযোগ ও রেজিস্টার্ড অফিস" else "Contact & Registered Office",
                subtitle = "Nirmala Bhawan, Arambagh, Hooghly – 712601 · CIN: U24299WB2020PTC240755",
                onClick = { app.companyPage = "contact" },
            )
        }

        ReportEntryCard(lang = lang, pending = app.pendingReports) { actions.onReportProblem("profile", "") }

        DeveloperCreditCard(lang = lang, onEmail = { actions.onEmail(DEVELOPER_EMAIL, "GMM Organic app v${com.gmmdistributors.app.BuildConfig.VERSION_NAME}") })

        Spacer(Modifier.height(24.dp))
    }
}

const val DEVELOPER_NAME = "Sourav"
const val DEVELOPER_EMAIL = "tosourav6@gmail.com"

/** Highlighted maker's credit: gold-ringed monogram, name, role, version and a one-tap email button. */
@Composable
fun DeveloperCreditCard(lang: String, onEmail: () -> Unit) {
    val bn = lang == "bn"
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(Forest, Color(0xFF1B5A34))))
            .border(1.5.dp, SunGold.copy(alpha = 0.55f), RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(56.dp).clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(SunGold, Color(0xFFE0701A))))
                    .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("S", style = MaterialTheme.typography.headlineMedium, color = SunInk, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (bn) "অ্যাপ ডিজাইন ও ডেভেলপমেন্ট" else "DESIGNED & DEVELOPED BY",
                    style = MaterialTheme.typography.labelMedium, color = SunGold, letterSpacing = 1.2.sp,
                )
                Text(DEVELOPER_NAME, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text(
                    if (bn) "অ্যান্ড্রয়েড অ্যাপ · ওয়েবসাইট · সার্ভার" else "Android app · Website · Server",
                    style = MaterialTheme.typography.bodySmall, color = MutedOnDark,
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.12f))
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                .clickable(onClick = onEmail).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("✉️", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(DEVELOPER_EMAIL, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1)
                Text(if (bn) "মতামত বা সমস্যা জানাতে ইমেল করুন" else "Email for feedback or to report a problem",
                    style = MaterialTheme.typography.bodySmall, color = MutedOnDark, maxLines = 1)
            }
            Text("→", style = MaterialTheme.typography.titleLarge, color = SunGold)
        }
        Text(
            "GMM Organic v${com.gmmdistributors.app.BuildConfig.VERSION_NAME} · © 2026 GMM Distributors Pvt. Ltd.",
            style = MaterialTheme.typography.bodySmall, color = MutedOnDark.copy(alpha = 0.8f),
            modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun CompanyLinkRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Text("›", style = MaterialTheme.typography.headlineMedium, color = AccentGreen)
    }
}

@Composable
fun CompanyPageScreen(
    page: String,
    app: AppController,
    actions: PhoneActions,
    onBack: () -> Unit,
) {
    val lang = app.language
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
            Text(if (lang == "bn") "প্রোফাইলে ফিরে যান" else "Back to Profile", style = MaterialTheme.typography.labelLarge)
        }

        when (page) {
            "vision" -> {
                AppCard(title = "Vision, Mission & 100% Organic Pledge") {
                    Text(
                        "Responsible agriculture begins with living soil, 100% organic inputs, and zero synthetic chemicals.",
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentGreen,
                    )
                    Text(
                        "Our Vision: To build a trusted organic agricultural products business from Arambagh, Hooghly by providing 100% chemical-free soil boosters, humic & seaweed conditioners, bio-manures, and biological pest shields for farmers.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        "Why Zero Chemicals? Synthetic chemical fertilizers and pesticides harden farm soil, kill earthworms, and leave toxic residues in food. Every product sold in the GMM app is strictly organic, biological, or botanical.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
            }

            "leadership" -> {
                AppCard(title = t("Board of Directors")) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.director_chikur_ray),
                            contentDescription = "Chikur Kumar Ray",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(76.dp).clip(CircleShape).border(2.dp, SunGold, CircleShape),
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("Chikur Kumar Ray", style = MaterialTheme.typography.titleLarge)
                            Text("Founder & Director", style = MaterialTheme.typography.bodyMedium, color = AccentGreen)
                            Text("Phone / WhatsApp: +91 90837 32101", style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                    }
                    HorizontalDivider(color = PaperLine)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.director_bina_debnath),
                            contentDescription = "Bina Debnath",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(76.dp).clip(CircleShape).border(2.dp, SunGold, CircleShape),
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("Bina Debnath", style = MaterialTheme.typography.titleLarge)
                            Text("Director", style = MaterialTheme.typography.bodyMedium, color = AccentGreen)
                            Text("GMM Distributors Private Limited", style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                    }
                }
                AppCard(title = "Company at a Glance") {
                    Text("Company: GMM Distributors Private Limited", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("CIN: U24299WB2020PTC240755", style = MaterialTheme.typography.bodyMedium)
                    Text("Incorporated: 23 October 2020 (RoC Kolkata)", style = MaterialTheme.typography.bodyMedium)
                    Text("Registered Office: Nirmala Bhawan, Fansi Bagan, Ward No. 13, Arambagh, Hooghly, West Bengal – 712601", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }

            "visiting_card" -> {
                AppCard(title = "Digital Visiting Card — Chikur Kumar Ray") {
                    Image(
                        painter = painterResource(id = R.drawable.visiting_card_front),
                        contentDescription = "Visiting Card Front",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).border(1.dp, PaperLine, RoundedCornerShape(14.dp)),
                    )
                    Image(
                        painter = painterResource(id = R.drawable.visiting_card_back),
                        contentDescription = "Visiting Card Back",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).border(1.dp, PaperLine, RoundedCornerShape(14.dp)),
                    )
                    BigButton(
                        text = "💬 Chat with Chikur Kumar Ray on WhatsApp",
                        whatsapp = true,
                        onClick = {
                            actions.onSendEnquiryWhatsApp("Namaste Chikur Ji, I viewed your Digital Visiting Card on the GMM Organic App.")
                        },
                    )
                }
            }

            else -> {
                AppCard(title = "Contact GMM Distributors") {
                    Text("Founder & Director: Chikur Kumar Ray", style = MaterialTheme.typography.titleMedium)
                    Text("Phone & WhatsApp: +91 90837 32101", style = MaterialTheme.typography.bodyLarge, color = AccentGreen)
                    Text("Email: ray.chikur@gmail.com", style = MaterialTheme.typography.bodyMedium)
                    Text("Registered Office: Nirmala Bhawan, Fansi Bagan, Ward No. 13, Arambagh, Hooghly, West Bengal – 712601", style = MaterialTheme.typography.bodyMedium, color = Muted)
                    BigButton(
                        text = "📞 Call +91 90837 32101",
                        gold = true,
                        onClick = { actions.onCallSupport("9083732101") },
                    )
                    BigButton(
                        text = "💬 Send Dealership / Bulk Order Enquiry on WhatsApp",
                        whatsapp = true,
                        onClick = {
                            actions.onSendEnquiryWhatsApp("Namaste GMM Distributors, I would like to enquire about dealership / bulk order of your organic products.")
                        },
                    )
                }
            }
        }
    }
}


/** Edit name / email and delete the account (Google Play requires in-app account deletion). */
@Composable
fun AccountScreen(app: AppController, onBack: () -> Unit) {
    val bn = app.language == "bn"
    val me = app.me
    var name by rememberSaveable { mutableStateOf(me?.name ?: "") }
    var email by rememberSaveable { mutableStateOf(me?.email ?: "") }
    var confirmDelete by remember { mutableStateOf(false) }
    var deleteText by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().background(Paper).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            Modifier.clip(RoundedCornerShape(50)).background(CardBg).border(1.dp, PaperLine, RoundedCornerShape(50))
                .clickable(onClick = onBack).padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (bn) "ফিরে যান" else "Back", style = MaterialTheme.typography.labelLarge)
        }
        AppCard(title = if (bn) "আমার তথ্য" else "My details") {
            AppField(label = if (bn) "নাম" else "Name", value = name, onChange = { name = it.take(80) })
            AppField(label = if (bn) "ইমেল (ঐচ্ছিক)" else "Email (optional)", value = email, onChange = { email = it.trim().take(120) },
                keyboard = KeyboardType.Email)
            Text(
                if (bn) "মোবাইল নম্বর বদলাতে আমাদের ফোন করুন।" else "To change your mobile number, please call us.",
                style = MaterialTheme.typography.bodySmall, color = Muted,
            )
            BigButton(
                text = if (bn) "সেভ করুন" else "Save",
                enabled = !app.busy && name.isNotBlank(),
                onClick = { app.updateProfile(name.trim(), email) },
            )
        }
        AppCard(title = if (bn) "অ্যাকাউন্ট মুছে ফেলুন" else "Delete account") {
            Text(
                if (bn) "আপনার নাম, ঠিকানা ও লগইন মুছে যাবে। আইনি কারণে পুরনো অর্ডার ও GST ইনভয়েস নাম ছাড়া রাখা থাকবে।"
                else "Your name, addresses and sign-in are removed. Past orders and GST invoices are kept without your name, as the law requires.",
                style = MaterialTheme.typography.bodySmall, color = Muted,
            )
            GhostButton(text = if (bn) "অ্যাকাউন্ট মুছুন" else "Delete my account", danger = true, onClick = { confirmDelete = true })
        }
        Spacer(Modifier.height(24.dp))
    }

    if (confirmDelete) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(if (bn) "অ্যাকাউন্ট মুছবেন?" else "Delete your account?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (bn) "নিশ্চিত করতে DELETE লিখুন।" else "Type DELETE to confirm.")
                    AppField(label = "DELETE", value = deleteText, onChange = { deleteText = it.trim().uppercase().take(6) })
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    enabled = deleteText == "DELETE" && !app.busy,
                    onClick = { confirmDelete = false; app.deleteAccount() },
                ) { Text(if (bn) "মুছে ফেলুন" else "Delete", color = Danger, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirmDelete = false }) { Text(if (bn) "না" else "Cancel") }
            },
        )
    }
}
