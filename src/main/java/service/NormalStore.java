/*
 *@Type NormalStore.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 02:07
 * @version
 */
package service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import controller.SocketServerHandler;
import model.command.Command;
import model.command.CommandPos;
import model.command.RmCommand;
import model.command.SetCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.CommandUtil;
import utils.LoggerUtil;
import utils.RandomAccessFileUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.jar.JarEntry;

public class NormalStore implements Store {

    public static final String TABLE = ".table";
    public static final String RW_MODE = "rw";
    public static final String NAME = "data";
    private final Logger LOGGER = LoggerFactory.getLogger(NormalStore.class);
    private final String logFormat = "[NormalStore][{}]: {}";


    /**
     * 内存表，类似缓存
     */
    private TreeMap<String, Command> memTable;

    /**
     * hash索引，存的是数据长度和偏移量
     * */
    private HashMap<String, CommandPos> index;

    /**
     * 数据目录
     */
    private final String dataDir;

    /**
     * 读写锁，支持多线程，并发安全写入
     */
    private final ReadWriteLock indexLock;

    /**
     * 暂存数据的日志句柄
     */
    private RandomAccessFile writerReader;

    /**
     * 持久化阈值
     */
//    private final int storeThreshold;

    public NormalStore(String dataDir) {
        this.dataDir = dataDir;
        this.indexLock = new ReentrantReadWriteLock();
        this.memTable = new TreeMap<String, Command>();
        this.index = new HashMap<>();

        File file = new File(dataDir);
        if (!file.exists()) {
            LoggerUtil.info(LOGGER,logFormat, "NormalStore","dataDir isn't exist,creating...");
            file.mkdirs();
        }
        this.reloadIndex();
    }

    public String genFilePath() {
        return this.dataDir + File.separator + NAME + TABLE;
    }


    public void reloadIndex() {
        try {
            RandomAccessFile file = new RandomAccessFile(this.genFilePath(), RW_MODE);
            long len = file.length();
            long start = 0;
            file.seek(start);
            while (start < len) {
                int cmdLen = file.readInt();
                byte[] bytes = new byte[cmdLen];
                file.read(bytes);
                JSONObject value = JSON.parseObject(new String(bytes, StandardCharsets.UTF_8));
                Command command = CommandUtil.jsonToCommand(value);
                start += 4;
                if (command != null) {
                    CommandPos cmdPos = new CommandPos((int) start, cmdLen);
                    index.put(command.getKey(), cmdPos);
                }
                start += cmdLen;
            }
            file.seek(file.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
        LoggerUtil.debug(LOGGER, logFormat, "reload index: "+index.toString());
    }

    public void set(String key, String value) {
        try {
            SetCommand command = new SetCommand(key, value);
            byte[] commandBytes = JSONObject.toJSONBytes(command);

            // 加锁
            indexLock.writeLock().lock();

            try {
                // 写table（wal）文件
                RandomAccessFileUtil.writeInt(this.genFilePath(), commandBytes.length);
                int pos = RandomAccessFileUtil.write(this.genFilePath(), commandBytes);

                // 保存到memTable
                // 添加索引
                CommandPos cmdPos = new CommandPos(pos, commandBytes.length);
                index.put(key, cmdPos);

                // 判断是否需要将内存表中的值写回table
                // 假设我们有一个阈值，比如内存表的大小达到某个值就刷新到磁盘
                if (shouldFlushMemTable()) {
                    flushMemTableToDisk();
                }
            } finally {
                // 确保在操作完成后释放锁
                indexLock.writeLock().unlock();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    // 检查内存表是否达到刷新阈值的方法
    private boolean shouldFlushMemTable() {
        // 这里的具体条件根据你的应用场景来设定
        // 例如，可以基于内存表的大小或条目数量
        int YOUR_THRESHOLD = 4096;
        return memTable.size() > YOUR_THRESHOLD; // YOUR_THRESHOLD是刷新阈值
    }

    // 将内存表刷新到磁盘的方法
    private void flushMemTableToDisk() throws IOException {
        // 遍历memTable，将所有条目写入磁盘文件
        for (Map.Entry<String, CommandPos> entry : index.entrySet()) {
            CommandPos cmdPos = entry.getValue();
            byte[] commandBytes = readCommandBytesFromWAL(cmdPos);
            // 将commandBytes写入磁盘的table文件
            RandomAccessFileUtil.write(this.genFilePath(), commandBytes);
        }

        // 清空内存表，重置大小
        memTable.clear();
        // 可以选择在这里进行其他操作，比如更新索引或状态
    }

    // 从WAL文件读取指定位置的命令字节
    private byte[] readCommandBytesFromWAL(CommandPos cmdPos) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(this.genFilePath(), "r")) {
            file.seek(cmdPos.getPos());
            return new byte[]{file.readByte()};
        }
    }

    @Override
    public String get(String key) {
        try {
            indexLock.readLock().lock();
            // 从索引中获取信息
            CommandPos cmdPos = index.get(key);
            if (cmdPos == null) {
                return null;
            }
            byte[] commandBytes = RandomAccessFileUtil.readByIndex(this.genFilePath(), cmdPos.getPos(), cmdPos.getLen());

            JSONObject value = JSONObject.parseObject(new String(commandBytes));
            Command cmd = CommandUtil.jsonToCommand(value);
            if (cmd instanceof SetCommand) {
                return ((SetCommand) cmd).getValue();
            }
            if (cmd instanceof RmCommand) {
                return null;
            }

        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            indexLock.readLock().unlock();
        }
        return null;
    }

    @Override
    public void rm(String key) {
        try {
            RmCommand command = new RmCommand(key);
            byte[] commandBytes = JSONObject.toJSONBytes(command);

            // 加锁
            indexLock.writeLock().lock();

            try {
                // 写table（wal）文件
                RandomAccessFileUtil.write(this.genFilePath(), commandBytes);
                // 将删除命令写入内存表
                memTable.put(key, command);

                // 判断是否需要将内存表中的值写回table
                if (shouldFlushMemTable()) {
                    flushMemTableToDisk();
                }
            } finally {
                indexLock.writeLock().unlock();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            // 加锁以确保在关闭期间不会进行写入操作
            indexLock.writeLock().lock();

            // 将内存表中的数据刷新到磁盘
            flushMemTableToDisk();

            // 执行关闭前的清理工作，例如关闭文件句柄等
            cleanupResources();

        } catch (Throwable t) {
            throw new IOException("Failed to close the database", t);
        } finally {
            // 确保锁被释放
            indexLock.writeLock().unlock();
        }
    }

    private void cleanupResources() {
        // 这里可以释放其他资源，例如关闭文件句柄等
        // 如果有文件输入输出流或其他资源，确保在这里关闭它们
    }
}
