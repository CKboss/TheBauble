package edu.ecnu.ica.index.indisk.BPlusTreeV2;

import edu.ecnu.ica.index.ParametersConfig;
import edu.ecnu.ica.index.demo.BPlusTreeV2.KeyPair;
import edu.ecnu.ica.index.doubleindex.TwoDimensionIndexFileFactory;
import edu.ecnu.ica.index.doubleindex.TwoDimensionIndexFileHandler;
import edu.ecnu.ica.index.doubleindex.bean.TwoDimensionPage;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ckboss on 16-7-15.
 */
public class PageNodeOperationV2 {

    private static int D = ParametersConfig.D2;
    //private static int D = 3;
    private static int LIMIL = D/2+1;

    final int INF = 0x3f3f3f3f;

    private TwoDimensionIndexFileHandler indexFileHandler;

    PageNodeOperationV2(TwoDimensionIndexFileHandler handler) {
        indexFileHandler=handler;
    }

    public int getNextIdAndIncOne() {
        int ret = (int) indexFileHandler.getNextId();
        indexFileHandler.setNextId(ret+1);
        return ret+1;
    }

    public long binSearch(int id , KeyPair x) {

        TwoDimensionPage page = this.indexFileHandler.getPage(id);
        TwoDimensionPage.TwoDimensionPageHeader pageHeader = page.getPageHeader();

        long ret = -1;
        int low=0,high=pageHeader.getKeynum()-1;
        while(low<=high) {
            int mid = (low+high) / 2;
            KeyPair d = new KeyPair(page.get(0,mid),page.get(1,mid));
            if(d.compareTo(x)>=0) {
                ret = page.get(2,mid);
                high = mid-1;
            } else {
                low = mid+1;
            }
        }
        return ret;
    }

    public int binSearchID(int id, KeyPair x) {

        TwoDimensionPage page = this.indexFileHandler.getPage(id);
        TwoDimensionPage.TwoDimensionPageHeader pageHeader = page.getPageHeader();

        int ret = -INF;
        int low = 0 , high = pageHeader.getKeynum()-1;

        KeyPair kp_frist = new KeyPair(page.get(0,low),page.get(1,low));
        KeyPair kp_last = new KeyPair(page.get(0,high),page.get(1,high));

        if(x.compareTo(kp_last)>=0) {
            return INF;
        } else if(x.compareTo(kp_frist)<0) {
            return -INF;
        }

        while(low<=high) {
            int mid = (low+high)/2;
            KeyPair kp_mid = new KeyPair(page.get(0,mid),page.get(1,mid));
            if(kp_mid.compareTo(x)>=0) {
                ret = mid;
                high = mid-1;
            } else {
                low = mid+1;
            }
        }

        return ret;
    }

    public void SetPageHeader(int id,int type,int fa) {

        TwoDimensionPage page = this.indexFileHandler.getPage(id);
        TwoDimensionPage.TwoDimensionPageHeader pageHeader = page.getPageHeader();

        pageHeader.setId(id);
        pageHeader.setBrother(-1);
        pageHeader.setType(type);
        pageHeader.setFather(fa);
        pageHeader.setKeynum(0);
    }

    int SortedAddPair(int id,KeyPair key,long value) {

        TwoDimensionPage page = this.indexFileHandler.getPage(id);
        TwoDimensionPage.TwoDimensionPageHeader pageHeader = page.getPageHeader();

        if(pageHeader.getKeynum()==0) {
            page.put(key.getFirst(),0,0);
            page.put(key.getSecond(),1,0);
            page.put(value,2,0);
            int newkeynum = pageHeader.getKeynum()+1;
            pageHeader.setKeynum(newkeynum);
            return newkeynum;
        }

        boolean write = false;

        for(int i=pageHeader.getKeynum()-1;i>=0&&write==false;i--) {

            KeyPair d_i = new KeyPair(page.get(0,i),page.get(1,i));

            if(d_i.compareTo(key)>0) {

                page.put(d_i.getFirst(),0,i+1);
                page.put(d_i.getSecond(),1,i+1);

                page.put(page.get(2,i),2,i+1);
            } else {

                page.put(key.getFirst(),0,i+1);
                page.put(key.getSecond(),1,i+1);

                page.put(value,2,i+1);
                write=true;
            }
        }

        if(!write) {

            page.put(key.getFirst(),0,0);
            page.put(key.getSecond(),1,0);

            page.put(value,2,0);
        }

        int keynum = pageHeader.getKeynum() + 1;
        pageHeader.setKeynum(keynum);

        return keynum;
    }

