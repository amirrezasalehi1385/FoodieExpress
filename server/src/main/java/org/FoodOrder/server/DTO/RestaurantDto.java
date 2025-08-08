package org.FoodOrder.server.DTO;

public class RestaurantDto {
    private int id;
    private String name;
    private double averageRating;
    private String logoBase64;
    private String address;
    private String phone;
    private boolean isFavorite;

    public RestaurantDto() {}

    public RestaurantDto(int id, String name, double averageRating, String logoBase64, String address, String phone, boolean isFavorite) {
        this.id = id;
        this.name = name;
        this.averageRating = averageRating;
        this.logoBase64 = logoBase64;
        this.address = address;
        this.phone = phone;
        this.isFavorite = isFavorite;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public String getLogoBase64() { return logoBase64; }
    public void setLogoBase64(String logoBase64) { this.logoBase64 = logoBase64; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}