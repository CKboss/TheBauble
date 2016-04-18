import redis.clients.jedis.Jedis;
import tools.JEdisSetting;
import tools.Stemmer;
import tools.StopWords;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by ckboss on 16-4-15.
 */
public class Pretreatment {

    public Jedis jedis;

    Pretreatment() {
        jedis = JEdisSetting.jedisPool.getResource();
    }

    void DoIt(Report report) {

        Docu doc = new Docu(report);

        for(String field : doc.fieldsmap.keySet()) {
            for(String word : doc.fieldsmap.get(field)) {
                String key = word+"@"+field;
                jedis.hincrBy(key,report.checksum,1);
                // len
                String key2 = "len@"+field;
                jedis.hincrBy(key2,report.checksum,1);
            }
        }
    }

    void GaoIt(String dirpath) {

        File dir = new File(dirpath);
        File[] files = dir.listFiles();

        Field[] fields = Report.class.getDeclaredFields();

        int nt=0;
        for(File file : files) {
            //System.out.println(file.getAbsoluteFile());
            Report report = new Report(file.getAbsolutePath());
            DoIt(report);

            for(Field field : fields) {
                String name = field.getName();
                try {
                    int num = Integer.valueOf(jedis.hget("len@" + name, report.checksum));
                    jedis.hincrBy("avglen", name, num);
                } catch (NumberFormatException e) {
                    //e.printStackTrace();
                    //System.out.println(name+" ---> "+jedis.hget("len@"+name, report.checksum));
                }
            }

            nt++;
            if(nt%500==0) {
                System.out.println("nt: "+nt);
                System.out.printf("%.2f\n",((double)nt)/files.length);
            }
        }

        // calu avg len
        for(Field field : fields) {
            String name = field.getName();
            try {
                int num = Integer.valueOf(jedis.hget("avglen", name)) / files.length;
                jedis.hset("avglen", name, String.valueOf(num));
            } catch (Exception e) {
                System.out.println(name+" ---> "+jedis.hget("avglen", name));
            }
        }
    }

    void Close() {
        jedis.close();
    }

    public static void main(String[] args) {
        /*
        Report report = new Report("/tmp/report45.xml");
        pre.DoIt(report);
        */
        long being = System.currentTimeMillis();
        Pretreatment pre = new Pretreatment();
        //pre.GaoIt("/home/ckboss/Documents/MachineLearning/数据集/搜索数据/trec_data1_renameCheckSum");
        pre.GaoIt("/home/ckboss/Documents/MachineLearning/数据集/搜索数据/101");
        pre.Close();
        long end = System.currentTimeMillis();
        System.out.println(end-being);
    }
}
