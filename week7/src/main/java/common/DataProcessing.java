package common;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户数据处理
 * 用户数据的增删改查
 *
 * @author gongjing
 */
public class DataProcessing {
    private static boolean connectToDB = false;
    static final double EXCEPTION_CONNECT_PROBABILITY = 0.1;
    static final double EXCEPTION_DISCONNECT_PROBABILITY = 0.1;

    // 数据库连接相关变量
    private static Connection connection = null;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/archive_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "000721";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 用户存储容器
     * 以用户名为键，common.AbstractUser 对象为值
     */
    private static Map<String, AbstractUser> users = new HashMap<>();
    /**
     * 档案存储容器
     * 以档案ID为键，common.Archive 对象为值
     */
    private static Map<String, Archive> archives = new HashMap<>();

    final static String ROLE_ADMINISTRATOR = "administrator";
    final static String ROLE_OPERATOR = "operator";
    final static String ROLE_BROWSER = "browser";

    /**
     * 连接数据库
     *
     * @throws SQLException SQL 异常
     */
    public static void connectToDatabase() throws SQLException {
        // 避免重复初始化
        if (connectToDB) {
            return;
        }

        double ranValue = Math.random();
        if (ranValue > EXCEPTION_CONNECT_PROBABILITY) {
            try {
                // 加载驱动
                Class.forName("com.mysql.cj.jdbc.Driver");
                // 建立连接
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                connectToDB = true;
                System.out.println("数据库连接成功");
            } catch (ClassNotFoundException e) {
                connectToDB = false;
                throw new SQLException("数据库驱动加载失败: " + e.getMessage());
            } catch (SQLException e) {
                connectToDB = false;
                throw new SQLException("Not Connected to Database");
            }
        } else {
            connectToDB = false;
            throw new SQLException("Not Connected to Database");
        }
    }

    /**
     * 关闭数据库连接
     *
     * @throws SQLException 数据库断开异常
     */
    public static void disconnectFromDataBase() throws SQLException {
        if (connectToDB) {
            // close Statement and Connection
            try {
                if (Math.random() < EXCEPTION_DISCONNECT_PROBABILITY) {
                    throw new SQLException("Error in disconnecting DB");
                }
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
                throw sqlException;
            } finally {
                connectToDB = false;
                connection = null;
            }
        }
    }

