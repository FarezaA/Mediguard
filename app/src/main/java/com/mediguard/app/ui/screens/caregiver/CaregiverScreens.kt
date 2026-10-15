package com.mediguard.app.ui.screens.caregiver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediguard.app.data.model.*
import com.mediguard.app.data.repository.MediGuardRepository
import com.mediguard.app.ui.screens.common.AsyncPhotoPreviewDialog
import com.mediguard.app.ui.screens.patient.DoseStatusBadge
import com.mediguard.app.ui.theme.*
import java.util.*

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

    val myMemberships = members.filter { it.userId == currentUser?.id }
    val approvedRoomIds = myMemberships.filter { it.status == MembershipStatus.APPROVED }.map { it.roomId }

    val pendingVerifications = doseEvents.filter {
        it.status == DoseStatus.DILAPORKAN_DIMINUM
    }

    val lateDoses = doseEvents.filter {
        it.status == DoseStatus.MENUNGGU_LAPORAN
    }

    val today = remember {
        val cal = Calendar.getInstance()
        val days = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
        val months = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val dayName = days[cal.get(Calendar.DAY_OF_WEEK) - 1]
        val date = cal.get(Calendar.DAY_OF_MONTH)
        val month = months[cal.get(Calendar.MONTH)]
        val year = cal.get(Calendar.YEAR)
        "$dayName, $date $month $year"
    }

    val greetingTime = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour in 4..10 -> "Selamat Pagi"
            hour in 11..14 -> "Selamat Siang"
            hour in 15..18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── 1. Gradient Header ──
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGradient)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.20f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (currentUser?.name?.firstOrNull() ?: 'P').toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "$greetingTime 👋",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentUser?.name ?: "Pemantau",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                text = "Pemantau Tepercaya",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = today,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = onNavigateToJoin,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Gabung Kode", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // ── 2. Metric Stat Cards ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pending Verifications
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(StatusTakenBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FactCheck, contentDescription = null, tint = StatusTaken, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${pendingVerifications.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StatusTaken)
                        Text(
                            "Perlu Verifikasi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Late / Missed
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(StatusPendingBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = StatusPending, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${lateDoses.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StatusPending)
                        Text(
                            "Belum Lapor",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Active Rooms
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(TealContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${approvedRoomIds.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                        Text(
                            "Ruang Pasien",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ── 3. Reminder Feedback Toast ──
        if (reminderSentMessage != null) {
            item {
                Surface(
                    color = StatusVerifiedBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusVerified, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = reminderSentMessage ?: "",
                                color = StatusVerifiedText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        TextButton(
                            onClick = { reminderSentMessage = null },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Tutup", fontSize = 11.sp, color = StatusVerifiedText)
                        }
                    }
                }
            }
        }

        // ── 4. Section: Late Doses / Needs Reminder ──
        if (lateDoses.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = StatusPending, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Perlu Perhatian (Belum Ada Laporan)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextDark
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(lateDoses) { lateDose ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lateDose.medicationName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextDark
                                )
                                Text(
                                    text = "${lateDose.dosage} • Jadwal: ${lateDose.scheduledTime}",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            DoseStatusBadge(status = lateDose.status)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = StatusPendingBg,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = StatusPendingText, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pasien belum melapor minum obat ini. Pengingat terkirim: ${lateDose.reminderCount}x.",
                                    fontSize = 11.sp,
                                    color = StatusPendingText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val res = repository.sendCaregiverReminder(lateDose.id)
                                if (res.isSuccess) {
                                    reminderSentMessage = "Pengingat ke-${res.getOrThrow()} berhasil dikirimkan ke pasien!"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kirim Notifikasi Pengingat ke Pasien", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // ── 5. Section: Pending Verifications ──
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Verifikasi Tertunda (${pendingVerifications.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextDark
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (pendingVerifications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(TealContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TealPrimary)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Semua Laporan Telah Terverifikasi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextDark
                        )
                        Text(
                            text = "Tidak ada laporan obat pasien yang menunggu verifikasi saat ini.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(pendingVerifications) { dose ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dose.medicationName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextDark
                                )
                                Text(
                                    text = "${dose.dosage} • Jadwal: ${dose.scheduledTime}",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            DoseStatusBadge(status = dose.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Dilaporkan Pasien pada: ${dose.reportedAt ?: "-"}",
                            fontSize = 12.sp,
                            color = StatusTaken,
                            fontWeight = FontWeight.Medium
                        )

                        // Photo Proof Attachment
                        if (dose.photoUri != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Foto Bukti Pasien:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AccentCyan)
                                            Text(dose.photoUri, fontSize = 11.sp, color = TextMuted)
                                        }
                                    }
                                    TextButton(
                                        onClick = { previewPhotoUri = dose.photoUri },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Periksa Foto", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                selectedDoseForVerification = dose
                                directObservationConfirmed = false
                                verificationNotes = ""
                                errorMessage = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusVerified)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verifikasi Pengamatan Langsung", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // ── 6. Section: Followed Patient Rooms ──
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ruang Pasien yang Diikuti",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextDark
                )
                TextButton(onClick = onNavigateToRooms) {
                    Text("Kelola Ruang", fontSize = 12.sp, color = TealPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (myMemberships.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Anda belum bergabung dengan ruang manapun.", fontSize = 13.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onNavigateToJoin,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Text("Gabung dengan Kode Undangan", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(myMemberships) { membership ->
                val room = rooms.find { it.id == membership.roomId }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = room?.name ?: "Ruang Pasien",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextDark,
                                modifier = Modifier.weight(1f)
                            )
                            MembershipStatusBadge(status = membership.status)
                        }
                        Text(
                            text = "Pasien Pemilik: ${room?.ownerName ?: "-"}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (membership.status == MembershipStatus.PENDING) {
                            Text(
                                text = "Menunggu persetujuan pasien. Data pengobatan belum dapat diakses.",
                                fontSize = 11.sp,
                                color = StatusPendingText
                            )
                        } else if (membership.status == MembershipStatus.APPROVED) {
                            Text(
                                text = "Hak Akses: Jadwal=${if (membership.canViewSchedules) "Ya" else "Tidak"}, Aktivitas=${if (membership.canViewActivities) "Ya" else "Tidak"}",
                                fontSize = 11.sp,
                                color = StatusVerifiedText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // ── Dialog: Verifikasi Pengamatan Langsung ──
    if (selectedDoseForVerification != null) {
        val dose = selectedDoseForVerification!!

        AlertDialog(
            onDismissRequest = { selectedDoseForVerification = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(StatusVerifiedBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = StatusVerified, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Verifikasi Pengamatan Nyata", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextDark)
                }
            },
            text = {
                Column {
                    Surface(
                        color = SurfaceSubtle,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Obat: ${dose.medicationName} (${dose.dosage})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                            Text("Waktu Laporan Pasien: ${dose.reportedAt ?: "-"}", fontSize = 12.sp, color = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = StatusMissed,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Ethical guidelines callout
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Ketentuan Integritas Medis:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF92400E))
                            Text(
                                "Pemantau HANYA boleh memverifikasi jika benar-benar menyaksikan pasien mengonsumsi obat secara langsung. Jangan berasumsi.",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    if (dose.photoUri != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bukti Foto Terlampir", fontSize = 11.sp, color = AccentCyan, fontWeight = FontWeight.SemiBold)
                                }
                                TextButton(
                                    onClick = { previewPhotoUri = dose.photoUri },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Lihat Foto", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = directObservationConfirmed,
                            onCheckedChange = { directObservationConfirmed = it },
                            colors = CheckboxDefaults.colors(checkedColor = StatusVerified)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Saya menyatakan telah memeriksa bukti dan menyaksikan langsung pasien meminum obat ini.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = verificationNotes,
                        onValueChange = { verificationNotes = it },
                        label = { Text("Catatan Pengamatan (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
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
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusVerified)
                ) {
                    Text("Setujui & Verifikasi", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDoseForVerification = null }) {
                    Text("Batal", fontSize = 13.sp, color = TextMuted)
                }
            }
        )
    }

    // ── Dialog: Preview Foto ──
    if (previewPhotoUri != null) {
        AsyncPhotoPreviewDialog(
            photoUri = previewPhotoUri,
            medicationName = "Bukti Foto Pasien",
            dosage = "Laporan Mandiri",
            takenAt = "Dilaporkan Pasien",
            repository = repository,
            onDismiss = { previewPhotoUri = null }
        )
    }
}

@Composable
fun MembershipStatusBadge(
    status: MembershipStatus,
    modifier: Modifier = Modifier
) {
    val bgColor: Color
    val textColor: Color
    val icon: ImageVector

    when (status) {
        MembershipStatus.APPROVED -> {
            bgColor = StatusVerifiedBg
            textColor = StatusVerifiedText
            icon = Icons.Default.CheckCircle
        }
        MembershipStatus.PENDING -> {
            bgColor = StatusPendingBg
            textColor = StatusPendingText
            icon = Icons.Default.Schedule
        }
        MembershipStatus.REJECTED -> {
            bgColor = StatusMissedBg
            textColor = StatusMissedText
            icon = Icons.Default.Cancel
        }
        MembershipStatus.REVOKED -> {
            bgColor = SurfaceSubtle
            textColor = TextLight
            icon = Icons.Default.Block
        }
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(50.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.displayName,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
            .background(AppBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextDark)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Gabung Medication Room",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Masukkan kode undangan yang dibagikan oleh pasien untuk mengajukan akses pemantauan keluarga.",
            fontSize = 13.sp,
            color = TextMuted,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = StatusMissedBg),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = StatusMissedText,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp
                )
            }
        }

        if (infoMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = StatusVerifiedBg),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = infoMessage ?: "",
                    color = StatusVerifiedText,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp
                )
            }
        }

        OutlinedTextField(
            value = inviteCode,
            onValueChange = {
                inviteCode = it
                errorMessage = null
            },
            label = { Text("Kode Undangan Ruang (misal: MG-8821)") },
            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = TextMuted) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = SurfaceBorder)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val res = repository.findRoomPreviewByCode(inviteCode.trim())
                if (res.isSuccess) {
                    previewRoom = res.getOrThrow()
                    errorMessage = null
                } else {
                    errorMessage = res.exceptionOrNull()?.message
                    previewRoom = null
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Cari Ruang Pasien", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }

        if (previewRoom != null) {
            Spacer(modifier = Modifier.height(18.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Ruang Pasien Ditemukan:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TealPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = previewRoom!!.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(text = "Pasien Pemilik: ${previewRoom!!.ownerName}", fontSize = 13.sp, color = TextMuted)
                    if (previewRoom!!.description.isNotBlank()) {
                        Text(text = previewRoom!!.description, fontSize = 12.sp, color = TextMuted)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = SurfaceSubtle,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Catatan Privasi: Data jadwal & obat hanya dapat diakses setelah pasien menyetujui permintaan bergabung Anda.",
                            modifier = Modifier.padding(10.dp),
                            fontSize = 11.sp,
                            color = TextDark,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val reqRes = repository.requestJoinRoom(inviteCode.trim())
                            if (reqRes.isSuccess) {
                                infoMessage = "Permintaan bergabung berhasil diajukan! Silakan tunggu persetujuan dari pasien."
                                previewRoom = null
                            } else {
                                errorMessage = reqRes.exceptionOrNull()?.message
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text("Ajukan Permintaan Bergabung", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
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
            .background(AppBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Ruang Pasien yang Dipantau",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "Daftar ruang pengobatan pasien yang memberi Anda izin pendampingan",
            fontSize = 12.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (myMemberships.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Anda belum tergabung dalam ruang pasien manapun.", fontSize = 13.sp, color = TextMuted)
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

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, SurfaceBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = room?.name ?: "Ruang Pasien",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextDark,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                MembershipStatusBadge(status = membership.status)
                            }
                            Text(
                                text = "Pasien Pemilik: ${room?.ownerName ?: "-"}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = SurfaceBorder)
                            Spacer(modifier = Modifier.height(10.dp))

                            when (membership.status) {
                                MembershipStatus.APPROVED -> {
                                    if (membership.canViewSchedules) {
                                        Text(
                                            text = "Daftar Obat Pasien:",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = TextDark
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        if (roomMeds.isEmpty()) {
                                            Text(
                                                text = "Belum ada obat aktif di ruang ini.",
                                                fontSize = 12.sp,
                                                color = TextMuted
                                            )
                                        } else {
                                            roomMeds.forEach { med ->
                                                val medScheds = schedules.filter { it.medicationId == med.id }
                                                Surface(
                                                    color = SurfaceSubtle,
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 3.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                        Text(
                                                            text = "• ${med.name} (${med.dosage}) - ${med.instructions}",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = TextDark
                                                        )
                                                        if (medScheds.isNotEmpty()) {
                                                            Text(
                                                                text = "   Jadwal minum: ${medScheds.first().times.joinToString(", ")}",
                                                                fontSize = 11.sp,
                                                                color = TealPrimary
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "Pasien membatasi izin untuk melihat rincian jadwal obat.",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                                MembershipStatus.PENDING -> {
                                    Text(
                                        text = "Permintaan bergabung sedang menunggu persetujuan pasien.",
                                        fontSize = 12.sp,
                                        color = StatusPendingText
                                    )
                                }
                                MembershipStatus.REVOKED -> {
                                    Text(
                                        text = "Akses pemantauan ke ruang ini telah dicabut oleh pasien.",
                                        fontSize = 12.sp,
                                        color = StatusMissed
                                    )
                                }
                                MembershipStatus.REJECTED -> {
                                    Text(
                                        text = "Permintaan bergabung ditolak oleh pasien.",
                                        fontSize = 12.sp,
                                        color = StatusMissed
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

    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Aktivitas & Kepatuhan Pasien",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "Pantau linimasa kepatuhan dan riwayat pengamatan langsung",
            fontSize = 12.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceCard,
            contentColor = TealPrimary
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Linimasa Audit", fontWeight = FontWeight.SemiBold) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Statistik Kepatuhan", fontWeight = FontWeight.SemiBold) })
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(auditLogs) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = log.action, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                                    Text(text = log.timestamp, fontSize = 11.sp, color = TextLight)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = log.details, fontSize = 12.sp, color = TextMuted)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "Oleh: ${log.actorName}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TealPrimary)
                            }
                        }
                    }
                }
            }
            1 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, SurfaceBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text("Statistik Kepatuhan Pengobatan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "${stats.compliancePercentage}%",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCyan
                                    )
                                    Text("Laporan Mandiri Pasien", fontSize = 12.sp, color = TextMuted)
                                }
                                Column {
                                    Text(
                                        "${stats.verifiedPercentage}%",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusVerified
                                    )
                                    Text("Pengamatan Nyata", fontSize = 12.sp, color = TextMuted)
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Rincian Jadwal Jatuh Tempo:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Jadwal Jatuh Tempo:", fontSize = 13.sp, color = TextMuted)
                                Text("${stats.totalDue} dosis", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextDark)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Dilaporkan Diminum (Mandiri):", fontSize = 13.sp, color = TextMuted)
                                Text("${stats.reportedTaken} dosis", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StatusTaken)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Terverifikasi Langsung:", fontSize = 13.sp, color = TextMuted)
                                Text("${stats.directlyVerified} dosis", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StatusVerified)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Dosis Terlewat:", fontSize = 13.sp, color = TextMuted)
                                Text("${stats.missed} dosis", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StatusMissed)
                            }
                        }
                    }
                }
            }
        }
    }
}
