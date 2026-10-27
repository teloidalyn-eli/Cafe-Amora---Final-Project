package Frame;

import Main.*;
import Main.Main.SystemManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StaffDashboard extends JFrame {

    private static final double DISCOUNT_RATE = 0.20;
    private static final double VENTI_SURCHARGE = 15.00;
    private static final String[] CATEGORIES = {"All", "Iced/Hot Coffee", "Non-Coffee", "Pastries"};

    private final SystemManager manager;
    private final User staff;
    private final ArrayList<JButton> categoryButtons = new ArrayList<>();
    private final ArrayList<Order> heldOrders = new ArrayList<>();
    private String activeCategory = "All";

    private JPanel productsPanel, cartItemsPanel, discountRow;
    private JLabel cartCountBadge, subtotalValue, discountNameLabel, discountValue, totalValue;
    private JButton discountBtn, heldBtn;
    private Order currentOrder;

    public StaffDashboard(SystemManager manager, User staff) {
        this.manager = manager;
        this.staff = staff;
        this.currentOrder = manager.createOrder(staff.getUsername());
        setTitle("Staff Dashboard - " + staff.getUsername());
        setSize(1180, 700);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.LIGHT);
        dashboard();
    }

    private void dashboard() {
        add(headerPanel(), BorderLayout.NORTH);
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.LIGHT);
        main.add(productsArea(), BorderLayout.CENTER);
        main.add(cartSidebar(), BorderLayout.EAST);
        add(main, BorderLayout.CENTER);
        refreshCart();
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

    private JScrollPane scrollOf(Component view, Color bg) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(bg);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private void fullWidth(JComponent c, int height) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
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

        heldBtn = Theme.blueBtn("Held (0)");
        heldBtn.addActionListener(e -> showHeldOrders());
        JButton logoutBtn = Theme.blueBtn("Logout");
        logoutBtn.addActionListener(e -> logout());
        JButton changePassBtn = Theme.blueBtn("Change Password");
        changePassBtn.addActionListener(e -> changeMyPassword());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(label(staff.getUsername() + " (Cashier)  ", Theme.FONT_LABEL, Theme.DARK));
        right.add(heldBtn);
        right.add(changePassBtn);
        right.add(logoutBtn);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel productsArea() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Theme.LIGHT);
        wrap.setBorder(new EmptyBorder(18, 24, 18, 12));

        JLabel heading = label("Menu", Theme.FONT_TITLE.deriveFont(20f), Theme.DARK);
        heading.setBorder(new EmptyBorder(0, 4, 10, 0));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 10, 0));
        top.add(heading, BorderLayout.NORTH);
        top.add(categoryFilterRow(), BorderLayout.CENTER);
        wrap.add(top, BorderLayout.NORTH);

        productsPanel = new JPanel(new GridLayout(0, 4, 16, 16));
        productsPanel.setBackground(Theme.LIGHT);
        refreshProductCards();
        wrap.add(scrollOf(productsPanel, Theme.LIGHT), BorderLayout.CENTER);
        return wrap;
    }

    private JPanel categoryFilterRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        for (String cat : CATEGORIES) {
            JButton btn = new JButton(cat);
            btn.setFont(Theme.FONT_BOLD.deriveFont(13f));
            btn.setForeground(Theme.DARK);
            btn.setContentAreaFilled(false);
            btn.setOpaque(true);
            btn.setFocusPainted(false);
            btn.setBorder(new EmptyBorder(8, 16, 8, 16));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                activeCategory = cat;
                updateCategoryHighlight();
                refreshProductCards();
            });
            categoryButtons.add(btn);
            row.add(btn);
        }
        updateCategoryHighlight();
        return row;
    }

    private void updateCategoryHighlight() {
        for (JButton b : categoryButtons) {
            b.setBackground(b.getText().equals(activeCategory) ? Theme.NAV_ACTIVE : Theme.NAV_INACTIVE);
        }
    }

    private void refreshProductCards() {
        productsPanel.removeAll();
        int shown = 0;
        for (Product p : manager.getProducts()) {
            if ("All".equals(activeCategory) || p.getCategory().equalsIgnoreCase(activeCategory)) {
                productsPanel.add(productCard(p, shown++));
            }
        }
        if (shown == 0) {
            JLabel empty = label("No items in \"" + activeCategory + "\".", Theme.FONT_LABEL, Theme.PRIMARY);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setBorder(new EmptyBorder(40, 10, 10, 10));
            productsPanel.add(empty);
        }
        productsPanel.revalidate();
        productsPanel.repaint();
    }

    private JPanel productCard(Product product, int index) {
        boolean outOfStock = product.getStock() <= 0;
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD_BG);
        card.setBorder(BorderFactory.createLineBorder(Theme.ACCENT, 1));
        card.setPreferredSize(new Dimension(240, 300));
        card.setCursor(new Cursor(outOfStock ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));

        JPanel photoBox = new JPanel(new GridBagLayout());
        photoBox.setPreferredSize(new Dimension(0, 220));
        ImageIcon img = image(product.getImagePath(), 240, 220);
        if (img != null) {
            photoBox.setBackground(Theme.CARD_BG);
            photoBox.setLayout(new BorderLayout());
            photoBox.add(new JLabel(img, SwingConstants.CENTER), BorderLayout.CENTER);
        } else {
            photoBox.setBackground(outOfStock ? Theme.LIGHT : Theme.CARD_ACCENTS[index % Theme.CARD_ACCENTS.length]);
            photoBox.add(label(initial(product.getName()), Theme.FONT_TITLE.deriveFont(45f),
                    outOfStock ? Theme.PRIMARY : Theme.LIGHT));
        }
        card.add(photoBox, BorderLayout.NORTH);

        JLabel nameLbl = label(product.getName(), Theme.FONT_BOLD, outOfStock ? Theme.PRIMARY : Theme.DARK);
        JLabel priceLbl = label(outOfStock ? "Out of stock" : money(product.getPrice()),
                Theme.FONT_LABEL.deriveFont(11f), outOfStock ? Theme.RED : Theme.PRIMARY);
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(Theme.CARD_BG);
        info.setBorder(new EmptyBorder(8, 6, 8, 6));
        for (JLabel l : new JLabel[]{nameLbl, priceLbl}) {
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        info.add(nameLbl);
        info.add(Box.createVerticalStrut(2));
        info.add(priceLbl);
        card.add(info, BorderLayout.CENTER);

        if (outOfStock) {
            card.setToolTipText(product.getName() + " is out of stock.");
        } else {
            java.awt.event.MouseAdapter click = new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    addToOrder(product);
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    card.setBorder(BorderFactory.createLineBorder(Theme.PRIMARY, 2));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    card.setBorder(BorderFactory.createLineBorder(Theme.ACCENT, 1));
                }
            };
            card.addMouseListener(click);
            info.addMouseListener(click);
        }
        return card;
    }

    private JPanel cartSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(460, 0));
        sidebar.setBackground(Theme.LIGHT);
        sidebar.setBorder(new MatteBorder(0, 1, 0, 0, Theme.ACCENT));

        JPanel cartHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cartHeader.setBackground(Theme.LIGHT);
        cartHeader.setBorder(new EmptyBorder(18, 18, 10, 18));
        cartCountBadge = Theme.badge("0");
        cartHeader.add(label("Cart", Theme.FONT_TITLE.deriveFont(20f), Theme.DARK));
        cartHeader.add(cartCountBadge);
        sidebar.add(cartHeader, BorderLayout.NORTH);

        cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setBackground(Theme.CARD_BG);
        sidebar.add(scrollOf(cartItemsPanel, Theme.CARD_BG), BorderLayout.CENTER);
        sidebar.add(checkoutPanel(), BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel summaryRow(JLabel left, JLabel right, int bottomGap) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Theme.LIGHT);
        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        row.setBorder(new EmptyBorder(0, 0, bottomGap, 0));
        fullWidth(row, 24);
        return row;
    }

    private JPanel checkoutPanel() {
        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.setBackground(Theme.LIGHT);
        south.setBorder(new EmptyBorder(10, 18, 18, 18));

        subtotalValue = label("0.00", Theme.FONT_LABEL, Theme.DARK);
        south.add(summaryRow(label("Subtotal", Theme.FONT_LABEL, Theme.PRIMARY), subtotalValue, 4));

        discountNameLabel = label("Discount", Theme.FONT_LABEL, Theme.GREEN.darker());
        discountValue = label("-0.00", Theme.FONT_LABEL, Theme.GREEN.darker());
        discountRow = summaryRow(discountNameLabel, discountValue, 8);
        south.add(discountRow);

        totalValue = label("0.00", Theme.FONT_HEADER, Theme.LIGHT);
        JPanel totalBar = new JPanel(new BorderLayout());
        totalBar.setBackground(Theme.PRIMARY);
        totalBar.setBorder(new EmptyBorder(12, 14, 12, 14));
        totalBar.add(label("Item Total", Theme.FONT_BOLD, Theme.LIGHT), BorderLayout.WEST);
        totalBar.add(totalValue, BorderLayout.EAST);
        totalBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        south.add(Box.createVerticalStrut(2));
        south.add(totalBar);
        south.add(Box.createVerticalStrut(14));

        discountBtn = Theme.blueBtn("Discount");
        fullWidth(discountBtn, 40);
        discountBtn.addActionListener(e -> chooseDiscount());

        JButton checkoutBtn = Theme.greenBtn("Checkout");
        fullWidth(checkoutBtn, 44);
        checkoutBtn.addActionListener(e -> checkout());

        JButton holdBtn = Theme.grayBtn("Hold");
        JButton cancelBtn = Theme.redBtn("Cancel");
        holdBtn.addActionListener(e -> holdOrder());
        cancelBtn.addActionListener(e -> cancelOrder());
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomRow.setBackground(Theme.LIGHT);
        bottomRow.add(holdBtn);
        bottomRow.add(cancelBtn);

        fullWidth(bottomRow, 40);

        south.add(discountBtn);
        south.add(Box.createVerticalStrut(8));
        south.add(checkoutBtn);
        south.add(Box.createVerticalStrut(8));
        south.add(bottomRow);
        return south;
    }

    private JPanel cartRow(OrderItem item, int accentIndex) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Theme.CARD_BG);
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.ACCENT), new EmptyBorder(10, 14, 10, 14)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JPanel icon = new JPanel(new BorderLayout());
        icon.setPreferredSize(new Dimension(40, 40));
        Product p = manager.findProduct(item.getProductName());
        ImageIcon img = p == null ? null : image(p.getImagePath(), 36, 36);
        if (img != null) {
            icon.setBackground(Theme.CARD_BG);
            icon.add(new JLabel(img, SwingConstants.CENTER), BorderLayout.CENTER);
        } else {
            icon.setBackground(Theme.CARD_ACCENTS[accentIndex % Theme.CARD_ACCENTS.length]);
            JLabel letter = label(initial(item.getProductName()), Theme.FONT_BOLD, Theme.LIGHT);
            letter.setHorizontalAlignment(SwingConstants.CENTER);
            icon.add(letter, BorderLayout.CENTER);
        }
        row.add(icon, BorderLayout.WEST);

        JPanel details = new JPanel(new GridLayout(2, 1));
        details.setOpaque(false);
        details.add(label(item.getDisplayName(), Theme.FONT_BOLD, Theme.DARK));
        details.add(label(money(item.getUnitPrice()), Theme.FONT_LABEL, Theme.PRIMARY));
        row.add(details, BorderLayout.CENTER);

        JTextField qtyField = new JTextField(String.valueOf(item.getQuantity()), 3);
        qtyField.setHorizontalAlignment(JTextField.CENTER);
        qtyField.setFont(Theme.FONT_BOLD);
        qtyField.setForeground(Theme.DARK);
        qtyField.setBackground(Color.WHITE);
        qtyField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.ACCENT, 1, true), new EmptyBorder(4, 4, 4, 4)));
        qtyField.setToolTipText("Type a quantity for bulk orders, then press Enter");
        qtyField.addActionListener(e -> commitQuantityChange(item, qtyField));
        qtyField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                qtyField.selectAll();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                commitQuantityChange(item, qtyField);
            }
        });

        JButton minus = Theme.plusMinusBtn("-", Theme.PRIMARY, Theme.LIGHT);
        JButton plus = Theme.plusMinusBtn("+", Theme.PRIMARY, Theme.LIGHT);
        minus.addActionListener(e -> changeQty(item, -1));
        plus.addActionListener(e -> changeQty(item, +1));

        JPanel stepper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        stepper.setOpaque(false);
        stepper.add(minus);
        stepper.add(qtyField);
        stepper.add(plus);
        row.add(stepper, BorderLayout.EAST);
        return row;
    }

    private int availableStock(String productName, int otherSizesQty, int desiredQty) {
        int maxForThisLine = Math.max(0, manager.maxOrderQuantity(productName) - otherSizesQty);
        if (desiredQty > maxForThisLine) {
            JOptionPane.showMessageDialog(this,
                    "Only " + maxForThisLine + " more unit(s) of \"" + productName + "\" available.");
            return maxForThisLine;
        }
        return desiredQty;
    }

    private int totalQuantityInCart(String productName) {
        int sum = 0;
        for (OrderItem it : currentOrder.getItems()) {
            if (it.getProductName().equalsIgnoreCase(productName)) {
                sum += it.getQuantity();
            }
        }
        return sum;
    }

    private void changeQty(OrderItem item, int delta) {
        int newQty = item.getQuantity() + delta;
        if (delta > 0) {
            int otherQty = totalQuantityInCart(item.getProductName()) - item.getQuantity();
            newQty = availableStock(item.getProductName(), otherQty, newQty);
        } else if (newQty < 0) {
            newQty = 0;
        }
        setQty(item, newQty);
    }

    private void setQty(OrderItem item, int qty) {
        if (qty <= 0) {
            currentOrder.getItems().remove(item);
        } else {
            item.setQuantity(qty);
        }
        refreshCart();
    }

    private void commitQuantityChange(OrderItem item, JTextField field) {
        String text = field.getText().trim();
        if (!currentOrder.getItems().contains(item)) {
            return;
        }
        if (!isInt(text)) {
            JOptionPane.showMessageDialog(this, "Enter a whole number quantity.");
            refreshCart();
            return;
        }
        int qty = Integer.parseInt(text);
        if (qty == item.getQuantity()) {
            return;
        }
        if (qty > 0) {
            int otherQty = totalQuantityInCart(item.getProductName()) - item.getQuantity();
            qty = availableStock(item.getProductName(), otherQty, qty);
        }
        setQty(item, qty);
    }

    private void addToOrder(Product product) {
        String size = "";
        double unitPrice = product.getPrice();
        if (product.isDrink()) {
            size = chooseSize(product);
            if (size == null) {
                return;
            }
            unitPrice += "Venti".equals(size) ? VENTI_SURCHARGE : 0;
        }
        for (OrderItem item : currentOrder.getItems()) {
            if (item.getProductName().equalsIgnoreCase(product.getName()) && item.getSize().equals(size)) {
                changeQty(item, 1);
                return;
            }
        }
        int capped = availableStock(product.getName(), totalQuantityInCart(product.getName()), 1);
        if (capped > 0) {
            currentOrder.addItem(new OrderItem(product.getName(), unitPrice, capped, size));
            refreshCart();
        }
    }

    private void refreshCart() {
        cartItemsPanel.removeAll();
        List<OrderItem> items = currentOrder.getItems();
        int totalQty = 0;
        for (int i = 0; i < items.size(); i++) {
            cartItemsPanel.add(cartRow(items.get(i), i));
            totalQty += items.get(i).getQuantity();
        }
        if (items.isEmpty()) {
            JLabel empty = label("Cart is empty - tap a product to add it.", Theme.FONT_LABEL, Theme.PRIMARY);
            empty.setBorder(new EmptyBorder(30, 10, 10, 10));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            cartItemsPanel.add(empty);
        }
        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();

        cartCountBadge.setText(String.valueOf(totalQty));
        subtotalValue.setText(String.valueOf(round(currentOrder.getSubtotal())));
        totalValue.setText(String.valueOf(round(currentOrder.getTotal())));

        boolean hasDiscount = currentOrder.hasDiscount();
        discountRow.setVisible(hasDiscount);
        if (hasDiscount) {
            discountNameLabel.setText(currentOrder.getDiscountLabel()
                    + " (" + Math.round(currentOrder.getDiscountRate() * 100) + "%)");
            discountValue.setText("-" + round(currentOrder.getDiscountAmount()));
        }
        discountBtn.setText(hasDiscount ? "Discount: " + currentOrder.getDiscountLabel() : "Discount");
    }

    private boolean confirm(String title, JPanel panel) {
        return JOptionPane.showConfirmDialog(this, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    private JPanel boxPanel(Component... parts) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.CARD_BG);
        for (Component c : parts) {
            panel.add(c);
        }
        return panel;
    }

    private JRadioButton radio(String text, boolean selected, ButtonGroup group) {
        JRadioButton rb = new JRadioButton(text, selected);
        rb.setFont(Theme.FONT_LABEL);
        rb.setBackground(Theme.CARD_BG);
        group.add(rb);
        return rb;
    }

    private String chooseSize(Product product) {
        ButtonGroup group = new ButtonGroup();
        JRadioButton grande = radio("Grande - " + money(product.getPrice()), true, group);
        JRadioButton venti = radio("Venti - " + money(product.getPrice() + VENTI_SURCHARGE), false, group);
        JPanel panel = boxPanel(new JLabel("Choose a size for " + product.getName() + ":"),
                Box.createVerticalStrut(8), grande, venti);
        if (!confirm("Select Size", panel)) {
            return null;
        }
        return venti.isSelected() ? "Venti" : "Grande";
    }

    private void chooseDiscount() {
        String current = currentOrder.getDiscountLabel();
        ButtonGroup group = new ButtonGroup();
        JRadioButton none = radio("None", !currentOrder.hasDiscount(), group);
        JRadioButton senior = radio("Senior Citizen (20%)", "Senior Citizen".equals(current), group);
        JRadioButton pwd = radio("PWD (20%)", "PWD".equals(current), group);
        JLabel note = label("Valid ID must be presented and recorded per store policy.",
                Theme.FONT_LABEL.deriveFont(Font.ITALIC, 11f), Theme.PRIMARY);
        JPanel panel = boxPanel(new JLabel("Apply a discount to this order:"), Box.createVerticalStrut(8),
                none, senior, pwd, Box.createVerticalStrut(8), note);
        if (!confirm("Discount", panel)) {
            return;
        }
        if (senior.isSelected()) {
            currentOrder.setDiscount("Senior Citizen", DISCOUNT_RATE);
        } else if (pwd.isSelected()) {
            currentOrder.setDiscount("PWD", DISCOUNT_RATE);
        } else {
            currentOrder.setDiscount("None", 0.0);
        }
        refreshCart();
    }

    private void startNewOrder() {
        currentOrder = manager.createOrder(staff.getUsername());
        refreshCart();
    }

    private void checkout() {
        if (currentOrder.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty. Add items first.");
            return;
        }
        double total = currentOrder.getTotal();
        JComboBox<String> methodBox = new JComboBox<>(new String[]{"Cash", "GCash", "PayPal"});
        methodBox.setFont(Theme.FONT_LABEL);
        JTextField paymentField = new JTextField();
        paymentField.setFont(Theme.FONT_LABEL);

        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 6));
        panel.setBackground(Theme.CARD_BG);
        panel.add(new JLabel("Amount due: " + money(total)));
        panel.add(new JLabel("Select Payment Method:"));
        panel.add(methodBox);
        panel.add(new JLabel("Enter payment received:"));
        panel.add(paymentField);
        if (!confirm("Checkout", panel)) {
            return;
        }

        String method = (String) methodBox.getSelectedItem();
        String paymentText = paymentField.getText().trim();
        if (!isNumber(paymentText)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid payment amount.");
            return;
        }
        double payment = Double.parseDouble(paymentText);
        if ("Cash".equals(method) && payment < total) {
            JOptionPane.showMessageDialog(this, "Insufficient payment. Amount is short by " + money(total - payment));
            return;
        } else if (!"Cash".equals(method) && Math.abs(payment - total) >= 0.005) {
            JOptionPane.showMessageDialog(this, "For " + method + ", payment must be exactly "
                    + money(total) + ". Extra amount is not allowed.");
            return;
        }

        manager.completeOrder(currentOrder, payment, method);
        new Receipt(this, currentOrder).setVisible(true);
        startNewOrder();
        refreshProductCards();
    }

    private void holdOrder() {
        if (currentOrder.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty - nothing to hold.");
            return;
        }
        heldOrders.add(currentOrder);
        heldBtn.setText("Held (" + heldOrders.size() + ")");
        startNewOrder();
        JOptionPane.showMessageDialog(this, "Order held. Use \"Held\" in the header to resume it.");
    }

    private void showHeldOrders() {
        if (heldOrders.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No held orders.");
            return;
        }
        String[] labels = new String[heldOrders.size()];
        for (int i = 0; i < labels.length; i++) {
            Order o = heldOrders.get(i);
            labels[i] = "Held " + (i + 1) + " - " + o.getItems().size()
                    + " item(s) - " + money(o.getTotal());
        }
        String choice = (String) JOptionPane.showInputDialog(this, "Select a held order to resume:",
                "Held Orders", JOptionPane.PLAIN_MESSAGE, null, labels, labels[0]);
        if (choice == null) {
            return;
        }
        Order resumed = heldOrders.remove(java.util.Arrays.asList(labels).indexOf(choice));
        if (!currentOrder.getItems().isEmpty()) {
            heldOrders.add(currentOrder);
        }
        currentOrder = resumed;
        heldBtn.setText("Held (" + heldOrders.size() + ")");
        refreshCart();
    }

    private void cancelOrder() {
        if (!currentOrder.getItems().isEmpty() && JOptionPane.showConfirmDialog(this,
                "Clear the current cart?", "Cancel Order", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            startNewOrder();
        }
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
        } else if (!manager.changePassword(staff.getUsername(), oldPass, newPass)) {
            JOptionPane.showMessageDialog(this, "Current password is incorrect.");
        } else {
            JOptionPane.showMessageDialog(this, "Password changed.");
        }
    }

    private void logout() {
        dispose();
        new Login(manager).setVisible(true);
    }
}