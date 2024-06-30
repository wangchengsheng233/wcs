/*
 *@Type RandomAccessFileUtil.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 02:58
 * @version
 */
package utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/*
* RandomAccessFileUtil 类提供了三个静态方法，用于对文件进行基本的读写操作。
write 方法：将字节数组写入文件末尾，并返回写入前的文件长度。
writeInt 方法：将整数写入文件末尾。
readByIndex 方法：从文件的指定位置读取指定长度的字节数据。
这些方法通过 RandomAccessFile 类实现文件的随机访问，
允许在文件的任意位置进行读写操作。每个方法都包含了异常处理和文件资源的关闭操作，
* 以确保安全可靠的文件操作。*/
public class RandomAccessFileUtil {

    private static final String RW_MODE = "rw";

    public static int write(String filePath, byte[] value) {
        RandomAccessFile file = null;
        long len = -1L;
        try {
            file = new RandomAccessFile(filePath, RW_MODE);
            len = file.length();
            file.seek(len);
            file.write(value);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (int) len;
    }

    public static void writeInt(String filePath, int value) {
        RandomAccessFile file = null;
        long len = -1L;
        try {
            file = new RandomAccessFile(filePath, RW_MODE);
            len = file.length();
            file.seek(len);
            file.writeInt(value);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] readByIndex(String filePath, int index, int len) {
        RandomAccessFile file = null;
        byte[] res = new byte[len];
        try {
            file = new RandomAccessFile(filePath, RW_MODE);
            file.seek((long) index);
            file.read(res, 0, len);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

        //  TODO：压缩进行分层文件
    /*
    TODO：需要遍历各个文件
     * 文件指定位置删除
     先简单实现基本
     * */
    public static void writeAtIndex(String filePath, int index, byte[] value) {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(filePath, RW_MODE);
            file.seek((long) index);
            file.write(value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    * TODO：数据块的构造 稀疏索引，进行指定位置修改
    * */


}

