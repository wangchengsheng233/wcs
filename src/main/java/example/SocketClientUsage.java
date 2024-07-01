/*
 *@Type SocketClientUsage.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 14:07
 * @version
 */
package example;

import client.Client;
import client.CmdClient;
import client.SocketClient;

import java.util.Scanner;

public class SocketClientUsage {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;
        Client client = new SocketClient(host, port);
        CmdClient cmdClient = new CmdClient(client);

        // 创建 Scanner 对象用于 CmdClient
        Scanner scanner = new Scanner(System.in);

        // 创建一个新线程来运行 CmdClient 的 main 方法
        Thread cmdClientThread = new Thread(() -> CmdClient.main(scanner));
        cmdClientThread.start(); // 启动线程

        // 主线程可以继续执行其他任务，例如发送命令
        try {
            // 等待客户端线程启动并运行
            cmdClientThread.join();

            // 发送一些命令以测试
            client.set("zsy12", "for test");
            String value = client.get("zsy12");
            System.out.println("Value for 'zsy12': " + value);
            // client.rm("zsy12");
            // value = client.get("zsy12");
            // System.out.println("Value for 'zsy12': " + value);

            // 以下循环可以用于保持应用程序运行，直到你决定退出
            while (true) {
                // 这里可以添加一些逻辑，例如定期发送心跳或其他任务
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}