package com.blooddonation;

import com.blooddonation.ui.LoginFrame;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Blood donation management system started.");
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
