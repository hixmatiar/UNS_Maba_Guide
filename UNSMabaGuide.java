import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

class Mahasiswa {

    String nim;
    String nama;
    String asalDaerah;
    ArrayList<String> hobi;
    String daerahKost;

    Mahasiswa(String nim, String nama, String asalDaerah,
              ArrayList<String> hobi, String daerahKost) {
        this.nim        = nim;
        this.nama       = nama;
        this.asalDaerah = asalDaerah;
        this.hobi       = hobi;
        this.daerahKost = daerahKost;
    }

    @Override
    public String toString() {
        return nama + " (" + nim + ") | Asal: " + asalDaerah + " | Kost: " + daerahKost;
    }
}

class Utils {

    static String normalisasiDaerah(String asal) {
        String s = asal.toLowerCase();

        if (ada(s, "jakarta","bogor","depok","tangerang","bekasi","jabodetabek"))
            return "Jabodetabek";

        if (ada(s, "jawa timur","jatim","surabaya","malang","sidoarjo","jember",
                   "madiun","kediri","blitar","mojokerto","pasuruan","probolinggo",
                   "banyuwangi","tuban","lamongan","gresik","ngawi","ponorogo",
                   "magetan","trenggalek","lumajang","bondowoso","situbondo"))
            return "Jawa Timur";

        if (ada(s, "jawa barat","jabar","bandung","cirebon","tasikmalaya",
                   "sukabumi","garut","cianjur","karawang","purwakarta",
                   "subang","indramayu","majalengka","kuningan"))
            return "Jawa Barat";

        if (ada(s, "yogyakarta","jogja","sleman","bantul","gunung kidul",
                   "gunungkidul","kulon progo","kulonprogo","diy"))
            return "Yogyakarta (DIY)";

        if (ada(s, "jawa tengah","jateng","semarang","solo","surakarta",
                   "purwokerto","tegal","pekalongan","magelang","klaten","boyolali",
                   "wonogiri","karanganyar","salatiga","kudus","jepara","demak",
                   "purworejo","wonosobo","banyumas","cilacap","brebes","pemalang",
                   "batang","kendal","temanggung","sragen","blora","rembang","pati",
                   "grobogan","kebumen","banjarnegara","purbalingga"))
            return "Jawa Tengah";

        if (ada(s, "kalimantan","kalsel","kaltim","kalbar","kalteng","kaltara",
                   "banjarmasin","samarinda","pontianak","palangkaraya",
                   "balikpapan","bontang","tarakan","singkawang","kotabaru"))
            return "Kalimantan";

        if (ada(s, "sumatera","sumatra","sumsel","sumbar","sumut","medan","padang",
                   "palembang","lampung","riau","aceh","jambi","bengkulu","bangka",
                   "belitung","pekanbaru","dumai","batam","tanjungpinang","lubuklinggau"))
            return "Sumatera";

        if (ada(s, "sulawesi","makassar","manado","palu","kendari",
                   "gorontalo","mamuju","bitung","palopo","parepare"))
            return "Sulawesi";

        if (ada(s, "papua","jayapura","manokwari","sorong","merauke","timika","wamena"))
            return "Papua";

        if (ada(s, "bali","denpasar","singaraja","gianyar","tabanan","badung"))
            return "Bali";

        if (ada(s, "ntt","nusa tenggara","kupang","mataram","lombok",
                   "sumbawa","flores","timor","ende","maumere"))
            return "Nusa Tenggara";

        if (ada(s, "maluku","ambon","ternate","tidore","sofifi"))
            return "Maluku";

        return asal; // kembalikan apa adanya jika tidak cocok
    }

    // Cek apakah teks mengandung salah satu kata kunci
    static boolean ada(String teks, String... kataKunci) {
        for (String k : kataKunci) {
            if (teks.contains(k)) return true;
        }
        return false;
    }
}

class SocialGraph {

    // ---- Struktur Data ----
    private HashMap<String, Mahasiswa>         database;       // node
    private HashMap<String, ArrayList<String>> adjacencyList;  // edge

