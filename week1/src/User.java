
/**
 * 用户抽象类
 * 定义了用户的基本属性和行为
 * 所有具体用户类型都继承自此类
 */
public abstract class User {
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
     * @param name 用户名
     * @param password 用户密码
     * @param role 用户角色
     */
    User(String name,String password,String role){
        this.name=name;
        this.password=password;
        this.role=role;
    }

    /**
     * 修改用户个人信息
     * @param password 新密码
     * @return 修改是否成功
     */
    public boolean changeSelfInfo(String password) {
        // 定义常量以提高可维护性
        final String SUCCESS_MESSAGE = "修改成功";
        final String FAILURE_MESSAGE = "修改失败";
        final String INVALID_PASSWORD_MESSAGE = "密码不符合要求";

        // 密码合法性校验
        if (password == null || password.length() < 3) {
            System.err.println(INVALID_PASSWORD_MESSAGE);
            return false;
        }

        // 写用户信息到存储
        if (DataProcessing.update(name, password, role)) {
            this.password = password;
            System.out.println(SUCCESS_MESSAGE);
            return true;
        } else {
            System.err.println(FAILURE_MESSAGE);
            return false;
        }
    }

    /**
     * 显示用户菜单
     * 抽象方法，由具体子类实现
     */
    public abstract void showMenu();

    /**
     * 下载文件
     * @param id 档案号
     * @return 下载是否成功
     */
    public boolean downloadFile(String id){
        System.out.println("下载文件... ...");
        return true;
    }

    /**
     * 显示文件列表
     */
    public void showFileList(){
        System.out.println("列表... ...");
    }

    /**
     * 退出系统
     */
    public void exitSystem(){
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