package org.FoodOrder.server.models;

import jakarta.persistence.*;
import lombok.*;
import org.FoodOrder.server.enums.OrderStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.FoodOrder.server.enums.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Review review;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @ManyToOne
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "raw_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal rawPrice = BigDecimal.ZERO;

    @Column(name = "tax_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxFee = BigDecimal.ZERO;

    @Column(name = "additional_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal additionalFee = BigDecimal.ZERO;

    @Column(name = "courier_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal courierFee = BigDecimal.ZERO;

    @Column(name = "pay_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal payPrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "restaurant_order_status", nullable = false)
    private RestaurantOrderStatus restaurantOrderStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus deliveryStatus;


    public Order(Customer customer, Restaurant restaurant, String deliveryAddress) {
        this.customer = customer;
        this.restaurant = restaurant;
        this.deliveryAddress = deliveryAddress;
    }

    public void updateStatus() {
        switch (this.restaurantOrderStatus) {
            case BASE:
                this.status = OrderStatus.SUBMITTED;
                this.deliveryStatus = DeliveryStatus.BASE;
                break;

            case ACCEPTED:
                this.status = OrderStatus.WAITING_VENDOR;
                this.deliveryStatus = DeliveryStatus.BASE;
                break;

            case REJECTED:
                this.status = OrderStatus.CANCELLED;
                this.deliveryStatus = DeliveryStatus.BASE;
                break;

            case SERVED:
                switch (this.deliveryStatus) {
                    case BASE:
                        this.status = OrderStatus.FINDING_COURIER;
                        break;

                    case DELIVERED:
                        this.status = OrderStatus.COMPLETED;
                        break;

                    default:
                        this.status = OrderStatus.ON_THE_WAY;
                }
        }
    }

    public void calculateTotalPrice() {
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem item : items) {
            BigDecimal price = new BigDecimal(String.valueOf(item.getFood().getPrice()));
            BigDecimal quantity = new BigDecimal(item.getCount());
            sum = sum.add(price.multiply(quantity));
        }
        this.rawPrice = sum;
        BigDecimal totalPrice = this.rawPrice.add(this.taxFee).add(this.additionalFee);
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (this.coupon != null) {
            if (this.coupon.getType() == CouponType.FIXED) {
                discountAmount = new BigDecimal(String.valueOf(this.coupon.getValue()));
            } else if (this.coupon.getType() == CouponType.PERCENT) {
                discountAmount = totalPrice.multiply(new BigDecimal(this.coupon.getValue()).divide(new BigDecimal("100.0"), 2, RoundingMode.HALF_UP));
            }
        }
        this.payPrice = totalPrice.subtract(discountAmount);
    }

    public BigDecimal getTotalPrice() {
        return payPrice;
    }

}