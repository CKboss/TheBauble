package edu.ecnu.ica.index.demo.LIRS;

/**
 * Created by ckboss on 16-7-9.
 */

public class LIRS {

    public static void main(String[] args) {

        IndexLIRSCache.Config config = new IndexLIRSCache.Config();
        IndexLIRSCache lirs = new IndexLIRSCache(config);

        for(int i=1;i<=10000;i++) {
            lirs.put(i%20, "PAGETD");
        }

        String str = (String) lirs.get(12);

        System.out.println(str);
    }
}
