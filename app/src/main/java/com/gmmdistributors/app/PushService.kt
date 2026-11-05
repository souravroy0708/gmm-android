package com.gmmdistributors.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Firebase push (same approach as Bas1Minute). Two kinds of DATA message from the server:
 *  - "campaign": offers, season reminders, Krishi tips (text already personalised and in the right language)
 *  - "order":    instant order status ("dispatched", "delivered")
 */
class PushService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Prefs(this).pushToken = token
        Push.upload(applicationContext)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val d = message.data
        val ctx = applicationContext
        when (d["type"]) {
            "campaign" -> {
                if (!Prefs(ctx).promoPush) return  // customer switched offers off
                val title = d["title"]?.takeIf { it.isNotBlank() } ?: return
                Push.show(ctx, Push.CH_OFFERS, title, d["body"].orEmpty(), 2000 + (d["id"]?.toIntOrNull() ?: 0),
                    link = d["link"].orEmpty(), campaignId = d["id"].orEmpty())
            }
            "order" -> {
                val no = d["order_no"] ?: return
                Push.show(ctx, StatusWorker.CHANNEL, d["title"] ?: "GMM Organic", d["body"].orEmpty(), no.hashCode(), orderNo = no)
            }
        }
    }
}

object Push {
    const val CH_OFFERS = "offers"
    const val EXTRA_LINK = "push_link"
    const val EXTRA_CAMPAIGN = "push_campaign"

    /** Send this phone's token to GMM ("" when the customer turned notifications off). Safe without Firebase. */
    fun upload(context: Context) {
        val prefs = Prefs(context)
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { com.gmmdistributors.app.api.Repo(context).registerPush(prefs.pushToken, prefs.promoPush) }
        }
    }

    /** Ask Firebase for the token. Does nothing if the app was built without google-services.json. */
    fun refreshToken(context: Context) {
        try {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { t ->
                if (t != Prefs(context).pushToken) {
                    Prefs(context).pushToken = t
                    upload(context)
                }
            }
        } catch (_: Exception) {
            // Firebase not configured in this build: pushes are off, everything else works.
        }
    }

    fun ensureChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        if (nm.getNotificationChannel(CH_OFFERS) == null) {
            nm.createNotificationChannel(NotificationChannel(CH_OFFERS, "Offers & Krishi tips · অফার ও কৃষি পরামর্শ",
                NotificationManager.IMPORTANCE_DEFAULT).apply { description = "Discounts, season reminders and farming tips from GMM" })
        }
        StatusWorker.ensureChannel(context)
    }

    fun show(context: Context, channel: String, title: String, body: String, id: Int,
             link: String = "", campaignId: String = "", orderNo: String = "") {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        ensureChannels(context)
        val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra(EXTRA_LINK, link).putExtra(EXTRA_CAMPAIGN, campaignId)
        if (orderNo.isNotBlank()) intent.putExtra(StatusWorker.EXTRA_ORDER, orderNo)
        val open = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_gmm)
            .setColor(0xFF1F6B3A.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(id, n) }
    }
}
