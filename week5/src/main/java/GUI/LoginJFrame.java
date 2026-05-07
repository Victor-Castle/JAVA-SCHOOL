package GUI;

import common.AbstractUser;
import common.DataProcessing;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class LoginJFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;



    public LoginJFrame() {
        //初始化主题布局
        initJFrame();
        //初始化组件
        initComponents();
        //初始化事件监听器
        initListeners();


    }


    private void initListeners() {
        // 添加登录按钮事件
        loginButton.addActionListener(e -> {
            try {
                login();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "数据库连接失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 添加退出按钮事件
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void login() throws SQLException {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // 连接数据库
        DataProcessing.connectToDatabase();

        // 验证用户
        AbstractUser user = DataProcessing.searchUser(username, password);
        if (user != null) {
            // 登录成功，打开主窗口
            this.dispose();
            new MainJFrame(user);
        } else {
            // 登录失败
            JOptionPane.showMessageDialog(this, "用户名或密码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void initComponents() {
        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(4, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // 添加标题
        JLabel titleLabel = new JLabel("档案管理系统");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 30));
        //设置文字水平居中
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel);

        // 添加用户名面板
        JPanel usernamePanel = new JPanel();
        //设置流式布局，左对齐，水平间距10，垂直间距10
        usernamePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JLabel usernameLabel = new JLabel("用户名：");
        usernameField = new JTextField(15);
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);
        mainPanel.add(usernamePanel);

        // 添加密码面板
        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JLabel passwordLabel = new JLabel("密码： ");
        passwordField = new JPasswordField(15);
        passwordField.setEchoChar('*');
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        mainPanel.add(passwordPanel);

        // 添加按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        loginButton = new JButton("登录");
        setLoginButton(loginButton);
        exitButton = new JButton("退出");
        loginButton.setPreferredSize(new Dimension(100, 30));
        exitButton.setPreferredSize(new Dimension(100, 30));
        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);
        mainPanel.add(buttonPanel);

        // 添加主面板到窗口
        this.add(mainPanel);
        // 重新绘制窗口
        this.revalidate();
    }

    private void initJFrame() {
        //设置标题
        this.setTitle("登录界面");
        //设置退出程序
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //设置窗口大小
        this.setSize(400, 300);
        //设置窗口居中显示
        this.setLocationRelativeTo(null);
        //设置窗口总在顶部
        //this.setAlwaysOnTop(true);
        //设置窗口可见
        this.setVisible(true);
        //把图标设置为登录界面的图标
        ImageIcon icon = new ImageIcon("week4/111.jpg");
        // 把图标设置为登录界面的图标
        this.setIconImage( icon.getImage());


    }

    public void setLoginButton(JButton loginButton) {
        this.loginButton = loginButton;
    }
}
