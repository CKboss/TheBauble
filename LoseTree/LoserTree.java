package edu.ecnu.ica.mergesort;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by ckboss on 16-7-30.
 */
public class LoserTree {

    static final long INF = Long.MAX_VALUE;
    static final int BUFFERSIZE = 600100;
    public ArrayList<String> filecollect ;
    public static int C = 6;

    public String outputfilepath;

    int N;
    int[] ls;
    long[][] b;
    int[] bnt;

    TriLong[][] buffer;
    int[] buffernt;

    TriLong[] OutPutBuffer;
    int outputnt;

    BufferedReader[] brs;
    BufferedWriter bw;

    public LoserTree(String outputfilepath,ArrayList<String> collect) {

        this(collect.size());

        this.filecollect = collect;
        this.outputfilepath = outputfilepath;
        brs = new BufferedReader[N+1];
        buffer = new TriLong[N + 1][];
        buffernt = new int[N+1];
        outputnt=0;

        //OutPutBuffer = new ArrayList<>(BUFFERSIZE*C);
        OutPutBuffer = new TriLong[BUFFERSIZE*C+1];

        for(int i=0;i<N;i++) {
            try {

                brs[i] = new BufferedReader(new FileReader(filecollect.get(i)));
                buffer[i]=new TriLong[BUFFERSIZE+1];

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            PowerUp(i);
        }

        try {
            bw = new BufferedWriter(new FileWriter(this.outputfilepath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void PowerUp(int x) {

        //buffer[x].clear();
        buffernt[x]=0;
        bnt[x]=0;
        for(int i=0;i<BUFFERSIZE;i++) {

            try {

                if(brs[x].ready()) {

                    String line = brs[x].readLine();
                    int p1 = line.indexOf("\t");
                    int p2 = line.lastIndexOf("\t");

                    long goodHashID = Long.valueOf(line.substring(0,p1));
                    long fileId = Long.valueOf(line.substring(p1+1,p2));
                    long offset = Long.valueOf(line.substring(p2+1));

                    b[x][bnt[x]++] = goodHashID;
                    //buffer[x].add(new LongAndString(goodHashID,line));

                    buffer[x][buffernt[x]]=new TriLong(new long[]{goodHashID,fileId,offset});
                    buffernt[x]++;

                } else {

                    b[x][bnt[x]++] = INF;
                    //buffer[x].add(null);
                    buffer[x][buffernt[x]]=null; buffernt[x]++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        bnt[x]=0;
    }

    LoserTree(int n) {

        N = n;
        ls = new int[N*2];
        b = new long[N+1][];
        for(int i=0;i<N+1;i++) {
            b[i] = new long[BUFFERSIZE];
        }
        bnt = new int[N+1];
    }

    long getBhead(int x) {
        if(bnt[x]>=BUFFERSIZE) {
            PowerUp(x);
        }
        return b[x][bnt[x]];
    }

    void rmBhead(int x) {
        bnt[x]++;
        if(bnt[x]>=BUFFERSIZE) {
            PowerUp(x);
        }
    }

    void Adjust(int s) {

        int t = (s+N)/2;

        while(t>0) {

            if(getBhead(s)>getBhead(ls[t])) {

                int tmp = s;
                s = ls[t]; // new winer
                ls[t] = tmp;

            }

            t = t/2;
        }

        ls[0] = s;
    }

    public void CreatLoseTree() {

        b[N][0] = -INF; bnt[N] = 0;
        for(int i=0;i<N;i++) {
            ls[i]=N;
        }
        for(int i=N-1;i>=0;i--) {
            Adjust(i);
        }
    }

    void Display() {

        for(int i=0;i<N;i++) {
            System.out.printf("%d(%d) ",ls[i],b[ls[i]][0]);
        }
    }

    public void MergeSort() {

        int cnt=0;
        while(true) {
            long smallest = getBhead(ls[0]);
            if(smallest==INF) {
                break;
            }

            //String line = buffer[ls[0]].get(bnt[ls[0]]).getString();
            //String line = buffer[ls[0]][bnt[ls[0]]].getString();

            /*cnt++;
            if(cnt%100000==0) {
                System.out.println(" in sort "+cnt);
            }*/

            // OutPut
            writeToBuffer(buffer[ls[0]][bnt[ls[0]]]);

            rmBhead(ls[0]);
            Adjust(ls[0]);
        }

        WriteToDisk();
    }

    void writeToBuffer(TriLong triLong) {

        //OutPutBuffer.add(line);

//        if(OutPutBuffer.size()%(BUFFERSIZE*C)==0) {
//            WriteToDisk();
//        }
        OutPutBuffer[outputnt++]=triLong;
        if(outputnt%(BUFFERSIZE*C)==0) {
            WriteToDisk();
        }
    }

    void WriteToDisk() {

//        for(String line : OutPutBuffer) {
//            try {
//                bw.write(line+"\n");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        OutPutBuffer.clear();

        for(int i=0;i<outputnt;i++) {
            TriLong triLong = OutPutBuffer[i];
            try {
                bw.write(triLong.toString()+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        outputnt=0;
    }

    public void Close() {
        try {
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

//        LoserTree.filecollect.add("/tmp/PRE/order.sort1_temp_00.txt");
//        LoserTree.filecollect.add("/tmp/PRE/order.sort1_temp_01.txt");
//        LoserTree.filecollect.add("/tmp/PRE/order.sort1_temp_02.txt");
//        LoserTree.filecollect.add("/tmp/PRE/order.sort1_temp_03.txt");
//        LoserTree.filecollect.add("/tmp/PRE/order.sort1_temp_04.txt");
//        LoserTree.filecollect.add("/tmp/PRE/order.sort1_temp_05.txt");
//        LoserTree.filecollect.add("/tmp/PRE/order.sort1_temp_06.txt");

        File files = new File("/run/media/root/Data/store/PRE/good");

        ArrayList<String> filecollect = new ArrayList<>();

        for(File file : files.listFiles()) {
            filecollect.add(file.getAbsolutePath());
        }

        LoserTree lt = new LoserTree("/run/media/root/Data/store/PRE/O1.txt",filecollect);
        lt.CreatLoseTree();
        lt.MergeSort();
        lt.Close();

//        LoserTree lt = new LoserTree(5);
//
//        lt.b[0][0]=9;
//        lt.b[0][1]=19;
//        lt.b[0][2]=9999;
//        lt.b[0][3]=INF;
//
//        lt.b[1][0]=9;
//        lt.b[1][1]=INF;
//
//        lt.b[2][0]=9;
//        lt.b[2][1]=INF;
//
//        lt.b[3][0]=9;
//        lt.b[3][1]=INF;
//
//        lt.b[4][0]=0;
//        lt.b[4][1]=INF;
//
//        lt.CreatLoseTree();
//        lt.Display();
//        lt.MergeSort();
    }
}
