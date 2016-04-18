import redis.clients.jedis.Jedis;
import tools.JEdisSetting;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ckboss on 16-4-16.
 */

class Producer implements Runnable {

    private final CountDownLatch countDownLatch;
    private final BlockingQueue queue;
    private File dir;
    private int id;

    Producer(int _id,CountDownLatch contdown,String filepath , BlockingQueue queue) {
        this.queue = queue;
        dir = new File(filepath);
        id = _id;
        countDownLatch = contdown;
    }

    @Override
    public void run() {
        try {
            File[] files = dir.listFiles();
            for(File file : files) {
                Report report = new Report(file.getAbsolutePath());
                queue.add(report);
            }
            countDownLatch.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

class Consumer implements Runnable {

    private final CountDownLatch countDownLatch;
    private final BlockingQueue queue;

    private int id;
    private Jedis jedis;
    private static Field[] fields = Report.class.getDeclaredFields();

    Consumer(int _id,CountDownLatch contdown,BlockingQueue queue) {
        this.queue = queue;
        jedis = JEdisSetting.jedisPool.getResource();
        id=_id;
        countDownLatch = contdown;
    }

    @Override
    public void run() {
        try {
            while (!queue.isEmpty()) {
                Report report = (Report) queue.take();
                GaoIt(report);
            }
            countDownLatch.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void GaoIt(Report report) {

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
    }
}


public class MulitPretreatment {

    public static AtomicInteger cnt = new AtomicInteger(0);

    private Jedis jedis;
    private static Field[] fields = Report.class.getDeclaredFields();

    MulitPretreatment() {

        long begin = System.currentTimeMillis();
        long end = 0;

        int N = 30;
        BlockingQueue blockingQueue = new LinkedBlockingQueue<>();
        CountDownLatch doneSignal = new CountDownLatch(1);

        String filepath = "/home/ckboss/Documents/MachineLearning/数据集/搜索数据/trec_data1_renameCheckSum";
        //String filepath = "/home/ckboss/Documents/MachineLearning/数据集/搜索数据/101";
        Producer producer0 = new Producer(0,doneSignal,filepath,blockingQueue);

        new Thread(producer0).start();
        //new Thread(producer1).start();

        try {
            doneSignal.await();
            System.out.println("queue size: "+blockingQueue.size());
            doneSignal = new CountDownLatch(N);
            int size = blockingQueue.size();
            jedis.set("docnum",String.valueOf(size));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<Consumer> ac = new ArrayList<>();

        for(int i=0;i<N;i++) {
            ac.add(new Consumer(i,doneSignal,blockingQueue));
        }

        for(int i=0;i<N;i++) {
            new Thread(ac.get(i)).start();
        }

        try {

            doneSignal.await();

            // 合并
            // calu avg len
            jedis = JEdisSetting.jedisPool.getResource();
            for(Field field : fields) {
                String name = field.getName();
                try {
                    int len = new File(filepath).listFiles().length;
                    int num = Integer.valueOf(jedis.hget("avglen", name)) / len;
                    jedis.hset("avglen", name, String.valueOf(num));
                } catch (Exception e) {
                    //System.out.println(name+" ---> "+jedis.hget("avglen", name));
                }
            }
            jedis.close();

            end = System.currentTimeMillis();
            System.out.println(end - begin);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MulitPretreatment();
    }
}
