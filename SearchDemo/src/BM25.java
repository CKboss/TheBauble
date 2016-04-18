import redis.clients.jedis.Jedis;
import tools.JEdisSetting;
import tools.Stemmer;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by ckboss on 16-4-18.
 */
public class BM25 {

    int docnum ;
    String query ;
    private double k1 = 2,b = 0.75;
    private Jedis jedis ;

    BM25() {
        jedis = JEdisSetting.jedisPool.getResource();
        docnum = Integer.valueOf(jedis.get("docnum"));
    }

    ArrayList<String> parase(String str) {

        String[] words = str.replaceAll("\n|\r|\t|/|-"," ").split(" ");

        Stemmer stemmer = new Stemmer();

        ArrayList<String> ans = new ArrayList<>();

        for(String word : words) {

            word = word.toLowerCase();
            if(jedis.sismember("stopword",word)) continue;
            word = stemmer.StemmerString(word);
            if(word.length()==0) continue;

            ans.add(word);
        }

        return ans;
    }

    double IDF(String qi,String field) {
        String key = qi+"@"+field;
        double nq = 0;
        try {
            nq = Double.valueOf(jedis.hlen(key));
        } catch (Exception e) {
        }

        double fengzhi = docnum - nq + 0.5;
        double fenmu = nq + 0.5;

        return Math.log(fengzhi/fenmu);
    }

    double Relation(String word,String report,String field) {

        double avgdl = 0,dl=0,f1=0,K=0;

        try {

            avgdl = Double.valueOf(jedis.hget("avglen", field));
            dl = Double.valueOf(jedis.hget("len@" + field, report));
            f1 = Double.valueOf(jedis.hget(word + "@" + field, report));
            K = k1 * (1 - b + b * dl / avgdl);

        } catch (Exception e) {
            return 0;
        }

        return f1*(k1+1)/(f1+K);
    }

    double QueryField(String q, String report, String field) {

        query = q;
        ArrayList<String> words = parase(query);

        /*
        System.out.println(words);

        words.stream().forEach(s -> {

            System.out.println(s+" IDF: "+IDF(s,field));
            System.out.println(s+" Relation: "+Relation(s,report,field));

        });
        */

        return words.stream().mapToDouble(s -> IDF(s, field) * Relation(s, report, field)).reduce(0.0, (x, y) -> x + y);
    }

    public double Query(String q,String report) {

        double ans = 0;

        Field[] fields = Report.class.getDeclaredFields();
        for(Field field : fields) {
            String name = field.getName();
            if(name.equals("report_text")) {
                ans += 0.95*QueryField(q,report,name);
            } else if(name.equals("chief_complaint")) {
                //ans += 0.05*QueryField(q,report,name);
            }
        }

        return ans;
    }

    public double getK1() {
        return k1;
    }

    public void setK1(double k1) {
        this.k1 = k1;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public static void main(String[] args) {

        long beg = System.currentTimeMillis();

        BM25 bm25 = new BM25();
        for(int i=1;i<101000;i++) {
            String report = "report"+i;
            double ans = bm25.Query("Patients with hearing loss", report);
        }

        long end = System.currentTimeMillis();
        //double ans = bm25.Query("Patients with hearing loss", "report145");
        //System.out.println(ans);

        System.out.println((end-beg+1)/1000.);
    }
}
