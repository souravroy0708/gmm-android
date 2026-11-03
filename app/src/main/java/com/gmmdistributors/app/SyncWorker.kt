package com.gmmdistributors.app

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gmmdistributors.app.api.Repo
import java.util.concurrent.TimeUnit

/**
 * Sends orders that were made without internet. Android runs it as soon as any network is available,
 * even if the app is closed, and retries with back-off on a flaky connection. It also refreshes the
 * catalogue snapshot (a ~60-byte call when nothing changed).
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repo = Repo(applicationContext)
        val lang = Prefs(applicationContext).language
        repo.loadCatalog()
        val r = repo.flushPending(lang)
        return if (r.stillWaiting > 0) Result.retry() else Result.success()
    }

    companion object {
        private const val NAME = "gmm-send-pending-orders"

        fun schedule(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}
