# My Movie Favorite

## Ikhtisar Proyek
My Movie Favorite adalah aplikasi Android studi kasus yang menampilkan katalog film ala Netflix dengan data yang tersimpan di Firebase Realtime Database. Aplikasi dibangun menggunakan Android Studio (AGP 8.13.0) dan bahasa pemrograman Java. Fokus proyek ini adalah memperlihatkan struktur arsitektur Android modern (fragment, adapter, form input, Firebase, dan komponen Material Design) untuk keperluan asesmen.

## Konsep Android yang Digunakan
- **Activity & Fragment**
  - `MainActivity` bertugas sebagai host seluruh tab utama. Untuk setiap menu, aplikasi menampilkan fragment yang berbeda: `MovieListFragment`, `SearchFragment`, `WatchlistFragment`, dan `AboutFragment`. Fragment dipilih via `BottomNavigationView`, sehingga pengalaman navigasi terasa native dan modular.
  - `MovieDetailActivity` berjalan terpisah untuk menampilkan detail film dan mendukung edit data.
  - `MovieFormActivity` digunakan untuk tambah/edit film melalui form input.
  - `LoginActivity` dan `RegisterActivity` menangani autentikasi sederhana.

- **RecyclerView & Adapter**
  - Daftar film pada tab Browse/Search/Watchlist dirender memakai `RecyclerView` dengan `MovieAdapter`. Adapter ini memanfaatkan pola ViewHolder untuk menjaga performa scrolling serta menerapkan binding state (judul, genre, rating, overview).
  - Setiap kartu menggunakan `MaterialCardView` dan `Chip` untuk rating, mengikuti guideline Material Design.

- **Sumber Data Firebase**
  - Data film disimpan di Firebase Realtime Database pada path `movies`.
  - `MovieDataSource` memuat data dari Firebase, menyimpan cache lokal, dan melakukan CRUD. Jika database kosong, data awal otomatis disalin dari `app/src/main/assets/movies.json`.
  - Status watchlist disimpan dan diperbarui di Firebase sehingga konsisten lintas layar.

- **Autentikasi Sederhana**
  - Data user disimpan di Firebase Realtime Database pada path `users`.
  - Login/registrasi memvalidasi email dan password secara sederhana, lalu menyimpan sesi di `SharedPreferences` untuk menampilkan greeting dan menjaga sesi login.

- **Image Loading**
  - Library **Glide** digunakan untuk memuat URL poster pada daftar dan detail film. Glide menangani caching, placeholder, dan fallback agar UI tetap stabil ketika jaringan lambat.

- **Material Components**
  - Aplikasi mengadopsi warna dominan `netflix_black` dan `netflix_red`. Komponen seperti `MaterialToolbar`, `MaterialButton`, `TextInputLayout`, dan `MaterialDivider` digunakan untuk mempertahankan estetika Netflix.
  - Tema diatur melalui `Theme.MaterialComponents.DayNight.NoActionBar` dengan penyesuaian warna di `values/themes.xml`.

## Fitur Utama
1. **Browse (Browse/Showing Today)**
   - Menampilkan daftar film dari Firebase dengan layout kartu. Header “Showing Today” ditampilkan di atas daftar.
   - Greeting pengguna login tampil di atas daftar.
   - Tombol tambah film (FAB) membuka form untuk menambahkan film baru.
   - Swipe ke kiri pada item untuk menghapus film.
   - Klik kartu film membuka halaman detail.

2. **Search**
   - Input kata kunci melalui `TextInputEditText`. Daftar hasil diperbarui secara real-time saat pengguna mengetik (filter berdasarkan judul, overview, atau genre).
   - Menyediakan empty state ketika tidak ada hasil cocok.

3. **Watchlist**
   - Menampilkan film yang ditandai sebagai watchlist. Data bersumber dari flag `watchlisted` pada model `Movie`. Tampilan empty state membantu memberi konteks ketika watchlist kosong.

4. **Detail Film**
   - Halaman detail menampilkan poster resolusi tinggi, metadata (tahun, rating, lama durasi), genre, dan sinopsis.
   - Tombol watchlist mendukung _toggle_: pengguna dapat menambahkan atau menghapus film dari watchlist langsung dari halaman detail. Feedback diberikan melalui `Snackbar`.
   - Tombol edit membuka form untuk memperbarui data film.
   - Disediakan tombol “Back” untuk kembali ke tab sebelumnya.

5. **About**
   - Menampilkan identitas aplikasi/pengembang beserta highlight watchlist.
   - Menyediakan tombol logout yang menghapus sesi dan kembali ke halaman login.

6. **Login & Register**
   - Registrasi user baru (nama, email, password) disimpan ke Firebase.
   - Login memverifikasi user dan menyimpan sesi lokal untuk greeting.

## Alur Navigasi
1. **Bottom Navigation**
   - Empat menu (Browse, Search, Watchlist, About) dipasang pada `BottomNavigationView`. Title di `MaterialToolbar` otomatis menyesuaikan fragment aktif.

2. **Detail Navigation**
   - Saat pengguna memilih film pada daftar (Browse/Search/Watchlist), aplikasi melakukan intent ke `MovieDetailActivity` dengan membawa `EXTRA_MOVIE_ID`.
   - Activity detail membaca ulang data dari `MovieDataSource` untuk memastikan state terbaru (misal status watchlist).

3. **Authentication Flow**
   - Jika belum login, aplikasi mengarahkan ke `LoginActivity`.
   - Logout di About akan menghapus sesi dan kembali ke login.

## Cara Menjalankan
1. Buka proyek pada Android Studio (versi flamingo ke atas/AGP 8+).
2. Pastikan `google-services.json` sudah terpasang dan Firebase Realtime Database sudah diaktifkan.
3. Jalankan `./gradlew :app:assembleDebug` atau gunakan tombol “Run” di Android Studio.
4. Pastikan emulator/perangkat memiliki koneksi internet agar Glide bisa mengambil poster dari URL TMDB.
5. Jika database kosong, aplikasi akan melakukan seed data dari `app/src/main/assets/movies.json` saat pertama kali dijalankan.

## Catatan Pengembangan
- Autentikasi saat ini bersifat sederhana dan menyimpan password sebagai plain text di Firebase. Untuk produksi, gunakan Firebase Authentication.
- Sesi login disimpan di `SharedPreferences` untuk kebutuhan greeting dan akses aplikasi.
- Desain warna, ikon, dan komponen dapat dikembangkan lebih lanjut agar semakin mendekati Netflix (misalnya menambahkan carousel atau kategori).
- Semua string disimpan di `values/strings.xml` untuk memudahkan lokaliasi.
