package Main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private static final DateTimeFormatter TS_FORMAT
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int orderId;
    private LocalDateTime timestamp;
    private String cashierUsername;
    private ArrayList<OrderItem> items = new ArrayList<>();
    private double payment;
    private String paymentMethod;
    private String discountLabel = "None";
    private double discountRate = 0.0;

    public Order(int orderId, String cashierUsername) {
        this.orderId = orderId;
        this.cashierUsername = cashierUsername;
        this.timestamp = LocalDateTime.now();
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public double getSubtotal() {
        double sum = 0;
        for (OrderItem item : items) {
            sum += item.getSubtotal();
        }
        return sum;
    }

    public double getDiscountAmount() {
        return getSubtotal() * discountRate;
    }

    public double getTotal() {
        return getSubtotal() - getDiscountAmount();
    }

    public double getChange() {
        return payment - getTotal();
    }

    public void setDiscount(String label, double rate) {
        this.discountLabel = (label == null || label.isBlank()) ? "None" : label;
        this.discountRate = rate;
    }

    public String getDiscountLabel() {
        return discountLabel;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public boolean hasDiscount() {
        return discountRate > 0.0;
    }

    public void setPayment(double payment) {
        this.payment = payment;
    }

    public double getPayment() {
        return payment;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getCashierUsername() {
        return cashierUsername;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(TS_FORMAT);
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}