package edu.ecnu.ica.index.indisk.BPlusTreeV2;

import edu.ecnu.ica.index.ParametersConfig;
import edu.ecnu.ica.index.demo.BPlusTreeV2.KeyPair;
import edu.ecnu.ica.index.doubleindex.TwoDimensionIndexFileFactory;
import edu.ecnu.ica.index.doubleindex.TwoDimensionIndexFileHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by ckboss on 16-7-15.
 */
public class BPlusTreeV2 {

    public TwoDimensionIndexFileHandler indexFileHandler;
    public PageNodeOperationV2 pnov2;

    public BPlusTreeV2(String indexpath,boolean isclean) {

        if(isclean==true) {
            try {
                TwoDimensionIndexFileFactory.createIndexFile(indexpath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            indexFileHandler = TwoDimensionIndexFileFactory.openIndexFile(indexpath);
            pnov2 = new PageNodeOperationV2(indexFileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(isclean==true) {
            indexFileHandler.setRootIndex(0);
            pnov2.SetPageHeader((int) indexFileHandler.getRootIndex(),1,-1);
        }
    }

    public void Add(KeyPair key,long value) {
        int rootindex = (int) indexFileHandler.getRootIndex();
        pnov2.Insert(rootindex,key,value);
    }

    public long Insert(KeyPair key, long value) {
        int rootindex = (int) indexFileHandler.getRootIndex();
        return pnov2.Insert(rootindex,key,value);
    }

    public long Query(KeyPair x) {
        int rootindex = (int) indexFileHandler.getRootIndex();
        return pnov2.Query(rootindex,x);
    }

    public ArrayList<Long> QueryRange(KeyPair k1, KeyPair k2) {
        int rootindex = (int) indexFileHandler.getRootIndex();
        return pnov2.getRange(rootindex,k1,k2);
    }

    public void DEBUG() {
        int rootindex = (int) indexFileHandler.getRootIndex();
        System.out.println(">The tree size is: "+indexFileHandler.getNextId());
        System.out.println(">now the root index is: "+rootindex);
        pnov2.ShowTree(rootindex);
    }

    public void CloseHandler() {
        indexFileHandler.close();
    }

    public void test_insert_speed() {

        BPlusTreeV2 bpt = new BPlusTreeV2("/tmp/PRE/test.idx2",true);
        Random random = new Random();

        long beg = System.currentTimeMillis();
        int loop = 18750000;
        for(int i=0;i<loop;i++) {
            long a = random.nextLong();
            long b = random.nextLong();
            KeyPair kp = new KeyPair(a, b);
            long value = random.nextLong();

            bpt.Insert(kp,value);

            if(i%(loop/100)==0) {
                System.out.println("..........."+(i/(loop/100)));
            }
        }

        long end= System.currentTimeMillis();
        System.out.println(end-beg+1);
    }

    public void test() {

        BPlusTreeV2 bpt = new BPlusTreeV2("/tmp/PRE/test.idx2",true);
        Random random = new Random();

        int loop = 30000000;

        HashSet<KeyPair> hk = new HashSet<>();
        ArrayList<KeyPair> ak = new ArrayList<>();
        ArrayList<Long> ar = new ArrayList<>();


        for(int i=0;i<loop;i++) {
            long a = random.nextLong();
            long b = random.nextLong();
            KeyPair kp = new KeyPair(a, b);
            while (hk.contains(kp)) {
                a = random.nextLong();
                b = random.nextLong();
                kp = new KeyPair(a, b);
            }
            hk.add(kp);
            ak.add(kp);

            long value = random.nextLong();
            ar.add(value);
        }

        System.out.println("BEGIN!!!");
        long beg = System.currentTimeMillis();

        for(int i=0;i<loop;i++) {

            KeyPair kp = ak.get(i);
            long value = ar.get(i);

            bpt.Insert(kp,value);

            if(i%(loop/100)==0) {
                System.out.printf("running: %d %%\n",(i/(loop/100)));

            }
        }

        long mid = System.currentTimeMillis();
        System.out.println("Add: "+(mid-beg+1));

        int err=0;
        for(int i=0;i<loop;i++) {
            KeyPair kp = ak.get(i);
            long value = ar.get(i);
            if(value!=bpt.Query(kp)) {
                System.out.println(value+"  "+bpt.Query(kp));
                err++;
            }
        }

        System.out.println("err: "+err);

        long end = System.currentTimeMillis();
        System.out.println("End: "+(end-mid+1));

    }

    public static void main(String[] args) {

        BPlusTreeV2 bpt = new BPlusTreeV2("/tmp/PRE/test.idx2",true);

        bpt.test();

//        bpt.Add(new KeyPair(5,0),0);
//        bpt.Add(new KeyPair(6,4),1);
//        bpt.Add(new KeyPair(0,7),2);
//        bpt.Add(new KeyPair(2,0),3);
//        bpt.Add(new KeyPair(4,1),4);
//        bpt.Add(new KeyPair(8,4),5);
//        bpt.Add(new KeyPair(4,8),6);
//        bpt.Add(new KeyPair(4,2),7);
//        bpt.Add(new KeyPair(7,9),8);
//        bpt.Add(new KeyPair(8,3),9);
//
//        bpt.DEBUG();
//
//        ArrayList<Long> ret = bpt.QueryRange(new KeyPair(4,3),new KeyPair(8,1));
//
//        System.out.println(ret);
//
//        System.out.println(bpt.Insert(new KeyPair(7,9),999));
//        System.out.println(bpt.Insert(new KeyPair(6,4),999));
//        System.out.println(bpt.Insert(new KeyPair(8,3),999));
//        System.out.println(bpt.Insert(new KeyPair(5,0),999));
    }
}
