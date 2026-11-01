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
                openWhatsAppToOwner(app.prefs.ownerWhatsapp, text)
            },
            onSendEnquiryWhatsApp = { customMessage ->
                openWhatsAppToOwner(app.prefs.ownerWhatsapp, customMessage)
            },
            onShareTextWhatsApp = { text ->
                shareWhatsAppText(text)
            },
            onShareImage = { bitmap, caption ->
                shareBitmapImage(bitmap, caption)
            },
            onCallSupport = { phone ->
                callPhone(phone.ifBlank { app.siteConfig.supportPhone })
            },
            onCopyText = { label, text ->
                copyToClipboard(label, text)
            },
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

    override fun onResume() {
        super.onResume()
        app.refresh()
    }

    /**
     * Opens a direct WhatsApp chat with the GMM Owner (`919083732101`) pre-filled with the formatted order receipt.
     * Tries native WhatsApp / WhatsApp Business first, then falls back to `https://wa.me/...` browser link or share sheet.
     */
    private fun openWhatsAppToOwner(ownerPhoneDigits: String, messageText: String) {
        val digits = ownerPhoneDigits.filter { it.isDigit() }.let {
            if (it.length == 10) "91$it" else it.ifBlank { "919083732101" }
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
