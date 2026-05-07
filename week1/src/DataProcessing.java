import java.util.*;

/**
 * 用户数据处理
 * 用户数据的增删改查
 */
public  class DataProcessing {

    final static String ROLE_ADMINISTRATOR = "administrator";
    final static String ROLE_OPERATOR = "operator";
    final static String ROLE_BROWSER = "browser";

    /**
     * 用户存储容器
     * 以用户名为键，User对象为值
     */
    private static final Map<String, User> users = new HashMap<>();

    /**
     * 静态初始化
     * 初始化系统默认用户
     */
    static {
        users.put("jack", new Operator("jack","123",ROLE_OPERATOR));		// 初始化操作员用户
        users.put("rose", new Browser("rose","123",ROLE_BROWSER));		// 初始化浏览器用户
        users.put("kate", new Administrator("kate","123",ROLE_ADMINISTRATOR));		// 初始化管理员用户
    }

    /**
     * 通过用户名查询用户
     * @param name 用户名
     * @return 用户对象，如果不存在则返回null
     */
    public static User searchUser(String name) {
        // 空值检查
        if (name == null) {
            System.err.println("查询失败：用户名为空");
            return null;
        }

        User user = users.get(name);
        return user;
    }

    /**
     * 通过用户名和密码查询用户，用于登录验证
     * @param name 用户名
     * @param password 密码
     * @return 验证成功返回用户对象，验证失败返回null
     */
    public static User search(String name, String password) {
        // 空值检查
        if (name == null || password == null) {
            System.err.println("登录失败：用户名或密码为空");
            return null;
        }

        User temp = users.get(name);
        if (temp != null && (temp.getPassword()).equals(password)) {
            return temp;
        }

        // 验证失败返回 null
        return null;
    }

    /**
     * 获取所有用户
     * @return 用户对象的集合
     */
    public static Collection<User> getAllUser() {
        return users.values();
    }

    /**
     * 更新用户信息
     * @param name 用户名
     * @param password 密码
     * @param role 用户角色
     * @return 更新是否成功
     */
    public static boolean update(String name, String password, String role) {
        // 空值检查
        if (name == null || password == null || role == null) {
            System.err.println("更新失败：用户名、密码或角色为空");
            return false;
        }

        // 根据角色字符串生成对应的用户对象
        User user;
        if (!users.containsKey(name)) {
            System.err.println("更新失败：用户名不存在");
            return false;
        }else {
            if (ROLE_ADMINISTRATOR.equalsIgnoreCase(role)) {
                user = new Administrator(name, password, role);
            } else if (ROLE_OPERATOR.equalsIgnoreCase(role)) {
                user = new Operator(name, password, role);
            } else if (ROLE_BROWSER.equalsIgnoreCase(role)) {
                user = new Browser(name, password, role);
            } else {
                System.err.println("更新失败：无效的角色");
                return false;
            }

            users.put(name, user);
            return true;
        }
    }

    /**
     * 新增用户
     * @param name 用户名
     * @param password 密码
     * @param role 用户角色
     * @return 新增是否成功
     */
    public static boolean insert(String name, String password, String role) {
        // 空值检查
        if (name == null || password == null || role == null) {
            System.err.println("新增失败：用户名、密码或角色为空");
            return false;
        }

        // 根据角色字符串生成对应的用户对象
        User user;
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
     * @param name 用户名
     * @return 删除是否成功
     */
    public static boolean delete(String name) {
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
}