    // Minimum skor agar dua mahasiswa terhubung (punya edge)
    private static final double THRESHOLD = 0.25;

    SocialGraph() {
        database      = new HashMap<>();
        adjacencyList = new HashMap<>();
    }

    // ----------------------------------------------------------
    // Tambah mahasiswa baru ke graph
    // Sekaligus hitung similarity dengan semua node yang ada,
    // lalu buat edge jika skor >= THRESHOLD.
    // Kompleksitas: O(n) per mahasiswa baru
    // ----------------------------------------------------------
    void tambahMahasiswa(Mahasiswa mhs) {
        database.put(mhs.nim, mhs);
        adjacencyList.put(mhs.nim, new ArrayList<>());

        for (String nimLain : database.keySet()) {
            if (nimLain.equals(mhs.nim)) continue;

            double skor = hitungSimilarity(mhs, database.get(nimLain));

            if (skor >= THRESHOLD) {
                adjacencyList.get(mhs.nim).add(nimLain); // edge A -> B
                adjacencyList.get(nimLain).add(mhs.nim); // edge B -> A
            }
        }
    }

    // ----------------------------------------------------------
    // ALGORITMA 1: Weighted Similarity Scoring
    //
    // Hitung skor kecocokan dua mahasiswa (0.0 - 1.0)
    //   Asal daerah sama  -> +0.40
    //   Hobi sama         -> +0.25
    //   Daerah kost sama  -> +0.35
    // ----------------------------------------------------------
    double hitungSimilarity(Mahasiswa a, Mahasiswa b) {
        double skor = 0.0;

        // Cek asal daerah (normalisasi dulu agar "Surabaya" == "Malang" = "Jawa Timur")
        String daerahA = Utils.normalisasiDaerah(a.asalDaerah);
        String daerahB = Utils.normalisasiDaerah(b.asalDaerah);
        if (daerahA.equalsIgnoreCase(daerahB)) skor += 0.40;

        // Cek hobi (cukup satu kesamaan)
        boolean hobiSama = false;
        luarLoop:
        for (String hA : a.hobi)
            for (String hB : b.hobi)
                if (hA.trim().equalsIgnoreCase(hB.trim())) { hobiSama = true; break luarLoop; }
        if (hobiSama) skor += 0.25;

        // Cek daerah kost
        if (a.daerahKost.trim().equalsIgnoreCase(b.daerahKost.trim())) skor += 0.35;

        return Math.min(skor, 1.0);
    }

    // ----------------------------------------------------------
    // ALGORITMA 2: BFS (Breadth-First Search)
    //
    // Telusuri graph dari node user, kumpulkan kandidat teman
    // sampai kedalaman level 2 (teman dari teman).
    //
    // Struktur data BFS:
    //   Queue<String>   antrian         -> FIFO, pastikan level-by-level
    //   HashSet<String> sudahDikunjungi -> O(1) cek agar tidak loop
    //
    // Hasil diurutkan by skor menggunakan Bubble Sort.
    // ----------------------------------------------------------
    ArrayList<String[]> rekomendasiTeman(String nimUser, int topN) {
        ArrayList<String[]> hasil = new ArrayList<>();
        if (!adjacencyList.containsKey(nimUser)) return hasil;

        Queue<String>   antrian         = new LinkedList<>();
        HashSet<String> sudahDikunjungi = new HashSet<>();

        antrian.add(nimUser);
        sudahDikunjungi.add(nimUser);

        int level    = 0;
        int maxLevel = 2;

        while (!antrian.isEmpty() && level < maxLevel) {
            int ukuranLevel = antrian.size();
            level++;

            for (int i = 0; i < ukuranLevel; i++) {
                String nimSekarang = antrian.poll();

                for (String nimTetangga : adjacencyList.get(nimSekarang)) {
                    if (sudahDikunjungi.contains(nimTetangga)) continue;

                    sudahDikunjungi.add(nimTetangga);
                    antrian.add(nimTetangga);

                    Mahasiswa user     = database.get(nimUser);
                    Mahasiswa kandidat = database.get(nimTetangga);
                    double skor        = hitungSimilarity(user, kandidat);
                    String alasan      = buatAlasan(user, kandidat, level);

                    hasil.add(new String[]{ nimTetangga, alasan, String.valueOf(skor) });
                }
            }
        }

        // Fallback: jika tidak ada koneksi BFS, hitung langsung ke semua
        if (hasil.isEmpty()) hasil = fallbackSimilarity(nimUser);

        // Bubble Sort: urutkan dari skor terbesar ke terkecil
        for (int i = 0; i < hasil.size() - 1; i++) {
            for (int j = 0; j < hasil.size() - 1 - i; j++) {
                double kiri  = Double.parseDouble(hasil.get(j)[2]);
                double kanan = Double.parseDouble(hasil.get(j + 1)[2]);
                if (kiri < kanan) {
                    String[] tmp = hasil.get(j);
                    hasil.set(j, hasil.get(j + 1));
                    hasil.set(j + 1, tmp);
                }
            }
        }

        return new ArrayList<>(hasil.subList(0, Math.min(topN, hasil.size())));
    }

