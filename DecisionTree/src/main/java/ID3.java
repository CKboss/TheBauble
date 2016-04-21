import Entity.Car;
import javafx.util.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.*;

/**
 * Created by ckboss on 16-4-20.
 */
public class ID3 {

    final double eps = 1e-8;
    Graph graph;
    static int nextNode;

    static double Log2(double x) {
        return Math.log(x)/Math.log(2);
    }

    static double Log2(double x,double base) {
        return Math.log(x)/Math.log(base);
    }

    <K,V> void showMap(Map<K,V> map) {

        List<Map.Entry> list = new LinkedList<>(map.entrySet());
        list.stream().forEach( e -> {
            System.out.println(e.getKey()+" ---> "+e.getValue());
        });

    }

    <T> void BuildTree(Class T, ArrayList<T> array) {
        Set<Object> st = new HashSet<>();
        st.add("id"); st.add("kind");
        BuildTree(0,0,T,array,st,0);
    }

    <T> void BuildTree(int fa,int u,Class T, ArrayList<T> array,final Set<Object> usedfield,int deep) {
        Field[] fields = T.getDeclaredFields();

        // 信息商 H(D)

        Map<Object,Integer> moi = new HashMap<>();

        int n = array.size();
        for(T t : array) {
            //System.out.println(t);
            try {

                Field field = T.getDeclaredField("kind");
                Object kind = field.get(t);
                moi.put(kind,moi.getOrDefault(kind,0)+1);

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        /*
        System.out.println("moi:");
        showMap(moi);
        */

        double HD = 0.0;
        for(Object obj : moi.keySet()) {
            double p = (double)moi.get(obj)/n;
            HD -= p*Log2(p);
        }

        //System.out.println("HD: "+HD);

        // 所有可能的分类
        Set<Object> classes = new HashSet<>();

        //
        String MostPopluarField = null;
        double BEST = 1e9;
        for(Field field : fields) {

            String name = field.getName();
            if(usedfield.contains(name)) {
                continue;
            }

            // 检查关于field的信息增益
            Map<Object,Integer> DI = new HashMap<>();
            Map<Pair<Object,Object>,Integer> NDI = new HashMap<>();

            for(T t : array) {
                try {

                    Object obj = field.get(t);
                    DI.put(obj,DI.getOrDefault(obj,0)+1);
                    Object kind = T.getDeclaredField("kind").get(t);
                    classes.add(kind);
                    Pair pair = new Pair(obj,kind);
                    NDI.put(pair,NDI.getOrDefault(pair,0)+1);

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }

            /*
            System.out.println("------->   "+field.getName());
            System.out.println("DI: ");
            showMap(DI);
            System.out.println("NDI: ");
            showMap(NDI);
            */

            /// 计算 field 条件信息熵
            double HDA = 0;
            for(Object key : DI.keySet()) {
                int di = DI.get(key);

                double xi = (double)di/n;

                // 计算 hdi 的信息熵
                double hda = 0;
                for(Object cls : classes) {

                    Pair pair = new Pair(key,cls);
                    int nik = NDI.getOrDefault(pair,0);

                    double pi = (double)(nik)/di;
                    if(pi >= eps) {
                        hda -= xi * pi * Log2(pi);
                    }
                }

                HDA+=hda;
            }

            //System.out.println(field.getName()+" 的条件信息熵: "+HDA);
            if(HDA<BEST) {
                MostPopluarField = field.getName();
                BEST = HDA;
            }
        }

        /*
        System.out.println("MostPopluarField: "+MostPopluarField);
        System.out.println("Gain: "+(HD-BEST));
        */

        double gain= HD - BEST;

        if(gain<eps) {
            // 信息增益很小结束这个节点,选择多数节点的结果
            Object kind = null;
            int mx = 0;

            for(Object obj : moi.keySet()) {
                int number = moi.get(obj);
                if(number>mx) {
                    mx = number;
                    kind = obj;
                }
            }

            graph.classification.put(u,kind);

            return ;
        }

        // 按照fields进行分类建立第二层树

        Map<Object,ArrayList<T>> moa = new HashMap<>();

        try {

            Field field = T.getDeclaredField(MostPopluarField);
            for(T t : array) {
                Object obj = field.get(t);
                ArrayList<T> temparray = moa.getOrDefault(obj,new ArrayList<T>());
                temparray.add(t);
                moa.put(obj,temparray);
            }

            for(Object obj : moa.keySet()) {

                usedfield.add(MostPopluarField);
                //System.out.println("add Edge "+deep+"  "+MostPopluarField+" obj: "+obj);
                int v = nextNode++;
                Pair pair = new Pair(MostPopluarField,obj);
                graph.Add(u,v,pair);
                BuildTree(u,v,T,moa.get(obj),usedfield,deep+1);
                usedfield.remove(MostPopluarField);

            }

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    void showDFS(int u) {

        if(graph.classification.containsKey(u)) {
            System.out.println(u+": "+graph.classification.get(u));
            return;
        }

        for(int i = graph.Adj[u];i!=-1;i=graph.edge[i].next) {

            int v = graph.edge[i].to;
            Pair msg = graph.edge[i].edgepair;
            System.out.println(u+" -----> "+v+" pairInfo: "+msg);

        }

        for(int i = graph.Adj[u];i!=-1;i=graph.edge[i].next) {
            int v = graph.edge[i].to;
            showDFS(v);
        }
    }

    <T> int checkItem(T t) {

        int ret = walkTree(t,0);

        return ret;
    }

    <T> int walkTree(T t,int u) {


        if(graph.classification.containsKey(u)) {
            System.out.println(u+": "+graph.classification.get(u));
            return (int) graph.classification.get(u);
        }


        for(int i = graph.Adj[u];i!=-1;i=graph.edge[i].next) {

            int v = graph.edge[i].to;
            Pair pair = graph.edge[i].edgepair;

            String fieldname = (String) pair.getKey();

            try {

                Field field = t.getClass().getDeclaredField(fieldname);

                int tv = (Integer)field.get(t);
                int value = Integer.valueOf(String.valueOf(pair.getValue()));

                if(tv==value) {
                    return walkTree(t,v);
                }


            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return 1;
    }

    void ShowTree() {
        System.out.println(" show tree ");
        showDFS(0);
    }

    ID3() {
        graph = new Graph(10000,1000000);
        nextNode = 1;
    }

    static void PeopleTree() {

        ReadCSV readCSV = new ReadCSV("/home/ckboss/Documents/MachineLearning/数据集/Toydataset/t1.csv");
        ArrayList<People> peoples = readCSV.Read(People.class);

        ID3 id3 = new ID3();
        id3.BuildTree(People.class,peoples);
        id3.ShowTree();
    }

    static void CarTree() {

        ReadCSV readCSV = new ReadCSV("/home/ckboss/Documents/MachineLearning/数据集/car/car_train.csv");
        ArrayList<Car> cars = readCSV.Read(Car.class);

        ID3 id3 = new ID3();
        id3.BuildTree(Car.class,cars);
        id3.ShowTree();

        readCSV = new ReadCSV("/home/ckboss/Documents/MachineLearning/数据集/car/car_test.csv");
        ArrayList<Car> textcar = readCSV.Read(Car.class);

        int correct=0;
        for(Car car : textcar) {
            int ret = id3.walkTree(car, 0);
            if(ret==car.kind) {
                correct++;
            }
        }

        System.out.println("correct: "+correct);
        System.out.println((double)correct/textcar.size());
    }

    public static void main(String[] args) {

        //PeopleTree();
        CarTree();
    }
}
