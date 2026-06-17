import java.io.File;
import java.io.FileNotFoundException;
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
        this.nim = nim;
        this.nama = nama;
        this.asalDaerah = asalDaerah;
        this.hobi = hobi;
        this.daerahKost = daerahKost;
    }
    @Override
    public String toString() {
        return nama + " (" + nim + ") | Asal: " + asalDaerah + " | Kost: " + daerahKost;
    }
}

class Utils {
    static Scanner input = new Scanner(System.in);
    static boolean wilayah(String teks, String ... kataKunci) {
        for (String k : kataKunci) {
            if (teks.contains(k)) return true;
        }
        return false;
    }

    static String normalisasiDaerah(String provinsiInput) {
        String s = provinsiInput.toLowerCase().trim();
        if (wilayah(s, "banten")) 
            return "Banten";
        if (wilayah(s, "jakarta", "dki", "dki jakarta")) 
            return "DKI Jakarta";
        if (wilayah(s, "jawa timur", "jatim")) 
            return "Jawa Timur";
        if (wilayah(s, "jawa barat", "jabar")) 
            return "Jawa Barat";
        if (wilayah(s, "yogyakarta", "diy", "diy yogyakarta")) 
            return "DI Yogyakarta";
        if (wilayah(s, "jawa tengah", "jateng")) 
            return "Jawa Tengah";
        if (wilayah(s, "kalimantan selatan", "kalimantan utara", "kalimantan barat", "kalimantan timur", "kalimantan tengah")) 
            return "Kalimantan";
        if (wilayah(s, "sumatera barat", "sumatera utara", "sumatera selatan", "riau", "aceh", "lampung", "bengkulu", "kepulauan riau", "jambi")) 
            return "Sumatera";
        if (wilayah(s, "sulawesi selatan", "sulawesi utara", "sulawesi tengah", "sulawesi tenggara", "sulawesi barat")) 
            return "Sulawesi";
        if (wilayah(s, "papua","papua barat", "papua selatan", "papua tengah", "papua pegunungan")) 
            return "Papua";
        if (wilayah(s, "bali")) 
            return "Bali";
        if (wilayah(s, "nusa tenggara timur", "ntt")) 
            return "Nusa Tenggara Timur";
        if (wilayah(s, "nusa tenggara barat", "ntb")) 
            return "Nusa Tenggara Barat";
        if (wilayah(s, "maluku")) 
            return "Maluku";
        return "Wilayah Tidak Dikenal";
    }

        static void clearTerminal() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            System.out.println("Gagal Clear Terminal!");
        }
    }

    static void lanjut() {
        System.out.print("Tekan ENTER untuk lanjut...");
        input.nextLine();
    }
}

class SocialGraph {
    private HashMap<String, Mahasiswa> database;
    private HashMap<String, ArrayList<String>> adjacencyList;
    HashMap<String, Mahasiswa> getDatabase() { return database; }
    Mahasiswa getMahasiswa(String nim) { return database.get(nim); }

    private static final double batas = 0.25;
    SocialGraph() {
        database = new HashMap<>();
        adjacencyList = new HashMap<>();
    }

    void tambahMahasiswa(Mahasiswa mhs) {
        database.put(mhs.nim, mhs);
        adjacencyList.put(mhs.nim, new ArrayList<>());
        for (String nimLain : database.keySet()) {
            if (nimLain.equals(mhs.nim)) continue;
            double skor = hitungSimilarity(mhs, database.get(nimLain));
            if (skor >= batas) {
                adjacencyList.get(mhs.nim).add(nimLain);
                adjacencyList.get(nimLain).add(mhs.nim);
            }
        }
    }
    double hitungSimilarity(Mahasiswa a, Mahasiswa b) {
        double skor = 0.0;

        // Cek asal daerah
        String daerahA = Utils.normalisasiDaerah(a.asalDaerah);
        String daerahB = Utils.normalisasiDaerah(b.asalDaerah);
        if (daerahA.equalsIgnoreCase(daerahB)) skor += 0.40;

        // Cek hobi 
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
        int level = 0;
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
                    Mahasiswa user = database.get(nimUser);
                    Mahasiswa kandidat = database.get(nimTetangga);
                    double skor = hitungSimilarity(user, kandidat);
                    String alasan = buatAlasan(user, kandidat, level);
                    hasil.add(new String[]{ nimTetangga, alasan, String.valueOf(skor) });
                }
            }
        }

        // jika tidak ada koneksi BFS, hitung langsung ke semua
        if (hasil.isEmpty()) hasil = fallbackSimilarity(nimUser);

        // mengurutkan dari skor terbesar ke terkecil (Bubble Sort)
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
        for (String hobiA : a.hobi)
            for (String hobiB : b.hobi)
                if (hobiA.trim().equalsIgnoreCase(hobiB.trim())) {
                    alasan.add("Hobi " + hobiA.trim());
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
}

