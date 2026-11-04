package com.gmmdistributors.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmmdistributors.app.AppController
import com.gmmdistributors.app.R
import com.gmmdistributors.app.Tab
import com.gmmdistributors.app.api.Order

data class PhoneActions(
    val onSendOrderWhatsApp: (Order) -> Unit,
    val onPayUpi: (Order) -> Unit,
    val onShareInvoicePdf: (com.gmmdistributors.app.api.Invoice) -> Unit,
    val onSendEnquiryWhatsApp: (String) -> Unit,
    val onCallSupport: (String) -> Unit,
    val onCopyText: (String, String) -> Unit,
    val onSendSms: (String, String) -> Unit = { _, _ -> },
    val onEmail: (String, String) -> Unit = { _, _ -> },
    /** (screen name, order no): captures the current screen, then opens the report form. */
    val onReportProblem: (String, String) -> Unit = { _, _ -> },
    val onPickReportPhoto: () -> Unit = {},
    val onUpdateApp: () -> Unit = {},
)

private data class NavTabItem(val tab: Tab, val label: String, val icon: ImageVector)

private val NAV_TABS = listOf(
    NavTabItem(Tab.Shop, "Shop", Icons.Filled.ShoppingBag),
    NavTabItem(Tab.Advisory, "Advisory", Icons.Filled.Eco),
    NavTabItem(Tab.Cart, "Cart", Icons.Filled.ShoppingCart),
    NavTabItem(Tab.Orders, "Orders", Icons.Filled.Inventory2),
    NavTabItem(Tab.Profile, "Profile", Icons.Filled.Person),
)

/**
 * Top Brand Bar + Floating Cart Bar + 5-Tab Bottom Navigation Bar + Cross-Fade Screen Transitions.
 * Modeled directly on `bas1minute-android`'s `MainShell.kt` and `gmm`'s `templates/base.html`.
 */
