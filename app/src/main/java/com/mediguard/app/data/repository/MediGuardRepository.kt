package com.mediguard.app.data.repository

import com.mediguard.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class MediGuardRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _rooms = MutableStateFlow<List<MedicationRoom>>(emptyList())
    val rooms: StateFlow<List<MedicationRoom>> = _rooms.asStateFlow()

    private val _roomMembers = MutableStateFlow<List<RoomMember>>(emptyList())
    val roomMembers: StateFlow<List<RoomMember>> = _roomMembers.asStateFlow()

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    private val _schedules = MutableStateFlow<List<MedicationSchedule>>(emptyList())
    val schedules: StateFlow<List<MedicationSchedule>> = _schedules.asStateFlow()

    private val _doseEvents = MutableStateFlow<List<DoseEvent>>(emptyList())
    val doseEvents: StateFlow<List<DoseEvent>> = _doseEvents.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    init {
        seedInitialDemoData()
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun seedInitialDemoData() {
        val patient = User(
            id = "user_patient_1",
            name = "Bapak Hendra",
            email = "hendra@mediguard.com",
            password = "password123",
            activeRole = UserRole.PASIEN,
            createdAt = "2026-10-10 08:00"
        )
        val caregiver = User(
            id = "user_caregiver_1",
            name = "Siti Rahma (Keluarga)",
            email = "siti@mediguard.com",
            password = "password123",
            activeRole = UserRole.PEMANTAU,
            createdAt = "2026-10-10 09:00"
        )
        val caregiver2 = User(
            id = "user_caregiver_2",
            name = "Budi Santoso (Adik)",
            email = "budi@mediguard.com",
            password = "password123",
            activeRole = UserRole.PEMANTAU,
            createdAt = "2026-10-11 10:00"
        )

        _users.value = listOf(patient, caregiver, caregiver2)
        _currentUser.value = patient

        val roomId = "room_1"
        val room = MedicationRoom(
            id = roomId,
            name = "Pemantauan Pengobatan Rutin Ayah",
            description = "Ruang pemantauan tensi & gula darah keluarga",
            ownerUserId = patient.id,
            ownerName = patient.name,
            inviteCode = "MG-8821",
            status = RoomStatus.ACTIVE,
            createdAt = "2026-10-10 10:00"
        )
        _rooms.value = listOf(room)

        val memberOwner = RoomMember(
            id = "member_1",
            roomId = roomId,
            userId = patient.id,
            userName = patient.name,
            userEmail = patient.email,
            role = UserRole.PASIEN,
            status = MembershipStatus.APPROVED,
            canViewSchedules = true,
            canViewActivities = true,
            canViewStats = true,
            requestedAt = "2026-10-10 10:00",
            updatedAt = "2026-10-10 10:00"
        )
        val memberCaregiverApproved = RoomMember(
            id = "member_2",
            roomId = roomId,
            userId = caregiver.id,
            userName = caregiver.name,
            userEmail = caregiver.email,
            role = UserRole.PEMANTAU,
            status = MembershipStatus.APPROVED,
            canViewSchedules = true,
            canViewActivities = true,
            canViewStats = true,
            requestedAt = "2026-10-10 11:00",
            updatedAt = "2026-10-10 11:30"
        )
        val memberCaregiverPending = RoomMember(
            id = "member_3",
            roomId = roomId,
            userId = caregiver2.id,
            userName = caregiver2.name,
            userEmail = caregiver2.email,
            role = UserRole.PEMANTAU,
            status = MembershipStatus.PENDING,
            canViewSchedules = true,
            canViewActivities = true,
            canViewStats = true,
            requestedAt = "2026-10-12 07:30",
            updatedAt = "2026-10-12 07:30"
        )
        _roomMembers.value = listOf(memberOwner, memberCaregiverApproved, memberCaregiverPending)

        val med1 = Medication(
            id = "med_1",
            roomId = roomId,
            name = "Amlodipine",
            form = "Tablet",
            dosage = "5 mg",
            instructions = "1x sehari pagi sesudah makan - untuk tekanan darah",
            notes = "Diminum rutin setiap pagi",
            startDate = "2026-10-01",
            isArchived = false
        )
        val med2 = Medication(
            id = "med_2",
            roomId = roomId,
            name = "Metformin",
            form = "Tablet",
            dosage = "500 mg",
            instructions = "2x sehari sesudah makan (pagi & malam) - untuk gula darah",
            notes = "Konsumsi bersama makanan untuk mencegah mual",
            startDate = "2026-10-01",
            isArchived = false
        )
        val med3 = Medication(
            id = "med_3",
            roomId = roomId,
            name = "Atorvastatin",
            form = "Kapsul",
            dosage = "20 mg",
            instructions = "1x sehari malam sebelum tidur - untuk kolesterol",
            notes = "Malam hari",
            startDate = "2026-10-01",
            isArchived = false
        )
        _medications.value = listOf(med1, med2, med3)

        val sched1 = MedicationSchedule(
            id = "sched_1",
            medicationId = med1.id,
            medicationName = med1.name,
            frequency = "1x sehari",
            times = listOf("08:00"),
            startDate = "2026-10-01",
            isActive = true
        )
        val sched2 = MedicationSchedule(
            id = "sched_2",
            medicationId = med2.id,
            medicationName = med2.name,
            frequency = "2x sehari",
            times = listOf("08:00", "19:00"),
            startDate = "2026-10-01",
            isActive = true
        )
        val sched3 = MedicationSchedule(
            id = "sched_3",
            medicationId = med3.id,
            medicationName = med3.name,
            frequency = "1x sehari",
            times = listOf("21:00"),
            startDate = "2026-10-01",
            isActive = true
        )
        _schedules.value = listOf(sched1, sched2, sched3)

        val today = getTodayDate()
        val dose1 = DoseEvent(
            id = "dose_1",
            scheduleId = sched1.id,
            medicationId = med1.id,
            medicationName = med1.name,
            dosage = med1.dosage,
            instructions = med1.instructions,
            scheduledDate = today,
            scheduledTime = "08:00",
            status = DoseStatus.DILAPORKAN_DIMINUM,
            reportedByUserId = patient.id,
            reportedByName = patient.name,
            reportedAt = "$today 08:15",
            takenAt = "$today 08:10",
            photoUri = "demo_photo_amlodipine.jpg",
            verification = null
        )
        val dose2 = DoseEvent(
            id = "dose_2",
            scheduleId = sched2.id,
            medicationId = med2.id,
            medicationName = med2.name,
            dosage = med2.dosage,
            instructions = med2.instructions,
            scheduledDate = today,
            scheduledTime = "08:00",
            status = DoseStatus.TERVERIFIKASI_LANGSUNG,
            reportedByUserId = patient.id,
            reportedByName = patient.name,
            reportedAt = "$today 08:10",
            takenAt = "$today 08:05",
            photoUri = "demo_photo_metformin.jpg",
            verification = DoseVerification(
                id = "verif_1",
                doseEventId = "dose_2",
                verifierUserId = caregiver.id,
                verifierName = caregiver.name,
                verificationType = "Pengamatan Langsung",
                verifiedAt = "$today 08:12",
                statement = "Menyaksikan konsumsi secara langsung saat sarapan bersama"
            )
        )
        val dose3 = DoseEvent(
            id = "dose_3",
            scheduleId = sched2.id,
            medicationId = med2.id,
            medicationName = med2.name,
            dosage = med2.dosage,
            instructions = med2.instructions,
            scheduledDate = today,
            scheduledTime = "19:00",
            status = DoseStatus.MENUNGGU_LAPORAN,
            reminderCount = 1,
            reportedByUserId = null
        )
        val dose4 = DoseEvent(
            id = "dose_4",
            scheduleId = sched3.id,
            medicationId = med3.id,
            medicationName = med3.name,
            dosage = med3.dosage,
            instructions = med3.instructions,
            scheduledDate = today,
            scheduledTime = "21:00",
            status = DoseStatus.TERJADWAL,
            reportedByUserId = null
        )
        _doseEvents.value = listOf(dose1, dose2, dose3, dose4)

        val notif1 = NotificationItem(
            id = "notif_1",
            userId = patient.id,
            title = "Permintaan Bergabung Baru",
            body = "${caregiver2.name} ingin bergabung ke ruang '${room.name}'",
            type = NotificationType.JOIN_REQUEST,
            relatedEntityId = memberCaregiverPending.id,
            isRead = false,
            createdAt = "$today 07:30"
        )
        val notif2 = NotificationItem(
            id = "notif_2",
            userId = caregiver.id,
            title = "Laporan Konsumsi Obat",
            body = "${patient.name} telah melaporkan minum ${med1.name}. Menunggu verifikasi jika Anda menyaksikan langsung.",
            type = NotificationType.DOSE_REPORTED,
            relatedEntityId = dose1.id,
            isRead = false,
            createdAt = "$today 08:15"
        )
        val notif3 = NotificationItem(
            id = "notif_3",
            userId = patient.id,
            title = "Pengingat Jadwal Minum Obat",
            body = "Waktu konsumsi ${med2.name} tiba pada pukul 19:00.",
            type = NotificationType.REMINDER,
            relatedEntityId = dose3.id,
            isRead = false,
            createdAt = "$today 19:00"
        )
        _notifications.value = listOf(notif1, notif2, notif3)

        val log1 = AuditLog(
            id = "log_1",
            roomId = roomId,
            actorUserId = patient.id,
            actorName = patient.name,
            action = "CREATE_ROOM",
            entityType = "MedicationRoom",
            entityId = roomId,
            details = "Membuat ruang pemantauan '${room.name}' dengan kode ${room.inviteCode}",
            timestamp = "2026-10-10 10:00"
        )
        val log2 = AuditLog(
            id = "log_2",
            roomId = roomId,
            actorUserId = caregiver2.id,
            actorName = caregiver2.name,
            action = "JOIN_REQUEST",
            entityType = "RoomMember",
            entityId = memberCaregiverPending.id,
            details = "Mengajukan permintaan bergabung ke ruang pemantauan",
            timestamp = "$today 07:30"
        )
        val log3 = AuditLog(
            id = "log_3",
            roomId = roomId,
            actorUserId = patient.id,
            actorName = patient.name,
            action = "REPORT_DOSE_TAKEN",
            entityType = "DoseEvent",
            entityId = dose1.id,
            details = "Melaporkan telah mengonsumsi ${med1.name} 5 mg",
            timestamp = "$today 08:15"
        )
        val log4 = AuditLog(
            id = "log_4",
            roomId = roomId,
            actorUserId = caregiver.id,
            actorName = caregiver.name,
            action = "VERIFY_DOSE",
            entityType = "DoseEvent",
            entityId = dose2.id,
            details = "Memverifikasi langsung konsumsi ${med2.name} 500 mg",
            timestamp = "$today 08:12"
        )
        _auditLogs.value = listOf(log1, log2, log3, log4)
    }

    // ---------------- AUTHENTICATION ----------------

    fun register(name: String, email: String, password: String, role: UserRole): Result<User> {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Semua bidang harus diisi."))
        }
        if (!email.contains("@")) {
            return Result.failure(IllegalArgumentException("Format email tidak valid."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Kata sandi minimal 6 karakter."))
        }
        val existing = _users.value.find { it.email.equals(email.trim(), ignoreCase = true) }
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Email sudah terdaftar."))
        }
        val newUser = User(
            name = name.trim(),
            email = email.trim(),
            password = password,
            activeRole = role,
            createdAt = getCurrentTimestamp()
        )
        _users.value = _users.value + newUser
        _currentUser.value = newUser
        return Result.success(newUser)
    }

    fun login(email: String, password: String): Result<User> {
        val user = _users.value.find { it.email.equals(email.trim(), ignoreCase = true) }
        if (user == null || user.password != password) {
            return Result.failure(IllegalArgumentException("Email atau kata sandi tidak sesuai."))
        }
        _currentUser.value = user
        return Result.success(user)
    }

    fun logout() {
        _currentUser.value = null
    }

    fun switchRole(newRole: UserRole) {
        val current = _currentUser.value ?: return
        val updated = current.copy(activeRole = newRole)
        _currentUser.value = updated
        _users.value = _users.value.map { if (it.id == updated.id) updated else it }
    }

    fun switchUser(userId: String) {
        val target = _users.value.find { it.id == userId } ?: return
        _currentUser.value = target
    }

    fun resetPassword(email: String): String {
        return "Jika email $email terdaftar, tautan pemulihan kata sandi telah dikirim ke email tersebut."
    }

    fun updateProfile(name: String, email: String): Result<User> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val updated = current.copy(name = name.trim(), email = email.trim())
        _currentUser.value = updated
        _users.value = _users.value.map { if (it.id == updated.id) updated else it }
        return Result.success(updated)
    }

    // ---------------- MEDICATION ROOMS ----------------

    fun createRoom(name: String, description: String): Result<MedicationRoom> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Nama ruang tidak boleh kosong."))
        }
        val code = "MG-" + (1000..9999).random()
        val newRoom = MedicationRoom(
            name = name.trim(),
            description = description.trim(),
            ownerUserId = current.id,
            ownerName = current.name,
            inviteCode = code,
            createdAt = getCurrentTimestamp()
        )
        _rooms.value = _rooms.value + newRoom

        // Add owner as member
        val ownerMember = RoomMember(
            roomId = newRoom.id,
            userId = current.id,
            userName = current.name,
            userEmail = current.email,
            role = UserRole.PASIEN,
            status = MembershipStatus.APPROVED,
            canViewSchedules = true,
            canViewActivities = true,
            canViewStats = true,
            requestedAt = getCurrentTimestamp(),
            updatedAt = getCurrentTimestamp()
        )
        _roomMembers.value = _roomMembers.value + ownerMember

        logAudit(
            roomId = newRoom.id,
            actor = current,
            action = "CREATE_ROOM",
            entityType = "MedicationRoom",
            entityId = newRoom.id,
            details = "Membuat Medication Room '${newRoom.name}' dengan kode $code"
        )

        return Result.success(newRoom)
    }

    fun rotateInviteCode(roomId: String): Result<String> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val room = _rooms.value.find { it.id == roomId } ?: return Result.failure(IllegalArgumentException("Ruang tidak ditemukan"))
        if (room.ownerUserId != current.id) {
            return Result.failure(SecurityException("Hanya pemilik ruang yang dapat memperbarui kode."))
        }
        val newCode = "MG-" + (1000..9999).random()
        val updatedRoom = room.copy(inviteCode = newCode)
        _rooms.value = _rooms.value.map { if (it.id == roomId) updatedRoom else it }

        logAudit(
            roomId = roomId,
            actor = current,
            action = "ROTATE_INVITE_CODE",
            entityType = "MedicationRoom",
            entityId = roomId,
            details = "Memperbarui kode undangan menjadi $newCode"
        )
        return Result.success(newCode)
    }

    fun findRoomPreviewByCode(code: String): Result<MedicationRoom> {
        val cleanCode = code.trim().uppercase()
        val room = _rooms.value.find { it.inviteCode.equals(cleanCode, ignoreCase = true) && it.status == RoomStatus.ACTIVE }
        return if (room != null) {
            // Safe preview: only basic metadata, never exposes meds or private data
            Result.success(room)
        } else {
            Result.failure(IllegalArgumentException("Kode undangan tidak valid atau ruang sudah tidak aktif."))
        }
    }

    fun requestJoinRoom(inviteCode: String): Result<RoomMember> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val roomResult = findRoomPreviewByCode(inviteCode)
        if (roomResult.isFailure) {
            return Result.failure(roomResult.exceptionOrNull() ?: IllegalArgumentException("Ruang tidak valid"))
        }
        val room = roomResult.getOrThrow()

        if (room.ownerUserId == current.id) {
            return Result.failure(IllegalArgumentException("Anda adalah pemilik ruang ini."))
        }

        val existingMember = _roomMembers.value.find { it.roomId == room.id && it.userId == current.id }
        if (existingMember != null) {
            return when (existingMember.status) {
                MembershipStatus.APPROVED -> Result.failure(IllegalArgumentException("Anda sudah menjadi anggota ruang ini."))
                MembershipStatus.PENDING -> Result.failure(IllegalArgumentException("Permintaan bergabung Anda sedang menunggu persetujuan pasien."))
                MembershipStatus.REVOKED -> Result.failure(IllegalArgumentException("Akses Anda ke ruang ini telah dicabut oleh pasien."))
                MembershipStatus.REJECTED -> Result.failure(IllegalArgumentException("Permintaan bergabung sebelumnya telah ditolak."))
            }
        }

        val newMember = RoomMember(
            roomId = room.id,
            userId = current.id,
            userName = current.name,
            userEmail = current.email,
            role = UserRole.PEMANTAU,
            status = MembershipStatus.PENDING,
            requestedAt = getCurrentTimestamp(),
            updatedAt = getCurrentTimestamp()
        )
        _roomMembers.value = _roomMembers.value + newMember

        // Send notification to room owner
        val notif = NotificationItem(
            userId = room.ownerUserId,
            title = "Permintaan Bergabung Baru",
            body = "${current.name} meminta izin untuk memantau ruang '${room.name}'.",
            type = NotificationType.JOIN_REQUEST,
            relatedEntityId = newMember.id,
            isRead = false,
            createdAt = getCurrentTimestamp()
        )
        _notifications.value = _notifications.value + notif

        logAudit(
            roomId = room.id,
            actor = current,
            action = "JOIN_REQUEST",
            entityType = "RoomMember",
            entityId = newMember.id,
            details = "${current.name} mengajukan permintaan bergabung menggunakan kode"
        )

        return Result.success(newMember)
    }

    fun approveMember(memberId: String): Result<Unit> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val member = _roomMembers.value.find { it.id == memberId } ?: return Result.failure(IllegalArgumentException("Anggota tidak ditemukan"))
        val room = _rooms.value.find { it.id == member.roomId } ?: return Result.failure(IllegalArgumentException("Ruang tidak ditemukan"))

        if (room.ownerUserId != current.id) {
            return Result.failure(SecurityException("Hanya pemilik ruang yang dapat menyetujui anggota."))
        }

        val updated = member.copy(
            status = MembershipStatus.APPROVED,
            updatedAt = getCurrentTimestamp()
        )
        _roomMembers.value = _roomMembers.value.map { if (it.id == memberId) updated else it }

        // Notify caregiver
        val notif = NotificationItem(
            userId = member.userId,
            title = "Permintaan Diterima",
            body = "Permintaan bergabung ke ruang '${room.name}' telah disetujui oleh ${current.name}.",
            type = NotificationType.JOIN_APPROVED,
            relatedEntityId = room.id,
            isRead = false,
            createdAt = getCurrentTimestamp()
        )
        _notifications.value = _notifications.value + notif

        logAudit(
            roomId = room.id,
            actor = current,
            action = "APPROVE_MEMBER",
            entityType = "RoomMember",
            entityId = memberId,
            details = "Menyetujui akses pemantauan untuk ${member.userName}"
        )
        return Result.success(Unit)
    }

    fun rejectMember(memberId: String): Result<Unit> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val member = _roomMembers.value.find { it.id == memberId } ?: return Result.failure(IllegalArgumentException("Anggota tidak ditemukan"))
        val room = _rooms.value.find { it.id == member.roomId } ?: return Result.failure(IllegalArgumentException("Ruang tidak ditemukan"))

        if (room.ownerUserId != current.id) {
            return Result.failure(SecurityException("Hanya pemilik ruang yang dapat menolak anggota."))
        }

        val updated = member.copy(
            status = MembershipStatus.REJECTED,
            updatedAt = getCurrentTimestamp()
        )
        _roomMembers.value = _roomMembers.value.map { if (it.id == memberId) updated else it }

        val notif = NotificationItem(
            userId = member.userId,
            title = "Permintaan Ditolak",
            body = "Permintaan bergabung ke ruang '${room.name}' tidak disetujui oleh pasien.",
            type = NotificationType.JOIN_REJECTED,
            relatedEntityId = room.id,
            isRead = false,
            createdAt = getCurrentTimestamp()
        )
        _notifications.value = _notifications.value + notif

        logAudit(
            roomId = room.id,
            actor = current,
            action = "REJECT_MEMBER",
            entityType = "RoomMember",
            entityId = memberId,
            details = "Menolak permohonan akses dari ${member.userName}"
        )
        return Result.success(Unit)
    }

    fun revokeMemberAccess(memberId: String): Result<Unit> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val member = _roomMembers.value.find { it.id == memberId } ?: return Result.failure(IllegalArgumentException("Anggota tidak ditemukan"))
        val room = _rooms.value.find { it.id == member.roomId } ?: return Result.failure(IllegalArgumentException("Ruang tidak ditemukan"))

        if (room.ownerUserId != current.id) {
            return Result.failure(SecurityException("Hanya pemilik ruang yang dapat mencabut akses anggota."))
        }

        val updated = member.copy(
            status = MembershipStatus.REVOKED,
            updatedAt = getCurrentTimestamp()
        )
        _roomMembers.value = _roomMembers.value.map { if (it.id == memberId) updated else it }

        val notif = NotificationItem(
            userId = member.userId,
            title = "Akses Dicabut",
            body = "Akses pemantauan Anda ke ruang '${room.name}' telah dicabut oleh pasien.",
            type = NotificationType.ACCESS_REVOKED,
            relatedEntityId = room.id,
            isRead = false,
            createdAt = getCurrentTimestamp()
        )
        _notifications.value = _notifications.value + notif

        logAudit(
            roomId = room.id,
            actor = current,
            action = "REVOKE_MEMBER",
            entityType = "RoomMember",
            entityId = memberId,
            details = "Mencabut izin akses untuk ${member.userName}"
        )
        return Result.success(Unit)
    }

    fun updateMemberPermissions(
        memberId: String,
        canViewSchedules: Boolean,
        canViewActivities: Boolean,
        canViewStats: Boolean
    ): Result<Unit> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val member = _roomMembers.value.find { it.id == memberId } ?: return Result.failure(IllegalArgumentException("Anggota tidak ditemukan"))
        val room = _rooms.value.find { it.id == member.roomId } ?: return Result.failure(IllegalArgumentException("Ruang tidak ditemukan"))

        if (room.ownerUserId != current.id) {
            return Result.failure(SecurityException("Hanya pemilik ruang yang dapat mengubah izin."))
        }

        val updated = member.copy(
            canViewSchedules = canViewSchedules,
            canViewActivities = canViewActivities,
            canViewStats = canViewStats,
            updatedAt = getCurrentTimestamp()
        )
        _roomMembers.value = _roomMembers.value.map { if (it.id == memberId) updated else it }

        logAudit(
            roomId = room.id,
            actor = current,
            action = "UPDATE_PERMISSIONS",
            entityType = "RoomMember",
            entityId = memberId,
            details = "Mengubah izin akses ${member.userName}: Jadwal=$canViewSchedules, Aktivitas=$canViewActivities, Statistik=$canViewStats"
        )
        return Result.success(Unit)
    }

    fun getRoomsForUser(userId: String): List<MedicationRoom> {
        val user = _users.value.find { it.id == userId } ?: return emptyList()
        return if (user.activeRole == UserRole.PASIEN) {
            _rooms.value.filter { it.ownerUserId == userId }
        } else {
            val memberships = _roomMembers.value.filter { it.userId == userId }
            val roomIds = memberships.map { it.roomId }
            _rooms.value.filter { roomIds.contains(it.id) }
        }
    }

    fun getMembershipForUserInRoom(roomId: String, userId: String): RoomMember? {
        return _roomMembers.value.find { it.roomId == roomId && it.userId == userId }
    }

    fun getMembersForRoom(roomId: String): List<RoomMember> {
        return _roomMembers.value.filter { it.roomId == roomId }
    }

    // ---------------- MEDICATIONS & SCHEDULES ----------------

    fun addMedication(
        roomId: String,
        name: String,
        form: String,
        dosage: String,
        instructions: String,
        notes: String,
        startDate: String,
        endDate: String
    ): Result<Medication> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val room = _rooms.value.find { it.id == roomId } ?: return Result.failure(IllegalArgumentException("Ruang tidak ditemukan"))
        if (room.ownerUserId != current.id) {
            return Result.failure(SecurityException("Hanya pemilik ruang yang dapat menambah obat."))
        }
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Nama obat tidak boleh kosong."))
        }

        val newMed = Medication(
            roomId = roomId,
            name = name.trim(),
            form = form.trim(),
            dosage = dosage.trim(),
            instructions = instructions.trim(),
            notes = notes.trim(),
            startDate = startDate.ifBlank { getTodayDate() },
            endDate = endDate.trim(),
            createdAt = getCurrentTimestamp()
        )
        _medications.value = _medications.value + newMed

        logAudit(
            roomId = roomId,
            actor = current,
            action = "ADD_MEDICATION",
            entityType = "Medication",
            entityId = newMed.id,
            details = "Menambahkan obat baru: ${newMed.name} ($form, $dosage)"
        )
        return Result.success(newMed)
    }

    fun updateMedication(medication: Medication): Result<Medication> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val room = _rooms.value.find { it.id == medication.roomId } ?: return Result.failure(IllegalArgumentException("Ruang tidak ditemukan"))
        if (room.ownerUserId != current.id) {
            return Result.failure(SecurityException("Hanya pemilik ruang yang dapat mengubah obat."))
        }
        _medications.value = _medications.value.map { if (it.id == medication.id) medication else it }
        logAudit(
            roomId = room.id,
            actor = current,
            action = "UPDATE_MEDICATION",
            entityType = "Medication",
            entityId = medication.id,
            details = "Memperbarui obat: ${medication.name}"
        )
        return Result.success(medication)
    }

    fun archiveMedication(medicationId: String): Result<Unit> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val med = _medications.value.find { it.id == medicationId } ?: return Result.failure(IllegalArgumentException("Obat tidak ditemukan"))
        val room = _rooms.value.find { it.id == med.roomId } ?: return Result.failure(IllegalArgumentException("Ruang tidak ditemukan"))
        if (room.ownerUserId != current.id) {
            return Result.failure(SecurityException("Hanya pemilik ruang yang dapat mengarsipkan obat."))
        }

        val updated = med.copy(isArchived = true)
        _medications.value = _medications.value.map { if (it.id == medicationId) updated else it }
        logAudit(
            roomId = room.id,
            actor = current,
            action = "ARCHIVE_MEDICATION",
            entityType = "Medication",
            entityId = medicationId,
            details = "Mengarsipkan obat: ${med.name}"
        )
        return Result.success(Unit)
    }

    fun addSchedule(
        medicationId: String,
        frequency: String,
        times: List<String>,
        startDate: String,
        endDate: String
    ): Result<MedicationSchedule> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val med = _medications.value.find { it.id == medicationId } ?: return Result.failure(IllegalArgumentException("Obat tidak ditemukan"))
        val room = _rooms.value.find { it.id == med.roomId } ?: return Result.failure(IllegalArgumentException("Ruang tidak ditemukan"))
        if (room.ownerUserId != current.id) {
            return Result.failure(SecurityException("Hanya pemilik ruang yang dapat mengatur jadwal."))
        }

        val newSched = MedicationSchedule(
            medicationId = medicationId,
            medicationName = med.name,
            frequency = frequency,
            times = times,
            startDate = startDate.ifBlank { getTodayDate() },
            endDate = endDate,
            isActive = true
        )
        _schedules.value = _schedules.value + newSched

        // Generate dose events for today
        val today = getTodayDate()
        val newEvents = times.map { timeStr ->
            DoseEvent(
                scheduleId = newSched.id,
                medicationId = med.id,
                medicationName = med.name,
                dosage = med.dosage,
                instructions = med.instructions,
                scheduledDate = today,
                scheduledTime = timeStr,
                status = DoseStatus.TERJADWAL
            )
        }
        _doseEvents.value = _doseEvents.value + newEvents

        logAudit(
            roomId = room.id,
            actor = current,
            action = "ADD_SCHEDULE",
            entityType = "MedicationSchedule",
            entityId = newSched.id,
            details = "Membuat jadwal ${med.name} $frequency pada jam: ${times.joinToString(", ")}"
        )
        return Result.success(newSched)
    }

    // ---------------- DOSE CONSUMPTION & VERIFICATION ----------------

    fun reportDoseTaken(doseEventId: String, takenAt: String, photoUri: String? = null): Result<DoseEvent> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val event = _doseEvents.value.find { it.id == doseEventId } ?: return Result.failure(IllegalArgumentException("Jadwal dosis tidak ditemukan"))
        val med = _medications.value.find { it.id == event.medicationId } ?: return Result.failure(IllegalArgumentException("Obat tidak ditemukan"))
        val room = _rooms.value.find { it.id == med.roomId } ?: return Result.failure(IllegalArgumentException("Ruang tidak ditemukan"))

        val now = getCurrentTimestamp()
        val updated = event.copy(
            status = DoseStatus.DILAPORKAN_DIMINUM,
            reportedByUserId = current.id,
            reportedByName = current.name,
            reportedAt = now,
            takenAt = takenAt.ifBlank { now },
            photoUri = photoUri
        )
        _doseEvents.value = _doseEvents.value.map { if (it.id == doseEventId) updated else it }

        // Notify approved caregivers
        val approvedCaregivers = _roomMembers.value.filter {
            it.roomId == room.id && it.role == UserRole.PEMANTAU && it.status == MembershipStatus.APPROVED
        }
        val photoInfo = if (photoUri != null) " dengan bukti foto" else ""
        approvedCaregivers.forEach { caregiver ->
            val notif = NotificationItem(
                userId = caregiver.userId,
                title = "Laporan Minum Obat",
                body = "${current.name} telah melaporkan minum ${event.medicationName} (${event.dosage})$photoInfo. Silakan periksa foto dan verifikasi jika Anda menyaksikan langsung.",
                type = NotificationType.DOSE_REPORTED,
                relatedEntityId = doseEventId,
                isRead = false,
                createdAt = now
            )
            _notifications.value = _notifications.value + notif
        }

        logAudit(
            roomId = room.id,
            actor = current,
            action = "REPORT_DOSE_TAKEN",
            entityType = "DoseEvent",
            entityId = doseEventId,
            details = "Melaporkan minum ${event.medicationName} (${event.dosage}) pada $now$photoInfo"
        )
        return Result.success(updated)
    }

    fun sendCaregiverReminder(doseEventId: String): Result<Int> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val event = _doseEvents.value.find { it.id == doseEventId } ?: return Result.failure(IllegalArgumentException("Jadwal dosis tidak ditemukan"))
        val med = _medications.value.find { it.id == event.medicationId } ?: return Result.failure(IllegalArgumentException("Obat tidak ditemukan"))
        val room = _rooms.value.find { it.id == med.roomId } ?: return Result.failure(IllegalArgumentException("Ruang tidak ditemukan"))

        val membership = _roomMembers.value.find { it.roomId == room.id && it.userId == current.id }
        if (membership == null || membership.status != MembershipStatus.APPROVED) {
            return Result.failure(SecurityException("Hanya pemantau yang disetujui yang dapat mengirim pengingat."))
        }

        val newCount = event.reminderCount + 1
        val updated = event.copy(reminderCount = newCount)
        _doseEvents.value = _doseEvents.value.map { if (it.id == doseEventId) updated else it }

        val now = getCurrentTimestamp()
        val notif = NotificationItem(
            userId = room.ownerUserId,
            title = "Pengingat dari Pemantau (${current.name})",
            body = "Pengingat ke-$newCount: Anda belum melaporkan minum obat ${event.medicationName} (${event.dosage}) jadwal ${event.scheduledTime}. Segera minum dan kirimkan bukti foto!",
            type = NotificationType.REMINDER,
            relatedEntityId = doseEventId,
            isRead = false,
            createdAt = now
        )
        _notifications.value = _notifications.value + notif

        logAudit(
            roomId = room.id,
            actor = current,
            action = "SEND_REMINDER",
            entityType = "DoseEvent",
            entityId = doseEventId,
            details = "${current.name} mengirim pengingat ke-$newCount untuk ${event.medicationName} kepada pasien"
        )
        return Result.success(newCount)
    }

    fun reportDoseMissed(doseEventId: String, reason: String = ""): Result<DoseEvent> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val event = _doseEvents.value.find { it.id == doseEventId } ?: return Result.failure(IllegalArgumentException("Jadwal dosis tidak ditemukan"))
        val med = _medications.value.find { it.id == event.medicationId } ?: return Result.failure(IllegalArgumentException("Obat tidak ditemukan"))
        val room = _rooms.value.find { it.id == med.roomId } ?: return Result.failure(IllegalArgumentException("Ruang tidak ditemukan"))

        val now = getCurrentTimestamp()
        val updated = event.copy(
            status = DoseStatus.TERLEWAT,
            reportedByUserId = current.id,
            reportedByName = current.name,
            reportedAt = now,
            notes = reason
        )
        _doseEvents.value = _doseEvents.value.map { if (it.id == doseEventId) updated else it }

        // Notify caregivers
        val approvedCaregivers = _roomMembers.value.filter {
            it.roomId == room.id && it.role == UserRole.PEMANTAU && it.status == MembershipStatus.APPROVED
        }
        approvedCaregivers.forEach { caregiver ->
            val notif = NotificationItem(
                userId = caregiver.userId,
                title = "Dosis Obat Terlewat",
                body = "Perhatian: ${current.name} mencatat jadwal ${event.medicationName} sebagai TERLEWAT.",
                type = NotificationType.DOSE_REPORTED,
                relatedEntityId = doseEventId,
                isRead = false,
                createdAt = now
            )
            _notifications.value = _notifications.value + notif
        }

        logAudit(
            roomId = room.id,
            actor = current,
            action = "REPORT_DOSE_MISSED",
            entityType = "DoseEvent",
            entityId = doseEventId,
            details = "Mencatat dosis ${event.medicationName} TERLEWAT. Alasan: ${reason.ifBlank { "Tidak ada" }}"
        )
        return Result.success(updated)
    }

    fun snoozeDose(doseEventId: String, minutes: Int): Result<DoseEvent> {
        val event = _doseEvents.value.find { it.id == doseEventId } ?: return Result.failure(IllegalArgumentException("Jadwal dosis tidak ditemukan"))
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, minutes)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val snoozedTime = sdf.format(calendar.time)

        val updated = event.copy(
            snoozedUntil = snoozedTime
        )
        _doseEvents.value = _doseEvents.value.map { if (it.id == doseEventId) updated else it }
        return Result.success(updated)
    }

    fun verifyDoseDirectObservation(
        doseEventId: String,
        directObservationConfirmed: Boolean,
        notes: String = ""
    ): Result<DoseEvent> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        if (!directObservationConfirmed) {
            return Result.failure(IllegalArgumentException("Verifikasi mewajibkan pemantau menyatakan menyaksikan konsumsi secara langsung."))
        }

        val event = _doseEvents.value.find { it.id == doseEventId } ?: return Result.failure(IllegalArgumentException("Dosis tidak ditemukan"))
        val med = _medications.value.find { it.id == event.medicationId } ?: return Result.failure(IllegalArgumentException("Obat tidak ditemukan"))
        val room = _rooms.value.find { it.id == med.roomId } ?: return Result.failure(IllegalArgumentException("Ruang tidak ditemukan"))

        // Check if current user is approved caregiver in this room
        val membership = _roomMembers.value.find { it.roomId == room.id && it.userId == current.id }
        if (membership == null || membership.status != MembershipStatus.APPROVED) {
            return Result.failure(SecurityException("Anda tidak memiliki izin verifikasi di ruang ini."))
        }

        val now = getCurrentTimestamp()
        val verification = DoseVerification(
            doseEventId = doseEventId,
            verifierUserId = current.id,
            verifierName = current.name,
            verificationType = "Pengamatan Langsung",
            verifiedAt = now,
            statement = "Menyaksikan konsumsi secara langsung. ${notes.trim()}".trim()
        )

        val updated = event.copy(
            status = DoseStatus.TERVERIFIKASI_LANGSUNG,
            verification = verification
        )
        _doseEvents.value = _doseEvents.value.map { if (it.id == doseEventId) updated else it }

        // Notify patient
        val notif = NotificationItem(
            userId = room.ownerUserId,
            title = "Konsumsi Obat Terverifikasi",
            body = "${current.name} telah memverifikasi secara langsung konsumsi ${event.medicationName}.",
            type = NotificationType.DOSE_VERIFIED,
            relatedEntityId = doseEventId,
            isRead = false,
            createdAt = now
        )
        _notifications.value = _notifications.value + notif

        logAudit(
            roomId = room.id,
            actor = current,
            action = "VERIFY_DOSE",
            entityType = "DoseEvent",
            entityId = doseEventId,
            details = "Memverifikasi langsung konsumsi ${event.medicationName} (${event.dosage})"
        )
        return Result.success(updated)
    }

    // ---------------- AUDIT LOGS & ACTIVITIES ----------------

    private fun logAudit(
        roomId: String,
        actor: User,
        action: String,
        entityType: String,
        entityId: String,
        details: String
    ) {
        val log = AuditLog(
            roomId = roomId,
            actorUserId = actor.id,
            actorName = actor.name,
            action = action,
            entityType = entityType,
            entityId = entityId,
            details = details,
            timestamp = getCurrentTimestamp()
        )
        _auditLogs.value = _auditLogs.value + log
    }

    fun getAuditLogsForRoom(roomId: String): List<AuditLog> {
        return _auditLogs.value.filter { it.roomId == roomId }.sortedByDescending { it.timestamp }
    }

    // ---------------- NOTIFICATIONS ----------------

    fun getNotificationsForCurrentUser(): List<NotificationItem> {
        val current = _currentUser.value ?: return emptyList()
        return _notifications.value.filter { it.userId == current.id }.sortedByDescending { it.createdAt }
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsAsRead() {
        val current = _currentUser.value ?: return
        _notifications.value = _notifications.value.map {
            if (it.userId == current.id) it.copy(isRead = true) else it
        }
    }

    // ---------------- STATISTICS ----------------

    fun getAdherenceStats(roomId: String? = null): AdherenceStats {
        val events = if (roomId != null) {
            val medIds = _medications.value.filter { it.roomId == roomId }.map { it.id }
            _doseEvents.value.filter { medIds.contains(it.medicationId) }
        } else {
            _doseEvents.value
        }

        val totalDue = events.count { it.status != DoseStatus.TERJADWAL && it.status != DoseStatus.DIBATALKAN }
        val reportedTaken = events.count {
            it.status == DoseStatus.DILAPORKAN_DIMINUM ||
                    it.status == DoseStatus.TERVERIFIKASI_LANGSUNG ||
                    it.status == DoseStatus.TERLAMBAT_DILAPORKAN
        }
        val directlyVerified = events.count { it.status == DoseStatus.TERVERIFIKASI_LANGSUNG }
        val missed = events.count { it.status == DoseStatus.TERLEWAT }

        val denom = if (totalDue > 0) totalDue.toDouble() else 1.0
        val compliancePct = (reportedTaken.toDouble() / denom) * 100.0
        val verifiedPct = (directlyVerified.toDouble() / denom) * 100.0

        return AdherenceStats(
            totalDue = totalDue,
            reportedTaken = reportedTaken,
            directlyVerified = directlyVerified,
            missed = missed,
            compliancePercentage = Math.round(compliancePct * 10.0) / 10.0,
            verifiedPercentage = Math.round(verifiedPct * 10.0) / 10.0
        )
    }
}
