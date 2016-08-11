package edu.ecnu.ica.index.demo.BPlusTree;


import edu.ecnu.ica.util.Pair;

import java.util.*;

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

    void Add(long key,long value) {
        root.Insert(-1,key,value);
    }

    long Query(long key) {
        return root.Query(key);
    }

    void Debug() {
        System.out.println(".........................................");
        System.out.println("in debug ...... node num: "+VP.size());
        System.out.println("current root is : "+root.ID);
        root.ShowTree();
        System.out.println(".........................................");
    }

    public static void main(String[] args) {
        BPlusTree bpt = new BPlusTree();


        Random random = new Random(12);
        HashSet<Long> hi = new HashSet<>();
        List<Long> array = new ArrayList<>();

        int loop = 10000000;
        while(loop-->0) {
            long x = random.nextLong();
            while(hi.contains(x)) {
                x = random.nextLong();
            }
            hi.add(x);
            array.add(x);
        }

        System.out.println();

        for(int i=0;i<array.size();i++) {
            // System.out.println("add "+i);
            long x = array.get(i);
            bpt.Add(x,x);
        }


        int err=0;
        for(int i=0;i<array.size();i++) {
            long x = array.get(i);
            if(x!=bpt.Query(x)) {
                err++;
                //System.out.println(x+"   "+bpt.Query(x));
            }
        }

        System.out.println("err: "+err);

/*        bpt.VP.get(0).getBrotherArray();
        System.out.println("");

        array.sort(Comparator.comparingLong(Long::longValue));

        for(int i=0;i<array.size();i++) {
            System.out.printf(array.get(i)+",");
        }
        System.out.println("");


        long q = bpt.Query(116412129235624395L);
        System.out.println("Q: "+q);

        long q2 = bpt.Query(80289981171612705L);
        System.out.println("Q: "+q2);*/

        /*
        Random random = new Random(12);

        bpt.Add(2,3);
        bpt.Add(2,4);
        bpt.Add(2,5);
        bpt.Add(2,6);
        bpt.Add(2,7);
        bpt.Add(2,8);
        bpt.Add(2,9);
        bpt.Add(2,1);
        bpt.Add(2,0);

        bpt.Debug();
        */

        /*
        HashSet<Integer> uniquekey = new HashSet<>();
        Vector<Integer> vkey = new Vector<>();
        Vector<Integer> vvalue = new Vector<>();

        int loop=1000000;

        for(int i=0;i<loop;i++) {
            int key = random.nextInt();
            while(uniquekey.contains(key)==true) {
                key = random.nextInt();
            }
            uniquekey.add(key);
            int value = random.nextInt();

            vkey.add(key);
            vvalue.add(value);
            bpt.Add(key,value);

            if(i%1000000==0) {
                System.out.println("now: "+i/1000000);
            }
        }

        System.out.println("after insert");

        for(int i=0;i<loop;i++) {

            int key = vkey.get(i);
            int value = vvalue.get(i);

            int q = bpt.Query(key);
            if(q!=value) {
                System.out.printf("Warning!!! query %d need %d find %d\n",key,value,q);
            }
        }

        */
    }
}
