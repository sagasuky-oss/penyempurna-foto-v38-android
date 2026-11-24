# Penyempurna Foto V38 – Android APK Wrapper

Proyek Android yang membungkus V38 Optimasi HP dalam WebView. HTML dikemas sebagai aset lokal; aplikasi tidak perlu menjalankan server Termux. Fitur yang mengambil model AI eksternal (misalnya deteksi wajah) tetap memerlukan internet.

## Cara paling praktis membuat APK dari HP

Build Android penuh langsung di Termux membutuhkan Android SDK, Build Tools, dan konfigurasi Gradle yang cukup besar/rumit. Paket ini disertai GitHub Actions agar proses kompilasi dapat dilakukan dari browser HP tanpa PC:

1. Ekstrak ZIP ini.
2. Buat repository GitHub baru, misalnya `penyempurna-foto-v38-android`.
3. Upload seluruh isi folder `PenyempurnaFotoAPK` ke repository (pastikan `app/`, `build.gradle`, `settings.gradle`, `gradle.properties`, dan `.github/` berada di root repository; jangan membuat folder bertingkat tambahan).
4. Buka tab **Actions** di repository dan pilih workflow **Build Penyempurna Foto APK**.
5. Tekan **Run workflow**. Tunggu sampai proses selesai dengan status hijau.
6. Buka hasil run, cari bagian **Artifacts**, lalu unduh `Penyempurna-Foto-V38-debug-APK`.
7. Ekstrak ZIP artifact dan pasang `app-debug.apk` di Android. Jika diminta, izinkan pemasangan aplikasi dari sumber tersebut.

Setiap build yang berhasil menghasilkan APK debug untuk pengujian. APK debug bukan rilis Play Store dan belum ditandatangani sebagai rilis produksi.

## Build melalui Android Studio / AndroidIDE

Buka folder proyek sebagai proyek Gradle. Gunakan JDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3, Android SDK Platform 35, dan Build Tools yang sesuai. Jalankan tugas `:app:assembleDebug`.

## Fitur wrapper

- JavaScript dan DOM storage aktif.
- HTML aplikasi dimuat dari `assets/index.html`; server Termux tidak diperlukan.
- Pemilih file menggunakan Android document picker.
- Unduhan Blob diarahkan ke folder Download Android.
- Tautan HTTPS eksternal dibuka di browser.

## Pemeriksaan setelah instalasi

Source project belum sama dengan APK yang sudah diuji. Setelah APK dibuat dan dipasang, uji: buka aplikasi, pilih foto, semua kontrol Prompt Builder, proses pratinjau, ekspor PNG/JPEG, simpan/buka proyek JSON, unduhan, serta deteksi wajah ketika internet aktif. Laporkan pesan error jika ada.
