package com.mediguard.app.ui.screens.common

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediguard.app.data.model.NotificationItem
import com.mediguard.app.data.model.UserRole
import com.mediguard.app.data.repository.MediGuardRepository
import com.mediguard.app.ui.navigation.NavRoute
import com.mediguard.app.ui.screens.caregiver.*
import com.mediguard.app.ui.screens.patient.*
import com.mediguard.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    repository: MediGuardRepository,
    onNavigate: (String) -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val notifications by repository.notifications.collectAsState()
    val isOnline by repository.isOnline.collectAsState()
    val serverUrl by repository.serverUrl.collectAsState()

    var showServerConfigDialog by remember { mutableStateOf(false) }
    var selectedPatientTab by remember { mutableStateOf(0) }
    var selectedCaregiverTab by remember { mutableStateOf(0) }

    val unreadNotifCount = notifications.count { it.userId == currentUser?.id && !it.isRead }
    val isPatient = currentUser?.activeRole == UserRole.PASIEN

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceCard,
                    titleContentColor = TextDark
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TealContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "MediGuard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (isPatient) TealContainer else AccentCyanContainer,
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                text = currentUser?.activeRole?.displayName ?: "User",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPatient) TealPrimary else Color(0xFF0369A1)
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .clickable { showServerConfigDialog = true },
                        color = if (isOnline) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(50.dp),
                        border = BorderStroke(1.dp, if (isOnline) Color(0xFF86EFAC) else Color(0xFFFCA5A5))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFF16A34A) else Color(0xFFDC2626))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isOnline) "Online" else "Offline",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) Color(0xFF15803D) else Color(0xFFB91C1C)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = { onNavigate(NavRoute.Notifications.route) }) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifCount > 0) {
                                    Badge(
                                        containerColor = StatusMissed,
                                        contentColor = Color.White
                                    ) {
                                        Text("$unreadNotifCount", fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = "Notifikasi",
                                tint = TextDark
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCard,
                tonalElevation = 8.dp
            ) {
                if (isPatient) {
                    NavigationBarItem(
                        selected = selectedPatientTab == 0,
                        onClick = { selectedPatientTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                        label = { Text("Beranda", fontSize = 11.sp, fontWeight = if (selectedPatientTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            indicatorColor = TealContainer
                        )
                    )
                    NavigationBarItem(
                        selected = selectedPatientTab == 1,
                        onClick = { selectedPatientTab = 1 },
                        icon = { Icon(Icons.Default.Medication, contentDescription = "Obat") },
                        label = { Text("Obat", fontSize = 11.sp, fontWeight = if (selectedPatientTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            indicatorColor = TealContainer
                        )
                    )
                    NavigationBarItem(
                        selected = selectedPatientTab == 2,
                        onClick = { selectedPatientTab = 2 },
                        icon = { Icon(Icons.Default.MeetingRoom, contentDescription = "Ruang") },
                        label = { Text("Ruang", fontSize = 11.sp, fontWeight = if (selectedPatientTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            indicatorColor = TealContainer
                        )
                    )
                    NavigationBarItem(
                        selected = selectedPatientTab == 3,
                        onClick = { selectedPatientTab = 3 },
                        icon = { Icon(Icons.Default.History, contentDescription = "Riwayat") },
                        label = { Text("Riwayat", fontSize = 11.sp, fontWeight = if (selectedPatientTab == 3) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            indicatorColor = TealContainer
                        )
                    )
                    NavigationBarItem(
                        selected = selectedPatientTab == 4,
                        onClick = { selectedPatientTab = 4 },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                        label = { Text("Profil", fontSize = 11.sp, fontWeight = if (selectedPatientTab == 4) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            indicatorColor = TealContainer
                        )
                    )
                } else {
                    NavigationBarItem(
                        selected = selectedCaregiverTab == 0,
                        onClick = { selectedCaregiverTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                        label = { Text("Beranda", fontSize = 11.sp, fontWeight = if (selectedCaregiverTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            indicatorColor = TealContainer
                        )
                    )
                    NavigationBarItem(
                        selected = selectedCaregiverTab == 1,
                        onClick = { selectedCaregiverTab = 1 },
                        icon = { Icon(Icons.Default.MeetingRoom, contentDescription = "Ruang") },
                        label = { Text("Ruang", fontSize = 11.sp, fontWeight = if (selectedCaregiverTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            indicatorColor = TealContainer
                        )
                    )
                    NavigationBarItem(
                        selected = selectedCaregiverTab == 2,
                        onClick = { selectedCaregiverTab = 2 },
                        icon = { Icon(Icons.Default.Timeline, contentDescription = "Aktivitas") },
                        label = { Text("Aktivitas", fontSize = 11.sp, fontWeight = if (selectedCaregiverTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            indicatorColor = TealContainer
                        )
                    )
                    NavigationBarItem(
                        selected = selectedCaregiverTab == 3,
                        onClick = { selectedCaregiverTab = 3 },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifCount > 0) {
                                        Badge(
                                            containerColor = StatusMissed,
                                            contentColor = Color.White
                                        ) {
                                            Text("$unreadNotifCount", fontSize = 10.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.NotificationsNone, contentDescription = "Notifikasi")
                            }
                        },
                        label = { Text("Notifikasi", fontSize = 11.sp, fontWeight = if (selectedCaregiverTab == 3) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            indicatorColor = TealContainer
                        )
                    )
                    NavigationBarItem(
                        selected = selectedCaregiverTab == 4,
                        onClick = { selectedCaregiverTab = 4 },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                        label = { Text("Profil", fontSize = 11.sp, fontWeight = if (selectedCaregiverTab == 4) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            indicatorColor = TealContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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

    if (showServerConfigDialog) {
        ServerConfigDialog(
            currentUrl = serverUrl,
            isOnline = isOnline,
            onSaveUrl = { newUrl -> repository.setServerUrl(newUrl) },
            onDismiss = { showServerConfigDialog = false }
        )
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
                    text = "Pusat Notifikasi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "Pengingat jadwal obat & pembaruan Medication Room",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            if (notifications.any { !it.isRead }) {
                TextButton(onClick = { repository.markAllNotificationsAsRead() }) {
                    Text("Tandai Semua Dibaca", fontSize = 12.sp, color = TealPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (notifications.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(TealContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Belum Ada Notifikasi Baru", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                    Text("Pemberitahuan pengingat dan verifikasi akan muncul di sini.", fontSize = 12.sp, color = TextMuted)
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
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (notif.isRead) SurfaceCard else Color(0xFFF0FDF4)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (notif.isRead) SurfaceBorder else Color(0xFFBBF7D0)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!notif.isRead) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(TealPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        text = notif.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextDark
                                    )
                                }
                                Text(
                                    text = notif.createdAt,
                                    fontSize = 11.sp,
                                    color = TextLight
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notif.body,
                                fontSize = 12.sp,
                                color = TextMuted,
                                lineHeight = 17.sp
                            )

                            if (!notif.isRead) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "Tandai Dibaca",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable { repository.markNotificationAsRead(notif.id) }
                                            .padding(4.dp)
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

// ---------------- PROFILE & SETTINGS SCREEN ----------------

@Composable
fun ProfileScreen(
    repository: MediGuardRepository,
    onLogout: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val allUsers by repository.users.collectAsState()
    val isOnline by repository.isOnline.collectAsState()
    val serverUrl by repository.serverUrl.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showServerConfigDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Profil & Pengaturan",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "Kelola data akun, peran aktif, dan privasi pengawasan",
                fontSize = 12.sp,
                color = TextMuted
            )
        }

        // Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, SurfaceBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(TealContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (currentUser?.name?.firstOrNull() ?: 'U').toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = TealPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = currentUser?.name ?: "-", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextDark)
                                Text(text = currentUser?.email ?: "-", fontSize = 12.sp, color = TextMuted)
                            }
                        }

                        OutlinedButton(
                            onClick = { showEditDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Text("Ubah", fontSize = 12.sp, color = TextDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = SurfaceBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Peran Aktif Saat Ini:", fontSize = 12.sp, color = TextMuted)
                        Surface(
                            color = TealContainer,
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                text = currentUser?.activeRole?.displayName ?: "-",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val newRole = if (currentUser?.activeRole == UserRole.PASIEN) UserRole.PEMANTAU else UserRole.PASIEN
                            repository.switchRole(newRole)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentUser?.activeRole == UserRole.PASIEN) "Beralih ke Tampilan Pemantau" else "Beralih ke Tampilan Pasien",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Real-Time Server Sync Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lan, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KONEKSI SERVER REAL-TIME",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextMuted,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Surface(
                            color = if (isOnline) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                text = if (isOnline) "● Terhubung" else "● Terputus",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) Color(0xFF15803D) else Color(0xFFB91C1C)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Sinkronisasi antar-perangkat (Pasien & Pemantau) berjalan via server:",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = serverUrl,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showServerConfigDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ubah Alamat IP Server", fontSize = 12.sp)
                    }
                }
            }
        }

        // Demo Accounts Switcher
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AKUN DEMO PENGUJIAN:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    allUsers.forEach { u ->
                        val isCurrent = u.id == currentUser?.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            color = if (isCurrent) TealContainer else SurfaceSubtle,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isCurrent) Color(0xFF99F6E4) else SurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${u.name} (${u.activeRole.displayName})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextDark
                                    )
                                    Text(text = u.email, fontSize = 11.sp, color = TextMuted)
                                }
                                if (!isCurrent) {
                                    OutlinedButton(
                                        onClick = { repository.switchUser(u.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Text("Pilih", fontSize = 11.sp, color = TextDark)
                                    }
                                } else {
                                    Text("Sedang Aktif", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FilledTonalButton(
                        onClick = { repository.seedInitialDemoData() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset & Muat Ulang Data Simulasi", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Logout
        item {
            OutlinedButton(
                onClick = {
                    repository.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.2.dp, Color(0xFFFCA5A5)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusMissed)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar dari Akun", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showEditDialog) {
        var editName by remember { mutableStateOf(currentUser?.name ?: "") }
        var editEmail by remember { mutableStateOf(currentUser?.email ?: "") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceCard,
            title = { Text("Ubah Profil Akun", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextDark) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Alamat Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateProfile(editName.trim(), editEmail.trim())
                        showEditDialog = false
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Simpan", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal", fontSize = 13.sp, color = TextMuted)
                }
            }
        )
    }

    if (showServerConfigDialog) {
        ServerConfigDialog(
            currentUrl = serverUrl,
            isOnline = isOnline,
            onSaveUrl = { newUrl -> repository.setServerUrl(newUrl) },
            onDismiss = { showServerConfigDialog = false }
        )
    }
}

// ---------------- DIALOGS FOR ONLINE SYNC & PHOTO PREVIEW ----------------

@Composable
fun ServerConfigDialog(
    currentUrl: String,
    isOnline: Boolean,
    onSaveUrl: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var urlInput by remember { mutableStateOf(currentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = SurfaceCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = if (isOnline) Color(0xFF16A34A) else Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Koneksi Server Online", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                    Text(
                        text = if (isOnline) "Status: Terhubung (Online)" else "Status: Terputus (Offline)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOnline) Color(0xFF15803D) else Color(0xFFB91C1C)
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    text = "Hubungkan perangkat fisik lain atau emulator ke server backend yang sama untuk menguji sinkronisasi real-time ruangan obat dan notifikasi.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("URL Server MediGuard") },
                    leadingIcon = { Icon(Icons.Default.Lan, contentDescription = null, tint = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = SurfaceBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Pilihan Cepat (Preset):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { urlInput = "http://192.168.1.28:8080" },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Wi-Fi LAN", fontSize = 10.sp)
                    }
                    OutlinedButton(
                        onClick = { urlInput = "http://10.0.2.2:8080" },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Emulator", fontSize = 10.sp)
                    }
                    OutlinedButton(
                        onClick = { urlInput = "http://localhost:8080" },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Localhost", fontSize = 10.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveUrl(urlInput.trim())
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Simpan & Hubungkan", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", fontSize = 13.sp, color = TextMuted)
            }
        }
    )
}

@Composable
fun AsyncPhotoPreviewDialog(
    photoUri: String?,
    medicationName: String,
    dosage: String,
    takenAt: String?,
    repository: MediGuardRepository,
    onDismiss: () -> Unit
) {
    var bitmap by remember(photoUri) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(photoUri) { mutableStateOf(true) }

    LaunchedEffect(photoUri) {
        if (!photoUri.isNullOrBlank()) {
            isLoading = true
            bitmap = repository.loadBitmap(photoUri)
            isLoading = false
        } else {
            isLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = SurfaceCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(TealContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Bukti Foto Pengobatan", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextDark)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        when {
                            isLoading -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = TealPrimary, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Mengunduh foto bukti dari server...", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                            bitmap != null -> {
                                Image(
                                    bitmap = bitmap!!.asImageBitmap(),
                                    contentDescription = "Bukti Foto",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(44.dp),
                                        tint = TealPrimary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = photoUri ?: "Belum ada berkas foto",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextDark,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Bukti foto terlampir dari laporan mandiri pasien.",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = SurfaceSubtle,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "$medicationName ($dosage)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextDark
                        )
                        Text(
                            text = "Waktu minum: ${takenAt ?: "Sesuai Jadwal"}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Tutup", fontSize = 13.sp)
            }
        }
    )
}
