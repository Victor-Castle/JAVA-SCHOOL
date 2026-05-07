package GUI;

import common.AbstractUser;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class PersonalCenterJFrame extends JFrame {
    private AbstractUser user;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;

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
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建信息面板
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(3, 2, 10, 15));

        // 添加用户名标签和文本框
        infoPanel.add(new JLabel("用户名："));
        usernameField = new JTextField(user.getName());
        usernameField.setEditable(false); // 用户名不可编辑
        infoPanel.add(usernameField);

        // 添加密码标签和密码框
        infoPanel.add(new JLabel("密码："));
        String password = user.getPassword();
        String passwordStars = "";
        for (int i = 0; i < password.length(); i++) {
            passwordStars += "*";
        }
        passwordField = new JPasswordField(passwordStars);
        passwordField.setEditable(false); // 密码不可编辑，只用于显示
        infoPanel.add(passwordField);

        // 添加角色标签和下拉框
        infoPanel.add(new JLabel("角色："));
        roleComboBox = new JComboBox<>(new String[]{"档案管理员", "档案操作员", "档案浏览人员"});
        // 根据用户角色设置下拉框选中项
        String roleText = "";
        switch (user.getRole()) {
            case "administrator":
                roleText = "档案管理员";
                break;
            case "operator":
                roleText = "档案操作员";
                break;
            case "browser":
                roleText = "档案浏览人员";
                break;
        }
        roleComboBox.setSelectedItem(roleText);
        roleComboBox.setEnabled(false); // 角色不可编辑
        infoPanel.add(roleComboBox);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));
        JButton changePasswordButton = new JButton("修改密码");
        JButton closeButton = new JButton("关闭窗口");
        changePasswordButton.setPreferredSize(new Dimension(120, 30));
        closeButton.setPreferredSize(new Dimension(120, 30));
        buttonPanel.add(changePasswordButton);
        buttonPanel.add(closeButton);

        // 添加组件到主面板
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 添加主面板到窗口
        this.add(mainPanel);
        // 重新绘制窗口
        this.revalidate();

        // 添加按钮事件
        changePasswordButton.addActionListener(e -> showChangePasswordDialog());

        closeButton.addActionListener(e -> this.dispose());
    }

    private void showChangePasswordDialog() {
        // 创建修改密码对话框
        JDialog changePasswordDialog = new JDialog(this, "修改密码", true);
        changePasswordDialog.setSize(400, 250);
        changePasswordDialog.setLocationRelativeTo(this);

        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new BorderLayout(10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建输入面板
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2, 10, 15));

        // 添加原密码标签和密码框
        inputPanel.add(new JLabel("原密码："));
        JPasswordField oldPasswordField = new JPasswordField();
        inputPanel.add(oldPasswordField);

        // 添加新密码标签和密码框
        inputPanel.add(new JLabel("新密码："));
        JPasswordField newPasswordField = new JPasswordField();
        inputPanel.add(newPasswordField);

        // 添加确认密码标签和密码框
        inputPanel.add(new JLabel("确认密码："));
        JPasswordField confirmPasswordField = new JPasswordField();
        inputPanel.add(confirmPasswordField);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));
        JButton confirmButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        confirmButton.setPreferredSize(new Dimension(100, 30));
        cancelButton.setPreferredSize(new Dimension(100, 30));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        // 添加组件到对话框面板
        dialogPanel.add(inputPanel, BorderLayout.CENTER);
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

        changePasswordDialog.add(dialogPanel);

        // 添加按钮事件
        confirmButton.addActionListener(e -> {
            String oldPassword = new String(oldPasswordField.getPassword()).trim();
            String newPassword = new String(newPasswordField.getPassword()).trim();
            String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

            // 验证原密码
            if (!oldPassword.equals(user.getPassword())) {
                JOptionPane.showMessageDialog(changePasswordDialog, "原密码错误", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 验证新密码
            if (newPassword.isEmpty() || newPassword.length() < 3) {
                JOptionPane.showMessageDialog(changePasswordDialog, "新密码长度不能少于3个字符", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 验证确认密码
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(changePasswordDialog, "两次输入的密码不一致", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 修改密码
            try {
                boolean success = user.changeSelfInfo(newPassword);
                if (success) {
                    JOptionPane.showMessageDialog(changePasswordDialog, "密码修改成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    changePasswordDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(changePasswordDialog, "密码修改失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(changePasswordDialog, "密码修改失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> changePasswordDialog.dispose());

        changePasswordDialog.setVisible(true);
    }
}