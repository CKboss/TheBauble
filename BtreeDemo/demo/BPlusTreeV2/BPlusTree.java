package edu.ecnu.ica.index.demo.BPlusTreeV2;

import edu.ecnu.ica.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

/**
 * Created by ckboss on 16-7-9.
 *
 * 不能插入重复的key值,查询会不准确
 */
public class BPlusTree {

    public static Vector<PageNode> VP = new Vector<>();
    public static Vector<Pair<Long,Long>> Data = new Vector<>();
    public static PageNode root;
    public static int id = 0;
    public static int getID() {
        return id++;
    }


    BPlusTree() {
        root = new PageNode(getID(),1,-1);
        VP.add(root);
    }

    boolean Add(KeyPair kp,long value) {
        return root.Insert(-1,kp,value);
    }

    long Query(KeyPair kp) {
        return root.Query(kp);
    }

    ArrayList<Long> GetRange(KeyPair left,KeyPair right) {
        return root.getRange(left,right);
    }

    void Debug() {
        System.out.println(".........................................");
        System.out.println("in debug ...... node num: "+VP.size());
        System.out.println("current root is : "+root.ID);
        root.ShowTree();
        System.out.println(".........................................");
    }

    void test() {

        BPlusTree bpt = new BPlusTree();
        int loop = 10;

        HashSet<KeyPair> hk = new HashSet<>();
        ArrayList<KeyPair> ak = new ArrayList<>();
        ArrayList<Long> ar = new ArrayList<>();
        long beg = System.currentTimeMillis();
        Random random = new Random(24);

        for(int i=0;i<loop;i++) {
            long a = random.nextInt(10);
            long b = random.nextInt(10);
            KeyPair kp = new KeyPair(a,b);
            while(hk.contains(kp)) {
                a = random.nextInt(10);
                b = random.nextInt(10);
                kp = new KeyPair(a,b);
            }
            hk.add(kp); ak.add(kp);

            long value = i;

            ar.add(value);
            bpt.Add(kp,value);

            /*if(i%(loop/100)==0) {
                System.out.println("..........."+(i/(loop/100)));
            }*/
        }

        long mid = System.currentTimeMillis();
        System.out.println("Add: "+(mid-beg+1));

        int err=0;
        for(int i=0;i<loop;i++) {

            KeyPair kp = ak.get(i);
            long value = ar.get(i);

            //System.out.println("ADD: "+kp);

            if(value!=bpt.Query(kp)) {
                System.out.println(kp+" -> "+value+"  "+bpt.Query(kp));
                err++;
            }
        }


        System.out.println("err: "+err);

        long end = System.currentTimeMillis();
        System.out.println("End: "+(end-mid+1));

        bpt.Debug();

        bpt.VP.get(1).getBrotherArray();
    }

    public static void main(String[] args) {

        BPlusTree bpt = new BPlusTree();
        bpt.test();
/*
        boolean b1 = bpt.Add(new KeyPair(1,3),1);
        boolean b2 = bpt.Add(new KeyPair(1,3),2);
        boolean b3 = bpt.Add(new KeyPair(1,3),3);
        boolean b4 = bpt.Add(new KeyPair(-1,3),3);

        System.out.println(b1+","+b2+","+b3+","+b4);

        bpt.Debug();*/

       /* bpt.Add(new KeyPair(1,3),1);
        bpt.Add(new KeyPair(1,6),2);
        bpt.Add(new KeyPair(1,7),3);
        bpt.Add(new KeyPair(1,9),4);
        bpt.Add(new KeyPair(1,11),5);
        bpt.Add(new KeyPair(1,13),6);
        bpt.Add(new KeyPair(2,2),7);
        bpt.Add(new KeyPair(2,5),8);
        bpt.Add(new KeyPair(2,9),9);
        bpt.Add(new KeyPair(2,11),10);
        bpt.Add(new KeyPair(2,12),11);
        bpt.Add(new KeyPair(2,28),12);
        bpt.Add(new KeyPair(3,2),13);
        bpt.Add(new KeyPair(3,5),14);
        bpt.Add(new KeyPair(3,8),15);
        bpt.Add(new KeyPair(3,11),16);

        //bpt.Debug();

        ArrayList<Long> ret = bpt.GetRange(new KeyPair(1,1),new KeyPair(1,2));

        System.out.println("ret: "+ret);
*/
/*
        int x,y;
        Scanner in = new Scanner(System.in);

        while(in.hasNext()) {

            int a,b;
            a = in.nextInt();
            b = in.nextInt();

            KeyPair kp = new KeyPair(a,b);

            long xxx = bpt.Query(kp);

            System.out.println("xxx: "+xxx);
        }*/
    }
}
