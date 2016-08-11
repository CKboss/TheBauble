package edu.ecnu.ica.index.demo.BPlusTree;


import edu.ecnu.ica.index.store.bean.IndexPage;

/**
 * Created by ckboss on 16-7-9.
 */
public class PageNode {

    private int D = 3;
    private int LIMIT = D/2+1;

    final int INF = 0x3f3f3f3f;

    // 0 other 1 leav
    int TYPE;
    int father;
    int brother;
    int ID;

    // sep and point
    long[] d = null;
    long[] p = null;

    int keynum;

    PageNode(int id,int type,int fa) {

        d = new long[D+2];
        p = new long[D+2];

        TYPE = type;
        father = fa;
        ID = id;
        keynum=0;
        brother=-1;
    }

       /**
     * 二分查找最靠近左边的又大于等于x的pair的位置
     * 如果x是最大的,则返回INF
     * 如果x是最小的,则返回-INF
     * @param x
     * @return
     */
    int binSearchID(long x) {
        int ret = -INF;
        int low=0,high=this.keynum-1;

        if(x>=d[high]) {
            return INF;
        } else if(x<d[low]){
            return -INF;
        }

        while(low<=high) {
            int mid = (low+high) / 2;
            if(d[mid]>=x) {
                ret = mid;
                high = mid-1;
            } else {
                low = mid+1;
            }
        }
        return ret;
    }

    int binSearch(int x) {
        int low=0, high=keynum-1;
        int mid,ret = high;

        while(low<=high){

            mid = (low+high)/2;

            System.out.printf("%d vs %d\n",d[mid],x);
            System.out.printf("%d <--- %d ----> %d\n",low,mid,high);

            if(d[mid]<=x) {
                low = mid + 1;
                ret = mid;
            } else {
                high = mid-1;
            }

        }

        return ret;
    }

    int SortedAddPair(long key,long value) {

        if(keynum==0) {
            d[0]=key;
            p[0]=value;
            keynum++;
            return keynum;
        }

        boolean write = false;
        for(int i=keynum-1;i>=0&&write==false;i--) {


            if(d[i]>key) {
                d[i+1]=d[i];
                p[i+1]=p[i];
            } else {
                d[i+1]=key;
                p[i+1]=value;
                write=true;
            }
        }

        if(!write) {
            d[0]=key;
            p[0]=value;
        }

        keynum++;

        return keynum;
    }

    // from top to bottom insert a key-value to the left
    boolean Insert(int fa,long key,long value) {

        if(TYPE==1) {
            // Only insert into Leave Node
            if (keynum < D) {
                // ok can be insert into current node
                SortedAddPair(key,value);
            } else if(keynum == D){
                // split PageNodeOperation
                // left node [0,LIMIT]
                // right node [LIMIT+1,D]

                InsertOrSplit(key,value);
            }
        } else {
            // find a children

            boolean isminist = true;

            int postion = this.binSearchID(key);
            if(postion==INF) postion = keynum-1;
            else if(postion==-INF) postion = 0;

            for(int i=keynum-1;i>=0;i--) {
                if(key>=d[i]) {

                    isminist = false;
                    PageNode childnode = BPlusTree.VP.get((int) p[i]);
                    return childnode.Insert(ID,key,value);
                }
            }

            if(isminist==true) {
                // 更新一下d[0]的值 , 然后往最小的节点走
                this.d[0]=key;
                PageNode childnode = BPlusTree.VP.get((int) p[0]);
                return childnode.Insert(ID,key,value);
            }

        }

        return true;
    }

    // split node from bottom to top
    void InsertOrSplit(long key,long value) {
        if(keynum+1<=D) {
            // ok just insert into this node
            SortedAddPair(key,value);

        } else {
            // no enough free space , go up

            // split
            PageNode newchildren = new PageNode(BPlusTree.getID(),this.TYPE,this.father);

            BPlusTree.VP.add(newchildren);

            this.SortedAddPair(key,value);

            // left node [0,LIMIT]
            // right node [LIMIT+1,D+1]
            for(int i=LIMIT;i<keynum;i++) {
                newchildren.SortedAddPair(this.d[i],this.p[i]);
                if(newchildren.TYPE==0) {
                    // change the father of children node
                    BPlusTree.VP.get((int) p[i]).father = newchildren.ID;
                }
            }
            this.keynum = LIMIT;

            if(this.TYPE==1) {
                newchildren.brother = this.brother;
                this.brother = newchildren.ID;
            }

            if(this.father!=-1) {
                PageNode fatherNode = BPlusTree.VP.get(this.father);
                fatherNode.InsertOrSplit(newchildren.d[0],newchildren.ID);

            } else {
                // make a newbaba
                PageNode newbaba = new PageNode(BPlusTree.getID(),0,-1);
                BPlusTree.VP.add(newbaba);
                BPlusTree.root=newbaba;

                this.father = newbaba.ID;
                newchildren.father = newbaba.ID;

                newbaba.SortedAddPair(this.d[0],this.ID);
                newbaba.SortedAddPair(newchildren.d[0],newchildren.ID);
            }
        }
    }

    long Query(long x) {

        if(this.TYPE==1) {
            //找到叶子节点
            for(int i=0;i<this.keynum;i++) {
                if(d[i]==x) {
                    return p[i];
                }
            }
        } else {

            int postion = this.binSearchID(x);
            if (postion == INF) postion = this.keynum - 1;
            else if(postion==-INF) postion = 0;

            for (int i = postion; i >= 0; i--) {
                if (x >= d[i]) {
                    PageNode children = BPlusTree.VP.get((int) p[i]);
                    return children.Query(x);
                }
            }
        }
        return -1;
    }

    public void getBrotherArray() {

        if(TYPE==1) {
            for (int i = 0, sz = keynum; i < sz; i++) {
                System.out.printf(d[i] + ",");
            }

            if (brother != -1) {
                PageNode b = BPlusTree.VP.get((int)brother);
                b.getBrotherArray();
            }
        }
    }

    @Override
    public String toString() {

        String str_d = "[";
        String str_p = "[";

        for(int i=0;i<this.keynum;i++) {
            str_d += d[i];
            str_p += p[i];
            if(i!=this.keynum-1) {
                str_d += ", ";
                str_p += ", ";
            }
        }

        str_d+="]";
        str_p+="]";

        return "PageNodeOperation{" +
                " ID=" + ID+
                ", D=" + D +
                ", LIMIT=" + LIMIT +
                ", TYPE=" + TYPE +
                ", father=" + father +
                ", keynum=" + keynum +
                ", brother=" + brother +
                "\n d=" + str_d +
                "\n p=" + str_p +
                " }";
    }

    void ShowTree() {
        if(this.TYPE==0) {
            System.out.println(this);
            for (int i = 0; i < this.keynum; i++) {
                PageNode pageNode = BPlusTree.VP.get((int) this.p[i]);
                pageNode.ShowTree();
            }
        } else {
            System.out.println("this is data node!!! ");
            System.out.println(this);
        }
    }

    public static void main(String[] args) {

        PageNode pageNode = new PageNode(1,1,-1);

        pageNode.keynum=2;

        pageNode.d[0] = 7; pageNode.d[1] = 8;
        pageNode.p[0] = 7; pageNode.p[1] = 8;

        pageNode.SortedAddPair(6,6);

        System.out.println(pageNode);

        /*
        pageNode.d = new int[]{1,5,10,15,20,30};

        int x;
        Scanner in = new Scanner(System.in);
        while(in.hasNext()) {
            x = in.nextInt();
            int pos = pageNode.binSearch(x);
            System.out.println("pos: "+pos);
        }
        */

    }
}
