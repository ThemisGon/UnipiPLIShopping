package com.example.unipiplishopping;

public class StoreProduct {
    private String productId;
    private String title;
    private String description;
    private double price;
    private double latitude;
    private double longitude;
    private String imageUrl;

    // Κενός constructor για χρήση με Gson ή Firebase
    public StoreProduct() {}

    public StoreProduct(String productId, String title, String description, double price, double latitude, double longitude, String imageUrl) {
        this.productId = productId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getImageUrl() { return imageUrl; }
}
