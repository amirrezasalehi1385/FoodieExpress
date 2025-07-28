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

    private int supply;

    @ElementCollection
    @CollectionTable(name = "food_categories", joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "categories")
    private List<String> categories = new ArrayList<>();

//    @ManyToMany(mappedBy = "items", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
//    private List<Menu> menus = new ArrayList<>();

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String imageBase64;

    @Column(nullable = false)
    private int price;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    public FoodItem() {
    }

    public FoodItem(String name,
                    String description,
                    int supply,
                    List<String> categories,
                    String imageBase64,
                    int price) {
        this.name = name;
        this.description = description;
        this.supply = supply;
        this.categories = categories;
        this.imageBase64 = imageBase64;
        this.price = price;
    }
}
