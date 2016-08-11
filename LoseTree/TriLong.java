package edu.ecnu.ica.mergesort;

import java.util.Comparator;

/**
 * Created by root on 7/31/16.
 */
public class TriLong implements Comparable<TriLong>{

    // 0 -> hashid
    // 1 -> fileID
    // 2 -> offset int file

    public long[] t;

    public TriLong(long[] a) {
        t = new long[3];
        t[0]=a[0]; t[1]=a[1]; t[2]=a[2];
    }

    public TriLong() {
        t = new long[3];
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("");
        sb.append(t[0]); sb.append("\t");
        sb.append(t[1]); sb.append("\t");
        sb.append(t[2]);
        return sb.toString();
    }

    @Override
    public int compareTo(TriLong o) {
        return Long.compare(this.t[0],o.t[0]);
    }
}
