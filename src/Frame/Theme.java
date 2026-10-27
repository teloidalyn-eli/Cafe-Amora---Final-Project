package Frame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public final class Theme {

    public static final Color DARK = new Color(35, 46, 64);
    public static final Color LIGHT = new Color(255, 255, 255);
    public static final Color PRIMARY = new Color(58, 99, 179);
    public static final Color ACCENT = new Color(58, 99, 179);
    public static final Color CARD_BG = new Color(246, 249, 253);
    public static final Color GREEN = new Color(90, 158, 96);
    public static final Color RED = new Color(196, 77, 74);
    public static final Color NAV_INACTIVE = new Color(241, 243, 246);
    public static final Color NAV_ACTIVE = new Color(214, 228, 249);

    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 26);
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_LABEL = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_MONO = new Font(Font.MONOSPACED, Font.PLAIN, 13);

    public static final Color[] CARD_ACCENTS = {
        PRIMARY, brighten(PRIMARY, 35), ACCENT, brighten(DARK, 60), brighten(PRIMARY, 60)
    };

    public static JButton button(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(brighten(bg, 18));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(bg);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(bg.darker());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(brighten(bg, 18));
                }
            }
        });
        btn.addPropertyChangeListener("enabled",
                e -> btn.setBackground(btn.isEnabled() ? bg : brighten(bg, 90)));
        return btn;
    }

    public static JButton blueBtn(String text) {
        return button(text, PRIMARY, LIGHT);
    }

    public static JButton redBtn(String text) {
        return button(text, RED, LIGHT);
    }

    public static JButton greenBtn(String text) {
        return button(text, GREEN, LIGHT);
    }

    public static JButton grayBtn(String text) {
        return button(text, new Color(139, 150, 165), LIGHT);
    }

    public static JButton plusMinusBtn(String text, Color bg, Color fg) {
        JButton btn = button(text, bg, fg);
        btn.setFont(FONT_BUTTON.deriveFont(14f));
        btn.setBorder(new EmptyBorder(4, 10, 4, 10));
        btn.setMargin(new Insets(0, 0, 0, 0));
        return btn;
    }

    public static JLabel badge(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(PRIMARY);
        lbl.setForeground(LIGHT);
        lbl.setFont(FONT_BOLD.deriveFont(12f));
        lbl.setBorder(new EmptyBorder(3, 10, 3, 10));
        return lbl;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(FONT_LABEL);
        table.setForeground(DARK);
        table.setGridColor(ACCENT);
        table.setSelectionBackground(PRIMARY);
        table.setSelectionForeground(LIGHT);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 4));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 34));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setBackground(PRIMARY);
                setForeground(LIGHT);
                setFont(FONT_HEADER.deriveFont(13f));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(LIGHT);
                    c.setForeground(DARK);
                }
                setBorder(new EmptyBorder(2, 10, 2, 10));
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    public static Color brighten(Color c, int amt) {
        return new Color(
                Math.min(255, c.getRed() + amt),
                Math.min(255, c.getGreen() + amt),
                Math.min(255, c.getBlue() + amt));
    }

    public static JPanel borderedPanel(Color bg, Color borderColor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 2),
                new EmptyBorder(5, 5, 5, 5)));
        return p;
    }

}