package Frame;

import Main.User;
import Main.Main.SystemManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class Login extends JFrame {

    private final SystemManager manager;
    private JTextField username;
    private JPasswordField password;
    private JLabel messageLabel;

    public Login(SystemManager manager) {
        this.manager = manager;
        LoginFrame();
    }

    private void LoginFrame() {
        setTitle("Cafe Amora - Login");
        setSize(420, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.LIGHT);

        JLabel title = new JLabel("Cafe Amora", SwingConstants.CENTER);
        title.setFont(Theme.FONT_HEADER.deriveFont(20f));
        title.setForeground(Theme.DARK);
        title.setBorder(new EmptyBorder(28, 10, 20, 10));
        ImageIcon logoIcon = new ImageIcon("src\\img\\LOGO\\CafeAmora-LOGO.png");
        Image scaledLogo = logoIcon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
        title.setIcon(new ImageIcon(scaledLogo));
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(4, 1, 0, 16));
        formPanel.setBackground(Theme.CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.ACCENT, 1),
                new EmptyBorder(24, 24, 24, 24)));

        username = new JTextField();
        username.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Theme.ACCENT), "Username",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                Theme.FONT_LABEL, Theme.PRIMARY));
        username.setFont(Theme.FONT_LABEL);
        username.setOpaque(false);
        password = new JPasswordField();
        password.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Theme.ACCENT), "Password",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                Theme.FONT_LABEL, Theme.PRIMARY));
        password.setFont(Theme.FONT_LABEL);
        password.setOpaque(false);
        JButton loginBtn = Theme.blueBtn("Login");

        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(Theme.FONT_BOLD);
        messageLabel.setForeground(Theme.RED);

        formPanel.add(username);
        formPanel.add(password);
        formPanel.add(loginBtn);
        formPanel.add(messageLabel);

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(Theme.LIGHT);
        wrap.setBorder(new EmptyBorder(0, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        wrap.add(formPanel, gbc);
        add(wrap, BorderLayout.CENTER);

        loginBtn.addActionListener(this::attemptLogin);
        password.addActionListener(this::attemptLogin);
    }

    private void attemptLogin(ActionEvent e) {
        String username = this.username.getText().trim();
        String password = new String(this.password.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            return;
        }

        String result = manager.login(username, password);

        if (result.equals(SystemManager.LOGIN_NOT_FOUND)) {
            messageLabel.setText("Username not found.");
            this.password.setText("");
            return;
        }
        if (result.equals(SystemManager.LOGIN_BLOCKED)) {
            messageLabel.setText("Account blocked. Contact the admin.");
            this.password.setText("");
            return;
        }
        if (result.equals(SystemManager.LOGIN_WRONG_PASSWORD)) {
            User found = manager.findUser(username);
            int left = SystemManager.MAX_FAILED_ATTEMPTS - found.getFailedAttempts();
            messageLabel.setText("Wrong password. Attempts left: " + left);
            this.password.setText("");
            return;
        }

        User user = manager.findUser(username);
        dispose();
        if (user.getRole().equals("ADMIN")) {
            new AdminDashboard(manager, user).setVisible(true);
        } else if (user.getRole().equals("STAFF")) {
            new StaffDashboard(manager, user).setVisible(true);
        }
    }
}
