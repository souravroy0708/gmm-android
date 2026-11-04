package com.gmmdistributors.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
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
    // ---- Mandatory updates through Google Play (IMMEDIATE flow: full screen, app restarts updated) ----
    private val updateManager by lazy { AppUpdateManagerFactory.create(this) }
    private val updateLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        // Backed out or failed: block the app with "Please update" until they update.
        if (result.resultCode != RESULT_OK) app.playUpdateRequired = true
    }

    /** Asks Google Play if a newer version is published. No internet / not installed from Play: carry on (offline-first). */
    private fun checkForPlayUpdate() {
        updateManager.appUpdateInfo.addOnSuccessListener { info ->
            val available = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
                info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            if (available && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                runCatching {
                    updateManager.startUpdateFlowForResult(info, updateLauncher, AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build())
                }.onFailure { app.playUpdateRequired = true }
            } else if (info.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                app.playUpdateRequired = false
            }
        }
    }

    /** "Update now" on the blocking screen: try the Play flow again, else open the Play Store page. */
    private fun updateApp() {
        updateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                updateManager.startUpdateFlowForResult(info, updateLauncher, AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build())
            } else openPlayStore()
        }.addOnFailureListener { openPlayStore() }
    }

    private fun openPlayStore() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (_: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    /** Android photo picker: no storage permission needed. Picked image replaces the auto screenshot. */
    private val pickReportPhoto = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) Thread {
            val path = runCatching {
                contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it) }?.let { saveReportJpeg(it) }
            }.getOrNull()
            runOnUiThread {
                if (path != null) { app.reportShot?.let { File(it).delete() }; app.reportShot = path }
                else app.message = "Could not open that photo."
            }
        }.start()
    }


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
            onCallSupport = { phone ->
                callPhone(phone.ifBlank { app.siteConfig.supportPhone.ifBlank { app.prefs.supportPhone } })
            },
            onCopyText = { label, text ->
                copyToClipboard(label, text)
            },
            onSendSms = { phone, text -> sendSms(phone, text) },
            onUpdateApp = { updateApp() },
            onReportProblem = { screen, orderNo -> captureScreen { path -> app.openReport(screen, orderNo, path) } },
            onPickReportPhoto = { pickReportPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onEmail = { to, subject -> sendEmail(to, subject) },
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

    /**
     * Screenshot of exactly what the customer is looking at (PixelCopy reads the real window, so Compose,
     * images and dialogs are all captured). Saved small (720 px wide, JPEG 70 ≈ 40-90 KB) for slow networks.
     */
    private fun captureScreen(done: (String?) -> Unit) {
        val view = window.decorView
        if (view.width <= 0 || view.height <= 0) { done(null); return }
        val bmp = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        try {
            android.view.PixelCopy.request(window, bmp, { result ->
                if (result != android.view.PixelCopy.SUCCESS) { done(null); return@request }
                Thread {
                    val path = runCatching { saveReportJpeg(bmp) }.getOrNull()
                    runOnUiThread { done(path) }
                }.start()
            }, android.os.Handler(android.os.Looper.getMainLooper()))
        } catch (_: Exception) {
            done(null)
        }
    }

    private fun saveReportJpeg(src: Bitmap): String {
        val w = minOf(720, src.width)
        val h = (src.height * (w.toFloat() / src.width)).toInt().coerceAtLeast(1)
        val small = if (w == src.width) src else Bitmap.createScaledBitmap(src, w, h, true)
        val dir = File(filesDir, "feedback").apply { mkdirs() }
        val file = File(dir, "shot-${System.currentTimeMillis()}.jpg")
        file.outputStream().use { small.compress(Bitmap.CompressFormat.JPEG, 70, it) }
        return file.absolutePath
    }

    private fun sendEmail(to: String, subject: String) {
        val body = "\n\n---\nApp ${BuildConfig.VERSION_NAME} · Android ${android.os.Build.VERSION.RELEASE} · ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
        val uri = Uri.parse("mailto:$to?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}")
        try {
            startActivity(Intent(Intent.ACTION_SENDTO, uri))
        } catch (_: Exception) {
            copyToClipboard("Email", to)
            app.message = if (app.language == "bn") "ইমেল ঠিকানা কপি হয়েছে: $to" else "Email address copied: $to"
        }
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
        checkForPlayUpdate()
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
