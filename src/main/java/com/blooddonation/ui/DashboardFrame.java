package com.blooddonation.ui;

import com.blooddonation.service.AuthService.UserSession;
import com.blooddonation.service.BusinessService;
import com.blooddonation.service.BusinessService.BusinessResult;
import com.blooddonation.dto.ItemInsightDTO;
import com.blooddonation.dto.RecommendationDTO;
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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RowFilter;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import org.bson.Document;

/**
 * 根据当前用户角色展示库存、申请、报表和管理功能。
 */
public class DashboardFrame extends JFrame {
    private final BusinessService businessService;
    private final List<JButton> navButtons = new ArrayList<>();
    private final JPanel mainPanel = new JPanel(new BorderLayout(0, 18));

    /**
     * 使用默认业务服务创建主界面。
     *
     * @param session 当前登录会话
     */
    public DashboardFrame(UserSession session) {
        this(session, new BusinessService());
    }

    /** 使用指定业务服务创建可测试的主界面。 */
    DashboardFrame(UserSession session, BusinessService businessService) {
        this.businessService = businessService;
        setTitle("献血管理系统");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);
        setContentPane(content(session));
    }

    /** @return 当前会话对应的主界面内容 */
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

    /** @return 按角色生成的侧边导航栏 */
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
        String[] modules = isAdmin(session)
            ? new String[] {"业务数据", "推荐批次", "订单记录", "用户管理", "分类管理", "统计报表", "系统日志"}
            : new String[] {"业务数据", "推荐批次", "订单记录", "我的档案", "统计报表"};
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

        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setBackground(Ui.SIDEBAR);
        JLabel user = new JLabel("<html>" + session.username() + "<br>" + session.role() + "</html>");
        user.setForeground(Ui.PANEL);
        user.setFont(Ui.font(14, Font.PLAIN));
        footer.add(user, BorderLayout.NORTH);
        JButton logout = new JButton("登出");
        logout.setFocusPainted(false);
        logout.setForeground(Ui.PANEL);
        logout.setBackground(new Color(61, 65, 75));
        logout.setOpaque(true);
        logout.setContentAreaFilled(true);
        logout.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        logout.addActionListener(event -> {
            if (confirm("确认登出当前账号？")) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        footer.add(logout, BorderLayout.SOUTH);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    /** 切换到指定业务模块。 */
    private void showModule(String module, UserSession session) {
        if ("业务数据".equals(module)) {
            showBusinessPanel(session);
            return;
        }
        if ("订单记录".equals(module)) {
            showOrderPanel(session);
            return;
        }
        if ("推荐批次".equals(module)) {
            showRecommendPanel(session);
            return;
        }
        if ("用户管理".equals(module)) {
            showUserProfilePanel(session);
            return;
        }
        if ("我的档案".equals(module)) {
            showOwnProfilePanel(session);
            return;
        }
        if ("分类管理".equals(module)) {
            if (isAdmin(session)) {
                showCategoryPanel(session);
            }
            return;
        }
        if ("统计报表".equals(module)) {
            showStatisticsPanel(session);
            return;
        }
        if ("系统日志".equals(module)) {
            if (isAdmin(session)) {
                showLogPanel(session);
            }
        }
    }

    /** 显示库存批次管理页面。 */
    private void showBusinessPanel(UserSession session) {
        resetMain("业务数据", "维护血液库存批次，双击表格行打开详情。");

        DefaultTableModel model = tableModel("item_id", "库存批次", "分类", "血型", "数量", "状态", "评论", "行为", "订单");
        JTable table = table(model);
        hideFirstColumn(table);
        setColumnWidths(table, 0, 280, 110, 90, 90, 90, 90, 90, 90);

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
            Ui.toolbarButton(addButton, 96, true);
            addButton.addActionListener(event -> showCreateItemDialog(model));
            actions.add(addButton);
            JButton deleteButton = new JButton("删除");
            Ui.toolbarButton(deleteButton, 96, false);
            deleteButton.addActionListener(event -> {
                Long itemId = selectedId(table);
                if (itemId == null) {
                    warn("请先在表格中选择库存批次。");
                    return;
                }
                if (confirm("确认删除选中的库存批次？")) {
                    showResult(businessService.deleteItem(itemId));
                    reloadItemRows(model);
                    closeDetailTab(tabs, "item:" + itemId);
                }
            });
            actions.add(deleteButton);
        }

        JPanel listPanel = section(
            "库存列表",
            filteredTablePanel(table, filterBar(table, new FilterChoice("状态", 5), new FilterChoice("血型", 3), new FilterChoice("分类", 2))),
            isAdmin(session) ? actions : null
        );
        tabs.add(listPanel, "list");
        mainPanel.add(tabs, BorderLayout.CENTER);
        reloadItemRows(model);
        refreshMain();
    }

    /** 显示库存推荐页面。 */
    private void showRecommendPanel(UserSession session) {
        resetMain("推荐批次", "根据你的记录和热门数据推荐可用库存批次。");

        DefaultTableModel model = tableModel("item_id", "库存批次", "分类", "血型", "数量", "评分", "推荐理由");
        JTable table = table(model);
        hideFirstColumn(table);
        setColumnWidths(table, 0, 300, 110, 90, 90, 90, 220);

        JPanel tabs = tabs();
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Long itemId = selectedId(table);
                    if (itemId != null) {
                        openInsightDetailTab(tabs, itemId, session);
                    }
                }
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Ui.PANEL);
        JButton applyButton = new JButton("申请");
        Ui.toolbarButton(applyButton, 96, true);
        applyButton.addActionListener(event -> {
            Long itemId = selectedId(table);
            if (itemId == null) {
                warn("请先选择推荐批次。");
                return;
            }
            showApplyOrderDialog(session, itemId, itemTitle(table, itemId));
        });
        actions.add(applyButton);

        tabs.add(section("推荐列表", tableScroll(table), actions), "list");
        mainPanel.add(tabs, BorderLayout.CENTER);
        loadRecommendations(model, session);
        refreshMain();
    }

    /** 打开新增库存批次对话框。 */
    private void showCreateItemDialog(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "新增库存", true);
        JTextField titleField = field();
        JComboBox<Option> categoryBox = new JComboBox<>();
        Ui.comboBox(categoryBox, 240);
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
                    reloadItemRows(model);
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

    /** 打开库存批次编辑对话框。 */
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
        Ui.comboBox(categoryBox, 240);
        loadCategories(categoryBox);
        selectOption(categoryBox, ((Number) item.get("category_id")).longValue());
        JSpinner amountSpinner = amountSpinner();
        amountSpinner.setValue(((BigDecimal) item.get("amount")).doubleValue());
        JComboBox<String> statusBox = new JComboBox<>(new String[] {"停用", "可用"});
        Ui.comboBox(statusBox, 240);
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
                    reloadItemRows(model);
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

    /** 打开用户档案编辑对话框。 */
    private void showEditUserProfileDialog(long userId, UserSession session, DefaultTableModel model, Runnable onSaved) {
        Map<String, Object> row = findUserProfileRow(userId, session);
        if (row == null) {
            warn("用户不存在。");
            return;
        }

        JDialog dialog = new JDialog(this, "编辑用户档案", true);
        JTextField usernameField = field();
        usernameField.setText(String.valueOf(row.get("username")));
        usernameField.setEditable(false);
        JTextField emailField = field();
        emailField.setText(valueText(row.get("email")));
        JTextField phoneField = field();
        phoneField.setText(valueText(row.get("phone")));
        JTextField realNameField = field();
        realNameField.setText(valueText(row.get("real_name")));
        JTextField idCardField = field();
        idCardField.setText(valueText(row.get("id_card")));
        JTextField addressField = field();
        addressField.setText(valueText(row.get("address")));
        JTextArea notesArea = area(4);
        notesArea.setText(valueText(row.get("notes")));
        JComboBox<String> statusBox = new JComboBox<>(new String[] {"禁用", "启用"});
        Ui.comboBox(statusBox, 240);
        statusBox.setSelectedIndex(((Number) row.get("status")).intValue());
        statusBox.setEnabled(isAdmin(session));
        JComboBox<String> roleBox = new JComboBox<>(new String[] {"普通用户", "管理员"});
        Ui.comboBox(roleBox, 240);
        roleBox.setSelectedIndex("ADMIN".equals(row.get("role")) ? 1 : 0);
        roleBox.setEnabled(isSuperAdmin(session) && !isSuperAdmin(userId));

        JPanel form = userProfileForm(usernameField, roleBox, statusBox, emailField, phoneField, realNameField, idCardField, addressField, notesArea);

        JButton cancelButton = new JButton("取消");
        Ui.textButton(cancelButton);
        cancelButton.addActionListener(event -> dialog.dispose());
        JButton saveButton = new JButton("保存");
        Ui.primaryButton(saveButton, 112);
        saveButton.addActionListener(event -> {
            try {
                BusinessResult result = businessService.saveUserProfile(
                    session.userId(),
                    isAdmin(session),
                    userId,
                    emailField.getText(),
                    phoneField.getText(),
                    selectedRole(roleBox),
                    statusBox.getSelectedIndex(),
                    realNameField.getText(),
                    idCardField.getText(),
                    addressField.getText(),
                    notesArea.getText()
                );
                showResult(result);
                if (result.success()) {
                    loadUserProfiles(model, session);
                    onSaved.run();
                    dialog.dispose();
                }
            } catch (RuntimeException ex) {
                warn("保存失败，请检查邮箱、证件号是否重复。");
            }
        });

        dialog.setContentPane(dialogContent("编辑用户档案", form, cancelButton, saveButton));
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(760, 620));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /** 显示用血申请页面。 */
    private void showOrderPanel(UserSession session) {
        resetMain("订单记录", "查看用血记录，双击表格行打开详情。");

        DefaultTableModel model = tableModel("order_id", "库存批次", "数量", "状态", "分类", "血型");
        JTable table = table(model);
        hideFirstColumn(table);
        hideColumn(table, 4);
        hideColumn(table, 5);
        setColumnWidths(table, 0, 420, 120, 120, 0, 0);

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
        Ui.toolbarButton(createButton, 96, true);
        createButton.addActionListener(event -> showCreateOrderDialog(session, model));
        actions.add(createButton);
        if (isAdmin(session)) {
            JButton deleteButton = new JButton("删除");
            Ui.toolbarButton(deleteButton, 96, false);
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

        tabs.add(section(
            "我的记录",
            filteredTablePanel(table, filterBar(table, new FilterChoice("状态", 3), new FilterChoice("血型", 5), new FilterChoice("分类", 4))),
            actions.getComponentCount() > 0 ? actions : null
        ), "list");
        mainPanel.add(tabs, BorderLayout.CENTER);
        loadOrders(model, session);
        refreshMain();
    }

    /** 显示分类管理页面。 */
    private void showCategoryPanel(UserSession session) {
        if (!isAdmin(session)) {
            showBusinessPanel(session);
            return;
        }
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
            Ui.toolbarButton(addButton, 96, true);
            addButton.addActionListener(event -> showCreateCategoryDialog(model));
            actions.add(addButton);

            JButton deleteButton = new JButton("删除");
            Ui.toolbarButton(deleteButton, 96, false);
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

        tabs.add(section("分类列表", tableScroll(table), isAdmin(session) ? actions : null), "list");
        mainPanel.add(tabs, BorderLayout.CENTER);
        loadCategories(model);
        refreshMain();
    }

    /** 显示管理员用户档案页面。 */
    private void showUserProfilePanel(UserSession session) {
        if (!isAdmin(session)) {
            showOwnProfilePanel(session);
            return;
        }

        resetMain("用户管理", "维护用户权限、状态和档案信息，双击表格行打开详情。");

        DefaultTableModel model = tableModel("user_id", "用户名", "角色", "状态", "邮箱", "手机号", "姓名", "证件号");
        JTable table = table(model);
        hideFirstColumn(table);
        setColumnWidths(table, 0, 150, 90, 90, 220, 140, 130, 170);

        JPanel tabs = tabs();
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Long userId = selectedId(table);
                    if (userId != null) {
                        openUserProfileDetailTab(tabs, table, userId, session, model);
                    }
                }
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Ui.PANEL);
        if (isAdmin(session)) {
            JButton deleteButton = new JButton("删除");
            Ui.toolbarButton(deleteButton, 96, false);
            deleteButton.addActionListener(event -> {
                Long userId = selectedId(table);
                if (userId == null) {
                    warn("请先在表格中选择用户。");
                    return;
                }
                if (confirm("确认删除选中的用户？")) {
                    BusinessResult result = businessService.deleteUser(session.userId(), true, userId);
                    showResult(result);
                    if (result.success()) {
                        loadUserProfiles(model, session);
                        closeDetailTab(tabs, "user:" + userId);
                    }
                }
            });
            actions.add(deleteButton);
        }

        tabs.add(section(
            "用户列表",
            filteredTablePanel(table, filterBar(table, new FilterChoice("状态", 3), new FilterChoice("角色", 2))),
            actions.getComponentCount() > 0 ? actions : null
        ), "list");
        mainPanel.add(tabs, BorderLayout.CENTER);
        loadUserProfiles(model, session);
        refreshMain();
    }

    /** 显示当前用户档案页面。 */
    private void showOwnProfilePanel(UserSession session) {
        resetMain("我的档案", "查看和维护自己的联系方式与档案。");

        DefaultTableModel model = tableModel("user_id", "用户名", "角色", "状态", "邮箱", "手机号", "姓名", "证件号");
        loadUserProfiles(model, session);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Ui.PANEL);
        JButton editButton = new JButton("编辑");
        Ui.toolbarButton(editButton, 96, true);
        editButton.addActionListener(event -> showEditUserProfileDialog(session.userId(), session, model, () -> {
            loadUserProfiles(model, session);
            showOwnProfilePanel(session);
        }));
        actions.add(editButton);

        mainPanel.add(detailPage("我的档案", userProfileDetailBody(model, session.userId()), actions), BorderLayout.CENTER);
        refreshMain();
    }

    /** 显示当前角色可查看的统计页面。 */
    private void showStatisticsPanel(UserSession session) {
        if (!isAdmin(session)) {
            showUserStatisticsPanel(session);
            return;
        }

        resetMain("统计报表", "查看批次操作热度、评论评分和月度用血报表。");

        JTabbedPane tabs = innerTabs();

        BarChartPanel topItems = new BarChartPanel(0, " 次");
        tabs.addTab("操作热度", section("操作热度", chartScroll(topItems)));

        BarChartPanel ratings = new BarChartPanel(5, " / 5");
        tabs.addTab("评论评分", section("评论评分", chartScroll(ratings)));

        YearMonth[] selectedMonth = {YearMonth.now()};
        BarChartPanel monthly = new BarChartPanel(0, " 单位");
        JLabel period = new JLabel(monthLabel(selectedMonth[0]), SwingConstants.CENTER);
        period.setFont(Ui.font(14, Font.BOLD));
        period.setForeground(Ui.TEXT);
        period.setPreferredSize(new Dimension(136, 38));
        period.setBorder(BorderFactory.createLineBorder(Ui.BORDER));
        JButton previous = new JButton("<");
        Ui.toolbarButton(previous, 42, false);
        previous.setFont(Ui.font(16, Font.BOLD));
        previous.setBorder(BorderFactory.createLineBorder(Ui.BORDER));
        JButton next = new JButton(">");
        Ui.toolbarButton(next, 42, false);
        next.setFont(Ui.font(16, Font.BOLD));
        next.setBorder(BorderFactory.createLineBorder(Ui.BORDER));
        previous.addActionListener(event -> {
            selectedMonth[0] = selectedMonth[0].minusMonths(1);
            period.setText(monthLabel(selectedMonth[0]));
            loadMonthlyReport(monthly, selectedMonth[0]);
        });
        next.addActionListener(event -> {
            selectedMonth[0] = selectedMonth[0].plusMonths(1);
            period.setText(monthLabel(selectedMonth[0]));
            loadMonthlyReport(monthly, selectedMonth[0]);
        });

        JPanel monthlyActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        monthlyActions.setBackground(Ui.PANEL);
        monthlyActions.add(previous);
        monthlyActions.add(period);
        monthlyActions.add(next);
        tabs.addTab("月度报表", section("月度报表", chartScroll(monthly), monthlyActions));
        addUserStatisticsTabs(tabs, session);

        loadStatistics(topItems, ratings);
        loadMonthlyReport(monthly, selectedMonth[0]);
        mainPanel.add(tabs, BorderLayout.CENTER);
        refreshMain();
    }

    /** 显示普通用户个人统计页面。 */
    private void showUserStatisticsPanel(UserSession session) {
        resetMain("统计报表", "查看你的订单、评论和月度申请统计。");

        JTabbedPane tabs = innerTabs();
        addUserStatisticsTabs(tabs, session);

        mainPanel.add(tabs, BorderLayout.CENTER);
        refreshMain();
    }

    /** 向标签页添加个人申请和评论统计。 */
    private void addUserStatisticsTabs(JTabbedPane tabs, UserSession session) {
        BarChartPanel statuses = new BarChartPanel(0, " 单");
        tabs.addTab("订单状态", section("订单状态", chartScroll(statuses)));

        BarChartPanel categories = new BarChartPanel(0, " 单");
        tabs.addTab("分类分布", section("分类分布", chartScroll(categories)));

        BarChartPanel bloodTypes = new BarChartPanel(0, " 单");
        tabs.addTab("血型分布", section("血型分布", chartScroll(bloodTypes)));

        BarChartPanel months = new BarChartPanel(0, " 单位");
        tabs.addTab("月度趋势", section("月度趋势", chartScroll(months)));

        BarChartPanel ratings = new BarChartPanel(5, " / 5");
        tabs.addTab("我的评分", section("我的评分", chartScroll(ratings)));

        loadUserStatistics(statuses, categories, bloodTypes, months, ratings, session);
    }

    /** 显示行为日志、登录日志和审计统计。 */
    private void showLogPanel(UserSession session) {
        if (!isAdmin(session)) {
            showBusinessPanel(session);
            return;
        }
        resetMain("系统日志", isAdmin(session) ? "查看业务操作和登录日志。" : "查看自己的业务操作记录。");

        JTabbedPane tabs = innerTabs();

        DefaultTableModel actions = tableModel("用户", "库存批次", "操作", "时间");
        JTable actionsTable = table(actions);
        tabs.addTab("行为日志", section(
            "行为日志",
            filteredTablePanel(actionsTable, filterBar(actionsTable, new FilterChoice("操作", 2), new FilterChoice("批次", 1)))
        ));

        DefaultTableModel logins = tableModel("用户", "级别", "消息", "时间");
        JTable loginTable = table(logins);
        if (isAdmin(session)) {
            tabs.addTab("登录日志", section(
                "登录日志",
                filteredTablePanel(loginTable, filterBar(loginTable, new FilterChoice("级别", 1)))
            ));

            DefaultTableModel audit = tableModel("日志类型", "级别", "数量", "最近时间");
            JTable auditTable = table(audit);
            tabs.addTab("操作审计", section(
                "操作审计",
                filteredTablePanel(auditTable, filterBar(auditTable, new FilterChoice("级别", 1), new FilterChoice("类型", 0)))
            ));
            loadLogs(actions, logins, audit, session);
        } else {
            loadLogs(actions, logins, null, session);
        }

        mainPanel.add(tabs, BorderLayout.CENTER);
        refreshMain();
    }

    /** 打开新增顶级分类对话框。 */
    private void showCreateCategoryDialog(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "新增分类", true);
        JTextField nameField = field();
        JComboBox<Option> parentBox = new JComboBox<>();
        Ui.comboBox(parentBox, 240);
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

    /** 打开新增子分类对话框。 */
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

    /** 打开分类编辑对话框。 */
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
        Ui.comboBox(parentBox, 240);
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

    /** @return 分类名称和父分类表单 */
    private JPanel categoryForm(JTextField nameField, JComboBox<Option> parentBox) {
        JPanel form = formPanel();
        form.setBorder(BorderFactory.createEmptyBorder(22, 24, 12, 24));
        addFormField(form, 0, 0, 2, "分类名称", nameField);
        addFormField(form, 1, 0, 2, "父分类", parentBox);
        return form;
    }

    /** 打开新增用血申请对话框。 */
    private void showCreateOrderDialog(UserSession session, DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "创建用血记录", true);
        JComboBox<Option> itemBox = new JComboBox<>();
        Ui.comboBox(itemBox, 240);
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

    /** 打开指定库存的快速申请对话框。 */
    private void showApplyOrderDialog(UserSession session, long itemId, String itemTitle) {
        JDialog dialog = new JDialog(this, "申请用血", true);
        JSpinner amountSpinner = amountSpinner();

        JPanel form = formPanel();
        form.setBorder(BorderFactory.createEmptyBorder(22, 24, 12, 24));
        addFormField(form, 0, 0, 2, "库存批次", infoBlock("批次", itemTitle));
        addFormField(form, 1, 0, 2, "数量", amountSpinner);

        JButton cancelButton = new JButton("取消");
        Ui.textButton(cancelButton);
        cancelButton.addActionListener(event -> dialog.dispose());
        JButton saveButton = new JButton("申请");
        Ui.primaryButton(saveButton, 112);
        saveButton.addActionListener(event -> {
            try {
                BusinessResult result = businessService.createOrder(session.userId(), itemId, spinnerAmount(amountSpinner));
                showResult(result);
                if (result.success()) {
                    dialog.dispose();
                }
            } catch (RuntimeException ex) {
                warn("申请失败，请检查数据库连接。");
            }
        });

        dialog.setContentPane(dialogContent("申请用血", form, cancelButton, saveButton));
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(560, 320));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /** 打开用户自己的待审批申请编辑对话框。 */
    private void showEditOwnOrderDialog(long orderId, UserSession session, DefaultTableModel model, Runnable onSaved) {
        JDialog dialog = new JDialog(this, "编辑用血申请", true);
        JComboBox<Option> itemBox = new JComboBox<>();
        Ui.comboBox(itemBox, 240);
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

    /** 打开管理员申请状态更新对话框。 */
    private void showUpdateOrderDialog(long orderId, UserSession session, DefaultTableModel model, Runnable onSaved) {
        JDialog dialog = new JDialog(this, "更新处理状态", true);
        JComboBox<String> statusBox = new JComboBox<>(new String[] {"待处理", "已完成", "已取消"});
        Ui.comboBox(statusBox, 240);

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

    /** 显示尚未提供实际内容的占位页面。 */
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

    /** 重置主内容区域并显示页面标题。 */
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

    /** @return 带标题的内容分区 */
    private JPanel section(String title, java.awt.Component body) {
        return section(title, body, null);
    }

    /** @return 带标题和操作区的内容分区 */
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

    /** @return 组合筛选栏与表格的面板 */
    private JPanel filteredTablePanel(JTable table, JPanel filters) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Ui.PANEL);
        panel.add(filters, BorderLayout.NORTH);
        panel.add(tableScroll(table), BorderLayout.CENTER);
        return panel;
    }

    /** @return 使用统一样式包装表格的滚动面板 */
    private JScrollPane tableScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Ui.BORDER));
        scroll.getViewport().setBackground(Ui.PANEL);
        scroll.setBackground(Ui.PANEL);
        return scroll;
    }

    /** @return 支持关键词和列值过滤的筛选栏 */
    private JPanel filterBar(JTable table, FilterChoice... choices) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) table.getModel());
        table.setRowSorter(sorter);

        JTextField keyword = new JTextField(18);
        Ui.field(keyword);
        keyword.setPreferredSize(new Dimension(320, 44));
        keyword.setMinimumSize(new Dimension(320, 44));

        List<JComboBox<String>> boxes = new ArrayList<>();
        JPanel panel = new JPanel(new GridLayout(1, choices.length + 1, 12, 0));
        panel.setBackground(Ui.PANEL);
        panel.add(filterBlock("关键词", keyword));
        for (FilterChoice choice : choices) {
            JComboBox<String> box = new JComboBox<>();
            Ui.comboBox(box, 180);
            boxes.add(box);
            panel.add(filterBlock(choice.label(), box));
        }

        Runnable apply = () -> sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String query = keyword.getText().trim().toLowerCase();
                if (!query.isEmpty() && !rowContains(entry, query)) {
                    return false;
                }
                for (int i = 0; i < choices.length; i++) {
                    String selected = String.valueOf(boxes.get(i).getSelectedItem());
                    if (!"全部".equals(selected) && !selected.equals(cellText(entry, choices[i].column()))) {
                        return false;
                    }
                }
                return true;
            }
        });
        Runnable refreshOptions = () -> {
            for (int i = 0; i < choices.length; i++) {
                Object selectedItem = boxes.get(i).getSelectedItem();
                String selected = selectedItem == null ? "全部" : String.valueOf(selectedItem);
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                model.addElement("全部");
                for (String value : columnValues(table, choices[i].column())) {
                    model.addElement(value);
                }
                boxes.get(i).setModel(model);
                boxes.get(i).setSelectedItem(selected.isBlank() ? "全部" : selected);
            }
        };

        keyword.getDocument().addDocumentListener(new SimpleDocumentListener(apply));
        for (JComboBox<String> box : boxes) {
            box.addActionListener(event -> apply.run());
        }
        table.getModel().addTableModelListener(event -> SwingUtilities.invokeLater(() -> {
            refreshOptions.run();
            apply.run();
        }));
        refreshOptions.run();
        apply.run();
        return panel;
    }

    /** @return 带标签的筛选控件块 */
    private JPanel filterBlock(String labelText, java.awt.Component field) {
        JPanel block = new JPanel(new BorderLayout(0, 4));
        block.setBackground(Ui.PANEL);
        JLabel label = new JLabel(labelText);
        label.setForeground(new Color(91, 94, 102));
        label.setFont(Ui.font(12, Font.BOLD));
        block.add(label, BorderLayout.NORTH);
        block.add(field, BorderLayout.CENTER);
        return block;
    }

    /** @return 表格指定列中的去重文本值 */
    private Set<String> columnValues(JTable table, int column) {
        Set<String> values = new LinkedHashSet<>();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        for (int row = 0; row < model.getRowCount(); row++) {
            String value = String.valueOf(model.getValueAt(row, column));
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    /** @return 当前表格行是否包含查询文本 */
    private boolean rowContains(RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry, String query) {
        for (int column = 0; column < entry.getValueCount(); column++) {
            if (cellText(entry, column).toLowerCase().contains(query)) {
                return true;
            }
        }
        return false;
    }

    /** @return 当前表格行指定列的文本 */
    private String cellText(RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry, int column) {
        Object value = entry.getValue(column);
        return value == null ? "" : String.valueOf(value);
    }

    /** @return 主界面使用的标签页容器 */
    private JPanel tabs() {
        JPanel tabs = new JPanel(new CardLayout());
        tabs.setBackground(Ui.PAGE);
        return tabs;
    }

    /** @return 使用统一样式的内层标签页 */
    private JTabbedPane innerTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                tabAreaInsets = new Insets(0, 0, 0, 0);
                contentBorderInsets = new Insets(0, 0, 0, 0);
                tabInsets = new Insets(10, 18, 10, 18);
                selectedTabPadInsets = new Insets(0, 0, 0, 0);
            }

            @Override
            protected void paintContentBorder(java.awt.Graphics g, int tabPlacement, int selectedIndex) {
            }

            @Override
            protected void paintTabBorder(
                java.awt.Graphics g,
                int tabPlacement,
                int tabIndex,
                int x,
                int y,
                int w,
                int h,
                boolean isSelected
            ) {
            }

            @Override
            protected void paintTabBackground(
                java.awt.Graphics g,
                int tabPlacement,
                int tabIndex,
                int x,
                int y,
                int w,
                int h,
                boolean isSelected
            ) {
                g.setColor(Ui.PAGE);
                g.fillRect(x, y, w, h);
                if (isSelected) {
                    g.setColor(Ui.PRIMARY);
                    g.fillRect(x + 14, y + h - 3, w - 28, 3);
                }
            }

            @Override
            protected void paintFocusIndicator(
                java.awt.Graphics g,
                int tabPlacement,
                java.awt.Rectangle[] rects,
                int tabIndex,
                java.awt.Rectangle iconRect,
                java.awt.Rectangle textRect,
                boolean isSelected
            ) {
            }
        });
        tabs.setFont(Ui.font(14, Font.BOLD));
        tabs.setBackground(Ui.PAGE);
        tabs.setForeground(Ui.TEXT);
        tabs.setBorder(BorderFactory.createEmptyBorder());
        return tabs;
    }

    /** @return 包装柱状图的滚动面板 */
    private JScrollPane chartScroll(BarChartPanel chart) {
        JScrollPane scroll = new JScrollPane(chart);
        scroll.setBorder(BorderFactory.createLineBorder(Ui.BORDER));
        scroll.getViewport().setBackground(Ui.PANEL);
        scroll.setBackground(Ui.PANEL);
        return scroll;
    }

    /** 打开库存详情标签页。 */
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
        Ui.toolbarButton(closeButton, 96, false);
        closeButton.addActionListener(event -> closeDetailTab(tabs, key));
        actions.add(closeButton);

        if (isAdmin(session)) {
            JButton editButton = new JButton("编辑");
            Ui.toolbarButton(editButton, 96, true);
            editButton.addActionListener(event -> showEditItemDialog(itemId, model, () -> {
                closeDetailTab(tabs, key);
                openItemDetailTab(tabs, table, itemId, model, session);
            }));
            actions.add(editButton);

            JButton deleteButton = new JButton("删除");
            Ui.toolbarButton(deleteButton, 96, false);
            deleteButton.addActionListener(event -> {
                if (confirm("确认删除该库存批次？")) {
                    showResult(businessService.deleteItem(itemId));
                    reloadItemRows(model);
                    closeDetailTab(tabs, key);
                }
            });
            actions.add(deleteButton);
        }

        JPanel detail = detailPage("批次详情", itemDetailBody(table, itemId, session, () -> {
            closeDetailTab(tabs, key);
            openItemDetailTab(tabs, table, itemId, model, session);
        }), actions);
        addDetailTab(tabs, detailTabTitle("批次", table, itemId, 1), detail, key);
    }

    /** 打开申请详情标签页。 */
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
        Ui.toolbarButton(closeButton, 96, false);
        closeButton.addActionListener(event -> closeDetailTab(tabs, key));
        actions.add(closeButton);

        if (isAdmin(session)) {
            JButton editButton = new JButton("编辑");
            Ui.toolbarButton(editButton, 96, true);
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
            Ui.toolbarButton(editButton, 96, true);
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

    /** 打开分类详情标签页。 */
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
        Ui.toolbarButton(closeButton, 96, false);
        closeButton.addActionListener(event -> closeDetailTab(tabs, key));
        actions.add(closeButton);

        if (isAdmin(session)) {
            JButton editButton = new JButton("编辑");
            Ui.toolbarButton(editButton, 96, true);
            editButton.addActionListener(event -> showEditCategoryDialog(categoryId, model, () -> {
                closeDetailTab(tabs, key);
                openCategoryDetailTab(tabs, table, categoryId, session, model);
            }));
            actions.add(editButton);

            JButton deleteButton = new JButton("删除");
            Ui.toolbarButton(deleteButton, 96, false);
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

    /** 打开用户档案详情标签页。 */
    private void openUserProfileDetailTab(
        JPanel tabs,
        JTable table,
        long userId,
        UserSession session,
        DefaultTableModel model
    ) {
        String key = "user:" + userId;
        if (selectDetailTab(tabs, key)) {
            return;
        }

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Ui.PANEL);
        JButton closeButton = new JButton("关闭");
        Ui.toolbarButton(closeButton, 96, false);
        closeButton.addActionListener(event -> closeDetailTab(tabs, key));
        actions.add(closeButton);

        JButton editButton = new JButton("编辑");
        Ui.toolbarButton(editButton, 96, true);
        editButton.addActionListener(event -> showEditUserProfileDialog(userId, session, model, () -> {
            closeDetailTab(tabs, key);
            openUserProfileDetailTab(tabs, table, userId, session, model);
        }));
        actions.add(editButton);

        if (isAdmin(session)) {
            JButton deleteButton = new JButton("删除");
            Ui.toolbarButton(deleteButton, 96, false);
            deleteButton.addActionListener(event -> {
                if (confirm("确认删除该用户？")) {
                    BusinessResult result = businessService.deleteUser(session.userId(), true, userId);
                    showResult(result);
                    if (result.success()) {
                        loadUserProfiles(model, session);
                        closeDetailTab(tabs, key);
                    }
                }
            });
            actions.add(deleteButton);
        }

        JPanel detail = detailPage("用户管理", userProfileDetailBody(model, userId), actions);
        addDetailTab(tabs, detailTabTitle("用户", table, userId, 1), detail, key);
    }

    /** 打开库存洞察详情标签页。 */
    private void openInsightDetailTab(JPanel tabs, long itemId, UserSession session) {
        String key = "insight:" + itemId;
        if (selectDetailTab(tabs, key)) {
            return;
        }

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Ui.PANEL);
        JButton closeButton = new JButton("关闭");
        Ui.toolbarButton(closeButton, 96, false);
        closeButton.addActionListener(event -> closeDetailTab(tabs, key));
        actions.add(closeButton);
        JButton applyButton = new JButton("申请");
        Ui.toolbarButton(applyButton, 96, true);
        applyButton.addActionListener(event -> showApplyOrderDialog(session, itemId, insightTitle(itemId)));
        actions.add(applyButton);

        JPanel detail = detailPage("批次综合信息", insightDetailBody(itemId), actions);
        addDetailTab(tabs, "综合信息", detail, key);
    }

    /** 根据权限向申请详情添加删除按钮。 */
    private void addOrderDeleteButton(
        JPanel actions,
        JPanel tabs,
        String key,
        UserSession session,
        DefaultTableModel model,
        long orderId
    ) {
        JButton deleteButton = new JButton("删除");
        Ui.toolbarButton(deleteButton, 96, false);
        deleteButton.addActionListener(event -> {
            if (confirm("确认删除该用血记录？")) {
                showResult(deleteOrder(session, orderId));
                loadOrders(model, session);
                closeDetailTab(tabs, key);
            }
        });
        actions.add(deleteButton);
    }

    /** @return 按当前角色规则删除申请的结果 */
    private BusinessResult deleteOrder(UserSession session, long orderId) {
        return isAdmin(session)
            ? businessService.deleteOrder(orderId)
            : businessService.deleteOwnOrder(session.userId(), orderId);
    }

    /** @return 带操作区的详情页面 */
    private JPanel detailPage(String title, JPanel body, JPanel actions) {
        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        javax.swing.SwingUtilities.invokeLater(() -> scroll.getViewport().setViewPosition(new java.awt.Point(0, 0)));
        return section(title, scroll, actions);
    }

    /** @return 是否已选中指定详情标签页 */
    private boolean selectDetailTab(JPanel tabs, String key) {
        for (java.awt.Component component : tabs.getComponents()) {
            if (component instanceof JPanel panel && key.equals(panel.getClientProperty("detailKey"))) {
                ((CardLayout) tabs.getLayout()).show(tabs, key);
                return true;
            }
        }
        return false;
    }

    /** 关闭指定详情标签页。 */
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

    /** 添加并选中详情标签页。 */
    private void addDetailTab(JPanel tabs, String title, JPanel detail, String key) {
        detail.putClientProperty("detailKey", key);
        tabs.add(detail, key);
        ((CardLayout) tabs.getLayout()).show(tabs, key);
    }

    /** @return 根据表格内容生成的详情标签标题 */
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

    /** @return 库存批次详情内容 */
    private JPanel itemDetailBody(JTable table, long itemId, UserSession session, Runnable onChanged) {
        JPanel body = detailBody();
        for (int i = 0; i < table.getModel().getRowCount(); i++) {
            if (((Number) table.getModel().getValueAt(i, 0)).longValue() == itemId) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                int bloodTypeColumn = model.findColumn("血型");
                int amountColumn = model.findColumn("数量");
                int statusColumn = model.findColumn("状态");
                JPanel grid = detailGrid();
                addInfo(grid, 0, 0, 2, "库存批次", model.getValueAt(i, 1));
                addInfo(grid, 1, 0, 1, "分类", model.getValueAt(i, 2));
                addInfo(grid, 1, 1, 1, "血型", bloodTypeColumn >= 0 ? model.getValueAt(i, bloodTypeColumn) : "未填写");
                addInfo(grid, 2, 0, 1, "数量", model.getValueAt(i, amountColumn));
                addInfo(grid, 2, 1, 1, "状态", model.getValueAt(i, statusColumn));
                body.add(grid, BorderLayout.NORTH);
                body.add(itemExtraPanel(itemId, session, onChanged), BorderLayout.CENTER);
                return body;
            }
        }
        body.add(emptyDetail("该记录未在当前列表中。"), BorderLayout.CENTER);
        return body;
    }

    /** @return 用血申请详情内容 */
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

    /** @return 分类详情及其子分类内容 */
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

    /** @return 用户档案详情内容 */
    private JPanel userProfileDetailBody(DefaultTableModel model, long userId) {
        JPanel body = detailBody();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (((Number) model.getValueAt(i, 0)).longValue() == userId) {
                JPanel grid = detailGrid();
                addInfo(grid, 0, 0, 2, "用户名", model.getValueAt(i, 1));
                addInfo(grid, 1, 0, 1, "角色", model.getValueAt(i, 2));
                addInfo(grid, 1, 1, 1, "状态", model.getValueAt(i, 3));
                addInfo(grid, 2, 0, 1, "邮箱", model.getValueAt(i, 4));
                addInfo(grid, 2, 1, 1, "手机号", model.getValueAt(i, 5));
                addInfo(grid, 3, 0, 1, "姓名", model.getValueAt(i, 6));
                addInfo(grid, 3, 1, 1, "证件号", model.getValueAt(i, 7));
                body.add(grid, BorderLayout.NORTH);
                return body;
            }
        }
        body.add(emptyDetail("该用户未在当前列表中。"), BorderLayout.CENTER);
        return body;
    }

    /** @return 库存评分、热度和申请统计详情 */
    private JPanel insightDetailBody(long itemId) {
        JPanel body = detailBody();
        ItemInsightDTO insight = findInsight(itemId);
        if (insight == null) {
            body.add(emptyDetail("该库存批次未在当前列表中。"), BorderLayout.CENTER);
            return body;
        }

        JPanel grid = detailGrid();
        addInfo(grid, 0, 0, 2, "库存批次", insight.title());
        addInfo(grid, 1, 0, 1, "分类", insight.categoryName());
        addInfo(grid, 1, 1, 1, "血型", blankText(insight.bloodType()));
        addInfo(grid, 2, 0, 1, "数量", amountText(insight.amount()));
        addInfo(grid, 2, 1, 1, "状态", itemStatus(insight.status()));
        addInfo(grid, 3, 0, 1, "评论", insight.commentCount() + " 条");
        addInfo(grid, 3, 1, 1, "评分", insight.averageRating() == 0D ? "暂无评分" : numberText(insight.averageRating()));
        addInfo(grid, 4, 0, 1, "行为", insight.actionCount() + " 次");
        addInfo(grid, 4, 1, 1, "订单", insight.orderCount() + " 条");
        body.add(grid, BorderLayout.NORTH);
        if (!insight.description().isBlank()) {
            body.add(textBlock("详情说明", insight.description()), BorderLayout.CENTER);
        }
        return body;
    }

    /** @return 指定父分类的子分类管理面板 */
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
            Ui.toolbarButton(addButton, 128, true);
            addButton.addActionListener(event -> showCreateChildCategoryDialog(parentId, mainModel, onChanged));
            actions.add(addButton);

            JButton editButton = new JButton("编辑");
            Ui.toolbarButton(editButton, 96, true);
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
            Ui.toolbarButton(deleteButton, 96, false);
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

    /** @return 统一详情内容容器 */
    private JPanel detailBody() {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(Ui.PANEL);
        return body;
    }

    /** @return 详情信息网格 */
    private JPanel detailGrid() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(Ui.PANEL);
        return grid;
    }

    /** 向详情网格添加信息块。 */
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

    /** @return 单个标签和值组成的信息块 */
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

    /** @return 库存扩展详情和评论区域 */
    private JPanel itemExtraPanel(long itemId, UserSession session, Runnable onChanged) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Ui.PANEL);
        panel.add(commentPanel(itemId, session, onChanged), BorderLayout.NORTH);
        try {
            Document detail = businessService.findItemDetail(itemId).orElse(null);
            String description = detail == null ? "" : detail.getString("description");
            panel.add(textBlock("详情说明", description == null || description.isBlank() ? "暂无详情说明" : description), BorderLayout.CENTER);
        } catch (RuntimeException ex) {
            panel.add(textBlock("详情说明", "详情加载失败，请检查数据库连接。"), BorderLayout.CENTER);
        }
        return panel;
    }

    /** @return 评论列表及新增入口 */
    private JPanel commentPanel(long itemId, UserSession session, Runnable onChanged) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(new Color(250, 249, 246));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Ui.BORDER),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(250, 249, 246));
        JLabel title = new JLabel("评论互动");
        title.setForeground(Ui.TEXT);
        title.setFont(Ui.font(15, Font.BOLD));
        header.add(title, BorderLayout.WEST);
        JButton addButton = new JButton("发表评论");
        Ui.toolbarButton(addButton, 112, true);
        addButton.addActionListener(event -> showCreateCommentDialog(itemId, session, onChanged));
        header.add(addButton, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        try {
            List<Document> comments = businessService.findItemComments(itemId);
            panel.add(commentPager(comments, session, onChanged), BorderLayout.CENTER);
        } catch (RuntimeException ex) {
            panel.add(textBlock("评论", "评论加载失败，请检查数据库连接。"), BorderLayout.CENTER);
        }
        return panel;
    }

    /** @return 支持前后翻页的评论查看器 */
    private JPanel commentPager(List<Document> comments, UserSession session, Runnable onChanged) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(250, 249, 246));
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Ui.PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Ui.BORDER),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        JPanel meta = detailGrid();
        JTextArea content = area(3);
        content.setEditable(false);
        content.setBackground(Ui.PANEL);
        content.setBorder(BorderFactory.createEmptyBorder());
        card.add(meta, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setBackground(new Color(250, 249, 246));
        controls.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        JButton prev = new JButton("上一条");
        JButton next = new JButton("下一条");
        JButton delete = new JButton("删除");
        Ui.toolbarButton(prev, 96, false);
        Ui.toolbarButton(next, 96, false);
        Ui.toolbarButton(delete, 96, false);
        JLabel page = new JLabel();
        page.setForeground(Ui.TEXT);
        page.setFont(Ui.font(13, Font.PLAIN));
        controls.add(prev);
        controls.add(page);
        controls.add(next);
        controls.add(delete);
        panel.add(controls);
        panel.add(card);

        int[] index = {0};
        Runnable render = () -> renderComment(comments, index[0], session, meta, content, page, prev, next, delete);
        prev.addActionListener(event -> {
            index[0]--;
            render.run();
        });
        next.addActionListener(event -> {
            index[0]++;
            render.run();
        });
        delete.addActionListener(event -> {
            if (comments.isEmpty() || !confirm("确认删除当前评论？")) {
                return;
            }
            Object id = comments.get(index[0]).get("_id");
            BusinessResult result = businessService.deleteComment(session.userId(), isAdmin(session), String.valueOf(id));
            showResult(result);
            if (result.success()) {
                onChanged.run();
            }
        });
        render.run();
        return panel;
    }

    /** 渲染指定下标的评论。 */
    private void renderComment(
        List<Document> comments,
        int index,
        UserSession session,
        JPanel meta,
        JTextArea content,
        JLabel page,
        JButton prev,
        JButton next,
        JButton delete
    ) {
        if (comments.isEmpty()) {
            meta.removeAll();
            addInfo(meta, 0, 0, 2, "评论", "暂无评论");
            content.setText("");
            page.setText("0 / 0");
            prev.setEnabled(false);
            next.setEnabled(false);
            delete.setVisible(false);
            return;
        }
        Document comment = comments.get(index);
        int rating = comment.get("rating", Number.class) == null ? 0 : comment.get("rating", Number.class).intValue();
        String tags = commentTags(comment);
        meta.removeAll();
        addInfo(meta, 0, 0, 1, "用户", commentUsername(comment, session));
        addInfo(meta, 0, 1, 1, "评分", rating);
        addInfo(meta, 0, 2, 1, "标签", tags.isEmpty() ? "无" : tags);
        addInfo(meta, 0, 3, 1, "时间", dateText(comment.getDate("created_at")));
        meta.revalidate();
        meta.repaint();
        content.setText(comment.getString("content"));
        content.setCaretPosition(0);
        page.setText((index + 1) + " / " + comments.size());
        prev.setEnabled(index > 0);
        next.setEnabled(index < comments.size() - 1);
        delete.setVisible(canDeleteComment(session, comment));
    }

    /** @return 评论作者的显示名称 */
    private String commentUsername(Document comment, UserSession session) {
        String userId = comment.getString("user_id");
        if (String.valueOf(session.userId()).equals(userId)) {
            return session.username();
        }
        try {
            return businessService.findUsername(Long.parseLong(userId));
        } catch (RuntimeException ex) {
            return "用户 " + userId;
        }
    }

    /** @return 当前用户是否可以删除指定评论 */
    private boolean canDeleteComment(UserSession session, Document comment) {
        return comment.get("_id") != null && (isAdmin(session) || String.valueOf(session.userId()).equals(comment.getString("user_id")));
    }

    /** @return 评论标签的显示文本 */
    private String commentTags(Document comment) {
        List<?> tags = comment.getList("tags", Object.class, List.of());
        return tags.stream()
            .map(String::valueOf)
            .filter(tag -> !tag.isBlank())
            .reduce((left, right) -> left + "、" + right)
            .orElse("");
    }

    /** 打开新增评论对话框。 */
    private void showCreateCommentDialog(long itemId, UserSession session, Runnable onChanged) {
        JDialog dialog = new JDialog(this, "发表评论", true);
        JTextArea content = area(3);
        JSpinner rating = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        Ui.spinner(rating, 120);
        JTextField tags = field();
        JPanel form = formPanel();
        form.setBorder(BorderFactory.createEmptyBorder(22, 24, 12, 24));
        addFormField(form, 0, 0, 2, "评论内容", areaScroll(content));
        addFormField(form, 1, 0, 1, "评分", rating);
        addFormField(form, 1, 1, 1, "标签", tags);

        JButton cancelButton = new JButton("取消");
        Ui.textButton(cancelButton);
        cancelButton.addActionListener(event -> dialog.dispose());
        JButton saveButton = new JButton("发布");
        Ui.primaryButton(saveButton, 112);
        saveButton.addActionListener(event -> {
            BusinessResult result = businessService.createComment(
                session.userId(),
                itemId,
                content.getText(),
                ((Number) rating.getValue()).intValue(),
                tags(tags.getText())
            );
            showResult(result);
            if (result.success()) {
                dialog.dispose();
                onChanged.run();
            }
        });
        dialog.setContentPane(dialogContent("发表评论", form, cancelButton, saveButton));
        dialog.setSize(560, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /** @return 清理并去重后的标签列表 */
    private List<String> tags(String value) {
        if (value == null || value.trim().isEmpty()) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split("[,，\\s]+"))
            .map(String::trim)
            .filter(tag -> !tag.isEmpty())
            .toList();
    }

    /** @return 日期的界面显示文本 */
    private String dateText(Date date) {
        return date == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    /** @return 带标签的多行文本块 */
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
        JTextArea textView = area(2);
        textView.setEditable(false);
        textView.setOpaque(false);
        textView.setBorder(BorderFactory.createEmptyBorder());
        textView.setText(text);
        block.add(labelView, BorderLayout.NORTH);
        block.add(textView, BorderLayout.CENTER);
        return block;
    }

    /** @return 详情为空时的提示标签 */
    private JLabel emptyDetail(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(new Color(91, 94, 102));
        label.setFont(Ui.font(15, Font.PLAIN));
        return label;
    }

    /** @return 限制长度并添加省略号的文本 */
    private String ellipsis(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    /** @return 带标题、表单和操作按钮的对话框内容 */
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

    /** @return 使用统一布局的表单面板 */
    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Ui.PANEL);
        return panel;
    }

    /** @return 库存批次编辑表单 */
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
        addFormField(form, 3, 0, 2, "详情", areaScroll(descriptionArea));
        return form;
    }

    /** @return 用户档案编辑表单 */
    private JPanel userProfileForm(
        JTextField usernameField,
        JComboBox<String> roleBox,
        JComboBox<String> statusBox,
        JTextField emailField,
        JTextField phoneField,
        JTextField realNameField,
        JTextField idCardField,
        JTextField addressField,
        JTextArea notesArea
    ) {
        JPanel form = formPanel();
        form.setBorder(BorderFactory.createEmptyBorder(22, 24, 12, 24));
        addFormField(form, 0, 0, 1, "用户名", usernameField);
        addFormField(form, 0, 1, 1, "角色", roleBox);
        addFormField(form, 1, 0, 1, "状态", statusBox);
        addFormField(form, 1, 1, 1, "邮箱", emailField);
        addFormField(form, 2, 0, 1, "手机号", phoneField);
        addFormField(form, 2, 1, 1, "姓名", realNameField);
        addFormField(form, 3, 0, 2, "证件号", idCardField);
        addFormField(form, 4, 0, 2, "地址", addressField);
        addFormField(form, 5, 0, 2, "备注", areaScroll(notesArea));
        return form;
    }

    /** 向网格表单添加带标签的字段。 */
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

    /** @return 使用统一样式的文本输入框 */
    private JTextField field() {
        JTextField field = new JTextField(18);
        Ui.field(field);
        field.setPreferredSize(new Dimension(240, 44));
        field.setMinimumSize(new Dimension(220, 44));
        return field;
    }

    /** @return 库存或申请数量选择器 */
    private JSpinner amountSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 99999.0, 1.0));
        Ui.spinner(spinner, 240);
        return spinner;
    }

    /** @return 数量选择器中的精确十进制值 */
    private BigDecimal spinnerAmount(JSpinner spinner) {
        return BigDecimal.valueOf(((Number) spinner.getValue()).doubleValue());
    }

    /** @return 血型选择框 */
    private JComboBox<String> bloodTypeBox() {
        JComboBox<String> box = new JComboBox<>(new String[] {"A型", "B型", "AB型", "O型"});
        Ui.comboBox(box, 240);
        return box;
    }

    /** @return 使用统一样式的多行文本框 */
    private JTextArea area(int rows) {
        JTextArea area = new JTextArea(rows, 18);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        Ui.area(area);
        return area;
    }

    /** @return 包装多行文本框的滚动面板 */
    private JScrollPane areaScroll(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(Ui.BORDER));
        scroll.getViewport().setBackground(Ui.PANEL);
        return scroll;
    }

    /** @return 只读表格模型 */
    private DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /** @return 使用统一样式和排序器的表格 */
    private JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(Ui.font(14, Font.PLAIN));
        table.getTableHeader().setFont(Ui.font(14, Font.BOLD));
        table.getTableHeader().setBackground(new Color(238, 235, 229));
        table.getTableHeader().setForeground(Ui.TEXT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Ui.BORDER));
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(new Color(238, 235, 229));
        headerRenderer.setForeground(Ui.TEXT);
        headerRenderer.setFont(Ui.font(14, Font.BOLD));
        headerRenderer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Ui.BORDER),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        table.setGridColor(new Color(228, 224, 216));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(new Color(248, 226, 228));
        table.setSelectionForeground(Ui.TEXT);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setForeground(Ui.TEXT);
        cellRenderer.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        table.setDefaultRenderer(Object.class, cellRenderer);
        return table;
    }

    /** 设置表格各列的建议宽度。 */
    private void setColumnWidths(JTable table, int... widths) {
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    /** 隐藏表格第一列的内部编号。 */
    private void hideFirstColumn(JTable table) {
        hideColumn(table, 0);
    }

    /** 隐藏表格指定列。 */
    private void hideColumn(JTable table, int column) {
        table.getColumnModel().getColumn(column).setMinWidth(0);
        table.getColumnModel().getColumn(column).setMaxWidth(0);
        table.getColumnModel().getColumn(column).setPreferredWidth(0);
    }

    /** 向简单表单添加一行字段。 */
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

    /** 加载全部分类选项。 */
    private void loadCategories(JComboBox<Option> box) {
        DefaultComboBoxModel<Option> model = new DefaultComboBoxModel<>();
        for (Map<String, Object> row : businessService.findCategories()) {
            if (row.get("parent_id") != null) {
                model.addElement(new Option(((Number) row.get("category_id")).longValue(), String.valueOf(row.get("name"))));
            }
        }
        box.setModel(model);
    }

    /** 加载可选父分类并排除当前分类。 */
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

    /** 加载可用库存选项。 */
    private void loadItemOptions(JComboBox<Option> box) {
        DefaultComboBoxModel<Option> model = new DefaultComboBoxModel<>();
        for (Map<String, Object> row : businessService.findItems()) {
            if (((Number) row.get("status")).intValue() == 1) {
                model.addElement(new Option(((Number) row.get("item_id")).longValue(), String.valueOf(row.get("title"))));
            }
        }
        box.setModel(model);
    }

    /** 将全部分类加载到表格。 */
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

    /** 将指定父分类的子分类加载到表格。 */
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

    /** @return 指定库存行；不存在时返回空映射 */
    private Map<String, Object> findItemRow(long itemId) {
        for (Map<String, Object> row : businessService.findItems()) {
            if (((Number) row.get("item_id")).longValue() == itemId) {
                return row;
            }
        }
        return null;
    }

    /** @return 指定分类行；不存在时返回空映射 */
    private Map<String, Object> findCategoryRow(long categoryId) {
        for (Map<String, Object> row : businessService.findCategories()) {
            if (((Number) row.get("category_id")).longValue() == categoryId) {
                return row;
            }
        }
        return null;
    }

    /** @return 当前会话可查看的指定用户档案 */
    private Map<String, Object> findUserProfileRow(long userId, UserSession session) {
        for (Map<String, Object> row : businessService.findUserProfiles(session.userId(), isAdmin(session))) {
            if (((Number) row.get("user_id")).longValue() == userId) {
                return row;
            }
        }
        return null;
    }

    /** 按编号选中下拉框选项。 */
    private void selectOption(JComboBox<Option> box, long id) {
        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).id() == id) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    /** 按显示文本选中下拉框选项。 */
    private void selectOptionByLabel(JComboBox<Option> box, String label) {
        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).label().equals(label)) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    /** @return 表格模型中的指定申请行 */
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

    /** @return 指定申请是否处于待审批状态 */
    private boolean isPendingOrder(DefaultTableModel model, long orderId) {
        return findOrderRow(model, orderId)
            .map(row -> "待处理".equals(row.get("status")))
            .orElse(false);
    }

    /** 将库存批次加载到表格。 */
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

    /** 将用户推荐加载到表格。 */
    private void loadRecommendations(DefaultTableModel model, UserSession session) {
        try {
            model.setRowCount(0);
            for (RecommendationDTO row : businessService.recommendItems(session.userId(), 20)) {
                ItemInsightDTO item = row.item();
                model.addRow(new Object[] {
                    item.itemId(),
                    item.title(),
                    item.categoryName(),
                    blankText(item.bloodType()),
                    amountText(item.amount()),
                    item.averageRating() == 0D ? "暂无" : numberText(item.averageRating()),
                    row.reason()
                });
            }
        } catch (RuntimeException ex) {
            warn("推荐加载失败，请检查数据库连接。");
        }
    }

    /** 将库存综合统计加载到表格。 */
    private void loadInsights(DefaultTableModel model) {
        try {
            model.setRowCount(0);
            for (ItemInsightDTO row : businessService.findItemInsights()) {
                model.addRow(new Object[] {
                    row.itemId(),
                    row.title(),
                    row.categoryName(),
                    blankText(row.bloodType()),
                    amountText(row.amount()),
                    itemStatus(row.status()),
                    row.commentCount(),
                    row.actionCount(),
                    row.orderCount()
                });
            }
        } catch (RuntimeException ex) {
            warn("综合查询加载失败，请检查数据库连接。");
        }
    }

    /** 重新加载库存相关表格。 */
    private void reloadItemRows(DefaultTableModel model) {
        if (model.findColumn("评论") >= 0) {
            loadInsights(model);
        } else {
            loadItems(model);
        }
    }

    /** 按当前角色加载申请记录。 */
    private void loadOrders(DefaultTableModel model, UserSession session) {
        try {
            Map<Long, ItemInsightDTO> items = itemInsightsById();
            model.setRowCount(0);
            for (Map<String, Object> row : businessService.findOrders(session.userId(), isAdmin(session))) {
                ItemInsightDTO item = items.get(((Number) row.get("item_id")).longValue());
                model.addRow(new Object[] {
                    row.get("order_id"),
                    item == null ? "库存批次已删除" : item.title(),
                    amountText(row.get("amount")),
                    orderStatus(row.get("status")),
                    item == null ? "未分类" : item.categoryName(),
                    item == null ? "未填写" : blankText(item.bloodType())
                });
            }
        } catch (RuntimeException ex) {
            warn("记录加载失败，请检查数据库连接。");
        }
    }

    /** 按当前角色加载用户档案。 */
    private void loadUserProfiles(DefaultTableModel model, UserSession session) {
        try {
            model.setRowCount(0);
            for (Map<String, Object> row : businessService.findUserProfiles(session.userId(), isAdmin(session))) {
                model.addRow(new Object[] {
                    row.get("user_id"),
                    row.get("username"),
                    roleText(row.get("user_id"), row.get("role")),
                    userStatus(row.get("status")),
                    blankText(valueText(row.get("email"))),
                    blankText(valueText(row.get("phone"))),
                    blankText(valueText(row.get("real_name"))),
                    blankText(valueText(row.get("id_card")))
                });
            }
        } catch (RuntimeException ex) {
            warn("用户档案加载失败，请检查数据库连接。");
        }
    }

    /** 加载管理员使用的热度和评分统计。 */
    private void loadStatistics(BarChartPanel topItems, BarChartPanel ratings) {
        try {
            Map<Long, String> items = itemNames();
            topItems.setRows(actionChartRows(items, businessService.topActionItems(50)).stream().limit(10).toList());
            ratings.setRows(ratingChartRows(items, businessService.commentRatingSummary()));

        } catch (RuntimeException ex) {
            warn("统计数据加载失败，请检查数据库连接。");
        }
    }

    /** 加载普通用户的个人统计图表。 */
    private void loadUserStatistics(
        BarChartPanel statuses,
        BarChartPanel categories,
        BarChartPanel bloodTypes,
        BarChartPanel months,
        BarChartPanel ratings,
        UserSession session
    ) {
        try {
            List<Map<String, Object>> orders = businessService.findOrdersByUser(session.userId());
            Map<Long, ItemInsightDTO> items = itemInsightsById();
            statuses.setRows(orderStatusChartRows(orders));
            categories.setRows(orderItemChartRows(orders, items, "未分类", true));
            bloodTypes.setRows(orderItemChartRows(orders, items, "未填写", false));
            months.setRows(orderMonthChartRows(orders));
            ratings.setRows(ratingChartRows(itemNames(), businessService.commentRatingSummaryByUser(session.userId())));
        } catch (RuntimeException ex) {
            warn("个人统计加载失败，请检查数据库连接。");
        }
    }

    /** 加载指定年月的月度报表。 */
    private void loadMonthlyReport(BarChartPanel monthly, int year, int month) {
        try {
            monthly.setRows(monthlyChartRows(businessService.monthlyReport(year, month)));
        } catch (RuntimeException ex) {
            warn("月度报表加载失败，请检查数据库连接。");
        }
    }

    /** 加载指定月份的月度报表。 */
    private void loadMonthlyReport(BarChartPanel monthly, YearMonth month) {
        loadMonthlyReport(monthly, month.getYear(), month.getMonthValue());
    }

    /** @return 年月的中文显示文本 */
    private static String monthLabel(YearMonth month) {
        return month.getYear() + " 年 " + month.getMonthValue() + " 月";
    }

    /** @return 行为热度柱状图数据 */
    private static List<ChartRow> actionChartRows(Map<Long, String> items, List<Document> rows) {
        List<ChartRow> chartRows = new ArrayList<>();
        for (Document row : rows) {
            Long itemId = itemId(row.get("item_id"));
            if (itemId == null || !items.containsKey(itemId)) {
                continue;
            }
            Number count = row.get("action_count", Number.class);
            chartRows.add(new ChartRow(items.get(itemId), count == null ? 0 : count.doubleValue()));
        }
        return chartRows;
    }

    /** @return 申请状态柱状图数据 */
    private List<ChartRow> orderStatusChartRows(List<Map<String, Object>> rows) {
        Map<String, Double> counts = new LinkedHashMap<>();
        counts.put("待处理", 0D);
        counts.put("已完成", 0D);
        counts.put("已取消", 0D);
        for (Map<String, Object> row : rows) {
            String label = orderStatus(row.get("status"));
            counts.put(label, counts.getOrDefault(label, 0D) + 1D);
        }
        return chartRows(counts);
    }

    /** @return 按库存汇总的申请柱状图数据 */
    private List<ChartRow> orderItemChartRows(
        List<Map<String, Object>> rows,
        Map<Long, ItemInsightDTO> items,
        String fallback,
        boolean category
    ) {
        Map<String, Double> counts = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            ItemInsightDTO item = items.get(((Number) row.get("item_id")).longValue());
            String label = item == null ? fallback : category ? item.categoryName() : blankText(item.bloodType());
            counts.put(label, counts.getOrDefault(label, 0D) + 1D);
        }
        return chartRows(counts);
    }

    /** @return 按月份汇总的申请柱状图数据 */
    private static List<ChartRow> orderMonthChartRows(List<Map<String, Object>> rows) {
        Map<String, Double> amounts = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String label = monthText(row.get("created_at"));
            amounts.put(label, amounts.getOrDefault(label, 0D) + doubleValue(row.get("amount")));
        }
        return chartRows(amounts);
    }

    /** @return 数据库月份值的标准文本 */
    static String monthText(Object value) {
        if (value instanceof Date date) {
            return new SimpleDateFormat("yyyy-MM").format(date);
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        if (value != null && String.valueOf(value).length() >= 7) {
            return String.valueOf(value).substring(0, 7);
        }
        return "未记录";
    }

    /** @return 名称和值映射对应的柱状图数据 */
    private static List<ChartRow> chartRows(Map<String, Double> values) {
        return values.entrySet().stream()
            .filter(entry -> entry.getValue() > 0)
            .map(entry -> new ChartRow(entry.getKey(), entry.getValue()))
            .toList();
    }

    /** @return 评论评分柱状图数据 */
    private static List<ChartRow> ratingChartRows(Map<Long, String> items, List<Document> rows) {
        List<ChartRow> chartRows = new ArrayList<>();
        for (Document row : rows) {
            Long itemId = itemId(row.get("item_id"));
            if (itemId == null || !items.containsKey(itemId)) {
                continue;
            }
            Number averageRating = row.get("average_rating", Number.class);
            Number commentCount = row.get("comment_count", Number.class);
            String label = items.get(itemId) + "（" + (commentCount == null ? 0 : commentCount.intValue()) + " 条）";
            chartRows.add(new ChartRow(label, averageRating == null ? 0 : averageRating.doubleValue()));
        }
        return chartRows;
    }

    /** @return 月度用血柱状图数据 */
    private static List<ChartRow> monthlyChartRows(List<Map<String, Object>> rows) {
        List<ChartRow> chartRows = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String label = row.get("category_name")
                + "（" + intValue(row.get("order_count")) + "单：待" + intValue(row.get("pending_count"))
                + " / 完" + intValue(row.get("completed_count")) + " / 取消" + intValue(row.get("cancelled_count")) + "）";
            chartRows.add(new ChartRow(label, doubleValue(row.get("used_amount"))));
        }
        return chartRows;
    }

    /** @return 对象的整数值，无法转换时返回 0 */
    private static int intValue(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    /** @return 对象的双精度值，无法转换时返回 0 */
    private static double doubleValue(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0D;
    }

    /** @return 对象表示的库存编号，无法转换时为空 */
    private static Long itemId(Object value) {
        if (value == null || "NONE".equals(String.valueOf(value))) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /** 将行为、登录和审计日志加载到表格。 */
    private void loadLogs(DefaultTableModel actions, DefaultTableModel logins, DefaultTableModel audit, UserSession session) {
        try {
            Map<Long, String> items = itemNames();
            actions.setRowCount(0);
            for (Document row : businessService.findActionLogs(session.userId(), isAdmin(session), 100)) {
                actions.addRow(new Object[] {
                    actionUser(row, session),
                    itemName(items, row.get("item_id")),
                    actionLabel(row.getString("action_type")),
                    dateText(row.getDate("created_at"))
                });
            }

            logins.setRowCount(0);
            if (isAdmin(session)) {
                for (Document row : businessService.findSystemLogs("LOGIN", 100)) {
                    logins.addRow(new Object[] {
                        logUser(row.getString("user_id")),
                        row.get("log_level"),
                        row.get("message"),
                        dateText(row.getDate("timestamp"))
                    });
                }
            }

            if (audit != null) {
                audit.setRowCount(0);
                for (Document row : businessService.auditSummary()) {
                    audit.addRow(new Object[] {
                        row.get("log_type"),
                        row.get("log_level"),
                        row.get("log_count"),
                        dateText(row.getDate("last_log_at"))
                    });
                }
            }
        } catch (RuntimeException ex) {
            warn("日志加载失败，请检查数据库连接。");
        }
    }

    /** @return 按分类编号索引的分类名称 */
    private Map<Long, String> categoryNames() {
        return categoryNames(businessService.findCategories());
    }

    /** @return 指定分类行对应的名称索引 */
    private Map<Long, String> categoryNames(List<Map<String, Object>> rows) {
        java.util.HashMap<Long, String> names = new java.util.HashMap<>();
        for (Map<String, Object> row : rows) {
            names.put(((Number) row.get("category_id")).longValue(), String.valueOf(row.get("name")));
        }
        return names;
    }

    /** @return 按库存编号索引的库存名称 */
    private Map<Long, String> itemNames() {
        java.util.HashMap<Long, String> names = new java.util.HashMap<>();
        for (Map<String, Object> row : businessService.findItems()) {
            names.put(((Number) row.get("item_id")).longValue(), String.valueOf(row.get("title")));
        }
        return names;
    }

    /** @return 按库存编号索引的综合统计 */
    private Map<Long, ItemInsightDTO> itemInsightsById() {
        java.util.HashMap<Long, ItemInsightDTO> items = new java.util.HashMap<>();
        for (ItemInsightDTO row : businessService.findItemInsights()) {
            items.put(row.itemId(), row);
        }
        return items;
    }

    /** @return 指定库存的综合统计，不存在时为空 */
    private ItemInsightDTO findInsight(long itemId) {
        try {
            for (ItemInsightDTO row : businessService.findItemInsights()) {
                if (row.itemId() == itemId) {
                    return row;
                }
            }
        } catch (RuntimeException ex) {
            warn("综合信息加载失败，请检查数据库连接。");
        }
        return null;
    }

    /** @return 指定库存洞察的标题 */
    private String insightTitle(long itemId) {
        ItemInsightDTO insight = findInsight(itemId);
        return insight == null ? "库存批次 " + itemId : insight.title();
    }

    /** @return 库存编号对应的名称 */
    private String itemName(Map<Long, String> items, Object itemId) {
        if (itemId == null || "NONE".equals(String.valueOf(itemId))) {
            return "无关联批次";
        }
        try {
            long id = Long.parseLong(String.valueOf(itemId));
            return items.getOrDefault(id, "库存批次已删除");
        } catch (NumberFormatException ex) {
            return String.valueOf(itemId);
        }
    }

    /** @return 行为日志用户的显示名称 */
    private String actionUser(Document row, UserSession session) {
        String userId = row.getString("user_id");
        if (String.valueOf(session.userId()).equals(userId)) {
            return session.username();
        }
        return logUser(userId);
    }

    /** @return 系统日志用户的显示名称 */
    private String logUser(String userId) {
        try {
            return userId == null || "SYSTEM".equals(userId) ? "系统" : businessService.findUsername(Long.parseLong(userId));
        } catch (RuntimeException ex) {
            return userId == null ? "" : userId;
        }
    }

    /** @return 行为类型的中文标签 */
    private String actionLabel(String actionType) {
        return switch (actionType == null ? "" : actionType) {
            case "CREATE_ORDER" -> "创建记录";
            case "UPDATE_ORDER" -> "编辑记录";
            case "UPDATE_ORDER_STATUS" -> "更新状态";
            case "DELETE_ORDER" -> "删除记录";
            case "CREATE_COMMENT" -> "发表评论";
            case "DELETE_COMMENT" -> "删除评论";
            default -> actionType == null ? "" : actionType;
        };
    }

    /** @return 下拉框当前选项 */
    private Option selected(JComboBox<Option> box) {
        Object value = box.getSelectedItem();
        return value instanceof Option option ? option : null;
    }

    /** @return 当前选择的父分类编号 */
    private Long selectedParentId(JComboBox<Option> box) {
        Option option = selected(box);
        return option == null || option.id() == 0L ? null : option.id();
    }

    /** @return 当前选择的用户角色 */
    private String selectedRole(JComboBox<String> box) {
        return box.getSelectedIndex() == 1 ? "ADMIN" : "USER";
    }

    /** @return 指定分类是否是目标父分类的后代 */
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

    /** @return 当前会话是否属于管理员 */
    private boolean isAdmin(UserSession session) {
        return "ADMIN".equals(session.role());
    }

    /** @return 当前会话是否属于超级管理员 */
    private boolean isSuperAdmin(UserSession session) {
        return isSuperAdmin(session.userId());
    }

    /** @return 指定用户是否为超级管理员 */
    private boolean isSuperAdmin(long userId) {
        return userId == 1L;
    }

    /** @return 表格当前选中行的内部编号 */
    private Long selectedId(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        Object value = table.getModel().getValueAt(table.convertRowIndexToModel(row), 0);
        return ((Number) value).longValue();
    }

    /** @return 指定库存的表格标题 */
    private String itemTitle(JTable table, long itemId) {
        for (int row = 0; row < table.getModel().getRowCount(); row++) {
            if (((Number) table.getModel().getValueAt(row, 0)).longValue() == itemId) {
                return String.valueOf(table.getModel().getValueAt(row, 1));
            }
        }
        return "库存批次 " + itemId;
    }

    /** @return 库存状态的中文文本 */
    private String itemStatus(Object status) {
        return ((Number) status).intValue() == 1 ? "可用" : "停用";
    }

    /** @return 用户状态的中文文本 */
    private String userStatus(Object status) {
        return ((Number) status).intValue() == 1 ? "启用" : "禁用";
    }

    /** @return 用户角色的中文文本 */
    private String roleText(Object userId, Object role) {
        if (((Number) userId).longValue() == 1L) {
            return "超级管理员";
        }
        return "ADMIN".equals(role) ? "管理员" : "普通用户";
    }

    /** @return 数量的显示文本 */
    private String amountText(Object amount) {
        return amount instanceof BigDecimal value ? value.toPlainString() : String.valueOf(amount);
    }

    /** @return 对象的非空显示文本 */
    private String valueText(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /** @return 空字符串对应的占位文本 */
    private String blankText(String value) {
        return value == null || value.isBlank() ? "未填写" : value;
    }

    /** @return 数值对象的显示文本 */
    private String numberText(Object value) {
        return value instanceof Number number ? String.format("%.2f", number.doubleValue()) : String.valueOf(value);
    }

    /** @return 申请状态的中文文本 */
    private String orderStatus(Object status) {
        int value = ((Number) status).intValue();
        return switch (value) {
            case 1 -> "已完成";
            case 2 -> "已取消";
            default -> "待处理";
        };
    }

    /** 显示业务操作结果。 */
    private void showResult(BusinessResult result) {
        if (result.success()) {
            JOptionPane.showMessageDialog(this, result.message(), "操作成功", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        warn(result.message());
    }

    /** 显示警告提示。 */
    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "操作失败", JOptionPane.WARNING_MESSAGE);
    }

    /** @return 用户是否确认当前操作 */
    private boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "确认操作", JOptionPane.OK_CANCEL_OPTION)
            == JOptionPane.OK_OPTION;
    }

    /** 刷新当前主模块。 */
    private void refreshMain() {
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    /** 更新侧边导航的选中样式。 */
    private void selectNav(JButton selected) {
        for (JButton button : navButtons) {
            boolean active = button == selected;
            button.setForeground(Ui.PANEL);
            button.setBackground(active ? Ui.PRIMARY : new Color(61, 65, 75));
            button.setOpaque(true);
            button.setContentAreaFilled(true);
        }
    }

    /** 表示柱状图中的标签和值。 */
    private record ChartRow(String label, double value) {
    }

    /** 表示筛选项的名称和目标列。 */
    private record FilterChoice(String label, int column) {
    }

    /** 将文档变化事件统一转换为一个回调。 */
    private static class SimpleDocumentListener implements DocumentListener {
        private final Runnable action;

        /** 创建文档变化监听器。 */
        SimpleDocumentListener(Runnable action) {
            this.action = action;
        }

        @Override
        public void insertUpdate(DocumentEvent event) {
            action.run();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            action.run();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            action.run();
        }
    }

    /** 绘制简单横向柱状图。 */
    private static class BarChartPanel extends JPanel {
        private List<ChartRow> rows = List.of();
        private final double fixedMax;
        private final String valueSuffix;

        /** 创建指定最大值和单位后缀的柱状图。 */
        BarChartPanel(double fixedMax, String valueSuffix) {
            this.fixedMax = fixedMax;
            this.valueSuffix = valueSuffix;
            setBackground(Ui.PANEL);
            setPreferredSize(new Dimension(720, 420));
        }

        void setRows(List<ChartRow> rows) {
            this.rows = rows == null ? List.of() : rows;
            setPreferredSize(new Dimension(720, Math.max(360, this.rows.size() * 42 + 40)));
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            super.paintComponent(graphics);
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            if (rows.isEmpty()) {
                g.setFont(Ui.font(15, Font.PLAIN));
                g.setColor(new Color(91, 94, 102));
                g.drawString("暂无统计数据", 28, 42);
                g.dispose();
                return;
            }

            int labelWidth = Math.min(360, Math.max(240, getWidth() / 3));
            int barX = labelWidth + 28;
            int barMaxWidth = Math.max(80, getWidth() - barX - 86);
            int y = 28;
            double max = fixedMax > 0 ? fixedMax : rows.stream().mapToDouble(ChartRow::value).max().orElse(1);
            for (ChartRow row : rows) {
                int barWidth = Math.max(4, (int) (barMaxWidth * (row.value() / max)));
                g.setFont(Ui.font(14, Font.PLAIN));
                g.setColor(Ui.TEXT);
                g.drawString(ellipsis(row.label(), 24), 28, y + 15);
                g.setColor(new Color(238, 235, 229));
                g.fillRoundRect(barX, y, barMaxWidth, 16, 5, 5);
                g.setColor(Ui.PRIMARY);
                g.fillRoundRect(barX, y, barWidth, 16, 5, 5);
                g.setFont(Ui.font(14, Font.BOLD));
                g.setColor(Ui.TEXT);
                g.drawString(valueText(row.value()) + valueSuffix, barX + barMaxWidth + 16, y + 14);
                y += 42;
            }
            g.dispose();
        }

        /** @return 柱状图数值的显示文本 */
        private String valueText(double value) {
            return value == Math.rint(value) ? String.valueOf((int) value) : String.format("%.1f", value);
        }

        /** @return 限制长度后的柱状图标签 */
        private String ellipsis(String text, int maxLength) {
            if (text == null || text.length() <= maxLength) {
                return text == null ? "" : text;
            }
            return text.substring(0, maxLength) + "...";
        }
    }

    /** 表示带编号和值的下拉框选项。 */
    private record Option(long id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
