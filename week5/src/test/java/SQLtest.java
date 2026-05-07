import java.sql.*;

/**
 * SQL 测试类
 * 用于测试 MySQL 数据库连接和查询功能
 */
public class SQLtest {

    /**
     * 主方法 - 数据库连接测试入口
     * 演示如何连接 MySQL 数据库并查询用户表数据
     *
     * @param args 命令行参数，本方法不使用该参数
     */
    public static void main(String[] args) {
        // JDBC 驱动类和数据库连接 URL 配置
        String driverName = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/archive_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";

        String user="root";
        String password="123456";

        // 加载 JDBC 驱动
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC 驱动加载失败：" + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 定义查询所有用户信息的 SQL 语句
        String sql = "SELECT name, password, role FROM users";

        // 使用 try-with-resources 自动管理数据库资源
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql,
                     ResultSet.TYPE_SCROLL_INSENSITIVE,
                     ResultSet.CONCUR_READ_ONLY);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            // 统计查询到的记录数
            int count = 0;

            // 遍历结果集并输出用户信息
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String pwd = resultSet.getString("password");
                String role = resultSet.getString("role");

                // 检查字段完整性并输出记录
                if (name != null && pwd != null && role != null) {
                    System.out.println(name + ";" + pwd + ";" + role);
                    count++;
                } else {
                    System.err.println("警告：发现 null 值记录 - ID=" + resultSet.getInt("id"));
                }
            }

            // 输出查询结果统计信息
            if (count == 0) {
                System.out.println("用户表为空，无数据记录");
            } else {
                System.out.println("共查询到 " + count + " 条用户记录");
            }

        } catch (SQLException e) {
            // 捕获并处理数据库操作异常
            System.err.println("数据库操作失败：" + e.getMessage());
            System.err.println("SQL 状态码：" + e.getSQLState());
            System.err.println("错误代码：" + e.getErrorCode());
            e.printStackTrace();
        }
    }
}
