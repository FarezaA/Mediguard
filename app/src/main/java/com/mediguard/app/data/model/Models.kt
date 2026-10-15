package com.mediguard.app.data.model

import java.util.UUID

enum class UserRole(val displayName: String) {
    PASIEN("Pasien"),
    PEMANTAU("Pemantau")
}

enum class ThemeMode(val displayName: String) {
    SYSTEM("Sistem"),
    LIGHT("Terang"),
    DARK("Gelap")
}

enum class ColorBlindMode(val displayName: String, val description: String) {
    OFF("Normal", "Palet warna medis standar"),
    PROTANOPIA_DEUTERANOPIA("Bebas Merah-Hijau", "Optimal untuk Protanopia & Deuteranopia"),
    TRITANOPIA("Bebas Biru-Kuning", "Optimal untuk Tritanopia"),
    HIGH_CONTRAST("Kontras Tinggi", "Kontras visual tinggi untuk keterbacaan maksimal")
}

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val password: String = "password123",
    val activeRole: UserRole = UserRole.PASIEN,
    val createdAt: String = "2026-10-12 08:00"
)

enum class RoomStatus {
    ACTIVE,
    CLOSED
}

data class MedicationRoom(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val ownerUserId: String,
    val ownerName: String,
    val inviteCode: String,
    val status: RoomStatus = RoomStatus.ACTIVE,
    val createdAt: String = "2026-10-12 08:00"
)

enum class MembershipStatus(val displayName: String) {
    PENDING("Menunggu Persetujuan"),
    APPROVED("Disetujui"),
    REJECTED("Ditolak"),
    REVOKED("Akses Dicabut")
}

data class RoomMember(
    val id: String = UUID.randomUUID().toString(),
    val roomId: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val role: UserRole = UserRole.PEMANTAU,
    val status: MembershipStatus = MembershipStatus.PENDING,
    val canViewSchedules: Boolean = true,
    val canViewActivities: Boolean = true,
    val canViewStats: Boolean = true,
    val requestedAt: String = "2026-10-12 09:00",
    val updatedAt: String = "2026-10-12 09:00"
)

data class Medication(
    val id: String = UUID.randomUUID().toString(),
    val roomId: String,
    val name: String,
    val form: String = "Tablet", // Tablet, Kapsul, Sirup, Tetes, etc.
    val dosage: String = "500 mg",
    val instructions: String = "Sesudah makan",
    val notes: String = "",
    val startDate: String = "2026-10-01",
    val endDate: String = "",
    val isArchived: Boolean = false,
    val createdAt: String = "2026-10-12 08:00"
)

data class MedicationSchedule(
    val id: String = UUID.randomUUID().toString(),
    val medicationId: String,
    val medicationName: String,
    val frequency: String = "1x sehari", // 1x sehari, 2x sehari, 3x sehari
    val times: List<String> = listOf("08:00"),
    val startDate: String = "2026-10-01",
    val endDate: String = "",
    val isActive: Boolean = true
)

enum class DoseStatus(val displayName: String) {
    TERJADWAL("Terjadwal"),
    MENUNGGU_LAPORAN("Menunggu laporan"),
    DILAPORKAN_DIMINUM("Dilaporkan diminum"),
    TERVERIFIKASI_LANGSUNG("Terverifikasi langsung"),
    TERLEWAT("Terlewat"),
    TERLAMBAT_DILAPORKAN("Terlambat dilaporkan"),
    DIBATALKAN("Dibatalkan")
}

data class DoseVerification(
    val id: String = UUID.randomUUID().toString(),
    val doseEventId: String,
    val verifierUserId: String,
    val verifierName: String,
    val verificationType: String = "Pengamatan Langsung",
    val verifiedAt: String,
    val statement: String = "Menyaksikan konsumsi secara langsung"
)

data class DoseEvent(
    val id: String = UUID.randomUUID().toString(),
    val scheduleId: String,
    val medicationId: String,
    val medicationName: String,
    val dosage: String,
    val instructions: String,
    val scheduledDate: String, // YYYY-MM-DD
    val scheduledTime: String, // HH:mm
    val status: DoseStatus = DoseStatus.TERJADWAL,
    val reportedByUserId: String? = null,
    val reportedByName: String? = null,
    val reportedAt: String? = null,
    val takenAt: String? = null,
    val photoUri: String? = null,
    val reminderCount: Int = 0,
    val snoozedUntil: String? = null,
    val notes: String? = null,
    val verification: DoseVerification? = null
)

enum class NotificationType {
    REMINDER,
    JOIN_REQUEST,
    JOIN_APPROVED,
    JOIN_REJECTED,
    DOSE_REPORTED,
    DOSE_VERIFIED,
    ACCESS_REVOKED
}

data class NotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val relatedEntityId: String? = null,
    val isRead: Boolean = false,
    val createdAt: String
)

data class AuditLog(
    val id: String = UUID.randomUUID().toString(),
    val roomId: String,
    val actorUserId: String,
    val actorName: String,
    val action: String,
    val entityType: String = "",
    val entityId: String = "",
    val details: String,
    val timestamp: String
)

data class AdherenceStats(
    val totalDue: Int,
    val reportedTaken: Int,
    val directlyVerified: Int,
    val missed: Int,
    val compliancePercentage: Double,
    val verifiedPercentage: Double
)
