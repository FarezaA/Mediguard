package com.mediguard.app

import com.mediguard.app.data.model.ColorBlindMode
import com.mediguard.app.data.model.DoseStatus
import com.mediguard.app.data.model.MembershipStatus
import com.mediguard.app.data.model.ThemeMode
import com.mediguard.app.data.model.UserRole
import com.mediguard.app.data.repository.MediGuardRepository
import com.mediguard.app.ui.theme.resolveMediGuardColors
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MediGuardRepositoryTest {

    private lateinit var repository: MediGuardRepository

    @Before
    fun setUp() {
        repository = MediGuardRepository()
        repository.seedInitialDemoData()
    }

    @Test
    fun testAuthenticationAndRoleSwitch() {
        // Register new user
        val regResult = repository.register("Dr. Andi", "andi@mediguard.com", "password123", UserRole.PASIEN)
        assertTrue(regResult.isSuccess)
        assertEquals("Dr. Andi", repository.currentUser.value?.name)
        assertEquals(UserRole.PASIEN, repository.currentUser.value?.activeRole)

        // Switch role
        repository.switchRole(UserRole.PEMANTAU)
        assertEquals(UserRole.PEMANTAU, repository.currentUser.value?.activeRole)

        // Logout
        repository.logout()
        assertNull(repository.currentUser.value)

        // Login
        val loginResult = repository.login("andi@mediguard.com", "password123")
        assertTrue(loginResult.isSuccess)
        assertNotNull(repository.currentUser.value)
    }

    @Test
    fun testMedicationRoomCreationAndInviteCodeRotation() {
        repository.switchUser("user_patient_1")
        val roomResult = repository.createRoom("Ruang Pengobatan Mandiri", "Deskripsi ruang")
        assertTrue(roomResult.isSuccess)
        val room = roomResult.getOrThrow()
        assertTrue(room.inviteCode.startsWith("MG-"))

        // Rotate invite code
        val rotateResult = repository.rotateInviteCode(room.id)
        assertTrue(rotateResult.isSuccess)
        val newCode = rotateResult.getOrThrow()
        assertNotEquals(room.inviteCode, newCode)

        // Update room name and description
        val updateResult = repository.updateRoom(room.id, "Ruang Kamar Pasien", "Deskripsi baru kamar")
        assertTrue(updateResult.isSuccess)
        val updatedRoom = updateResult.getOrThrow()
        assertEquals("Ruang Kamar Pasien", updatedRoom.name)
        assertEquals("Deskripsi baru kamar", updatedRoom.description)

        // Delete room
        val deleteResult = repository.deleteRoom(room.id)
        assertTrue(deleteResult.isSuccess)
        assertNull(repository.rooms.value.find { it.id == room.id })
    }

    @Test
    fun testFlexibleRoomCodeOrIdLookup() {
        repository.switchUser("user_patient_1")
        val roomResult = repository.createRoom("Ruang Mandiri", "Deskripsi")
        assertTrue(roomResult.isSuccess)
        val room = roomResult.getOrThrow()

        // Lookup with exact code
        val lookup1 = repository.findRoomPreviewByCode(room.inviteCode)
        assertTrue(lookup1.isSuccess)
        assertEquals(room.id, lookup1.getOrThrow().id)

        // Lookup with code without MG- prefix
        val bareCode = room.inviteCode.removePrefix("MG-")
        val lookup2 = repository.findRoomPreviewByCode(bareCode)
        assertTrue(lookup2.isSuccess)
        assertEquals(room.id, lookup2.getOrThrow().id)

        // Lookup with room ID
        val lookup3 = repository.findRoomPreviewByCode(room.id)
        assertTrue(lookup3.isSuccess)
        assertEquals(room.id, lookup3.getOrThrow().id)
    }

    @Test
    fun testJoinRequestRequiresPatientApproval() {
        // Create new caregiver
        val newCaregiver = repository.register("Rina", "rina@mediguard.com", "password123", UserRole.PEMANTAU).getOrThrow()

        // Submit join request
        val joinResult = repository.requestJoinRoom("MG-8821")
        assertTrue(joinResult.isSuccess)
        val member = joinResult.getOrThrow()
        assertEquals(MembershipStatus.PENDING, member.status)

        // Switch back to patient
        repository.switchUser("user_patient_1")

        // Patient approves
        val approveResult = repository.approveMember(member.id)
        assertTrue(approveResult.isSuccess)

        val updatedMember = repository.roomMembers.value.find { it.id == member.id }
        assertEquals(MembershipStatus.APPROVED, updatedMember?.status)
    }

    @Test
    fun testPatientSelfReportDistinctFromCaregiverVerification() {
        repository.switchUser("user_patient_1")
        val dose = repository.doseEvents.value.first { it.status == DoseStatus.MENUNGGU_LAPORAN }

        // Patient self-reports taken
        val reportResult = repository.reportDoseTaken(dose.id, "19:05")
        assertTrue(reportResult.isSuccess)
        val reportedDose = reportResult.getOrThrow()

        // Crucial requirement: Must be DILAPORKAN_DIMINUM, not automatically verified!
        assertEquals(DoseStatus.DILAPORKAN_DIMINUM, reportedDose.status)
        assertNull(reportedDose.verification)

        // Switch to Caregiver Siti
        repository.switchUser("user_caregiver_1")

        // Direct observation verification without confirmation must fail
        val failVerify = repository.verifyDoseDirectObservation(
            doseEventId = dose.id,
            directObservationConfirmed = false
        )
        assertTrue(failVerify.isFailure)

        // Direct observation verification with confirmation succeeds
        val successVerify = repository.verifyDoseDirectObservation(
            doseEventId = dose.id,
            directObservationConfirmed = true,
            notes = "Disaksikan saat makan malam bersama"
        )
        assertTrue(successVerify.isSuccess)
        val verifiedDose = successVerify.getOrThrow()
        assertEquals(DoseStatus.TERVERIFIKASI_LANGSUNG, verifiedDose.status)
        assertNotNull(verifiedDose.verification)
        assertEquals("Pengamatan Langsung", verifiedDose.verification?.verificationType)
    }

    @Test
    fun testPatientRevokeAccess() {
        repository.switchUser("user_patient_1")
        val memberCaregiver = repository.roomMembers.value.first {
            it.role == UserRole.PEMANTAU && it.status == MembershipStatus.APPROVED
        }

        // Revoke
        val revokeRes = repository.revokeMemberAccess(memberCaregiver.id)
        assertTrue(revokeRes.isSuccess)

        val updated = repository.roomMembers.value.find { it.id == memberCaregiver.id }
        assertEquals(MembershipStatus.REVOKED, updated?.status)
    }

    @Test
    fun testComplianceStatisticsDistinguishesSelfReportAndVerified() {
        val stats = repository.getAdherenceStats("room_1")
        assertTrue(stats.totalDue > 0)
        assertTrue(stats.compliancePercentage >= 0.0)
        assertTrue(stats.verifiedPercentage >= 0.0)
    }

    @Test
    fun testPhotoProofAndRepeatedCaregiverReminders() {
        // Patient reports dose with photo proof
        repository.switchUser("user_patient_1")
        val dose = repository.doseEvents.value.first { it.status == DoseStatus.MENUNGGU_LAPORAN }
        val reportRes = repository.reportDoseTaken(dose.id, "19:10", "foto_bukti_pasien.jpg")
        assertTrue(reportRes.isSuccess)
        assertEquals("foto_bukti_pasien.jpg", reportRes.getOrThrow().photoUri)

        // Caregiver sends repeated reminders for another late/due dose
        repository.switchUser("user_caregiver_1")
        val lateDose = repository.doseEvents.value.first { it.id == "dose_3" }
        val initialCount = lateDose.reminderCount // 1 default

        // First caregiver reminder
        val remindRes1 = repository.sendCaregiverReminder(lateDose.id)
        assertTrue(remindRes1.isSuccess)
        assertEquals(initialCount + 1, remindRes1.getOrThrow())

        // Second caregiver reminder (repeated)
        val remindRes2 = repository.sendCaregiverReminder(lateDose.id)
        assertTrue(remindRes2.isSuccess)
        assertEquals(initialCount + 2, remindRes2.getOrThrow())

        // Patient received reminder notifications
        repository.switchUser("user_patient_1")
        val patientNotifs = repository.getNotificationsForCurrentUser()
        val reminderNotifs = patientNotifs.filter { it.relatedEntityId == lateDose.id }
        assertTrue(reminderNotifs.size >= 2)
    }

    @Test
    fun testThemeAndColorBlindAccessibilityModes() {
        // Default theme and color-blind settings
        assertEquals(ThemeMode.SYSTEM, repository.themeMode.value)
        assertEquals(ColorBlindMode.OFF, repository.colorBlindMode.value)

        // Switching theme mode
        repository.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, repository.themeMode.value)

        repository.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, repository.themeMode.value)

        // Switching color-blind accessibility modes
        repository.setColorBlindMode(ColorBlindMode.PROTANOPIA_DEUTERANOPIA)
        assertEquals(ColorBlindMode.PROTANOPIA_DEUTERANOPIA, repository.colorBlindMode.value)

        // Verify Protanopia palette replaces green/red with blue/magenta
        val protanopiaLight = resolveMediGuardColors(isDark = false, mode = ColorBlindMode.PROTANOPIA_DEUTERANOPIA)
        val normalLight = resolveMediGuardColors(isDark = false, mode = ColorBlindMode.OFF)
        assertNotEquals(normalLight.statusVerified, protanopiaLight.statusVerified)
        assertNotEquals(normalLight.statusMissed, protanopiaLight.statusMissed)

        // Verify Tritanopia replaces pending yellow with violet
        val tritanopiaLight = resolveMediGuardColors(isDark = false, mode = ColorBlindMode.TRITANOPIA)
        assertNotEquals(normalLight.statusPending, tritanopiaLight.statusPending)

        // Verify High Contrast dark palette
        val highContrastDark = resolveMediGuardColors(isDark = true, mode = ColorBlindMode.HIGH_CONTRAST)
        assertTrue(highContrastDark.isDark)
        assertEquals(ColorBlindMode.HIGH_CONTRAST, highContrastDark.colorBlindMode)
    }
}
