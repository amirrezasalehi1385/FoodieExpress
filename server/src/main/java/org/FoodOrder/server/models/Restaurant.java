package org.FoodOrder.server.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.FoodOrder.server.enums.RestaurantCategory;
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
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id")
    @JsonIgnore
    private Vendor seller;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();


    @JsonIgnore
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>();

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private RestaurantCategory category;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String logoBase64;

    private Double averageRating = 0.0;
    private Integer taxFee;
    private Integer additionalFee;

    @Transient
    private boolean isFavorite;

    public Restaurant() {}

    public Restaurant(String name, String address, String phone, Vendor vendor, String logoBase64) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.seller = vendor;
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

    public Double getAverageRating() {
        if (reviews == null || reviews.isEmpty()) return 0d;
        long sum = 0;
        for (Review review : reviews) {
            sum += review.getRating();
        }
        return (double) sum / reviews.size();
    }
}