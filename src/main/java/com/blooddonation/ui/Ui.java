package com.blooddonation.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextField;

final class Ui {
    static final Color PRIMARY = new Color(174, 38, 45);
    static final Color TEXT = new Color(35, 38, 46);
    static final Color SIDEBAR = new Color(35, 38, 46);
    static final Color PAGE = new Color(246, 244, 239);
    static final Color PANEL = Color.WHITE;
    static final Color BORDER = new Color(213, 208, 198);

    private Ui() {
    }

    static Font font(int size, int style) {
        return new Font(Font.SANS_SERIF, style, size);
    }

    static void field(JTextField field) {
        field.setFont(font(16, Font.PLAIN));
        field.setPreferredSize(new Dimension(320, 44));
        field.setMinimumSize(new Dimension(320, 44));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(9, 10, 9, 10)
        ));
    }

    static void primaryButton(JButton button, int width) {
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(width, 44));
        button.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
    }

    static void textButton(JButton button) {
        button.setForeground(PRIMARY);
        button.setBackground(PAGE);
        button.setOpaque(true);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
    }
}
