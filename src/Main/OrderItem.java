package Main;

public class OrderItem{

    private String productName;
    private double unitPrice;
    private int quantity;
    private String size;

    public OrderItem(String productName, double unitPrice, int quantity) {
        this(productName, unitPrice, quantity, "");
    }

    public OrderItem(String productName, double unitPrice, int quantity, String size) {
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.size = size == null ? "" : size;
    }

    public String getProductName() {
        return productName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSize() {
        return size;
    }

    public boolean hasSize() {
        return size != null && !size.isBlank();
    }
    
    public String getDisplayName() {
        return hasSize() ? productName + " (" + size + ")" : productName;
    }

    public double getSubtotal() {
        return unitPrice * quantity;
    }

    @Override
     public String toString() {
        return getDisplayName() + " x" + quantity + "  P" + Math.round(getSubtotal() * 100) / 100.0;
    }
}