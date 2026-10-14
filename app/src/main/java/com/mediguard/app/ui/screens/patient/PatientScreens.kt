package com.mediguard.app.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

    val todaySdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
    val todayFormatted = todaySdf.format(Date())

    // Summary counts
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
                        text = "Halo, ${currentUser?.name ?: "Pasien"}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = todayFormatted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ruang Aktif: ${rooms.firstOrNull()?.name ?: "Belum ada ruang"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item {
            Text(text = "Ringkasan Hari Ini", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "$reportedCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StatusTaken)
                        Text(text = "Dilaporkan", fontSize = 11.sp)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "$verifiedCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StatusVerified)
                        Text(text = "Terverifikasi", fontSize = 11.sp)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "$pendingReportCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StatusPending)
                        Text(text = "Menunggu", fontSize = 11.sp)
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
                Text(text = "Jadwal Konsumsi Hari Ini", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                TextButton(onClick = onNavigateToMedications) {
                    Text("Kelola Obat")
                }
            }
        }

        if (todayDoses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Belum ada jadwal obat untuk hari ini.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onNavigateToMedications) {
                            Text("Tambah Obat & Jadwal")
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
    }

    // Confirmation dialog for Taking Dose
    if (selectedDoseForAction != null && actionType == "TAKE") {
        AlertDialog(
            onDismissRequest = { selectedDoseForAction = null },
            title = { Text("Konfirmasi Konsumsi Obat") },
            text = {
                Column {
                    Text("Apakah Anda menyatakan telah meminum obat berikut?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Obat: ${selectedDoseForAction?.medicationName} (${selectedDoseForAction?.dosage})", fontWeight = FontWeight.Bold)
                    Text("Jadwal: ${selectedDoseForAction?.scheduledTime}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = takenTime,
                        onValueChange = { takenTime = it },
                        label = { Text("Waktu Minum (HH:mm)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Photo Proof Requirement
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Foto", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Foto Bukti Minum Obat (Wajib):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pemantau akan memeriksa foto ini sebelum memberikan verifikasi langsung.",
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = photoUriInput ?: "Belum ada foto",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            photoUriInput = "foto_bukti_kamera_${System.currentTimeMillis()}.jpg"
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Ambil Foto", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Catatan: Foto dan laporan akan dikirim ke pemantau untuk diperiksa secara transparan.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
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
                    enabled = !photoUriInput.isNullOrBlank()
                ) {
                    Text("Ya, Kirim Laporan & Foto")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDoseForAction = null }) {
                    Text("Batal")
                }
            }
        )
    }

    if (previewPhotoDose != null) {
        val dose = previewPhotoDose!!
        AlertDialog(
            onDismissRequest = { previewPhotoDose = null },
            title = { Text("Foto Bukti Minum Obat") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Foto", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Bukti Foto: ${dose.photoUri}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Terlampir pada laporan pasien", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Obat: ${dose.medicationName} (${dose.dosage})", fontWeight = FontWeight.Bold)
                    Text("Waktu Minum: ${dose.takenAt ?: "-"}", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(onClick = { previewPhotoDose = null }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Confirmation dialog for Missing Dose
    if (selectedDoseForAction != null && actionType == "MISS") {
        AlertDialog(
            onDismissRequest = { selectedDoseForAction = null },
            title = { Text("Tandai Dosis Terlewat") },
            text = {
                Column {
                    Text("Anda akan mencatat dosis ini sebagai TERLEWAT.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Obat: ${selectedDoseForAction?.medicationName} (${selectedDoseForAction?.dosage})", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = missReason,
                        onValueChange = { missReason = it },
                        label = { Text("Alasan (opsional, misal: tertidur/puasa)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Peringatan: Jangan menggandakan dosis berikutnya tanpa instruksi dokter.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error
                    )
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Tandai Terlewat")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDoseForAction = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog for Snoozing
    if (selectedDoseForAction != null && actionType == "SNOOZE") {
        AlertDialog(
            onDismissRequest = { selectedDoseForAction = null },
            title = { Text("Tunda Pengingat") },
            text = {
                Column {
                    Text("Pilih waktu penundaan pengingat:")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = {
                            selectedDoseForAction?.let { repository.snoozeDose(it.id, 15) }
                            selectedDoseForAction = null
                        }) {
                            Text("15 Menit")
                        }
                        FilledTonalButton(onClick = {
                            selectedDoseForAction?.let { repository.snoozeDose(it.id, 30) }
                            selectedDoseForAction = null
                        }) {
                            Text("30 Menit")
                        }
                        FilledTonalButton(onClick = {
                            selectedDoseForAction?.let { repository.snoozeDose(it.id, 60) }
                            selectedDoseForAction = null
                        }) {
                            Text("1 Jam")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedDoseForAction = null }) {
                    Text("Tutup")
                }
            }
        )
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
                        text = "${dose.dosage} • ${dose.instructions}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DoseStatusBadge(status = dose.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jadwal: ${dose.scheduledTime}" + (if (dose.snoozedUntil != null) " (Ditunda: ${dose.snoozedUntil})" else ""),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (dose.takenAt != null) {
                    Text(text = "Diminum: ${dose.takenAt}", fontSize = 12.sp, color = StatusTaken)
                }
            }

            // Reminder count warning if pending/late
            if (dose.reminderCount > 0 && (dose.status == DoseStatus.MENUNGGU_LAPORAN || dose.status == DoseStatus.TERJADWAL)) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "Pengingat", tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pengingat: Pemantau & sistem telah mengingatkan Anda sebanyak ${dose.reminderCount} kali!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            // Photo proof attached
            if (dose.photoUri != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.extraSmall,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📷 Bukti Foto: ${dose.photoUri}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (onViewPhoto != null) {
                            TextButton(
                                onClick = { onViewPhoto(dose) },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("Lihat Foto", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Status details
            if (dose.status == DoseStatus.TERVERIFIKASI_LANGSUNG && dose.verification != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "✓ Terverifikasi Langsung oleh ${dose.verification.verifierName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusVerified
                        )
                        Text(
                            text = "Waktu verifikasi: ${dose.verification.verifiedAt} (${dose.verification.statement})",
                            fontSize = 11.sp
                        )
                    }
                }
            } else if (dose.status == DoseStatus.DILAPORKAN_DIMINUM) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Laporan mandiri pasien (${dose.reportedAt}). Menunggu verifikasi pengamatan langsung pemantau.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            } else if (dose.status == DoseStatus.TERLEWAT) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Dilaporkan terlewat (${dose.reportedAt}). ${dose.notes ?: ""}",
                    fontSize = 11.sp,
                    color = StatusMissed
                )
            }

            // Action buttons if pending report
            if (dose.status == DoseStatus.MENUNGGU_LAPORAN || dose.status == DoseStatus.TERJADWAL) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onTakeClick,
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text("Sudah Minum", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onMissClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Terlewat", fontSize = 12.sp)
                    }
                    IconButton(onClick = onSnoozeClick) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Tunda")
                    }
                }
            }
        }
    }
}

@Composable
fun DoseStatusBadge(status: DoseStatus) {
    val (bgColor, textColor) = when (status) {
        DoseStatus.TERJADWAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        DoseStatus.MENUNGGU_LAPORAN -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        DoseStatus.DILAPORKAN_DIMINUM -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
        DoseStatus.TERVERIFIKASI_LANGSUNG -> Color(0xFFDCFCE7) to Color(0xFF166534)
        DoseStatus.TERLEWAT -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        DoseStatus.TERLAMBAT_DILAPORKAN -> Color(0xFFFED7AA) to Color(0xFF9A3412)
        DoseStatus.DIBATALKAN -> Color(0xFFF3F4F6) to Color(0xFF4B5563)
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
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddMedDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Obat")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Obat Pasien",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                FilterChip(
                    selected = showArchived,
                    onClick = { showArchived = !showArchived },
                    label = { Text(if (showArchived) "Arsip" else "Aktif") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (activeMeds.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Belum ada obat yang terdaftar di kategori ini.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showAddMedDialog = true }) {
                            Text("Tambah Obat Baru")
                        }
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
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = med.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(text = "${med.form} • ${med.dosage}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (!med.isArchived) {
                                        TextButton(onClick = { repository.archiveMedication(med.id) }) {
                                            Text("Arsipkan", fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Petunjuk: ${med.instructions}", fontSize = 13.sp)
                                if (med.notes.isNotBlank()) {
                                    Text(text = "Catatan: ${med.notes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(text = "Jadwal:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                if (medSchedules.isEmpty()) {
                                    Text(text = "Belum ada jadwal minum.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                } else {
                                    medSchedules.forEach { s ->
                                        Text(
                                            text = "• ${s.frequency}: jam ${s.times.joinToString(", ")}",
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                if (!med.isArchived) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { selectedMedForSchedule = med },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("+ Tambah Jadwal Baru", fontSize = 12.sp)
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
            title = { Text("Tambah Obat Baru") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (errorMsg != null) {
                        Text(text = errorMsg ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Obat") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = form,
                        onValueChange = { form = it },
                        label = { Text("Bentuk Sediaan (Tablet/Kapsul/Sirup)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosis (misal: 500 mg / 1 tablet)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Petunjuk Konsumsi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Tambahan (opsional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isBlank() || dosage.isBlank()) {
                        errorMsg = "Nama obat dan dosis harus diisi."
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
                }) {
                    Text("Simpan Obat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMedDialog = false }) {
                    Text("Batal")
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
            title = { Text("Atur Jadwal untuk ${med.name}") },
            text = {
                Column {
                    Text("Pilih Frekuensi:")
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

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = time1,
                        onValueChange = { time1 = it },
                        label = { Text("Waktu 1 (HH:mm)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (frequency == "2x sehari" || frequency == "3x sehari") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = time2,
                            onValueChange = { time2 = it },
                            label = { Text("Waktu 2 (HH:mm)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (frequency == "3x sehari") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = time3,
                            onValueChange = { time3 = it },
                            label = { Text("Waktu 3 (HH:mm)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
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
                }) {
                    Text("Simpan Jadwal")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMedForSchedule = null }) {
                    Text("Batal")
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
    var selectedTab by remember { mutableStateOf(0) } // 0: Info Ruang & Kode, 1: Anggota & Permintaan, 2: Izin Pemantau

    val myRoom = rooms.firstOrNull { it.ownerUserId == currentUser?.id } ?: rooms.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Medication Room",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(onClick = { showCreateRoomDialog = true }) {
                Text("+ Buat Ruang")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (myRoom == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Anda belum memiliki Medication Room.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showCreateRoomDialog = true }) {
                        Text("Buat Medication Room Sekarang")
                    }
                }
            }
        } else {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Ruang & Kode") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Anggota") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Izin Privasi") })
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // Info Ruang & Invite Code
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = myRoom.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = myRoom.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(text = "Kode Undangan Pemantau:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = myRoom.inviteCode,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = {
                                        repository.rotateInviteCode(myRoom.id)
                                    }) {
                                        Text("Ganti Kode", fontSize = 12.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Bagikan kode ini kepada keluarga/pemantau. Pemantau harus mengajukan permintaan dan disetujui terlebih dahulu sebelum dapat melihat data pengobatan.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                1 -> {
                    // Anggota & Pending Join Requests
                    val roomMembers = members.filter { it.roomId == myRoom.id }
                    val pendingRequests = roomMembers.filter { it.status == MembershipStatus.PENDING }
                    val approvedCaregivers = roomMembers.filter { it.status == MembershipStatus.APPROVED && it.role == UserRole.PEMANTAU }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Pending Requests Section
                        if (pendingRequests.isNotEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Permintaan Masuk (${pendingRequests.size})",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                        Text(
                                            text = "Keluarga/pemantau berikut meminta izin memantau ruang ini:",
                                            fontSize = 12.sp,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                }
                            }

                            items(pendingRequests) { req ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = req.userName, fontWeight = FontWeight.Bold)
                                            Text(text = req.userEmail, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                            Text(text = "Meminta: ${req.requestedAt}", fontSize = 11.sp)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = { repository.approveMember(req.id) }
                                            ) {
                                                Text("Setujui", fontSize = 12.sp)
                                            }
                                            OutlinedButton(
                                                onClick = { repository.rejectMember(req.id) }
                                            ) {
                                                Text("Tolak", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Approved Members Section
                        item {
                            Text(
                                text = "Pemantau Aktif (${approvedCaregivers.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (approvedCaregivers.isEmpty()) {
                            item {
                                Text(
                                    text = "Belum ada pemantau aktif yang disetujui.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        } else {
                            items(approvedCaregivers) { caregiver ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = caregiver.userName, fontWeight = FontWeight.Bold)
                                            Text(text = caregiver.userEmail, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                            Text(
                                                text = "Status: ${caregiver.status.displayName}",
                                                fontSize = 11.sp,
                                                color = StatusVerified,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { repository.revokeMemberAccess(caregiver.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Text("Cabut Akses", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Privacy & Permissions toggles
                    val approvedCaregivers = members.filter {
                        it.roomId == myRoom.id && it.status == MembershipStatus.APPROVED && it.role == UserRole.PEMANTAU
                    }

                    if (approvedCaregivers.isEmpty()) {
                        Text(
                            text = "Belum ada pemantau aktif untuk dikonfigurasi izinnya.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(approvedCaregivers) { c ->
                                var canSchedules by remember { mutableStateOf(c.canViewSchedules) }
                                var canActivities by remember { mutableStateOf(c.canViewActivities) }
                                var canStats by remember { mutableStateOf(c.canViewStats) }

                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = "Pengaturan Izin: ${c.userName}", fontWeight = FontWeight.Bold)
                                        Text(text = c.userEmail, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = canSchedules,
                                                onCheckedChange = { canSchedules = it }
                                            )
                                            Text("Izinkan Melihat Jadwal Obat", fontSize = 13.sp)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = canActivities,
                                                onCheckedChange = { canActivities = it }
                                            )
                                            Text("Izinkan Melihat Aktivitas & Laporan", fontSize = 13.sp)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = canStats,
                                                onCheckedChange = { canStats = it }
                                            )
                                            Text("Izinkan Melihat Statistik Kepatuhan", fontSize = 13.sp)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                repository.updateMemberPermissions(
                                                    memberId = c.id,
                                                    canViewSchedules = canSchedules,
                                                    canViewActivities = canActivities,
                                                    canViewStats = canStats
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Simpan Perubahan Izin")
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
            title = { Text("Buat Medication Room Baru") },
            text = {
                Column {
                    OutlinedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = { Text("Nama Ruang (misal: Pemantauan Ayah)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = roomDesc,
                        onValueChange = { roomDesc = it },
                        label = { Text("Deskripsi (opsional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (roomName.isNotBlank()) {
                        repository.createRoom(roomName, roomDesc)
                        showCreateRoomDialog = false
                    }
                }) {
                    Text("Buat Ruang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateRoomDialog = false }) {
                    Text("Batal")
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
    val doseEvents by repository.doseEvents.collectAsState()
    val stats = repository.getAdherenceStats()

    var selectedView by remember { mutableStateOf(0) } // 0: Statistik Kepatuhan, 1: Log Aktivitas

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Riwayat & Kepatuhan",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = selectedView) {
            Tab(selected = selectedView == 0, onClick = { selectedView = 0 }, text = { Text("Statistik Kepatuhan") })
            Tab(selected = selectedView == 1, onClick = { selectedView = 1 }, text = { Text("Linimasa Aktivitas") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedView) {
            0 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Ringkasan Kepatuhan Pengobatan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "${stats.compliancePercentage}%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text(text = "Tingkat Laporan Mandiri", fontSize = 12.sp)
                                    }
                                    Column {
                                        Text(text = "${stats.verifiedPercentage}%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = StatusVerified)
                                        Text(text = "Terverifikasi Langsung", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Rincian Jadwal Jatuh Tempo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Jadwal Jatuh Tempo:", fontSize = 13.sp)
                                    Text("${stats.totalDue} dosis", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Dilaporkan Diminum (Mandiri):", fontSize = 13.sp)
                                    Text("${stats.reportedTaken} dosis", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StatusTaken)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Terverifikasi Langsung oleh Pemantau:", fontSize = 13.sp)
                                    Text("${stats.directlyVerified} dosis", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StatusVerified)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Dosis Terlewat:", fontSize = 13.sp)
                                    Text("${stats.missed} dosis", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StatusMissed)
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Catatan Metodologi Perhitungan:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(
                                    "1. Laporan mandiri dan verifikasi langsung dibedakan secara transparan.\n" +
                                            "2. Verifikasi hanya sah jika pemantau menyaksikan langsung saat konsumsi.\n" +
                                            "3. Statistik ini bukan penilaian klinis atau jaminan kondisi medis pasien.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            1 -> {
                // Linimasa Aktivitas (Audit logs)
                if (auditLogs.isEmpty()) {
                    Text("Belum ada catatan aktivitas.", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                } else {
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
                                    Text(text = "Pelaku: ${log.actorName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
