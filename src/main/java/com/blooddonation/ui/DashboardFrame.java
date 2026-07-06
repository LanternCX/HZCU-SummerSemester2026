package com.blooddonation.ui;

import com.blooddonation.service.AuthService.UserSession;
import com.blooddonation.service.BusinessService;
import com.blooddonation.service.BusinessService.BusinessResult;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.bson.Document;

public class DashboardFrame extends JFrame {
    private final BusinessService businessService;
    private final List<JButton> navButtons = new ArrayList<>();
    private final JPanel mainPanel = new JPanel(new BorderLayout(0, 18));

    public DashboardFrame(UserSession session) {
        this(session, new BusinessService());
    }

    DashboardFrame(UserSession session, BusinessService businessService) {
        this.businessService = businessService;
        setTitle("献血管理系统");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);
        setContentPane(content(session));
    }

    private JPanel content(UserSession session) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Ui.PAGE);
        root.add(sidebar(session), BorderLayout.WEST);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(28, 34, 28, 34));
        mainPanel.setBackground(Ui.PAGE);
        root.add(mainPanel, BorderLayout.CENTER);
        showBusinessPanel(session);
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
                showModule(module, session);
            });
            navButtons.add(button);
            nav.add(button);
        }
        selectNav(navButtons.get(0));
        panel.add(nav, BorderLayout.CENTER);

        JLabel user = new JLabel("<html>" + session.username() + "<br>" + session.role() + "</html>");
        user.setForeground(Ui.PANEL);
        user.setFont(Ui.font(14, Font.PLAIN));
        panel.add(user, BorderLayout.SOUTH);
        return panel;
    }

    private void showModule(String module, UserSession session) {
        if ("业务数据".equals(module)) {
            showBusinessPanel(session);
            return;
        }
        if ("订单记录".equals(module)) {
            showOrderPanel(session);
            return;
        }
        if ("分类管理".equals(module)) {
            showCategoryPanel(session);
            return;
        }
        showPlaceholder(module, "该模块将在后续阶段接入。", session);
    }

    private void showBusinessPanel(UserSession session) {
        resetMain("业务数据", "维护血液库存批次，双击表格行打开详情。");

        DefaultTableModel model = tableModel("item_id", "库存批次", "分类", "数量", "状态");
        JTable table = table(model);
        hideFirstColumn(table);
        setColumnWidths(table, 0, 360, 120, 120, 100);

        JPanel tabs = tabs();
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Long itemId = selectedId(table);
                    if (itemId != null) {
                        openItemDetailTab(tabs, table, itemId, model, session);
                    }
                }
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Ui.PANEL);
        if (isAdmin(session)) {
            JButton addButton = new JButton("新增");
            Ui.primaryButton(addButton, 128);
            addButton.addActionListener(event -> showCreateItemDialog(model));
            actions.add(addButton);
            JButton deleteButton = new JButton("删除");
            Ui.primaryButton(deleteButton, 128);
            deleteButton.addActionListener(event -> {
                Long itemId = selectedId(table);
                if (itemId == null) {
                    warn("请先在表格中选择库存批次。");
                    return;
                }
                if (confirm("确认删除选中的库存批次？")) {
                    showResult(businessService.deleteItem(itemId));
                    loadItems(model);
                    closeDetailTab(tabs, "item:" + itemId);
                }
            });
            actions.add(deleteButton);
        }

        JPanel listPanel = section("库存列表", new JScrollPane(table), isAdmin(session) ? actions : null);
        tabs.add(listPanel, "list");
        mainPanel.add(tabs, BorderLayout.CENTER);
        loadItems(model);
        refreshMain();
    }

    private void showCreateItemDialog(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "新增库存", true);
        JTextField titleField = field();
        JComboBox<Option> categoryBox = new JComboBox<>();
        loadCategories(categoryBox);
        JSpinner amountSpinner = amountSpinner();
        JComboBox<String> bloodTypeBox = bloodTypeBox();
        JTextArea descriptionArea = area(5);

        JPanel form = itemForm(titleField, categoryBox, amountSpinner, null, bloodTypeBox, descriptionArea);

        JButton cancelButton = new JButton("取消");
        Ui.textButton(cancelButton);
        cancelButton.addActionListener(event -> dialog.dispose());
        JButton saveButton = new JButton("保存");
        Ui.primaryButton(saveButton, 112);
        saveButton.addActionListener(event -> {
            Option category = selected(categoryBox);
            if (category == null) {
                warn("请先选择分类。");
                return;
            }
            try {
                BusinessResult result = businessService.createItem(
                    titleField.getText(),
                    category.id(),
                    spinnerAmount(amountSpinner),
                    descriptionArea.getText(),
                    List.of(),
                    new Document("blood_type", bloodTypeBox.getSelectedItem())
                );
                showResult(result);
                if (result.success()) {
                    loadItems(model);
                    dialog.dispose();
                }
            } catch (RuntimeException ex) {
                warn("保存失败，请检查数据库连接。");
            }
        });

        dialog.setContentPane(dialogContent("新增库存", form, cancelButton, saveButton));
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(700, 520));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showEditItemDialog(long itemId, DefaultTableModel model, Runnable onSaved) {
        Map<String, Object> item = findItemRow(itemId);
        if (item == null) {
            warn("业务数据不存在。");
            return;
        }

        JDialog dialog = new JDialog(this, "编辑库存", true);
        JTextField titleField = field();
        titleField.setText(String.valueOf(item.get("title")));
        JComboBox<Option> categoryBox = new JComboBox<>();
        loadCategories(categoryBox);
        selectOption(categoryBox, ((Number) item.get("category_id")).longValue());
        JSpinner amountSpinner = amountSpinner();
        amountSpinner.setValue(((BigDecimal) item.get("amount")).doubleValue());
        JComboBox<String> statusBox = new JComboBox<>(new String[] {"停用", "可用"});
        statusBox.setSelectedIndex(((Number) item.get("status")).intValue());
        JComboBox<String> bloodTypeBox = bloodTypeBox();
        JTextArea descriptionArea = area(5);
        businessService.findItemDetail(itemId).ifPresent(document -> {
            descriptionArea.setText(document.getString("description"));
            Object metadata = document.get("metadata");
            if (metadata instanceof Document metadataDocument && metadataDocument.get("blood_type") != null) {
                bloodTypeBox.setSelectedItem(String.valueOf(metadataDocument.get("blood_type")));
            }
        });

        JPanel form = itemForm(titleField, categoryBox, amountSpinner, statusBox, bloodTypeBox, descriptionArea);

        JButton cancelButton = new JButton("取消");
        Ui.textButton(cancelButton);
        cancelButton.addActionListener(event -> dialog.dispose());
        JButton saveButton = new JButton("保存");
        Ui.primaryButton(saveButton, 112);
        saveButton.addActionListener(event -> {
            try {
                Option category = selected(categoryBox);
                if (category == null) {
                    warn("请先选择分类。");
                    return;
                }
                BusinessResult result = businessService.updateItem(
                    itemId,
                    titleField.getText(),
                    category.id(),
                    spinnerAmount(amountSpinner),
                    statusBox.getSelectedIndex(),
                    descriptionArea.getText(),
                    List.of(),
                    new Document("blood_type", bloodTypeBox.getSelectedItem())
                );
                showResult(result);
                if (result.success()) {
                    loadItems(model);
                    onSaved.run();
                    dialog.dispose();
                }
            } catch (RuntimeException ex) {
                warn("保存失败，请检查数据库连接。");
            }
        });

        dialog.setContentPane(dialogContent("编辑库存", form, cancelButton, saveButton));
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(720, 560));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showOrderPanel(UserSession session) {
        resetMain("订单记录", "查看用血记录，双击表格行打开详情。");

        DefaultTableModel model = tableModel("order_id", "库存批次", "数量", "状态");
        JTable table = table(model);
        hideFirstColumn(table);
        setColumnWidths(table, 0, 420, 120, 120);

        JPanel tabs = tabs();
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Long orderId = selectedId(table);
                    if (orderId != null) {
                        openOrderDetailTab(tabs, table, orderId, session, model);
                    }
                }
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Ui.PANEL);
        JButton createButton = new JButton("新增");
        Ui.primaryButton(createButton, 128);
        createButton.addActionListener(event -> showCreateOrderDialog(session, model));
        actions.add(createButton);
        if (isAdmin(session)) {
            JButton deleteButton = new JButton("删除");
            Ui.primaryButton(deleteButton, 128);
            deleteButton.addActionListener(event -> {
                Long orderId = selectedId(table);
                if (orderId == null) {
                    warn("请先在表格中选择记录。");
                    return;
                }
                if (confirm("确认删除选中的用血记录？")) {
                    showResult(deleteOrder(session, orderId));
                    loadOrders(model, session);
                    closeDetailTab(tabs, "order:" + orderId);
                }
            });
            actions.add(deleteButton);
        }

        tabs.add(section("我的记录", new JScrollPane(table), actions.getComponentCount() > 0 ? actions : null), "list");
        mainPanel.add(tabs, BorderLayout.CENTER);
        loadOrders(model, session);
        refreshMain();
    }

    private void showCategoryPanel(UserSession session) {
        resetMain("分类管理", "维护血液分类，双击表格行打开详情。");

        DefaultTableModel model = tableModel("category_id", "分类名称", "父分类", "层级");
        JTable table = table(model);
        hideFirstColumn(table);
        setColumnWidths(table, 0, 300, 220, 120);

        JPanel tabs = tabs();
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Long categoryId = selectedId(table);
                    if (categoryId != null) {
                        openCategoryDetailTab(tabs, table, categoryId, session, model);
                    }
                }
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Ui.PANEL);
        if (isAdmin(session)) {
            JButton addButton = new JButton("新增");
            Ui.primaryButton(addButton, 128);
            addButton.addActionListener(event -> showCreateCategoryDialog(model));
            actions.add(addButton);

            JButton deleteButton = new JButton("删除");
            Ui.primaryButton(deleteButton, 128);
            deleteButton.addActionListener(event -> {
                Long categoryId = selectedId(table);
                if (categoryId == null) {
                    warn("请先在表格中选择分类。");
                    return;
                }
                if (confirm("确认删除选中的分类？")) {
                    BusinessResult result = businessService.deleteCategory(categoryId);
                    showResult(result);
                    if (result.success()) {
                        loadCategories(model);
                        closeDetailTab(tabs, "category:" + categoryId);
                    }
                }
            });
            actions.add(deleteButton);
        }

        tabs.add(section("分类列表", new JScrollPane(table), isAdmin(session) ? actions : null), "list");
        mainPanel.add(tabs, BorderLayout.CENTER);
        loadCategories(model);
        refreshMain();
    }

    private void showCreateCategoryDialog(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "新增分类", true);
        JTextField nameField = field();
        JComboBox<Option> parentBox = new JComboBox<>();
        loadParentCategories(parentBox, null);

        JPanel form = categoryForm(nameField, parentBox);

        JButton cancelButton = new JButton("取消");
        Ui.textButton(cancelButton);
        cancelButton.addActionListener(event -> dialog.dispose());
        JButton saveButton = new JButton("保存");
        Ui.primaryButton(saveButton, 112);
        saveButton.addActionListener(event -> {
            try {
                BusinessResult result = businessService.createCategory(nameField.getText(), selectedParentId(parentBox));
                showResult(result);
                if (result.success()) {
                    loadCategories(model);
                    dialog.dispose();
                }
            } catch (RuntimeException ex) {
                warn("保存失败，分类名称可能重复或数据库连接异常。");
            }
        });

        dialog.setContentPane(dialogContent("新增分类", form, cancelButton, saveButton));
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(620, 340));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showCreateChildCategoryDialog(long parentId, DefaultTableModel model, Runnable onSaved) {
        JDialog dialog = new JDialog(this, "新增子分类", true);
        JTextField nameField = field();

        JPanel form = formPanel();
        form.setBorder(BorderFactory.createEmptyBorder(22, 24, 12, 24));
        addFormField(form, 0, 0, 2, "子分类名称", nameField);

        JButton cancelButton = new JButton("取消");
        Ui.textButton(cancelButton);
        cancelButton.addActionListener(event -> dialog.dispose());
        JButton saveButton = new JButton("保存");
        Ui.primaryButton(saveButton, 112);
        saveButton.addActionListener(event -> {
            try {
                BusinessResult result = businessService.createCategory(nameField.getText(), parentId);
                showResult(result);
                if (result.success()) {
                    loadCategories(model);
                    onSaved.run();
                    dialog.dispose();
                }
            } catch (RuntimeException ex) {
                warn("保存失败，分类名称可能重复或数据库连接异常。");
            }
        });

        dialog.setContentPane(dialogContent("新增子分类", form, cancelButton, saveButton));
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(620, 280));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showEditCategoryDialog(long categoryId, DefaultTableModel model, Runnable onSaved) {
        Map<String, Object> category = findCategoryRow(categoryId);
        if (category == null) {
            warn("分类不存在。");
            return;
        }

        JDialog dialog = new JDialog(this, "编辑分类", true);
        JTextField nameField = field();
        nameField.setText(String.valueOf(category.get("name")));
        JComboBox<Option> parentBox = new JComboBox<>();
        loadParentCategories(parentBox, categoryId);
        Object parentId = category.get("parent_id");
        if (parentId != null) {
            selectOption(parentBox, ((Number) parentId).longValue());
        }

        JPanel form = categoryForm(nameField, parentBox);

        JButton cancelButton = new JButton("取消");
        Ui.textButton(cancelButton);
        cancelButton.addActionListener(event -> dialog.dispose());
        JButton saveButton = new JButton("保存");
        Ui.primaryButton(saveButton, 112);
        saveButton.addActionListener(event -> {
            try {
                BusinessResult result = businessService.updateCategory(categoryId, nameField.getText(), selectedParentId(parentBox));
                showResult(result);
                if (result.success()) {
                    loadCategories(model);
                    onSaved.run();
                    dialog.dispose();
                }
            } catch (RuntimeException ex) {
                warn("保存失败，分类名称可能重复或数据库连接异常。");
            }
        });

        dialog.setContentPane(dialogContent("编辑分类", form, cancelButton, saveButton));
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(620, 340));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel categoryForm(JTextField nameField, JComboBox<Option> parentBox) {
        JPanel form = formPanel();
        form.setBorder(BorderFactory.createEmptyBorder(22, 24, 12, 24));
        addFormField(form, 0, 0, 2, "分类名称", nameField);
        addFormField(form, 1, 0, 2, "父分类", parentBox);
        return form;
    }

    private void showCreateOrderDialog(UserSession session, DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "创建用血记录", true);
        JComboBox<Option> itemBox = new JComboBox<>();
        loadItemOptions(itemBox);
        JSpinner amountSpinner = amountSpinner();

        JPanel form = formPanel();
        form.setBorder(BorderFactory.createEmptyBorder(22, 24, 12, 24));
        addRow(form, 0, "库存批次", itemBox);
        addRow(form, 1, "数量", amountSpinner);

        JButton cancelButton = new JButton("取消");
        Ui.textButton(cancelButton);
        cancelButton.addActionListener(event -> dialog.dispose());
        JButton saveButton = new JButton("创建");
        Ui.primaryButton(saveButton, 112);
        saveButton.addActionListener(event -> {
            Option item = selected(itemBox);
            if (item == null) {
                warn("请先选择库存批次。");
                return;
            }
            try {
                BusinessResult result = businessService.createOrder(session.userId(), item.id(), spinnerAmount(amountSpinner));
                showResult(result);
                if (result.success()) {
                    loadOrders(model, session);
                    dialog.dispose();
                }
            } catch (RuntimeException ex) {
                warn("创建失败，请检查数据库连接。");
            }
        });

        dialog.setContentPane(dialogContent("创建用血记录", form, cancelButton, saveButton));
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(540, 300));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showEditOwnOrderDialog(long orderId, UserSession session, DefaultTableModel model, Runnable onSaved) {
        JDialog dialog = new JDialog(this, "编辑用血申请", true);
        JComboBox<Option> itemBox = new JComboBox<>();
        loadItemOptions(itemBox);
        JSpinner amountSpinner = amountSpinner();
        findOrderRow(model, orderId).ifPresent(row -> {
            selectOptionByLabel(itemBox, String.valueOf(row.get("item")));
            amountSpinner.setValue(new BigDecimal(String.valueOf(row.get("amount"))).doubleValue());
        });

        JPanel form = formPanel();
        form.setBorder(BorderFactory.createEmptyBorder(22, 24, 12, 24));
        addRow(form, 0, "库存批次", itemBox);
        addRow(form, 1, "数量", amountSpinner);

        JButton cancelButton = new JButton("取消");
        Ui.textButton(cancelButton);
        cancelButton.addActionListener(event -> dialog.dispose());
        JButton saveButton = new JButton("保存");
        Ui.primaryButton(saveButton, 112);
        saveButton.addActionListener(event -> {
            Option item = selected(itemBox);
            if (item == null) {
                warn("请先选择库存批次。");
                return;
            }
            BusinessResult result = businessService.updateOwnOrder(session.userId(), orderId, item.id(), spinnerAmount(amountSpinner));
            showResult(result);
            if (result.success()) {
                loadOrders(model, session);
                onSaved.run();
                dialog.dispose();
            }
        });

        dialog.setContentPane(dialogContent("编辑用血申请", form, cancelButton, saveButton));
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(540, 300));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showUpdateOrderDialog(long orderId, UserSession session, DefaultTableModel model, Runnable onSaved) {
        JDialog dialog = new JDialog(this, "更新处理状态", true);
        JComboBox<String> statusBox = new JComboBox<>(new String[] {"待处理", "已完成", "已取消"});

        JPanel form = formPanel();
        form.setBorder(BorderFactory.createEmptyBorder(22, 24, 12, 24));
        addRow(form, 0, "状态", statusBox);

        JButton cancelButton = new JButton("取消");
        Ui.textButton(cancelButton);
        cancelButton.addActionListener(event -> dialog.dispose());
        JButton saveButton = new JButton("保存");
        Ui.primaryButton(saveButton, 112);
        saveButton.addActionListener(event -> {
            BusinessResult result = businessService.updateOrderStatus(orderId, statusBox.getSelectedIndex());
            showResult(result);
            if (result.success()) {
                loadOrders(model, session);
                onSaved.run();
                dialog.dispose();
            }
        });

        dialog.setContentPane(dialogContent("更新处理状态", form, cancelButton, saveButton));
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(440, 240));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showPlaceholder(String titleText, String message, UserSession session) {
        resetMain(titleText, session.username() + " · " + session.role());
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(Ui.PANEL);
        label.setForeground(Ui.TEXT);
        label.setFont(Ui.font(18, Font.PLAIN));
        label.setBorder(BorderFactory.createLineBorder(Ui.BORDER));
        mainPanel.add(label, BorderLayout.CENTER);
        refreshMain();
    }

    private void resetMain(String titleText, String subtitleText) {
        mainPanel.removeAll();
        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setBackground(Ui.PAGE);
        JLabel title = new JLabel(titleText);
        title.setFont(Ui.font(30, Font.BOLD));
        title.setForeground(Ui.TEXT);
        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setFont(Ui.font(14, Font.PLAIN));
        subtitle.setForeground(new Color(91, 94, 102));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        mainPanel.add(header, BorderLayout.NORTH);
    }

    private JPanel section(String title, java.awt.Component body) {
        return section(title, body, null);
    }

    private JPanel section(String title, java.awt.Component body, java.awt.Component actions) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setPreferredSize(new Dimension(380, 0));
        panel.setBackground(Ui.PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Ui.BORDER),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Ui.PANEL);
        JLabel label = new JLabel(title);
        label.setForeground(Ui.TEXT);
        label.setFont(Ui.font(18, Font.BOLD));
        header.add(label, BorderLayout.WEST);
        if (actions != null) {
            header.add(actions, BorderLayout.EAST);
        }
        panel.add(header, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel tabs() {
        JPanel tabs = new JPanel(new CardLayout());
        tabs.setBackground(Ui.PAGE);
        return tabs;
    }

    private void openItemDetailTab(
        JPanel tabs,
        JTable table,
        long itemId,
        DefaultTableModel model,
        UserSession session
    ) {
        String key = "item:" + itemId;
        if (selectDetailTab(tabs, key)) {
            return;
        }

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Ui.PANEL);
        JButton closeButton = new JButton("关闭");
        Ui.textButton(closeButton);
        closeButton.addActionListener(event -> closeDetailTab(tabs, key));
        actions.add(closeButton);

        if (isAdmin(session)) {
            JButton editButton = new JButton("编辑");
            Ui.primaryButton(editButton, 112);
            editButton.addActionListener(event -> showEditItemDialog(itemId, model, () -> {
                closeDetailTab(tabs, key);
                openItemDetailTab(tabs, table, itemId, model, session);
            }));
            actions.add(editButton);

            JButton deleteButton = new JButton("删除");
            Ui.primaryButton(deleteButton, 112);
            deleteButton.addActionListener(event -> {
                if (confirm("确认删除该库存批次？")) {
                    showResult(businessService.deleteItem(itemId));
                    loadItems(model);
                    closeDetailTab(tabs, key);
                }
            });
            actions.add(deleteButton);
        }

        JPanel detail = detailPage("批次详情", itemDetailBody(table, itemId), actions);
        addDetailTab(tabs, detailTabTitle("批次", table, itemId, 1), detail, key);
    }

    private void openOrderDetailTab(
        JPanel tabs,
        JTable table,
        long orderId,
        UserSession session,
        DefaultTableModel model
    ) {
        String key = "order:" + orderId;
        if (selectDetailTab(tabs, key)) {
            return;
        }

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Ui.PANEL);
        JButton closeButton = new JButton("关闭");
        Ui.textButton(closeButton);
        closeButton.addActionListener(event -> closeDetailTab(tabs, key));
        actions.add(closeButton);

        if (isAdmin(session)) {
            JButton editButton = new JButton("编辑");
            Ui.primaryButton(editButton, 112);
            editButton.addActionListener(event -> showUpdateOrderDialog(
                orderId,
                session,
                model,
                () -> {
                    closeDetailTab(tabs, key);
                    openOrderDetailTab(tabs, table, orderId, session, model);
                }
            ));
            actions.add(editButton);

            addOrderDeleteButton(actions, tabs, key, session, model, orderId);
        } else if (isPendingOrder(model, orderId)) {
            JButton editButton = new JButton("编辑");
            Ui.primaryButton(editButton, 112);
            editButton.addActionListener(event -> showEditOwnOrderDialog(
                orderId,
                session,
                model,
                () -> {
                    closeDetailTab(tabs, key);
                    openOrderDetailTab(tabs, table, orderId, session, model);
                }
            ));
            actions.add(editButton);
            addOrderDeleteButton(actions, tabs, key, session, model, orderId);
        }

        JPanel detail = detailPage("记录详情", orderDetailBody(model, orderId), actions);
        addDetailTab(tabs, detailTabTitle("记录", table, orderId, 1), detail, key);
    }

    private void openCategoryDetailTab(
        JPanel tabs,
        JTable table,
        long categoryId,
        UserSession session,
        DefaultTableModel model
    ) {
        String key = "category:" + categoryId;
        if (selectDetailTab(tabs, key)) {
            return;
        }

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Ui.PANEL);
        JButton closeButton = new JButton("关闭");
        Ui.textButton(closeButton);
        closeButton.addActionListener(event -> closeDetailTab(tabs, key));
        actions.add(closeButton);

        if (isAdmin(session)) {
            JButton editButton = new JButton("编辑");
            Ui.primaryButton(editButton, 112);
            editButton.addActionListener(event -> showEditCategoryDialog(categoryId, model, () -> {
                closeDetailTab(tabs, key);
                openCategoryDetailTab(tabs, table, categoryId, session, model);
            }));
            actions.add(editButton);

            JButton deleteButton = new JButton("删除");
            Ui.primaryButton(deleteButton, 112);
            deleteButton.addActionListener(event -> {
                if (confirm("确认删除该分类？")) {
                    BusinessResult result = businessService.deleteCategory(categoryId);
                    showResult(result);
                    if (result.success()) {
                        loadCategories(model);
                        closeDetailTab(tabs, key);
                    }
                }
            });
            actions.add(deleteButton);
        }

        JPanel detail = detailPage("分类详情", categoryDetailBody(model, categoryId, session, () -> {
            closeDetailTab(tabs, key);
            openCategoryDetailTab(tabs, table, categoryId, session, model);
        }), actions);
        addDetailTab(tabs, detailTabTitle("分类", table, categoryId, 1), detail, key);
    }

    private void addOrderDeleteButton(
        JPanel actions,
        JPanel tabs,
        String key,
        UserSession session,
        DefaultTableModel model,
        long orderId
    ) {
        JButton deleteButton = new JButton("删除");
        Ui.primaryButton(deleteButton, 112);
        deleteButton.addActionListener(event -> {
            if (confirm("确认删除该用血记录？")) {
                showResult(deleteOrder(session, orderId));
                loadOrders(model, session);
                closeDetailTab(tabs, key);
            }
        });
        actions.add(deleteButton);
    }

    private BusinessResult deleteOrder(UserSession session, long orderId) {
        return isAdmin(session)
            ? businessService.deleteOrder(orderId)
            : businessService.deleteOwnOrder(session.userId(), orderId);
    }

    private JPanel detailPage(String title, JPanel body, JPanel actions) {
        return section(title, body, actions);
    }

    private boolean selectDetailTab(JPanel tabs, String key) {
        for (java.awt.Component component : tabs.getComponents()) {
            if (component instanceof JPanel panel && key.equals(panel.getClientProperty("detailKey"))) {
                ((CardLayout) tabs.getLayout()).show(tabs, key);
                return true;
            }
        }
        return false;
    }

    private void closeDetailTab(JPanel tabs, String key) {
        for (java.awt.Component component : tabs.getComponents()) {
            if (component instanceof JPanel panel && key.equals(panel.getClientProperty("detailKey"))) {
                tabs.remove(component);
                ((CardLayout) tabs.getLayout()).show(tabs, "list");
                tabs.revalidate();
                tabs.repaint();
                return;
            }
        }
    }

    private void addDetailTab(JPanel tabs, String title, JPanel detail, String key) {
        detail.putClientProperty("detailKey", key);
        tabs.add(detail, key);
        ((CardLayout) tabs.getLayout()).show(tabs, key);
    }

    private String detailTabTitle(String prefix, JTable table, long id, int nameColumn) {
        for (int i = 0; i < table.getModel().getRowCount(); i++) {
            Object rowId = table.getModel().getValueAt(i, 0);
            if (((Number) rowId).longValue() == id) {
                String name = String.valueOf(table.getModel().getValueAt(i, nameColumn));
                return prefix + " · " + ellipsis(name, 12);
            }
        }
        return prefix + "详情";
    }

    private JPanel itemDetailBody(JTable table, long itemId) {
        JPanel body = detailBody();
        for (int i = 0; i < table.getModel().getRowCount(); i++) {
            if (((Number) table.getModel().getValueAt(i, 0)).longValue() == itemId) {
                JPanel grid = detailGrid();
                addInfo(grid, 0, 0, 2, "库存批次", table.getModel().getValueAt(i, 1));
                addInfo(grid, 1, 0, 1, "分类", table.getModel().getValueAt(i, 2));
                addInfo(grid, 1, 1, 1, "数量", table.getModel().getValueAt(i, 3));
                addInfo(grid, 2, 0, 2, "状态", table.getModel().getValueAt(i, 4));
                body.add(grid, BorderLayout.NORTH);
                body.add(itemExtraPanel(itemId), BorderLayout.CENTER);
                return body;
            }
        }
        body.add(emptyDetail("该记录未在当前列表中。"), BorderLayout.CENTER);
        return body;
    }

    private JPanel orderDetailBody(DefaultTableModel model, long orderId) {
        JPanel body = detailBody();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (((Number) model.getValueAt(i, 0)).longValue() == orderId) {
                JPanel grid = detailGrid();
                addInfo(grid, 0, 0, 2, "库存批次", model.getValueAt(i, 1));
                addInfo(grid, 1, 0, 1, "数量", model.getValueAt(i, 2));
                addInfo(grid, 1, 1, 1, "状态", model.getValueAt(i, 3));
                body.add(grid, BorderLayout.NORTH);
                return body;
            }
        }
        body.add(emptyDetail("该记录未在当前列表中。"), BorderLayout.CENTER);
        return body;
    }

    private JPanel categoryDetailBody(DefaultTableModel model, long categoryId, UserSession session, Runnable onChanged) {
        JPanel body = detailBody();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (((Number) model.getValueAt(i, 0)).longValue() == categoryId) {
                JPanel grid = detailGrid();
                addInfo(grid, 0, 0, 2, "分类名称", model.getValueAt(i, 1));
                addInfo(grid, 1, 0, 1, "父分类", model.getValueAt(i, 2));
                addInfo(grid, 1, 1, 1, "层级", model.getValueAt(i, 3));
                body.add(grid, BorderLayout.NORTH);
                body.add(childCategoriesPanel(categoryId, session, model, onChanged), BorderLayout.CENTER);
                return body;
            }
        }
        body.add(emptyDetail("该分类未在当前列表中。"), BorderLayout.CENTER);
        return body;
    }

    private JPanel childCategoriesPanel(long parentId, UserSession session, DefaultTableModel mainModel, Runnable onChanged) {
        DefaultTableModel childModel = tableModel("category_id", "子分类名称");
        JTable childTable = table(childModel);
        hideFirstColumn(childTable);
        setColumnWidths(childTable, 0, 360);
        loadChildCategories(childModel, parentId);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Ui.PANEL);
        if (isAdmin(session)) {
            JButton addButton = new JButton("新增子分类");
            Ui.primaryButton(addButton, 128);
            addButton.addActionListener(event -> showCreateChildCategoryDialog(parentId, mainModel, onChanged));
            actions.add(addButton);

            JButton editButton = new JButton("编辑");
            Ui.primaryButton(editButton, 112);
            editButton.addActionListener(event -> {
                Long childId = selectedId(childTable);
                if (childId == null) {
                    warn("请先选择子分类。");
                    return;
                }
                showEditCategoryDialog(childId, mainModel, onChanged);
            });
            actions.add(editButton);

            JButton deleteButton = new JButton("删除");
            Ui.primaryButton(deleteButton, 112);
            deleteButton.addActionListener(event -> {
                Long childId = selectedId(childTable);
                if (childId == null) {
                    warn("请先选择子分类。");
                    return;
                }
                if (confirm("确认删除选中的子分类？")) {
                    BusinessResult result = businessService.deleteCategory(childId);
                    showResult(result);
                    if (result.success()) {
                        loadCategories(mainModel);
                        onChanged.run();
                    }
                }
            });
            actions.add(deleteButton);
        }

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Ui.PANEL);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Ui.PANEL);
        JLabel title = new JLabel("子分类");
        title.setForeground(Ui.TEXT);
        title.setFont(Ui.font(16, Font.BOLD));
        header.add(title, BorderLayout.WEST);
        if (actions.getComponentCount() > 0) {
            header.add(actions, BorderLayout.EAST);
        }
        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(childTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel detailBody() {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(Ui.PANEL);
        return body;
    }

    private JPanel detailGrid() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(Ui.PANEL);
        return grid;
    }

    private void addInfo(JPanel grid, int row, int column, int width, String label, Object value) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = column;
        c.gridwidth = width;
        c.weightx = width;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 12, column == 0 && width == 1 ? 12 : 0);
        grid.add(infoBlock(label, value), c);
    }

    private JPanel infoBlock(String label, Object value) {
        JPanel block = new JPanel(new BorderLayout(0, 4));
        block.setBackground(new Color(250, 249, 246));
        block.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Ui.BORDER),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        JLabel labelView = new JLabel(label);
        labelView.setForeground(new Color(91, 94, 102));
        labelView.setFont(Ui.font(13, Font.PLAIN));
        JLabel valueView = new JLabel(String.valueOf(value));
        valueView.setForeground(Ui.TEXT);
        valueView.setFont(Ui.font(17, Font.BOLD));
        block.add(labelView, BorderLayout.NORTH);
        block.add(valueView, BorderLayout.CENTER);
        return block;
    }

    private JPanel itemExtraPanel(long itemId) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 12));
        panel.setBackground(Ui.PANEL);
        try {
            Document detail = businessService.findItemDetail(itemId).orElse(null);
            String description = detail == null ? "" : detail.getString("description");
            panel.add(textBlock("详情说明", description == null || description.isBlank() ? "暂无详情说明" : description));
        } catch (RuntimeException ex) {
            panel.add(textBlock("详情说明", "详情加载失败，请检查数据库连接。"));
        }
        return panel;
    }

    private JPanel textBlock(String label, String text) {
        JPanel block = new JPanel(new BorderLayout(0, 8));
        block.setBackground(new Color(250, 249, 246));
        block.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Ui.BORDER),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        JLabel labelView = new JLabel(label);
        labelView.setForeground(Ui.TEXT);
        labelView.setFont(Ui.font(15, Font.BOLD));
        JTextArea textView = area(4);
        textView.setEditable(false);
        textView.setOpaque(false);
        textView.setBorder(BorderFactory.createEmptyBorder());
        textView.setText(text);
        block.add(labelView, BorderLayout.NORTH);
        block.add(textView, BorderLayout.CENTER);
        return block;
    }

    private JLabel emptyDetail(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(new Color(91, 94, 102));
        label.setFont(Ui.font(15, Font.PLAIN));
        return label;
    }

    private String ellipsis(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private JPanel dialogContent(String title, JPanel form, JButton cancelButton, JButton saveButton) {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(Ui.PAGE);
        root.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Ui.TEXT);
        titleLabel.setFont(Ui.font(24, Font.BOLD));
        root.add(titleLabel, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Ui.PAGE);
        actions.add(cancelButton);
        actions.add(saveButton);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Ui.PANEL);
        return panel;
    }

    private JPanel itemForm(
        JTextField titleField,
        JComboBox<Option> categoryBox,
        JSpinner amountSpinner,
        JComboBox<String> statusBox,
        JComboBox<String> bloodTypeBox,
        JTextArea descriptionArea
    ) {
        JPanel form = formPanel();
        form.setBorder(BorderFactory.createEmptyBorder(22, 24, 12, 24));
        addFormField(form, 0, 0, 2, "库存批次", titleField);
        addFormField(form, 1, 0, 1, "分类", categoryBox);
        addFormField(form, 1, 1, 1, "数量", amountSpinner);
        if (statusBox == null) {
            addFormField(form, 2, 0, 2, "血型", bloodTypeBox);
        } else {
            addFormField(form, 2, 0, 1, "状态", statusBox);
            addFormField(form, 2, 1, 1, "血型", bloodTypeBox);
        }
        addFormField(form, 3, 0, 2, "详情", new JScrollPane(descriptionArea));
        return form;
    }

    private void addFormField(JPanel form, int row, int column, int width, String labelText, java.awt.Component field) {
        JPanel block = new JPanel(new BorderLayout(0, 8));
        block.setBackground(Ui.PANEL);
        JLabel label = new JLabel(labelText);
        label.setForeground(Ui.TEXT);
        label.setFont(Ui.font(14, Font.BOLD));
        block.add(label, BorderLayout.NORTH);
        block.add(field, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = column;
        c.gridwidth = width;
        c.weightx = width;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 18, column == 0 && width == 1 ? 18 : 0);
        form.add(block, c);
    }

    private JTextField field() {
        JTextField field = new JTextField(18);
        Ui.field(field);
        field.setPreferredSize(new Dimension(240, 40));
        field.setMinimumSize(new Dimension(220, 40));
        return field;
    }

    private JSpinner amountSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 99999.0, 1.0));
        spinner.setFont(Ui.font(16, Font.PLAIN));
        spinner.setPreferredSize(new Dimension(240, 40));
        spinner.setMinimumSize(new Dimension(220, 40));
        return spinner;
    }

    private BigDecimal spinnerAmount(JSpinner spinner) {
        return BigDecimal.valueOf(((Number) spinner.getValue()).doubleValue());
    }

    private JComboBox<String> bloodTypeBox() {
        JComboBox<String> box = new JComboBox<>(new String[] {"A型", "B型", "AB型", "O型"});
        box.setFont(Ui.font(16, Font.PLAIN));
        box.setPreferredSize(new Dimension(240, 40));
        return box;
    }

    private JTextArea area(int rows) {
        JTextArea area = new JTextArea(rows, 18);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(Ui.font(15, Font.PLAIN));
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return area;
    }

    private DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(Ui.font(14, Font.PLAIN));
        table.getTableHeader().setFont(Ui.font(14, Font.BOLD));
        table.getTableHeader().setBackground(new Color(238, 235, 229));
        table.getTableHeader().setForeground(Ui.TEXT);
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(new Color(238, 235, 229));
        headerRenderer.setForeground(Ui.TEXT);
        headerRenderer.setFont(Ui.font(14, Font.BOLD));
        headerRenderer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Ui.BORDER));
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        table.setGridColor(Ui.BORDER);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(248, 226, 228));
        table.setSelectionForeground(Ui.TEXT);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        table.setDefaultRenderer(Object.class, cellRenderer);
        return table;
    }

    private void setColumnWidths(JTable table, int... widths) {
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void hideFirstColumn(JTable table) {
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
    }

    private void addRow(JPanel panel, int row, String labelText, java.awt.Component field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 12, 12);
        JLabel label = new JLabel(labelText);
        label.setForeground(Ui.TEXT);
        label.setFont(Ui.font(14, Font.PLAIN));
        panel.add(label, c);

        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
    }

    private void loadCategories(JComboBox<Option> box) {
        DefaultComboBoxModel<Option> model = new DefaultComboBoxModel<>();
        for (Map<String, Object> row : businessService.findCategories()) {
            if (row.get("parent_id") != null) {
                model.addElement(new Option(((Number) row.get("category_id")).longValue(), String.valueOf(row.get("name"))));
            }
        }
        box.setModel(model);
    }

    private void loadParentCategories(JComboBox<Option> box, Long currentId) {
        DefaultComboBoxModel<Option> model = new DefaultComboBoxModel<>();
        model.addElement(new Option(0L, "无父分类"));
        List<Map<String, Object>> rows = businessService.findCategories();
        for (Map<String, Object> row : rows) {
            long id = ((Number) row.get("category_id")).longValue();
            if (currentId == null || (id != currentId && !isDescendant(rows, id, currentId))) {
                model.addElement(new Option(id, String.valueOf(row.get("name"))));
            }
        }
        box.setModel(model);
    }

    private void loadItemOptions(JComboBox<Option> box) {
        DefaultComboBoxModel<Option> model = new DefaultComboBoxModel<>();
        for (Map<String, Object> row : businessService.findItems()) {
            if (((Number) row.get("status")).intValue() == 1) {
                model.addElement(new Option(((Number) row.get("item_id")).longValue(), String.valueOf(row.get("title"))));
            }
        }
        box.setModel(model);
    }

    private void loadCategories(DefaultTableModel model) {
        try {
            List<Map<String, Object>> rows = businessService.findCategories();
            Map<Long, String> names = categoryNames(rows);
            model.setRowCount(0);
            for (Map<String, Object> row : rows) {
                Object parentId = row.get("parent_id");
                model.addRow(new Object[] {
                    row.get("category_id"),
                    row.get("name"),
                    parentId == null ? "无父分类" : names.getOrDefault(((Number) parentId).longValue(), "父分类已删除"),
                    parentId == null ? "一级分类" : "子分类"
                });
            }
        } catch (RuntimeException ex) {
            warn("分类加载失败，请检查数据库连接。");
        }
    }

    private void loadChildCategories(DefaultTableModel model, long parentId) {
        try {
            model.setRowCount(0);
            for (Map<String, Object> row : businessService.findCategories()) {
                Object rowParentId = row.get("parent_id");
                if (rowParentId != null && ((Number) rowParentId).longValue() == parentId) {
                    model.addRow(new Object[] {
                        row.get("category_id"),
                        row.get("name")
                    });
                }
            }
        } catch (RuntimeException ex) {
            warn("子分类加载失败，请检查数据库连接。");
        }
    }

    private Map<String, Object> findItemRow(long itemId) {
        for (Map<String, Object> row : businessService.findItems()) {
            if (((Number) row.get("item_id")).longValue() == itemId) {
                return row;
            }
        }
        return null;
    }

    private Map<String, Object> findCategoryRow(long categoryId) {
        for (Map<String, Object> row : businessService.findCategories()) {
            if (((Number) row.get("category_id")).longValue() == categoryId) {
                return row;
            }
        }
        return null;
    }

    private void selectOption(JComboBox<Option> box, long id) {
        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).id() == id) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectOptionByLabel(JComboBox<Option> box, String label) {
        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).label().equals(label)) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    private java.util.Optional<Map<String, Object>> findOrderRow(DefaultTableModel model, long orderId) {
        for (int row = 0; row < model.getRowCount(); row++) {
            if (((Number) model.getValueAt(row, 0)).longValue() == orderId) {
                return java.util.Optional.of(Map.of(
                    "item", model.getValueAt(row, 1),
                    "amount", model.getValueAt(row, 2),
                    "status", model.getValueAt(row, 3)
                ));
            }
        }
        return java.util.Optional.empty();
    }

    private boolean isPendingOrder(DefaultTableModel model, long orderId) {
        return findOrderRow(model, orderId)
            .map(row -> "待处理".equals(row.get("status")))
            .orElse(false);
    }

    private void loadItems(DefaultTableModel model) {
        try {
            Map<Long, String> categories = categoryNames();
            model.setRowCount(0);
            for (Map<String, Object> row : businessService.findItems()) {
                model.addRow(new Object[] {
                    row.get("item_id"),
                    row.get("title"),
                    categories.getOrDefault(((Number) row.get("category_id")).longValue(), "未分类"),
                    amountText(row.get("amount")),
                    itemStatus(row.get("status"))
                });
            }
        } catch (RuntimeException ex) {
            warn("库存加载失败，请检查数据库连接。");
        }
    }

    private void loadOrders(DefaultTableModel model, UserSession session) {
        try {
            Map<Long, String> items = itemNames();
            model.setRowCount(0);
            for (Map<String, Object> row : businessService.findOrders(session.userId(), isAdmin(session))) {
                model.addRow(new Object[] {
                    row.get("order_id"),
                    items.getOrDefault(((Number) row.get("item_id")).longValue(), "库存批次已删除"),
                    amountText(row.get("amount")),
                    orderStatus(row.get("status"))
                });
            }
        } catch (RuntimeException ex) {
            warn("记录加载失败，请检查数据库连接。");
        }
    }

    private Map<Long, String> categoryNames() {
        return categoryNames(businessService.findCategories());
    }

    private Map<Long, String> categoryNames(List<Map<String, Object>> rows) {
        java.util.HashMap<Long, String> names = new java.util.HashMap<>();
        for (Map<String, Object> row : rows) {
            names.put(((Number) row.get("category_id")).longValue(), String.valueOf(row.get("name")));
        }
        return names;
    }

    private Map<Long, String> itemNames() {
        java.util.HashMap<Long, String> names = new java.util.HashMap<>();
        for (Map<String, Object> row : businessService.findItems()) {
            names.put(((Number) row.get("item_id")).longValue(), String.valueOf(row.get("title")));
        }
        return names;
    }

    private Option selected(JComboBox<Option> box) {
        Object value = box.getSelectedItem();
        return value instanceof Option option ? option : null;
    }

    private Long selectedParentId(JComboBox<Option> box) {
        Option option = selected(box);
        return option == null || option.id() == 0L ? null : option.id();
    }

    private boolean isDescendant(List<Map<String, Object>> rows, long categoryId, long parentId) {
        for (Map<String, Object> row : rows) {
            Object rowParentId = row.get("parent_id");
            long rowId = ((Number) row.get("category_id")).longValue();
            if (rowParentId != null && ((Number) rowParentId).longValue() == parentId) {
                if (rowId == categoryId || isDescendant(rows, categoryId, rowId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAdmin(UserSession session) {
        return "ADMIN".equals(session.role());
    }

    private Long selectedId(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        Object value = table.getModel().getValueAt(table.convertRowIndexToModel(row), 0);
        return ((Number) value).longValue();
    }

    private String itemStatus(Object status) {
        return ((Number) status).intValue() == 1 ? "可用" : "停用";
    }

    private String amountText(Object amount) {
        return amount instanceof BigDecimal value ? value.toPlainString() : String.valueOf(amount);
    }

    private String orderStatus(Object status) {
        int value = ((Number) status).intValue();
        return switch (value) {
            case 1 -> "已完成";
            case 2 -> "已取消";
            default -> "待处理";
        };
    }

    private void showResult(BusinessResult result) {
        if (result.success()) {
            JOptionPane.showMessageDialog(this, result.message(), "操作成功", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        warn(result.message());
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "操作失败", JOptionPane.WARNING_MESSAGE);
    }

    private boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "确认操作", JOptionPane.OK_CANCEL_OPTION)
            == JOptionPane.OK_OPTION;
    }

    private void refreshMain() {
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

    private record Option(long id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
