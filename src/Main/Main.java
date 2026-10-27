package Main;

import Frame.Login;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        manager = new SystemManager();
        SwingUtilities.invokeLater(() -> new Login(manager).setVisible(true));
    }
    private static SystemManager manager;

    public static class SystemManager {

        private static final String DATA = "data";
        private static final String USERS = DATA + File.separator + "users.txt";
        private static final String PRODUCTS = DATA + File.separator + "products.txt";
        private static final String ORDERS = DATA + File.separator + "orders.txt";
        private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        private ArrayList<User> users = new ArrayList<>();
        private ArrayList<Product> products = new ArrayList<>();
        private ArrayList<Order> orders = new ArrayList<>();
        private int nextOrderId = 1;

        public SystemManager() {
            new File(DATA).mkdirs();
            loadUsers();
            loadProducts();
            loadOrders();
        }

        public static final int MAX_FAILED_ATTEMPTS = 3;
        public static final String LOGIN_OK = "OK";
        public static final String LOGIN_BLOCKED = "BLOCKED";
        public static final String LOGIN_WRONG_PASSWORD = "WRONG_PASSWORD";
        public static final String LOGIN_NOT_FOUND = "NOT_FOUND";

        public String login(String username, String password) {
            User u = findUser(username);
            if (u == null) {
                return LOGIN_NOT_FOUND;
            }
            if (u.isBlocked()) {
                return LOGIN_BLOCKED;
            }
            if (u.checkPassword(password)) {
                u.resetFailedAttempts();
                return LOGIN_OK;
            }
            u.addFailedAttempt();
            if (u.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                u.setBlocked(true);
                saveUsers();
                return LOGIN_BLOCKED;
            }
            return LOGIN_WRONG_PASSWORD;
        }

        public boolean unblockUser(String username) {
            User u = findUser(username);
            if (u == null) {
                return false;
            }
            u.setBlocked(false);
            u.resetFailedAttempts();
            saveUsers();
            return true;
        }

        public boolean addUser(String username, String password, String role) {
            if (findUser(username) != null) {
                return false;
            }
            users.add(createUser(username, password, role.toUpperCase()));
            saveUsers();
            return true;
        }

        public boolean changePassword(String username, String oldPassword, String newPassword) {
            User u = findUser(username);
            if (u == null || !u.checkPassword(oldPassword)) {
                return false;
            }
            u.setPassword(newPassword);
            saveUsers();
            return true;
        }

        public boolean verifyAdmin(String password) {
            for (User u : users) {
                if (u.getRole().equals("ADMIN") && u.checkPassword(password)) {
                    return true;
                }
            }
            return false;
        }

        public static boolean hasBadChar(String text) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == ',' || c == ':' || c == ';' || c == '|') {
                    return true;
                }
            }
            return false;
        }

        public boolean removeStaff(String username) {
            User u = findUser(username);
            if (u == null || !u.getRole().equals("STAFF")) {
                return false;
            }
            users.remove(u);
            saveUsers();
            return true;
        }

        public User findUser(String username) {
            for (User u : users) {
                if (u.getUsername().equalsIgnoreCase(username)) {
                    return u;
                }
            }
            return null;
        }

        public ArrayList<User> getUsers() {
            return users;
        }

        public ArrayList<Product> getProducts() {
            return products;
        }

        public Product findProduct(String name) {
            for (Product p : products) {
                if (p.getName().equalsIgnoreCase(name)) {
                    return p;
                }
            }
            return null;
        }

        public boolean addProduct(String name, String category, double price, int stock) {
            return addProduct(name, category, price, stock, "");
        }

        public boolean addProduct(String name, String category, double price, int stock, String imagePath) {
            if (findProduct(name) != null) {
                return false;
            }
            products.add(new Product(name, category, price, stock, imagePath));
            saveProducts();
            return true;
        }

        public boolean updateProduct(String originalName, String name, String category,
                double price, int stock) {
            Product existing = findProduct(originalName);
            String imagePath = existing != null ? existing.getImagePath() : "";
            return updateProduct(originalName, name, category, price, stock, imagePath);
        }

        public boolean updateProduct(String originalName, String name, String category,
                double price, int stock, String imagePath) {
            Product p = findProduct(originalName);
            if (p == null) {
                return false;
            }
            Product sameName = findProduct(name);
            if (sameName != null && sameName != p) {
                return false;
            }
            p.setName(name);
            p.setCategory(category);
            p.setPrice(price);
            p.setStock(stock);
            p.setImagePath(imagePath);
            saveProducts();
            return true;
        }

        public boolean deleteProduct(String name) {
            Product p = findProduct(name);
            if (p == null) {
                return false;
            }
            products.remove(p);
            saveProducts();
            return true;
        }

        public Order createOrder(String cashierUsername) {
            return new Order(0, cashierUsername);
        }

        public int maxOrderQuantity(String name) {
            Product p = findProduct(name);
            return p != null ? p.getStock() : Integer.MAX_VALUE;
        }

        public void completeOrder(Order order, double payment, String paymentMethod) {
            order.setOrderId(nextOrderId++);
            order.setPayment(payment);
            order.setPaymentMethod(paymentMethod);

            for (OrderItem item : order.getItems()) {
                Product p = findProduct(item.getProductName());
                if (p != null) {
                    p.reduceStock(item.getQuantity());
                }
            }

            orders.add(order);
            saveProducts();
            appendOrder(order);
        }

        public ArrayList<Order> getOrders() {
            return orders;
        }

        public ArrayList<Order> getOrdersBetween(LocalDateTime from, LocalDateTime to) {
            ArrayList<Order> result = new ArrayList<>();
            for (Order o : orders) {
                if (!o.getTimestamp().isBefore(from) && !o.getTimestamp().isAfter(to)) {
                    result.add(o);
                }
            }
            return result;
        }

        public ArrayList<Order> getDailyOrders(LocalDateTime day) {
            LocalDateTime start = day.truncatedTo(ChronoUnit.DAYS);
            LocalDateTime end = start.plusDays(1).minusNanos(1);
            return getOrdersBetween(start, end);
        }

        public ArrayList<Order> getMonthlyOrders(LocalDateTime anyDayInMonth) {
            LocalDateTime start = anyDayInMonth.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1);
            LocalDateTime end = start.plusMonths(1).minusNanos(1);
            return getOrdersBetween(start, end);
        }

        private ArrayList<String> readLines(String path) {
            File file = new File(path);
            if (!file.exists()) {
                return new ArrayList<>();
            }
            ArrayList<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException e) {
                System.out.println("Failed to read file: " + path + " - " + e.getMessage());
            }
            return lines;
        }

        private void writeLines(String path, List<String> lines, boolean append) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(path, append))) {
                for (String line : lines) {
                    pw.println(line);
                }
            } catch (IOException e) {
                String action = append ? "append to" : "write";
                System.out.println("Failed to " + action + " file: " + path + " - " + e.getMessage());
            }
        }

        private User createUser(String username, String password, String role) {
            return role.equalsIgnoreCase("ADMIN")
                    ? new User.Admin(username, password)
                    : new User.Staff(username, password);
        }

        private void loadUsers() {
            if (!new File(USERS).exists()) {
                users.add(createUser("admin", "admin123", "ADMIN"));
                users.add(createUser("staff", "staff123", "STAFF"));
                saveUsers();
                return;
            }
            for (String line : readLines(USERS)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",", 4);
                if (parts.length < 3) {
                    continue;
                }
                String username = parts[0];
                String password = parts[1];
                String role = parts[2].trim();
                User u = createUser(username, password, role.toUpperCase());
                if (parts.length >= 4) {
                    u.setBlocked(parts[3].trim().equalsIgnoreCase("true"));
                }
                users.add(u);
            }
        }

        private void saveUsers() {
            ArrayList<String> lines = new ArrayList<>();
            for (User u : users) {
                lines.add(u.getUsername() + "," + u.getPassword() + "," + u.getRole() + "," + u.isBlocked());
            }
            writeLines(USERS, lines, false);
        }

        private void loadProducts() {
            for (String line : readLines(PRODUCTS)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",", 5);
                if (parts.length < 4) {
                    continue;
                }
                String name = parts[0];
                String category = parts[1];
                double price = Double.parseDouble(parts[2]);
                int stock = Integer.parseInt(parts[3]);
                String imagePath = parts.length >= 5 ? parts[4] : "";
                products.add(new Product(name, category, price, stock, imagePath));
            }
        }

        private void saveProducts() {
            ArrayList<String> lines = new ArrayList<>();
            for (Product p : products) {
                lines.add(p.getName() + "," + p.getCategory() + "," + p.getPrice() + "," + p.getStock()
                        + "," + p.getImagePath());
            }
            writeLines(PRODUCTS, lines, false);
        }

        private void loadOrders() {
            int maxId = 0;
            for (String line : readLines(ORDERS)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\\|", 8);
                if (parts.length < 5) {
                    continue;
                }
                int id = Integer.parseInt(parts[0]);
                LocalDateTime ts = LocalDateTime.parse(parts[1], TS_FORMAT);
                String cashier = parts[2];
                double payment = Double.parseDouble(parts[3]);
                Order order = new Order(id, cashier);
                order.setTimestamp(ts);
                order.setPayment(payment);
                if (!parts[4].isBlank()) {
                    for (String itemStr : parts[4].split(";")) {
                        String[] ip = itemStr.split(":");
                        if (ip.length < 3) {
                            continue;
                        }
                        String size = ip.length >= 4 ? ip[3] : "";
                        order.addItem(new OrderItem(ip[0], Double.parseDouble(ip[2]), Integer.parseInt(ip[1]), size));
                    }
                }

                if (parts.length >= 8) {
                    order.setDiscount(parts[5], Double.parseDouble(parts[6]));
                    order.setPaymentMethod(parts[7]);
                }
                orders.add(order);
                maxId = Math.max(maxId, id);
            }
            nextOrderId = maxId + 1;
        }

        private void appendOrder(Order order) {
            ArrayList<String> lines = new ArrayList<>();
            StringBuilder itemsStr = new StringBuilder();
            for (OrderItem item : order.getItems()) {
                if (itemsStr.length() > 0) {
                    itemsStr.append(";");
                }
                itemsStr.append(item.getProductName()).append(":")
                        .append(item.getQuantity()).append(":")
                        .append(item.getUnitPrice()).append(":")
                        .append(item.getSize());
            }
            lines.add(order.getOrderId() + "|" + order.getFormattedTimestamp() + "|"
                    + order.getCashierUsername() + "|" + order.getPayment() + "|" + itemsStr
                    + "|" + order.getDiscountLabel() + "|" + order.getDiscountRate()
                    + "|" + order.getPaymentMethod());
            writeLines(ORDERS, lines, true);
        }
    }

}
