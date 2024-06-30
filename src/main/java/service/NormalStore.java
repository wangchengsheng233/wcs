/*
 *@Type NormalStore.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 02:07
 * @version
 */
package service;

import com.alibaba.fastjson.JSONObject;
import model.command.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.CommandUtil;
import utils.LoggerUtil;
import utils.RandomAccessFileUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/*
 *
 * */
public class NormalStore implements Store {

    public static final String TABLE = ".table";
    public static final String RW_MODE = "rw";
    public static final String NAME = "data";
    private static final int STORE_THRESHOLD = 10;
    private final Logger LOGGER = LoggerFactory.getLogger(NormalStore.class);
    private final String logFormat = "[NormalStore][{}]: {}";
    /**
     * 数据目录
     */
    private final String dataDir;
    /**
     * 读写锁，支持多线程，并发安全写入
     */
    private final ReadWriteLock indexLock;
    /**
     * 暂存数据
     */
    private final WAL wal;
    /**
     * 内存表，类似缓存
     */
    private TreeMap<String, Command> memTable;

    /**
     * 暂存数据的日志句柄
     */
    // private RandomAccessFile writerReader;
    /**
     * hash索引，存的是数据长度和偏移量
     */
    private HashMap<String, CommandPos> index;


    /**
     * 持久化阈值
     */
//    private final int storeThreshold;
    public NormalStore(String dataDir) throws IOException {
        this.dataDir = dataDir;
        this.indexLock = new ReentrantReadWriteLock();
        this.memTable = new TreeMap<String, Command>();
        this.index = new HashMap<>();
        this.wal = new WAL(dataDir);

        File file = new File(dataDir);
        if (!file.exists()) {
            LoggerUtil.info(LOGGER, logFormat, "NormalStore", "dataDir isn't exist,creating...");
            file.mkdirs();
        }
        this.reloadIndex();
    }

    public String genFilePath() {
        return this.dataDir + File.separator + NAME + TABLE;
    }