@Composable
fun MainShell(app: AppController, actions: PhoneActions) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(app.message) {
        app.message?.let {
            snackbar.showSnackbar(it)
            app.message = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            Column {
                GmmTopBar(app = app, onCall = { actions.onCallSupport(app.siteConfig.supportPhone) })
                if (app.offline && app.products.isNotEmpty()) {
                    Text(
                        text = if (app.language == "bn") "📶 অফলাইন · সব দেখা ও অর্ডার করা যাবে, নেট এলে পাঠানো হবে" else "📶 Offline · browse and order; we send it when you're online",
                        color = SunInk,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.fillMaxWidth().background(SunGold).clickable { app.refresh() }.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }
            }
        },
        bottomBar = {
            Column {
                // Floating quick-cart bar on Shop & Advisory screens when cart has items
                if (app.cartCount > 0 && (app.tab == Tab.Shop || app.tab == Tab.Advisory) && app.openProductSlug == null) {
                    FloatingCartBar(
                        itemCount = app.cartCount,
                        subtotal = app.cartSubtotal,
                        lang = app.language,
                        onClick = { app.tab = Tab.Cart },
                    )
                }
                NavigationBar(containerColor = Forest) {
                    NAV_TABS.forEach { item ->
                        val isSelected = app.tab == item.tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                app.newlyPlacedOrder = null
                                app.queuedOrder = null
                                app.authOpen = false
                                if (item.tab == Tab.Shop) app.openProductSlug = null
                                if (item.tab == Tab.Advisory) { app.openCropSlug = null }
                                if (item.tab == Tab.Orders) { app.openOrderNo = null; app.invoiceOrderNo = null }
                                if (item.tab == Tab.Profile) { app.companyPage = null; app.addingAddress = false; app.editingAddress = null }
                                app.tab = item.tab
                            },
                            icon = {
                                if (item.tab == Tab.Cart && app.cartCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            val bump = remember { androidx.compose.animation.core.Animatable(1f) }
                                            LaunchedEffect(app.cartBump) {
                                                if (app.cartBump > 0) {
                                                    bump.animateTo(1.45f, androidx.compose.animation.core.tween(120))
                                                    bump.animateTo(1f, androidx.compose.animation.core.spring(dampingRatio = 0.4f))
                                                }
                                            }
                                            Badge(containerColor = SunGold, contentColor = SunInk, modifier = Modifier.scale(bump.value)) {
                                                Text(app.cartCount.toString(), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    ) {
                                        Icon(item.icon, contentDescription = null)
                                    }
                                } else {
                                    Icon(item.icon, contentDescription = null)
                                }
                            },
                            label = { Text(t(item.label), style = MaterialTheme.typography.labelMedium, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Forest,
                                selectedTextColor = SunGold,
                                indicatorColor = SunGold,
                                unselectedIconColor = MutedOnDark,
                                unselectedTextColor = MutedOnDark,
                            ),
                        )
                    }
                }
            }
        },
        containerColor = Paper,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        PriceReviewDialog(app)
        PackPickerSheet(app)
        if (app.reportOpen) {
            BackHandler { app.closeReport() }
        }
        val placed = app.newlyPlacedOrder
        val queued = app.queuedOrder
        if (app.reportOpen) {
            Box(Modifier.fillMaxSize().padding(padding)) { ReportProblemScreen(app, actions) }
        } else if (queued != null) {
            BackHandler { app.queuedOrder = null }
            Box(Modifier.fillMaxSize().padding(padding)) {
                QueuedOrderScreen(
                    order = queued,
                    lang = app.language,
                    canSms = app.siteConfig.supportPhone.isNotBlank(),
                    onSms = { actions.onSendSms(app.siteConfig.supportPhone, app.buildSmsOrder(queued)) },
                    onDone = { app.queuedOrder = null; app.tab = Tab.Orders },
                )
            }
        } else if (placed != null) {
            BackHandler { app.newlyPlacedOrder = null }
            Box(Modifier.fillMaxSize().padding(padding)) {
                OrderSuccessScreen(
                    order = placed,
                    lang = app.language,
                    onPrimary = { app.newlyPlacedOrder = null; app.tab = Tab.Orders; app.openOrderNo = placed.orderNo },
                    onWhatsApp = if (app.ownerWhatsapp.isNotBlank()) ({ actions.onSendOrderWhatsApp(placed) }) else null,
                    onContinue = { app.newlyPlacedOrder = null; app.openOrderNo = null; app.tab = Tab.Shop },
                )
            }
        } else if (app.authOpen) {
            BackHandler { app.authOpen = false }
            Box(Modifier.fillMaxSize().padding(padding)) {
                AuthScreen(app = app, onBack = { app.authOpen = false })
            }
        } else {

        val screenKey = "${app.tab}:${app.openProductSlug}:${app.openCropSlug}:${app.openOrderNo}:${app.invoiceOrderNo}:${app.companyPage}:${app.addingAddress}"
        AnimatedContent(
            targetState = screenKey,
            transitionSpec = {
                fadeIn(tween(200, delayMillis = 40)) togetherWith fadeOut(tween(110))
            },
            label = "gmm_screens",
            modifier = Modifier.fillMaxSize().padding(padding),
        ) { _ ->
            Box(Modifier.fillMaxSize()) {
                if (app.needsUpdate) {
                    StatusNotice(
                        title = if (app.language == "bn") "নতুন সংস্করণ এসেছে" else "A new version is ready",
                        body = if (app.language == "bn") "অ্যাপটি চালাতে নতুন সংস্করণে আপডেট করুন। আপনার কার্ট ও অর্ডার ঠিক থাকবে।"
                        else "Please update to keep using the GMM app. Your cart and orders stay safe.",
                        button = if (app.language == "bn") "এখনই আপডেট করুন" else "Update now",
                        onClick = { actions.onUpdateApp() },
                    )
                } else if (app.products.isEmpty() && (app.tab == Tab.Shop || app.tab == Tab.Cart)) {
                    if (!app.catalogLoaded || app.refreshing) {
                        HomeSkeleton()
                    } else {
                        StatusNotice(
                            title = if (app.language == "bn") "সংযোগ নেই" else "Can't reach GMM",
                            body = if (app.language == "bn") "ইন্টারনেট চালু করে আবার চেষ্টা করুন।" else "Check your internet connection and try again.",
                            button = if (app.language == "bn") "আবার চেষ্টা করুন" else "Try again",
                            onClick = { app.refresh() },
                        )
                    }
                } else {
                when (app.tab) {
                    Tab.Shop -> {
                        val openProd = app.openProductSlug?.let { slug -> app.products.firstOrNull { it.slug == slug } }
                        if (openProd != null) {
                            BackHandler { app.openProductSlug = null }
                            ProductDetailScreen(
                                product = openProd,
                                app = app,
                                actions = actions,
                                onBack = { app.openProductSlug = null },
                            )
                        } else {
                            ShopTab(app = app, actions = actions)
                        }
                    }

                    Tab.Advisory -> {
                        when {
                            app.openCropSlug != null -> {
                                val guide = com.gmmdistributors.app.AdvisoryContent.CROP_GUIDES.firstOrNull { it.slug == app.openCropSlug }
                                if (guide != null) {
                                    BackHandler { app.openCropSlug = null }
                                    CropDetailScreen(
                                        guide = guide,
                                        app = app,
                                        actions = actions,
                                        onBack = { app.openCropSlug = null },
                                    )
                                } else {
                                    AdvisoryTab(app = app, actions = actions)
                                }
                            }
                            else -> AdvisoryTab(app = app, actions = actions)
                        }
                    }

                    Tab.Cart -> {
                        if (app.addingAddress || app.editingAddress != null) {
                            BackHandler { app.addingAddress = false; app.editingAddress = null }
                            AddressFormScreen(
                                app = app,
                                existing = app.editingAddress,
                                onBack = { app.addingAddress = false; app.editingAddress = null },
                            )
                        } else {
                            CartCheckoutTab(app = app, actions = actions)
                        }
                    }

                    Tab.Orders -> {
                        val detailOrder = app.openOrderNo?.let { no -> app.orders.firstOrNull { it.orderNo == no } }
                        when {
                            app.invoiceOrderNo != null -> {
                                BackHandler { app.invoiceOrderNo = null }
                                InvoiceScreen(
                                    app = app,
                                    actions = actions,
                                    onBack = { app.invoiceOrderNo = null },
                                )
                            }
                            detailOrder != null -> {
                                BackHandler { app.openOrderNo = null }
                                OrderDetailScreen(
                                    order = detailOrder,
                                    app = app,
                                    actions = actions,
                                    onBack = { app.openOrderNo = null },
                                )
                            }
                            else -> OrdersTab(app = app, actions = actions)
                        }
                    }

                    Tab.Profile -> {
                        when {
                            app.addingAddress || app.editingAddress != null -> {
                                BackHandler { app.addingAddress = false; app.editingAddress = null }
                                AddressFormScreen(
                                    app = app,
                                    existing = app.editingAddress,
                                    onBack = { app.addingAddress = false; app.editingAddress = null },
                                )
                            }
                            app.companyPage == "account" && app.me != null -> {
                                BackHandler { app.companyPage = null }
                                AccountScreen(app = app, onBack = { app.companyPage = null })
                            }
                            app.companyPage != null -> {
                                BackHandler { app.companyPage = null }
                                CompanyPageScreen(
                                    page = app.companyPage!!,
                                    app = app,
                                    actions = actions,
                                    onBack = { app.companyPage = null },
                                )
                            }
                            else -> ProfileTab(app = app, actions = actions)
                        }
                    }
                }
                }
            }
        }
    }
    }
}

