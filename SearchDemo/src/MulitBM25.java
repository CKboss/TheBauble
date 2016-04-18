import redis.clients.jedis.Jedis;
import tools.JEdisSetting;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ckboss on 16-4-18.
 */
public class MulitBM25 {

    Map<String,Double> sorces = new ConcurrentHashMap<>();

    class Customer extends BM25 implements Runnable{

        private final CountDownLatch countDownLatch;
        ArrayList<String> reportlist;
        String query;

        Customer(CountDownLatch countDownLatch,String query) {
            this.countDownLatch = countDownLatch;
            this.reportlist = new ArrayList<>();
            this.query = query;
        }

        @Override
        public void run() {

            for(String report : reportlist) {
                sorces.put(report,Query(query,report));
            }
            countDownLatch.countDown();
        }
    }


    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list = new LinkedList<>( map.entrySet() );
        Collections.sort( list, (o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }


    public void Gao(String query) {

        int N=5;
        long beg = System.currentTimeMillis();

        CountDownLatch countDownLatch = new CountDownLatch(N);

        ArrayList<Customer> customerArrayList = new ArrayList<>();

        for(int i=0;i<N;i++) {
            customerArrayList.add(new Customer(countDownLatch,query));
        }

        Jedis jedis = JEdisSetting.jedisPool.getResource();
        int docnum = Integer.valueOf(jedis.get("docnum"));
        jedis.close();

        for(int i=1;i<=docnum;i++) {
            customerArrayList.get(i%N).reportlist.add("report"+i);
        }

        for(int i=0;i<N;i++) {
            new Thread(customerArrayList.get(i)).start();
        }

        try {

            countDownLatch.await();
            long end = System.currentTimeMillis();
            System.out.println(end - beg +1);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void ShowTopResult(int cnt) {

        Map<String,Double> ordermap = sortByValue(sorces);
        cnt=10;
        for(String key : ordermap.keySet()) {
            System.out.println(key+" ---> "+ordermap.get(key));
            cnt--; if(cnt==0) break;
        }

    }

    public static void main(String[] args) {

        MulitBM25 mbm25 = new MulitBM25();
        mbm25.Gao("Patients with hearing loss");
        mbm25.ShowTopResult(10);

    }
}
