import java.util.Collection;
import java.util.Scanner;

public class Administrator extends User{
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
        String name = sc.next();

        System.out.print("请输入密码：");
        String password = sc.next();

        System.out.print("请输入数字选择职责：1:administrator,2:operator,3:browser");
        String role = sc.next();
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
        boolean result = DataProcessing.insert(name, password, role);


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
        String name = sc.next();

        // 调用DataProcessing的delete方法来删除用户
        boolean result = DataProcessing.delete(name);

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
        boolean result = DataProcessing.update(name, password, role);

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
        Collection<User> users = DataProcessing.getAllUser();
        for (User user : users) {
            System.out.println(user.getName() + "\t" + user.getPassword() + "\t\t" + user.getRole());
        }

        System.out.println("----------------------");
        System.out.println("用户列表显示完毕");
    }

     //5.下载档案的方法
    public void Download(){
        System.out.println("下载中...");
        System.out.println("下载完毕");
    }

     //6.档案列表的方法
    public void FileList(){
        System.out.println("档案如下：");
    }

    //7.修改密码的方法
    public void ChangePassword(){
        Scanner sc = new Scanner(System.in);
        System.out.println("****修改密码****");
        System.out.println("请输入你要修改的人的名字：");
        String name = sc.next();
        User user = DataProcessing.searchUser(name);
        if (user == null) {
            System.out.println("用户不存在");
            return;
        }else {
            System.out.println("请输入用户原密码：");
            String password = sc.next();
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

     //8.退出的方法
    public void Exit(){
        System.out.println("退出成功");

    }

}
