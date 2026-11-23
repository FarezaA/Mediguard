package com.mediguard.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.mediguard.app.data.repository.MediGuardRepository

class MedicationReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TRIGGER_REMINDER = "com.mediguard.app.ACTION_TRIGGER_REMINDER"
        const val ACTION_SNOOZE = "com.mediguard.app.ACTION_SNOOZE"

        const val EXTRA_DOSE_EVENT_ID = "extra_dose_event_id"
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val EXTRA_INSTRUCTIONS = "extra_instructions"
        const val EXTRA_SCHEDULED_TIME = "extra_scheduled_time"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val doseEventId = intent.getStringExtra(EXTRA_DOSE_EVENT_ID) ?: return
        val medName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Obat Anda"
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: ""
        val instructions = intent.getStringExtra(EXTRA_INSTRUCTIONS) ?: ""
        val scheduledTime = intent.getStringExtra(EXTRA_SCHEDULED_TIME) ?: ""
        val notifId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, doseEventId.hashCode())

        when (action) {
            ACTION_TRIGGER_REMINDER -> {
                NotificationHelper.showMedicationReminder(
                    context = context,
                    doseEventId = doseEventId,
                    medicationName = medName,
                    dosage = dosage,
                    instructions = instructions,
                    scheduledTime = scheduledTime,
                    notificationId = notifId
                )
            }
            ACTION_SNOOZE -> {
                // Dismiss the current notification banner
                NotificationManagerCompat.from(context).cancel(notifId)

                // Schedule alarm for 15 minutes later
                MedicationAlarmScheduler.scheduleSnooze(
                    context = context,
                    doseEventId = doseEventId,
                    medicationName = medName,
                    dosage = dosage,
                    instructions = instructions,
                    minutes = 15
                )

                // Update dose status in repository if active
                MediGuardRepository.instance?.snoozeDose(doseEventId, 15)

                Toast.makeText(context, "Pengingat $medName ditunda 15 menit", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
