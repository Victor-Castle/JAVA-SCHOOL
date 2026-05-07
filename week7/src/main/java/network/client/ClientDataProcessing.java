package network.client;

import common.*;
import network.Request;
import network.Response;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;

public class ClientDataProcessing {
    private static ArchiveClient client;

    public static void connectToDatabase() throws SQLException {
        try {
            client = new ArchiveClient();
            System.out.println("连接服务器成功");
        } catch (Exception e) {
            throw new SQLException("连接服务器失败: " + e.getMessage());
        }
    }

    public static void disconnectFromDataBase() throws SQLException {
        try {
            if (client != null) {
                client.close();
                client = null;
                System.out.println("断开服务器连接");
            }
        } catch (Exception e) {
            throw new SQLException("断开服务器连接失败: " + e.getMessage());
        }
    }

    public static AbstractUser searchUser(String name) throws SQLException {
        try {
            Response response = client.sendRequest(new Request(Request.RequestType.SEARCH_USER, name));
            if (response.isSuccess()) {
                return (AbstractUser) response.getData();
            } else {
                throw new SQLException(response.getMessage());
            }
        } catch (Exception e) {
            throw new SQLException("查询用户失败: " + e.getMessage());
        }
    }

    public static AbstractUser searchUser(String name, String password) throws SQLException {
        try {
            Response response = client.sendRequest(new Request(Request.RequestType.LOGIN, name, password));
            if (response.isSuccess()) {
                return (AbstractUser) response.getData();
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new SQLException("登录失败: " + e.getMessage());
        }
    }

    public static Collection<AbstractUser> getAllUsers() throws SQLException {
        try {
            Response response = client.sendRequest(new Request(Request.RequestType.GET_ALL_USERS));
            if (response.isSuccess()) {
                return (Collection<AbstractUser>) response.getData();
            } else {
                throw new SQLException(response.getMessage());
            }
        } catch (Exception e) {
            throw new SQLException("获取用户列表失败: " + e.getMessage());
        }
    }

    public static Collection<Archive> getAllArchives() throws SQLException {
        try {
            Response response = client.sendRequest(new Request(Request.RequestType.GET_ALL_ARCHIVES));
            if (response.isSuccess()) {
                return (Collection<Archive>) response.getData();
            } else {
                throw new SQLException(response.getMessage());
            }
        } catch (Exception e) {
            throw new SQLException("获取档案列表失败: " + e.getMessage());
        }
    }

    public static Archive searchArchive(String archiveId) throws SQLException {
        try {
            Response response = client.sendRequest(new Request(Request.RequestType.SEARCH_ARCHIVE, archiveId));
            if (response.isSuccess()) {
                return (Archive) response.getData();
            } else {
                throw new SQLException(response.getMessage());
            }
        } catch (Exception e) {
            throw new SQLException("查询档案失败: " + e.getMessage());
        }
    }

    public static boolean insertUser(String name, String password, String role) throws SQLException {
        try {
            Response response = client.sendRequest(new Request(Request.RequestType.INSERT_USER, name, password, role));
            return response.isSuccess();
        } catch (Exception e) {
            throw new SQLException("新增用户失败: " + e.getMessage());
        }
    }

    public static boolean updateUser(String name, String password, String role) throws SQLException {
        try {
            Response response = client.sendRequest(new Request(Request.RequestType.UPDATE_USER, name, password, role));
            return response.isSuccess();
        } catch (Exception e) {
            throw new SQLException("更新用户失败: " + e.getMessage());
        }
    }

    public static boolean deleteUser(String name) throws SQLException {
        try {
            Response response = client.sendRequest(new Request(Request.RequestType.DELETE_USER, name));
            return response.isSuccess();
        } catch (Exception e) {
            throw new SQLException("删除用户失败: " + e.getMessage());
        }
    }

    public static boolean insertArchive(String archiveId, String creator, LocalDateTime timestamp, String description, String fileName) throws SQLException {
        try {
            Response response = client.sendRequest(new Request(Request.RequestType.INSERT_ARCHIVE, archiveId, creator, timestamp, description, fileName));
            return response.isSuccess();
        } catch (Exception e) {
            throw new SQLException("新增档案失败: " + e.getMessage());
        }
    }

    public static boolean updateArchive(String archiveId, String creator, LocalDateTime timestamp, String description, String fileName) throws SQLException {
        try {
            Response response = client.sendRequest(new Request(Request.RequestType.UPDATE_ARCHIVE, archiveId, creator, timestamp, description, fileName));
            return response.isSuccess();
        } catch (Exception e) {
            throw new SQLException("更新档案失败: " + e.getMessage());
        }
    }

    public static boolean deleteArchive(String archiveId) throws SQLException {
        try {
            Response response = client.sendRequest(new Request(Request.RequestType.DELETE_ARCHIVE, archiveId));
            return response.isSuccess();
        } catch (Exception e) {
            throw new SQLException("删除档案失败: " + e.getMessage());
        }
    }
}