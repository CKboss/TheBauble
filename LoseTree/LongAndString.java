package edu.ecnu.ica.mergesort;

/**
 * Created by ckboss on 16-7-29.
 */
public class LongAndString implements Comparable<LongAndString> {

    public long hashgoodid;
    public String string;

    public LongAndString(long hashgoodid, String string) {
        this.hashgoodid = hashgoodid;
        this.string = string;
    }

    public long getHashgoodid() {
        return hashgoodid;
    }

    public void setHashgoodid(long hashgoodid) {
        this.hashgoodid = hashgoodid;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LongAndString)) return false;

        LongAndString that = (LongAndString) o;

        if (getHashgoodid() != that.getHashgoodid()) return false;
        return getString() != null ? getString().equals(that.getString()) : that.getString() == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (getHashgoodid() ^ (getHashgoodid() >>> 32));
        result = 31 * result + (getString() != null ? getString().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("LongAndString{");
        sb.append("hashgoodid=").append(hashgoodid);
        sb.append(", string='").append(string).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(LongAndString o) {
        return Long.compare(this.hashgoodid,o.hashgoodid);
    }
}
