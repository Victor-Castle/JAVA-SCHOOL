import java.util.*;
import java.sql.*;
import java.time.LocalDateTime;

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

    /**
     * 用户存储容器
     * 以用户名为键，AbstractUser 对象为值
     */
    private static Map<String, AbstractUser> users = new HashMap<>();
    /**
     * 档案存储容器
     * 以档案ID为键，Archive 对象为值
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
            connectToDB = true;
            // 初始化用户数据以及档案数据（只在首次连接时初始化）
            if (users.isEmpty()) {
                users.put("jack", new Operator("jack", "123", ROLE_OPERATOR));
                users.put("rose", new Browser("rose", "123", ROLE_BROWSER));
                users.put("kate", new Administrator("kate", "123", ROLE_ADMINISTRATOR));

                archives.put("001", new Archive("001", "jack", LocalDateTime.now(), "test", "test.txt"));
                archives.put("002", new Archive("002", "jack", LocalDateTime.now(), "data", "data.txt"));
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
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
                throw sqlException;
            } finally {
                connectToDB = false;
            }
        }
    }

    /**
     * 通过用户名查询用户
     *
     * @param name 用户名
     * @return 用户对象 AbstractUser，如果不存在则返回 null
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

        return users.get(name.trim());
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

        AbstractUser temp = users.get(name.trim());
        if (temp != null) {
            String userPassword = temp.getPassword();
            if (userPassword != null && userPassword.equals(password.trim())) {
                return temp;
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

        return new ArrayList<>(users.values());
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
        if (!users.containsKey(name)) {
            System.err.println("更新失败：用户名不存在");
            return false;
        }
        users.put(name, user);
        return true;
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
        if (!users.containsKey(trimmedName)) {
            System.err.println("更新失败：用户名不存在 - " + trimmedName);
            return false;
        }

        AbstractUser updatedUser = createUserByRole(trimmedName, trimmedPassword, trimmedRole);
        if (updatedUser == null) {
            return false;
        }

        users.put(trimmedName, updatedUser);
        System.out.println("用户信息更新成功");
        return true;
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
        if (users.putIfAbsent(name, user) == null) {
            return true;
        } else {
            System.err.println("新增失败：用户已存在");
            return false;
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

        AbstractUser user = createUserByRole(trimmedName, trimmedPassword, trimmedRole);
        if (user == null) {
            return false;
        }

        if (users.putIfAbsent(trimmedName, user) == null) {
            System.out.println("用户新增成功：" + trimmedName);
            return true;
        } else {
            System.err.println("新增失败：用户已存在 - " + trimmedName);
            return false;
        }
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

        if (users.remove(name) != null) {
            return true;
        } else {
            System.err.println("删除失败：用户不存在");
            return false;
        }
    }

    /**
     * 通过档案号查找档案
     *
     * @param archiveId 档案号
     * @return 档案对象 Archive，如果不存在则返回 null
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

        return archives.get(archiveId.trim());
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
        if (archives.containsKey(trimmedArchiveId)) {
            System.err.println("新增失败：档案号已存在");
            return false;
        }

        // 创建档案对象并添加到存储中
        Archive archive = new Archive(trimmedArchiveId, creator.trim(), timestamp,
                description != null ? description.trim() : "",
                fileName.trim());

        archives.put(trimmedArchiveId, archive);
        return true;
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
        if (archives.containsKey(archiveId)) {
            System.err.println("新增失败：档案号已存在");
            return false;
        }

        // 创建档案对象并添加到存储中
        archives.put(archiveId, archive);
        return true;
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

        return new ArrayList<>(archives.values());
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

        if (archives.remove(archiveId.trim()) != null) {
            System.out.println("删除成功");
            return true;
        } else {
            System.err.println("删除失败：档案不存在");
            return false;
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
        if (!archives.containsKey(trimmedArchiveId)) {
            System.err.println("更新失败：档案号不存在");
            return false;
        }

        // 更新档案信息
        Archive archive = archives.get(trimmedArchiveId);
        archive.setCreator(creator.trim());
        archive.setTimestamp(timestamp);
        archive.setDescription(description != null ? description.trim() : "");
        archive.setFileName(fileName.trim());

        archives.put(trimmedArchiveId, archive);

        System.out.println("更新成功");
        return true;
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
        if (!archives.containsKey(archiveId)) {
            System.err.println("更新失败：档案号不存在");
            return false;
        }

        // 更新档案信息
        archives.put(archiveId, archive);

        System.out.println("更新成功");
        return true;
    }
}