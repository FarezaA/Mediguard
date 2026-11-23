package com.mediguard.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mediguard.app.data.model.DoseStatus
import com.mediguard.app.data.repository.MediGuardRepository

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            NotificationHelper.createNotificationChannel(context)

            // Re-schedule active upcoming doses for today
            val repository = MediGuardRepository.instance
            if (repository != null) {
                val upcomingDoses = repository.doseEvents.value.filter {
                    it.status == DoseStatus.TERJADWAL || it.status == DoseStatus.MENUNGGU_LAPORAN
                }
                for (dose in upcomingDoses) {
                    MedicationAlarmScheduler.scheduleDose(context, dose)
                }
            }
        }
    }
}