    // Fallback jika BFS tidak menemukan koneksi apapun
    private ArrayList<String[]> fallbackSimilarity(String nimUser) {
        ArrayList<String[]> hasil = new ArrayList<>();
        Mahasiswa user = database.get(nimUser);
        for (String nim : database.keySet()) {
            if (nim.equals(nimUser)) continue;
            Mahasiswa kandidat = database.get(nim);
            double skor = hitungSimilarity(user, kandidat);
            if (skor > 0)
                hasil.add(new String[]{ nim, buatAlasan(user, kandidat, 1), String.valueOf(skor) });
        }
        return hasil;
    }

    // Buat teks alasan mengapa dua mahasiswa direkomendasikan
    private String buatAlasan(Mahasiswa a, Mahasiswa b, int level) {
        ArrayList<String> alasan = new ArrayList<>();

        String daerahA = Utils.normalisasiDaerah(a.asalDaerah);
        String daerahB = Utils.normalisasiDaerah(b.asalDaerah);
        if (daerahA.equalsIgnoreCase(daerahB))
            alasan.add("Satu daerah (" + daerahA + ")");

        luarLoop:
        for (String hA : a.hobi)
            for (String hB : b.hobi)
                if (hA.trim().equalsIgnoreCase(hB.trim())) {
                    alasan.add("Hobi " + hA.trim());
                    break luarLoop;
                }

        if (a.daerahKost.trim().equalsIgnoreCase(b.daerahKost.trim()))
            alasan.add("Kost " + b.daerahKost);

        if (alasan.isEmpty())
            return level >= 2 ? "Teman dari temanmu" : "Mungkin kamu kenal";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < alasan.size(); i++) {
            if (i > 0) sb.append(" | ");
            sb.append(alasan.get(i));
        }
        return sb.toString();
    }

    // ---- Getter ----
    HashMap<String, Mahasiswa> getDatabase()       { return database; }
    Mahasiswa getMahasiswa(String nim)             { return database.get(nim); }

    void tampilkanInfoGraph() {
        int totalEdge = 0;
        for (ArrayList<String> adj : adjacencyList.values()) totalEdge += adj.size();
        System.out.println("\n  === INFO GRAPH ===");
        System.out.println("  Total mahasiswa (node) : " + database.size());
        System.out.println("  Total koneksi (edge)   : " + (totalEdge / 2));
        System.out.println("  Threshold koneksi      : " + (int)(THRESHOLD * 100) + "%\n");
    }
}

public class UNSMabaGuide {
    static SocialGraph      graph;
    static PaguyubanManager paguyuban;
    static Scanner          sc = new Scanner(System.in);

