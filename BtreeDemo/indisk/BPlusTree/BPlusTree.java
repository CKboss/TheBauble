package edu.ecnu.ica.index.indisk.BPlusTree;

import edu.ecnu.ica.index.store.IndexFileHandler;
import edu.ecnu.ica.index.store.IndexFileHandlerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by ckboss on 16-7-10.
 */
public class BPlusTree {

    /**
     * 这个B树不可以插入相同的Key,否则会导致查询不准确
     */

    public static class Config {

        public Config(String indexfilepath, boolean clean) {
            this.indexfile = indexfilepath;
            this.clean = clean;
        }

        public String indexfile;
        boolean clean = true;

    }

    Config config ;

    IndexFileHandler handler;
    static int id = 1;


    public PageNodeOperation pno;


    public IndexFileHandler getIndexFileHandler() {
        return handler;
    }

    public BPlusTree(String idexfilepath,boolean isclean) {
        this(new Config(idexfilepath,isclean));
    }

    public BPlusTree(Config _config) {

        this.config = _config;
        try {

            if(config.clean==true) {
                IndexFileHandlerFactory.createIndexFile(this.config.indexfile);
            }
            handler = IndexFileHandlerFactory.openIndexFile(this.config.indexfile);
            pno = new PageNodeOperation(handler);
            // root节点的初始化 (1号节点)
            if(config.clean==true) {
                pno.SetPageHeader(1, 1, -1);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public long Insert(long key,long value) {
        return this.Add(key,value);
    }

    public long Add(long key,long value) {
        int rootindex = (int) handler.getRootIndex();
        return pno.Insert(rootindex,-1,key,value);
    }

    public long Query(long key) {
        int rootindex = (int) handler.getRootIndex();
        return pno.Query(rootindex,key);
    }

    public void CloseHandler() {
        handler.close();
    }

    public static HashSet<Long> hi = new HashSet<>();

    public static void main(String[] args) throws InterruptedException {

        BPlusTree bpt = new BPlusTree(new Config("/tmp/PRE/debug.idx",true));

        System.out.println(bpt.Add(1,1));
        System.out.println(bpt.Add(2,2));
        System.out.println(bpt.Add(1,1));
        System.out.println(bpt.Add(2,1));

        Thread.sleep(120000);
        /*
        long key = 116412129235624395L;
        bpt.Add(key,key);
        long q = bpt.Query(key);

        int root = (int) bpt.getIndexFileHandler().getRootIndex();
        int sz = bpt.getIndexFileHandler().getNextId();

        bpt.pno.ShowTree(1);

        System.out.println("root: "+root);
        System.out.println("sz: "+sz);
        System.out.println("key: "+key);
        System.out.println("Q: "+q);

        return ;
        */

        int a=1000,b=2000;

        Random random = new Random(12);
        hi = new HashSet<>();
        List<Long> array = new ArrayList<>();

        int loop = 1000000;
        while(loop-->0) {
            long x = random.nextLong();
            while(hi.contains(x)) {
                x = random.nextLong();
            }
            hi.add(x);
            array.add(x);
        }

        long beg = System.currentTimeMillis();

        System.out.println("---------------------------");

        for(int i=0,sz=array.size();i<sz;i++) {
           // System.out.println("add "+i);
            long x = array.get(i);
            bpt.Add(x,x);
            if(i%(sz/100)==0) {
                System.out.println(i);
            }
        }

        System.out.println("---------------------------"+(System.currentTimeMillis()-beg));

        //array.sort(Comparator.comparingLong(Long::longValue));
        int cnt=0;
        for(int i=0,sz=array.size();i<sz;i++) {
            long x = array.get(i);
            //System.out.println(x+"<-->"+bpt.Query(x));
            if(i%(sz/100)==0) {
                System.out.println(i);
            }
            if(x!=bpt.Query(x)) {
                //System.out.println("FUCK! "+x+"<-->"+bpt.Query(x));
                cnt++;
            }
        }

        System.out.println("---------------------------"+(System.currentTimeMillis()-beg));
        System.out.println("error: "+cnt);

        //System.out.println("root: "+handler.getRootIndex());
       // PageNodeOperation.ShowTree((int) handler.getRootIndex());


        //bpt.Add(12,12);


        //BPlusTree.getIndexFileHandler().force();

        /*
        sz = BPlusTree.getIndexFileHandler().getNextId();
        System.out.println("sz: "+sz);
        System.out.println("root: "+handler.getRootIndex());

        */

/*        long beg = System.currentTimeMillis();
        System.out.println("NOW DEBUG!!!!");

        for(int i=0;i<array.size();i++) {
            long x = array.get(i);
            //System.out.println(x+"<-->"+bpt.Query(x));
            if(x+1!=bpt.Query(x)) {
                System.out.println("FUCK! "+x+"<-->"+bpt.Query(x));
            }
        }

        System.out.println(System.currentTimeMillis()-beg);*/

       // bpt.CloseHandler();

        //PageNodeOperation.ShowTree((int) handler.getRootIndex());

    }
}
