import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientDemo {
    public static void main(String[] args) {
        try {
            /** 创建 Socket */
            // 创建一个流套接字并将其连接到指定 IP 地址的指定端口号
            Socket socket = new Socket("127.0.0.1", 12345);
            // 60s超时
            socket.setSoTimeout(60000);

            /** 向服务器发送信息 */
            // 由 Socket对象得到输出流，并构造相应的 ObjectOutputStream 对象
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            // 将输入的字符串发送给 Server
            Scanner scanner = new Scanner(System.in);
            System.out.print("Input:");
            String message = scanner.nextLine();
            out.writeObject(message);
            // 刷新输出流，使 Server马上收到该字符串
            out.flush();

            /** 获取服务端传输来的信息 */
            // 由 Socket对象得到输入流，并构造相应的 ObjectInputStream 对象
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            // 读入并输出从服务器发送过来的字符串
            message = (String) input.readObject();
            System.out.println("Server say : " + message);

            /** 关闭 Socket */
            scanner.close();
            out.close();
            input.close();
            socket.close();
        } catch (Exception e) {
            System.out.println("Exception:" + e);
        }
    }
}
