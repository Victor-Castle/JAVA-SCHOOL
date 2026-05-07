package network.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransferServer {
    private static final int PORT = 12346;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("文件传输服务器启动，监听端口：" + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("文件传输客户端连接：" + clientSocket.getInetAddress());
                new Thread(new FileTransferHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class FileTransferHandler implements Runnable {
    private Socket socket;

    public FileTransferHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream());
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("D:\\code\\JAVA2025\\JAVA-School\\archive_files\\" + dis.readUTF()))) {

            long fileLength = dis.readLong();
            byte[] buffer = new byte[1024];
            int read;
            long totalRead = 0;

            while ((read = dis.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
                totalRead += read;
                if (totalRead >= fileLength) break;
            }

            bos.flush();
            System.out.println("文件接收完成");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}