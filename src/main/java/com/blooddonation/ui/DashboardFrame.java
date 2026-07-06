package com.blooddonation.ui;

import com.blooddonation.service.AuthService.UserSession;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class DashboardFrame extends JFrame {
    private final List<JButton> navButtons = new ArrayList<>();
    private final JPanel mainPanel = new JPanel(new BorderLayout(0, 24));

    public DashboardFrame(UserSession session) {
        setTitle("献血管理系统");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);
        setContentPane(content(session));
    }

    private JPanel content(UserSession session) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Ui.PAGE);
        root.add(sidebar(session), BorderLayout.WEST);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(34, 42, 34, 42));
        mainPanel.setBackground(Ui.PAGE);
        root.add(mainPanel, BorderLayout.CENTER);
        showModule("业务数据", "该模块尚未接入业务页面。", session);
        return root;
    }

    private JPanel sidebar(UserSession session) {
        JPanel panel = new JPanel(new BorderLayout(0, 22));
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(28, 18, 28, 18));
        panel.setBackground(Ui.SIDEBAR);

        JLabel title = new JLabel("<html>献血<br>管理系统</html>");
        title.setForeground(Ui.PANEL);
        title.setFont(Ui.font(24, Font.BOLD));
        panel.add(title, BorderLayout.NORTH);

        JPanel nav = new JPanel(new GridLayout(0, 1, 0, 10));
        nav.setBackground(Ui.SIDEBAR);

        String[] modules = {"业务数据", "订单记录", "分类管理", "评论互动", "统计报表", "系统日志"};
        for (String module : modules) {
            JButton button = new JButton(module);
            button.setFocusPainted(false);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            button.addActionListener(event -> {
                selectNav(button);
                showModule(module, "该模块尚未接入业务页面。", session);
            });
            navButtons.add(button);
            nav.add(button);
        }
        if (!navButtons.isEmpty()) {
            selectNav(navButtons.get(0));
        }
        panel.add(nav, BorderLayout.CENTER);

        JLabel user = new JLabel("<html>" + session.username() + "<br>" + session.role() + "</html>");
        user.setForeground(Ui.PANEL);
        user.setFont(Ui.font(14, Font.PLAIN));
        panel.add(user, BorderLayout.SOUTH);
        return panel;
    }

    private void showModule(String titleText, String message, UserSession session) {
        mainPanel.removeAll();

        JLabel title = new JLabel(titleText);
        title.setFont(Ui.font(30, Font.BOLD));
        title.setForeground(Ui.TEXT);
        mainPanel.add(title, BorderLayout.NORTH);

        JLabel welcome = new JLabel(
            "<html><b>" + session.username() + "</b> · " + session.role()
                + "<br><br>" + message + "</html>",
            SwingConstants.LEFT
        );
        welcome.setOpaque(true);
        welcome.setBackground(Ui.PANEL);
        welcome.setForeground(Ui.TEXT);
        welcome.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, Ui.PRIMARY),
            BorderFactory.createEmptyBorder(28, 30, 28, 30)
        ));
        welcome.setFont(Ui.font(18, Font.PLAIN));

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Ui.PAGE);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        center.add(welcome, c);
        mainPanel.add(center, BorderLayout.CENTER);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void selectNav(JButton selected) {
        for (JButton button : navButtons) {
            boolean active = button == selected;
            button.setForeground(Ui.PANEL);
            button.setBackground(active ? Ui.PRIMARY : new Color(61, 65, 75));
            button.setOpaque(true);
            button.setContentAreaFilled(true);
        }
    }
}
