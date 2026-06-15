import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

class Mahasiswa {
    String nim;
    String nama;
    String asalDaerah;
    ArrayList<String> hobi;
    String daerahKost;
    Mahasiswa(String nim, String nama, String asalDaerah, ArrayList<String> hobi, String daerahKost) {
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
    static boolean ada(String teks, String ...kataKunci) {
        for (String k : kataKunci) {
            if (teks.contains(k)) return true;
        }
        return false;
    }
}

class SocialGraph {
    private HashMap<String, Mahasiswa>         database;
    private HashMap<String, ArrayList<String>> adjacencyList;
    private static final double THRESHOLD = 0.25;

    SocialGraph() {
        database      = new HashMap<>();
        adjacencyList = new HashMap<>();
    }

    void tambahMahasiswa(Mahasiswa mhs) {
        database.put(mhs.nim, mhs);
        adjacencyList.put(mhs.nim, new ArrayList<>());
        for (String nimLain : database.keySet()) {
            if (nimLain.equals(mhs.nim)) continue;
            double skor = hitungSimilarity(mhs, database.get(nimLain));
            if (skor >= THRESHOLD) {
                adjacencyList.get(mhs.nim).add(nimLain);
                adjacencyList.get(nimLain).add(mhs.nim);
            }
        }
    }

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

    HashMap<String, Mahasiswa> getDatabase() { return database; }
    Mahasiswa getMahasiswa(String nim) { return database.get(nim); }

    void tampilkanInfoGraph() {
        int totalEdge = 0;
        for (ArrayList<String> adj : adjacencyList.values()) totalEdge += adj.size();
        System.out.println("\n  === INFO GRAPH ===");
        System.out.println("  Total mahasiswa (node) : " + database.size());
        System.out.println("  Total koneksi (edge)   : " + (totalEdge / 2));
        System.out.println("  Threshold koneksi      : " + (int)(THRESHOLD * 100) + "%\n");
    }
}

class PaguyubanManager {
    private HashMap<String, ArrayList<String>> grupPaguyuban;
    private HashMap<String, String> ketuaPaguyuban;
    private HashMap<String, Mahasiswa> database; // referensi ke database SocialGraph
    PaguyubanManager(HashMap<String, Mahasiswa> database) {
        this.database       = database;
        this.grupPaguyuban  = new HashMap<>();
        this.ketuaPaguyuban = new HashMap<>();
    }

    // UNION: masukkan mahasiswa ke grup daerahnya
    void tambahKePaguyuban(Mahasiswa mhs) {
        String paguyuban = Utils.normalisasiDaerah(mhs.asalDaerah);
        if (!grupPaguyuban.containsKey(paguyuban)) {
            grupPaguyuban.put(paguyuban, new ArrayList<>());
            ketuaPaguyuban.put(paguyuban, mhs.nim); // orang pertama = ketua
        }
        grupPaguyuban.get(paguyuban).add(mhs.nim);
    }

    // FIND: cari mahasiswa ini ada di paguyuban mana
    String cariPaguyuban(String nimUser) {
        for (Map.Entry<String, ArrayList<String>> entry : grupPaguyuban.entrySet())
            if (entry.getValue().contains(nimUser)) return entry.getKey();
        return null;
    }

    void tampilkanInfoPaguyuban(String nimUser) {
        String namaPaguyuban = cariPaguyuban(nimUser);
        if (namaPaguyuban == null) { System.out.println("  [!] Paguyuban tidak ditemukan."); return; }
        ArrayList<String> anggota  = grupPaguyuban.get(namaPaguyuban);
        Mahasiswa ketua            = database.get(ketuaPaguyuban.get(namaPaguyuban));
        String namaKetua           = (ketua != null) ? ketua.nama : "Belum ditentukan";
        System.out.println();
        garis('=', 50);
        System.out.println("  PAGUYUBAN DAERAHMU");
        garis('-', 50);
        System.out.println("  Nama    : Paguyuban " + namaPaguyuban);
        System.out.println("  Ketua   : " + namaKetua);
        System.out.println("  Anggota : " + anggota.size() + " mahasiswa");
        garis('-', 50);
        System.out.println("  Daftar Anggota:");

        int no = 1;
        for (String nim : anggota) {
            Mahasiswa m = database.get(nim);
            if (m == null) continue;
            String marker = nim.equals(nimUser) ? "  <-- KAMU" : "";
            System.out.println("  " + no++ + ". " + m.nama + " - " + m.asalDaerah + marker);
        }

        garis('=', 50);
        System.out.println("  Ketua paguyuban siap membantu adaptasi!");
        System.out.println("  Cari tumpangan pulang kampung bareng :)");
        garis('=', 50);
        System.out.println();
    }

