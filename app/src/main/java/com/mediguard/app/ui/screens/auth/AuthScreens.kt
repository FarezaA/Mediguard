package com.mediguard.app.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediguard.app.data.model.UserRole
import com.mediguard.app.data.repository.MediGuardRepository
import com.mediguard.app.ui.navigation.NavRoute
import com.mediguard.app.ui.theme.*

@Composable
fun SplashScreen(
    repository: MediGuardRepository,
    onNavigate: (String) -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo Shield Badge
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(TealContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = "MediGuard Logo",
                    tint = TealPrimary,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "MediGuard",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = TealContainer,
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    text = "Pengawasan Kepatuhan Obat Terpercaya",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TealPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Dirancang untuk memastikan kepatuhan pengobatan pasien secara jujur, transparan, dan saling peduli bersama orang terdekat.",
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = TextMuted,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (currentUser != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sesi Aktif Ditemukan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TealPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentUser?.name ?: "",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Peran: ${currentUser?.activeRole?.displayName}",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onNavigate(NavRoute.Main.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Lanjutkan ke Dashboard", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            } else {
                Button(
                    onClick = { onNavigate(NavRoute.Onboarding.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Mulai Sekarang", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { onNavigate(NavRoute.Login.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, TealPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary)
                ) {
                    Text("Sudah Punya Akun? Masuk", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Human Trust & Privacy Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = StatusVerified,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Aman • Privasi Pasien Terlindungi • Bebas Asumsi",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun OnboardingScreen(
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = TealContainer,
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    text = "Prinsip Utama MediGuard",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Pendamping Minum Obat Tanpa Prasangka",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tiga pilar utama pengawasan yang melindungi integritas pengobatan dan rasa hormat pada pasien.",
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Pillar 1: Medication Room
            OnboardingPillarCard(
                icon = Icons.Default.MeetingRoom,
                iconBg = Color(0xFFCCFBF1),
                iconTint = Color(0xFF0F766E),
                title = "1. Medication Room Mandiri",
                description = "Pasien yang memegang kendali. Pasien membuat ruang pemantauan dan membagikan kode undangan secara selektif hanya kepada keluarga tepercaya."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pillar 2: Transparent Logging
            OnboardingPillarCard(
                icon = Icons.Default.FactCheck,
                iconBg = Color(0xFFE0F2FE),
                iconTint = Color(0xFF0284C7),
                title = "2. Pencatatan Transparan & Terbuka",
                description = "Pasien mencatat laporan mandiri disertai bukti waktu & foto. Laporan mandiri tidak otomatis dianggap terverifikasi untuk menjaga keabsahan data medis."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pillar 3: Direct Observation Verification
            OnboardingPillarCard(
                icon = Icons.Default.VerifiedUser,
                iconBg = Color(0xFFDCFCE7),
                iconTint = Color(0xFF166534),
                title = "3. Verifikasi Pengamatan Nyata",
                description = "Pemantau hanya memverifikasi jika benar-benar menyaksikan langsung pasien meminum obat. Menghindari asumsi dan saling menjaga kejujuran."
            )
        }

        Column(modifier = Modifier.padding(top = 28.dp, bottom = 12.dp)) {
            Button(
                onClick = { onNavigate(NavRoute.Login.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Masuk ke Akun", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { onNavigate(NavRoute.Register.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, TealPrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary)
            ) {
                Text("Daftar Akun Baru", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun OnboardingPillarCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    repository: MediGuardRepository,
    onNavigate: (String) -> Unit
) {
    var email by remember { mutableStateOf("hendra@mediguard.com") }
    var password by remember { mutableStateOf("password123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(TealContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.HealthAndSafety,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Selamat Datang 👋",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Masuk untuk memantau jadwal obat Anda dan keluarga",
            fontSize = 13.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = StatusMissedBg),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = StatusMissedText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = StatusMissedText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("Alamat Email") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted)
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = SurfaceBorder
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Kata Sandi") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted)
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Sembunyikan sandi" else "Tampilkan sandi",
                        tint = TextMuted
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = SurfaceBorder
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { onNavigate(NavRoute.ForgotPassword.route) }) {
                Text(
                    text = "Lupa Kata Sandi?",
                    fontSize = 12.sp,
                    color = TealPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                val result = repository.login(email.trim(), password)
                if (result.isSuccess) {
                    onNavigate(NavRoute.Main.route)
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Gagal masuk. Periksa email & sandi."
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
            Text("Masuk", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { onNavigate(NavRoute.Register.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.2.dp, SurfaceBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark)
        ) {
            Text("Belum Punya Akun? Daftar Sekarang", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Demo Accounts Quick Switcher
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, SurfaceBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AKSES DEMO CEPAT (1-KLIK):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Demo Patient
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                repository.switchUser("user_patient_1")
                                onNavigate(NavRoute.Main.route)
                            },
                        color = Color(0xFFE6F4F1),
                        border = BorderStroke(1.dp, Color(0xFF99F6E4)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("👤 Pasien", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TealPrimary)
                            Text("Hendra (Pengelola)", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    // Demo Caregiver
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                repository.switchUser("user_caregiver_1")
                                onNavigate(NavRoute.Main.route)
                            },
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🛡️ Pemantau", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentCyan)
                            Text("Siti (Keluarga)", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RegisterScreen(
    repository: MediGuardRepository,
    onNavigate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.PASIEN) }
    var agreedToTerms by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Daftar Akun MediGuard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "Lengkapi data untuk bergabung ke sistem pemantauan",
            fontSize = 13.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = StatusMissedBg),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusMissedText, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = errorMessage ?: "", color = StatusMissedText, fontSize = 13.sp)
                }
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; errorMessage = null },
            label = { Text("Nama Lengkap") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = SurfaceBorder)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("Alamat Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = SurfaceBorder)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Kata Sandi (min. 6 karakter)") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = SurfaceBorder)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; errorMessage = null },
            label = { Text("Konfirmasi Kata Sandi") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = SurfaceBorder)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Pilih Peran Akun Utama:",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Patient option card
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { selectedRole = UserRole.PASIEN },
                color = if (selectedRole == UserRole.PASIEN) TealContainer else SurfaceCard,
                border = BorderStroke(
                    if (selectedRole == UserRole.PASIEN) 2.dp else 1.dp,
                    if (selectedRole == UserRole.PASIEN) TealPrimary else SurfaceBorder
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedRole == UserRole.PASIEN,
                            onClick = { selectedRole = UserRole.PASIEN },
                            colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                        )
                        Text("Pasien", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text(
                        text = "Kelola obat sendiri & undang pemantau tepercaya.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }

            // Caregiver option card
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { selectedRole = UserRole.PEMANTAU },
                color = if (selectedRole == UserRole.PEMANTAU) AccentCyanContainer else SurfaceCard,
                border = BorderStroke(
                    if (selectedRole == UserRole.PEMANTAU) 2.dp else 1.dp,
                    if (selectedRole == UserRole.PEMANTAU) AccentCyan else SurfaceBorder
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedRole == UserRole.PEMANTAU,
                            onClick = { selectedRole = UserRole.PEMANTAU },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentCyan)
                        )
                        Text("Pemantau", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text(
                        text = "Mendampingi & memverifikasi minum obat langsung.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = agreedToTerms,
                onCheckedChange = { agreedToTerms = it },
                colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Saya menyetujui ketentuan privasi Medication Room & etika verifikasi langsung.",
                fontSize = 11.sp,
                color = TextDark,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                    errorMessage = "Harap lengkapi semua kolom pendaftaran."
                    return@Button
                }
                if (password.length < 6) {
                    errorMessage = "Kata sandi minimal 6 karakter."
                    return@Button
                }
                if (password != confirmPassword) {
                    errorMessage = "Konfirmasi kata sandi tidak cocok."
                    return@Button
                }
                if (!agreedToTerms) {
                    errorMessage = "Harap setujui ketentuan layanan dan privasi."
                    return@Button
                }
                val result = repository.register(name.trim(), email.trim(), password, selectedRole)
                if (result.isSuccess) {
                    onNavigate(NavRoute.Main.route)
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Gagal mendaftar."
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
            Text("Buat Akun", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = { onNavigate(NavRoute.Login.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Sudah Punya Akun? Masuk",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TealPrimary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ForgotPasswordScreen(
    repository: MediGuardRepository,
    onNavigate: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(TealContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Key, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(28.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Pemulihan Kata Sandi",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Masukkan alamat email Anda untuk menerima instruksi pemulihan kata sandi akun MediGuard.",
            fontSize = 13.sp,
            color = TextMuted,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (infoMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = StatusVerifiedBg),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusVerifiedText, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = infoMessage ?: "",
                        fontSize = 13.sp,
                        color = StatusVerifiedText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Terdaftar") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = SurfaceBorder)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                if (email.isNotBlank()) {
                    infoMessage = repository.resetPassword(email.trim())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
            Text("Kirim Tautan Pemulihan", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { onNavigate(NavRoute.Login.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.2.dp, SurfaceBorder)
        ) {
            Text("Kembali ke Login", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
        }
    }
}
