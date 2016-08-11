package edu.ecnu.ica.mergesort;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by root on 7/31/16.
 */
public class FileIdMap {

    public static Map<String,Integer> FileNameToID = new ConcurrentHashMap<>();
    public static Map<Integer,String> FileIDToName = new ConcurrentHashMap<>();

    private static int id1 = 0;

    public synchronized static int GetFileID(String filename) {
        if (FileNameToID.containsKey(filename)) {
            return FileNameToID.get(filename);
        } else {
            FileNameToID.put(filename, id1);
            FileIDToName.put(id1,filename);
            return id1++;
        }
    }

    public static String GetFileName(int x) {
        return FileIDToName.get(x);
    }
}
