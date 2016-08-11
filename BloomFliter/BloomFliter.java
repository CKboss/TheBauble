package edu.ecnu.ica.util.BloomFliter;

import java.util.BitSet;
import java.util.Scanner;

/**
 * Created by ckboss on 16-7-28.
 */
public class BloomFliter {

    // N = 1.5e8
    // error 0.001
    public final int M = 2130000000;
    public final int K = 10;
    RepeatedMurmurHash murmurHash;
    BitSet bitSet;

    BloomFliter() {
        murmurHash = new RepeatedMurmurHash(K,M);
        bitSet = new BitSet(M);
    }

    int[] LongtoIntarray(long x) {

        int len = (int)(Math.log10(x))+1;
        int[] ret = new int[len];
        int cnt=0;

        while(x>0) {
            long p = x%10L;
            ret[cnt++]=(int)p;
            x/=10L;
        }

        return ret;
    }

    void add(long x) {

        int[] data = LongtoIntarray(x);
        int[] bits = murmurHash.hash(data);

        for(int i=0,len=bits.length;i<len;i++) {
            bitSet.set(bits[i]);
        }
    }

    boolean contains(long x) {

        int[] data = LongtoIntarray(x);
        int[] bits = murmurHash.hash(data);

        boolean ret = true;
        for(int i=0,len=bits.length;i<len&&ret;i++) {
            ret = bitSet.get(bits[i]);
            if(ret==false) break;
        }

        return ret;
    }

    public static void main(String[] args) {

        BloomFliter bf = new BloomFliter();

        for(int i=1;i<1e8;i++) {
            bf.add(i);
            if(i%1000000==0) {
                System.out.println("---> "+i);
            }
        }

    }
}