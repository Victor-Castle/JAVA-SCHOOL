package network.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileTransferServer {
    private static final int PORT = 12346;
    // 创建固定大小的线程池，线程数可根据实际需求调整
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("文件传输服务器启动，监听端口：" + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("文件传输客户端连接：" + clientSocket.getInetAddress());
                // 提交任务到线程池
                executorService.execute(new FileTransferHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭线程池
            executorService.shutdown();
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
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            // 读取操作类型（0表示下载，1表示上传）
            int operation = dis.readInt();
            if (operation == 0) {
                // 处理下载请求
                handleDownload(dis, dos);
            } else if (operation == 1) {
                // 处理上传请求
                handleUpload(dis);
            } else {
                System.out.println("未知操作类型：" + operation);
            }

        } catch (EOFException e) {
            // 客户端正常关闭连接
            System.out.println("文件传输客户端连接关闭：" + socket.getInetAddress());
        } catch (Exception e) {
            System.err.println("客户端连接异常：" + e.getMessage());
            // 只打印错误消息，不打印堆栈跟踪，避免控制台混乱
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                // 忽略关闭错误
            }
        }
    }

    private void handleDownload(DataInputStream dis, DataOutputStream dos) throws IOException {
        String fileName = dis.readUTF();
        System.out.println("收到文件下载请求：" + fileName);

        File file = new File("D:\\code\\JAVA2025\\JAVA-School\\archive_files\\" + fileName);
        if (!file.exists()) {
            System.out.println("文件不存在：" + fileName);
            try {
                dos.writeLong(-1); // 发送错误信号
                dos.flush();
            } catch (IOException e) {
                System.err.println("发送错误信号失败：" + e.getMessage());
            }
            return;
        }

        System.out.println("开始发送文件：" + fileName + "，大小：" + file.length() + "字节");
        try {
            dos.writeLong(file.length());
            dos.flush();
        } catch (IOException e) {
            System.err.println("发送文件长度失败：" + e.getMessage());
            return;
        }

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = bis.read(buffer)) != -1) {
                try {
                    dos.write(buffer, 0, read);
                    dos.flush();
                } catch (IOException e) {
                    System.err.println("发送文件数据失败：" + e.getMessage());
                    System.out.println("文件发送中断：" + fileName);
                    return;
                }
            }
            System.out.println("文件发送完成：" + fileName);
        }
    }

    private void handleUpload(DataInputStream dis) throws IOException {
        String fileName = dis.readUTF();
        long fileLength = dis.readLong();
        System.out.println("收到文件上传请求：" + fileName + "，大小：" + fileLength + "字节");

        // 确保目录存在
        File dir = new File("D:\\code\\JAVA2025\\JAVA-School\\archive_files");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, fileName);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            byte[] buffer = new byte[1024];
            int read;
            long totalRead = 0;

            while ((read = dis.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
                totalRead += read;
                if (totalRead >= fileLength) break;
            }

            bos.flush();
            System.out.println("文件上传完成：" + fileName);
        }
    }
}