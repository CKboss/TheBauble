package edu.ecnu.ica.mergesort;

import com.alibaba.middleware.race.model.OrderPreHandler;
import edu.ecnu.ica.util.HashString;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by ckboss on 16-7-29.
 */
public class PreReadFile {

    final int N = 52;
    final int M = 5000000;

    ArrayList<TriLong> gooddatas;
    ArrayList<TriLong> buyerdatas;

    List<String> fileCollect;

    String inputfile;
    String good_outputfile;
    String buyer_outputfile;

    int nth;
    int cnt;

    int fileId;
    int diskChoose;

    static Semaphore semaphore = new Semaphore(5);

    public PreReadFile(String inputfilepath,String goodoutfilepath,String buyeroutputfile, List<String> collect, int diskChoose) {

        int fId = FileIdMap.GetFileID(inputfilepath);
        inputfile = inputfilepath;
        good_outputfile = goodoutfilepath;
        buyer_outputfile = buyeroutputfile;
        gooddatas = new ArrayList<>();
        buyerdatas = new ArrayList<>();
        this.fileCollect = collect;
        this.fileId = fId;
        cnt = 0; nth = 0;

        this.diskChoose = diskChoose;
    }

    public PreReadFile(String inputfilepath,String goodoutfilepath,String buyeroutputfile,int fileId,ArrayList<String> collect, int diskChoose) {

        inputfile = inputfilepath;
        good_outputfile = goodoutfilepath;
        buyer_outputfile = buyeroutputfile;
        gooddatas = new ArrayList<>();
        buyerdatas = new ArrayList<>();
        this.fileCollect = collect;
        this.fileId = fileId;

        cnt = 0; nth = 0;
        this.diskChoose = diskChoose;
    }

    public static Semaphore isOK = new Semaphore(0);

    class SortThread implements Runnable {

        int Nth;
        ArrayList<TriLong> Datas;
        String outputfile;

        SortThread(int nth,String outputfile,ArrayList<TriLong> datas) {
            Nth=nth;
            Datas = datas;
            this.outputfile = outputfile;
        }

        @Override
        public void run() {
            // sort file
            System.out.printf("排序文件 %d\n",Nth);

            BufferedWriter bw = null;
            try {

                String tempfilepath = String.format("%s_temp_%02d.txt",outputfile,Nth);
                bw = new BufferedWriter(new FileWriter(tempfilepath));
                fileCollect.add(tempfilepath);
                // write file
                Collections.sort(Datas);
                for(TriLong data : Datas) {
                    bw.write(data.toString()+"\n");
                }
                bw.flush();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    isOK.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void SortIt() {
        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));

            int linenum = 0;
            long beg = System.currentTimeMillis();

            String line = null;
            String goodID = null;
            String buyerID = null;
            long orderId = -1;

            long curentoffset = 0;

            while((line = br.readLine())!=null) {
                int p1 = line.indexOf("orderid:");
                int p2 = line.indexOf("\t", p1);
                if(p2  != -1) {
                    orderId = Long.valueOf(line.substring(p1 + 8, p2));
                } else {
                    orderId = Long.valueOf(line.substring(p1 + 8));
                }

                long value = OrderPreHandler.LongPack(curentoffset, this.fileId);

                // change line to LongAndString ;
                //get goodID
                p1 = line.indexOf("goodid:");
                p2 = line.indexOf("\t",p1);


                if(p2!=-1) {
                    goodID = line.substring(p1+7,p2);
                } else {
                    goodID = line.substring(p1+7);
                }

                //datas.add(new LongAndString(HashString.HashIt(goodID),line));
                //datas.add(new LongAndString(HashString.HashIt(goodID),"-"));

                gooddatas.add(new TriLong(new long[]{HashString.HashIt(goodID),value,orderId}));


                p1 = line.indexOf("buyerid:");
                p2 = line.indexOf("\t",p1);


                if(p2!=-1) {
                    buyerID = line.substring(p1+8,p2);
                } else {
                    buyerID = line.substring(p1+8);
                }

                buyerdatas.add(new TriLong(new long[]{HashString.HashIt(buyerID),value,orderId}));

                curentoffset += line.getBytes().length + 1;

                cnt++; linenum++;

                if(cnt%M==0) {

                    try {
                        semaphore.acquire();
                        Thread t = new Thread(new SortThread(nth,good_outputfile,gooddatas));
                        t.start();
                        isOK.release();

                        Thread t2 = new Thread(new SortThread(nth,buyer_outputfile,buyerdatas));
                        t2.start();
                        isOK.release();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        semaphore.release();
                    }

                    // init next round
                    cnt=0; nth++;
                    gooddatas = new ArrayList<>();
                    buyerdatas = new ArrayList<>();
                }
            }

            if(cnt!=0) {

                Thread t = new Thread(new SortThread(nth,good_outputfile,gooddatas));
                t.start();
                isOK.release();

                Thread t2 = new Thread(new SortThread(nth,buyer_outputfile,buyerdatas));
                t2.start();
                isOK.release();
            }

            System.out.println("partOne time: "+(System.currentTimeMillis()-beg));
            System.out.println(inputfile+" linenum: "+linenum);

            while(isOK.availablePermits() != 0) {
                Thread.sleep(1000);
            }
            System.out.println("文件" + this.inputfile + " sort 过程退出! 耗时: " + (System.currentTimeMillis() - beg));
            System.out.println("====================================================================");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class Pack {
        long offset;
        long orderId;
        int diskChoose;

        public Pack(long offset, long orderId, int diskChoose) {
            this.offset = offset;
            this.orderId = orderId;
            this.diskChoose = diskChoose;
        }

        public int getDiskChoose() {
            return diskChoose;
        }

        public long getOrderId() {
            return orderId;
        }

        public void setOffset(long offset) {

            this.offset = offset;
        }

        public void setOrderId(long orderId) {
            this.orderId = orderId;
        }

        public long getOffset() {

            return offset;
        }
    }

    public static void main(String[] args) {

        /*PreReadFile prf = new PreReadFile("/run/media/root/Data/disk1/orders/order.0.0",
                "/run/media/root/Data/store/PRE/good/good_order.0.0","/run/media/root/Data/store/PRE/buyer/buyer_order.0.0",
                OrderPreHandler.firstFileCollect);

        prf.SortIt();*/

    }
}
