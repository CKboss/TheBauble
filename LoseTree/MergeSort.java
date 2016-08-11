package edu.ecnu.ica.mergesort;

import com.alibaba.middleware.race.model.OrderPreHandler;

import java.io.File;

/**
 * Created by root on 7/30/16.
 */
public class MergeSort {

    MergeSort() {

    }

    public static void main(String[] args) {


/*//        PreReadFile prf = new PreReadFile("/run/media/root/Data/disk1/orders/order4g","/run/media/root/Data/store/PRE/sort1");
//        prf.SortIt();

       // String[] paths = {"/media/DATA/disk1/orders/","/run/media/root/Data/disk1/orders/","/media/DATA2/disk3/orders/"};
        String[] paths = {"/run/media/root/Data/disk1/orders/"};


        int cnt = 0;
        for(String path : paths) {
            File files = new File(path);

            for (File file : files.listFiles()) {
                System.out.println(file.getAbsolutePath());
                cnt++;
                String outputpath = String.format("/run/media/root/Data/store/PRE/sort%d", cnt);
//                PreReadFile prf = new PreReadFile(file.getAbsolutePath(), outputpath, OrderPreHandler.firstFileCollect);
//                prf.SortIt();
            }
        }


        System.out.println(OrderPreHandler.firstFileCollect.size());

        long beg = System.currentTimeMillis();
        LoserTree lt = new LoserTree("/run/media/root/Data/store/PRE/O1.txt",OrderPreHandler.firstFileCollect);
        lt.CreatLoseTree();
        lt.MergeSort();
        lt.Close();

        System.out.println("sort time: "+(System.currentTimeMillis()-beg));*/
    }
}
