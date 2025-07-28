package org.FoodOrder.server.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(nullable = false)
    private Integer rating;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "review_images", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "image_base64", columnDefinition = "TEXT")
    private List<String> imagesBase64 = new ArrayList<>();
//
//    @CreationTimestamp
//    private LocalDateTime createdAt;
//    @UpdateTimestamp
//    private LocalDateTime updatedAt;
    public Review(Order order, User author, Integer rating, String comment) {
        this.order = order;
        this.user = author;
        this.rating = rating;
        this.comment = comment;
    }
}