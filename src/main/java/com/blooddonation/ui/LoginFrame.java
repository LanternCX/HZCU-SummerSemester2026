package com.blooddonation.ui;

import com.blooddonation.exception.DBException;
import com.blooddonation.service.AuthService;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends JFrame {
    private final AuthService authService;
    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    public LoginFrame() {
        this(new AuthService());
    }

    LoginFrame(AuthService authService) {
        this.authService = authService;
        setTitle("献血管理系统登录");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 460));
        setLocationRelativeTo(null);
        setContentPane(content());
    }

    private JPanel content() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Ui.PAGE);
        root.add(brandPanel(), BorderLayout.WEST);
        root.add(formPanel(), BorderLayout.CENTER);
        return root;
    }

    private JPanel brandPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBackground(Ui.PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(42, 34, 42, 34));

        JLabel title = new JLabel("<html>献血<br>管理系统</html>");
        title.setForeground(Ui.PANEL);
        title.setFont(Ui.font(34, Font.BOLD));
        panel.add(title, BorderLayout.NORTH);

        JLabel note = new JLabel("<html>血液库存、用血记录、统计报表统一管理。</html>");
        note.setForeground(Ui.PANEL);
        note.setFont(Ui.font(15, Font.PLAIN));
        panel.add(note, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Ui.PAGE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 56, 40, 56));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 26, 0);
        JLabel title = new JLabel("账号登录");
        title.setForeground(Ui.TEXT);
        title.setFont(Ui.font(28, Font.BOLD));
        panel.add(title, c);

        addField(panel, c, 1, "用户名", usernameField);
        addField(panel, c, 2, "密码", passwordField);

        JButton loginButton = new JButton("登录");
        Ui.primaryButton(loginButton, 320);
        loginButton.addActionListener(event -> login());
        getRootPane().setDefaultButton(loginButton);

        JButton registerButton = new JButton("注册账号");
        Ui.textButton(registerButton);
        registerButton.addActionListener(event -> register());

        c.gridy = 3;
        c.gridx = 1;
        c.gridwidth = 1;
        c.insets = new Insets(18, 0, 0, 0);
        c.anchor = GridBagConstraints.EAST;
        panel.add(loginButton, c);

        c.gridy = 4;
        c.insets = new Insets(10, 0, 0, 0);
        panel.add(registerButton, c);
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints c, int row, String label, JTextField field) {
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setForeground(Ui.TEXT);
        fieldLabel.setFont(Ui.font(15, Font.PLAIN));

        c.gridy = row;
        c.gridx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(0, 0, 14, 16);
        c.anchor = GridBagConstraints.WEST;
        panel.add(fieldLabel, c);

        Ui.field(field);

        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panel.add(field, c);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
    }

    private void login() {
        try {
            AuthService.LoginResult result = authService.login(
                usernameField.getText(),
                new String(passwordField.getPassword()),
                "127.0.0.1"
            );
            if (!result.success()) {
                JOptionPane.showMessageDialog(this, result.message(), "登录失败", JOptionPane.WARNING_MESSAGE);
                return;
            }

            new DashboardFrame(result.session()).setVisible(true);
            dispose();
        } catch (DBException ex) {
            JOptionPane.showMessageDialog(this, "数据库连接失败，请检查服务和配置。", "登录失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void register() {
        JTextField newUsername = new JTextField(18);
        JPasswordField newPassword = new JPasswordField(18);
        JTextField email = new JTextField(18);
        JTextField phone = new JTextField(18);

        JDialog dialog = new JDialog(this, "注册账号", true);
        JPanel root = new JPanel(new BorderLayout(0, 22));
        root.setBackground(Ui.PAGE);
        root.setBorder(BorderFactory.createEmptyBorder(28, 34, 28, 34));

        JLabel title = new JLabel("注册账号");
        title.setForeground(Ui.TEXT);
        title.setFont(Ui.font(26, Font.BOLD));
        root.add(title, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Ui.PAGE);
        GridBagConstraints c = new GridBagConstraints();
        addField(panel, c, 0, "用户名", newUsername);
        addField(panel, c, 1, "密码", newPassword);
        addField(panel, c, 2, "邮箱", email);
        addField(panel, c, 3, "手机", phone);
        root.add(panel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Ui.PAGE);
        JButton cancelButton = new JButton("取消");
        Ui.textButton(cancelButton);
        cancelButton.addActionListener(event -> dialog.dispose());
        JButton submitButton = new JButton("注册");
        Ui.primaryButton(submitButton, 112);
        submitButton.addActionListener(event -> {
            try {
                AuthService.RegisterResult result = authService.register(
                    newUsername.getText(),
                    new String(newPassword.getPassword()),
                    email.getText(),
                    phone.getText()
                );
                if (!result.success()) {
                    JOptionPane.showMessageDialog(this, result.message(), "注册失败", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                usernameField.setText(newUsername.getText().trim());
                passwordField.setText("");
                JOptionPane.showMessageDialog(this, "注册成功，请登录。", "注册成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (DBException ex) {
                JOptionPane.showMessageDialog(this, "注册失败，请检查信息是否重复。", "注册失败", JOptionPane.ERROR_MESSAGE);
            }
        });
        actions.add(cancelButton);
        actions.add(submitButton);
        root.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton(submitButton);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(440, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
