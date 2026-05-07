package network.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ArchiveServer {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("档案管理服务器启动，监听端口：" + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("客户端连接：" + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
