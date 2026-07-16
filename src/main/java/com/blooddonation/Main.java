package com.blooddonation;

import com.blooddonation.ui.LoginFrame;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 献血管理系统的应用入口。
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    /**
     * 在 Swing 事件线程中打开登录窗口。
     *
     * @param args 命令行参数，当前未使用
     */
    public static void main(String[] args) {
        log.info("Blood donation management system started.");
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
