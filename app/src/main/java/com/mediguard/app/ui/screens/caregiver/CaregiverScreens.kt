package com.mediguard.app.ui.screens.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediguard.app.data.model.*
import com.mediguard.app.data.repository.MediGuardRepository
import com.mediguard.app.ui.screens.patient.DoseStatusBadge
import com.mediguard.app.ui.theme.StatusTaken
import com.mediguard.app.ui.theme.StatusVerified

@Composable
fun CaregiverDashboardScreen(
    repository: MediGuardRepository,
    onNavigateToJoin: () -> Unit,
    onNavigateToRooms: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val rooms by repository.rooms.collectAsState()
    val members by repository.roomMembers.collectAsState()
    val doseEvents by repository.doseEvents.collectAsState()

    var selectedDoseForVerification by remember { mutableStateOf<DoseEvent?>(null) }
    var directObservationConfirmed by remember { mutableStateOf(false) }
    var verificationNotes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var previewPhotoUri by remember { mutableStateOf<String?>(null) }
    var reminderSentMessage by remember { mutableStateOf<String?>(null) }

    // Monitored rooms where caregiver is approved
    val myMemberships = members.filter { it.userId == currentUser?.id }
    val approvedRoomIds = myMemberships.filter { it.status == MembershipStatus.APPROVED }.map { it.roomId }

    // Doses needing verification: reported taken but not yet directly verified
    val pendingVerifications = doseEvents.filter {
        it.status == DoseStatus.DILAPORKAN_DIMINUM
    }

    // Doses that are late / pending report
    val lateDoses = doseEvents.filter {
        it.status == DoseStatus.MENUNGGU_LAPORAN
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dashboard Pemantau",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = "Pemantau: ${currentUser?.name ?: "Keluarga"}", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${approvedRoomIds.size} Ruang Pasien Aktif",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Button(onClick = onNavigateToJoin) {
                            Text("+ Gabung Kode", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (reminderSentMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✓ $reminderSentMessage",
                            color = Color(0xFF166534),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { reminderSentMessage = null }) {
                            Text("Tutup", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Section: Jadwal Pasien Telat & Tombol Pengingat Berulang
        if (lateDoses.isNotEmpty()) {
            item {
                Text(
                    text = "Jadwal Telat / Belum Dilaporkan (${lateDoses.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFFB45309)
                )
            }

            items(lateDoses) { lateDose ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = lateDose.medicationName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "${lateDose.dosage} • Jadwal: ${lateDose.scheduledTime}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DoseStatusBadge(status = lateDose.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ Pasien belum melaporkan minum obat ini (Telat).",
                            fontSize = 12.sp,
                            color = Color(0xFFB45309),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Pengingat terkirim: ${lateDose.reminderCount} kali (Termasuk 1x notifikasi default sistem)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val res = repository.sendCaregiverReminder(lateDose.id)
                                if (res.isSuccess) {
                                    reminderSentMessage = "Pengingat ke-${res.getOrThrow()} berhasil dikirimkan ke pasien!"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Ingatkan")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kirim Notifikasi Pengingat ke Pasien")
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Verifikasi Tertunda (${pendingVerifications.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        if (pendingVerifications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tidak ada laporan obat yang menunggu verifikasi saat ini.",
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(pendingVerifications) { dose ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = dose.medicationName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "${dose.dosage} • Jadwal: ${dose.scheduledTime}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DoseStatusBadge(status = dose.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Dilaporkan oleh Pasien pada: ${dose.reportedAt ?: "-"}",
                            fontSize = 12.sp,
                            color = StatusTaken
                        )

                        // Photo Proof from Patient preview card
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = "Foto", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Foto Bukti Pasien:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(dose.photoUri ?: "Foto bukti terlampir", fontSize = 11.sp)
                                    }
                                }
                                Button(
                                    onClick = { previewPhotoUri = dose.photoUri ?: "foto_bukti_pasien.jpg" },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Periksa Foto", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                selectedDoseForVerification = dose
                                directObservationConfirmed = false
                                verificationNotes = ""
                                errorMessage = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusVerified)
                        ) {
                            Text("Verifikasi Pengamatan Langsung")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ruang Pasien yang Diikuti",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                TextButton(onClick = onNavigateToRooms) {
                    Text("Lihat Semua")
                }
            }
        }

        if (myMemberships.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Anda belum bergabung dengan ruang pengobatan manapun.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onNavigateToJoin) {
                            Text("Gabung dengan Kode Undangan")
                        }
                    }
                }
            }
        } else {
            items(myMemberships) { membership ->
                val room = rooms.find { it.id == membership.roomId }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = room?.name ?: "Ruang Pasien",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            MembershipStatusBadge(status = membership.status)
                        }
                        Text(
                            text = "Pasien Pemilik: ${room?.ownerName ?: "-"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (membership.status == MembershipStatus.PENDING) {
                            Text(
                                text = "Menunggu persetujuan pasien. Data kesehatan belum dapat diakses.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        } else if (membership.status == MembershipStatus.APPROVED) {
                            Text(
                                text = "Izin aktif: Jadwal=${if (membership.canViewSchedules) "Ya" else "Tidak"}, Aktivitas=${if (membership.canViewActivities) "Ya" else "Tidak"}",
                                fontSize = 11.sp,
                                color = StatusVerified
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Verification Dialog (Direct Observation principle)
    if (selectedDoseForVerification != null) {
        val dose = selectedDoseForVerification!!

        AlertDialog(
            onDismissRequest = { selectedDoseForVerification = null },
            title = { Text("Verifikasi Pengamatan Langsung") },
            text = {
                Column {
                    Text("Obat: ${dose.medicationName} (${dose.dosage})", fontWeight = FontWeight.Bold)
                    Text("Waktu Laporan Pasien: ${dose.reportedAt ?: "-"}")

                    Spacer(modifier = Modifier.height(12.dp))

                    if (errorMessage != null) {
                        Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Ketentuan Verifikasi:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Pemantau hanya boleh memverifikasi jika benar-benar menyaksikan pasien mengonsumsi obat secara langsung.",
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Display Photo Proof Before Approving
                    if (dose.photoUri != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = "Foto", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Foto Bukti dari Pasien:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dose.photoUri,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Pastikan Anda memeriksa kesesuaian foto obat sebelum memverifikasi.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = directObservationConfirmed,
                            onCheckedChange = { directObservationConfirmed = it }
                        )
                        Text(
                            text = "Saya telah memeriksa foto bukti dan menyatakan telah menyaksikan langsung pasien meminum obat ini.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = verificationNotes,
                        onValueChange = { verificationNotes = it },
                        label = { Text("Catatan Verifikasi (opsional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val result = repository.verifyDoseDirectObservation(
                            doseEventId = dose.id,
                            directObservationConfirmed = directObservationConfirmed,
                            notes = verificationNotes
                        )
                        if (result.isSuccess) {
                            selectedDoseForVerification = null
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Gagal verifikasi"
                        }
                    },
                    enabled = directObservationConfirmed,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusVerified)
                ) {
                    Text("Setujui & Verifikasi")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDoseForVerification = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal Photo Preview Dialog
    if (previewPhotoUri != null) {
        AlertDialog(
            onDismissRequest = { previewPhotoUri = null },
            title = { Text("Foto Bukti Konsumsi Pasien") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Foto", modifier = Modifier.size(52.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Foto Bukti: $previewPhotoUri", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Dikirimkan oleh pasien saat konfirmasi minum", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pemeriksaan foto bukti oleh pemantau sebelum verifikasi", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(onClick = { previewPhotoUri = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
fun MembershipStatusBadge(status: MembershipStatus) {
    val (bgColor, textColor) = when (status) {
        MembershipStatus.APPROVED -> Color(0xFFDCFCE7) to Color(0xFF166534)
        MembershipStatus.PENDING -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        MembershipStatus.REJECTED -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        MembershipStatus.REVOKED -> Color(0xFFF3F4F6) to Color(0xFF4B5563)
    }

    Surface(
        color = bgColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

// ---------------- JOIN ROOM SCREEN ----------------

@Composable
fun JoinRoomScreen(
    repository: MediGuardRepository,
    onBack: () -> Unit
) {
    var inviteCode by remember { mutableStateOf("MG-8821") }
    var previewRoom by remember { mutableStateOf<MedicationRoom?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Gabung Medication Room",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Masukkan kode undangan yang dibagikan oleh pasien untuk mengajukan akses pemantauan.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp
                )
            }
        }

        if (infoMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text(
                    text = infoMessage ?: "",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp
                )
            }
        }

        OutlinedTextField(
            value = inviteCode,
            onValueChange = { inviteCode = it; errorMessage = null },
            label = { Text("Kode Undangan (misal: MG-8821)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val res = repository.findRoomPreviewByCode(inviteCode)
                if (res.isSuccess) {
                    previewRoom = res.getOrThrow()
                    errorMessage = null
                } else {
                    errorMessage = res.exceptionOrNull()?.message
                    previewRoom = null
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Search, contentDescription = "Cari")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cari Ruang")
        }

        // Preview Room (Privacy preserved: only basic metadata)
        if (previewRoom != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Ruang Ditemukan:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = previewRoom!!.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Pasien Pemilik: ${previewRoom!!.ownerName}", fontSize = 13.sp)
                    if (previewRoom!!.description.isNotBlank()) {
                        Text(text = previewRoom!!.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Catatan Privasi: Data jadwal dan obat hanya dapat dilihat setelah pasien menyetujui permintaan bergabung Anda.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val reqRes = repository.requestJoinRoom(inviteCode)
                            if (reqRes.isSuccess) {
                                infoMessage = "Permintaan bergabung berhasil diajukan. Silakan tunggu persetujuan dari pasien."
                                previewRoom = null
                            } else {
                                errorMessage = reqRes.exceptionOrNull()?.message
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ajukan Permintaan Akses")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Kembali")
        }
    }
}

// ---------------- CAREGIVER ROOMS & PATIENT DETAILS ----------------

@Composable
fun CaregiverRoomsScreen(
    repository: MediGuardRepository
) {
    val currentUser by repository.currentUser.collectAsState()
    val rooms by repository.rooms.collectAsState()
    val members by repository.roomMembers.collectAsState()
    val medications by repository.medications.collectAsState()
    val schedules by repository.schedules.collectAsState()

    val myMemberships = members.filter { it.userId == currentUser?.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Ruang Pasien yang Dipantau",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (myMemberships.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Anda belum tergabung dalam ruang pasien.")
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(myMemberships) { membership ->
                    val room = rooms.find { it.id == membership.roomId }
                    val roomMeds = medications.filter { it.roomId == membership.roomId && !it.isArchived }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = room?.name ?: "Ruang",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                MembershipStatusBadge(status = membership.status)
                            }
                            Text(
                                text = "Pasien: ${room?.ownerName ?: "-"}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(10.dp))

                            when (membership.status) {
                                MembershipStatus.APPROVED -> {
                                    if (membership.canViewSchedules) {
                                        Text(text = "Daftar Obat Pasien:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        if (roomMeds.isEmpty()) {
                                            Text(text = "Belum ada obat aktif.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                        } else {
                                            roomMeds.forEach { med ->
                                                val medScheds = schedules.filter { it.medicationId == med.id }
                                                Text(
                                                    text = "• ${med.name} (${med.dosage}) - ${med.instructions}",
                                                    fontSize = 12.sp
                                                )
                                                if (medScheds.isNotEmpty()) {
                                                    Text(
                                                        text = "   Jadwal: ${medScheds.first().times.joinToString(", ")}",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "Pasien membatasi izin untuk melihat daftar jadwal obat.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                                MembershipStatus.PENDING -> {
                                    Text(
                                        text = "Permintaan bergabung sedang menunggu persetujuan dari pasien. Anda belum dapat melihat rincian obat atau jadwal.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                MembershipStatus.REVOKED -> {
                                    Text(
                                        text = "Akses pemantauan ke ruang ini telah dicabut oleh pasien.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                MembershipStatus.REJECTED -> {
                                    Text(
                                        text = "Permintaan bergabung ke ruang ini ditolak oleh pasien.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- CAREGIVER ACTIVITY & STATS SCREEN ----------------

@Composable
fun CaregiverActivityScreen(
    repository: MediGuardRepository
) {
    val auditLogs by repository.auditLogs.collectAsState()
    val stats = repository.getAdherenceStats()

    var selectedTab by remember { mutableStateOf(0) } // 0: Aktivitas, 1: Statistik

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Aktivitas & Kepatuhan Pasien",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Linimasa") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Statistik Kepatuhan") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(auditLogs) { log ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = log.action, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = log.timestamp, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = log.details, fontSize = 13.sp)
                                Text(text = "Oleh: ${log.actorName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
            1 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Statistik Kepatuhan Pasien", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("${stats.compliancePercentage}%", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                                    Text("Laporan Mandiri Pasien", fontSize = 12.sp)
                                }
                                Column {
                                    Text("${stats.verifiedPercentage}%", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = StatusVerified)
                                    Text("Terverifikasi Langsung", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Rincian Jadwal Jatuh Tempo:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Total Jadwal Jatuh Tempo: ${stats.totalDue} dosis")
                            Text("Dilaporkan Diminum: ${stats.reportedTaken} dosis")
                            Text("Terverifikasi Langsung: ${stats.directlyVerified} dosis")
                            Text("Dosis Terlewat: ${stats.missed} dosis")
                        }
                    }
                }
            }
        }
    }
}
