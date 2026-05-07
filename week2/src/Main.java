import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        //循环输出主菜单
        while(true){
            PrintMenu();
            int n = 0;
            try {
                n = sc.nextInt();
            } catch (java.util.InputMismatchException e) {
                System.out.println("输入错误，请输入数字选项！");
                sc.next(); // 清空输入缓冲区
                continue;
            }
            switch (n){
                case 1:
                    login();
                    break;
                case 2:
                    try {
                        DataProcessing.disconnectFromDataBase();
                    } catch (SQLException e) {
                        System.out.println("数据库断开连接失败：" + e.getMessage());
                    }
                    System.out.println("系统退出，谢谢使用");
                    System.exit(0);
                    break;
                default:
                    break;
            }
        }



    }
    //打印菜单界面
    public static void PrintMenu(){
        System.out.println("****欢迎进入档案系统****");
        System.out.println("        1.登录        ");
        System.out.println("        2.退出        ");
        System.out.println("*********************");
    }

    //登录界面判断
    public static void login() {
        Scanner sc = new Scanner(System.in);
        try {

            DataProcessing.connectToDatabase();
            System.out.println("请输入用户名：");
            String name = sc.next();
            System.out.println("请输入口令：");
            String password = sc.next();
            //没有查到人
            if (DataProcessing.searchUser(name, password) == null) {
                System.out.println("查找失败，没有此人");
                return;//返回主菜单
            } else {
                String role = DataProcessing.searchUser(name).getRole();
                if (role.equals("administrator")) {
                    Administrator u = (Administrator) DataProcessing.searchUser(name);
                    boolean exitFlag = false;
                    while (!exitFlag) {
                        u.showMenu();
                        String select = sc.next();
                        switch (select) {
                            case "1":
                                u.Insert();
                                break;
                            case "2":
                                u.Delete();
                                break;
                            case "3":
                                u.Update();
                                break;
                            case "4":
                                u.UserList();
                                break;
                            case "5":
                                u.Download();
                                break;
                            case "6":
                                u.FileList();
                                break;
                            case "7":
                                u.ChangePassword();
                                break;
                            case "8":
                                u.Exit();
                                exitFlag = true;
                                break;
                            default:
                                System.out.println("您的选择有误，请重新输入");
                                break;
                        }

                    }
                } else if (role.equals("operator")) {
                    Operator u = (Operator) DataProcessing.searchUser(name);
                    boolean exitFlag = false;
                    while (!exitFlag) {
                        u.showMenu();
                        String select = sc.next();
                        switch (select) {
                            case "1":
                                u.Upload();
                                break;
                            case "2":
                                u.Download();
                                break;
                            case "3":
                                u.FileList();
                                break;
                            case "4":
                                u.ChangePassword();
                                break;
                            case "5":
                                u.Exit();
                                exitFlag = true;
                                break;
                            default:
                                System.out.println("您的选择有误，请重新输入");
                                break;
                        }
                    }
                } else if (role.equals("browser")) {
                    Browser u = (Browser) DataProcessing.searchUser(name);
                    boolean exitFlag = false;
                    while (!exitFlag) {
                        u.showMenu();
                        String select = sc.next();
                        switch (select) {
                            case "1":
                                u.Download();
                                break;
                            case "2":
                                u.FileList();
                                break;
                            case "3":
                                u.ChangePassword();
                                break;
                            case "4":
                                u.Exit();
                                exitFlag = true;
                                break;
                            default:
                                System.out.println("您的选择有误，请重新输入");
                                break;
                        }
                    }
                } else {
                    System.out.println("您的角色不存在");
                    return;//返回主菜单
                }

            }

        }catch (SQLException e){
            System.out.println("数据库操作失败：" + e.getMessage());
            return;
        }finally {
            try {
                DataProcessing.disconnectFromDataBase();
            } catch (SQLException e) {
                System.out.println("数据库断开连接失败：" + e.getMessage());
            }
        }
    }
}