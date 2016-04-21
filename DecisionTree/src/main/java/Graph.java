import javafx.util.Pair;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by ckboss on 16-4-20.
 */
public class Graph {

    class Edge {
        int to;
        int next;
        Pair<Object,Object> edgepair;
    }

    int Size;
    int[] Adj;
    Edge[] edge;
    Map<Integer,Object> classification = new HashMap<>();

    Graph(int n,int m) {

        Size=0;
        edge = new Edge[1000+m];
        Adj = new int[100+n];
        Arrays.fill(Adj,-1);
    }

    void Add(int u,int v,Pair pair) {

        if(edge[Size]==null) {
            edge[Size] = new Edge();
        }
        edge[Size].to=v;
        edge[Size].edgepair=pair;
        edge[Size].next=Adj[u];
        Adj[u]=Size++;
    }

    void Show(int u) {
        for(int i = Adj[u];i!=-1;i=edge[i].next) {
            System.out.println(u+" ----> "+edge[i].to);
        }
    }

    public static void main(String[] args) {
    }
}
