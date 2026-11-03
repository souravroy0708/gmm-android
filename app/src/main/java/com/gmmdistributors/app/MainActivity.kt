package com.gmmdistributors.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.gmmdistributors.app.ui.GmmTheme
import com.gmmdistributors.app.ui.Lang
import com.gmmdistributors.app.ui.MainShell
import com.gmmdistributors.app.ui.PhoneActions
import java.io.File

/**
 * Single-Activity Jetpack Compose entry point modeled directly on `bas1minute-android`'s `MainActivity.kt`.
 * Handles native intents for:
 * - Direct WhatsApp order notification to GMM Owner (`wa.me/919083732101?text=...` with `com.whatsapp` / `com.whatsapp.w4b`)
 * - Sharing GMM Thought Cards as PNG via `FileProvider`
 * - One-tap phone dialer (`tel:9083732101`)
 * - Clipboard copy for manual UPI ID / Bank Account / Order Summary
 */
class MainActivity : ComponentActivity() {

    private lateinit var app: AppController

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        app = AppController(applicationContext, lifecycleScope)

        val actions = PhoneActions(
            onSendOrderWhatsApp = { order ->
                val text = app.buildWhatsAppOrderMessage(order)
                app.markOrderWhatsappSent(order.orderNo)
                openWhatsAppToOwner(app.ownerWhatsapp, text)
            },
            onPayUpi = { order -> payWithUpi(order) },
            onShareInvoicePdf = { inv -> shareInvoicePdf(inv) },
            onSendEnquiryWhatsApp = { customMessage ->
                openWhatsAppToOwner(app.ownerWhatsapp, customMessage)
            },
            onShareTextWhatsApp = { text ->
                shareWhatsAppText(text)
            },
            onShareImage = { bitmap, caption ->
                shareBitmapImage(bitmap, caption)
            },
            onCallSupport = { phone ->
                callPhone(phone.ifBlank { app.siteConfig.supportPhone.ifBlank { app.prefs.supportPhone } })
            },
            onCopyText = { label, text ->
                copyToClipboard(label, text)
            },
            onSendSms = { phone, text -> sendSms(phone, text) },
        )

        setContent {
            val systemDark = isSystemInDarkTheme()
            Lang.current = app.language
            GmmTheme(
                dark = when (app.theme) {
                    "dark" -> true
                    "light" -> false
                    else -> systemDark
                }
            ) {
                MainShell(app = app, actions = actions)
            }
        }
    }

    private var netCallback: android.net.ConnectivityManager.NetworkCallback? = null

    override fun onStart() {
        super.onStart()
        // Network back -> refresh the catalogue (tiny if unchanged) and send orders saved offline.
        val cm = getSystemService(android.net.ConnectivityManager::class.java) ?: return
        val cb = object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                runOnUiThread { app.onNetworkAvailable() }
            }
            override fun onLost(network: android.net.Network) {
                runOnUiThread { app.offline = true }
            }
        }
        runCatching { cm.registerDefaultNetworkCallback(cb); netCallback = cb }
    }

    override fun onStop() {
        netCallback?.let { cb -> runCatching { getSystemService(android.net.ConnectivityManager::class.java)?.unregisterNetworkCallback(cb) } }
        netCallback = null
        super.onStop()
    }

    /** Opens the SMS app with the order text to GMM's number. Works without mobile data. */
    private fun sendSms(phone: String, text: String) {
        val digits = phone.filter { it.isDigit() || it == '+' }
        try {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$digits")).putExtra("sms_body", text))
        } catch (_: Exception) {
            copyToClipboard("Order", text)
        }
    }

    override fun onResume() {
        super.onResume()
        app.refresh()
        // Coming back from the UPI app: show the latest payment state of the open order.
        app.openOrderNo?.let { app.refreshOrder(it) }
    }

    /** Opens the customer's UPI app (GPay, PhonePe, Paytm, BHIM...) with our UPI ID and the amount.
     *  This is a plain UPI intent, not a payment gateway: money goes straight to GMM's account. */
    private fun payWithUpi(order: com.gmmdistributors.app.api.Order) {
        val uri = app.upiPayUri(order)
        if (uri == null) {
            app.message = if (app.language == "bn") "UPI এখনও চালু হয়নি। ব্যাংক ট্রান্সফার করুন বা ফোন করুন।"
            else "UPI is not set up yet. Please use bank transfer or call us."
            return
        }
        try {
            startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, uri), if (app.language == "bn") "UPI অ্যাপ বেছে নিন" else "Pay with"))
        } catch (_: Exception) {
            copyToClipboard("UPI ID", app.siteConfig.upiId)
            app.message = if (app.language == "bn") "কোনো UPI অ্যাপ পাওয়া যায়নি। UPI ID কপি করা হয়েছে।"
            else "No UPI app found. The UPI ID has been copied."
        }
    }

    private fun shareInvoicePdf(inv: com.gmmdistributors.app.api.Invoice) {
        try {
            val file = InvoicePdf.write(this, inv)
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val send = Intent(Intent.ACTION_SEND)
                .setType("application/pdf")
                .putExtra(Intent.EXTRA_STREAM, uri)
                .putExtra(Intent.EXTRA_SUBJECT, "GMM Invoice ${inv.invoiceNo}")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(send, "Invoice ${inv.invoiceNo}"))
        } catch (_: Exception) {
            app.message = "Could not create the PDF. Please try again."
        }
    }

    /**
     * Opens a direct WhatsApp chat with the GMM Owner (`919083732101`) pre-filled with the formatted order receipt.
     * Tries native WhatsApp / WhatsApp Business first, then falls back to `https://wa.me/...` browser link or share sheet.
     */
    private fun openWhatsAppToOwner(ownerPhoneDigits: String, messageText: String) {
        val digits = ownerPhoneDigits.filter { it.isDigit() }.let {
            if (it.length == 10) "91$it" else it.ifBlank { BuildConfig.OWNER_WHATSAPP }
        }
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$digits&text=${Uri.encode(messageText)}")
        for (pkg in listOf("com.whatsapp", "com.whatsapp.w4b")) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, uri).setPackage(pkg)
                startActivity(intent)
                return
            } catch (_: Exception) {
                // Package not installed; try next
            }
        }
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits?text=${Uri.encode(messageText)}")))
        } catch (_: Exception) {
            shareWhatsAppText(messageText)
        }
    }

    private fun shareWhatsAppText(text: String) {
        for (pkg in listOf("com.whatsapp", "com.whatsapp.w4b")) {
            try {
                startActivity(
                    Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, text)
                        .setPackage(pkg)
                )
                return
            } catch (_: Exception) {
            }
        }
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(send, "Share via"))
    }

    private fun shareBitmapImage(bitmap: Bitmap, caption: String) {
        try {
            val dir = File(cacheDir, "share").apply { mkdirs() }
            val file = File(dir, "gmm_organic_card.png")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val send = Intent(Intent.ACTION_SEND)
                .setType("image/png")
                .putExtra(Intent.EXTRA_STREAM, uri)
                .putExtra(Intent.EXTRA_TEXT, caption)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(send, "Share GMM Organic Card"))
        } catch (_: Exception) {
            app.message = "Could not share image. Please try again."
        }
    }

    private fun callPhone(number: String) {
        try {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
        } catch (_: Exception) {
            app.message = "Call $number from your phone dialer."
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getSystemService(ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
        app.message = if (app.language == "bn") "$label কপি করা হয়েছে ✓" else "$label copied to clipboard ✓"
    }
}
