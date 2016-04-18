import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ckboss on 16-4-18.
 */
public class Evaluation {

    Map<Pair<Integer,String>,Integer> mmi = new HashMap<>();

    void Pre() {
        try {

            BufferedReader br = new BufferedReader(new FileReader("/home/ckboss/Documents/MachineLearning/数据集/搜索数据/11qrels_visit_checksum.txt"));
            while(br.ready()) {
                String line = br.readLine();
                String[] words = line.split(" ");
                int problem = Integer.valueOf(words[0]);
                int result = Integer.valueOf(words[3]);
                String report = words[5];

                Pair<Integer,String> pair = new Pair<>(problem,report);
                mmi.put(pair,result);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int evaluate(int pid,String q) {

        int correct = 0;
        MulitBM25 bm25 = new MulitBM25();
        bm25.Gao(q);
        ArrayList<String> top = bm25.ShowTopResult(10);
        for(int i=0;i<10;i++) {
            Pair<Integer,String> pair = new Pair<>(pid,top.get(i));
            //System.out.println(pair);
            try {
                if(mmi.get(pair)>0) {
                    correct++;
                }
            } catch (Exception e) {

            }
        }

        System.out.println("correct: "+correct);

        return correct;
    }

    public static void main(String[] args) throws IOException {

        Evaluation evaluation = new Evaluation();
        evaluation.Pre();

        BufferedReader br = new BufferedReader(new FileReader("/home/ckboss/Documents/MachineLearning/数据集/搜索数据/0601_2011.msm"));

        int cnt=0;
        while(br.ready()) {
            String line = br.readLine();
            Integer id = Integer.valueOf(line.substring(0,3));
            String q = line.substring(4);
            /*
            System.out.println(id);
            System.out.println(q);
            */
            cnt += evaluation.evaluate(id,q);
        }

        System.out.println("cnt: "+cnt);

        /*
        Pair<Integer,String> pair = new Pair<>(101,"report24469");
        int id = evaluation.mmi.get(pair);
        System.out.println(id);
        */
    }
}
