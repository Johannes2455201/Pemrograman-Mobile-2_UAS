# My Movie Favorite

## Ikhtisar Proyek
My Movie Favorite adalah aplikasi Android studi kasus yang menampilkan katalog film ala Netflix dengan data dummy. Aplikasi dibangun menggunakan Android Studio (AGP 8.13.0) dan bahasa pemrograman Java. Fokus proyek ini adalah memperlihatkan struktur arsitektur dasar Android modern (fragment, adapter, asset JSON, dan komponen Material Design) untuk keperluan asesmen.

## Konsep Android yang Digunakan
- **Activity & Fragment**
  - `MainActivity` bertugas sebagai host seluruh tab utama. Untuk setiap menu, aplikasi menampilkan fragment yang berbeda: `MovieListFragment`, `SearchFragment`, `WatchlistFragment`, dan `ProfileFragment`. Fragment dipilih via `BottomNavigationView`, sehingga pengalaman navigasi terasa native dan modular.
  - `MovieDetailActivity` berjalan terpisah untuk menampilkan detail film. Activity ini memanfaatkan `MaterialToolbar` dengan tombol kembali serta memuat konten melalui Glide.

- **RecyclerView & Adapter**
  - Daftar film pada tab Browse/Search/Watchlist dirender memakai `RecyclerView` dengan `MovieAdapter`. Adapter ini memanfaatkan pola ViewHolder untuk menjaga performa scrolling serta menerapkan binding state (judul, genre, rating, overview).
  - Setiap kartu menggunakan `MaterialCardView` dan `Chip` untuk rating, mengikuti guideline Material Design.

- **Sumber Data Dummy**
  - Data film disimpan di `app/src/main/assets/movies.json`. File ini memuat id, judul, overview, genre, rating, runtime, status watchlist, dan URL poster.
  - `MovieDataSource` membaca JSON melalui `AssetManager`, kemudian memetakan ke model `Movie`. Data disimpan in-memory dan dipakai untuk pencarian, filter watchlist, serta toggle status watchlist.

- **Image Loading**
  - Library **Glide** digunakan untuk memuat URL poster pada daftar dan detail film. Glide menangani caching, placeholder, dan fallback agar UI tetap stabil ketika jaringan lambat.

- **Material Components**
  - Aplikasi mengadopsi warna dominan `netflix_black` dan `netflix_red`. Komponen seperti `MaterialToolbar`, `MaterialButton`, `TextInputLayout`, dan `MaterialDivider` digunakan untuk mempertahankan estetika Netflix.
  - Tema diatur melalui `Theme.MaterialComponents.DayNight.NoActionBar` dengan penyesuaian warna di `values/themes.xml`.

## Fitur Utama
1. **Browse (Browse/Showing Today)**
   - Menampilkan daftar film yang tersedia hari ini dengan layout kartu. Heading “Showing Today” ditampilkan di atas daftar.
   - Klik kartu film membuka halaman detail.

2. **Search**
   - Input kata kunci melalui `TextInputEditText`. Daftar hasil diperbarui secara real-time saat pengguna mengetik (filter berdasarkan judul, overview, atau genre).
   - Menyediakan empty state ketika tidak ada hasil cocok.

3. **Watchlist**
   - Menampilkan film yang ditandai sebagai watchlist. Data bersumber dari flag `watchlisted` pada model `Movie`. Tampilan empty state membantu memberi konteks ketika watchlist kosong.

4. **Detail Film**
   - Halaman detail menampilkan poster resolusi tinggi, metadata (tahun, rating, lama durasi), genre, dan sinopsis.
   - Tombol watchlist mendukung _toggle_: pengguna dapat menambahkan atau menghapus film dari watchlist langsung dari halaman detail. Feedback diberikan melalui `Snackbar`.
   - Disediakan tombol “Back” untuk kembali ke tab sebelumnya.

5. **Profile**
   - Menampilkan identitas pengguna (Johannes Triestanto, Kelas TIF RM 23 B, NIM 24552012008, Universitas Teknologi Bandung) beserta highlight watchlist (jumlah film).
   - Menggunakan `ShapeableImageView` sebagai avatar placeholder dan komponen Material untuk memisahkan section.

## Alur Navigasi
1. **Bottom Navigation**
   - Empat menu (Browse, Search, Watchlist, Profile) dipasang pada `BottomNavigationView`. Title di `MaterialToolbar` otomatis menyesuaikan fragment aktif.

2. **Detail Navigation**
   - Saat pengguna memilih film pada daftar (Browse/Search/Watchlist), aplikasi melakukan intent ke `MovieDetailActivity` dengan membawa `EXTRA_MOVIE_ID`.
   - Activity detail membaca ulang data dari `MovieDataSource` untuk memastikan state terbaru (misal status watchlist).

## Cara Menjalankan
1. Buka proyek pada Android Studio (versi flamingo ke atas/AGP 8+).
2. Jalankan `./gradlew :app:assembleDebug` atau gunakan tombol “Run” di Android Studio.
3. Pastikan emulator/perangkat memiliki koneksi internet agar Glide bisa mengambil poster dari URL TMDB.
4. Jika ingin menyesuaikan data film, edit `app/src/main/assets/movies.json` lalu jalankan ulang aplikasi.

## Catatan Pengembangan
- Aplikasi saat ini menyimpan status watchlist secara in-memory. Jika ingin persistensi lintas sesi, dapat menambahkan `SharedPreferences` atau database Room dan memperbarui `MovieDataSource`.
- Desain warna, ikon, dan komponen dapat dikembangkan lebih lanjut agar semakin mendekati Netflix (misalnya menambahkan carousel atau kategori).
- Semua string disimpan di `values/strings.xml` untuk memudahkan lokaliasi.
