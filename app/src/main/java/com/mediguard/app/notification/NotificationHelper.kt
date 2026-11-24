package com.mediguard.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mediguard.app.MainActivity
import com.mediguard.app.R

object NotificationHelper {

    const val CHANNEL_ID = "medication_reminders"
    const val CHANNEL_NAME = "Pengingat Minum Obat"
    const val CHANNEL_DESCRIPTION = "Pemberitahuan waktu konsumsi obat terjadwal"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showMedicationReminder(
        context: Context,
        doseEventId: String,
        medicationName: String,
        dosage: String,
        instructions: String,
        scheduledTime: String,
        notificationId: Int = doseEventId.hashCode()
    ) {
        createNotificationChannel(context)

        // Main action: Open App / Detail
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_DOSE_EVENT_ID", doseEventId)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action: 15 minutes
        val snoozeIntent = Intent(context, MedicationReminderReceiver::class.java).apply {
            action = MedicationReminderReceiver.ACTION_SNOOZE
            putExtra(MedicationReminderReceiver.EXTRA_DOSE_EVENT_ID, doseEventId)
            putExtra(MedicationReminderReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(MedicationReminderReceiver.EXTRA_DOSAGE, dosage)
            putExtra(MedicationReminderReceiver.EXTRA_INSTRUCTIONS, instructions)
            putExtra(MedicationReminderReceiver.EXTRA_SCHEDULED_TIME, scheduledTime)
            putExtra(MedicationReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 10000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val detailText = buildString {
            if (dosage.isNotBlank()) append(dosage)
            if (instructions.isNotBlank()) {
                if (isNotEmpty()) append(" • ")
                append(instructions)
            }
            if (scheduledTime.isNotBlank()) {
                if (isNotEmpty()) append(" (Jadwal: $scheduledTime)")
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Waktunya Minum Obat: $medicationName")
            .setContentText(detailText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$detailText\n\nSegera minum obat dan lakukan konfirmasi di aplikasi MediGuard."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(0, "Tunda 15 Mnt", snoozePendingIntent)
            .addAction(0, "Buka / Lapor", openAppPendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS may not have been granted
        }
    }
}
