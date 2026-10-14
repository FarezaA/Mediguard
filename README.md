# MediGuard — Medication Adherence Monitoring Application

Aplikasi Android native menggunakan **Kotlin** dan **Jetpack Compose** (Material 3) yang berfokus pada **keandalan fungsi** (fungsi inti pemantauan, otorisasi, Medication Room, pencatatan konsumsi, dan verifikasi langsung) dengan antarmuka pengguna yang bersih, jelas, dan sederhana.

---

## 1. Fitur Utama yang Telah Diimplementasikan

Sesuai instruksi dan spesifikasi dokumen kebutuhan:

### 1.1 Autentikasi, Akun, dan Manajemen Peran
- **Registrasi Akun:** Validasi email, nama, kata sandi, pemilihan peran awal, dan persetujuan ketentuan layanan serta privasi.
- **Login & Logout:** Autentikasi aman berbasis peran (*Pasien* dan *Pemantau/Caregiver*).
- **Lupa Kata Sandi:** Alur pemulihan kata sandi tanpa membocorkan eksistensi akun.
- **Peralihan Peran Fleksibel (Role Switcher):** Pengguna dapat beralih peran antara Pasien dan Pemantau kapan saja.
- **Peralihan Akun Demo Cepat:** Tersedia tombol langsung untuk menguji skenario sebagai *Bapak Hendra (Pasien)*, *Siti Rahma (Pemantau Disetujui)*, atau *Budi Santoso (Pemantau Menunggu)*.

### 1.2 Konsep Medication Room
- **Pembuatan Ruang:** Pasien membuat ruang pemantauan digital (misal: *Pemantauan Pengobatan Rutin Ayah*).
- **Kode Undangan Unik:** Menghasilkan kode undangan (misal: `MG-8821`) dan fitur *Rotate/Ganti Kode*.
- **Alur Permintaan Akses Transparan:** Kode undangan hanya menampilkan pratinjau aman ruang (nama ruang, pemilik). Data kesehatan obat/jadwal **tidak dapat diakses** sebelum disetujui pasien.
- **Persetujuan & Penolakan Pasien:** Pasien dapat menyetujui (*Approve*) atau menolak (*Reject*) permintaan masuk.
- **Pencabutan Akses (*Revoke*):** Pasien dapat mencabut izin akses pemantau kapan saja.
- **Kontrol Izin Privasi Granular:** Pasien dapat mengatur izin apakah pemantau boleh melihat Jadwal, Aktivitas, atau Statistik Kepatuhan.

### 1.3 Pengelolaan Obat & Jadwal
- **Pendataan Obat:** Nama obat, bentuk sediaan (Tablet, Kapsul, Sirup, dll.), dosis, petunjuk konsumsi (misal: *Sesudah makan*), catatan khusus, dan status aktif/arsip.
- **Pengaturan Jadwal:** Frekuensi (1x, 2x, 3x sehari) beserta jam konsumsi spesifik.
- **Pengarsipan Obat:** Pasien dapat mengarsipkan obat yang sudah selesai dikonsumsi.

### 1.4 Pencatatan Konsumsi & Status Transparan
Status dosis dirancang transparan membedakan laporan mandiri dan verifikasi:
- `Terjadwal`: Waktu konsumsi belum tiba.
- `Menunggu laporan`: Waktu konsumsi telah tiba / jatuh tempo.
- `Dilaporkan diminum`: Pasien menyatakan telah mengonsumsi obat (laporan mandiri).
- `Terverifikasi langsung`: Pemantau memverifikasi bahwa telah menyaksikan konsumsi secara langsung.
- `Terlewat`: Pasien mencatat dosis tidak diminum beserta alasan opsional.
- `Tunda Pengingat`: Menunda pengingat jadwal (15m, 30m, 1 jam).

