import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Scanner;

public class Administrator extends AbstractUser{
    public Administrator(String name, String password, String role) {
        super(name, password, role);
    }



    @Override
    public void showMenu() {
        System.out.println("****欢迎进入系统管理员菜单****");
        System.out.println("    1.新增用户    ");
        System.out.println("    2.删除用户    ");
        System.out.println("    3.修改用户    ");
        System.out.println("    4.用户列表    ");
        System.out.println("    5.下载档案    ");
        System.out.println("    6.档案列表    ");
        System.out.println("    7.修改密码    ");
        System.out.println("    8.退出    ");
        System.out.println("*************************");
        System.out.println("请选择菜单：");
    }

    //1.新增用户的方法
    public void Insert(){
        Scanner sc = new Scanner(System.in);

        System.out.println("****新增用户****");
        System.out.print("请输入用户名：");
        String name = sc.nextLine();

        System.out.print("请输入密码：");
        String password = sc.nextLine();

        System.out.print("请输入数字选择职责：1:administrator,2:operator,3:browser");
        String role = sc.nextLine();
        switch(role){
            case "1":
                role = "administrator";
                break;
            case "2":
                role = "operator";
                break;
            case "3":
                role = "browser";
                break;
            default:
        }

        // 调用DataProcessing的insert方法来添加用户
        boolean result = false;
        try {
            result = DataProcessing.insertUser(name, password, role);
        } catch (SQLException e) {
            System.out.println("新增用户失败：" + e.getMessage());
        }


        //让报错出现在下一次菜单出现之前
        // 清空可能存在的错误信息缓冲区
        sc.nextLine();
        System.out.println();

        if (result) {
            System.out.println("新增用户成功！");
        } else {
            return;
        }

    }
    //2.删除用户的方法
    public void Delete(){

        Scanner sc = new Scanner(System.in);
        System.out.println("****删除用户****");
        System.out.print("请输入要删除的用户名：");
        String name = sc.nextLine();

        // 调用DataProcessing的delete方法来删除用户
        boolean result = false;
        try {
            result = DataProcessing.deleteUser(name);
        } catch (SQLException e) {
            System.out.println("删除用户失败：" + e.getMessage());
        }

        // 清空可能存在的错误信息缓冲区
        sc.nextLine();
        System.out.println();

        if (result) {
            System.out.println("删除用户成功！");
        } else {
            return;
        }
    }

    //3.修改用户的方法
    public void Update(){
        Scanner sc = new Scanner(System.in);
        System.out.println("****更改用户****");
        System.out.println("请输入你要修改的人的名字:");
        String name = sc.next();
        System.out.println("请输入你要修改的人的口令:");
        String password = sc.next();
        System.out.println("请输入你要修改的人的职业:");
        String role = sc.next();
        boolean result = false;
        try {
            result = DataProcessing.updateUser(name, password, role);
        } catch (SQLException e) {
            System.out.println("修改用户失败：" + e.getMessage());
        }

        // 清空可能存在的错误信息缓冲区
        sc.nextLine();
        System.out.println();

        if (result) {
            System.out.println("修改用户成功！");
        }else {
            return;
        }
    }

    //4.用户列表的方法
    public void UserList(){
        System.out.println("****用户列表****");
        System.out.println("用户名\t口令\t\t角色");
        System.out.println("----------------------");

        // 获取所有用户并遍历输出
        Collection<AbstractUser> users = null;
        try {
            users = DataProcessing.getAllUsers();
        } catch (SQLException e) {
            System.out.println("获取用户列表失败：" + e.getMessage());
            return;
        }
        for (AbstractUser user : users) {
            System.out.println(user.getName() + "\t" + user.getPassword() + "\t\t" + user.getRole());
        }

        System.out.println("----------------------");
        System.out.println("用户列表显示完毕");
    }

    //5.下载档案的方法
    public void Download() throws SQLException {

        Scanner sc = new Scanner(System.in);
        System.out.print("请输入档案号：");
        String id = sc.nextLine();
        try {
            boolean result = downloadArchive(id, this.downloadDir);
            if (result) {
                System.out.println("下载完毕");
            } else {
                System.out.println("下载失败");
            }
        } catch (IOException e) {
            System.out.println("下载文件失败：" + e.getMessage());
        }

    }

    //6.档案列表的方法
    public void FileList(){
        try {
            listAllArchives();
        } catch (SQLException e) {
            System.out.println("获取文件列表失败：" + e.getMessage());
        }
    }

    //7.修改密码的方法
    public void ChangePassword(){
        Scanner sc = new Scanner(System.in);
        System.out.println("****修改密码****");
        System.out.println("请输入新密码；");
        try {
            changeSelfInfo(sc.nextLine());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //8.退出的方法
    public void Exit(){

        System.out.println("退出成功");

    }

}
