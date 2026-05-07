package GUI;

import common.AbstractUser;
import network.client.ClientDataProcessing;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class MainJFrame extends JFrame {
    private AbstractUser user;

    public MainJFrame(AbstractUser user) {
        this.user = user;
        initJFrame();
        initComponents();
    }

    private void initJFrame() {
        // 设置标题
        this.setTitle("档案管理系统");
        // 设置退出程序
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 设置窗口大小
        this.setSize(400, 300);
        // 设置窗口居中显示
        this.setLocationRelativeTo(null);
        // 设置窗口可见
        this.setVisible(true);
    }

    private void initComponents() {
        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(4, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // 添加用户管理按钮
        JButton userManageButton = new JButton("用户管理");
        userManageButton.setPreferredSize(new Dimension(200, 40));

        // 根据用户角色设置按钮状态
        if (!user.getRole().equals("administrator")) {
            userManageButton.setEnabled(false);
        }
        mainPanel.add(userManageButton);

        // 添加档案管理按钮
        JButton archiveManageButton = new JButton("档案管理");
        archiveManageButton.setPreferredSize(new Dimension(200, 40));
        // 根据用户角色设置按钮状态
        if (!user.getRole().equals("administrator") && !user.getRole().equals("operator")) {
            archiveManageButton.setEnabled(false);
        }
        mainPanel.add(archiveManageButton);

        // 添加个人中心按钮
        JButton personalCenterButton = new JButton("个人中心");
        personalCenterButton.setPreferredSize(new Dimension(200, 40));
        mainPanel.add(personalCenterButton);

        // 添加退出登录按钮
        JButton logoutButton = new JButton("退出登录");
        logoutButton.setPreferredSize(new Dimension(200, 40));
        mainPanel.add(logoutButton);

        // 添加主面板到窗口
        this.add(mainPanel);
        // 重新绘制窗口
        this.revalidate();

        // 添加按钮事件
        userManageButton.addActionListener(e -> {
            new UserManagementJFrame(user);
        });

        archiveManageButton.addActionListener(e -> {
            new ArchiveManagementJFrame(user);
        });

        personalCenterButton.addActionListener(e -> {
            new PersonalCenterJFrame(user);
        });

        logoutButton.addActionListener(e -> {
            try {
                ClientDataProcessing.disconnectFromDataBase();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            this.dispose();
            new LoginJFrame();
        });
    }
}