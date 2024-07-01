/*
 *@Type Store.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 02:05
 * @version
 */
package service;

import java.io.Closeable;


// Closeable 自动释放资源
public interface Store extends Closeable {

    /*
     * 增、改
     * */

    //SET

    /*
     * 删
     * */

   // RM,
    /*
     * 查
     * */
    //GET
    void set(String key, String value);

    String get(String key);

    void rm(String key);
}
