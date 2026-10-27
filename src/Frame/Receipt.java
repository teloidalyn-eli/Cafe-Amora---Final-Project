package Frame;

import Main.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class Receipt extends JDialog {

    public Receipt(JFrame parent, Order order) {
        super(parent, "Receipt - Order #" + order.getOrderId(), true);
        setSize(400, 560);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.LIGHT);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Payment Successful", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.setForeground(Theme.LIGHT);
        header.setOpaque(true);
        header.setBackground(Theme.GREEN);
        header.setBorder(new EmptyBorder(14, 10, 14, 10));
        add(header, BorderLayout.NORTH);

        JTextArea receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(Theme.FONT_MONO);
        receiptArea.setBackground(Theme.CARD_BG);
        receiptArea.setForeground(Theme.DARK);
        receiptArea.setBorder(new EmptyBorder(16, 18, 16, 18));
        receiptArea.setText(receiptText(order));

        JButton closeBtn = Theme.grayBtn("Close");
        closeBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        buttons.setBackground(Theme.LIGHT);
        buttons.add(closeBtn);

        JScrollPane scroll = new JScrollPane(receiptArea);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 16, 0, 16));
        scroll.getViewport().setBackground(Theme.CARD_BG);

        add(scroll, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private String receiptText(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("\tCAFE AMORA RECEIPT\n");
        sb.append("======================================\n");
        sb.append("Order #: ").append(order.getOrderId()).append("\n");
        sb.append("Date   : ").append(order.getFormattedTimestamp()).append("\n");
        sb.append("Cashier: ").append(order.getCashierUsername()).append("\n");
        sb.append("--------------------------------------\n");
        for (OrderItem item : order.getItems()) {
            sb.append(padRight(truncate(item.getDisplayName(), 14), 24))
                    .append(" x").append(padRight(String.valueOf(item.getQuantity()), 2))
                    .append(" ").append(padLeft(String.valueOf(round(item.getSubtotal())), 7))
                    .append("\n");
        }
        sb.append("--------------------------------------\n");
        if (order.hasDiscount()) {
            sb.append(line("SUBTOTAL", order.getSubtotal()));
            sb.append(line("SPECIAL DISCOUNT -" + Math.round(order.getDiscountRate() * 100) + "%",
                    -order.getDiscountAmount()));
        }
        sb.append(line("TOTAL", order.getTotal()));
        sb.append("--------------------------------------\n");

        String method = order.getPaymentMethod() != null ? order.getPaymentMethod() : "Cash";
        sb.append(padRight("PAYMENT METHOD", 28)).append(" ").append(method).append("\n");
        sb.append(line("PAYMENT", order.getPayment()));
        sb.append(line("CHANGE", order.getChange()));
        sb.append("======================================\n");
        sb.append("\tThank you for your order!\n");
        return sb.toString();
    }

    private String line(String label, double value) {
        return padRight(label, 28) + " " + padLeft(String.valueOf(round(value)), 7) + "\n";
    }

    private double round(double d) {
        return Math.round(d * 100) / 100.0;
    }

    private String padRight(String s, int width) {
        while (s.length() < width) {
            s = s + " ";
        }
        return s;
    }

    private String padLeft(String s, int width) {
        while (s.length() < width) {
            s = " " + s;
        }
        return s;
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }
}