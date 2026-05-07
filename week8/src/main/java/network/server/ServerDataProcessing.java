package network.server;

import common.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ServerDataProcessing {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/archive_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "000721";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_POOL_SIZE = 10;
    private static BlockingQueue<Connection> connectionPool;

    final static String ROLE_ADMINISTRATOR = "administrator";
    final static String ROLE_OPERATOR = "operator";
    final static String ROLE_BROWSER = "browser";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initializeConnectionPool();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void initializeConnectionPool() {
        connectionPool = new ArrayBlockingQueue<>(MAX_POOL_SIZE);
        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            try {
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                connectionPool.offer(connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println("数据库连接池初始化完成，连接数: " + connectionPool.size());
    }

    private Connection getConnection() throws SQLException {
        try {
            return connectionPool.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("获取数据库连接失败", e);
        }
    }

    private void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connectionPool.offer(connection);
                } else {
                    // 连接已关闭，创建新连接加入池
                    Connection newConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    connectionPool.offer(newConnection);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void connectToDatabase() throws SQLException {
        // 连接池已在静态初始化中创建，这里不需要额外操作
        System.out.println("数据库连接池就绪");
    }

    public void disconnectFromDataBase() throws SQLException {
        // 连接池由所有线程共享，不在这里关闭
        System.out.println("数据库连接已归还到连接池");
    }

    public AbstractUser searchUser(String name) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            if (name == null || name.trim().isEmpty()) {
                System.err.println("查询失败：用户名为空");
                return null;
            }

            String sql = "SELECT * FROM users WHERE name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        } finally {
            releaseConnection(conn);
        }
        return null;
    }

    public AbstractUser searchUser(String name, String password) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            if (name == null || name.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {
                System.err.println("登录失败：用户名或密码为空");
                return null;
            }

            String sql = "SELECT * FROM users WHERE name = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        } finally {
            releaseConnection(conn);
        }
        return null;
    }

    public Collection<AbstractUser> getAllUsers() throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            Collection<AbstractUser> userList = new ArrayList<>();
            String sql = "SELECT * FROM users";
            try (Statement stmt = conn.createStatement();
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
        } finally {
            releaseConnection(conn);
        }
    }

    public boolean updateUser(AbstractUser user) throws SQLException {
        return updateUser(user.getName(), user.getPassword(), user.getRole());
    }

    public boolean updateUser(String name, String password, String role) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
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
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, trimmedPassword);
                pstmt.setString(2, trimmedRole);
                pstmt.setString(3, trimmedName);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("用户信息更新成功");
                    return true;
                }
            }
        } finally {
            releaseConnection(conn);
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
        Connection conn = null;
        try {
            conn = getConnection();
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
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, trimmedName);
                pstmt.setString(2, trimmedPassword);
                pstmt.setString(3, trimmedRole);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("用户新增成功：" + trimmedName);
                    return true;
                }
            }
        } finally {
            releaseConnection(conn);
        }
        return false;
    }

    public boolean deleteUser(String name) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            if (name == null || name.trim().isEmpty()) {
                System.err.println("删除失败：用户名不能为空");
                return false;
            }

            String sql = "DELETE FROM users WHERE name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        } finally {
            releaseConnection(conn);
        }
    }

    public Archive searchArchive(String archiveId) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            if (archiveId == null || archiveId.trim().isEmpty()) {
                System.err.println("查找失败：档案号为空");
                return null;
            }

            String sql = "SELECT * FROM archives WHERE archive_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        } finally {
            releaseConnection(conn);
        }
        return null;
    }

    public boolean insertArchive(String archiveId, String creator, LocalDateTime timestamp,
                                 String description, String fileName) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
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
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        } finally {
            releaseConnection(conn);
        }
        return false;
    }

    public boolean insertArchive(Archive archive) throws SQLException {
        return insertArchive(archive.getArchiveId(), archive.getCreator(), archive.getTimestamp(),
                archive.getDescription(), archive.getFileName());
    }

    public Collection<Archive> getAllArchives() throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            Collection<Archive> archiveList = new ArrayList<>();
            String sql = "SELECT * FROM archives";
            try (Statement stmt = conn.createStatement();
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
        } finally {
            releaseConnection(conn);
        }
    }

    public boolean deleteArchive(String archiveId) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            if (archiveId == null || archiveId.trim().isEmpty()) {
                System.err.println("删除失败：档案号不能为空");
                return false;
            }

            String sql = "DELETE FROM archives WHERE archive_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        } finally {
            releaseConnection(conn);
        }
    }

    public boolean updateArchive(String archiveId, String creator, LocalDateTime timestamp,
                                 String description, String fileName) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
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
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        } finally {
            releaseConnection(conn);
        }
        return false;
    }

    public boolean updateArchive(Archive archive) throws SQLException {
        return updateArchive(archive.getArchiveId(), archive.getCreator(), archive.getTimestamp(),
                archive.getDescription(), archive.getFileName());
    }
}