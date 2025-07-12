package org.FoodOrder.server.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String title;

    @ManyToMany(fetch = FetchType.LAZY /*یا EAGER با دقت*/, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "menu_items",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private List<FoodItem> items = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    public Menu(Restaurant restaurant, String title) {
        this.restaurant = restaurant;
        this.title = title;
    }

    public void addItem(FoodItem item) {
        items.add(item);
        // اگر رابطه دوطرفه هست، این خط را اضافه کن:
        // item.getMenus().add(this);
    }
}