class PaguyubanManager {
    private HashMap<String, ArrayList<String>> grupPaguyuban;
    private HashMap<String, String> ketuaPaguyuban;
    private HashMap<String, Mahasiswa> database;
    PaguyubanManager(HashMap<String, Mahasiswa> database) {
        this.database = database;
        this.grupPaguyuban = new HashMap<>();
        this.ketuaPaguyuban = new HashMap<>();
    }

    // masukkan mahasiswa ke grup asal daerahnya
    void tambahKePaguyuban(Mahasiswa mhs) {
        String paguyuban = Utils.normalisasiDaerah(mhs.asalDaerah);
        if (!grupPaguyuban.containsKey(paguyuban)) {
            grupPaguyuban.put(paguyuban, new ArrayList<>());
            ketuaPaguyuban.put(paguyuban, mhs.nim);
        }
        grupPaguyuban.get(paguyuban).add(mhs.nim);
    }

    // cari mahasiswa dalam paguyuban
    String cariPaguyuban(String nimUser) {
        for (Map.Entry<String, ArrayList<String>> entry : grupPaguyuban.entrySet())
            if (entry.getValue().contains(nimUser)) return entry.getKey();
        return null;
    }

    void tampilkanInfoPaguyuban(String nimUser) {
        String namaPaguyuban = cariPaguyuban(nimUser);
        if (namaPaguyuban == null) { System.out.println("Paguyuban tidak ditemukan Bro"); return; }
        ArrayList<String> anggota = grupPaguyuban.get(namaPaguyuban);
        Mahasiswa ketua = database.get(ketuaPaguyuban.get(namaPaguyuban));
        String namaKetua = (ketua != null) ? ketua.nama : "Belum ditentukan";
        System.out.println();
        garis('=', 50);
        System.out.println("PAGUYUBAN DAERAHMU");
        garis('-', 50);
        System.out.println("Nama    : Paguyuban " + namaPaguyuban);
        System.out.println("Ketua   : " + namaKetua);
        System.out.println("Anggota : " + anggota.size() + " mahasiswa");
        garis('-', 50);
        System.out.println("Daftar Anggota:");
        int no = 1;
        for (String nim : anggota) {
            Mahasiswa m = database.get(nim);
            if (m == null) continue;
            String marker = nim.equals(nimUser) ? "   " : "";
            System.out.println(no++ + ". " + m.nama + " - " + m.asalDaerah + marker);
        }
        garis('=', 50);
        System.out.println();
    }

    void tampilkanSemuaPaguyuban() {
        System.out.println();
        garis('=', 50);
        System.out.println("Daftar Semua Paguyuban");
        garis('=', 50);
        if (grupPaguyuban.isEmpty()) {
            System.out.println("Belum ada paguyuban terbentuk.");
        } else {
            int no = 1;
            for (Map.Entry<String, ArrayList<String>> e : grupPaguyuban.entrySet()) {
                Mahasiswa ketua  = database.get(ketuaPaguyuban.get(e.getKey()));
                String namaKetua = (ketua != null) ? ketua.nama : "?";
                System.out.println(no++ + ". Paguyuban " + e.getKey());
                System.out.println("Ketua   : " + namaKetua);
                System.out.println("Anggota : " + e.getValue().size() + " mahasiswa");
                garis('-', 50);
            }
        }
        System.out.println();
        Utils.lanjut();
    }

    private void garis(char c, int n) {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < n; i++) sb.append(c);
        System.out.println(sb);
    }
}

public class UNSMabaGuide {
    static SocialGraph graph;
    static PaguyubanManager paguyuban;
    static Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        graph     = new SocialGraph();
        paguyuban = new PaguyubanManager(graph.getDatabase());
        muatSeedData();
        System.out.println();
        garis('-', 47);
        System.out.println("|      UNS MABA-GUIDE - Selamat Datang!       |");
        System.out.println("|  Sistem Adaptasi Mahasiswa Baru UNS Solo    |");
        garis('-', 47);

