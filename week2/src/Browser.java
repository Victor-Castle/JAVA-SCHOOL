import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Browser extends AbstractUser {
    public Browser(String name, String password, String role) {
        super(name, password, role);
    }

    @Override
    public void showMenu() {
        System.out.println("****欢迎进入档案浏览员菜单****");
        System.out.println("    1.下载文件    ");
        System.out.println("    2.文件列表    ");
        System.out.println("    3.修改密码    ");
        System.out.println("    4.退出    ");
        System.out.println("*************************");
        System.out.println("请选择菜单：");

    }

    //1.下载档案的方法
    public void Download(){
        Scanner sc = new Scanner(System.in);
        System.out.print("请输入档案号：");
        String id = sc.nextLine();
        try {
            boolean result = downloadFile(id);
            if (result) {
                System.out.println("下载完毕");
            } else {
                System.out.println("下载失败");
            }
        } catch (IOException e) {
            System.out.println("下载文件失败：" + e.getMessage());
        }
    }
    //2.文件列表的方法
    public void FileList(){
        try {
            showFileList();
        } catch (SQLException e) {
            System.out.println("获取文件列表失败：" + e.getMessage());
        }
    }
    //3.修改密码的方法
    public void ChangePassword(){
        Scanner sc = new Scanner(System.in);
        System.out.println("****修改密码****");
        System.out.println("请输入你要修改的人的名字：");
        String name = sc.next();
        AbstractUser user = null;
        try {
            user = DataProcessing.searchUser(name);
        } catch (SQLException e) {
            System.out.println("查询用户失败：" + e.getMessage());
            return;
        }
        if (user == null) {
            System.out.println("用户不存在");
            return;
        }else {
            System.out.println("请输入用户原密码：");
            String password = sc.nextLine();
            if (password.equals(user.getPassword())) {
                System.out.println("请输入新设置的密码:");
                String newPassword = sc.next();
                user.setPassword(newPassword);
                System.out.println("修改成功！");
            }
            else {
                System.out.println("原密码输入有误");
            }


        }

    }
    //4.退出的方法
    public void Exit(){
        try {
            DataProcessing.disconnectFromDataBase();
        } catch (SQLException e) {
            System.out.println("数据库断开连接失败：" + e.getMessage());
        }
        System.out.println("退出成功");
    }

}
