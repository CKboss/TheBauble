package edu.ecnu.ica.index.demo.BPlusTreeV2;

import java.util.ArrayList;

/**
 * Created by ckboss on 16-7-9.
 */
public class PageNode {

    private int D = 3;
    private int LIMIT = D/2+1;

    private final int INF =0x3f3f3f3f;

    // 0 other 1 leav
    int TYPE;
    int father;
    int brother;
    int ID;

    // sep and point
    KeyPair[] d = null;
    long[] p = null;

    int keynum;

    PageNode(int id,int type,int fa) {

        d = new KeyPair[D+2];
        p = new long[D+2];

        TYPE = type;
        father = fa;
        ID = id;
        keynum=0;
        brother=-1;
    }

    int SortedAddPair(KeyPair key,long value) {

        if(keynum==0) {
            d[0]=key;
            p[0]=value;
            keynum++;
            return keynum;
        }
        boolean write = false;

        for(int i=this.keynum-1;i>=0&&write==false;i--) {

            if(d[i].compareTo(key)>0) {
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

    long binSearch(KeyPair x) {
        long ret = -1;
        int low=0,high=this.keynum-1;
        while(low<=high) {
            int mid = (low+high) / 2;
            if(d[mid].compareTo(x)>=0) {
                ret = p[mid];
                high = mid-1;
            } else {
                low = mid+1;
            }
        }
        return ret;
    }

    /**
     * 二分查找最靠近左边的又大于等于x的pair的位置
     * 如果x是最大的,则返回INF
     * 如果x是最小的,则返回-INF
     * @param x
     * @return
     */
    int binSearchID(KeyPair x) {
        int ret = -1;
        int low=0,high=this.keynum-1;

        if(x.compareTo(d[high])>=0) {
            return INF;
        } else if(x.compareTo(d[low])<0){
            return -INF;
        }

        while(low<=high) {
            int mid = (low+high) / 2;
            if(d[mid].compareTo(x)>=0) {
                ret = mid;
                high = mid-1;
            } else {
                low = mid+1;
            }
        }
        return ret;
    }


    // from top to bottom insert a key-value to the left
    boolean Insert(int fa,KeyPair key,long value) {

        if(TYPE==1) {
            // Only insert into Leave Node

            // check if has same key
            for(int i=0;i<this.keynum;i++) {
                if(d[i].compareTo(key)==0) {
                    return false;
                }
            }

            if (keynum < D) {
                // ok can be insert into current node
                SortedAddPair(key,value);
            } else if(keynum == D){
                // split PageNodeOperation
                // left node [0,LIMIT]
                // right node [LIMIT+1,D]

                InsertOrSplit(key,value);
            }

            return true;

        } else {
            // find a children

            boolean isminist = true;

            int postion = (int) this.binSearchID(key);
            if(postion==INF) postion = this.keynum-1;
            else if(postion==-INF) postion = 0;

            for(int i=postion;i>=0;i--) {
                if(key.compareTo(d[i])>=0) {
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
    void InsertOrSplit(KeyPair key,long value) {
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

    /**
     * 大于等于x的第一个节点的位置
     * @param x
     * @return
     */
    long Query(KeyPair x) {

        if(this.TYPE==1) {
            //找到叶子节点
            int postion = (int) this.binSearchID(x);
            if(postion==INF) {
                postion = this.keynum-1;
            } else if(postion==-INF){
                postion = 0;
            }

            for(int i=postion;i>=0;i--) {
                if (x.compareTo(d[i]) >= 0) {
                    return p[i];
                }
            }

            return -INF;
        }

        if(this.TYPE==0) {

            boolean isminst=true;

            int postion = (int) this.binSearchID(x);
            if(postion==INF) {
                postion = this.keynum-1;
            } else if(postion==-INF){
                postion = 0;
            }

            for (int i = postion; i >= 0; i--) {
                if (x.compareTo(d[i]) >= 0) {
                    PageNode children = BPlusTree.VP.get((int) p[i]);
                    isminst = false;
                    return children.Query(x);
                }
            }
            if(isminst==true) {
                return BPlusTree.VP.get((int)p[0]).Query(x);
            }
        }

        return -1;
    }

    /**
     * 大于x的第一个PageNode的位置
     * @param x
     * @return
     */
    public long getLeftNode(KeyPair x) {

        if(this.TYPE==1) {
            return this.ID;
        } else {

            int postion = (int) this.binSearchID(x);
            if(postion==INF) {
                postion = keynum;
            } else if(postion==-INF){
                postion = 0;
            }

            for (int i = postion; i >= 0; i--) {
                if (x.compareTo(d[i]) >= 0) {
                    PageNode children = BPlusTree.VP.get((int) p[i]);
                    return children.getLeftNode(x);
                }
            }
            return BPlusTree.VP.get((int)p[0]).getLeftNode(x);
        }
    }

    /**
     * 找到在左右KeyPair之间的p[i]
     * @param left_kp
     * @param right_kp
     * @return
     */
    public ArrayList<Long> getRange(KeyPair left_kp,KeyPair right_kp) {

        long node = this.getLeftNode(left_kp);
        ArrayList<Long> ret = new ArrayList<>();
        PageNode pageNode = BPlusTree.VP.get((int) node);
        boolean goon = true;

        for(int i=0;i<pageNode.keynum;i++) {
            KeyPair k = pageNode.d[i];
            if(left_kp.compareTo(k)<=0&&right_kp.compareTo(k)>=0) {
                ret.add(pageNode.p[i]);
            } else if(right_kp.compareTo(k)<0){
                goon=false;
                break;
            }
        }

        long bro = BPlusTree.VP.get((int) node).brother;
        while(bro!=-1&&goon) {
            pageNode = BPlusTree.VP.get((int) bro);
            for (int i = 0; i < pageNode.keynum; i++) {
                KeyPair k = pageNode.d[i];
                if (left_kp.compareTo(k) <= 0 && right_kp.compareTo(k) >= 0) {
                    ret.add(pageNode.p[i]);
                } else if(right_kp.compareTo(k)>0){
                    goon = false; break;
                }
            }
            bro = BPlusTree.VP.get((int) bro).brother;
        }

        return ret;
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
            str_d += "("+d[i].getFirst()+" , "+d[i].getSecond()+")";
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

        PageNode pn = new PageNode(0,0,0);
        pn.d[0] = new KeyPair(1,3);
        pn.d[1] = new KeyPair(1,7);
        pn.keynum = 2;

        System.out.println(pn.binSearchID(new KeyPair(1,11)));
    }
}
