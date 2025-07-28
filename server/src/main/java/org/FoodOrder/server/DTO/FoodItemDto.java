package org.FoodOrder.server.DTO;
import lombok.Getter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Getter
public class FoodItemDto {
    private long id;
    private String name;
    private String description;
    private int supply;
    private List<String> categories;
    private String imageBase64;
    private int price;

    public FoodItemDto() {}

    public FoodItemDto(long id, String name, String description, int supply,
                       List<String> categories, String imageBase64,
                       int price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.supply = supply;
        this.categories = categories;
        this.imageBase64 = imageBase64;
        this.price = price;
    }
}
