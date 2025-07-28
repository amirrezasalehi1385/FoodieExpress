package org.FoodOrder.client.models;

import java.util.ArrayList;
import java.util.List;

public class FoodItem {
    private long id;
    private String name;
    private String description;
    private int supply;
    private List<String> categories = new ArrayList<>();
    private String imageBase64;
    private int price;
    public FoodItem() {
    }

    public FoodItem(long id, String name, String description, int count,
                    ArrayList<String> categories, String imageBase64,int price, int restaurantId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.supply = count;
        this.imageBase64 = imageBase64;
        this.categories = categories;
        this.price = price;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public int getSupply() {
        return supply;
    }
    public void setSupply(int count) {
        this.supply = count;
    }

    public List<String> getCategories() {
        return categories;
    }
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getImageBase64() {
        return imageBase64;
    }
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }


    public int getPrice() {
        return price;
    }
    public void setPrice(int price) {
        this.price = price;
    }

}
