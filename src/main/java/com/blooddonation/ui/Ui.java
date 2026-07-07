package com.blooddonation.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicSpinnerUI;

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
        field.setBackground(PANEL);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(9, 10, 9, 10)
        ));
    }

    static void comboBox(JComboBox<?> box, int width) {
        box.setFont(font(16, Font.PLAIN));
        box.setPreferredSize(new Dimension(width, 44));
        box.setMinimumSize(new Dimension(width, 44));
        box.setBackground(PANEL);
        box.setForeground(TEXT);
        box.setFocusable(false);
        box.setBorder(BorderFactory.createLineBorder(BORDER));
        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(font(16, Font.PLAIN));
                setForeground(TEXT);
                setBackground(isSelected ? new Color(248, 226, 228) : PANEL);
                setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                list.setSelectionBackground(new Color(248, 226, 228));
                list.setSelectionForeground(TEXT);
                list.setBackground(PANEL);
                return this;
            }
        });
        box.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return new JButton() {
                    {
                        setBackground(PANEL);
                        setOpaque(true);
                        setContentAreaFilled(true);
                        setFocusPainted(false);
                        setBorder(BorderFactory.createEmptyBorder());
                        setPreferredSize(new Dimension(42, 44));
                    }

                    @Override
                    protected void paintComponent(Graphics graphics) {
                        super.paintComponent(graphics);
                        Graphics2D g = (Graphics2D) graphics.create();
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setColor(TEXT);
                        int centerX = getWidth() / 2;
                        int centerY = getHeight() / 2 + 1;
                        g.drawLine(centerX - 4, centerY - 2, centerX, centerY + 2);
                        g.drawLine(centerX, centerY + 2, centerX + 4, centerY - 2);
                        g.dispose();
                    }
                };
            }
        });
    }

    static void spinner(JSpinner spinner, int width) {
        spinner.setFont(font(16, Font.PLAIN));
        spinner.setPreferredSize(new Dimension(width, 44));
        spinner.setMinimumSize(new Dimension(width, 44));
        spinner.setBackground(PANEL);
        spinner.setBorder(BorderFactory.createLineBorder(BORDER));
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
            JFormattedTextField field = editor.getTextField();
            field.setFont(font(16, Font.PLAIN));
            field.setForeground(TEXT);
            field.setBackground(PANEL);
            field.setCaretColor(TEXT);
            field.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
        }
        spinner.setUI(new BasicSpinnerUI() {
            @Override
            protected java.awt.Component createNextButton() {
                JButton button = spinnerButton("+");
                installNextButtonListeners(button);
                return button;
            }

            @Override
            protected java.awt.Component createPreviousButton() {
                JButton button = spinnerButton("-");
                installPreviousButtonListeners(button);
                return button;
            }
        });
    }

    static void area(JTextArea area) {
        area.setFont(font(15, Font.PLAIN));
        area.setForeground(TEXT);
        area.setBackground(PANEL);
        area.setCaretColor(TEXT);
        area.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
    }

    private static JButton spinnerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(font(13, Font.BOLD));
        button.setForeground(TEXT);
        button.setBackground(PANEL);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER));
        button.setPreferredSize(new Dimension(36, 22));
        return button;
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

    static void toolbarButton(JButton button, int width, boolean primary) {
        button.setFont(font(14, Font.BOLD));
        button.setForeground(primary ? Color.WHITE : PRIMARY);
        button.setBackground(primary ? PRIMARY : PANEL);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(width, 38));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(primary ? PRIMARY : BORDER),
            BorderFactory.createEmptyBorder(9, 18, 9, 18)
        ));
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
