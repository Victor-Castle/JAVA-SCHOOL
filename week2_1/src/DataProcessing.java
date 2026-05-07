import java.sql.SQLException;
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
public  class DataProcessing {
    private static boolean connectToDB=false;
    static final double EXCEPTION_CONNECT_PROBABILITY=0.6;
    static final double EXCEPTION_DISCONNECT_PROBABILITY=1.0;

    final static String ROLE_ADMINISTRATOR = "administrator";
    final static String ROLE_OPERATOR = "operator";
    final static String ROLE_BROWSER = "browser";

    /**
     * 用户存储容器
     * 以用户名为键，AbstractUser 对象为值
     */
    private static  Map<String, AbstractUser> users = new HashMap<>();

    /**
     * 连接数据库
     *
     * @throws SQLException SQL 异常
     */
    public static  void connectToDatabase() throws SQLException{
        // 避免重复初始化
        if (connectToDB) {
            return;
        }

        double ranValue= Math.random();
        if (ranValue>EXCEPTION_CONNECT_PROBABILITY) {
            connectToDB = true;
            // 初始化系统默认用户（只在首次连接时初始化）
            if (users.isEmpty()) {
                users.put("jack", new Operator("jack", "123", ROLE_OPERATOR));
                users.put("rose", new Browser("rose", "123", ROLE_BROWSER));
                users.put("kate", new Administrator("kate", "123", ROLE_ADMINISTRATOR));
            }else {
                connectToDB = false;
                throw new SQLException("Not Connected to Database");
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
     * @param name 用户名
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
    public static Collection<AbstractUser> getAllUser() throws SQLException {
        if (!connectToDB) {
            throw new SQLException("Not Connected to Database");
        }

        return new ArrayList<>(users.values());
    }


    /**
     * 更新用户信息
     *
     * @param name 用户名
     * @param password 密码
     * @param role 角色
     * @return boolean 更新是否成功
     * @throws SQLException 数据库未连接异常
     */
    public static boolean updateUser(String name, String password, String role) throws SQLException {
        if (!connectToDB) {
            throw new SQLException("Not Connected to Database");
        }

        // 空值和格式检查
        if (name == null || name.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                role == null || role.trim().isEmpty()) {
            System.err.println("更新失败：用户名、密码或角色为空");
            return false;
        }

        String trimmedName = name.trim();
        String trimmedPassword = password.trim();
        String trimmedRole = role.trim();

        // 检查用户是否存在
        if (!users.containsKey(trimmedName)) {
            System.err.println("更新失败：用户名不存在");
            return false;
        }

        // 根据角色字符串生成对应的用户对象
        AbstractUser user;
        if (ROLE_ADMINISTRATOR.equalsIgnoreCase(trimmedRole)) {
            user = new Administrator(trimmedName, trimmedPassword, trimmedRole);
        } else if (ROLE_OPERATOR.equalsIgnoreCase(trimmedRole)) {
            user = new Operator(trimmedName, trimmedPassword, trimmedRole);
        } else if (ROLE_BROWSER.equalsIgnoreCase(trimmedRole)) {
            user = new Browser(trimmedName, trimmedPassword, trimmedRole);
        } else {
            System.err.println("更新失败：无效的角色");
            return false;
        }

        users.put(trimmedName, user);
        return true;
    }

    /**
     * 新增用户
     *
     * @param name 用户名
     * @param password 密码
     * @param role 用户角色
     * @return boolean 新增是否成功
     * @throws SQLException SQL 异常
     */
    public static boolean insertUser(String name, String password, String role) throws SQLException{
        if (!connectToDB) {
            throw new SQLException("Not Connected to Database");
        }

        // 根据角色字符串生成对应的用户对象
        AbstractUser user;
        if (ROLE_ADMINISTRATOR.equalsIgnoreCase(role)) {
            user = new Administrator(name, password, role);
        } else if (ROLE_OPERATOR.equalsIgnoreCase(role)) {
            user = new Operator(name, password, role);
        } else if (ROLE_BROWSER.equalsIgnoreCase(role)) {
            user = new Browser(name, password, role);
        } else {
            System.err.println("新增失败：无效的角色");
            return false;
        }

        if (users.putIfAbsent(name, user) == null) {
            return true;
        } else {
            System.err.println("新增失败：用户已存在");
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
    public static boolean deleteUser(String name) throws SQLException{
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
}