package org.FoodOrder.server.models;

import org.FoodOrder.server.enums.FoodCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
public class FoodItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    private int count;

    @ElementCollection
    @CollectionTable(name = "food_hashtags", joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "hashtag")
    private List<String> hashtags = new ArrayList<>();

    @Lob
    private String imageBase64;

    @Enumerated(EnumType.STRING)
    private FoodCategory category;

    @Column(nullable = false)
    private int price;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    public FoodItem() {
    }

    public FoodItem(String name,
                    String description,
                    int count,
                    List<String> hashtags,
                    String imageBase64,
                    int price) {
        this.name = name;
        this.description = description;
        this.count = count;
        this.hashtags = hashtags;
        this.imageBase64 = imageBase64;
        this.price = price;
    }
}
