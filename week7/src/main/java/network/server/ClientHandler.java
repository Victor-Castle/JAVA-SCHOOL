package network.server;

import common.*;
import network.Request;
import network.Response;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private ServerDataProcessing dataProcessing;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.dataProcessing = new ServerDataProcessing();
    }

    @Override
    public void run() {
        try {
            // 初始化流
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            // 连接数据库
            dataProcessing.connectToDatabase();

            // 处理请求
            while (true) {
                Request request = (Request) inputStream.readObject();
                Response response = handleRequest(request);
                outputStream.writeObject(response);
                outputStream.flush();
            }
        } catch (Exception e) {
            System.err.println("客户端处理异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (socket != null) socket.close();
                dataProcessing.disconnectFromDataBase();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Response handleRequest(Request request) {
        try {
            switch (request.getType()) {
                case LOGIN:
                    String name = (String) request.getParameters()[0];
                    String password = (String) request.getParameters()[1];
                    AbstractUser user = dataProcessing.searchUser(name, password);
                    return new Response(user != null, user != null ? "登录成功" : "登录失败", user);

                case GET_ALL_USERS:
                    return new Response(true, "获取成功", dataProcessing.getAllUsers());

                case GET_ALL_ARCHIVES:
                    return new Response(true, "获取成功", dataProcessing.getAllArchives());

                case SEARCH_USER:
                    return new Response(true, "查询成功", dataProcessing.searchUser((String) request.getParameters()[0]));

                case SEARCH_ARCHIVE:
                    return new Response(true, "查询成功", dataProcessing.searchArchive((String) request.getParameters()[0]));

                case INSERT_USER:
                    boolean insertUserSuccess = dataProcessing.insertUser((String) request.getParameters()[0],
                            (String) request.getParameters()[1], (String) request.getParameters()[2]);
                    return new Response(insertUserSuccess, insertUserSuccess ? "新增成功" : "新增失败", null);

                case UPDATE_USER:
                    boolean updateUserSuccess = dataProcessing.updateUser((String) request.getParameters()[0],
                            (String) request.getParameters()[1], (String) request.getParameters()[2]);
                    return new Response(updateUserSuccess, updateUserSuccess ? "更新成功" : "更新失败", null);

                case DELETE_USER:
                    boolean deleteUserSuccess = dataProcessing.deleteUser((String) request.getParameters()[0]);
                    return new Response(deleteUserSuccess, deleteUserSuccess ? "删除成功" : "删除失败", null);

                case INSERT_ARCHIVE:
                    boolean insertArchiveSuccess = dataProcessing.insertArchive((String) request.getParameters()[0],
                            (String) request.getParameters()[1], (LocalDateTime) request.getParameters()[2],
                            (String) request.getParameters()[3], (String) request.getParameters()[4]);
                    return new Response(insertArchiveSuccess, insertArchiveSuccess ? "新增成功" : "新增失败", null);

                case UPDATE_ARCHIVE:
                    boolean updateArchiveSuccess = dataProcessing.updateArchive((String) request.getParameters()[0],
                            (String) request.getParameters()[1], (LocalDateTime) request.getParameters()[2],
                            (String) request.getParameters()[3], (String) request.getParameters()[4]);
                    return new Response(updateArchiveSuccess, updateArchiveSuccess ? "更新成功" : "更新失败", null);

                case DELETE_ARCHIVE:
                    boolean deleteArchiveSuccess = dataProcessing.deleteArchive((String) request.getParameters()[0]);
                    return new Response(deleteArchiveSuccess, deleteArchiveSuccess ? "删除成功" : "删除失败", null);

                default:
                    return new Response(false, "未知请求类型", null);
            }
        } catch (SQLException e) {
            return new Response(false, "数据库操作失败：" + e.getMessage(), null);
        } catch (Exception e) {
            return new Response(false, "处理请求失败：" + e.getMessage(), null);
        }
    }
}