@Composable
private fun GmmTopBar(app: AppController, onCall: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Forest)
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.gmm_logo),
            contentDescription = "GMM Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, SunGold, CircleShape)
                .clickable {
                    app.openProductSlug = null
                    app.tab = Tab.Shop
                },
        )
        Spacer(Modifier.width(10.dp))
        Column(
            Modifier
                .weight(1f)
                .clickable {
                    app.openProductSlug = null
                    app.tab = Tab.Shop
                }
        ) {
            Text(
                "GMM Distributors",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                t("100% Organic · Zero Chemicals"),
                color = SunGold,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Theme toggle button
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Forest2)
                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                .clickable {
                    app.setThemeMode(if (ThemeState.dark) "light" else "dark")
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (ThemeState.dark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = "Switch Theme",
                tint = SunGold,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(Modifier.width(8.dp))

        // Language toggle (বাংলা / EN)
        Box(
            Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(50))
                .background(Forest2)
                .border(1.dp, SunGold, RoundedCornerShape(50))
                .clickable { app.toggleLanguage() }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (app.language == "bn") "English" else "বাংলা",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
            )
        }

        Spacer(Modifier.width(8.dp))

        // One-tap Call Owner button
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SunGold)
                .clickable(onClick = onCall),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = t("Call Us"),
                tint = SunInk,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun FloatingCartBar(
    itemCount: Int,
    subtotal: Double,
    lang: String,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Forest)
            .border(1.5.dp, SunGold, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = if (lang == "bn") "$itemCount টি জৈব প্যাক কার্টে আছে" else "$itemCount organic pack${if (itemCount == 1) "" else "s"} in cart",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = if (lang == "bn") "মোট: ${formatRupees(subtotal)} (GST সহ)" else "Subtotal: ${formatRupees(subtotal)} (incl. GST)",
                color = MutedOnDark,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SunGold)
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Text(
                text = if (lang == "bn") "কার্ট দেখুন →" else "View Cart →",
                color = SunInk,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