### 1.5 Verifikasi Bukti Foto & Pengamatan Langsung (*Human-Verified*)
- **Wajib Melampirkan Foto Bukti Konsumsi**: Saat pasien mengonfirmasi *"Sudah Minum"*, pasien wajib menyertakan foto bukti konsumsi obat.
- **Daftar Verifikasi & Pemeriksaan Foto di Sisi Pemantau**:
  - Pada dashboard pemantau, terdapat daftar verifikasi tertunda.
  - Sebelum menyetujui, pemantau dapat melihat dan memeriksa foto bukti yang dikirimkan pasien via tombol *"Periksa Foto"*.
  - Di dalam modal verifikasi, foto bukti ditampilkan secara jelas dan pemantau mencentang: *"Saya telah memeriksa foto bukti dan menyatakan telah menyaksikan langsung pasien meminum obat ini"*.

### 1.6 Pengingat Pasien Telat & Tombol Pengingat Berulang Pemantau
- **Notifikasi Default Sistem**: Saat jadwal jatuh tempo atau terlambat, sistem otomatis menerbitkan 1 notifikasi pengingat awal ke pasien.
- **Tombol Pengingat Berulang dari Pemantau**:
  - Pada dashboard pemantau, terdapat bagian *"Jadwal Telat / Belum Dilaporkan"*.
  - Pemantau memiliki tombol khusus: *"Kirim Notifikasi Pengingat ke Pasien"*.
  - Setiap kali tombol ditekan, notifikasi pengingat baru langsung dikirimkan ke pasien sebanyak pemantau menekannya (disertai penomoran pengingat ke-1, ke-2, ke-3, dst.).
  - Pasien akan melihat tanda peringatan jumlah pengingat yang telah diterima dari pemantau.

### 1.7 Riwayat, Audit Log, dan Statistik Kepatuhan
- **Linimasa Aktivitas:** Mencatat jejak audit setiap aksi (buat ruang, tambah obat, laporan konsumsi, verifikasi, pengingat dikirim, perubahan anggota/izin).
- **Statistik Kepatuhan:**
  - Total jadwal jatuh tempo.
  - Jumlah dosis dilaporkan mandiri.
  - Jumlah dosis terverifikasi langsung.
  - Jumlah dosis terlewat.
  - Persentase kepatuhan mandiri vs persentase terverifikasi langsung.

### 1.8 Pusat Notifikasi Aplikasi
- Notifikasi pengingat minum obat (sistem & pemantau).
- Notifikasi permohonan bergabung baru ke pemilik ruang.
- Notifikasi penerimaan/penolakan ke pemantau.
- Notifikasi laporan minum obat baru ke pemantau untuk diverifikasi beserta foto bukti.
- Notifikasi verifikasi berhasil ke pasien.

---

## 2. Struktur Proyek

```text
Mediguard/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/mediguard/app/
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   └── Models.kt          # Domain models & enums
│   │   │   │   │   └── repository/
│   │   │   │   │       └── MediGuardRepository.kt # Business rules, authorization, state
│   │   │   │   ├── ui/
│   │   │   │   │   ├── theme/                 # Theme, colors (Material 3)
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   ├── NavRoutes.kt       # Navigation routes
│   │   │   │   │   │   └── AppNavHost.kt      # Compose NavHost
│   │   │   │   │   └── screens/
│   │   │   │   │       ├── auth/              # Splash, Onboarding, Login, Register, Forgot Password
│   │   │   │   │       ├── patient/           # Dashboard, Medications, Rooms, History & Stats
│   │   │   │   │       ├── caregiver/         # Dashboard, JoinRoom, Rooms, Activity & Stats
│   │   │   │   │       └── common/            # MainShell, Notifications, Profile & Switcher
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/                           # Values, strings, drawables
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   │       └── java/com/mediguard/app/
│   │           └── MediGuardRepositoryTest.kt # Automated unit tests
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew.bat
```

---

## 3. Hasil Build & Verifikasi

Proyek telah diuji dan diverifikasi menggunakan Gradle:
1. **Kompilasi Kotlin (`compileDebugKotlin`):** `BUILD SUCCESSFUL` (0 error)
2. **Pengujian Unit (`testDebugUnitTest`):** `BUILD SUCCESSFUL` (Seluruh skenario pengujian aturan bisnis dan otorisasi berhasil)
3. **Packaging APK (`assembleDebug`):** `BUILD SUCCESSFUL`
   - Berkas APK: `app/build/outputs/apk/debug/app-debug.apk` (18.9 MB)