    public static void main(String[] args) {
        graph     = new SocialGraph();
        paguyuban = new PaguyubanManager(graph.getDatabase());

        muatSeedData();

        System.out.println();
        garis('*', 50);
        System.out.println("*      UNS MABA-GUIDE - Selamat Datang!       *");
        System.out.println("*  Sistem Adaptasi Mahasiswa Baru UNS Solo    *");
        garis('*', 50);

        boolean jalan = true;
        while (jalan) {
            tampilMenu();
            switch (bacaInt("  Pilihan kamu (1-5): ")) {
                case 1: menuDaftar();                          break;
                case 2: menuCariTeman();                       break;
                case 3: paguyuban.tampilkanSemuaPaguyuban();   break;
                case 4: graph.tampilkanInfoGraph();            break;
                case 5:
                    jalan = false;
                    System.out.println("\n  Terima kasih! Semangat beradaptasi di UNS!\n");
                    break;
                default:
                    System.out.println("\n  [!] Pilihan tidak valid (1-5).\n");
            }
        }

        sc.close();
    }

    // ----------------------------------------------------------
    // Menu
    // ----------------------------------------------------------
    static void tampilMenu() {
        System.out.println();
        garis('=', 50);
        System.out.println("  MENU UTAMA - UNS MABA-GUIDE");
        garis('=', 50);
        System.out.println("  1. Daftar sebagai Mahasiswa Baru");
        System.out.println("  2. Cari Rekomendasi Teman");
        System.out.println("  3. Lihat Semua Paguyuban");
        System.out.println("  4. Info Jaringan (Statistik Graph)");
        System.out.println("  5. Keluar");
        garis('-', 50);
    }

    // ----------------------------------------------------------
    // Menu 1: Daftar Mahasiswa Baru
    // ----------------------------------------------------------
    static void menuDaftar() {
        System.out.println();
        garis('=', 50);
        System.out.println("  PENDAFTARAN MAHASISWA BARU");
        garis('=', 50);

        System.out.print("  NIM         : ");
        String nim = sc.nextLine().trim();
        if (graph.getMahasiswa(nim) != null) {
            System.out.println("\n  [!] NIM " + nim + " sudah terdaftar!\n");
            return;
        }

        System.out.print("  Nama        : ");
        String nama = sc.nextLine().trim();

        System.out.println("  Asal Daerah (contoh: Surabaya / Jakarta / Bandung)");
        System.out.print("              : ");
        String asal = sc.nextLine().trim();

        System.out.println("  Hobi (pisah koma, contoh: Basket, Gaming, Musik)");
        System.out.print("              : ");
        ArrayList<String> hobi = new ArrayList<>();
        for (String h : sc.nextLine().split(","))
            if (!h.trim().isEmpty()) hobi.add(h.trim());

        System.out.println("  Daerah Kost (contoh: Kentingan / Jebres / Kos UNS)");
        System.out.print("              : ");
        String kost = sc.nextLine().trim();

        Mahasiswa mhs = new Mahasiswa(nim, nama, asal, hobi, kost);
        graph.tambahMahasiswa(mhs);
        paguyuban.tambahKePaguyuban(mhs);

        System.out.println("\n  [OK] Selamat datang di UNS, " + nama + "!\n");

        tampilRekomendasi(nim);
        paguyuban.tampilkanInfoPaguyuban(nim);
    }

    // ----------------------------------------------------------
    // Menu 2: Cari Rekomendasi Teman
    // ----------------------------------------------------------
    static void menuCariTeman() {
        System.out.println();
        garis('-', 50);
        System.out.print("  Masukkan NIM kamu: ");
        String nim = sc.nextLine().trim();

        Mahasiswa mhs = graph.getMahasiswa(nim);
        if (mhs == null) {
            System.out.println("  [!] NIM tidak ditemukan. Silakan daftar dulu (Menu 1).\n");
            return;
        }

        System.out.println("  Halo, " + mhs.nama + "! Berikut rekomendasi untukmu:\n");
        tampilRekomendasi(nim);
        paguyuban.tampilkanInfoPaguyuban(nim);
    }

