/*
 *@Type SocketServerHandler.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 12:50
 * @version
 */
package controller;

import dto.ActionDTO;
import dto.ActionTypeEnum;
import dto.RespDTO;
import dto.RespStatusTypeEnum;
import model.command.Command;
import model.command.GetCommandHandler;
import model.command.RemoveCommandHandler;
import model.command.SetCommandHandler;
import service.NormalStore;
import service.Store;
import utils.LoggerUtil;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SocketServerHandler implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(SocketServerHandler.class);
    private Socket socket;
    private Store store;

    // 使用Map来存储命令类型和对应的命令处理器
    private final Map<ActionTypeEnum, Command> commandMap = new HashMap<>();

    public SocketServerHandler(Socket socket, Store store) {
        // 初始化命令映射
        initCommandMap(store);
        this.socket = socket;
        this.store = store;
    }

    private void initCommandMap(Store store) {
        // 为每种命令类型注册相应的处理器
        commandMap.put(ActionTypeEnum.GET, new GetCommandHandler(store) {
            @Override
            public String getKey() {
                return "";
            }
        });
        commandMap.put(ActionTypeEnum.SET, new SetCommandHandler(store) {
            @Override
            public String getKey() {
                return "";
            }
        });
        commandMap.put(ActionTypeEnum.RM, new RemoveCommandHandler(store) {
            @Override
            public String getKey() {
                return "";
            }
        });
    }

    @Override
    public void run() {
        try {
            // 初始化命令映射
            initCommandMap(store);
            try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

                // 接收序列化对象
                ActionDTO dto = (ActionDTO) ois.readObject();
                LOGGER.info("[SocketServerHandler][ActionDTO]: " + dto.toString());

                // 根据ActionDTO类型动态处理命令
                Command command = commandMap.get(dto.getType());
                if (command != null) {
                    command.execute(dto, oos);
                } else {
                    LOGGER.warn("Unknown action type: " + dto.getType());
                    // 发送错误响应
                    RespDTO resp = new RespDTO(RespStatusTypeEnum.FAIL, "Unknown action type");
                    oos.writeObject(resp);
                    oos.flush();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn("Error handling client request: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭socket连接
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    LOGGER.warn("Error closing socket: " + e.getMessage());
                }
            }
        }
    }


}
