
package org.FoodOrder.client.models;


public class CartItem {
    private Long id;
    private Cart cart;
    private FoodItem food;
    private int count;
    public CartItem() {
    }

    public CartItem(Cart cart, FoodItem food, int count) {
        this.cart = cart;
        this.food = food;
        this.count = count;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Cart getCart() {
        return cart;
    }
    public void setCart(Cart cart) {
        this.cart = cart;
    }
    public FoodItem getFood() {
        return food;
    }
    public void setFood(FoodItem food) {
        this.food = food;
    }
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }

}