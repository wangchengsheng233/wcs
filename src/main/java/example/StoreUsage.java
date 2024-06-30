/*
 *@Type Usage.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 03:59
 * @version
 */
package example;

import service.NormalStore;

import java.io.File;
import java.io.IOException;

public class StoreUsage {
    public static void main(String[] args) throws IOException {
        String dataDir="data"+ File.separator;
        NormalStore store = new NormalStore(dataDir);
     // store.set("zsy1","1");
        for (int i = 0; i < 10; i++) {
            store.set("xyq"+i,""+i);
        }
//        store.set("zsy2","2");
//        store.set("zsy3","3");
//        store.set("zsy4","你好");
        System.out.println(store.get("xyq1"));
        //store.set("zsy1","34");
//        store.rm("zsy4");
      //System.out.println(store.get("zsy1"));
        //store.rm("zsy1");
        //System.out.println(store.get("zsy1"));
    }
}
