package edu.ecnu.ica.index.indisk.BPlusTree;

import com.sun.org.apache.xpath.internal.SourceTree;
import edu.ecnu.ica.index.ParametersConfig;
import edu.ecnu.ica.index.store.IndexFileHandler;
import edu.ecnu.ica.index.store.bean.IndexPage;

/**
 * Created by ckboss on 16-7-9.
 */
public class PageNodeOperation {

    //TODO 二分查找和数组的移动

    //TODO 联合索引

    private static int D = ParametersConfig.D;
    private static int LIMIT = D/2+1;

    final int INF = 0x3f3f3f3f;

    IndexFileHandler indexFileHandler;

    PageNodeOperation(IndexFileHandler handler) {
        indexFileHandler = handler;
    }


    public int getNextIdAndIncOne() {
        int ret = indexFileHandler.getNextId();
        indexFileHandler.setNextId(ret+1);
        return ret+1;
    }

    public int binSearchID(int id,long x) {

        IndexPage page = this.indexFileHandler.getPage(id);
        IndexPage.IndexPageHeader pageHeader = page.getPageHeader();

        int ret = -INF;
        int low=0,high=pageHeader.getKeynum()-1;

        if(x>=page.get(0,high)) {
            return INF;
        } else if(x<page.get(0,low)) {
            return -INF;
        }


        while(low<=high) {
            int mid = (low+high) / 2;
            if(page.get(0,mid)>=x) {
                ret = mid;
                high = mid-1;
            } else {
                low = mid+1;
            }
        }
        return ret;
    }


    public void SetPageHeader(int id,int type,int fa) {

        IndexPage page = this.indexFileHandler.getPage(id);
        IndexPage.IndexPageHeader pageHeader = page.getPageHeader();

        pageHeader.setId(id);
        pageHeader.setBrother(-1);
        pageHeader.setFather(fa);
        pageHeader.setType(type);
        pageHeader.setKeynum(0);
    }

    public int SortedAddPair(int pageID,long key,long value) {

        IndexPage page = this.indexFileHandler.getPage(pageID);
        IndexPage.IndexPageHeader pageHeader = page.getPageHeader();

        if(pageHeader.getKeynum()==0) {

            page.put(key,0,0);
            page.put(value,1,0);
            pageHeader.setKeynum(pageHeader.getKeynum()+1);

            return pageHeader.getKeynum();
        }

        boolean write = false;
        for(int i=pageHeader.getKeynum()-1;i>=0&&write==false;i--) {

            if(page.get(0,i)==key) {
                return -0x3f3f3f3f;
            }

            if(page.get(0,i)>key) {

                page.put(page.get(0,i),0,i+1);
                page.put(page.get(1,i),1,i+1);
            } else {

                page.put(key,0,i+1);
                page.put(value,1,i+1);
                write = true;
            }

        }

        if(!write) {
            page.put(key,0,0);
            page.put(value,1,0);
        }

        int keynum = pageHeader.getKeynum() + 1;
        pageHeader.setKeynum(keynum);

        return keynum;
    }

    // from top to bottom insert a key-value to the leave
    public long Insert(int id,int fa,long key,long value) {

        IndexPage page = this.indexFileHandler.getPage(id);
        IndexPage.IndexPageHeader pageHeader = page.getPageHeader();

        if(pageHeader.getType()==1) {

            // check if has Same Key

            int postion = pageHeader.getKeynum()-1;

            if(pageHeader.getKeynum()!=0) {
                postion = binSearchID(id,key);
                if(postion==INF) postion=pageHeader.getKeynum()-1;
                else if(postion==-INF) postion=0;
            }

            for(int i=postion;i>=0;i--) {
                if(page.get(0,i)==key) {
                    return page.get(1,i);
                }
            }

            // Only insert into Leave Node
            if (pageHeader.getKeynum() < D) {
                // ok can be insert into current node
                SortedAddPair(id,key,value);
            } else if(pageHeader.getKeynum() == D){
                // split PageNodeOperation
                // left node [0,LIMIT]
                // right node [LIMIT+1,D]

                InsertOrSplit(id,key,value);
            }

            return -INF;
        } else {
            // find a children

            boolean isminist = true;

            int postion = binSearchID(id,key);
            if(postion==INF) postion=pageHeader.getKeynum()-1;
            else if(postion==-INF) postion=0;

            for(int i=postion;i>=0;i--) {
                if(key>=page.get(0,i)) {
                    isminist = false;
                    int childID = (int) page.get(1,i);
                    return Insert(childID,id,key,value);
                }
            }

            if(isminist==true) {
                // 更新一下d[0]的值 , 然后往最小的节点走
                page.put(key,0,0);
                int childID = (int) page.get(1,0);
                return Insert(childID,id,key,value);
            }

        }

        return -INF;
    }