        boolean jalan = true;
        while (jalan) {
            tampilMenu();
            System.out.print("Pilihan kamu : ");
            String input_user = input.nextLine();
            switch (input_user) {
                case "1": 
                    menuDaftar();
                    break;
                case "2": 
                    menuCariTeman();
                    break;
                case "3": 
                    paguyuban.tampilkanSemuaPaguyuban();
                    break;
                case "0":
                    jalan = false;
                    System.out.println("\nTerima kasih! Semangat beradaptasi di UNS!\n");
                    break;
                default:
                    continue;
            }
        }
        input.close();
    }

    static void tampilMenu() {
        System.out.println();
        Utils.clearTerminal();
        garis('=', 50);
        System.out.println("MENU UTAMA - UNS MABA-GUIDE");
        garis('=', 50);
        System.out.println("1. Daftar sebagai Mahasiswa Baru");
        System.out.println("2. Cari Rekomendasi Teman");
        System.out.println("3. Lihat Semua Paguyuban");
        System.out.println("0. Keluar");
        garis('=', 50);
    }
    
    static String NIM() {
        while (true) {
            System.out.print("NIM : ");
            String nim = input.nextLine().trim();
            if (!nim.isEmpty() && nim.matches("^[a-zA-Z0-9]+$")) {
                return nim;
            }
            System.out.println(nim.isEmpty() ? "NIM tidak boleh kosong!" : "NIM hanya boleh huruf dan angka (tanpa simbol/spasi)!");
        }
    }

    static String Nama() {
        while (true) {
            System.out.print("Nama : ");
            String nama = input.nextLine().trim();
            if (!nama.isEmpty() && nama.matches("[a-zA-Z\\s]+")) return nama;
            System.out.println(nama.isEmpty() ? "Nama tidak boleh kosong!" : "Nama hanya boleh huruf!");
        }
    }

    static String Daerah() {
        while (true) {
            System.out.print("Asal Daerah (Contoh: Kota, Provinsi = Serang, Banten) : ");
            String daerah = input.nextLine().trim();
            String[] asalDaerah = daerah.split(",");
            String provinsi = asalDaerah[1].trim();
            String wilayah = Utils.normalisasiDaerah(provinsi);
            if (wilayah.equals("Wilayah Tidak Dikenal")) {
                System.out.println("Asal Daerah tidak dikenali! Pastikan format benar dan provinsi termasuk dalam daftar wilayah Indonesia.");
                continue;
            }
            if (!daerah.isEmpty() && daerah.matches("[a-zA-Z\\s,]+")) {
                if (daerah.contains(",")) {
                    return daerah;
                }
                System.out.println("Format salah! Harus menyertakan koma (Contoh: Serang, Banten)");
                continue;
            }
            System.out.println(daerah.isEmpty() ? "Asal Daerah tidak boleh kosong!" : "Asal Daerah hanya boleh huruf dan koma!");
        }
    }

    static String Kost() {
        while (true) {
            System.out.print("Daerah Kost (Contoh: Jebres / Asrama UNS) : ");
            String kost = input.nextLine().trim();
            if (!kost.isEmpty() && kost.matches("[a-zA-Z\\s]+")) return kost;
            System.out.println(kost.isEmpty() ? "Daerah Kost tidak boleh kosong!" : "Daerah Kost hanya boleh huruf!");
        }
    }

    static void menuDaftar() {
        System.out.println();
        garis('=', 50);
        System.out.println("Pendaftaran Mahasiswa Baru");
        garis('=', 50);
        String nim = NIM();
        if (graph.getMahasiswa(nim) != null) {
            System.out.println("NIM " + nim + " sudah terdaftar!\n");
            Utils.lanjut();
            return;
        }
        
        String nama = Nama();
        String asal = Daerah();
        String kost = Kost();
        System.out.println("Hobi (Contoh: Basket, Gaming, Musik) : ");
        ArrayList<String> hobi = new ArrayList<>();
        for (String h : input.nextLine().split(","))
            if (!h.trim().isEmpty()) hobi.add(h.trim());
        Mahasiswa mhs = new Mahasiswa(nim, nama, asal, hobi, kost);
        graph.tambahMahasiswa(mhs);
        paguyuban.tambahKePaguyuban(mhs);
        simpanKeCSV(nim, nama, asal, kost, hobi);
        System.out.println("\nSelamat Datang di UNS, " + nama);
        tampilRekomendasi(nim);
        paguyuban.tampilkanInfoPaguyuban(nim);
        Utils.lanjut();
    }

    static void menuCariTeman() {
        System.out.println();
        garis('=', 50);
        System.out.print("NIM : ");
        String nim = input.nextLine().trim();
        Mahasiswa mhs = graph.getMahasiswa(nim);
        if (mhs == null) {
            System.out.println("NIM tidak ditemukan, Silakan Daftar Terlebih Dahulu\n");
            return;
        }
        System.out.println("Halo, " + mhs.nama + " Berikut Rekomendasi Teman :\n");
        tampilRekomendasi(nim);
        paguyuban.tampilkanInfoPaguyuban(nim);
        Utils.lanjut();
    }

    static void tampilRekomendasi(String nim) {
        ArrayList<String[]> list = graph.rekomendasiTeman(nim, 5);
        System.out.println();
        garis('=', 50);
        System.out.println("Rekomendasi Teman (Top 5)");
        garis('=', 50);
        if (list.isEmpty()) {
            System.out.println("Belum ada rekomendasi");
        } else {
            int no = 1;
            for (String[] r : list) {
                Mahasiswa m = graph.getMahasiswa(r[0]);
                int persen  = (int)(Double.parseDouble(r[2]) * 100);
                System.out.println(no++ + ". " + m.nama);
                System.out.println("Asal   : " + m.asalDaerah);
                System.out.println("Kost   : " + m.daerahKost);
                System.out.println("Alasan : " + r[1]);
                System.out.println("Cocok  : " + persen + "%");
                garis('-', 50);
            }
        }
        System.out.println();
        Utils.lanjut();
    }

    static void muatSeedData() {
        String namaFile = "UNS_MABA_GUIDE/DataMahasiswa.csv";
        try {
            java.io.File file = new java.io.File(namaFile);
            Scanner fileScanner = new Scanner(file);
            boolean barisPertama = true; 
            
            while (fileScanner.hasNextLine()) {
                String baris = fileScanner.nextLine().trim();
                if (barisPertama) {
                    barisPertama = false;
                    continue;
                }
                String[] data = baris.split("\\s*,\\s*"); 
                
                if (data.length >= 5) {
                    String nim = data[0];
                    String nama = data[1];
                    String asal = data[2] + ", " + data[3];
                    String kost = data[4];
                    ArrayList<String> hobiList = new ArrayList<>();
                    for (int i = 5; i < data.length; i++) {
                        hobiList.add(data[i]);
                    }
                    seed(nim, nama, asal, kost, hobiList.toArray(new String[0]));
                }
            }
            fileScanner.close();
        } catch (java.io.FileNotFoundException e) {
            System.out.println("File " + namaFile + " tidak ditemukan");
        }
    }

    static void seed(String nim, String nama, String asal, String kost, String... hobiArr) {
        Mahasiswa m = new Mahasiswa(nim, nama, asal, new ArrayList<>(Arrays.asList(hobiArr)), kost);
        graph.tambahMahasiswa(m);
        paguyuban.tambahKePaguyuban(m);
    }

    static void simpanKeCSV(String nim, String nama, String asal, String kost, ArrayList<String> hobi) {
        String namaFile = "SDA_PROJECT/DataMahasiswa.csv";
        try {
            java.io.File file = new java.io.File(namaFile);
            boolean fileAdaIsinya = file.exists() && file.length() > 0;
            java.io.FileWriter fw = new java.io.FileWriter(file, true);
            java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);
            java.io.PrintWriter out = new java.io.PrintWriter(bw);
            
            StringBuilder hobiStr = new StringBuilder();
            for (int i = 0; i < hobi.size(); i++) {
                hobiStr.append(hobi.get(i));
                if (i < hobi.size() - 1) hobiStr.append(",");
            }
            String dataBaru = nim + "," + nama + "," + asal + "," + kost + "," + hobiStr.toString();
            if (fileAdaIsinya) {
                out.print("\n" + dataBaru);
            } else {
                out.print(dataBaru);
            }
            out.close();
            bw.close();
            fw.close();
        } catch (java.io.IOException e) {
            System.out.println("Gagal Menyimpan Data");
        }
    }

    static void garis(char c, int n) {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < n; i++) sb.append(c);
        System.out.println(sb);
    }
}