    long Insert(int id, KeyPair key,long value) {

        TwoDimensionPage page = this.indexFileHandler.getPage(id);
        TwoDimensionPage.TwoDimensionPageHeader header = page.getPageHeader();

        if(header.getType()==1) {

            // check if has same key

            int keynum = header.getKeynum();
            int postion = keynum-1;

            if(keynum!=0) {
                postion = (int) this.binSearchID(id, key);
                if (postion == INF) postion = keynum - 1;
                else if (postion == -INF) postion = 0;
            }

            for(int i=postion;i>=0;i--) {
                KeyPair d_i = new KeyPair(page.get(0,i),page.get(1,i));
                if(d_i.compareTo(key)==0) {
                    // 已经插入过返回,以前的value值
                    return page.get(2,i);
                }
            }

            if(keynum<D) {
                // inset into current node
                SortedAddPair(id,key,value);
            } else if(keynum==D) {
                InsertOrSplit(id,key,value);
            }

            return -INF;

        } else {

            boolean isminist = true;
            int keynum = header.getKeynum();

            int postion = (int) this.binSearchID(id,key);
            if(postion==INF) postion = keynum-1;
            else if(postion==-INF) postion = 0;

            for(int i=postion;i>=0;i--) {
                KeyPair d_i = new KeyPair(page.get(0,i),page.get(1,i));
                if(key.compareTo(d_i)>=0) {
                    isminist = false;
                    int childID = (int) page.get(2,i);
                    return Insert(childID,key,value);
                }
            }

            if(isminist) {

                page.put(key.getFirst(),0,0);
                page.put(key.getSecond(),1,0);

                int childID = (int) page.get(2,0);
                return Insert(childID,key,value);
            }
        }

        return -INF;
    }


    void InsertOrSplit(int id,KeyPair key,long value) {

        TwoDimensionPage page = this.indexFileHandler.getPage(id);
        TwoDimensionPage.TwoDimensionPageHeader handler = page.getPageHeader();

        int keynum = handler.getKeynum();
        if(keynum+1<=D) {
            SortedAddPair(id,key,value);
        } else {

            int newchildId = this.getNextIdAndIncOne();

            TwoDimensionPage childrenPage = this.indexFileHandler.getPage(newchildId);
            TwoDimensionPage.TwoDimensionPageHeader childrenPageHandler = childrenPage.getPageHeader();

            SetPageHeader(newchildId,handler.getType(),handler.getFather());
            SortedAddPair(id,key,value);

            for(int i=LIMIL,sz = handler.getKeynum();i<sz;i++) {

                KeyPair d_i = new KeyPair(page.get(0,i),page.get(1,i));
                SortedAddPair(newchildId,d_i,page.get(2,i));

                if(childrenPageHandler.getType()==0) {
                    this.indexFileHandler.getPage((int)page.get(2,i)).getPageHeader().setFather(newchildId);
                }
            }

            handler.setKeynum(LIMIL);

            if(handler.getType()==1) {
                childrenPageHandler.setBrother(handler.getBrother());
                handler.setBrother(childrenPageHandler.getId());
            }
            // make a new baba

            if(handler.getFather()!=-1) {
                InsertOrSplit(handler.getFather(),new KeyPair(childrenPage.get(0,0),childrenPage.get(1,0)),childrenPageHandler.getId());
            } else {
                int newbabaID = this.getNextIdAndIncOne();
                this.indexFileHandler.setRootIndex(newbabaID);

                SetPageHeader(newbabaID, 0, -1);

                handler.setFather(newbabaID);
                childrenPageHandler.setFather(newbabaID);

                KeyPair page_d0 = new KeyPair(page.get(0, 0), page.get(1, 0));
                KeyPair children_d0 = new KeyPair(childrenPage.get(0, 0), childrenPage.get(1, 0));


                SortedAddPair(newbabaID, page_d0, handler.getId());
                SortedAddPair(newbabaID, children_d0, childrenPageHandler.getId());
            }
        }
    }


    /**
     * 大于等于x的第一个节点的位置
     * @param x
     * @return
     */
    long Query(int id,KeyPair x) {

        TwoDimensionPage page = this.indexFileHandler.getPage(id);
        TwoDimensionPage.TwoDimensionPageHeader header = page.getPageHeader();

        if(header.getType()==1) {

            int postion = binSearchID(id,x);
            if(postion==INF) {
                postion = header.getKeynum()-1;
            } else if(postion==-INF) {
                postion = 0;
            }

            for(int i=postion;i>=0;i--) {

                KeyPair d = new KeyPair(page.get(0,i),page.get(1,i));
                if(x.compareTo(d)>=0) {
                    return page.get(2,i);
                }
            }
        }

        if(header.getType()==0) {
            boolean isminist = true;

            int postion = binSearchID(id,x);
            if(postion==INF) {
                postion = header.getKeynum()-1;
            } else if(postion==-INF) {
                postion = 0;
            }

            for(int i=postion;i>=0;i--) {

                KeyPair d = new KeyPair(page.get(0,i),page.get(1,i));

                if(x.compareTo(d)>=0) {
                    int childrenId = (int) page.get(2,i);
                    isminist = false;
                    return Query(childrenId,x);
                }
            }

            if(isminist==true) {
                return Query((int) page.get(2,0),x);
            }
        }

        return -INF;
    }


