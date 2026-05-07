package GUI;

import common.AbstractUser;
import common.Archive;
import common.DataProcessing;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

public class ArchiveManagementJFrame extends JFrame {
    private AbstractUser user;
    private JTable archiveTable;
    private DefaultTableModel tableModel;

    public ArchiveManagementJFrame(AbstractUser user) {
        this.user = user;
        initJFrame();
        initComponents();
        loadArchiveData();
    }

    private void initJFrame() {
        // 设置标题
        this.setTitle("档案管理");
        // 设置退出程序
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // 设置窗口大小
        this.setSize(800, 500);
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
        String[] columnNames = {"档案号", "创建者", "时间", "文件名", "描述"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可编辑
            }
        };

        // 创建表格
        archiveTable = new JTable(tableModel);
        archiveTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(archiveTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton queryButton = new JButton("查询");
        JButton uploadButton = new JButton("上传");
        JButton downloadButton = new JButton("下载");

        queryButton.setPreferredSize(new Dimension(100, 30));
        uploadButton.setPreferredSize(new Dimension(100, 30));
        downloadButton.setPreferredSize(new Dimension(100, 30));

        buttonPanel.add(queryButton);
        buttonPanel.add(uploadButton);
        buttonPanel.add(downloadButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 添加主面板到窗口
        this.add(mainPanel);
        // 重新绘制窗口
        this.revalidate();

        // 添加按钮事件
        queryButton.addActionListener(e -> {
            String archiveId = JOptionPane.showInputDialog(this, "请输入档案号：");
            if (archiveId != null && !archiveId.trim().isEmpty()) {
                try {
                    Archive archive = DataProcessing.searchArchive(archiveId);
                    if (archive != null) {
                        // 清空表格
                        tableModel.setRowCount(0);
                        // 添加查询结果
                        addArchiveToTable(archive);
                    } else {
                        JOptionPane.showMessageDialog(this, "未找到该档案", "查询结果", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "查询失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        uploadButton.addActionListener(e -> {
            // 检查用户权限
            if (!user.getRole().equals("administrator") && !user.getRole().equals("operator")) {
                JOptionPane.showMessageDialog(this, "您没有权限上传档案", "权限不足", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 创建文件选择器
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return; // 用户取消选择
            }

            java.io.File selectedFile = fileChooser.getSelectedFile();

            // 创建上传对话框
            JDialog uploadDialog = new JDialog(this, "上传档案", true);
            uploadDialog.setSize(400, 300);
            uploadDialog.setLocationRelativeTo(this);

            JPanel uploadPanel = new JPanel();
            uploadPanel.setLayout(new GridLayout(5, 2, 10, 10));
            uploadPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            uploadPanel.add(new JLabel("档案号："));
            JTextField archiveIdField = new JTextField();
            uploadPanel.add(archiveIdField);

            uploadPanel.add(new JLabel("文件名："));
            JTextField fileNameField = new JTextField(selectedFile.getName());
            uploadPanel.add(fileNameField);

            uploadPanel.add(new JLabel("描述："));
            JTextField descriptionField = new JTextField();
            uploadPanel.add(descriptionField);

            JButton confirmButton = new JButton("确认上传");
            JButton cancelButton = new JButton("取消");

            JPanel buttonPanel2 = new JPanel();
            buttonPanel2.add(confirmButton);
            buttonPanel2.add(cancelButton);

            uploadPanel.add(new JLabel());
            uploadPanel.add(buttonPanel2);

            uploadDialog.add(uploadPanel);

            confirmButton.addActionListener(evt -> {
                String archiveId = archiveIdField.getText().trim();
                String fileName = fileNameField.getText().trim();
                String description = descriptionField.getText().trim();

                if (archiveId.isEmpty() || fileName.isEmpty()) {
                    JOptionPane.showMessageDialog(uploadDialog, "档案号和文件名不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    // 1. 将文件复制到档案目录
                    java.io.File archiveDir = new java.io.File(user.archiveDir);
                    if (!archiveDir.exists()) {
                        archiveDir.mkdirs();
                    }
                    java.io.File destFile = new java.io.File(archiveDir, fileName);

                    // 复制文件
                    java.nio.file.Files.copy(selectedFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

// 设置文件的修改时间为当前时间
                    java.nio.file.Files.setLastModifiedTime(destFile.toPath(), java.nio.file.attribute.FileTime.from(java.time.Instant.now()));

// 添加档案信息到系统
                    boolean success = DataProcessing.insertArchive(archiveId, user.getName(), LocalDateTime.now(), description, fileName);                    if (success) {
                        JOptionPane.showMessageDialog(uploadDialog, "上传成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                        uploadDialog.dispose();
                        loadArchiveData(); // 重新加载档案数据
                    } else {
                        // 如果添加档案信息失败，删除已复制的文件
                        destFile.delete();
                        JOptionPane.showMessageDialog(uploadDialog, "上传失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(uploadDialog, "上传失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } catch (java.io.IOException ex) {
                    JOptionPane.showMessageDialog(uploadDialog, "文件上传失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            });

            cancelButton.addActionListener(evt -> uploadDialog.dispose());

            uploadDialog.setVisible(true);
        });

        downloadButton.addActionListener(e -> {
            int selectedRow = archiveTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择要下载的档案", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String archiveId = (String) tableModel.getValueAt(selectedRow, 0);
            try {
                // 调用用户的downloadArchive方法
                boolean success = user.downloadArchive(archiveId, user.downloadDir);
                if (success) {
                    JOptionPane.showMessageDialog(this, "下载成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "下载失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "下载失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void loadArchiveData() {
        try {
            // 清空表格
            tableModel.setRowCount(0);

            // 获取所有档案
            Collection<Archive> archives = DataProcessing.getAllArchives();

            // 添加档案到表格
            for (Archive archive : archives) {
                addArchiveToTable(archive);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "加载档案失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addArchiveToTable(Archive archive) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String timestamp = archive.getTimestamp().format(formatter);

        Object[] rowData = {
                archive.getArchiveId(),
                archive.getCreator(),
                timestamp,
                archive.getFileName(),
                archive.getDescription()
        };

        tableModel.addRow(rowData);
    }
}