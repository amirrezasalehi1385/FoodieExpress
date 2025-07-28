package org.FoodOrder.server.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.FoodOrder.server.DAO.FoodDao;
import org.FoodOrder.server.exceptions.NotFoundException;

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

    @ManyToMany(fetch = FetchType.LAZY , cascade = {CascadeType.PERSIST, CascadeType.MERGE})
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
    }
    public void removeItem(int itemId) throws NotFoundException {
        Iterator<FoodItem> iterator = items.iterator();
        FoodItem item = null;
        while (iterator.hasNext()) {
            item = iterator.next();
            if (item.getId() == itemId) {
                iterator.remove();
                return;
            }
            item=null;
        }
        if (item==null){throw new NotFoundException("item is not  found in menu", 404);}
    }
}
