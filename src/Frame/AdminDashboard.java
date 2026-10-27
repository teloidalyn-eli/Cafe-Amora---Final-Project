package Frame;

import Main.*;
import Main.Main.SystemManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboard extends JFrame {

    private static final String[] CARDS = {"products", "orders", "reports", "staff"};
    private static final String[] NAV_TEXT = {"Manage Product", "View Orders", "Reports", "Accounts"};

    private final SystemManager manager;
    private final User admin;
    private final ArrayList<JButton> navButtons = new ArrayList<>();
    private CardLayout cardLayout;
    private JPanel contentPanel;

    private DefaultTableModel productModel, orderModel, staffModel;
    private JTable productTable, staffTable;
    private JTextField nameField, priceField, stockField;
    private JComboBox<String> categoryCombo;
    private JButton uploadImageBtn;
    private String selectedImagePath = "";

    private JTextArea reportArea;
    private JLabel statOrdersValue, statItemsValue, statRevenueValue;

    public AdminDashboard(SystemManager manager, User admin) {
        this.manager = manager;
        this.admin = admin;
        setTitle("Admin Dashboard - " + admin.getUsername());
        setSize(1080, 680);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.LIGHT);
        dashboard();
    }

    private void dashboard() {
        add(headerPanel(), BorderLayout.NORTH);
        add(sidebarPanel(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        contentPanel.add(manageProductsCard(), CARDS[0]);
        contentPanel.add(viewOrdersCard(), CARDS[1]);
        contentPanel.add(reportsCard(), CARDS[2]);
        contentPanel.add(staffCard(), CARDS[3]);

        JPanel contentWrap = Theme.borderedPanel(Theme.LIGHT, Theme.ACCENT);
        contentWrap.add(contentPanel, BorderLayout.CENTER);
        JPanel centerPad = new JPanel(new BorderLayout());
        centerPad.setBackground(Theme.LIGHT);
        centerPad.setBorder(new EmptyBorder(16, 8, 16, 16));
        centerPad.add(contentWrap, BorderLayout.CENTER);
        add(centerPad, BorderLayout.CENTER);

        showCard(0);
    }

    private JPanel headerPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.LIGHT);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.ACCENT),
                new EmptyBorder(14, 20, 14, 20)));

        JLabel title = label("Cafe Amora", Theme.FONT_HEADER, Theme.DARK);
        ImageIcon logo = image("src\\img\\LOGO\\CafeAmora-LOGO.png", 45, 45);
        if (logo != null) {
            title.setIcon(logo);
            title.setIconTextGap(10);
        }
        header.add(title, BorderLayout.WEST);

        JButton logoutBtn = Theme.blueBtn("Logout");
        logoutBtn.addActionListener(e -> logout());
        JButton changePassBtn = Theme.blueBtn("Change Password");
        changePassBtn.addActionListener(e -> changeMyPassword());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(label(admin.getUsername() + " (Admin)  ", Theme.FONT_LABEL, Theme.DARK));
        right.add(changePassBtn);
        right.add(logoutBtn);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel sidebarPanel() {
        JPanel sidebar = new JPanel(new GridLayout(4, 1, 0, 14));
        sidebar.setBackground(Theme.LIGHT);
        sidebar.setBorder(new EmptyBorder(22, 16, 22, 10));
        sidebar.setPreferredSize(new Dimension(260, 0));
        for (int i = 0; i < NAV_TEXT.length; i++) {
            int index = i;
            JButton btn = navButton(NAV_TEXT[i]);
            btn.addActionListener(e -> showCard(index));
            navButtons.add(btn);
            sidebar.add(btn);
        }
        return sidebar;
    }

    private void showCard(int index) {
        if (index == 0) {
            refreshProductTable();
        } else if (index == 1) {
            refreshOrderTable();
        } else if (index == 3) {
            refreshStaffTable();
        }
        cardLayout.show(contentPanel, CARDS[index]);
        for (int i = 0; i < navButtons.size(); i++) {
            JButton b = navButtons.get(i);
            b.putClientProperty("active", i == index);
            b.setBackground(i == index ? Theme.NAV_ACTIVE : Theme.NAV_INACTIVE);
        }
    }

    private JButton navButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_BOLD.deriveFont(18f));
        btn.setForeground(Theme.DARK);
        btn.setBackground(Theme.NAV_INACTIVE);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(20, 16, 20, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!Boolean.TRUE.equals(btn.getClientProperty("active"))) {
                    btn.setBackground(Theme.brighten(Theme.NAV_INACTIVE, -8));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!Boolean.TRUE.equals(btn.getClientProperty("active"))) {
                    btn.setBackground(Theme.NAV_INACTIVE);
                }
            }
        });
        return btn;
    }

    private void logout() {
        dispose();
        new Login(manager).setVisible(true);
    }

    private static JLabel label(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    private static ImageIcon image(String path, int w, int h) {
        if (path == null || path.isBlank() || !new File(path).exists()) {
            return null;
        }
        ImageIcon raw = new ImageIcon(path);
        return raw.getIconWidth() > 0
                ? new ImageIcon(raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH)) : null;
    }

    private static String initial(String name) {
        return String.valueOf(Character.toUpperCase(name.charAt(0)));
    }

    private static double round(double d) {
        return Math.round(d * 100) / 100.0;
    }

    private static String money(double d) {
        return "Php" + round(d);
    }

    private boolean isInt(String s) {
        if (s.isEmpty() || s.length() > 9) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isNumber(String s) {
        if (s.isEmpty() || s.length() > 12 || s.equals(".")) {
            return false;
        }
        int dots = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '.') {
                dots++;
            } else if (!Character.isDigit(c)) {
                return false;
            }
        }
        return dots <= 1;
    }

    private JPanel sectionHeader(String title, JButton... actions) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 12, 0));
        bar.add(label(title, Theme.FONT_HEADER, Theme.DARK), BorderLayout.WEST);
        if (actions.length > 0) {
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            right.setOpaque(false);
            for (JButton b : actions) {
                right.add(b);
            }
            bar.add(right, BorderLayout.EAST);
        }
        return bar;
    }

    private JPanel cardPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Theme.LIGHT);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        return card;
    }

    private JTable newTable(String... columns) {
        JTable table = new JTable(new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        });
        Theme.styleTable(table);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setResizable(false);
        }
        return table;
    }

    private JScrollPane scroll(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.getViewport().setBackground(Theme.LIGHT);
        sp.setBorder(BorderFactory.createLineBorder(Theme.ACCENT, 1));
        JPanel corner = new JPanel();
        corner.setBackground(Theme.PRIMARY);
        sp.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corner);
        return sp;
    }

    private JPanel searchBar(JTable table, int... columns) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);
        for (int i = 0; i < table.getColumnCount(); i++) {
            sorter.setSortable(i, false);
        }

        JTextField search = new JTextField();
        search.setFont(Theme.FONT_LABEL);
        JLabel status = label(" ", Theme.FONT_BOLD, Theme.DARK);

        search.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String text = search.getText().trim().toLowerCase();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                    status.setText(" ");
                } else {
                    sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
                        @Override
                        public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                            for (int col : columns) {
                                if (entry.getStringValue(col).toLowerCase().contains(text)) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    });
                    int found = table.getRowCount();
                    status.setText(found == 0 ? "Not found" : found + " found");
                    status.setForeground(found == 0 ? Theme.RED : Theme.DARK);
                }
                table.clearSelection();
            }
        });

        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 8, 0));
        bar.add(label("Search:", Theme.FONT_BOLD, Theme.DARK), BorderLayout.WEST);
        bar.add(search, BorderLayout.CENTER);
        bar.add(status, BorderLayout.EAST);
        return bar;
    }

    private JPanel tableCard(String title, JTable table, int[] searchColumns, JButton... actions) {
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(searchBar(table, searchColumns), BorderLayout.NORTH);
        center.add(scroll(table), BorderLayout.CENTER);

        JPanel card = cardPanel();
        card.add(sectionHeader(title, actions), BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel manageProductsCard() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(0, 5));

        JPanel topStack = new JPanel();
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        topStack.setOpaque(false);
        topStack.add(sectionHeader("Manage Products"));
        topStack.add(productForm());
        topStack.add(productActionsRow());
        card.add(topStack, BorderLayout.NORTH);

        productTable = newTable("", "Name", "Category", "Price", "Stock");
        productModel = (DefaultTableModel) productTable.getModel();
        TableColumn iconCol = productTable.getColumnModel().getColumn(0);
        iconCol.setCellRenderer(new ProductIconRenderer());
        iconCol.setMinWidth(48);
        iconCol.setMaxWidth(48);
        productTable.setRowHeight(40);

        productTable.getSelectionModel().addListSelectionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row >= 0) {
                row = productTable.convertRowIndexToModel(row);
                Product p = (Product) productModel.getValueAt(row, 0);
                nameField.setText(p.getName());
                priceField.setText(String.valueOf(p.getPrice()));
                stockField.setText(String.valueOf(p.getStock()));
                categoryCombo.setSelectedItem(p.getCategory());
                selectedImagePath = p.getImagePath();
                updateImagePreview();
            }
        });
        refreshProductTable();
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(searchBar(productTable, 1, 2), BorderLayout.NORTH);
        center.add(scroll(productTable), BorderLayout.CENTER);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel productActionsRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        row.setOpaque(false);
        JButton addBtn = Theme.greenBtn("Add");
        JButton updateBtn = Theme.blueBtn("Update");
        JButton deleteBtn = Theme.redBtn("Delete");
        JButton cancelBtn = Theme.grayBtn("Cancel");
        addBtn.addActionListener(e -> addProduct());
        updateBtn.addActionListener(e -> updateProduct());
        deleteBtn.addActionListener(e -> deleteProduct());
        cancelBtn.addActionListener(e -> clearProductForm());
        for (JButton b : new JButton[]{addBtn, updateBtn, deleteBtn, cancelBtn}) {
            row.add(b);
        }
        return row;
    }

    private JPanel productForm() {
        JPanel form = new JPanel(new BorderLayout(16, 10));
        form.setBackground(Theme.CARD_BG);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.ACCENT, 1), new EmptyBorder(14, 14, 14, 14)));

        uploadImageBtn = Theme.button("Upload Image", Theme.LIGHT, Theme.DARK);
        uploadImageBtn.setPreferredSize(new Dimension(110, 110));
        uploadImageBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.ACCENT, 1, true), new EmptyBorder(6, 6, 6, 6)));
        uploadImageBtn.addActionListener(e -> chooseImage());
        form.add(uploadImageBtn, BorderLayout.WEST);

        nameField = new JTextField();
        priceField = new JTextField();
        stockField = new JTextField();
        categoryCombo = new JComboBox<>(new String[]{"Iced/Hot Coffee", "Non-Coffee", "Pastries"});
        for (JComponent c : new JComponent[]{nameField, priceField, stockField, categoryCombo}) {
            c.setFont(Theme.FONT_LABEL);
        }

        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 10));
        grid.setOpaque(false);
        grid.add(formField("Product Name:", nameField));
        grid.add(formField("Stock:", stockField));
        grid.add(formField("Price:", priceField));
        grid.add(formField("Category:", categoryCombo));
        form.add(grid, BorderLayout.CENTER);

        updateImagePreview();
        return form;
    }

    private JPanel formField(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.setOpaque(false);
        p.add(label(text, Theme.FONT_BOLD, Theme.DARK), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser(new File("C:\\Users\\Eloida Lyn\\OneDrive\\Documents\\NetBeansProjects\\CafeAmora\\src\\img"));
        chooser.setFileFilter(new FileNameExtensionFilter("Image files", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImagePath = chooser.getSelectedFile().getPath();
            updateImagePreview();
        }
    }

    private void updateImagePreview() {
        ImageIcon icon = image(selectedImagePath, 110, 110);
        uploadImageBtn.setIcon(icon);
        uploadImageBtn.setText(icon == null ? "Upload Image" : null);
    }

    private static class ProductIconRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel lbl = new JLabel();
            lbl.setOpaque(true);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setBackground(isSelected ? Theme.PRIMARY : Theme.LIGHT);

            Product p = (Product) value;
            ImageIcon icon = image(p.getImagePath(), 30, 30);
            if (icon != null) {
                lbl.setIcon(icon);
            } else {
                lbl.setText(initial(p.getName()));
                lbl.setForeground(Theme.LIGHT);
                lbl.setBackground(Theme.CARD_ACCENTS[Math.abs(p.getName().hashCode()) % Theme.CARD_ACCENTS.length]);
                lbl.setFont(Theme.FONT_BOLD);
            }
            return lbl;
        }
    }

    private void refreshProductTable() {
        productModel.setRowCount(0);
        for (Product p : manager.getProducts()) {
            productModel.addRow(new Object[]{p, p.getName(), p.getCategory(), money(p.getPrice()), p.getStock()});
        }
    }

    private boolean formValid() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product name cannot be empty.");
            return false;
        }
        if (SystemManager.hasBadChar(nameField.getText()) || SystemManager.hasBadChar(categoryText())) {
            JOptionPane.showMessageDialog(this, "Product name and category must not contain , : ; or |");
            return false;
        }
        if (!isNumber(priceField.getText().trim()) || !isInt(stockField.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Price must be a number and Stock must be an integer.");
            return false;
        }
        return true;
    }

    private void addProduct() {
        if (!formValid()) {
            return;
        }
        boolean ok = manager.addProduct(nameField.getText().trim(), categoryText(),
                Double.parseDouble(priceField.getText().trim()),
                Integer.parseInt(stockField.getText().trim()), selectedImagePath);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "A product with that name already exists.");
            return;
        }
        refreshProductTable();
        clearProductForm();
    }

    private void updateProduct() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a product from the table to update.");
            return;
        }
        row = productTable.convertRowIndexToModel(row);
        if (!formValid()) {
            return;
        }
        String originalName = ((Product) productModel.getValueAt(row, 0)).getName();
        boolean ok = manager.updateProduct(originalName, nameField.getText().trim(), categoryText(),
                Double.parseDouble(priceField.getText().trim()),
                Integer.parseInt(stockField.getText().trim()), selectedImagePath);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "A product with that name already exists.");
            return;
        }
        refreshProductTable();
        clearProductForm();
    }

    private void deleteProduct() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a product from the table to delete.");
            return;
        }
        row = productTable.convertRowIndexToModel(row);
        Product selected = (Product) productModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete \"" + selected.getName() + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (!askAdminPassword()) {
                return;
            }
            manager.deleteProduct(selected.getName());
            refreshProductTable();
            clearProductForm();
        }
    }

    private String categoryText() {
        Object item = categoryCombo.getEditor().getItem();
        String text = item == null ? "" : item.toString().trim();
        return text.isEmpty() ? "General" : text;
    }

    private void clearProductForm() {
        nameField.setText("");
        priceField.setText("");
        stockField.setText("");
        categoryCombo.setSelectedIndex(0);
        selectedImagePath = "";
        updateImagePreview();
        productTable.clearSelection();
    }

    private JPanel viewOrdersCard() {
        JTable table = newTable("Order ID", "Date/Time", "Cashier", "Total", "Payment", "Change");
        orderModel = (DefaultTableModel) table.getModel();
        JButton refreshBtn = Theme.blueBtn("Refresh");
        refreshBtn.addActionListener(e -> refreshOrderTable());
        refreshOrderTable();
        return tableCard("All Transactions", table, new int[]{0, 2}, refreshBtn);
    }

    private void refreshOrderTable() {
        orderModel.setRowCount(0);
        List<Order> orders = manager.getOrders();
        for (int i = orders.size() - 1; i >= 0; i--) {
            Order o = orders.get(i);
            orderModel.addRow(new Object[]{o.getOrderId(), o.getFormattedTimestamp(), o.getCashierUsername(),
                money(o.getTotal()), money(o.getPayment()), money(o.getChange())});
        }
    }

    private JPanel reportsCard() {
        JPanel card = cardPanel();
        JButton dailyBtn = Theme.blueBtn("Daily Report");
        JButton monthlyBtn = Theme.blueBtn("Monthly Report");
        dailyBtn.addActionListener(e -> generateReport(true));
        monthlyBtn.addActionListener(e -> generateReport(false));

        statOrdersValue = new JLabel("0");
        statItemsValue = new JLabel("0");
        statRevenueValue = new JLabel("Php0.00");
        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 12, 0));
        summaryRow.setOpaque(false);
        summaryRow.setBorder(new EmptyBorder(0, 0, 4, 0));
        summaryRow.add(reportSummary("Total Orders", statOrdersValue));
        summaryRow.add(reportSummary("Items Sold", statItemsValue));
        summaryRow.add(reportSummary("Total Revenue", statRevenueValue));

        JPanel topStack = new JPanel();
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        topStack.setOpaque(false);
        topStack.add(sectionHeader("Sales Reports", dailyBtn, monthlyBtn));
        topStack.add(summaryRow);
        topStack.add(Box.createVerticalStrut(10));
        card.add(topStack, BorderLayout.NORTH);

        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(Theme.FONT_MONO);
        reportArea.setBackground(Theme.CARD_BG);
        reportArea.setForeground(Theme.DARK);
        reportArea.setBorder(new EmptyBorder(12, 14, 12, 14));
        JScrollPane sp = scroll(reportArea);
        sp.getViewport().setBackground(Theme.CARD_BG);
        card.add(sp, BorderLayout.CENTER);

        generateReport(true);
        return card;
    }

    private JPanel reportSummary(String text, JLabel valueLbl) {
        JPanel tile = new JPanel(new BorderLayout(0, 4));
        tile.setBackground(Theme.CARD_BG);
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.ACCENT, 1), new EmptyBorder(12, 14, 12, 14)));
        valueLbl.setFont(Theme.FONT_TITLE.deriveFont(20f));
        valueLbl.setForeground(Theme.PRIMARY);
        tile.add(label(text, Theme.FONT_LABEL, Theme.DARK), BorderLayout.NORTH);
        tile.add(valueLbl, BorderLayout.CENTER);
        return tile;
    }

    private void generateReport(boolean daily) {
        LocalDateTime now = LocalDateTime.now();
        List<Order> orders = daily ? manager.getDailyOrders(now) : manager.getMonthlyOrders(now);

        StringBuilder sb = new StringBuilder();
        sb.append(daily ? "DAILY SALES REPORT" : "MONTHLY SALES REPORT").append("\n");
        sb.append("Generated: ").append(now.toLocalDate()).append("\n");
        sb.append("=".repeat(50)).append("\n\n");

        double total = 0;
        int itemsSold = 0;
        for (Order o : orders) {
            sb.append(String.format("Order #%d | %s | Cashier: %s | P%s%n",
                    o.getOrderId(), o.getFormattedTimestamp(), o.getCashierUsername(), round(o.getTotal())));
            for (OrderItem item : o.getItems()) {
                itemsSold += item.getQuantity();
            }
            total += o.getTotal();
        }
        sb.append("\n").append("-".repeat(50)).append("\n");
        sb.append("Total Orders: ").append(orders.size()).append("\n");
        sb.append("Total Items Sold: ").append(itemsSold).append("\n");
        sb.append("Total Revenue: Php").append(round(total)).append("\n");

        reportArea.setText(sb.toString());
        statOrdersValue.setText(String.valueOf(orders.size()));
        statItemsValue.setText(String.valueOf(itemsSold));
        statRevenueValue.setText(money(total));
    }

    private JPanel staffCard() {
        staffTable = newTable("Username", "Role", "Status");
        staffModel = (DefaultTableModel) staffTable.getModel();
        refreshStaffTable();

        JButton addBtn = Theme.greenBtn("Add Account");
        JButton unblockBtn = Theme.blueBtn("Unblock Selected");
        JButton removeBtn = Theme.redBtn("Remove Selected");
        addBtn.addActionListener(e -> addAccount());
        unblockBtn.addActionListener(e -> unblockSelected());
        removeBtn.addActionListener(e -> removeSelected());

        return tableCard("Staff Accounts", staffTable, new int[]{0, 1}, addBtn, unblockBtn, removeBtn);
    }

    private void refreshStaffTable() {
        staffModel.setRowCount(0);
        for (User u : manager.getUsers()) {
            String status = u.isBlocked() ? "Blocked" : "Active";
            staffModel.addRow(new Object[]{u.getUsername(), u.getRole(), status});
        }
    }

    private void addAccount() {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Admin", "Staff"});
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.setBorder(new EmptyBorder(10, 4, 4, 4));
        p.add(new JLabel("Username* :"));
        p.add(userField);
        p.add(new JLabel("Password* :"));
        p.add(passField);
        p.add(new JLabel("Confirm Password* :"));
        p.add(confirmField);
        p.add(new JLabel("Role* :"));
        p.add(roleBox);
        if (JOptionPane.showConfirmDialog(this, p, "Add Account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());
        String confirm = new String(confirmField.getPassword());
        String role = ((String) roleBox.getSelectedItem()).toUpperCase();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password required.");
        } else if (SystemManager.hasBadChar(username) || SystemManager.hasBadChar(password)) {
            JOptionPane.showMessageDialog(this, "Username and password must not contain , : ; or |");
        } else if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
        } else if (!manager.addUser(username, password, role)) {
            JOptionPane.showMessageDialog(this, "That username already exists.");
        }
        refreshStaffTable();
    }

    private void unblockSelected() {
        int row = staffTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an account to unblock.");
            return;
        }
        row = staffTable.convertRowIndexToModel(row);
        String username = (String) staffModel.getValueAt(row, 0);
        if (!manager.findUser(username).isBlocked()) {
            JOptionPane.showMessageDialog(this, "That account is not blocked.");
            return;
        }
        manager.unblockUser(username);
        refreshStaffTable();
    }

    private void removeSelected() {
        int row = staffTable.getSelectedRow();
        if (row >= 0) {
            row = staffTable.convertRowIndexToModel(row);
        }
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a staff account to remove.");
        } else if (!"STAFF".equals(staffModel.getValueAt(row, 1))) {
            JOptionPane.showMessageDialog(this, "Only Staff accounts can be removed here.");
        } else if (askAdminPassword()) {
            manager.removeStaff((String) staffModel.getValueAt(row, 0));
            refreshStaffTable();
        }
    }

    private boolean askAdminPassword() {
        JPasswordField field = new JPasswordField();
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 6));
        p.add(new JLabel("Enter the admin password to continue:"));
        p.add(field);
        if (JOptionPane.showConfirmDialog(this, p, "Admin Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return false;
        }
        if (!manager.verifyAdmin(new String(field.getPassword()))) {
            JOptionPane.showMessageDialog(this, "Incorrect admin password.");
            return false;
        }
        return true;
    }

    private void changeMyPassword() {
        JPasswordField oldField = new JPasswordField();
        JPasswordField newField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.setBorder(new EmptyBorder(10, 4, 4, 4));
        p.add(new JLabel("Current Password* :"));
        p.add(oldField);
        p.add(new JLabel("New Password* :"));
        p.add(newField);
        p.add(new JLabel("Confirm New Password* :"));
        p.add(confirmField);
        if (JOptionPane.showConfirmDialog(this, p, "Change Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        String oldPass = new String(oldField.getPassword());
        String newPass = new String(newField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (oldPass.isEmpty() || newPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
        } else if (SystemManager.hasBadChar(newPass)) {
            JOptionPane.showMessageDialog(this, "Password must not contain , : ; or |");
        } else if (!newPass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.");
        } else if (!manager.changePassword(admin.getUsername(), oldPass, newPass)) {
            JOptionPane.showMessageDialog(this, "Current password is incorrect.");
        } else {
            JOptionPane.showMessageDialog(this, "Password changed.");
        }
    }
}