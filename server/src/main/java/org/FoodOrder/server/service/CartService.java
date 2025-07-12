package org.FoodOrder.server.service;
import org.FoodOrder.server.DAO.CartDao;
import org.FoodOrder.server.DAO.FoodDao;
import org.FoodOrder.server.models.*;

public class CartService {

    private final CartDao cartDao;
    private final FoodDao foodItemDao;

    public CartService(CartDao cartDao, FoodDao foodItemDao) {
        this.cartDao = cartDao;
        this.foodItemDao = foodItemDao;
    }

    public void addToCart(Long customerId, Long foodId, int count) {
        Cart cart = cartDao.findByCustomerId(customerId);
        FoodItem food = foodItemDao.findById(foodId);

        if (cart == null) {
            cart = new Cart();
            cart.setCustomer(new Customer());
            cartDao.save(cart);
        }

        cart.addItem(food, count);
        cartDao.save(cart);
    }

    public void removeFromCart(Long customerId, Long foodId) {
        Cart cart = cartDao.findByCustomerId(customerId);
        if (cart != null) {
            FoodItem food = foodItemDao.findById(foodId);
            cart.removeItem(food);
            cartDao.save(cart);
        }
    }

    public void clearCart(Long customerId) {
        Cart cart = cartDao.findByCustomerId(customerId);
        if (cart != null) {
            cart.clear();
            cartDao.save(cart);
        }
    }

    public Cart getCart(Long customerId) {
        return cartDao.findByCustomerId(customerId);
    }
}
