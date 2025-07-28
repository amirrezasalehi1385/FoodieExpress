package org.FoodOrder.server.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.FoodOrder.server.enums.CouponType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String couponCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private Long value;

    @Column(nullable = false)
    private Integer minPrice;

    @Column(nullable = false)
    private Integer userCount;
//
//    @Column(nullable = false)
//    private LocalDate startDate;
//
//    @Column(nullable = false)
//    private LocalDate endDate;

    public Coupon(String couponCode, CouponType type, Long value, Integer minPrice, Integer userCount) {
        this.couponCode = couponCode;
        this.type = type;
        this.value = value;
        this.minPrice = minPrice;
        this.userCount = userCount;
    }
}