    //由于索引没有持久化，因此，当数据库启动时，都需要从磁盘中进行redo操作，刷新索引到内存（引入冷启动问题）
    public void reloadIndex() {

        try {
            /*TODO:单层sstable@*/
            SSTable ssTable = new SSTable(this.genFilePath());
            HashMap<String, CommandPos> loadedIndex = ssTable.loadSparseIndex();

            if (loadedIndex != null) {
                index = loadedIndex;

                // 从WAL日志恢复内存表
                List<Command> commands = wal.loadCommands();
                if (commands != null) {
                    for (Command command : commands) {
                        if (command instanceof SetCommand) {
                            memTable.put(command.getKey(), command);
                        } else if (command instanceof RmCommand) {
                            memTable.remove(command.getKey());
                        }
                    }
                }
            } else {
                // 处理稀疏索引加载为空的情况
                System.out.println("Failed to load sparse index from SSTable.");
            }

//        try {
//            //用 RandomAccessFile 打开一个文件，以读写模式 (RW_MODE) 进行操作。
//            //this.genFilePath() 生成文件路径。
//            RandomAccessFile file = new RandomAccessFile(this.genFilePath(), RW_MODE);
//            long len = file.length();
//            long start = 0;
//
//            //使用 seek(start) 将文件指针移动到起始位置。
//            file.seek(start);
//
//            //循环读取文件内容：
////            每次循环读取一个命令长度 cmdLen（一个 int 类型的值，占 4 个字节）。
////            创建一个 byte 数组 bytes，其长度为 cmdLen，并读取命令数据到该数组中。
////            使用 JSON.parseObject 方法将字节数组转换为 JSONObject。
////            使用 CommandUtil.jsonToCommand 方法将 JSONObject 转换为 Command 对象。
////            将 start 加上 4（因为 cmdLen 是一个 int 类型，占 4 个字节）。
////            如果命令不为 null，创建一个 CommandPos 对象，其位置为 start，长度为 cmdLen，并将其存储到索引中（index）。
////            将 start 加上命令长度 cmdLen。
//
//            //TODO：加sstable，使用稀疏索引，加载到内存中
//            while (start < len) {
//                int cmdLen = file.readInt();
//                byte[] bytes = new byte[cmdLen];
//                file.read(bytes);
//                JSONObject value = JSON.parseObject(new String(bytes, StandardCharsets.UTF_8));
//                Command command = CommandUtil.jsonToCommand(value);
//                start += 4;
//                if (command != null) {
//                    CommandPos cmdPos = new CommandPos((int) start, cmdLen);
//                    index.put(command.getKey(), cmdPos);
//                }
//                start += cmdLen;
//            }
//            file.seek(file.length());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
            LoggerUtil.debug(LOGGER, logFormat, "reload index: " + index.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        //    public void set(String key, String value) {
//        try {
//            SetCommand command = new SetCommand(key, value);
//            byte[] commandBytes = JSONObject.toJSONBytes(command);
//            // 加锁
//            indexLock.writeLock().lock();
//
//
//            //写入WAL
//            wal.appendCommand(command);
//
//            // TODO://先写内存表，内存表达到一定阀值再写进磁盘
//            //简单put
//            memTable.put(key, command);
//
//            // TODO://判断是否需要将内存表中的值写回table
//            /*if (memTable.size() >= STORE_THRESHOLD) {
//                persistMemTable();
//            }*/
//
//     /*       // 写table（wal）文件
//            RandomAccessFileUtil.writeInt(this.genFilePath(), commandBytes.length);
//            int pos = RandomAccessFileUtil.write(this.genFilePath(), commandBytes);
//            //System.out.println("======="+pos);
//            // 保存到memTable
//            // 添加索引
//            CommandPos cmdPos = new CommandPos(pos, commandBytes.length);
//            index.put(key, cmdPos);
//            //判断是否需要将内存表中的值写回table*/
//        } catch (Throwable t) {
//            throw new RuntimeException(t);
//        } finally {
//            indexLock.writeLock().unlock();
//        }
//    }
    }

    @Override
    public void set(String key, String value) {
        try {
            SetCommand command = new SetCommand(key, value);
            byte[] commandBytes = JSONObject.toJSONBytes(command);

            // 加锁
            indexLock.writeLock().lock();

            // 写入内存表
            memTable.put(key, command);

            // 写入WAL
            wal.appendCommand(command);

            // 检查内存表是否满
            if (memTable.size() >= STORE_THRESHOLD) {
                // 内存表满了，触发持久化操作
                persistMemTableToSSTable();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            indexLock.writeLock().unlock();
        }
    }

    private void persistMemTableToSSTable() {
        System.out.println("触发");
        /*
        TODO：持久化硬盘，缺乏索引
        * */
        try {
            // 持久化内存表到SSTable
       /*     String sstableFilePath = this.genFilePath();
            RandomAccessFile sstableFile = new RandomAccessFile(sstableFilePath, "rw");
            sstableFile.seek(sstableFile.length());

            for (Map.Entry<String, Command> entry : memTable.entrySet()) {
                byte[] commandBytes = JSONObject.toJSONBytes(entry.getValue());
                sstableFile.writeInt(commandBytes.length);
                sstableFile.write(commandBytes);

                // 更新索引
                long pos = sstableFile.length() - commandBytes.length - 4;
                CommandPos cmdPos = new CommandPos((int) pos, commandBytes.length);
                index.put(entry.getKey(), cmdPos);
            }
*/
            SSTable ssTable = new SSTable(this.genFilePath());
            for (Map.Entry<String, Command> entry : memTable.entrySet()) {
                byte[] commandBytes = JSONObject.toJSONBytes(entry.getValue());
                ssTable.put(entry.getKey(), commandBytes);
            }
            ssTable.close();

            // 清空内存表
            memTable.clear();

            // 清空WAL
            wal.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String get(String key) {
        try {
            indexLock.readLock().lock();
            //先尝试从内存表中获取
            Command command = memTable.get(key);
            if (command != null) {
                if (command instanceof SetCommand) {
                    if (((SetCommand) command).getDeleted() == 0)
                        return ((SetCommand) command).getValue();
                } else if (command instanceof RmCommand) {
                    return null;
                }
            }

            // 从索引中获取信息 到sstable文件获取数据
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
            indexLock.readLock().lock();
            //先尝试从内存表中获取
            Command command = memTable.get(key);


            //进行删除
            if (command != null) {
                if (command instanceof SetCommand) {
                    if (((SetCommand) command).getDeleted() == 0) {
                        ((SetCommand) command).setType(CommandTypeEnum.RM);
                        ((SetCommand) command).setDeleted(1);
                        memTable.replace(command.getKey(), command);

                        //写入WAL
                        wal.appendCommand(memTable.get(key));

                        //TODO：返回状态码更好点
                        return;
                    }
                }
            }

            // 从索引中获取信息 到sstable文件获取数据
            CommandPos cmdPos = index.get(key);
            if (cmdPos == null) {
                return;
            }

            byte[] commandBytes = RandomAccessFileUtil.readByIndex(this.genFilePath(), cmdPos.getPos(), cmdPos.getLen());

            JSONObject value = JSONObject.parseObject(new String(commandBytes));
            Command cmd = CommandUtil.jsonToCommand(value);
            if (cmd instanceof SetCommand) {
                if (((SetCommand) cmd).getDeleted() == 0) {
                    ((SetCommand) cmd).setType(CommandTypeEnum.RM);
                    ((SetCommand) cmd).setDeleted(1);
                    String dataDir = "data" + File.separator;
                    String filepath = dataDir + File.separator + "redolog" + ".log";
                    RandomAccessFileUtil.writeAtIndex(filepath, cmdPos.getPos(), commandBytes);
                }
            }


        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            indexLock.readLock().unlock();
        }

    }

    @Override
    public void close() throws IOException {

    }
}
