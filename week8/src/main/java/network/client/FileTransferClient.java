package network.client;

import java.io.*;
import java.net.Socket;

public class FileTransferClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 12346;

    public static void uploadFile(File file) throws IOException {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             FileInputStream fis = new FileInputStream(file)) {

            // 发送操作类型（1表示上传）
            dos.writeInt(1);
            dos.writeUTF(file.getName());
            dos.writeLong(file.length());

            byte[] buffer = new byte[1024];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, read);
            }

            dos.flush();
            System.out.println("文件上传完成");

        } catch (IOException e) {
            throw new IOException("文件上传失败: " + e.getMessage());
        }
    }

    public static void downloadFile(String fileName, String savePath) throws IOException {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(savePath + "\\" + fileName))) {

            // 发送操作类型（0表示下载）
            dos.writeInt(0);
            dos.writeUTF(fileName);
            dos.flush();

            long fileLength = dis.readLong();
            if (fileLength == -1) {
                throw new IOException("文件不存在：" + fileName);
            }

            byte[] buffer = new byte[1024];
            int read;
            long totalRead = 0;

            while ((read = dis.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
                totalRead += read;
                if (totalRead >= fileLength) break;
            }

            bos.flush();
            System.out.println("文件下载完成");

        } catch (IOException e) {
            throw new IOException("文件下载失败: " + e.getMessage());
        }
    }
}