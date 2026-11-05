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
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gmmdistributors.app.api.Order
import com.gmmdistributors.app.api.Repo
import com.gmmdistributors.app.api.Res
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Order-status notifications without Firebase: every 6 hours (only with internet) compare the
 * customer's orders with what the phone last saw, and notify on real changes. The first run only
 * remembers the current state, so nobody gets a pile of old alerts.
 */
class StatusWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repo = Repo(applicationContext)
        if (!repo.signedIn) return Result.success()
        val prefs = Prefs(applicationContext)
        val lang = prefs.language
        val orders = (repo.loadOrders(lang) as? Res.Ok)?.value ?: return Result.retry()
        val db = LocalDb.get(applicationContext)
        val before = db.getCache("order_status")?.let { runCatching { JSONObject(it) }.getOrNull() }
        val now = JSONObject()
        orders.forEach { now.put(it.orderNo, "${it.status}|${it.paymentStatus}") }
        db.putCache("order_status", now.toString())
        if (before == null) return Result.success()
        orders.forEach { o ->
            val old = before.optString(o.orderNo, "")
            val cur = "${o.status}|${o.paymentStatus}"
            if (old.isNotEmpty() && old != cur) message(o, old.substringBefore('|'), old.substringAfter('|'), lang)?.let { notify(o, it) }
        }
        return Result.success()
    }

    private fun message(o: Order, oldStatus: String, oldPay: String, lang: String): String? {
        val bn = lang == "bn"
        if (o.status != oldStatus) return when (o.status) {
            "CONFIRMED" -> if (bn) "✓ অর্ডার ${o.orderNo} নিশ্চিত হয়েছে" else "✓ Order ${o.orderNo} is confirmed"
            "PACKED" -> if (bn) "📦 অর্ডার ${o.orderNo} প্যাক হয়েছে" else "📦 Order ${o.orderNo} is packed"
            "DISPATCHED" -> if (bn) "🚚 অর্ডার ${o.orderNo} পাঠানো হয়েছে, শীঘ্রই পৌঁছাবে" else "🚚 Order ${o.orderNo} is on the way"
            "DELIVERED" -> if (bn) "🏠 অর্ডার ${o.orderNo} পৌঁছে গেছে। ধন্যবাদ!" else "🏠 Order ${o.orderNo} delivered. Thank you!"
            "CANCELLED" -> if (bn) "অর্ডার ${o.orderNo} বাতিল হয়েছে" else "Order ${o.orderNo} was cancelled"
            else -> null
        }
        if (o.paymentStatus != oldPay) return when (o.paymentStatus) {
            "VERIFIED" -> if (bn) "💳 ${o.orderNo}: পেমেন্ট পাওয়া গেছে ✓" else "💳 ${o.orderNo}: payment received ✓"
            "REJECTED" -> if (bn) "⚠ ${o.orderNo}: UTR মেলেনি, অ্যাপে আবার পাঠান" else "⚠ ${o.orderNo}: UTR not matched, please resend"
            else -> null
        }
        return null
    }

    private fun notify(o: Order, text: String) {
        val ctx = applicationContext
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        ensureChannel(ctx)
        val open = PendingIntent.getActivity(
            ctx, o.orderNo.hashCode(),
            Intent(ctx, MainActivity::class.java).putExtra(EXTRA_ORDER, o.orderNo).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_gmm)
            .setColor(0xFF1F6B3A.toInt())
            .setContentTitle("GMM Organic")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(ctx).notify(o.orderNo.hashCode(), n) }
    }

    companion object {
        const val CHANNEL = "orders"
        const val EXTRA_ORDER = "open_order_no"
        private const val NAME = "gmm-order-status"

        fun ensureChannel(ctx: Context) {
            val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
            if (nm.getNotificationChannel(CHANNEL) == null) {
                nm.createNotificationChannel(NotificationChannel(CHANNEL, "Order updates · অর্ডারের খবর", NotificationManager.IMPORTANCE_DEFAULT))
            }
        }

        fun schedule(ctx: Context) {
            val req = PeriodicWorkRequestBuilder<StatusWorker>(6, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.KEEP, req)
        }
    }
}
