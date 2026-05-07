package network.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArchiveServer {
    private static final int PORT = 12345;
    // 创建固定大小的线程池，线程数可根据实际需求调整
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("档案管理服务器启动，监听端口：" + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("客户端连接：" + clientSocket.getInetAddress());
                // 提交任务到线程池
                executorService.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭线程池
            executorService.shutdown();
        }
    }
}