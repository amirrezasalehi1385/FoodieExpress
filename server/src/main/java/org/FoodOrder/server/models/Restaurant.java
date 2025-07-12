package org.FoodOrder.server.models;

import org.FoodOrder.server.enums.RestaurantCategory;
import org.FoodOrder.server.enums.ApprovalStatus;   // Changed from RestaurantStatus
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurant")
@Getter
@Setter
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", unique = true)
    private Vendor vendor;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private RestaurantCategory category;
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.WAITING;
    private String logoBase64;
    private Double averageRating = 0.0;
    private Integer taxFee;
    private Integer additionalFee;

    public Restaurant() {}

    public Restaurant(String name, String address, String phoneNumber, Vendor vendor, String logoBase64) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.vendor = vendor;
        this.logoBase64 = logoBase64;
    }

    public Menu getMenu(String title) {
        if (this.menus == null || title == null) return null;
        return this.menus.stream().filter(menu -> title.equals(menu.getTitle())).findFirst().orElse(null);
    }

    public void addMenu(Menu menu) {
        this.menus.add(menu);
        menu.setRestaurant(this);
    }

    public void removeMenu(String menuTitle) {
        this.menus.removeIf(menu -> menuTitle.equals(menu.getTitle()));
    }
}
