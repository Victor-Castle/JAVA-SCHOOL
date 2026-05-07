package common;

import java.io.Serializable;
import java.io.*;
import java.sql.SQLException;
import java.util.Collection;

/**
 * 用户抽象类
 * 定义了用户的基本属性和行为
 * 所有具体用户类型都继承自此类
 *
 * @author gongjing
 */
public abstract class AbstractUser implements Serializable {
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
     * 档案文件存储目录
     */
    public final String archiveDir="D:\\code\\JAVA2025\\JAVA-School\\archive_files";
    /**
     * 下载文件存储目录
     */
    public final String downloadDir="D:\\code\\JAVA2025\\JAVA-School\\download_files";
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
     * 该方法用于更新用户的密码信息，包含密码合法性验证和数据持久化操作
     *
     * @param password 新密码，需要满足最小长度要求且不能为空
     * @return boolean 修改是否成功，成功返回 true，失败返回 false
     * @throws SQLException 当数据库更新操作发生错误时抛出
     */
    public boolean changeSelfInfo(String password) throws SQLException{
        final String successMessage = "修改成功";
        final String failureMessage = "修改失败";
        final String invalidPasswordMessage = "密码不符合要求";
        final int minPasswordLength = 3;

        // 密码合法性校验：检查密码是否为空、是否满足最小长度要求
        if (password == null  || password.trim().isEmpty()|| password.length() < minPasswordLength) {
            System.err.println(invalidPasswordMessage);
            return false;
        }

        // 调用数据处理类更新用户信息
        if (network.client.ClientDataProcessing.updateUser(name, password, role)) {
            this.password = password;
            System.out.println(successMessage);
            return true;
        } else {
            System.err.println(failureMessage);
            return false;
        }
    }

    /**
     * 下载档案文件
     * 该方法根据档案号查找档案信息，并将档案文件从源目录下载到指定的目标路径
     *
     * @param archiveId 档案号，用于唯一标识要下载的档案
     * @param destPath 目标路径，文件将被下载到此目录
     * @return boolean 下载是否成功，成功返回 true，失败返回 false
     * @throws SQLException 当数据库查询发生错误时抛出
     * @throws IOException 当文件读写或 IO 操作发生错误时抛出
     */
    public boolean downloadArchive(String archiveId, String destPath) throws SQLException, IOException {
        final String failureMessage = "档案号不能为空";
        final int bufferSize = 8192;

        // 参数验证：检查档案号是否为空
        if (archiveId == null || archiveId.trim().isEmpty()) {
            System.err.println(failureMessage);
            return false;
        }

        // 参数验证：检查目标路径是否合法
        if (destPath == null || destPath.trim().isEmpty()) {
            System.err.println("目标路径不能为空");
            return false;
        }

        try {
            // 根据档案号查询档案信息
            Archive archive = network.client.ClientDataProcessing.searchArchive(archiveId.trim());
            if (archive == null) {
                System.err.println("下载失败：档案号不存在 - " + archiveId);
                return false;
            }

            // 显示档案详细信息
            System.out.println("\n=== 正在下载档案 ===");
            System.out.println("档案号：" + archive.getArchiveId());
            System.out.println("文件名：" + archive.getFileName());
            System.out.println("描述：" + archive.getDescription());
            System.out.println("创建者：" + archive.getCreator());

            System.out.println("档案源文件路径：" + archiveDir);
            System.out.println("目标文件路径：" + destPath);

            // 构建档案文件的完整路径
            File archiveFile = new File(archiveDir, archive.getFileName());

            // 安全检查：验证文件是否存在且为有效文件
            if (!archiveFile.exists() || !archiveFile.isFile()) {
                System.err.println("\n警告：档案文件不存在：" + archiveFile.getAbsolutePath());
                return false;
            }

            // 创建目标目录（如果不存在）
            File targetDir = new File(destPath);
            if (!targetDir.exists()) {
                if (!targetDir.mkdirs()) {
                    System.err.println("无法创建下载目录：" + targetDir);
                    return false;
                }
            }

            // 构建目标文件的完整路径
            File targetFile = new File(targetDir, archiveFile.getName());

            // 使用 try-with-resources 确保资源正确关闭
            // 通过缓冲流实现文件的高效复制
            byte[] buffer = new byte[bufferSize];
            BufferedReader br = new BufferedReader(new FileReader(archiveFile));
            BufferedWriter bw = new BufferedWriter(new FileWriter(targetFile));
            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
            }
            System.out.println("文件下载成功：" + targetFile.getAbsolutePath());
            bw.close();
            br.close();

            //反序列化
            return true;
        } catch (IOException e) {
            // 捕获并重新抛出 IO 异常
            System.err.println("下载过程中发生 IO 错误：" + e.getMessage());
            throw e;
        } catch (SQLException e) {
            // 捕获并重新抛出 SQL 异常
            System.err.println("数据库查询错误：" + e.getMessage());
            throw e;
        } catch (Exception e) {
            // 捕获未知异常并返回失败
            System.err.println("下载过程中发生未知错误：" + e.getMessage());
            return false;
        }
    }

    /**
     * 显示档案列表
     * 该方法从数据处理类获取所有档案信息，并展示在控制台
     * 包含档案号、创建者、文件名和描述等信息
     *
     * @throws SQLException 当数据库查询发生错误时抛出
     */
    public void listAllArchives() throws SQLException {
        System.out.println("\n========== 档案列表 ==========");

        try {
            // 从数据处理类获取所有档案信息
            Collection<Archive> allArchives = network.client.ClientDataProcessing.getAllArchives();

            // 处理空集合情况：没有档案记录时提前返回
            if (allArchives.isEmpty()) {
                System.out.println("当前没有档案记录");
                System.out.println("=============================\n");
                return;
            }

            // 输出档案总数和表头信息
            System.out.println("档案总数：" + allArchives.size());
            System.out.println("---------------------------------------------------------------");
            System.out.printf("%-10s %-10s %-20s %-20s%n", "档案号", "创建者", "文件名", "描述");
            System.out.println("---------------------------------------------------------------");

            // 遍历所有档案并格式化输出
            for (Archive archive : allArchives) {
                String description = archive.getDescription();
                // 截断过长的描述，保持格式整齐
                if (description.length() > 18) {
                    description = description.substring(0, 17) + "...";
                }

                System.out.printf("%-10s %-10s %-20s %-20s%n",
                        archive.getArchiveId(),
                        archive.getCreator(),
                        archive.getFileName(),
                        description);
            }

            // 输出结束分隔线
            System.out.println("---------------------------------------------------------------");
            System.out.println("=============================\n");

        } catch (SQLException e) {
            // 捕获并重新抛出 SQL 异常
            System.err.println("查询档案列表失败：" + e.getMessage());
            throw e;
        } catch (Exception e) {
            // 捕获未知异常并包装为 SQLException 抛出
            System.err.println("显示档案列表时发生未知错误：" + e.getMessage());
            throw new SQLException("显示档案列表失败", e);
        }
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