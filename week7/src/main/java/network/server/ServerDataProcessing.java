package network.server;

import common.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

public class ServerDataProcessing {
    private Connection connection = null;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/archive_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "000721";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    final static String ROLE_ADMINISTRATOR = "administrator";
    final static String ROLE_OPERATOR = "operator";
    final static String ROLE_BROWSER = "browser";

    public void connectToDatabase() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("数据库连接成功");
        } catch (ClassNotFoundException e) {
            throw new SQLException("数据库驱动加载失败: " + e.getMessage());
        } catch (SQLException e) {
            throw new SQLException("数据库连接失败: " + e.getMessage());
        }
    }

    public void disconnectFromDataBase() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("数据库连接已关闭");
        }
    }

    public AbstractUser searchUser(String name) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("数据库未连接");
        }

        if (name == null || name.trim().isEmpty()) {
            System.err.println("查询失败：用户名为空");
            return null;
        }

        String sql = "SELECT * FROM users WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("name");
                    String password = rs.getString("password");
                    String role = rs.getString("role");
                    return createUserByRole(username, password, role);
                }
            }
        }
        return null;
    }

    public AbstractUser searchUser(String name, String password) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("数据库未连接");
        }

        if (name == null || name.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            System.err.println("登录失败：用户名或密码为空");
            return null;
        }

        String sql = "SELECT * FROM users WHERE name = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name.trim());
            pstmt.setString(2, password.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("name");
                    String userPassword = rs.getString("password");
                    String role = rs.getString("role");
                    return createUserByRole(username, userPassword, role);
                }
            }
        }
        return null;
    }

    public Collection<AbstractUser> getAllUsers() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("数据库未连接");
        }

        Collection<AbstractUser> userList = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                String password = rs.getString("password");
                String role = rs.getString("role");
                AbstractUser user = createUserByRole(name, password, role);
                if (user != null) {
                    userList.add(user);
                }
            }
        }
        return userList;
    }

    public boolean updateUser(AbstractUser user) throws SQLException {
        return updateUser(user.getName(), user.getPassword(), user.getRole());
    }

    public boolean updateUser(String name, String password, String role) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("数据库未连接");
        }

        if (name == null || name.trim().isEmpty()) {
            System.err.println("更新失败：用户名不能为空");
            return false;
        }

        if (password == null || password.trim().isEmpty()) {
            System.err.println("更新失败：密码不能为空");
            return false;
        }

        if (role == null || role.trim().isEmpty()) {
            System.err.println("更新失败：角色不能为空");
            return false;
        }

        String trimmedName = name.trim();
        String trimmedPassword = password.trim();
        String trimmedRole = role.trim();

        if (searchUser(trimmedName) == null) {
            System.err.println("更新失败：用户名不存在 - " + trimmedName);
            return false;
        }

        String sql = "UPDATE users SET password = ?, role = ? WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, trimmedPassword);
            pstmt.setString(2, trimmedRole);
            pstmt.setString(3, trimmedName);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("用户信息更新成功");
                return true;
            }
        }
        return false;
    }

    private AbstractUser createUserByRole(String name, String password, String role) {
        if (ROLE_ADMINISTRATOR.equalsIgnoreCase(role)) {
            return new Administrator(name, password, role);
        } else if (ROLE_OPERATOR.equalsIgnoreCase(role)) {
            return new Operator(name, password, role);
        } else if (ROLE_BROWSER.equalsIgnoreCase(role)) {
            return new Browser(name, password, role);
        } else {
            System.err.println("创建失败：无效的角色 - " + role);
            return null;
        }
    }

    public boolean insertUser(AbstractUser user) throws SQLException {
        return insertUser(user.getName(), user.getPassword(), user.getRole());
    }

    public boolean insertUser(String name, String password, String role) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("数据库未连接");
        }

        if (name == null || name.trim().isEmpty()) {
            System.err.println("新增失败：用户名不能为空");
            return false;
        }

        if (password == null || password.trim().isEmpty()) {
            System.err.println("新增失败：密码不能为空");
            return false;
        }

        if (role == null || role.trim().isEmpty()) {
            System.err.println("新增失败：角色不能为空");
            return false;
        }

        String trimmedName = name.trim();
        String trimmedPassword = password.trim();
        String trimmedRole = role.trim();

        if (searchUser(trimmedName) != null) {
            System.err.println("新增失败：用户已存在 - " + trimmedName);
            return false;
        }

        String sql = "INSERT INTO users (name, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, trimmedName);
            pstmt.setString(2, trimmedPassword);
            pstmt.setString(3, trimmedRole);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("用户新增成功：" + trimmedName);
                return true;
            }
        }
        return false;
    }

    public boolean deleteUser(String name) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("数据库未连接");
        }

        if (name == null || name.trim().isEmpty()) {
            System.err.println("删除失败：用户名不能为空");
            return false;
        }

        String sql = "DELETE FROM users WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name.trim());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("用户删除成功：" + name.trim());
                return true;
            } else {
                System.err.println("删除失败：用户不存在");
                return false;
            }
        }
    }

    public Archive searchArchive(String archiveId) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("数据库未连接");
        }

        if (archiveId == null || archiveId.trim().isEmpty()) {
            System.err.println("查找失败：档案号为空");
            return null;
        }

        String sql = "SELECT * FROM archives WHERE archive_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, archiveId.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("archive_id");
                    String creator = rs.getString("creator");
                    String timestampStr = rs.getString("timestamp");
                    LocalDateTime timestamp = LocalDateTime.parse(timestampStr, DATE_TIME_FORMATTER);
                    String description = rs.getString("description");
                    String fileName = rs.getString("file_name");
                    return new Archive(id, creator, timestamp, description, fileName);
                }
            }
        }
        return null;
    }

    public boolean insertArchive(String archiveId, String creator, LocalDateTime timestamp,
                                 String description, String fileName) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("数据库未连接");
        }

        if (archiveId == null || archiveId.trim().isEmpty() ||
                creator == null || creator.trim().isEmpty() ||
                fileName == null || fileName.trim().isEmpty()) {
            System.err.println("新增失败：档案号、创建者或文件名为空");
            return false;
        }

        String trimmedArchiveId = archiveId.trim();

        if (searchArchive(trimmedArchiveId) != null) {
            System.err.println("新增失败：档案号已存在");
            return false;
        }

        String sql = "INSERT INTO archives (archive_id, creator, timestamp, description, file_name) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, trimmedArchiveId);
            pstmt.setString(2, creator.trim());
            pstmt.setString(3, timestamp.format(DATE_TIME_FORMATTER));
            pstmt.setString(4, description != null ? description.trim() : "");
            pstmt.setString(5, fileName.trim());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("档案新增成功：" + trimmedArchiveId);
                return true;
            }
        }
        return false;
    }

    public boolean insertArchive(Archive archive) throws SQLException {
        return insertArchive(archive.getArchiveId(), archive.getCreator(), archive.getTimestamp(),
                archive.getDescription(), archive.getFileName());
    }

    public Collection<Archive> getAllArchives() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("数据库未连接");
        }

        Collection<Archive> archiveList = new ArrayList<>();
        String sql = "SELECT * FROM archives";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String archiveId = rs.getString("archive_id");
                String creator = rs.getString("creator");
                String timestampStr = rs.getString("timestamp");
                LocalDateTime timestamp = LocalDateTime.parse(timestampStr, DATE_TIME_FORMATTER);
                String description = rs.getString("description");
                String fileName = rs.getString("file_name");
                Archive archive = new Archive(archiveId, creator, timestamp, description, fileName);
                archiveList.add(archive);
            }
        }
        return archiveList;
    }

    public boolean deleteArchive(String archiveId) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("数据库未连接");
        }

        if (archiveId == null || archiveId.trim().isEmpty()) {
            System.err.println("删除失败：档案号不能为空");
            return false;
        }

        String sql = "DELETE FROM archives WHERE archive_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, archiveId.trim());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("档案删除成功：" + archiveId.trim());
                return true;
            } else {
                System.err.println("删除失败：档案不存在");
                return false;
            }
        }
    }

    public boolean updateArchive(String archiveId, String creator, LocalDateTime timestamp,
                                 String description, String fileName) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("数据库未连接");
        }

        if (archiveId == null || archiveId.trim().isEmpty() ||
                creator == null || creator.trim().isEmpty() ||
                fileName == null || fileName.trim().isEmpty()) {
            System.err.println("更新失败：档案号、创建者或文件名为空");
            return false;
        }

        String trimmedArchiveId = archiveId.trim();

        if (searchArchive(trimmedArchiveId) == null) {
            System.err.println("更新失败：档案号不存在");
            return false;
        }

        String sql = "UPDATE archives SET creator = ?, timestamp = ?, description = ?, file_name = ? WHERE archive_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, creator.trim());
            pstmt.setString(2, timestamp.format(DATE_TIME_FORMATTER));
            pstmt.setString(3, description != null ? description.trim() : "");
            pstmt.setString(4, fileName.trim());
            pstmt.setString(5, trimmedArchiveId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("档案更新成功：" + trimmedArchiveId);
                return true;
            }
        }
        return false;
    }

    public boolean updateArchive(Archive archive) throws SQLException {
        return updateArchive(archive.getArchiveId(), archive.getCreator(), archive.getTimestamp(),
                archive.getDescription(), archive.getFileName());
    }
}