package com.mediguard.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mediguard.app.data.model.DoseEvent
import java.util.Calendar

object MedicationAlarmScheduler {

    /**
     * Calculates trigger timestamp in epoch milliseconds.
     * If [scheduledDate] is null or blank, uses today's date.
     */
    fun calculateTriggerMillis(
        scheduledDate: String?,
        scheduledTime: String,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        val timeParts = scheduledTime.trim().split(":")
        if (timeParts.size < 2) return -1L
        val hour = timeParts[0].toIntOrNull() ?: return -1L
        val minute = timeParts[1].toIntOrNull() ?: return -1L

        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (!scheduledDate.isNullOrBlank()) {
            val dateParts = scheduledDate.trim().split("-")
            if (dateParts.size == 3) {
                val year = dateParts[0].toIntOrNull()
                val month = dateParts[1].toIntOrNull() // 1-12
                val day = dateParts[2].toIntOrNull()
                if (year != null && month != null && day != null) {
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month - 1)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                }
            }
        }

        return calendar.timeInMillis
    }

    fun scheduleDose(context: Context, doseEvent: DoseEvent): Boolean {
        val targetTime = doseEvent.snoozedUntil ?: doseEvent.scheduledTime
        val triggerMillis = calculateTriggerMillis(doseEvent.scheduledDate, targetTime)

        if (triggerMillis <= System.currentTimeMillis()) {
            return false
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            action = MedicationReminderReceiver.ACTION_TRIGGER_REMINDER
            putExtra(MedicationReminderReceiver.EXTRA_DOSE_EVENT_ID, doseEvent.id)
            putExtra(MedicationReminderReceiver.EXTRA_MEDICATION_NAME, doseEvent.medicationName)
            putExtra(MedicationReminderReceiver.EXTRA_DOSAGE, doseEvent.dosage)
            putExtra(MedicationReminderReceiver.EXTRA_INSTRUCTIONS, doseEvent.instructions)
            putExtra(MedicationReminderReceiver.EXTRA_SCHEDULED_TIME, targetTime)
            putExtra(MedicationReminderReceiver.EXTRA_NOTIFICATION_ID, doseEvent.id.hashCode())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            doseEvent.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarmExact(alarmManager, triggerMillis, pendingIntent)
        return true
    }

    fun scheduleSnooze(
        context: Context,
        doseEventId: String,
        medicationName: String,
        dosage: String,
        instructions: String,
        minutes: Int = 15
    ): Long {
        val triggerMillis = System.currentTimeMillis() + (minutes * 60 * 1000L)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return -1L

        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            action = MedicationReminderReceiver.ACTION_TRIGGER_REMINDER
            putExtra(MedicationReminderReceiver.EXTRA_DOSE_EVENT_ID, doseEventId)
            putExtra(MedicationReminderReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(MedicationReminderReceiver.EXTRA_DOSAGE, dosage)
            putExtra(MedicationReminderReceiver.EXTRA_INSTRUCTIONS, instructions)
            putExtra(MedicationReminderReceiver.EXTRA_SCHEDULED_TIME, "+$minutes mnt")
            putExtra(MedicationReminderReceiver.EXTRA_NOTIFICATION_ID, doseEventId.hashCode())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            doseEventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarmExact(alarmManager, triggerMillis, pendingIntent)
        return triggerMillis
    }

    fun cancelDose(context: Context, doseEventId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            action = MedicationReminderReceiver.ACTION_TRIGGER_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            doseEventId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun setAlarmExact(alarmManager: AlarmManager, triggerMillis: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            // Fallback if exact alarm permission was revoked
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }
}
