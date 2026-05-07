import java.io.*;
import java.sql.SQLException;
import java.util.Scanner;

public class Operator extends AbstractUser{
    Operator(String name, String password, String role) {
        super(name, password, role);
    }

    @Override
    public void showMenu() {
        System.out.println("****欢迎进入档案录入人员菜单****");
        System.out.println("    1.上传档案    ");
        System.out.println("    2.下载档案    ");
        System.out.println("    3.文件列表    ");
        System.out.println("    4.修改密码    ");
        System.out.println("    5.退出    ");
        System.out.println("*************************");
        System.out.println("请选择菜单：");
    }

    //1.上传档案的方法
    public void Upload() throws IOException, SQLException {
        Scanner sc = new Scanner(System.in);
        System.out.println("****上传档案****");

        System.out.print("请输入档案号: ");
        String id = sc.nextLine();

        System.out.print("请输入档案描述: ");
        String description = sc.nextLine();

        System.out.print("请输入源文件名: ");
        String fileName = sc.nextLine();

        System.out.println("上传文件路径: ");
        String path = sc.nextLine();

        String sourcePath = path+"\\"+fileName;

        // 构建目标文件路径
        File targetFile = new File(archiveDir, fileName);

        FileInputStream fis = new FileInputStream(sourcePath);
        FileOutputStream fos = new FileOutputStream(targetFile);
        int bytesRead;
        byte[] buffer = new byte[1024];
        while ((bytesRead = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }
        fis.close();
        fos.close();

//        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(targetFile));
//        BufferedReader br = new BufferedReader(new FileReader(sourcePath));
//        String line;
//        while ((line = br.readLine()) != null) {
//            oos.writeObject(line);
//        }
//        br.close();
//        oos.close();

        // 保存档案信息到HashMap
        boolean result = DataProcessing.insertArchive(id, this.getName(), java.time.LocalDateTime.now(), description, fileName);

        if (result) {
            System.out.println("档案上传成功！");
            System.out.println("档案号: " + id);
            System.out.println("文件名: " + fileName);
            System.out.println("存储路径: " + targetFile.getAbsolutePath());
        } else {
            System.out.println("档案上传失败，可能是档案号已存在");
        }


    }

    //2.下载档案的方法
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

    //3.文件列表的方法
    public void FileList(){
        try {
            listAllArchives();
        } catch (SQLException e) {
            System.out.println("获取文件列表失败：" + e.getMessage());
        }
    }

    //4.修改密码的方法
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

    //5.退出的方法
    public void Exit(){
        System.out.println("退出成功");
    }


}