    public long getLeftNode(int id,KeyPair x) {

        TwoDimensionPage page = this.indexFileHandler.getPage(id);
        TwoDimensionPage.TwoDimensionPageHeader header = page.getPageHeader();

        if(header.getType()==1) {
            return id;
        } else {

            int postion = binSearchID(id,x);
            if(postion==INF) {
                postion = header.getKeynum()-1;
            } else if(postion==-INF){
                postion = 0;
            }

            for(int i=postion;i>=0;i--) {
                KeyPair d = new KeyPair(page.get(0,i),page.get(1,i));

                if(x.compareTo(d)>=0) {
                    int childId = (int) page.get(2,i);
                    return getLeftNode(childId,x);
                }
            }

            return getLeftNode((int) page.get(2,0),x);
        }
    }

    public ArrayList<Long> getRange(int id,KeyPair left_kp,KeyPair right_kp) {

        int leftnodeid = (int) getLeftNode(id,left_kp);

        TwoDimensionPage page = this.indexFileHandler.getPage(leftnodeid);
        TwoDimensionPage.TwoDimensionPageHeader header = page.getPageHeader();

        ArrayList<Long> ret = new ArrayList<>();
        boolean goon = true;

        for(int i=0,sz=header.getKeynum();i<sz;i++) {

            KeyPair d = new KeyPair(page.get(0,i),page.get(1,i));
            if(left_kp.compareTo(d)<=0&&right_kp.compareTo(d)>=0) {
                ret.add(page.get(2,i));
            } else if(right_kp.compareTo(d)<0) {
                goon = false; break;
            }
        }

        long bro = header.getBrother();
        while(bro!=-1&&goon) {

            TwoDimensionPage BroPage = this.indexFileHandler.getPage((int) bro);
            TwoDimensionPage.TwoDimensionPageHeader BroHeader = BroPage.getPageHeader();

            for(int i=0,sz=BroHeader.getKeynum();i<sz;i++) {

                KeyPair d = new KeyPair(BroPage.get(0,i),BroPage.get(1,i));
                if (left_kp.compareTo(d) <= 0 && right_kp.compareTo(d) >= 0) {
                    ret.add(BroPage.get(2, i));
                } else if (right_kp.compareTo(d) < 0) {
                    goon = false;
                    break;
                }
            }

            bro = BroHeader.getBrother();
        }

        return ret;
    }

    public void ShowPage(int id) {

        TwoDimensionPage page = this.indexFileHandler.getPage(id);
        TwoDimensionPage.TwoDimensionPageHeader header = page.getPageHeader();


        System.out.println("Show page: "+id+" ------------------  ");

        System.out.println("ID: "+page.getPageHeader().getId());
        System.out.println("TYPE: "+page.getPageHeader().getType());
        System.out.println("KeyNum: "+page.getPageHeader().getKeynum());
        System.out.println("Brother: "+page.getPageHeader().getBrother());
        System.out.println("Father: "+page.getPageHeader().getFather());

        System.out.println("Array D: ");
        for(int i=0;i<page.getPageHeader().getKeynum();i++) {
            KeyPair di = new KeyPair(page.get(0,i),page.get(1,i));
            System.out.printf(di+", ");
        }
        System.out.println("");

        System.out.println("Array P: ");
        for(int i=0;i<page.getPageHeader().getKeynum();i++) {
            System.out.printf(page.get(2,i)+", ");
        }
        System.out.println("");

    }

    public void ShowTree(int rootid) {

        TwoDimensionPage page = this.indexFileHandler.getPage(rootid);
        int keynum = page.getPageHeader().getKeynum();

        if(page.getPageHeader().getType()==1) {
            System.out.println("this is Data Node");
        }

        ShowPage(rootid);

        if(page.getPageHeader().getType()==0) {
            for (int i = 0; i < keynum; i++) {
                long childrenid = page.get(2, i);
                ShowTree((int) childrenid);
            }
        }
    }


    public static void main(String[] args) throws IOException {
        TwoDimensionIndexFileFactory.createIndexFile(ParametersConfig.order2DIndex);
        TwoDimensionIndexFileHandler indexFileHandler = TwoDimensionIndexFileFactory.openIndexFile(ParametersConfig.order2DIndex);
        PageNodeOperationV2 pno = new PageNodeOperationV2(indexFileHandler);

        indexFileHandler.setRootIndex(0);

        KeyPair ka = new KeyPair(4,2);
        KeyPair kb = new KeyPair(4,8);
        KeyPair kc = new KeyPair(4,1);
        KeyPair kd = new KeyPair(4,4);

        pno.SetPageHeader(0,1,-1);

        pno.Insert(0,ka,7);
        pno.Insert(0,kb,2);
        pno.Insert(0,kc,3);

        pno.Insert(0,kd,4);

        pno.ShowTree((int) indexFileHandler.getRootIndex());
    }
}
