package com.gmmdistributors.app.ui

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmmdistributors.app.AppController
import com.gmmdistributors.app.AdvisoryContent
import com.gmmdistributors.app.Tab
import java.time.LocalDate

private val MONTHS_BN = listOf("জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে")
private val MONTHS_EN = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

@Composable
fun AdvisoryTab(app: AppController, actions: PhoneActions) {
    val lang = app.language
    val currentMonth = remember { LocalDate.now().monthValue }
    val tipIndex = remember { LocalDate.now().dayOfYear % AdvisoryContent.DAILY_ORGANIC_TIPS_EN.size }
    val dailyTip = if (lang == "bn") AdvisoryContent.DAILY_ORGANIC_TIPS_BN[tipIndex] else AdvisoryContent.DAILY_ORGANIC_TIPS_EN[tipIndex]

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 1. Krishi Paramarsha Hero
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(listOf(Forest, Leaf)))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OrganicBadge(text = if (lang == "bn") "🌾 কৃষি পরামর্শ · ১০০% জৈব চাষাবাদ" else "🌾 Krishi Paramarsha · 100% Organic Farming")
            Text(
                text = if (lang == "bn") "ফসল অনুযায়ী জৈব সার ও জৈব কীটনাশক নির্দেশিকা"
                else "Crop-Wise Organic Fertilizer & Bio-Pesticide Advisory",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = if (lang == "bn") "ধান, আলু, বেগুন, সরিষা ও পাটের মরসুম, বীজ শোধন এবং রাসায়নিকমুক্ত রোগ-পোকা নিয়ন্ত্রণের সহজ গাইড।"
                else "Practical guides for Paddy, Potato, Brinjal, Mustard, and Jute in West Bengal — zero synthetic chemicals.",
                color = MutedOnDark,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        // Advice written by GMM staff in the admin (updates with the catalogue, readable offline)
        KrishiTipsSection(app.tips, app)

        // 2. Daily Organic Tip Card
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(WarnBg)
                .border(1.dp, SunGold, RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = if (lang == "bn") "💡 আজকের জৈব কৃষি টিপস" else "💡 Today's Organic Farming Tip",
                style = MaterialTheme.typography.titleMedium,
                color = WarnInk,
            )
            Text(dailyTip, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF3A2A05))
        }

        // 4. Crop Guides List
        Text(
            text = if (lang == "bn") "ফসল বেছে নিন (৫টি প্রধান ফসল)" else "Select a Crop Guide (5 Major Crops)",
            style = MaterialTheme.typography.headlineMedium,
        )

        AdvisoryContent.CROP_GUIDES.forEach { guide ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBg)
                    .border(1.dp, PaperLine, RoundedCornerShape(20.dp))
                    .clickable { app.openCropSlug = guide.slug }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DrawableImage(
                    drawableName = guide.drawableName,
                    contentDescription = guide.nameEn,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, PaperLine, RoundedCornerShape(16.dp)),
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (lang == "bn") "${guide.nameBn} (${guide.nameEn})" else "${guide.nameEn} (${guide.nameBn})",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    OrganicBadge(text = if (lang == "bn") guide.seasonBn else guide.seasonEn)
                    Text(
                        text = if (lang == "bn") guide.taglineBn else guide.taglineEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
                Text("→", style = MaterialTheme.typography.headlineMedium, color = AccentGreen)
            }
        }

        // 5. 12-Month Crop Calendar Card (from gmm/templates/advisory/index.html)
        AppCard(title = if (lang == "bn") "📅 ফসলের বারোমাসি ক্যালেন্ডার" else "📅 12-Month Organic Crop Calendar") {
            Text(
                text = if (lang == "bn") "হলুদ = বপন/রোপণ · হালকা সবুজ = পরিচর্যা · গাঢ় সবুজ = ফসল কাটা"
                else "Gold = Sowing/Planting · Light Green = Growth & Organic Care · Dark Green = Harvest",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Header row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (lang == "bn") "ফসল" else "Crop",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.width(90.dp),
                    )
                    (1..12).forEach { m ->
                        val isNow = m == currentMonth
                        Box(
                            Modifier
                                .width(42.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isNow) SunGold.copy(alpha = 0.25f) else Color.Transparent)
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (lang == "bn") MONTHS_BN[m - 1] else MONTHS_EN[m - 1],
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                color = if (isNow) Forest else Muted,
                            )
                        }
                    }
                }
                AdvisoryContent.CROP_GUIDES.forEach { g ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (lang == "bn") g.nameBn else g.nameEn.substringBefore(" "),
                            style = MaterialTheme.typography.labelMedium,
                            color = AccentGreen,
                            modifier = Modifier
                                .width(90.dp)
                                .clickable { app.openCropSlug = g.slug },
                        )
                        (1..12).forEach { m ->
                            val cellColor = when (m) {
                                in g.sowMonths -> SunGold
                                in g.harvestMonths -> Leaf
                                in g.growMonths -> MintLite
                                else -> Paper2
                            }
                            Box(
                                Modifier
                                    .width(42.dp)
                                    .padding(horizontal = 2.dp)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(cellColor)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CropDetailScreen(
    guide: AdvisoryContent.CropGuide,
    app: AppController,
    actions: PhoneActions,
    onBack: () -> Unit,
) {
    val lang = app.language
    val recommendedProducts = remember(guide.slug, app.products) {
        app.products.filter { it.slug in guide.recommendedProductSlugs }
    }

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
            Text(if (lang == "bn") "কৃষি পরামর্শে ফিরে যান" else "Back to Crop Advisory", style = MaterialTheme.typography.labelLarge)
        }

        // Crop Hero
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Forest)
        ) {
            DrawableImage(
                drawableName = guide.drawableName,
                contentDescription = guide.nameEn,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(210.dp),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC081C10))
                        )
                    )
                    .padding(18.dp),
                contentAlignment = Alignment.BottomStart,
            ) {
                Column {
                    OrganicBadge(text = if (lang == "bn") guide.seasonBn else guide.seasonEn)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${guide.nameBn} · ${guide.nameEn}",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Text(
                        text = if (lang == "bn") guide.taglineBn else guide.taglineEn,
                        color = MutedOnDark,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        // Quick Facts Grid
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            FactMiniCard(
                label = if (lang == "bn") "মেয়াদ" else "Duration",
                value = if (lang == "bn") guide.durationBn else guide.durationEn,
                modifier = Modifier.weight(1f),
            )
            FactMiniCard(
                label = if (lang == "bn") "বীজের হার" else "Seed Rate",
                value = if (lang == "bn") guide.seedBn else guide.seedEn,
                modifier = Modifier.weight(1f),
            )
        }

        // Organic Fertilizer Schedule
        AppCard(title = if (lang == "bn") "🌱 ১০০% জৈব সার ব্যবস্থাপনা" else "🌱 100% Organic Nutrition Schedule") {
            Text(
                text = if (lang == "bn") guide.organicFertPlanBn else guide.organicFertPlanEn,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        // Cultivation Stages
        AppCard(title = if (lang == "bn") "📋 ধাপে ধাপে জৈব পরিচর্যা" else "📋 Step-by-Step Organic Cultivation") {
            val stages = if (lang == "bn") guide.stages else guide.stagesEn
            stages.forEachIndexed { idx, (title, desc) ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier.size(30.dp).clip(CircleShape).background(Leaf),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${idx + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleMedium, color = AccentGreen)
                        Text(desc, style = MaterialTheme.typography.bodyMedium, color = Muted)
                    }
                }
            }
        }

        // Organic Bio-Pesticide Remedies
        AppCard(title = if (lang == "bn") "🛡️ জৈব উপায়ে রোগ ও পোকা দমন (রাসায়নিক ছাড়া)" else "🛡️ Organic Bio-Pesticide Remedies (Zero Chemicals)") {
            val pests = if (lang == "bn") guide.organicPests else guide.organicPestsEn
            pests.forEachIndexed { idx, (pestName, symptom, remedy) ->
                if (idx > 0) HorizontalDivider(color = PaperLine)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("🐞 $pestName", style = MaterialTheme.typography.titleMedium, color = Danger)
                    Text(
                        text = "${if (lang == "bn") "লক্ষণ" else "Symptom"}: $symptom",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                    Text(
                        text = "${if (lang == "bn") "জৈব সমাধান" else "Organic Remedy"}: $remedy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // Recommended GMM Products for this Crop (1-tap jump to product or add to cart)
        AppCard(title = if (lang == "bn") "🛒 এই ফসলের জন্য প্রস্তাবিত GMM জৈব পণ্য" else "🛒 Recommended GMM Organic Inputs for ${guide.nameEn}") {
            recommendedProducts.forEach { prod ->
                val defVar = prod.defaultVariant ?: prod.variants.first()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Paper)
                        .border(1.dp, PaperLine, RoundedCornerShape(14.dp))
                        .clickable {
                            app.openProductSlug = prod.slug
                            app.tab = Tab.Shop
                        }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProductImage(
                        product = prod,
                        modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)),
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(prod.localizedName(lang), style = MaterialTheme.typography.titleMedium)
                        Text("${defVar.localizedLabel(lang)} · ${formatRupees(defVar.price)}", style = MaterialTheme.typography.bodySmall, color = AccentGreen)
                    }
                    SmallPill(if (lang == "bn") "+ কার্ট" else "+ Cart", gold = true) {
                        app.setVariantQty(defVar.id, (app.cartQuantities[defVar.id] ?: 0) + 1)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FactMiniCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, PaperLine, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Muted)
        Text(value, style = MaterialTheme.typography.titleMedium, color = TextMain)
    }
}