    // ----------------------------------------------------------
    // Helper: tampilkan hasil rekomendasi BFS
    // ----------------------------------------------------------
    static void tampilRekomendasi(String nim) {
        ArrayList<String[]> list = graph.rekomendasiTeman(nim, 5);

        System.out.println();
        garis('=', 50);
        System.out.println("  REKOMENDASI TEMAN (Top 5)");
        garis('=', 50);

        if (list.isEmpty()) {
            System.out.println("  Belum ada rekomendasi. Tunggu maba lain mendaftar!");
        } else {
            int no = 1;
            for (String[] r : list) {
                Mahasiswa m = graph.getMahasiswa(r[0]);
                int persen  = (int)(Double.parseDouble(r[2]) * 100);
                System.out.println("  " + no++ + ". " + m.nama);
                System.out.println("     Asal   : " + m.asalDaerah);
                System.out.println("     Kost   : " + m.daerahKost);
                System.out.println("     Alasan : " + r[1]);
                System.out.println("     Cocok  : " + persen + "%");
                garis('-', 50);
            }
        }
        System.out.println();
    }

    // ----------------------------------------------------------
    // Seed Data: 15 mahasiswa awal
    // ----------------------------------------------------------
    static void muatSeedData() {
        System.out.println("\n  Loading data mahasiswa UNS...");
        seed("232300001", "Eko Prasetyo",    "Surabaya, Jawa Timur",       "Kentingan", "Basket","Gaming");
        seed("232300002", "Rina Aulia",      "Malang, Jawa Timur",         "Kentingan", "Gaming","Memasak");
        seed("232300003", "Dimas Putra",     "Jember, Jawa Timur",         "Jebres",    "Basket","Musik");
        seed("232300004", "Sari Dewi",       "Bandung, Jawa Barat",        "Kentingan", "Melukis","Musik");
        seed("232300005", "Hendra Kusuma",   "Banjarmasin, Kalimantan",    "Kos UNS",   "Futsal","Gaming");
        seed("232300006", "Dina Pratiwi",    "Jakarta, Jabodetabek",       "Jebres",    "Menyanyi","Tari");
        seed("232300007", "Rizky Ramadhan",  "Bekasi, Jabodetabek",        "Jebres",    "Gaming","Futsal");
        seed("232300008", "Maya Salsabila",  "Depok, Jabodetabek",         "Kentingan", "Menyanyi","Memasak");
        seed("232300009", "Agus Santoso",    "Sidoarjo, Jawa Timur",       "Kos UNS",   "Basket","Futsal");
        seed("232300010", "Putri Rahayu",    "Yogyakarta, DIY",            "Kentingan", "Melukis","Membaca");
        seed("232300011", "Farhan Maulana",  "Medan, Sumatera Utara",      "Jebres",    "Futsal","Musik");
        seed("232300012", "Dewi Anggraini",  "Padang, Sumatera Barat",     "Kos UNS",   "Memasak","Menyanyi");
        seed("232300013", "Bagas Aditya",    "Kediri, Jawa Timur",         "Jebres",    "Gaming","Futsal");
        seed("232300014", "Sinta Wulandari", "Makassar, Sulawesi Selatan", "Kos UNS",   "Menyanyi","Tari");
        seed("232300015", "Andre Wijaya",    "Pontianak, Kalimantan",      "Kentingan", "Gaming","Basket");
        System.out.println("  [OK] 15 data mahasiswa berhasil dimuat!\n");
    }

    static void seed(String nim, String nama, String asal, String kost, String... hobiArr) {
        Mahasiswa m = new Mahasiswa(nim, nama, asal, new ArrayList<>(Arrays.asList(hobiArr)), kost);
        graph.tambahMahasiswa(m);
        paguyuban.tambahKePaguyuban(m);
    }

    // ----------------------------------------------------------
    // Helper: baca input angka dengan validasi
    // ----------------------------------------------------------
    static int bacaInt(String prompt) {
        System.out.print(prompt);
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    static void garis(char c, int n) {
        StringBuilder sb = new StringBuilder("  ");
        for (int i = 0; i < n; i++) sb.append(c);
        System.out.println(sb);
    }
}

