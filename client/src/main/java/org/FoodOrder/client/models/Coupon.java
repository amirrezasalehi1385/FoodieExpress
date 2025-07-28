package org.FoodOrder.client.models;

import org.FoodOrder.client.enums.CouponType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Coupon {

    private Integer id;

    private String couponCode;

    private CouponType type;

    private Long value;

    private Integer minPrice;

    private Integer userCount;
//
//
//    private LocalDate startDate;
//
//
//    private LocalDate endDate;

    public Coupon(String couponCode, CouponType type, Long value, Integer minPrice, Integer userCount) {
        this.couponCode = couponCode;
        this.type = type;
        this.value = value;
        this.minPrice = minPrice;
        this.userCount = userCount;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getCouponCode() {
        return couponCode;
    }
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
    public CouponType getType() {
        return type;
    }
    public void setType(CouponType type) {
        this.type = type;
    }
    public Long getValue() {
        return value;
    }
    public void setValue(Long value) {
        this.value = value;
    }
    public Integer getMinPrice() {
        return minPrice;
    }
    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }
    public Integer getUserCount() {
        return userCount;
    }
    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

}