package com.qadam.lastpuff.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.qadam.lastpuff.QadamApplication
import com.qadam.lastpuff.R
import com.qadam.lastpuff.domain.support.MessageSession
import com.qadam.lastpuff.domain.support.SupportMessageBank
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as QadamApplication
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_HOLDING -> showEncouragement(context)
                    ACTION_RELAPSE -> app.repository.recordRelapse()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showEncouragement(context: Context) {
        createChannel(context)
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val message = SupportMessageBank.holdingEncouragement(MessageSession(day))
        val notification = NotificationCompat.Builder(context, MotivationNotificationWorker.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Qadam Last Puff")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(ENCOURAGEMENT_NOTIFICATION_ID, notification)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MotivationNotificationWorker.CHANNEL_ID,
                "Мотивация",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ежедневные напоминания и проверка дня"
            }
            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_HOLDING = "com.qadam.lastpuff.ACTION_HOLDING"
        const val ACTION_RELAPSE = "com.qadam.lastpuff.ACTION_RELAPSE"
        const val ENCOURAGEMENT_NOTIFICATION_ID = 1002
    }
}
