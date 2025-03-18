package com.example.unipiplishopping;

public class Product {
    private String id;
    private String title;
    private String imageUrl;


    // Κενός constructor (απαραίτητος για το Gson)
    public Product() {}

    public Product(String id, String title, String imageUrl) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;

    }

    // Getters και Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


}
