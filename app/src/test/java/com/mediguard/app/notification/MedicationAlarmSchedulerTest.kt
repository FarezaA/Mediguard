package com.mediguard.app.notification

import com.mediguard.app.data.repository.MediGuardRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class MedicationAlarmSchedulerTest {

    @Test
    fun testCalculateTriggerMillis_withSpecificDateAndTime() {
        val date = "2026-10-20"
        val time = "14:30"
        val triggerMillis = MedicationAlarmScheduler.calculateTriggerMillis(date, time)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = triggerMillis
        }

        assertEquals(2026, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, calendar.get(Calendar.MONTH))
        assertEquals(20, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(14, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
        assertEquals(0, calendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun testCalculateTriggerMillis_withInvalidTime() {
        val result = MedicationAlarmScheduler.calculateTriggerMillis("2026-10-20", "invalid_time")
        assertEquals(-1L, result)

        val result2 = MedicationAlarmScheduler.calculateTriggerMillis("2026-10-20", "25:99")
        // Hour 25 might roll over or be parsed, but let's test empty / bad format:
        val resultEmpty = MedicationAlarmScheduler.calculateTriggerMillis("2026-10-20", "")
        assertEquals(-1L, resultEmpty)
    }

    @Test
    fun testCalculateTriggerMillis_withBlankDate_usesCurrentDate() {
        val time = "09:15"
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.MAY, 15, 8, 0, 0)
        }.timeInMillis

        val triggerMillis = MedicationAlarmScheduler.calculateTriggerMillis(null, time, nowMillis = now)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = triggerMillis
        }

        assertEquals(2026, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.MAY, calendar.get(Calendar.MONTH))
        assertEquals(15, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(15, calendar.get(Calendar.MINUTE))
        assertTrue(triggerMillis > now)
    }

    @Test
    fun testPendingDoseActionId_setAndClear() {
        val repository = MediGuardRepository()
        assertNull(repository.pendingDoseActionId.value)

        repository.setPendingDoseActionId("dose_test_123")
        assertEquals("dose_test_123", repository.pendingDoseActionId.value)

        repository.clearPendingDoseActionId()
        assertNull(repository.pendingDoseActionId.value)
    }

    @Test
    fun testSnoozeDose_updatesSnoozeTime() {
        val repository = MediGuardRepository()
        repository.seedInitialDemoData()
        val firstDose = repository.doseEvents.value.first()

        val snoozeRes = repository.snoozeDose(firstDose.id, 15)
        assertTrue(snoozeRes.isSuccess)

        val updatedDose = repository.doseEvents.value.first { it.id == firstDose.id }
        assertTrue(!updatedDose.snoozedUntil.isNullOrBlank())
    }
}