    void tampilkanSemuaPaguyuban() {
        System.out.println();
        garis('=', 50);
        System.out.println("  DAFTAR SEMUA PAGUYUBAN");
        garis('=', 50);

        if (grupPaguyuban.isEmpty()) {
            System.out.println("  Belum ada paguyuban terbentuk.");
        } else {
            int no = 1;
            for (Map.Entry<String, ArrayList<String>> e : grupPaguyuban.entrySet()) {
                Mahasiswa ketua  = database.get(ketuaPaguyuban.get(e.getKey()));
                String namaKetua = (ketua != null) ? ketua.nama : "?";
                System.out.println("  " + no++ + ". Paguyuban " + e.getKey());
                System.out.println("     Ketua   : " + namaKetua);
                System.out.println("     Anggota : " + e.getValue().size() + " mahasiswa");
                garis('-', 50);
            }
        }
        System.out.println();
    }

    private void garis(char c, int n) {
        StringBuilder sb = new StringBuilder("  ");
        for (int i = 0; i < n; i++) sb.append(c);
        System.out.println(sb);
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
                case 1: 
                    menuDaftar();
                    break;
                case 2: 
                    menuCariTeman();
                    break;
                case 3: 
                    paguyuban.tampilkanSemuaPaguyuban();
                    break;
                case 4: graph.tampilkanInfoGraph();
                    break;
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

    static void tampilMenu() {
        System.out.println();
        garis('=', 50);
        System.out.println("MENU UTAMA - UNS MABA-GUIDE");
        garis('=', 50);
        System.out.println("1. Daftar sebagai Mahasiswa Baru");
        System.out.println("2. Cari Rekomendasi Teman");
        System.out.println("3. Lihat Semua Paguyuban");
        System.out.println("4. Info Jaringan (Statistik Graph)");
        System.out.println("5. Keluar");
        garis('-', 50);
    }

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

        System.out.println("  Hobi (Contoh: Basket, Gaming, Musik)");
        System.out.print("              : ");
        ArrayList<String> hobi = new ArrayList<>();
        for (String h : sc.nextLine().split(","))
            if (!h.trim().isEmpty()) hobi.add(h.trim());

        System.out.println("  Daerah Kost (Contoh: Kentingan / Jebres / Asrama UNS)");
        System.out.print("              : ");
        String kost = sc.nextLine().trim();

        Mahasiswa mhs = new Mahasiswa(nim, nama, asal, hobi, kost);
        graph.tambahMahasiswa(mhs);
        paguyuban.tambahKePaguyuban(mhs);

        System.out.println("\n  [OK] Selamat Datang di UNS, " + nama + "!\n");

        tampilRekomendasi(nim);
        paguyuban.tampilkanInfoPaguyuban(nim);
    }

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

    static void muatSeedData() {
        System.out.println("\n  Loading data mahasiswa UNS...");
        seed("L0225001", "Eko Prasetyo", "Surabaya", "Kentingan", "Basket","Gaming");
        seed("L0225002", "Rina Aulia", "Malang", "Kentingan", "Gaming","Memasak");
        seed("L0225003", "Dimas Putra", "Jember", "Jebres",    "Basket","Musik");
        seed("L0225004", "Sari Dewi", "Bandung", "Kentingan", "Melukis","Musik");
        seed("L0225005", "Hendra Kusuma", "Banjarmasin", "Asrama UNS",   "Futsal","Gaming");
        seed("L0225006", "Dina Pratiwi", "Jakarta", "Jebres",    "Menyanyi","Tari");
        seed("L0225007", "Rizky Ramadhan", "Bekasi", "Jebres",    "Gaming","Futsal");
        seed("L0225008", "Maya Salsabila", "Depok", "Kentingan", "Menyanyi","Memasak");
        seed("L0225009", "Agus Santoso", "Sidoarjo", "Asrama UNS",   "Basket","Futsal");
        seed("L0225010", "Putri Rahayu", "Yogyakarta", "Kentingan", "Melukis","Membaca");
        seed("L0225011", "Farhan Maulana", "Medan", "Jebres",    "Futsal","Musik");
        seed("L0225012", "Dewi Anggraini", "Padang", "Asrama UNS",   "Memasak","Menyanyi");
        seed("L0225013", "Bagas Aditya", "Kediri", "Jebres",    "Gaming","Futsal");
        seed("L0225014", "Sinta Wulandari", "Makassar", "Asrama UNS",   "Menyanyi","Tari");
        seed("L0225015", "Andre Wijaya", "Pontianak", "Kentingan", "Gaming","Basket");
        System.out.println("  [OK] 15 data mahasiswa berhasil dimuat!\n");
    }

    static void seed(String nim, String nama, String asal, String kost, String... hobiArr) {
        Mahasiswa m = new Mahasiswa(nim, nama, asal, new ArrayList<>(Arrays.asList(hobiArr)), kost);
        graph.tambahMahasiswa(m);
        paguyuban.tambahKePaguyuban(m);
    }

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

