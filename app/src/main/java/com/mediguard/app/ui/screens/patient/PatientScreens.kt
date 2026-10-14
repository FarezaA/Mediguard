package com.mediguard.app.ui.screens.patient

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediguard.app.data.model.*
import com.mediguard.app.data.repository.MediGuardRepository
import com.mediguard.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PatientDashboardScreen(
    repository: MediGuardRepository,
    onNavigateToMedications: () -> Unit,
    onNavigateToRooms: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val doseEvents by repository.doseEvents.collectAsState()
    val medications by repository.medications.collectAsState()
    val rooms by repository.rooms.collectAsState()

    var selectedDoseForAction by remember { mutableStateOf<DoseEvent?>(null) }
    var actionType by remember { mutableStateOf<String?>(null) } // "TAKE", "MISS", "SNOOZE"
    var missReason by remember { mutableStateOf("") }
    var takenTime by remember { mutableStateOf("") }
    var photoUriInput by remember { mutableStateOf<String?>("bukti_minum_obat.jpg") }
    var previewPhotoDose by remember { mutableStateOf<DoseEvent?>(null) }

    val todayFormatted = remember {
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

    // Counts
    val todayDoses = doseEvents
    val reportedCount = todayDoses.count {
        it.status == DoseStatus.DILAPORKAN_DIMINUM ||
                it.status == DoseStatus.TERVERIFIKASI_LANGSUNG ||
                it.status == DoseStatus.TERLAMBAT_DILAPORKAN
    }
    val pendingReportCount = todayDoses.count {
        it.status == DoseStatus.MENUNGGU_LAPORAN || it.status == DoseStatus.TERJADWAL
    }
    val verifiedCount = todayDoses.count { it.status == DoseStatus.TERVERIFIKASI_LANGSUNG }

    val activeRoom = rooms.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 1. Hero Card: Friendly Greeting & Room Status ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TealPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                            Column {
                                Text(
                                    text = "$greetingTime 👋",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = currentUser?.name ?: "Pasien",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                text = "Pasien Mandiri",
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = todayFormatted,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.90f),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Surface(
                            modifier = Modifier.clickable { onNavigateToRooms() },
                            color = Color.White.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.MeetingRoom,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = activeRoom?.name ?: "Pilih Ruang",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── 2. Adherence Overview Stat Cards ──
        item {
            Column {
                Text(
                    text = "Ringkasan Hari Ini",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Dilaporkan Mandiri
                    AdherenceMetricCard(
                        modifier = Modifier.weight(1f),
                        count = reportedCount,
                        label = "Dilaporkan",
                        sublabel = "Laporan Mandiri",
                        tint = StatusTaken,
                        bgTint = StatusTakenBg,
                        icon = Icons.Default.EditNote
                    )

                    // Terverifikasi Langsung
                    AdherenceMetricCard(
                        modifier = Modifier.weight(1f),
                        count = verifiedCount,
                        label = "Terverifikasi",
                        sublabel = "Pengamatan Nyata",
                        tint = StatusVerified,
                        bgTint = StatusVerifiedBg,
                        icon = Icons.Default.Verified
                    )

                    // Menunggu
                    AdherenceMetricCard(
                        modifier = Modifier.weight(1f),
                        count = pendingReportCount,
                        label = "Menunggu",
                        sublabel = "Jadwal Belum",
                        tint = StatusPending,
                        bgTint = StatusPendingBg,
                        icon = Icons.Default.Schedule
                    )
                }
            }
        }

        // ── 3. Schedule Section Header ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Jadwal Konsumsi Hari Ini",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextDark
                    )
                    Text(
                        text = "Catat laporan setelah Anda mengonsumsi obat",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                TextButton(
                    onClick = onNavigateToMedications,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Kelola Obat",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TealPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = TealPrimary
                    )
                }
            }
        }

        // ── 4. Dose Items List or Empty State ──
        if (todayDoses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(TealContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Medication,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Belum Ada Jadwal Obat Hari Ini",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Daftarkan obat resep atau rutin Anda beserta waktu pengingatnya.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToMedications,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Text("+ Tambah Obat & Jadwal", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        } else {
            items(todayDoses) { dose ->
                DoseEventCard(
                    dose = dose,
                    onTakeClick = {
                        selectedDoseForAction = dose
                        actionType = "TAKE"
                        val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        takenTime = timeSdf.format(Date())
                    },
                    onMissClick = {
                        selectedDoseForAction = dose
                        actionType = "MISS"
                        missReason = ""
                    },
                    onSnoozeClick = {
                        selectedDoseForAction = dose
                        actionType = "SNOOZE"
                    },
                    onViewPhoto = { previewPhotoDose = it }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ── Dialog: Konfirmasi Konsumsi Obat ──
    if (selectedDoseForAction != null && actionType == "TAKE") {
        AlertDialog(
            onDismissRequest = { selectedDoseForAction = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TealContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Konfirmasi Minum Obat", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextDark)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Apakah Anda menyatakan telah mengonsumsi obat ini secara mandiri?",
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = SurfaceSubtle,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = selectedDoseForAction?.medicationName ?: "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextDark
                            )
                            Text(
                                text = "Dosis: ${selectedDoseForAction?.dosage} • Jadwal: ${selectedDoseForAction?.scheduledTime}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = takenTime,
                        onValueChange = { takenTime = it },
                        label = { Text("Waktu Minum Nyata (HH:mm)") },
                        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = SurfaceBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Photo Proof Card
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = StatusVerified, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Bukti Foto Minum Obat (Wajib):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StatusVerifiedText)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Foto membantu pemantau memeriksa laporan sebelum memberikan verifikasi langsung.",
                                fontSize = 11.sp,
                                color = Color(0xFF14532D)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = photoUriInput ?: "Belum ada foto",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark
                                )
                                OutlinedButton(
                                    onClick = {
                                        photoUriInput = "foto_konsumsi_${System.currentTimeMillis()}.jpg"
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    border = BorderStroke(1.dp, StatusVerified)
                                ) {
                                    Text("Ambil Foto", fontSize = 11.sp, color = StatusVerified)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Transparansi: Laporan mandiri Anda akan tercatat di log audit dan dapat dipantau keluarga.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedDoseForAction?.let {
                            repository.reportDoseTaken(it.id, takenTime, photoUriInput)
                        }
                        selectedDoseForAction = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    enabled = !photoUriInput.isNullOrBlank()
                ) {
                    Text("Kirim Laporan Mandiri", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDoseForAction = null }) {
                    Text("Batal", fontSize = 13.sp, color = TextMuted)
                }
            }
        )
    }

    // ── Dialog: Preview Foto Bukti ──
    if (previewPhotoDose != null) {
        val dose = previewPhotoDose!!
        AlertDialog(
            onDismissRequest = { previewPhotoDose = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceCard,
            title = {
                Text("Bukti Foto Minum Obat", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextDark)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(46.dp),
                                    tint = TealPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = dose.photoUri ?: "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TextDark
                                )
                                Text(
                                    text = "Terlampir pada laporan pasien",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${dose.medicationName} (${dose.dosage})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextDark
                    )
                    Text(
                        text = "Waktu minum dilaporkan: ${dose.takenAt ?: "-"}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { previewPhotoDose = null },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Tutup", fontSize = 13.sp)
                }
            }
        )
    }

    // ── Dialog: Tandai Terlewat ──
    if (selectedDoseForAction != null && actionType == "MISS") {
        AlertDialog(
            onDismissRequest = { selectedDoseForAction = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = StatusMissed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tandai Terlewat", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextDark)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Anda akan mencatat jadwal berikut sebagai TERLEWAT:",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${selectedDoseForAction?.medicationName} (${selectedDoseForAction?.dosage})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = missReason,
                        onValueChange = { missReason = it },
                        label = { Text("Alasan (misal: tertidur / puasa)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StatusMissed,
                            unfocusedBorderColor = SurfaceBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = StatusMissedBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ Penting: Jangan menggandakan dosis berikutnya tanpa petunjuk dokter atau apoteker.",
                            modifier = Modifier.padding(10.dp),
                            fontSize = 11.sp,
                            color = StatusMissedText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedDoseForAction?.let {
                            repository.reportDoseMissed(it.id, missReason)
                        }
                        selectedDoseForAction = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusMissed)
                ) {
                    Text("Tandai Terlewat", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDoseForAction = null }) {
                    Text("Batal", fontSize = 13.sp, color = TextMuted)
                }
            }
        )
    }

    // ── Dialog: Tunda Pengingat ──
    if (selectedDoseForAction != null && actionType == "SNOOZE") {
        AlertDialog(
            onDismissRequest = { selectedDoseForAction = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Snooze, contentDescription = null, tint = StatusPending, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tunda Pengingat", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextDark)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Pilih durasi penundaan pengingat untuk ${selectedDoseForAction?.medicationName}:",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                selectedDoseForAction?.let { repository.snoozeDose(it.id, 15) }
                                selectedDoseForAction = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("15 Mnt", fontSize = 12.sp)
                        }
                        FilledTonalButton(
                            onClick = {
                                selectedDoseForAction?.let { repository.snoozeDose(it.id, 30) }
                                selectedDoseForAction = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("30 Mnt", fontSize = 12.sp)
                        }
                        FilledTonalButton(
                            onClick = {
                                selectedDoseForAction?.let { repository.snoozeDose(it.id, 60) }
                                selectedDoseForAction = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("1 Jam", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedDoseForAction = null }) {
                    Text("Tutup", fontSize = 13.sp, color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun AdherenceMetricCard(
    modifier: Modifier = Modifier,
    count: Int,
    label: String,
    sublabel: String,
    tint: Color,
    bgTint: Color,
    icon: ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bgTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$count",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = tint
            )

            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )

            Text(
                text = sublabel,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun DoseEventCard(
    dose: DoseEvent,
    onTakeClick: () -> Unit,
    onMissClick: () -> Unit,
    onSnoozeClick: () -> Unit,
    onViewPhoto: ((DoseEvent) -> Unit)? = null
) {
    val isPending = dose.status == DoseStatus.MENUNGGU_LAPORAN || dose.status == DoseStatus.TERJADWAL

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(
            1.dp,
            if (dose.status == DoseStatus.TERVERIFIKASI_LANGSUNG) Color(0xFF86EFAC)
            else if (dose.status == DoseStatus.MENUNGGU_LAPORAN) Color(0xFFFDE68A)
            else SurfaceBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon + Name + DoseStatusBadge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (dose.status) {
                                    DoseStatus.TERVERIFIKASI_LANGSUNG -> StatusVerifiedBg
                                    DoseStatus.DILAPORKAN_DIMINUM -> StatusTakenBg
                                    DoseStatus.MENUNGGU_LAPORAN -> StatusPendingBg
                                    DoseStatus.TERLEWAT -> StatusMissedBg
                                    else -> TealContainer
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Medication,
                            contentDescription = null,
                            tint = when (dose.status) {
                                DoseStatus.TERVERIFIKASI_LANGSUNG -> StatusVerified
                                DoseStatus.DILAPORKAN_DIMINUM -> StatusTaken
                                DoseStatus.MENUNGGU_LAPORAN -> StatusPending
                                DoseStatus.TERLEWAT -> StatusMissed
                                else -> TealPrimary
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = dose.medicationName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextDark
                        )
                        Text(
                            text = "${dose.dosage} • ${dose.instructions}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                DoseStatusBadge(status = dose.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time & Schedule Row
            Surface(
                color = SurfaceSubtle,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Jadwal: ${dose.scheduledTime}" + (if (dose.snoozedUntil != null) " (Tunda: ${dose.snoozedUntil})" else ""),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                    }

                    if (dose.takenAt != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = StatusTaken, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Diminum: ${dose.takenAt}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusTaken
                            )
                        }
                    }
                }
            }

            // Reminder Warning Banner
            if (dose.reminderCount > 0 && isPending) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pengingat: Pemantau mengingatkan Anda (${dose.reminderCount}x). Jangan lupa minum obat ya!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            // Direct Verification Proof Certificate
            if (dose.status == DoseStatus.TERVERIFIKASI_LANGSUNG && dose.verification != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = StatusVerified, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Terverifikasi Pengamatan Langsung",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusVerifiedText
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Oleh ${dose.verification.verifierName} pada ${dose.verification.verifiedAt}",
                            fontSize = 11.sp,
                            color = Color(0xFF14532D)
                        )
                        Text(
                            text = "\"${dose.verification.statement}\"",
                            fontSize = 10.sp,
                            color = Color(0xFF166534),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (dose.status == DoseStatus.DILAPORKAN_DIMINUM) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Laporan mandiri tercatat (${dose.reportedAt}). Menunggu verifikasi pengamatan langsung dari pemantau.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                )
            } else if (dose.status == DoseStatus.TERLEWAT) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Dosis dilaporkan terlewat (${dose.reportedAt}). ${dose.notes ?: ""}",
                    fontSize = 11.sp,
                    color = StatusMissedText
                )
            }

            // Photo Proof Attachment
            if (dose.photoUri != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = TealContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bukti Foto Terlampir",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TealPrimary
                            )
                        }
                        if (onViewPhoto != null) {
                            Text(
                                text = "Lihat Foto",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onViewPhoto(dose) }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }

            // Action Buttons
            if (isPending) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onTakeClick,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sudah Minum", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onMissClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, Color(0xFFFCA5A5)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusMissed)
                    ) {
                        Text("Terlewat", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    FilledTonalIconButton(
                        onClick = onSnoozeClick,
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Snooze, contentDescription = "Tunda", tint = TextDark, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DoseStatusBadge(status: DoseStatus) {
    val (bgColor, textColor, dotColor) = when (status) {
        DoseStatus.TERJADWAL -> Triple(SurfaceSubtle, TextMuted, StatusScheduled)
        DoseStatus.MENUNGGU_LAPORAN -> Triple(StatusPendingBg, StatusPendingText, StatusPending)
        DoseStatus.DILAPORKAN_DIMINUM -> Triple(StatusTakenBg, StatusTakenText, StatusTaken)
        DoseStatus.TERVERIFIKASI_LANGSUNG -> Triple(StatusVerifiedBg, StatusVerifiedText, StatusVerified)
        DoseStatus.TERLEWAT -> Triple(StatusMissedBg, StatusMissedText, StatusMissed)
        DoseStatus.TERLAMBAT_DILAPORKAN -> Triple(StatusLateBg, StatusLateText, StatusLate)
        DoseStatus.DIBATALKAN -> Triple(SurfaceSubtle, TextLight, Color.Gray)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = status.displayName,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

// ---------------- MEDICATIONS SCREEN ----------------

@Composable
fun MedicationListScreen(
    repository: MediGuardRepository
) {
    val medications by repository.medications.collectAsState()
    val schedules by repository.schedules.collectAsState()
    val rooms by repository.rooms.collectAsState()

    var showAddMedDialog by remember { mutableStateOf(false) }
    var selectedMedForSchedule by remember { mutableStateOf<Medication?>(null) }
    var showArchived by remember { mutableStateOf(false) }

    val activeMeds = medications.filter { it.isArchived == showArchived }

    Scaffold(
        containerColor = AppBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddMedDialog = true },
                containerColor = TealPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Tambah Obat", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daftar Obat Pasien",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Kelola obat resep & jadwal konsumsi rutin Anda",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = !showArchived,
                        onClick = { showArchived = false },
                        label = { Text("Aktif (${medications.count { !it.isArchived }})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealContainer,
                            selectedLabelColor = TealPrimary
                        )
                    )
                    FilterChip(
                        selected = showArchived,
                        onClick = { showArchived = true },
                        label = { Text("Arsip", fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (activeMeds.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(TealContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Medication, contentDescription = null, tint = TealPrimary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (showArchived) "Tidak ada obat yang diarsipkan" else "Belum ada obat aktif",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tambahkan obat baru untuk mulai membuat jadwal konsumsi.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(activeMeds) { med ->
                        val medSchedules = schedules.filter { it.medicationId == med.id && it.isActive }

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
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(TealContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Medication, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(22.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = med.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                                            Surface(
                                                color = SurfaceSubtle,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "${med.form} • ${med.dosage}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextMuted,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (!med.isArchived) {
                                        TextButton(
                                            onClick = { repository.archiveMedication(med.id) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Arsipkan", fontSize = 12.sp, color = TextMuted)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(text = "Petunjuk: ${med.instructions}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextDark)
                                        if (med.notes.isNotBlank()) {
                                            Text(text = "Catatan: ${med.notes}", fontSize = 11.sp, color = TextMuted)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(text = "Jadwal Pengingat:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextDark)
                                Spacer(modifier = Modifier.height(4.dp))
                                if (medSchedules.isEmpty()) {
                                    Text(text = "Belum ada jadwal minum aktif.", fontSize = 12.sp, color = TextMuted)
                                } else {
                                    medSchedules.forEach { s ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${s.frequency}: Jam ${s.times.joinToString(", ")}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TextDark
                                            )
                                        }
                                    }
                                }

                                if (!med.isArchived) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = { selectedMedForSchedule = med },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, TealPrimary)
                                    ) {
                                        Text("+ Tambah Jadwal Baru", fontSize = 12.sp, color = TealPrimary, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Medication Dialog
    if (showAddMedDialog) {
        var name by remember { mutableStateOf("") }
        var form by remember { mutableStateOf("Tablet") }
        var dosage by remember { mutableStateOf("") }
        var instructions by remember { mutableStateOf("Sesudah makan") }
        var notes by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        val activeRoom = rooms.firstOrNull()

        AlertDialog(
            onDismissRequest = { showAddMedDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceCard,
            title = {
                Text("Tambah Obat Baru", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (errorMsg != null) {
                        Text(text = errorMsg ?: "", color = StatusMissed, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Obat (misal: Amlodipine)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = SurfaceBorder)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = form,
                        onValueChange = { form = it },
                        label = { Text("Bentuk Sediaan (Tablet/Kapsul/Sirup)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = SurfaceBorder)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosis (misal: 5 mg / 1 tablet)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = SurfaceBorder)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Petunjuk Konsumsi (misal: Sesudah makan)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = SurfaceBorder)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Dokter (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = SurfaceBorder)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank() || dosage.isBlank()) {
                            errorMsg = "Nama obat dan dosis wajib diisi."
                            return@Button
                        }
                        val roomId = activeRoom?.id ?: "room_1"
                        repository.addMedication(
                            roomId = roomId,
                            name = name,
                            form = form,
                            dosage = dosage,
                            instructions = instructions,
                            notes = notes,
                            startDate = "",
                            endDate = ""
                        )
                        showAddMedDialog = false
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Simpan Obat", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMedDialog = false }) {
                    Text("Batal", fontSize = 13.sp, color = TextMuted)
                }
            }
        )
    }

    // Add Schedule Dialog
    if (selectedMedForSchedule != null) {
        val med = selectedMedForSchedule!!
        var frequency by remember { mutableStateOf("1x sehari") }
        var time1 by remember { mutableStateOf("08:00") }
        var time2 by remember { mutableStateOf("18:00") }
        var time3 by remember { mutableStateOf("21:00") }

        AlertDialog(
            onDismissRequest = { selectedMedForSchedule = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceCard,
            title = {
                Text("Atur Jadwal: ${med.name}", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextDark)
            },
            text = {
                Column {
                    Text("Pilih Frekuensi Konsumsi:", fontSize = 13.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = frequency == "1x sehari",
                            onClick = { frequency = "1x sehari" },
                            label = { Text("1x Sehari") }
                        )
                        FilterChip(
                            selected = frequency == "2x sehari",
                            onClick = { frequency = "2x sehari" },
                            label = { Text("2x Sehari") }
                        )
                        FilterChip(
                            selected = frequency == "3x sehari",
                            onClick = { frequency = "3x sehari" },
                            label = { Text("3x Sehari") }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = time1,
                        onValueChange = { time1 = it },
                        label = { Text("Waktu 1 (HH:mm)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (frequency == "2x sehari" || frequency == "3x sehari") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = time2,
                            onValueChange = { time2 = it },
                            label = { Text("Waktu 2 (HH:mm)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    if (frequency == "3x sehari") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = time3,
                            onValueChange = { time3 = it },
                            label = { Text("Waktu 3 (HH:mm)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val times = when (frequency) {
                            "1x sehari" -> listOf(time1)
                            "2x sehari" -> listOf(time1, time2)
                            else -> listOf(time1, time2, time3)
                        }
                        repository.addSchedule(
                            medicationId = med.id,
                            frequency = frequency,
                            times = times,
                            startDate = "",
                            endDate = ""
                        )
                        selectedMedForSchedule = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Simpan Jadwal", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMedForSchedule = null }) {
                    Text("Batal", fontSize = 13.sp, color = TextMuted)
                }
            }
        )
    }
}

// ---------------- ROOMS & MEMBERS SCREEN ----------------

@Composable
fun PatientRoomsScreen(
    repository: MediGuardRepository
) {
    val rooms by repository.rooms.collectAsState()
    val members by repository.roomMembers.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    val myRoom = rooms.firstOrNull { it.ownerUserId == currentUser?.id } ?: rooms.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Medication Room",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "Ruang privat pengawasan kepatuhan obat Anda",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Button(
                onClick = { showCreateRoomDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("+ Buat Ruang", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (myRoom == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Anda belum memiliki Medication Room.", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Buat ruang pemantauan agar keluarga dapat mendampingi jadwal minum obat Anda.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showCreateRoomDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text("Buat Medication Room Sekarang")
                    }
                }
            }
        } else {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceCard,
                contentColor = TealPrimary
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Ruang & Kode", fontWeight = FontWeight.SemiBold) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Anggota", fontWeight = FontWeight.SemiBold) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Izin Privasi", fontWeight = FontWeight.SemiBold) })
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(TealContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = myRoom.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextDark)
                                    Text(text = myRoom.description, fontSize = 12.sp, color = TextMuted)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = SurfaceBorder)
                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Kode Undangan Pemantau (PIN):",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                color = Color(0xFFE6F4F1),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.5.dp, Color(0xFF99F6E4)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = myRoom.inviteCode,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TealPrimary,
                                            letterSpacing = 2.sp
                                        )
                                        Text(text = "Aktif & Dapat Digunakan", fontSize = 11.sp, color = TextMuted)
                                    }

                                    OutlinedButton(
                                        onClick = { repository.rotateInviteCode(myRoom.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, TealPrimary)
                                    ) {
                                        Text("Acak Ulang", fontSize = 12.sp, color = TealPrimary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = SurfaceSubtle,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Privasi Terlindungi: Siapapun yang memasukkan kode ini tetap memerlukan persetujuan Anda di tab 'Anggota' sebelum dapat melihat jadwal obat.",
                                        fontSize = 11.sp,
                                        color = TextDark,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    val roomMembers = members.filter { it.roomId == myRoom.id }
                    val pendingRequests = roomMembers.filter { it.status == MembershipStatus.PENDING }
                    val approvedCaregivers = roomMembers.filter { it.status == MembershipStatus.APPROVED && it.role == UserRole.PEMANTAU }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (pendingRequests.isNotEmpty()) {
                            item {
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Permintaan Bergabung (${pendingRequests.size})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF92400E)
                                            )
                                            Text(
                                                text = "Pilih apakah Anda mengizinkan mereka memantau pengobatan Anda.",
                                                fontSize = 11.sp,
                                                color = Color(0xFF92400E)
                                            )
                                        }
                                    }
                                }
                            }

                            items(pendingRequests) { req ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                    border = BorderStroke(1.dp, SurfaceBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = req.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                                            Text(text = req.userEmail, fontSize = 12.sp, color = TextMuted)
                                            Text(text = "Mengajukan: ${req.requestedAt}", fontSize = 10.sp, color = TextLight)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = { repository.approveMember(req.id) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Text("Setujui", fontSize = 12.sp)
                                            }
                                            OutlinedButton(
                                                onClick = { repository.rejectMember(req.id) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Text("Tolak", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Pemantau Aktif (${approvedCaregivers.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextDark
                            )
                        }

                        if (approvedCaregivers.isEmpty()) {
                            item {
                                Surface(
                                    color = SurfaceCard,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, SurfaceBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Belum ada pemantau aktif yang disetujui.",
                                        modifier = Modifier.padding(16.dp),
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        } else {
                            items(approvedCaregivers) { caregiver ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                    border = BorderStroke(1.dp, SurfaceBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = caregiver.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                                            Text(text = caregiver.userEmail, fontSize = 12.sp, color = TextMuted)
                                            Surface(
                                                color = StatusVerifiedBg,
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                Text(
                                                    text = "● Akses Terverifikasi",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = StatusVerifiedText,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { repository.revokeMemberAccess(caregiver.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusMissed),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("Cabut Akses", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    val approvedCaregivers = members.filter {
                        it.roomId == myRoom.id && it.status == MembershipStatus.APPROVED && it.role == UserRole.PEMANTAU
                    }

                    if (approvedCaregivers.isEmpty()) {
                        Text(
                            text = "Belum ada pemantau aktif untuk dikonfigurasi hak aksesnya.",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(approvedCaregivers) { c ->
                                var canSchedules by remember { mutableStateOf(c.canViewSchedules) }
                                var canActivities by remember { mutableStateOf(c.canViewActivities) }
                                var canStats by remember { mutableStateOf(c.canViewStats) }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                    border = BorderStroke(1.dp, SurfaceBorder)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = "Pengaturan Privasi: ${c.userName}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                                        Text(text = c.userEmail, fontSize = 12.sp, color = TextMuted)
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = canSchedules,
                                                onCheckedChange = { canSchedules = it },
                                                colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
                                            )
                                            Text("Izinkan Melihat Jadwal Obat", fontSize = 13.sp, color = TextDark)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = canActivities,
                                                onCheckedChange = { canActivities = it },
                                                colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
                                            )
                                            Text("Izinkan Melihat Aktivitas & Bukti Foto", fontSize = 13.sp, color = TextDark)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = canStats,
                                                onCheckedChange = { canStats = it },
                                                colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
                                            )
                                            Text("Izinkan Melihat Statistik Kepatuhan", fontSize = 13.sp, color = TextDark)
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                repository.updateMemberPermissions(
                                                    memberId = c.id,
                                                    canViewSchedules = canSchedules,
                                                    canViewActivities = canActivities,
                                                    canViewStats = canStats
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                        ) {
                                            Text("Simpan Perubahan Izin", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Room Dialog
    if (showCreateRoomDialog) {
        var roomName by remember { mutableStateOf("") }
        var roomDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateRoomDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceCard,
            title = { Text("Buat Medication Room Baru", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextDark) },
            text = {
                Column {
                    OutlinedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = { Text("Nama Ruang (misal: Ruang Obat Hendra)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = SurfaceBorder)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = roomDesc,
                        onValueChange = { roomDesc = it },
                        label = { Text("Deskripsi (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = SurfaceBorder)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (roomName.isNotBlank()) {
                            repository.createRoom(roomName, roomDesc)
                            showCreateRoomDialog = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Buat Ruang", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateRoomDialog = false }) {
                    Text("Batal", fontSize = 13.sp, color = TextMuted)
                }
            }
        )
    }
}

// ---------------- HISTORY & ADHERENCE STATISTICS SCREEN ----------------

@Composable
fun PatientHistoryScreen(
    repository: MediGuardRepository
) {
    val auditLogs by repository.auditLogs.collectAsState()
    val stats = repository.getAdherenceStats()

    var selectedView by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Riwayat & Kepatuhan",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "Transparansi catatan kepatuhan dan linimasa pengobatan",
            fontSize = 12.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        TabRow(
            selectedTabIndex = selectedView,
            containerColor = SurfaceCard,
            contentColor = TealPrimary
        ) {
            Tab(selected = selectedView == 0, onClick = { selectedView = 0 }, text = { Text("Statistik Kepatuhan", fontWeight = FontWeight.SemiBold) })
            Tab(selected = selectedView == 1, onClick = { selectedView = 1 }, text = { Text("Linimasa Audit", fontWeight = FontWeight.SemiBold) })
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedView) {
            0 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, SurfaceBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text("Ringkasan Tingkat Kepatuhan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "${stats.compliancePercentage}%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                                        Text(text = "Laporan Mandiri Pasien", fontSize = 12.sp, color = TextMuted)
                                    }
                                    Column {
                                        Text(text = "${stats.verifiedPercentage}%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = StatusVerified)
                                        Text(text = "Pengamatan Langsung", fontSize = 12.sp, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Rincian Jadwal Jatuh Tempo", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Jadwal Terjadwal:", fontSize = 13.sp, color = TextMuted)
                                    Text("${stats.totalDue} dosis", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextDark)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Dilaporkan Diminum (Mandiri):", fontSize = 13.sp, color = TextMuted)
                                    Text("${stats.reportedTaken} dosis", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StatusTaken)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Terverifikasi Nyata (Pemantau):", fontSize = 13.sp, color = TextMuted)
                                    Text("${stats.directlyVerified} dosis", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StatusVerified)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Dosis Terlewat:", fontSize = 13.sp, color = TextMuted)
                                    Text("${stats.missed} dosis", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StatusMissed)
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            color = TealContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Catatan Integritas Data Medis:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TealPrimary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "1. Laporan mandiri dan verifikasi langsung dibedakan secara tegas.\n" +
                                            "2. Verifikasi hanya sah jika pemantau menyaksikan langsung saat konsumsi.\n" +
                                            "3. Riwayat ini mencerminkan keterbukaan tanpa saling menghakimi.",
                                    fontSize = 11.sp,
                                    color = OnTealContainer,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
            1 -> {
                if (auditLogs.isEmpty()) {
                    Text("Belum ada catatan aktivitas di log audit.", fontSize = 13.sp, color = TextMuted)
                } else {
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
            }
        }
    }
}
