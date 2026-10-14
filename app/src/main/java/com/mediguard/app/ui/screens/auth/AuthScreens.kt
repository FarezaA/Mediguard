package com.mediguard.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediguard.app.data.model.UserRole
import com.mediguard.app.data.repository.MediGuardRepository
import com.mediguard.app.ui.navigation.NavRoute

@Composable
fun SplashScreen(
    repository: MediGuardRepository,
    onNavigate: (String) -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MediGuard",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pendamping Pengawasan Kepatuhan Obat",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (currentUser != null) {
            Text(text = "Masuk sebagai: ${currentUser?.name} (${currentUser?.activeRole?.displayName})")
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onNavigate(NavRoute.Main.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lanjutkan ke Dashboard")
            }
        } else {
            Button(
                onClick = { onNavigate(NavRoute.Onboarding.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mulai")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onNavigate(NavRoute.Login.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sudah Punya Akun? Masuk")
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
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Selamat Datang di MediGuard",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Aplikasi pemantauan kepatuhan konsumsi obat berbasis Medication Room.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. Medication Room", fontWeight = FontWeight.Bold)
                    Text(
                        "Pasien membuat ruang pemantauan dan membagikan kode undangan kepada keluarga atau orang tepercaya.",
                        fontSize = 13.sp
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("2. Pencatatan Transparan", fontWeight = FontWeight.Bold)
                    Text(
                        "Pasien mencatat laporan konsumsi secara mandiri. Laporan mandiri tidak otomatis dianggap terverifikasi.",
                        fontSize = 13.sp
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("3. Verifikasi Pengamatan Langsung", fontWeight = FontWeight.Bold)
                    Text(
                        "Pemantau hanya memverifikasi apabila menyaksikan langsung pasien mengonsumsi obat. Privasi dan akses sepenuhnya dikontrol oleh pasien.",
                        fontSize = 13.sp
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(top = 24.dp)) {
            Button(
                onClick = { onNavigate(NavRoute.Login.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Masuk ke Akun")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onNavigate(NavRoute.Register.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Daftar Akun Baru")
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
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Masuk ke MediGuard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Silakan masukkan email dan kata sandi Anda",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

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

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Kata Sandi") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { onNavigate(NavRoute.ForgotPassword.route) }) {
                Text("Lupa Kata Sandi?")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val result = repository.login(email, password)
                if (result.isSuccess) {
                    onNavigate(NavRoute.Main.route)
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Gagal masuk"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Masuk")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { onNavigate(NavRoute.Register.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Belum Punya Akun? Daftar")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Akun Demo Cepat:",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    repository.switchUser("user_patient_1")
                    onNavigate(NavRoute.Main.route)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors()
            ) {
                Text("Pasien (Hendra)", fontSize = 12.sp)
            }
            Button(
                onClick = {
                    repository.switchUser("user_caregiver_1")
                    onNavigate(NavRoute.Main.route)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors()
            ) {
                Text("Pemantau (Siti)", fontSize = 12.sp)
            }
        }
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
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Daftar Akun Baru",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Lengkapi data untuk menggunakan MediGuard",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; errorMessage = null },
            label = { Text("Nama Lengkap") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Kata Sandi (min 6 karakter)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; errorMessage = null },
            label = { Text("Konfirmasi Kata Sandi") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(text = "Pilih Peran Utama:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedRole == UserRole.PASIEN,
                onClick = { selectedRole = UserRole.PASIEN },
                label = { Text("Pasien (Pengelola Obat)") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedRole == UserRole.PEMANTAU,
                onClick = { selectedRole = UserRole.PEMANTAU },
                label = { Text("Pemantau (Keluarga)") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = agreedToTerms,
                onCheckedChange = { agreedToTerms = it }
            )
            Text(
                text = "Saya menyetujui ketentuan layanan dan kebijakan privasi.",
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (password != confirmPassword) {
                    errorMessage = "Konfirmasi kata sandi tidak cocok."
                    return@Button
                }
                if (!agreedToTerms) {
                    errorMessage = "Harap setujui ketentuan layanan dan privasi."
                    return@Button
                }
                val result = repository.register(name, email, password, selectedRole)
                if (result.isSuccess) {
                    onNavigate(NavRoute.Main.route)
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Gagal mendaftar"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Buat Akun")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { onNavigate(NavRoute.Login.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sudah Punya Akun? Masuk")
        }
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
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Pemulihan Kata Sandi",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Masukkan alamat email Anda untuk menerima tautan pemulihan kata sandi.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

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
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Terdaftar") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotBlank()) {
                    infoMessage = repository.resetPassword(email.trim())
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kirim Tautan Pemulihan")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { onNavigate(NavRoute.Login.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kembali ke Login")
        }
    }
}
