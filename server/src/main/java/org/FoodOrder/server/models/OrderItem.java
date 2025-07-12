package org.FoodOrder.server.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "food_id")
    private FoodItem food;

    @Column(nullable = false)
    private int count;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    public OrderItem(Order order, FoodItem food, int count, Restaurant restaurant) {
        this.order = order;
        this.food = food;
        this.count = count;
        this.restaurant = restaurant;
    }
}
