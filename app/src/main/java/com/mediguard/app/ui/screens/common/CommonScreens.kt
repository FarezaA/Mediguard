package com.mediguard.app.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.mediguard.app.data.model.NotificationItem
import com.mediguard.app.data.model.UserRole
import com.mediguard.app.data.repository.MediGuardRepository
import com.mediguard.app.ui.navigation.NavRoute
import com.mediguard.app.ui.screens.caregiver.*
import com.mediguard.app.ui.screens.patient.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    repository: MediGuardRepository,
    onNavigate: (String) -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val notifications by repository.notifications.collectAsState()

    var selectedPatientTab by remember { mutableStateOf(0) }
    var selectedCaregiverTab by remember { mutableStateOf(0) }

    val unreadNotifCount = notifications.count { it.userId == currentUser?.id && !it.isRead }
    val isPatient = currentUser?.activeRole == UserRole.PASIEN

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MediGuard",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (isPatient) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = currentUser?.activeRole?.displayName ?: "User",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigate(NavRoute.Notifications.route) }) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifCount > 0) {
                                    Badge { Text("$unreadNotifCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifikasi")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                if (isPatient) {
                    NavigationBarItem(
                        selected = selectedPatientTab == 0,
                        onClick = { selectedPatientTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                        label = { Text("Beranda") }
                    )
                    NavigationBarItem(
                        selected = selectedPatientTab == 1,
                        onClick = { selectedPatientTab = 1 },
                        icon = { Icon(Icons.Default.Medication, contentDescription = "Obat") },
                        label = { Text("Obat") }
                    )
                    NavigationBarItem(
                        selected = selectedPatientTab == 2,
                        onClick = { selectedPatientTab = 2 },
                        icon = { Icon(Icons.Default.MeetingRoom, contentDescription = "Ruang") },
                        label = { Text("Ruang") }
                    )
                    NavigationBarItem(
                        selected = selectedPatientTab == 3,
                        onClick = { selectedPatientTab = 3 },
                        icon = { Icon(Icons.Default.History, contentDescription = "Riwayat") },
                        label = { Text("Riwayat") }
                    )
                    NavigationBarItem(
                        selected = selectedPatientTab == 4,
                        onClick = { selectedPatientTab = 4 },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                        label = { Text("Profil") }
                    )
                } else {
                    NavigationBarItem(
                        selected = selectedCaregiverTab == 0,
                        onClick = { selectedCaregiverTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                        label = { Text("Beranda") }
                    )
                    NavigationBarItem(
                        selected = selectedCaregiverTab == 1,
                        onClick = { selectedCaregiverTab = 1 },
                        icon = { Icon(Icons.Default.MeetingRoom, contentDescription = "Ruang") },
                        label = { Text("Ruang") }
                    )
                    NavigationBarItem(
                        selected = selectedCaregiverTab == 2,
                        onClick = { selectedCaregiverTab = 2 },
                        icon = { Icon(Icons.Default.Timeline, contentDescription = "Aktivitas") },
                        label = { Text("Aktivitas") }
                    )
                    NavigationBarItem(
                        selected = selectedCaregiverTab == 3,
                        onClick = { selectedCaregiverTab = 3 },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifCount > 0) {
                                        Badge { Text("$unreadNotifCount") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifikasi")
                            }
                        },
                        label = { Text("Notifikasi") }
                    )
                    NavigationBarItem(
                        selected = selectedCaregiverTab == 4,
                        onClick = { selectedCaregiverTab = 4 },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                        label = { Text("Profil") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (isPatient) {
                when (selectedPatientTab) {
                    0 -> PatientDashboardScreen(
                        repository = repository,
                        onNavigateToMedications = { selectedPatientTab = 1 },
                        onNavigateToRooms = { selectedPatientTab = 2 }
                    )
                    1 -> MedicationListScreen(repository = repository)
                    2 -> PatientRoomsScreen(repository = repository)
                    3 -> PatientHistoryScreen(repository = repository)
                    4 -> ProfileScreen(
                        repository = repository,
                        onLogout = { onNavigate(NavRoute.Login.route) }
                    )
                }
            } else {
                when (selectedCaregiverTab) {
                    0 -> CaregiverDashboardScreen(
                        repository = repository,
                        onNavigateToJoin = { onNavigate(NavRoute.JoinRoom.route) },
                        onNavigateToRooms = { selectedCaregiverTab = 1 }
                    )
                    1 -> CaregiverRoomsScreen(repository = repository)
                    2 -> CaregiverActivityScreen(repository = repository)
                    3 -> NotificationsScreen(repository = repository)
                    4 -> ProfileScreen(
                        repository = repository,
                        onLogout = { onNavigate(NavRoute.Login.route) }
                    )
                }
            }
        }
    }
}

// ---------------- NOTIFICATIONS SCREEN ----------------

@Composable
fun NotificationsScreen(
    repository: MediGuardRepository
) {
    val notifications = repository.getNotificationsForCurrentUser()

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
                text = "Pusat Notifikasi",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (notifications.any { !it.isRead }) {
                TextButton(onClick = { repository.markAllNotificationsAsRead() }) {
                    Text("Tandai Semua Dibaca", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (notifications.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Belum ada notifikasi baru.", fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notifications) { notif ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (notif.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = notif.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = notif.createdAt,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = notif.body, fontSize = 13.sp)

                            if (!notif.isRead) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { repository.markNotificationAsRead(notif.id) }) {
                                        Text("Tandai Dibaca", fontSize = 11.sp)
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

// ---------------- PROFILE & SETTINGS SCREEN ----------------

@Composable
fun ProfileScreen(
    repository: MediGuardRepository,
    onLogout: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val allUsers by repository.users.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Profil & Pengaturan",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = currentUser?.name ?: "-", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = currentUser?.email ?: "-", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { showEditDialog = true }) {
                        Text("Ubah")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Peran Aktif Saat Ini: ${currentUser?.activeRole?.displayName}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val newRole = if (currentUser?.activeRole == UserRole.PASIEN) UserRole.PEMANTAU else UserRole.PASIEN
                            repository.switchRole(newRole)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (currentUser?.activeRole == UserRole.PASIEN) "Beralih ke Peran Pemantau" else "Beralih ke Peran Pasien"
                        )
                    }
                }
            }
        }

        // Demo Fast User Switcher (For grading / presentation ease)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ganti Akun Demo Pengujian:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(8.dp))
                allUsers.forEach { u ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "${u.name} (${u.activeRole.displayName})", fontSize = 13.sp)
                            Text(text = u.email, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        if (u.id != currentUser?.id) {
                            OutlinedButton(onClick = { repository.switchUser(u.id) }) {
                                Text("Ganti", fontSize = 11.sp)
                            }
                        } else {
                            Text("Aktif", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = { repository.seedInitialDemoData() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset & Muat Ulang Data Demo")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = {
                repository.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Keluar")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Keluar dari Akun")
        }
    }

    if (showEditDialog) {
        var editName by remember { mutableStateOf(currentUser?.name ?: "") }
        var editEmail by remember { mutableStateOf(currentUser?.email ?: "") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Ubah Profil") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nama") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    repository.updateProfile(editName, editEmail)
                    showEditDialog = false
                }) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
