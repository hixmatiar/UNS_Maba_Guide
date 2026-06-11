import java.util.ArrayList;

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

public class UNSMabaGuide {
    
}
