# BLE Radar

Aplikasi Android untuk memindai perangkat Bluetooth Low Energy (BLE) di sekitar secara real-time, melihat kekuatan sinyal, dan memantau kedekatan perangkat melalui tampilan radar.

## Fitur

- Memulai dan menghentikan pemindaian BLE secara manual.
- Menampilkan nama perangkat jika tersedia, pengenal perangkat, RSSI mentah, dan kategori perkiraan jarak.
- Mengurutkan perangkat berdasarkan RSSI terkuat.
- Mencari perangkat berdasarkan nama atau pengenal, serta memfilter berdasarkan RSSI minimum.
- Melacak satu perangkat melalui layar radar yang menampilkan zona sinyal, RSSI, kestabilan sinyal, dan grafik perubahan RSSI.
- Menyimpan riwayat perangkat yang terdeteksi ke database lokal.
- Menangani Bluetooth nonaktif dan izin pemindaian yang belum diberikan.
- Menghentikan pemindaian saat aplikasi masuk ke latar belakang dan melanjutkannya saat aplikasi kembali ke depan jika pemindaian masih diminta.

## Persyaratan

- Android Studio dengan Android SDK.
- JDK yang kompatibel dengan Gradle proyek.
- Perangkat Android 7.0 (API 24) atau lebih baru dengan dukungan BLE. Pemindaian BLE memerlukan Bluetooth aktif dan izin yang diminta aplikasi.

## Menjalankan aplikasi

1. Buka proyek ini di Android Studio dan tunggu Gradle menyelesaikan sinkronisasi.
2. Hubungkan perangkat Android yang mendukung BLE atau jalankan emulator yang memiliki dukungan Bluetooth.
3. Pilih konfigurasi aplikasi Android, lalu jalankan aplikasi.
4. Berikan izin Bluetooth saat diminta. Tekan **Start** untuk memindai, lalu pilih perangkat untuk membuka layar radar.

Untuk membangun APK debug dari terminal:

```shell
./gradlew :androidApp:assembleDebug
```

Di Windows gunakan:

```powershell
.\gradlew.bat :androidApp:assembleDebug
```

## Arsitektur dan teknologi

Proyek menggunakan Kotlin dengan struktur Kotlin Multiplatform, sementara aplikasi yang dikonfigurasi saat ini adalah Android.

- **Arsitektur:** pemisahan `presentation`, `domain`, dan `data`; ViewModel mengelola state UI dan alur pemindaian.
- **UI:** Compose Multiplatform dan Material 3.
- **BLE:** Kable untuk menerima advertising BLE dan nilai RSSI.
- **Penyimpanan lokal:** Room dengan SQLite untuk riwayat perangkat.
- **Dependency injection:** Koin untuk menyediakan repository, use case, dan ViewModel.
- **Asinkron:** Kotlin Coroutines dan Flow untuk aliran hasil pemindaian.

## Pemetaan zona sinyal

Zona jarak mengikuti pemetaan yang ditetapkan dalam studi kasus dan ditentukan dari RSSI mentah terbaru:

| RSSI | Kategori | Perkiraan jarak |
|---|---|---|
| −10 s/d −30 dBm | Sangat kuat | < 1 m |
| −30 s/d −50 dBm | Kuat | 1–3 m |
| −50 s/d −70 dBm | Cukup / baik | 3–10 m |
| −70 s/d −80 dBm | Lemah | 10–20 m |
| −80 s/d −90 dBm | Sangat lemah | > 20 m (batas jangkauan) |
| < −90 dBm | Sinyal hilang | Terputus / di luar jangkauan |

## Keterbatasan dan asumsi

- Jarak BLE adalah **perkiraan berbasis RSSI**, bukan pengukuran jarak langsung. RSSI dapat berubah karena model perangkat, orientasi antena, tubuh atau benda yang menghalangi, dinding, pantulan sinyal, dan lingkungan radio. Karena itu, perangkat yang berdekatan masih dapat masuk ke kategori **1–3 m**; angka tersebut adalah rentang kategori studi kasus, bukan jaminan jarak fisik.
- Nama perangkat bisa tidak tersedia jika perangkat tidak menyertakannya pada advertising BLE. Pengenal yang ditampilkan berasal dari library BLE dan formatnya bergantung pada platform/perangkat; aplikasi tidak menjamin selalu berupa MAC address.
- Pemindaian dihentikan saat aplikasi masuk ke latar belakang. Pemindaian berlanjut kembali saat aplikasi aktif jika pengguna sebelumnya belum menekan **Stop**.
- Aplikasi ini berfokus pada perangkat Android dan memerlukan perangkat dengan Bluetooth LE.