    // split node from bottom to top
    public void InsertOrSplit(int ID,long key,long value) {

        IndexPage page = this.indexFileHandler.getPage(ID);
        IndexPage.IndexPageHeader pageHeader = page.getPageHeader();

        if(pageHeader.getKeynum()+1<=D) {
            // ok just insert into this node
            SortedAddPair(ID,key,value);

        } else {
            // no enough free space , go up

            // split
            //PageNodeOperation newchildren = new PageNodeOperation(BPlusTree.getID(),this.TYPE,this.father);

            int newchildId = this.getNextIdAndIncOne();

            IndexPage childrenPage = this.indexFileHandler.getPage(newchildId);
            IndexPage.IndexPageHeader childrenPageHeader = childrenPage.getPageHeader();

            SetPageHeader(newchildId,pageHeader.getType(),pageHeader.getFather());
            SortedAddPair(ID,key,value);

            // left node [0,LIMIT]
            // right node [LIMIT+1,D+1]
            for(int i=LIMIT,keynum = pageHeader.getKeynum();i<keynum;i++) {

                SortedAddPair(newchildId,page.get(0,i),page.get(1,i));

                if(childrenPageHeader.getType()==0) {
                    // change the father of children node
                    this.indexFileHandler.getPage((int) page.get(1,i)).getPageHeader().setFather(newchildId);
                }

            }
            pageHeader.setKeynum(LIMIT);

            if(pageHeader.getType()==1) {
                childrenPageHeader.setBrother(pageHeader.getBrother());
                pageHeader.setBrother(childrenPageHeader.getId());
            }

            if(pageHeader.getFather()!=-1) {

                InsertOrSplit(pageHeader.getFather(), childrenPage.get(0,0), childrenPageHeader.getId());

            } else {
                // make a newbaba

                /*
                PageNodeOperation newbaba = new PageNodeOperation(BPlusTree.getID(),0,-1);
                BPlusTree.VP.add(newbaba);
                BPlusTree.root=newbaba;
                */

                int newbabaID = this.getNextIdAndIncOne();
                this.indexFileHandler.setRootIndex(newbabaID);

                SetPageHeader(newbabaID,0,-1);

                pageHeader.setFather(newbabaID);
                childrenPageHeader.setFather(newbabaID);

                /*
                System.out.println(childrenPageHeader.getId()+" -------->  "+newbabaID);
                System.out.println(childrenPageHeader);
                ShowPage(6);
                */

                SortedAddPair(newbabaID,page.get(0,0),pageHeader.getId());
                SortedAddPair(newbabaID,childrenPage.get(0,0),childrenPageHeader.getId());
            }
        }
    }

    public long Query(int ID,long x) {

        IndexPage page = this.indexFileHandler.getPage(ID);
        IndexPage.IndexPageHeader pageheader = page.getPageHeader();

        if(pageheader.getType()==1) {
            //找到叶子节点
            for(int i=0,keynum=pageheader.getKeynum();i<keynum;i++) {
                if(page.get(0,i)==x) {
                    return page.get(1,i);
                }
            }
        }

        int postion = binSearchID(ID,x);
        if(postion==INF) postion=pageheader.getKeynum()-1;
        else if(postion==-INF) postion=0;

        for(int i=postion;i>=0;i--) {
            if(x>=page.get(0,i)) {
                if(pageheader.getType()==0) {
                    int childId = (int) page.get(1, i);
                    return Query(childId, x);
                }
            }
        }

        return -1;
    }

    public void getBrotherArray(int pageid) {

        IndexPage page = this.indexFileHandler.getPage(pageid);
        IndexPage.IndexPageHeader header = page.getPageHeader();

        if(header.getType()==1) {
            for (int i = 0, sz = header.getKeynum(); i < sz; i++) {
                System.out.printf(page.get(0, i) + ",");
            }

            int brother = header.getBrother();
            if (brother != -1) {
                getBrotherArray(brother);
            }
        }
    }

    public void ShowPage(int pageid) {

        IndexPage page = this.indexFileHandler.getPage(pageid);
        System.out.println(page.getPageHeader());

        System.out.println("Show page: "+pageid+" ------------------  ");

        System.out.println("ID: "+page.getPageHeader().getId());
        System.out.println("TYPE: "+page.getPageHeader().getType());
        System.out.println("KeyNum: "+page.getPageHeader().getKeynum());
        System.out.println("Brother: "+page.getPageHeader().getBrother());
        System.out.println("Father: "+page.getPageHeader().getFather());

        System.out.println("Array D: ");
        for(int i=0;i<page.getPageHeader().getKeynum();i++) {
            System.out.printf(page.get(0,i)+", ");
        }
        System.out.println("");

        System.out.println("Array P: ");
        for(int i=0;i<page.getPageHeader().getKeynum();i++) {
            System.out.printf(page.get(1,i)+", ");
        }
        System.out.println("");
    }


    public void ShowTree(int rootid) {

        IndexPage page = this.indexFileHandler.getPage(rootid);
        int keynum = page.getPageHeader().getKeynum();

        if(page.getPageHeader().getType()==1) {
            System.out.println("this is Data Node");
        }

        ShowPage(rootid);

        if(page.getPageHeader().getType()==0) {
            for (int i = 0; i < keynum; i++) {
                long childrenid = page.get(1, i);
                ShowTree((int) childrenid);
            }
        }
    }
}

