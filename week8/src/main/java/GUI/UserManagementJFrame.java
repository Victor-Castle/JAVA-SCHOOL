package GUI;

import common.AbstractUser;
import network.client.ClientDataProcessing;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Collection;

public class UserManagementJFrame extends JFrame {
    private AbstractUser currentUser;
    private JTable userTable;
    private DefaultTableModel tableModel;

    public UserManagementJFrame(AbstractUser user) {
        this.currentUser = user;
        initJFrame();
        initComponents();
        loadUserData();
    }

    private void initJFrame() {
        // 设置标题
        this.setTitle("用户管理");
        // 设置退出程序
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // 设置窗口大小
        this.setSize(600, 400);
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
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建表格模型
        String[] columnNames = {"用户名", "密码", "角色"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可编辑
            }
        };

        // 创建表格
        userTable = new JTable(tableModel);
        userTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(userTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton queryButton = new JButton("查询");
        JButton addButton = new JButton("新增");
        JButton modifyButton = new JButton("修改");
        JButton deleteButton = new JButton("删除");

        queryButton.setPreferredSize(new Dimension(100, 30));
        addButton.setPreferredSize(new Dimension(100, 30));
        modifyButton.setPreferredSize(new Dimension(100, 30));
        deleteButton.setPreferredSize(new Dimension(100, 30));

        buttonPanel.add(queryButton);
        buttonPanel.add(addButton);
        buttonPanel.add(modifyButton);
        buttonPanel.add(deleteButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 添加主面板到窗口
        this.add(mainPanel);
        // 重新绘制窗口
        this.revalidate();

        // 添加按钮事件
        queryButton.addActionListener(e -> showQueryDialog());
        addButton.addActionListener(e -> showAddDialog());
        modifyButton.addActionListener(e -> showModifyDialog());
        deleteButton.addActionListener(e -> showDeleteDialog());
    }

    private void loadUserData() {
        try {
            // 清空表格
            tableModel.setRowCount(0);

            // 获取所有用户
            Collection<AbstractUser> users = ClientDataProcessing.getAllUsers();

            // 添加用户到表格
            for (AbstractUser user : users) {
                Object[] rowData = {
                        user.getName(),
                        user.getPassword(),
                        user.getRole()
                };
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "加载用户数据失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showQueryDialog() {
        // 创建查询对话框
        JDialog queryDialog = new JDialog(this, "查询用户", true);
        queryDialog.setSize(300, 150);
        queryDialog.setLocationRelativeTo(this);

        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new GridLayout(2, 2, 10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        dialogPanel.add(new JLabel("用户名："));
        JTextField usernameField = new JTextField();
        dialogPanel.add(usernameField);

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialogPanel.add(new JLabel());
        dialogPanel.add(buttonPanel);

        queryDialog.add(dialogPanel);

        okButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (!username.isEmpty()) {
                try {
                    AbstractUser user = ClientDataProcessing.searchUser(username);
                    if (user != null) {
                        // 查找表格中对应的行
                        int rowIndex = -1;
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            if (tableModel.getValueAt(i, 0).equals(user.getName())) {
                                rowIndex = i;
                                break;
                            }
                        }

                        if (rowIndex != -1) {
                            // 选中该行
                            userTable.setRowSelectionInterval(rowIndex, rowIndex);
                            // 滚动到该行
                            userTable.scrollRectToVisible(userTable.getCellRect(rowIndex, 0, true));
                        } else {
                            // 如果表格中没有，添加到表格并选中
                            Object[] rowData = {
                                    user.getName(),
                                    user.getPassword(),
                                    user.getRole()
                            };
                            tableModel.addRow(rowData);
                            rowIndex = tableModel.getRowCount() - 1;
                            userTable.setRowSelectionInterval(rowIndex, rowIndex);
                        }
                    } else {
                        JOptionPane.showMessageDialog(queryDialog, "未找到！", "消息", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(queryDialog, "查询失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
            queryDialog.dispose();
        });

        cancelButton.addActionListener(e -> queryDialog.dispose());

        queryDialog.setVisible(true);
    }

    private void showAddDialog() {
        // 创建新增对话框
        JDialog addDialog = new JDialog(this, "新增用户", true);
        addDialog.setSize(400, 250);
        addDialog.setLocationRelativeTo(this);

        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new GridLayout(4, 2, 10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        dialogPanel.add(new JLabel("用户名："));
        JTextField usernameField = new JTextField();
        dialogPanel.add(usernameField);

        dialogPanel.add(new JLabel("密码："));
        JPasswordField passwordField = new JPasswordField();
        dialogPanel.add(passwordField);

        dialogPanel.add(new JLabel("角色："));
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"administrator", "operator", "browser"});
        dialogPanel.add(roleComboBox);

        JButton okButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialogPanel.add(new JLabel());
        dialogPanel.add(buttonPanel);

        addDialog.add(dialogPanel);

        okButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String role = (String) roleComboBox.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, "用户名和密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                boolean success = ClientDataProcessing.insertUser(username, password, role);
                if (success) {
                    JOptionPane.showMessageDialog(addDialog, "新增成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    addDialog.dispose();
                    loadUserData(); // 重新加载用户数据
                } else {
                    JOptionPane.showMessageDialog(addDialog, "新增失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(addDialog, "新增失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> addDialog.dispose());

        addDialog.setVisible(true);
    }

    private void showModifyDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要修改的用户", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String username = (String) tableModel.getValueAt(selectedRow, 0);
        String password = (String) tableModel.getValueAt(selectedRow, 1);
        String role = (String) tableModel.getValueAt(selectedRow, 2);

        // 创建修改对话框
        JDialog modifyDialog = new JDialog(this, "修改用户", true);
        modifyDialog.setSize(400, 250);
        modifyDialog.setLocationRelativeTo(this);

        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new GridLayout(4, 2, 10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        dialogPanel.add(new JLabel("用户名："));
        JTextField usernameField = new JTextField(username);
        usernameField.setEditable(false); // 用户名不可修改
        dialogPanel.add(usernameField);

        dialogPanel.add(new JLabel("密码："));
        JPasswordField passwordField = new JPasswordField(password);
        dialogPanel.add(passwordField);

        dialogPanel.add(new JLabel("角色："));
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"administrator", "operator", "browser"});
        roleComboBox.setSelectedItem(role);
        dialogPanel.add(roleComboBox);

        JButton okButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialogPanel.add(new JLabel());
        dialogPanel.add(buttonPanel);

        modifyDialog.add(dialogPanel);

        okButton.addActionListener(e -> {
            String newPassword = new String(passwordField.getPassword()).trim();
            String newRole = (String) roleComboBox.getSelectedItem();

            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(modifyDialog, "密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                boolean success = ClientDataProcessing.updateUser(username, newPassword, newRole);
                if (success) {
                    JOptionPane.showMessageDialog(modifyDialog, "修改成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    modifyDialog.dispose();
                    loadUserData(); // 重新加载用户数据
                } else {
                    JOptionPane.showMessageDialog(modifyDialog, "修改失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(modifyDialog, "修改失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> modifyDialog.dispose());

        modifyDialog.setVisible(true);
    }

    private void showDeleteDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的用户", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String username = (String) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "确定要删除用户 " + username + " 吗？", "确认删除", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = ClientDataProcessing.deleteUser(username);
                if (success) {
                    JOptionPane.showMessageDialog(this, "删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadUserData(); // 重新加载用户数据
                } else {
                    JOptionPane.showMessageDialog(this, "删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "删除失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}