import java.io.IOException;
import java.sql.SQLException;

/**
 * 用户抽象类
 * 定义了用户的基本属性和行为
 * 所有具体用户类型都继承自此类
 *
 * @author gongjing
 */
public abstract class AbstractUser {
    /**
     * 用户名
     */
    private String name;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 用户角色
     */
    private String role;

    /**
     * 构造方法
     *
     * @param name 用户名
     * @param password 用户密码
     * @param role 用户角色
     */
    AbstractUser(String name, String password, String role){
        this.name=name;
        this.password=password;
        this.role=role;
    }

    /**
     * 修改用户个人信息
     *
     * @param password 新密码
     * @return boolean 修改是否成功
     * @throws SQLException SQL 异常
     */
    public boolean changeSelfInfo(String password) throws SQLException{
        // 定义常量以提高可维护性
        final String successMessage = "修改成功";
        final String failureMessage = "修改失败";
        final String invalidPasswordMessage = "密码不符合要求";
        final int minPasswordLength = 3;
        // 密码合法性校验
        if (password == null  || password.trim().isEmpty()|| password.length() < minPasswordLength) {
            System.err.println(invalidPasswordMessage);
            return false;
        }

        // 写用户信息到存储
        try{
        if (DataProcessing.updateUser(name, password, role)) {
            this.password = password;
            System.out.println(successMessage);
            return true;
        } else {
            System.err.println(failureMessage);
            return false;
        }
        }catch (SQLException e){
            System.out.println("修改用户信息失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 下载文件
     *
     * @param id 档案号
     * @return boolean 下载是否成功
     * @throws IOException IO 异常
     */
    public boolean downloadFile(String id) throws IOException{
        final String failureMessage = "文件 ID 不能为空";

        // 参数验证
        if (id == null || id.trim().isEmpty()) {
            System.err.println(failureMessage);
            return false;
        }

        // TODO: 实现实际的文件下载逻辑
        System.out.println("下载文件... ...");

        return true;
    }

    /**
     * 显示文件列表
     *
     * @throws SQLException SQL 异常
     */
    public void showFileList() throws SQLException{
        // TODO: 实现实际的文件列表查询和显示逻辑
        System.out.println("列表... ...");
    }

    /**
     * 显示用户菜单
     * 抽象方法，由具体子类实现
     *
     */
    public abstract void showMenu();

    /**
     * 退出系统
     *
     */
    public void exitSystem(){
        // TODO: 添加资源清理逻辑
        System.out.println("系统退出, 谢谢使用 ! ");
        System.exit(0);
    }

    /**
     * 获取用户名
     * @return 用户名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置用户名
     * @param name 用户名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取用户密码
     * @return 用户密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置用户密码
     * @param password 用户密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取用户角色
     * @return 用户角色
     */
    public String getRole() {
        return role;
    }

    /**
     * 设置用户角色
     * @param role 用户角色
     */
    public void setRole(String role) {
        this.role = role;
    }
}
