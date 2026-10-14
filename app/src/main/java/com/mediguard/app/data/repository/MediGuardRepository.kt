package com.mediguard.app.data.repository

import android.graphics.Bitmap
import com.mediguard.app.data.model.*
import com.mediguard.app.data.network.MediGuardApiClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MediGuardRepository {

    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _serverUrl = MutableStateFlow(MediGuardApiClient.baseUrl)
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

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
        // 1. Seed initial local demo data so the app is instantly usable offline
        seedInitialDemoData()

        // 2. Start continuous real-time online synchronization loop
        startOnlineSync()
    }

    fun setServerUrl(newUrl: String) {
        var formatted = newUrl.trim()
        if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
            formatted = "http://$formatted"
        }
        if (formatted.endsWith("/")) {
            formatted = formatted.dropLast(1)
        }
        MediGuardApiClient.baseUrl = formatted
        _serverUrl.value = formatted

        // Trigger immediate health check & sync
        syncScope.launch {
            triggerSync()
        }
    }

    private fun startOnlineSync() {
        syncScope.launch {
            while (isActive) {
                triggerSync()
                delay(2500)
            }
        }
    }

    private suspend fun triggerSync() {
        try {
            val data = MediGuardApiClient.fetchData()
            if (data != null) {
                _isOnline.value = true
                applyServerData(data)
            } else {
                _isOnline.value = false
            }
        } catch (e: Exception) {
            _isOnline.value = false
        }
    }

    private fun applyServerData(json: JSONObject) {
        try {
            // Parse Users
            val usersArray = json.optJSONArray("users")
            if (usersArray != null) {
                val parsedUsers = mutableListOf<User>()
                for (i in 0 until usersArray.length()) {
                    val u = usersArray.getJSONObject(i)
                    parsedUsers.add(
                        User(
                            id = u.getString("id"),
                            name = u.getString("name"),
                            email = u.getString("email"),
                            password = u.optString("password", "password123"),
                            activeRole = try { UserRole.valueOf(u.optString("activeRole", "PASIEN")) } catch (e: Exception) { UserRole.PASIEN },
                            createdAt = u.optString("createdAt", getTodayDate())
                        )
                    )
                }
                _users.value = parsedUsers

                // Keep current user updated
                val current = _currentUser.value
                if (current != null) {
                    val fresh = parsedUsers.find { it.id == current.id }
                    if (fresh != null) _currentUser.value = fresh
                }
            }

            // Parse Rooms
            val roomsArray = json.optJSONArray("rooms")
            if (roomsArray != null) {
                val parsedRooms = mutableListOf<MedicationRoom>()
                for (i in 0 until roomsArray.length()) {
                    val r = roomsArray.getJSONObject(i)
                    parsedRooms.add(
                        MedicationRoom(
                            id = r.getString("id"),
                            ownerUserId = r.getString("ownerUserId"),
                            ownerName = r.getString("ownerName"),
                            name = r.getString("name"),
                            description = r.optString("description", ""),
                            inviteCode = r.getString("inviteCode"),
                            status = RoomStatus.ACTIVE,
                            createdAt = r.optString("createdAt", getTodayDate())
                        )
                    )
                }
                _rooms.value = parsedRooms
            }

            // Parse Room Members
            val membersArray = json.optJSONArray("roomMembers")
            if (membersArray != null) {
                val parsedMembers = mutableListOf<RoomMember>()
                for (i in 0 until membersArray.length()) {
                    val m = membersArray.getJSONObject(i)
                    parsedMembers.add(
                        RoomMember(
                            id = m.getString("id"),
                            roomId = m.getString("roomId"),
                            userId = m.getString("userId"),
                            userName = m.getString("userName"),
                            userEmail = m.getString("userEmail"),
                            role = try { UserRole.valueOf(m.optString("role", "PEMANTAU")) } catch (e: Exception) { UserRole.PEMANTAU },
                            status = try { MembershipStatus.valueOf(m.optString("status", "PENDING")) } catch (e: Exception) { MembershipStatus.PENDING },
                            canViewSchedules = m.optBoolean("canViewSchedules", true),
                            canViewActivities = m.optBoolean("canViewActivities", true),
                            canViewStats = m.optBoolean("canViewStats", true),
                            requestedAt = m.optString("requestedAt", getTodayDate()),
                            updatedAt = m.optString("updatedAt", getTodayDate())
                        )
                    )
                }
                _roomMembers.value = parsedMembers
            }

            // Parse Medications
            val medsArray = json.optJSONArray("medications")
            if (medsArray != null) {
                val parsedMeds = mutableListOf<Medication>()
                for (i in 0 until medsArray.length()) {
                    val m = medsArray.getJSONObject(i)
                    parsedMeds.add(
                        Medication(
                            id = m.getString("id"),
                            roomId = m.getString("roomId"),
                            name = m.getString("name"),
                            form = m.optString("form", "Tablet"),
                            dosage = m.optString("dosage", "500 mg"),
                            instructions = m.optString("instructions", "Sesudah makan"),
                            notes = m.optString("notes", ""),
                            startDate = m.optString("startDate", getTodayDate()),
                            endDate = m.optString("endDate", ""),
                            isArchived = m.optBoolean("isArchived", false)
                        )
                    )
                }
                _medications.value = parsedMeds
            }

            // Parse Schedules
            val schedsArray = json.optJSONArray("schedules")
            if (schedsArray != null) {
                val parsedScheds = mutableListOf<MedicationSchedule>()
                for (i in 0 until schedsArray.length()) {
                    val s = schedsArray.getJSONObject(i)
                    val timesArray = s.optJSONArray("times")
                    val timesList = mutableListOf<String>()
                    if (timesArray != null) {
                        for (j in 0 until timesArray.length()) {
                            timesList.add(timesArray.getString(j))
                        }
                    } else {
                        timesList.add("08:00")
                    }

                    parsedScheds.add(
                        MedicationSchedule(
                            id = s.getString("id"),
                            medicationId = s.getString("medicationId"),
                            medicationName = s.optString("medicationName", "Obat"),
                            frequency = s.optString("frequency", "1x sehari"),
                            times = timesList,
                            startDate = s.optString("startDate", getTodayDate()),
                            endDate = s.optString("endDate", ""),
                            isActive = s.optBoolean("isActive", true)
                        )
                    )
                }
                _schedules.value = parsedScheds
            }

            // Parse Dose Events
            val dosesArray = json.optJSONArray("doseEvents")
            if (dosesArray != null) {
                val parsedDoses = mutableListOf<DoseEvent>()
                for (i in 0 until dosesArray.length()) {
                    val d = dosesArray.getJSONObject(i)

                    var verifObj: DoseVerification? = null
                    val v = d.optJSONObject("verification")
                    if (v != null) {
                        verifObj = DoseVerification(
                            id = v.optString("id", UUID.randomUUID().toString()),
                            doseEventId = d.getString("id"),
                            verifierUserId = v.optString("verifiedByUserId", ""),
                            verifierName = v.optString("verifierName", "Pemantau"),
                            verificationType = "Pengamatan Langsung",
                            verifiedAt = v.optString("verifiedAt", ""),
                            statement = v.optString("statement", "Menyaksikan langsung saat konsumsi")
                        )
                    }

                    parsedDoses.add(
                        DoseEvent(
                            id = d.getString("id"),
                            scheduleId = d.optString("scheduleId", ""),
                            medicationId = d.optString("medicationId", ""),
                            medicationName = d.getString("medicationName"),
                            dosage = d.optString("dosage", ""),
                            instructions = d.optString("instructions", ""),
                            scheduledDate = getTodayDate(),
                            scheduledTime = d.optString("scheduledTime", "08:00"),
                            status = try { DoseStatus.valueOf(d.getString("status")) } catch (e: Exception) { DoseStatus.TERJADWAL },
                            reportedByUserId = d.optString("reportedByUserId", null),
                            reportedByName = d.optString("reportedByName", null),
                            reportedAt = d.optString("reportedAt", null),
                            takenAt = d.optString("takenAt", null),
                            photoUri = d.optString("photoUri", null),
                            reminderCount = d.optInt("reminderCount", 0),
                            snoozedUntil = d.optString("snoozedUntil", null),
                            notes = d.optString("notes", null),
                            verification = verifObj
                        )
                    )
                }
                _doseEvents.value = parsedDoses
            }

            // Parse Notifications
            val notifsArray = json.optJSONArray("notifications")
            if (notifsArray != null) {
                val parsedNotifs = mutableListOf<NotificationItem>()
                for (i in 0 until notifsArray.length()) {
                    val n = notifsArray.getJSONObject(i)
                    parsedNotifs.add(
                        NotificationItem(
                            id = n.getString("id"),
                            userId = n.getString("userId"),
                            title = n.getString("title"),
                            body = n.getString("body"),
                            type = try { NotificationType.valueOf(n.getString("type")) } catch (e: Exception) { NotificationType.REMINDER },
                            relatedEntityId = n.optString("relatedId", null),
                            isRead = n.optBoolean("isRead", false),
                            createdAt = n.optString("createdAt", getCurrentTimestamp())
                        )
                    )
                }
                _notifications.value = parsedNotifs
            }

            // Parse Audit Logs
            val logsArray = json.optJSONArray("auditLogs")
            if (logsArray != null) {
                val parsedLogs = mutableListOf<AuditLog>()
                for (i in 0 until logsArray.length()) {
                    val l = logsArray.getJSONObject(i)
                    parsedLogs.add(
                        AuditLog(
                            id = l.getString("id"),
                            roomId = l.optString("roomId", "room_1"),
                            actorUserId = l.optString("actorId", ""),
                            actorName = l.optString("actorName", "User"),
                            action = l.getString("action"),
                            entityType = "DoseEvent",
                            entityId = l.optString("id", ""),
                            details = l.getString("details"),
                            timestamp = l.getString("timestamp")
                        )
                    )
                }
                _auditLogs.value = parsedLogs
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // ---------------- PHOTO UPLOAD & DOWNLOAD ----------------

    suspend fun uploadPhoto(bytes: ByteArray, filename: String): String {
        val remoteUrl = MediGuardApiClient.uploadPhoto(bytes, filename)
        return remoteUrl ?: filename
    }

    suspend fun loadBitmap(uriOrUrl: String): Bitmap? {
        return MediGuardApiClient.loadBitmapFromUrl(uriOrUrl)
    }

    // ---------------- DEMO SEED DATA ----------------

    fun seedInitialDemoData() {
        val patient = User(
            id = "user_patient_1",
            name = "Hendra Wijaya",
            email = "hendra@mediguard.com",
            password = "password123",
            activeRole = UserRole.PASIEN,
            createdAt = "2026-10-10 08:00"
        )
        val caregiver = User(
            id = "user_caregiver_1",
            name = "Siti Rahmawati",
            email = "siti@mediguard.com",
            password = "password123",
            activeRole = UserRole.PEMANTAU,
            createdAt = "2026-10-10 09:00"
        )

        _users.value = listOf(patient, caregiver)
        if (_currentUser.value == null) {
            _currentUser.value = patient
        }

        val roomId = "room_1"
        val room = MedicationRoom(
            id = roomId,
            name = "Ruang Obat Hendra",
            description = "Pemantauan pengobatan hipertensi rutin",
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
        val memberCaregiver = RoomMember(
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
        _roomMembers.value = listOf(memberOwner, memberCaregiver)

        val med1 = Medication(
            id = "med_1",
            roomId = roomId,
            name = "Amlodipine",
            form = "Tablet",
            dosage = "5 mg",
            instructions = "1x sehari pagi sesudah makan",
            notes = "Obat rutin tekanan darah tinggi",
            startDate = "2026-10-01",
            isArchived = false
        )
        val med2 = Medication(
            id = "med_2",
            roomId = roomId,
            name = "Metformin",
            form = "Tablet",
            dosage = "500 mg",
            instructions = "2x sehari bersama makan",
            notes = "Pengontrol gula darah",
            startDate = "2026-10-01",
            isArchived = false
        )
        _medications.value = listOf(med1, med2)

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
            times = listOf("08:00", "20:00"),
            startDate = "2026-10-01",
            isActive = true
        )
        _schedules.value = listOf(sched1, sched2)

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
            status = DoseStatus.MENUNGGU_LAPORAN,
            reminderCount = 1
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
            status = DoseStatus.DILAPORKAN_DIMINUM,
            reportedByUserId = patient.id,
            reportedByName = patient.name,
            reportedAt = "$today 08:15",
            takenAt = "08:15",
            photoUri = "contoh_bukti_metformin.jpg"
        )
        val dose3 = DoseEvent(
            id = "dose_3",
            scheduleId = sched2.id,
            medicationId = med2.id,
            medicationName = med2.name,
            dosage = med2.dosage,
            instructions = med2.instructions,
            scheduledDate = today,
            scheduledTime = "20:00",
            status = DoseStatus.TERJADWAL
        )
        _doseEvents.value = listOf(dose1, dose2, dose3)

        val notif1 = NotificationItem(
            id = "notif_1",
            userId = caregiver.id,
            title = "Laporan Baru dari Pasien",
            body = "Hendra telah melaporkan minum Metformin (08:00). Silakan verifikasi bila menyaksikan langsung.",
            type = NotificationType.DOSE_REPORTED,
            relatedEntityId = dose2.id,
            isRead = false,
            createdAt = "$today 08:16"
        )
        val notif2 = NotificationItem(
            id = "notif_2",
            userId = patient.id,
            title = "Jadwal Obat Pagi",
            body = "Saatnya minum Amlodipine 5 mg (Jadwal: 08:00). Jangan lupa ambil foto bukti minum obat!",
            type = NotificationType.REMINDER,
            relatedEntityId = dose1.id,
            isRead = false,
            createdAt = "$today 08:00"
        )
        _notifications.value = listOf(notif1, notif2)

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
            actorUserId = patient.id,
            actorName = patient.name,
            action = "LAPORAN_MANDIRI",
            entityType = "DoseEvent",
            entityId = dose2.id,
            details = "Melaporkan minum obat Metformin (08:00) pada 08:15.",
            timestamp = "$today 08:15"
        )
        _auditLogs.value = listOf(log1, log2)

        // Sync with server if online
        syncScope.launch {
            MediGuardApiClient.sendAction("seedDemo", JSONObject(), _currentUser.value?.id)
        }
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

        syncScope.launch {
            val payload = JSONObject().apply {
                put("id", newUser.id)
                put("name", newUser.name)
                put("email", newUser.email)
                put("password", newUser.password)
                put("activeRole", newUser.activeRole.name)
            }
            MediGuardApiClient.sendAction("register", payload, newUser.id)
            triggerSync()
        }

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

        syncScope.launch {
            val payload = JSONObject().apply {
                put("activeRole", newRole.name)
            }
            MediGuardApiClient.sendAction("switchRole", payload, current.id)
            triggerSync()
        }
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

        syncScope.launch {
            val payload = JSONObject().apply {
                put("name", updated.name)
                put("email", updated.email)
            }
            MediGuardApiClient.sendAction("updateProfile", payload, current.id)
            triggerSync()
        }

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

        syncScope.launch {
            val payload = JSONObject().apply {
                put("name", newRoom.name)
                put("description", newRoom.description)
            }
            MediGuardApiClient.sendAction("createRoom", payload, current.id)
            triggerSync()
        }

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

        syncScope.launch {
            val payload = JSONObject().apply {
                put("roomId", roomId)
            }
            MediGuardApiClient.sendAction("rotateInviteCode", payload, current.id)
            triggerSync()
        }

        return Result.success(newCode)
    }

    fun findRoomPreviewByCode(code: String): Result<MedicationRoom> {
        val cleanCode = code.trim().uppercase()
        val room = _rooms.value.find { it.inviteCode.equals(cleanCode, ignoreCase = true) && it.status == RoomStatus.ACTIVE }
        return if (room != null) {
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

        syncScope.launch {
            val payload = JSONObject().apply {
                put("inviteCode", inviteCode)
            }
            MediGuardApiClient.sendAction("requestJoinRoom", payload, current.id)
            triggerSync()
        }

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

        syncScope.launch {
            val payload = JSONObject().apply {
                put("memberId", memberId)
            }
            MediGuardApiClient.sendAction("approveMember", payload, current.id)
            triggerSync()
        }

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

        logAudit(
            roomId = room.id,
            actor = current,
            action = "REJECT_MEMBER",
            entityType = "RoomMember",
            entityId = memberId,
            details = "Menolak permohonan akses dari ${member.userName}"
        )

        syncScope.launch {
            val payload = JSONObject().apply {
                put("memberId", memberId)
            }
            MediGuardApiClient.sendAction("rejectMember", payload, current.id)
            triggerSync()
        }

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

        logAudit(
            roomId = room.id,
            actor = current,
            action = "REVOKE_MEMBER",
            entityType = "RoomMember",
            entityId = memberId,
            details = "Mencabut izin akses untuk ${member.userName}"
        )

        syncScope.launch {
            val payload = JSONObject().apply {
                put("memberId", memberId)
            }
            MediGuardApiClient.sendAction("revokeMemberAccess", payload, current.id)
            triggerSync()
        }

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

        val updated = member.copy(
            canViewSchedules = canViewSchedules,
            canViewActivities = canViewActivities,
            canViewStats = canViewStats,
            updatedAt = getCurrentTimestamp()
        )
        _roomMembers.value = _roomMembers.value.map { if (it.id == memberId) updated else it }

        syncScope.launch {
            val payload = JSONObject().apply {
                put("memberId", memberId)
                put("canViewSchedules", canViewSchedules)
                put("canViewActivities", canViewActivities)
                put("canViewStats", canViewStats)
            }
            MediGuardApiClient.sendAction("updatePermissions", payload, current.id)
            triggerSync()
        }

        return Result.success(Unit)
    }

    fun getRoomsForUser(userId: String): List<MedicationRoom> {
        val user = _users.value.find { it.id == userId } ?: return emptyList()
        return if (user.activeRole == UserRole.PASIEN) {
            _rooms.value.filter { it.ownerUserId == userId }
        } else {
            val memberships = _roomMembers.value.filter { it.userId == userId && it.status == MembershipStatus.APPROVED }
            val roomIds = memberships.map { it.roomId }
            _rooms.value.filter { roomIds.contains(it.id) }
        }
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

        syncScope.launch {
            val payload = JSONObject().apply {
                put("roomId", roomId)
                put("name", newMed.name)
                put("form", newMed.form)
                put("dosage", newMed.dosage)
                put("instructions", newMed.instructions)
                put("notes", newMed.notes)
            }
            MediGuardApiClient.sendAction("addMedication", payload, current.id)
            triggerSync()
        }

        return Result.success(newMed)
    }

    fun archiveMedication(medicationId: String): Result<Unit> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val med = _medications.value.find { it.id == medicationId } ?: return Result.failure(IllegalArgumentException("Obat tidak ditemukan"))

        val updated = med.copy(isArchived = true)
        _medications.value = _medications.value.map { if (it.id == medicationId) updated else it }

        syncScope.launch {
            val payload = JSONObject().apply {
                put("medicationId", medicationId)
            }
            MediGuardApiClient.sendAction("archiveMedication", payload, current.id)
            triggerSync()
        }

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

        syncScope.launch {
            val payload = JSONObject().apply {
                put("medicationId", medicationId)
                put("frequency", frequency)
                put("times", JSONArray(times))
            }
            MediGuardApiClient.sendAction("addSchedule", payload, current.id)
            triggerSync()
        }

        return Result.success(newSched)
    }

    // ---------------- DOSE CONSUMPTION & VERIFICATION ----------------

    fun reportDoseTaken(doseEventId: String, takenAt: String, photoUri: String? = null): Result<DoseEvent> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val event = _doseEvents.value.find { it.id == doseEventId } ?: return Result.failure(IllegalArgumentException("Jadwal dosis tidak ditemukan"))

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

        syncScope.launch {
            val payload = JSONObject().apply {
                put("doseEventId", doseEventId)
                put("actualTakenTime", takenAt)
                put("photoUri", photoUri ?: "")
            }
            MediGuardApiClient.sendAction("reportDoseTaken", payload, current.id)
            triggerSync()
        }

        return Result.success(updated)
    }

    fun sendCaregiverReminder(doseEventId: String): Result<Int> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val event = _doseEvents.value.find { it.id == doseEventId } ?: return Result.failure(IllegalArgumentException("Jadwal dosis tidak ditemukan"))

        val newCount = event.reminderCount + 1
        val updated = event.copy(reminderCount = newCount)
        _doseEvents.value = _doseEvents.value.map { if (it.id == doseEventId) updated else it }

        syncScope.launch {
            val payload = JSONObject().apply {
                put("doseEventId", doseEventId)
            }
            MediGuardApiClient.sendAction("sendReminder", payload, current.id)
            triggerSync()
        }

        return Result.success(newCount)
    }

    fun reportDoseMissed(doseEventId: String, reason: String = ""): Result<DoseEvent> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Belum login"))
        val event = _doseEvents.value.find { it.id == doseEventId } ?: return Result.failure(IllegalArgumentException("Jadwal dosis tidak ditemukan"))

        val now = getCurrentTimestamp()
        val updated = event.copy(
            status = DoseStatus.TERLEWAT,
            reportedByUserId = current.id,
            reportedByName = current.name,
            reportedAt = now,
            notes = reason
        )
        _doseEvents.value = _doseEvents.value.map { if (it.id == doseEventId) updated else it }

        syncScope.launch {
            val payload = JSONObject().apply {
                put("doseEventId", doseEventId)
                put("reason", reason)
            }
            MediGuardApiClient.sendAction("reportDoseMissed", payload, current.id)
            triggerSync()
        }

        return Result.success(updated)
    }

    fun snoozeDose(doseEventId: String, minutes: Int): Result<DoseEvent> {
        val current = _currentUser.value
        val event = _doseEvents.value.find { it.id == doseEventId } ?: return Result.failure(IllegalArgumentException("Jadwal dosis tidak ditemukan"))
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, minutes)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val snoozedTime = sdf.format(calendar.time)

        val updated = event.copy(snoozedUntil = snoozedTime)
        _doseEvents.value = _doseEvents.value.map { if (it.id == doseEventId) updated else it }

        syncScope.launch {
            val payload = JSONObject().apply {
                put("doseEventId", doseEventId)
                put("minutes", minutes)
            }
            MediGuardApiClient.sendAction("snoozeDose", payload, current?.id)
            triggerSync()
        }

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

        syncScope.launch {
            val payload = JSONObject().apply {
                put("doseEventId", doseEventId)
                put("directObservationConfirmed", directObservationConfirmed)
                put("notes", notes)
            }
            MediGuardApiClient.sendAction("verifyDose", payload, current.id)
            triggerSync()
        }

        return Result.success(updated)
    }

    // ---------------- NOTIFICATIONS ----------------

    fun getNotificationsForCurrentUser(): List<NotificationItem> {
        val current = _currentUser.value ?: return emptyList()
        return _notifications.value.filter { it.userId == current.id }.sortedByDescending { it.createdAt }
    }

    fun markNotificationAsRead(id: String) {
        val current = _currentUser.value
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }

        syncScope.launch {
            val payload = JSONObject().apply {
                put("notificationId", id)
            }
            MediGuardApiClient.sendAction("markNotificationRead", payload, current?.id)
        }
    }

    fun markAllNotificationsAsRead() {
        val current = _currentUser.value ?: return
        _notifications.value = _notifications.value.map {
            if (it.userId == current.id) it.copy(isRead = true) else it
        }

        syncScope.launch {
            MediGuardApiClient.sendAction("markAllNotificationsRead", JSONObject(), current.id)
        }
    }

    // ---------------- AUDIT LOGS ----------------

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
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

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
