package edu.ecnu.ica.index.demo.BPlusTreeV2;

/**
 * Created by ckboss on 16-7-14.
 */
public class KeyPair implements Comparable<KeyPair>{

    long first;
    long second;

    public KeyPair(long x, long y) {
        first = x;
        second = y;
    }

    KeyPair() {
        first = 0;
        second = 0;
    }

    public long getFirst() {
        return first;
    }

    public void setFirst(long first) {
        this.first = first;
    }

    public long getSecond() {
        return second;
    }

    public void setSecond(long second) {
        this.second = second;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyPair)) return false;

        KeyPair keyPair = (KeyPair) o;

        if (getFirst() != keyPair.getFirst()) return false;
        return getSecond() == keyPair.getSecond();

    }

    @Override
    public int hashCode() {
        int result = (int) (getFirst() ^ (getFirst() >>> 32));
        result = 31 * result + (int) (getSecond() ^ (getSecond() >>> 32));
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("K.P.(");
        sb.append(first);
        sb.append(", ").append(second);
        sb.append(')');
        return sb.toString();
    }

    @Override
    public int compareTo(KeyPair o) {
        if(this.first!=o.first) {
            if(this.first<o.first) return -1;
            else return 1;
        } else if(this.second!=o.second) {
            if(this.second<o.second) return -1;
            return 1;
        }
        return 0;
    }
}
