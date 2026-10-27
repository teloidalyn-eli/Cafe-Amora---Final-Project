package Main;

public class Product {

    private String name;
    private String category;
    private double price;
    private int stock;
    private String imagePath; 

    public Product(String name, String category, double price, int stock) {
        this(name, category, price, stock, "");
    }

    public Product(String name, String category, double price, int stock, String imagePath) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.imagePath = imagePath == null ? "" : imagePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
     public boolean isDrink() {
        return "Iced/Hot Coffee".equalsIgnoreCase(category) || "Non-Coffee".equalsIgnoreCase(category);
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath == null ? "" : imagePath;
    }

    public boolean hasImage() {
        return imagePath != null && !imagePath.isBlank();
    }

    public void reduceStock(int qty) {
        this.stock = Math.max(0, this.stock - qty);
    }

    @Override
    public String toString() {
        return name;
    }
}