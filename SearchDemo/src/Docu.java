import redis.clients.jedis.Jedis;
import tools.JEdisSetting;
import tools.Stemmer;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by ckboss on 16-4-15.
 */
public class Docu {

    Map<String,ArrayList<String>> fieldsmap = new HashMap<>();

    Docu(Report report) {

        Field[] fields = Report.class.getDeclaredFields();

        for(Field field : fields) {
            String key = field.getName();
            try {

                String content = (String) field.get(report);
                String[] words = content.replaceAll("\n|\r|\t|/|-"," ").split(" ");

                Stemmer stemmer = new Stemmer();
                Jedis jedis = JEdisSetting.jedisPool.getResource();

                ArrayList<String> wordlist = new ArrayList<>();

                for(String word : words) {
                    word = word.toLowerCase();
                    if(jedis.sismember("stopword",word)) continue;
                    word = stemmer.StemmerString(word);
                    if(word.length()==0) continue;

                    wordlist.add(word);
                }

                fieldsmap.put(key, wordlist);
                jedis.close();

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        Docu doc = new Docu(new Report("/tmp/report45.xml"));
        for(String key : doc.fieldsmap.keySet()) {
            System.out.println(key+" -----> ");
            for(int i=0,len=doc.fieldsmap.get(key).size();i<len;i++) {
                System.out.printf(doc.fieldsmap.get(key).get(i)+",");
            }
            System.out.println();
        }
    }
}