    /**
     * 通过用户名查询用户
     *
     * @param name 用户名
     * @return 用户对象 common.AbstractUser，如果不存在则返回 null
     * @throws SQLException 数据库未连接异常
     */
    public static AbstractUser searchUser(String name) throws SQLException {
        if (!connectToDB) {
            throw new SQLException("Not Connected to Database");
        }

        // 空值和格式检查
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

    /**
     * 通过用户名和密码查询用户，用于登录验证
     *
     * @param name     用户名
     * @param password 密码
     * @return 验证成功返回用户对象，验证失败返回 null
     * @throws SQLException 数据库未连接异常
     */
    public static AbstractUser searchUser(String name, String password) throws SQLException {
        if (!connectToDB) {
            throw new SQLException("Not Connected to Database");
        }

        // 空值和格式检查
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

        // 验证失败返回 null
        return null;
    }

    /**
     * 获取所有用户
     *
     * @return 用户对象的集合
     * @throws SQLException 数据库未连接异常
     */
    public static Collection<AbstractUser> getAllUsers() throws SQLException {
        if (!connectToDB) {
            throw new SQLException("Not Connected to Database");
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

    /**
     * 更新用户信息
     *
     * @param user 用户对象
     * @return boolean 更新是否成功
     * @throws SQLException 数据库未连接异常
     */
    public static boolean updateUser(AbstractUser user) throws SQLException {
        if (!connectToDB) {
            throw new SQLException("Not Connected to Database");
        }

        String name = user.getName();
        // 检查用户是否存在
        if (searchUser(name) == null) {
            System.err.println("更新失败：用户名不存在");
            return false;
        }

        String sql = "UPDATE users SET password = ?, role = ? WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getPassword());
            pstmt.setString(2, user.getRole());
            pstmt.setString(3, name);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * 更新用户信息
     *
     * @param name     用户名（作为唯一标识，不可修改）
     * @param password 新密码
     * @param role     新角色
     * @return boolean 更新是否成功
     * @throws SQLException 数据库未连接异常
     */
    public static boolean updateUser(String name, String password, String role) throws SQLException {
        if (!connectToDB) {
            throw new SQLException("数据库未连接");
        }

        // 空值和格式检查
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

        // 检查用户是否存在
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

    /**
     * 根据角色创建对应的用户对象
     *
     * @param name     用户名
     * @param password 密码
     * @param role     用户角色
     * @return 对应的用户对象，如果角色无效则返回 null
     */
    private static AbstractUser createUserByRole(String name, String password, String role) {
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

    /**
     * 新增用户
     *
     * @param user 用户对象
     * @return boolean 新增是否成功
     * @throws SQLException SQL 异常
     */
    public static boolean insertUser(AbstractUser user) throws SQLException {
        if (!connectToDB) {
            throw new SQLException("Not Connected to Database");
        }

        String name = user.getName();
        if (searchUser(name) != null) {
            System.err.println("新增失败：用户已存在");
            return false;
        }

        String sql = "INSERT INTO users (name, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * 新增用户
     *
     * @param name     用户名
     * @param password 密码
     * @param role     用户角色
     * @return boolean 新增是否成功
     * @throws SQLException SQL 异常
     */
    public static boolean insertUser(String name, String password, String role) throws SQLException {
        if (!connectToDB) {
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

    /**
     * 删除用户
     *
     * @param name 用户名
     * @return boolean  删除是否成功
     * @throws SQLException SQL 异常
     */
    public static boolean deleteUser(String name) throws SQLException {
        if (!connectToDB) {
            throw new SQLException("Not Connected to Database");
        }
        // 空值检查
        if (name == null) {
            System.err.println("删除失败：用户名不能为空");
            return false;
        }

        String sql = "DELETE FROM users WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            } else {
                System.err.println("删除失败：用户不存在");
                return false;
            }
        }
    }

    /**
     * 通过档案号查找档案
     *
     * @param archiveId 档案号
     * @return 档案对象 common.Archive，如果不存在则返回 null
     * @throws SQLException 数据库未连接异常
     */
    public static Archive searchArchive(String archiveId) throws SQLException {
        if (!connectToDB) {
            throw new SQLException("Not Connected to Database");
        }

        // 空值和格式检查
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

    /**
     * 新增档案
     *
     * @param archiveId   档案号
     * @param creator     档案创建者
     * @param timestamp   时间戳
     * @param description 档案描述
     * @param fileName    文件名
     * @return boolean 新增是否成功
     * @throws SQLException SQL 异常
     */
    public static boolean insertArchive(String archiveId, String creator, LocalDateTime timestamp,
                                        String description, String fileName) throws SQLException {
        if (!connectToDB) {
            throw new SQLException("Not Connected to Database");
        }

        // 空值和格式检查
        if (archiveId == null || archiveId.trim().isEmpty() ||
                creator == null || creator.trim().isEmpty() ||
                fileName == null || fileName.trim().isEmpty()) {
            System.err.println("新增失败：档案号、创建者或文件名为空");
            return false;
        }

        String trimmedArchiveId = archiveId.trim();

        // 检查档案号是否已存在
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
            return rowsAffected > 0;
        }
    }

    /**
     * 新增档案
     *
     * @param archive 档案
     * @return boolean 新增是否成功
     * @throws SQLException SQL 异常
     */
    public static boolean insertArchive(Archive archive) throws SQLException {
        if (!connectToDB) {
            throw new SQLException("Not Connected to Database");
        }

        String archiveId = archive.getArchiveId();

        // 检查档案号是否已存在
        if (searchArchive(archiveId) != null) {
            System.err.println("新增失败：档案号已存在");
            return false;
        }

        String sql = "INSERT INTO archives (archive_id, creator, timestamp, description, file_name) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, archiveId);
            pstmt.setString(2, archive.getCreator());
            pstmt.setString(3, archive.getTimestamp().format(DATE_TIME_FORMATTER));
            pstmt.setString(4, archive.getDescription());
            pstmt.setString(5, archive.getFileName());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * 获取所有档案
     *
     * @return 档案对象的集合
     * @throws SQLException 数据库未连接异常
     */
    public static Collection<Archive> getAllArchives() throws SQLException {
        if (!connectToDB) {
            throw new SQLException("Not Connected to Database");
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

    /**
     * 删除档案
     *
     * @param archiveId 档案号
     * @return boolean 删除是否成功
     * @throws SQLException SQL 异常
     */
    public static boolean deleteArchive(String archiveId) throws SQLException {
        if (!connectToDB) {
            throw new SQLException("Not Connected to Database");
        }

        // 空值检查
        if (archiveId == null || archiveId.trim().isEmpty()) {
            System.err.println("删除失败：档案号不能为空");
            return false;
        }

        String sql = "DELETE FROM archives WHERE archive_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, archiveId.trim());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("删除成功");
                return true;
            } else {
                System.err.println("删除失败：档案不存在");
                return false;
            }
        }
    }

    /**
     * 更新档案信息
     *
     * @param archiveId   档案号
     * @param creator     档案创建者
     * @param timestamp   时间戳
     * @param description 档案描述
     * @param fileName    文件名
     * @return boolean 更新是否成功
     * @throws SQLException 数据库未连接异常
     */
    public static boolean updateArchive(String archiveId, String creator, LocalDateTime timestamp,
                                        String description, String fileName) throws SQLException {
        if (!connectToDB) {
            throw new SQLException("Not Connected to Database");
        }

        // 空值和格式检查
        if (archiveId == null || archiveId.trim().isEmpty() ||
                creator == null || creator.trim().isEmpty() ||
                fileName == null || fileName.trim().isEmpty()) {
            System.err.println("更新失败：档案号、创建者或文件名为空");
            return false;
        }

        String trimmedArchiveId = archiveId.trim();

        // 检查档案是否存在
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
                System.out.println("更新成功");
                return true;
            }
        }
        return false;
    }

    /**
     * 更新档案信息
     *
     * @param archive 档案
     * @return boolean 更新是否成功
     * @throws SQLException 数据库未连接异常
     */
    public static boolean updateArchive(Archive archive) throws SQLException {
        if (!connectToDB) {
            throw new SQLException("Not Connected to Database");
        }

        String archiveId = archive.getArchiveId();

        // 检查档案是否存在
        if (searchArchive(archiveId) == null) {
            System.err.println("更新失败：档案号不存在");
            return false;
        }

        String sql = "UPDATE archives SET creator = ?, timestamp = ?, description = ?, file_name = ? WHERE archive_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, archive.getCreator());
            pstmt.setString(2, archive.getTimestamp().format(DATE_TIME_FORMATTER));
            pstmt.setString(3, archive.getDescription());
            pstmt.setString(4, archive.getFileName());
            pstmt.setString(5, archiveId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("更新成功");
                return true;
            }
        }
        return false;
    }
}