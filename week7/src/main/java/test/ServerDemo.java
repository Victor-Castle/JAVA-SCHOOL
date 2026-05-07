package test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerDemo {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            /** 创建 ServerSocket */
            // 创建一个 ServerSocket 在端口 12345 监听客户请求
            serverSocket = new ServerSocket(12345);

            while (true) {
                // 侦听并接受到此 Socket的连接,请求到来则产生一个Socket对象，并继续执行
                System.out.println("等待连接……");
                Socket socket = serverSocket.accept();

                /** 获取客户端传来的信息 */
                // 由 Socket对象得到输入流，并构造相应的 ObjectInputStream 对象
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                // 读入并输出从客户端发送过来的字符串
                String message = (String) input.readObject();
                System.out.println("Client say : " + message);

                /** 向客户端发送信息 */
                // 由 Socket对象得到输出流，并构造相应的 ObjectOutputStream 对象
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                message = "hello Client, I am Server!";
                out.writeObject(message);
                // 刷新输出流，使 Client 马上收到该字符串
                out.flush();

                /** 关闭 Socket*/
                out.close();
                input.close();
                socket.close();
            }
        } catch (Exception e) {
            System.out.println("Exception:" + e);
        }
    }
}