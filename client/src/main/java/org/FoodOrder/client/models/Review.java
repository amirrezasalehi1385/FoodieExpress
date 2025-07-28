package org.FoodOrder.client.models;
import java.util.List;

public class Review {
    private Long id;
    private Long orderId;
    private Long userId;
    private Integer rating;
    private String comment;
    private Long restaurantId;
    private List<String> imagesBase64;


    public Review() {}

    public Review(Long id, Long orderId, Long userId, Integer rating, String comment, Long restaurantId,
                  List<String> imagesBase64) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.restaurantId = restaurantId;
        this.imagesBase64 = imagesBase64;

    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public List<String> getImagesBase64() { return imagesBase64; }
    public void setImagesBase64(List<String> imagesBase64) { this.imagesBase64 = imagesBase64; }
}
