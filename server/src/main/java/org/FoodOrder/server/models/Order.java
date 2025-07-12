package org.FoodOrder.server.models;

import jakarta.persistence.*;
import lombok.*;
import org.FoodOrder.server.enums.OrderStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @OneToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "deliveryman_id")
    private Courier courier;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "coupon_id")
    private Integer couponId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "raw_price", nullable = false)
    private int rawPrice;

    @Column(name = "tax_fee", nullable = false)
    private int taxFee;

    @Column(name = "additional_fee", nullable = false)
    private int additionalFee;

    @Column(name = "courier_fee", nullable = false)
    private int courierFee;

    @Column(name = "pay_price", nullable = false)
    private int payPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Order(Customer customer, Restaurant restaurant) {
        this.customer = customer;
        this.restaurant = restaurant;
    }
}
