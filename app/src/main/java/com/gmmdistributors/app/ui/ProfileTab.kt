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
    var serverUrlInput by rememberSaveable(app.serverUrl) { mutableStateOf(app.serverUrl) }
    var ownerWaInput by rememberSaveable(app.prefs.ownerWhatsapp) { mutableStateOf(app.prefs.ownerWhatsapp) }
    var useDummy by rememberSaveable(app.useDummyApi) { mutableStateOf(app.useDummyApi) }

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
                SmallPill("📞 Call ${app.siteConfig.supportPhone}", gold = true) {
                    actions.onCallSupport(app.siteConfig.supportPhone)
                }
                SmallPill("💬 WhatsApp Owner", whatsapp = true) {
                    actions.onSendEnquiryWhatsApp("Namaste GMM Distributors, I am reaching out from the GMM Organic Android App.")
                }
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
        AppCard(
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

        // 4. Backend & Dummy API Switcher (Built for seamless transition to the Python Django backend)
        AppCard(title = t("Backend & API Mode (Django Ready)")) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (useDummy) "Built-in Dummy API Mode (Active)" else "Live Python Django Backend Mode",
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentGreen,
                    )
                    Text(
                        text = "Currently serving 10 fixed 100% organic products & orders from in-app DummyServer + SQLite. Switch off when your Django `/api/v1/` backend is deployed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = useDummy,
                    colors = SwitchDefaults.colors(checkedTrackColor = Leaf, checkedThumbColor = SunGold),
                    onCheckedChange = { on ->
                        useDummy = on
                        app.updateBackendSettings(on, serverUrlInput, ownerWaInput)
                    },
                )
            }

            AppField(
                label = "Owner WhatsApp Number (with country code 91)",
                value = ownerWaInput,
                onChange = { ownerWaInput = it.filter { c -> c.isDigit() }.take(12) },
                keyboard = KeyboardType.Phone,
                hint = "Default: 919083732101 (Chikur Kumar Ray)",
            )

            AppField(
                label = "Python Django Server URL (for Phase 2)",
                value = serverUrlInput,
                onChange = { serverUrlInput = it.trim() },
                hint = "e.g., https://gmmdistributors.xyz or http://192.168.1.5:8000",
            )

            BigButton(
                text = "Save Backend & WhatsApp Settings",
                onClick = { app.updateBackendSettings(useDummy, serverUrlInput, ownerWaInput) },
            )
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

        Spacer(Modifier.height(24.dp))
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
