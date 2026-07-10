# 🚀 Harga Emas Hari Ini

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android-brightgreen.svg)
![Architecture](https://img.shields.io/badge/architecture-Clean%20Architecture-orange.svg)

Aplikasi Android modern yang menampilkan harga emas harian dari berbagai vendor di Indonesia (dimulai dari Galeri24). Aplikasi ini dirancang agar ringan, tanpa backend server konvensional, dan menggunakan GitHub Actions serta GitHub Repository sebagai sumber data (*Backend-less Data Strategy*).

## 📊 Arsitektur Sistem

Data Harga Emas ditarik otomatis dari situs vendor secara berkala menggunakan Script Python (Scraper) dan dieksekusi oleh **GitHub Actions**. Hasil data tersebut disimpan di dalam repository ini dalam format JSON, yang kemudian dikonsumsi secara asinkron oleh Aplikasi Android.

```text
[Website Vendor] -> (GitHub Actions: Scraper) -> [JSON di GitHub Repo] -> (Retrofit) -> [Aplikasi Android]
```

## 🛠 Tech Stack

- **Platform:** Android (Min SDK 24, Target SDK 36)
- **Bahasa Pemrograman:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Architecture:** MVVM + Clean Architecture (Data, Domain, Presentation)
- **Dependency Injection:** Manual DI (AppContainer) *Siap untuk bermigrasi ke Hilt/Koin*
- **Networking:** Retrofit + OkHttp + Gson
- **Asynchronous:** Coroutines + StateFlow
- **Image Loading:** Coil
- **Local Storage:** DataStore
- **Ads:** Google AdMob
- **Automation:** GitHub Actions & Python Scraper

## 📂 Struktur Proyek

```text
harga-emas/
├── android/                 # Source code Aplikasi Android
├── scraper/                 # Script Python untuk scraping data emas
├── json/                    # Database JSON statis yang dihasilkan oleh scraper
│   ├── prices.json          # Data harga emas harian
│   ├── history.json         # Riwayat harga emas (untuk grafik)
│   ├── articles.json        # Artikel investasi
│   └── config.json          # Konfigurasi remote (termasuk Test ID AdMob)
└── .github/workflows/       # Konfigurasi CI/CD (Scraper Cron Job)
```

## 🚀 Persiapan & Instalasi

### Android App
1. Buka folder `android/` di **Android Studio**.
2. Lakukan *Sync Project with Gradle Files*.
3. Build dan jalankan aplikasi (`./gradlew assembleDebug` atau *Run app*).

### Scraper Automation
Scraper dikonfigurasi berjalan secara otomatis pada pukul 08:00, 11:00, 14:00, dan 17:00 WIB.
Jika ingin menjalankan scraper secara manual:
```bash
pip install -r scraper/requirements.txt
python scraper/scraper.py
```

## 🔖 Version History

- **v1.0.0** (Initial Release)
  - Setup Repository Blueprint
  - Pembuatan Scraper Dasar untuk Galeri24
  - Pengaturan GitHub Actions CI/CD Scheduler
  - Kerangka Android App (Jetpack Compose, Clean Architecture, Retrofit, DataStore)
  - Integrasi UI Awal: Dashboard Harga Emas

---
*Dikembangkan oleh [Amri Marihot Jati](https://github.com/amrimarihotjati).*
