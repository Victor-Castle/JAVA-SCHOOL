import java.util.Scanner;

public class Browser extends User {
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
        System.out.println("下载中...");
        System.out.println("下载完毕");
    }
     //2.文件列表的方法
    public void FileList(){
        System.out.println("文件列表如下：");
    }
     //3.修改密码的方法
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
     //4.退出的方法
    public void Exit(){
        System.out.println("退出成功");
    }

}
