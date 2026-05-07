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
                try {
                    Request request = (Request) inputStream.readObject();
                    Response response = handleRequest(request);
                    outputStream.writeObject(response);
                    outputStream.flush();
                } catch (EOFException e) {
                    // 客户端正常关闭连接，不视为异常
                    System.out.println("客户端连接关闭：" + socket.getInetAddress());
                    break;
                } catch (Exception e) {
                    // 其他异常处理
                    System.err.println("处理请求异常：" + e.getMessage());
                    // 发送错误响应
                    Response errorResponse = new Response(false, "处理请求失败：" + e.getMessage(), null);
                    try {
                        outputStream.writeObject(errorResponse);
                        outputStream.flush();
                    } catch (Exception ex) {
                        // 忽略写入错误
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("客户端连接异常：" + e.getMessage());
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (socket != null) socket.close();
                dataProcessing.disconnectFromDataBase();
            } catch (Exception e) {
                // 忽略关闭错误
            }
        }
    }

    private Response handleRequest(Request request) {
        Response response;
        try {
            String threadName = Thread.currentThread().getName();
            System.out.println(threadName + ":");
            System.out.println("CLIENT_" + request.getType().name());
            if (request.getParameters() != null && request.getParameters().length > 0) {
                for (Object param : request.getParameters()) {
                    if (param != null) {
                        System.out.println(threadName + ":");
                        System.out.println(param.toString());
                    }
                }
            }

            switch (request.getType()) {
                case LOGIN:
                    String name = (String) request.getParameters()[0];
                    String password = (String) request.getParameters()[1];
                    AbstractUser user = dataProcessing.searchUser(name, password);
                    response = new Response(user != null, user != null ? "登录成功" : "登录失败", user);
                    break;

                case GET_ALL_USERS:
                    response = new Response(true, "获取成功", dataProcessing.getAllUsers());
                    break;

                case GET_ALL_ARCHIVES:
                    response = new Response(true, "获取成功", dataProcessing.getAllArchives());
                    break;

                case SEARCH_USER:
                    response = new Response(true, "查询成功", dataProcessing.searchUser((String) request.getParameters()[0]));
                    break;

                case SEARCH_ARCHIVE:
                    response = new Response(true, "查询成功", dataProcessing.searchArchive((String) request.getParameters()[0]));
                    break;

                case INSERT_USER:
                    boolean insertUserSuccess = dataProcessing.insertUser((String) request.getParameters()[0],
                            (String) request.getParameters()[1], (String) request.getParameters()[2]);
                    response = new Response(insertUserSuccess, insertUserSuccess ? "新增成功" : "新增失败", null);
                    break;

                case UPDATE_USER:
                    boolean updateUserSuccess = dataProcessing.updateUser((String) request.getParameters()[0],
                            (String) request.getParameters()[1], (String) request.getParameters()[2]);
                    response = new Response(updateUserSuccess, updateUserSuccess ? "更新成功" : "更新失败", null);
                    break;

                case DELETE_USER:
                    boolean deleteUserSuccess = dataProcessing.deleteUser((String) request.getParameters()[0]);
                    response = new Response(deleteUserSuccess, deleteUserSuccess ? "删除成功" : "删除失败", null);
                    break;

                case INSERT_ARCHIVE:
                    boolean insertArchiveSuccess = dataProcessing.insertArchive((String) request.getParameters()[0],
                            (String) request.getParameters()[1], (LocalDateTime) request.getParameters()[2],
                            (String) request.getParameters()[3], (String) request.getParameters()[4]);
                    response = new Response(insertArchiveSuccess, insertArchiveSuccess ? "新增成功" : "新增失败", null);
                    break;

                case UPDATE_ARCHIVE:
                    boolean updateArchiveSuccess = dataProcessing.updateArchive((String) request.getParameters()[0],
                            (String) request.getParameters()[1], (LocalDateTime) request.getParameters()[2],
                            (String) request.getParameters()[3], (String) request.getParameters()[4]);
                    response = new Response(updateArchiveSuccess, updateArchiveSuccess ? "更新成功" : "更新失败", null);
                    break;

                case DELETE_ARCHIVE:
                    boolean deleteArchiveSuccess = dataProcessing.deleteArchive((String) request.getParameters()[0]);
                    response = new Response(deleteArchiveSuccess, deleteArchiveSuccess ? "删除成功" : "删除失败", null);
                    break;

                default:
                    response = new Response(false, "未知请求类型", null);
                    break;
            }
        } catch (SQLException e) {
            response = new Response(false, "数据库操作失败：" + e.getMessage(), null);
        } catch (Exception e) {
            response = new Response(false, "处理请求失败：" + e.getMessage(), null);
        }

        // 输出服务器响应
        String threadName = Thread.currentThread().getName();
        System.out.println(threadName + ":");
        System.out.println("SERVER>>> SERVER_" + request.getType().name());
        return response;
    }
}