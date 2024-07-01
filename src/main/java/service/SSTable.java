package service;

import model.command.CommandPos;

import java.io.*;
import java.util.*;


/*
  管理sstable的地方
* 进行文件，自动触发压缩

loadIndex 方法从 SSTable 文件中加载索引。
write 方法将键值对写入 SSTable 文件。
read 方法从 SSTable 文件中读取指定键的值。
* */
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SSTable {
    private RandomAccessFile file;
    private String filePath;
    private HashMap<String, CommandPos> sparseIndex;
    private List<String> sortIndex;
    private final Object lock;
    private MetaInfo tableMetaInfo;

    public SSTable(String filePath) throws IOException {
        this.filePath = filePath;
        this.file = new RandomAccessFile(new File(filePath), "rw");
        this.sparseIndex = new HashMap<>();
        this.sortIndex = new ArrayList<>();
        this.lock = new Object();
        this.tableMetaInfo = new MetaInfo();
        loadMetaInfo();
    }

    // 保存稀疏索引区
    public void writeSparseIndex() throws IOException {
        synchronized (lock) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(sparseIndex);
            oos.flush();
            byte[] indexBytes = bos.toByteArray();
            file.seek(tableMetaInfo.getIndexStart());
            file.write(indexBytes);
            tableMetaInfo.setIndexLen(indexBytes.length);
        }
    }

    // 加载稀疏索引区
    public HashMap<String, CommandPos> loadSparseIndex() throws IOException, ClassNotFoundException {
        synchronized (lock) {
            long indexStart = tableMetaInfo.getIndexStart();
            long indexLen = tableMetaInfo.getIndexLen();
            if (indexLen > 0) {
                file.seek(indexStart);
                byte[] indexBytes = new byte[(int) indexLen];
                file.readFully(indexBytes);
                ByteArrayInputStream bis = new ByteArrayInputStream(indexBytes);
                ObjectInputStream ois = new ObjectInputStream(bis);
                sparseIndex = (HashMap<String, CommandPos>) ois.readObject();
                sortIndex = new ArrayList<>(sparseIndex.keySet());
                Collections.sort(sortIndex);
            }
        }
        return sparseIndex;
    }

    // 加载元数据
    private void loadMetaInfo() throws IOException {
        // 假设元数据位于文件的末尾固定位置
        file.seek(file.length() - MetaInfo.SIZE);
        byte[] metaBytes = new byte[MetaInfo.SIZE];
        file.readFully(metaBytes);
        ByteArrayInputStream bis = new ByteArrayInputStream(metaBytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        tableMetaInfo = (MetaInfo) ois.readObject();
    }

    // 写入数据并更新元数据
    public void put(String key, byte[] value) throws IOException {
        synchronized (lock) {
            long pos = file.length();
            file.seek(pos);
            byte[] keyBytes = key.getBytes();
            file.writeInt(keyBytes.length);
            file.write(keyBytes);
            file.writeInt(value.length);
            file.write(value);
            CommandPos position = new CommandPos((int) (pos + 4 + keyBytes.length + 4), value.length);
            sparseIndex.put(key, position);
            sortIndex.add(key);
            Collections.sort(sortIndex);
            tableMetaInfo.setDataLen(file.length());
            writeSparseIndex(); // 写入稀疏索引到文件
        }
    }

    // 关闭文件
    public void close() throws IOException {
        synchronized (lock) {
            writeSparseIndex(); // 确保关闭文件前将稀疏索引写入文件
            file.close();
        }
    }

    public HashMap<String, CommandPos> getSparseIndex() {
        return sparseIndex;
    }

    public List<String> getSortIndex() {
        return sortIndex;
    }
}