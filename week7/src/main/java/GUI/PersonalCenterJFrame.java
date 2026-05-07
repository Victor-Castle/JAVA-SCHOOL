package GUI;

import common.AbstractUser;
import network.client.ClientDataProcessing;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class PersonalCenterJFrame extends JFrame {
    private AbstractUser user;

    public PersonalCenterJFrame(AbstractUser user) {
        this.user = user;
        initJFrame();
        initComponents();
    }

    private void initJFrame() {
        // 设置标题
        this.setTitle("个人中心");
        // 设置退出程序
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // 设置窗口大小
        this.setSize(400, 300);
        // 设置窗口居中显示
        this.setLocationRelativeTo(null);
        // 设置窗口可见
        this.setVisible(true);
        // 把图标设置为登录界面的图标
        ImageIcon icon = new ImageIcon("week4/111.jpg");
// 把图标设置为登录界面的图标
        this.setIconImage(icon.getImage());

    }

    private void initComponents() {
        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(5, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // 添加用户信息
        mainPanel.add(new JLabel("用户名："));
        JLabel usernameLabel = new JLabel(user.getName());
        mainPanel.add(usernameLabel);

        mainPanel.add(new JLabel("角色："));
        JLabel roleLabel = new JLabel(user.getRole());
        mainPanel.add(roleLabel);

        mainPanel.add(new JLabel("新密码："));
        JPasswordField passwordField = new JPasswordField();
        mainPanel.add(passwordField);

        // 添加修改密码按钮
        JButton changePasswordButton = new JButton("修改密码");
        changePasswordButton.setPreferredSize(new Dimension(150, 30));
        mainPanel.add(new JLabel());
        mainPanel.add(changePasswordButton);

        // 添加主面板到窗口
        this.add(mainPanel);
        // 重新绘制窗口
        this.revalidate();

        // 添加按钮事件
        changePasswordButton.addActionListener(e -> {
            String newPassword = new String(passwordField.getPassword()).trim();
            try {
                boolean success = user.changeSelfInfo(newPassword);
                if (success) {
                    JOptionPane.showMessageDialog(this, "密码修改成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "密码修改失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "密码修改失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}