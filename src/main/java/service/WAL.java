package service;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import model.command.Command;
import utils.CommandUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;



import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WAL {
    private static final String RW_MODE = "rw";
    private final String walFilePath;
    private final RandomAccessFile walFile;

    public WAL(String dataDir) throws IOException {
        this.walFilePath = dataDir + File.separator + "redolog.log";
        this.walFile = new RandomAccessFile(this.walFilePath, RW_MODE);
    }

    public void appendCommand(Command command) {
        try {
            byte[] commandBytes = JSONObject.toJSONBytes(command);
            walFile.seek(walFile.length());
            walFile.writeInt(commandBytes.length);
            walFile.write(commandBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Command> loadCommands() {
        List<Command> commands = new ArrayList<>();
        try {
            long len = walFile.length();
            long start = 0;
            walFile.seek(start);
            while (start < len) {
                int cmdLen = walFile.readInt();
                byte[] bytes = new byte[cmdLen];
                walFile.read(bytes);
                JSONObject value = JSON.parseObject(new String(bytes, StandardCharsets.UTF_8));
                Command command = CommandUtil.jsonToCommand(value);
                if (command != null) {
                    commands.add(command);
                }
                start += 4 + cmdLen;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return commands;
    }

    public void clear() {
        try {
            walFile.setLength(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            walFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}