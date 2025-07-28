package org.FoodOrder.server.controllers;

import org.FoodOrder.server.DAO.*;
import org.FoodOrder.server.exceptions.NotFoundException;
import org.FoodOrder.server.models.*;
import org.FoodOrder.server.enums.OrderStatus;
import org.FoodOrder.server.enums.RestaurantOrderStatus;
import org.FoodOrder.server.enums.DeliveryStatus;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.FoodOrder.server.utils.HibernateUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderController {
    private static OrderDao orderDao = new OrderDao();
    private static FoodDao foodItemDao = new FoodDao();
    private static RestaurantDao restaurantDao = new RestaurantDao();
    private static CustomerDao customerDao = new CustomerDao();

    public static Order submitOrder(String customerId, Map<String, Object> orderData) throws Exception {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            // Extract order data
            String deliveryAddress = (String) orderData.get("delivery_address");
            Integer vendorId = (Integer) orderData.get("vendor_id");
            List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");

            // Validate inputs
            if (deliveryAddress == null || vendorId == null || items == null || items.isEmpty()) {
                throw new IllegalArgumentException("Invalid order data");
            }

            // Fetch customer and restaurant
            Customer customer = customerDao.findById(Long.parseLong(customerId));
            Restaurant restaurant = restaurantDao.findById(vendorId.longValue());
            if (customer == null || restaurant == null) {
                throw new IllegalArgumentException("Customer or restaurant not found");
            }

            // Create order
            Order order = new Order(customer, restaurant, deliveryAddress);
            order.setStatus(OrderStatus.SUBMITTED);
            order.setRestaurantOrderStatus(RestaurantOrderStatus.BASE);
            order.setDeliveryStatus(DeliveryStatus.BASE);
            order.setTaxFee(BigDecimal.ZERO); // Assuming no tax for simplicity
            order.setAdditionalFee(BigDecimal.ZERO);
            order.setCourierFee(BigDecimal.ZERO);

            // Validate and add items
            for (Map<String, Object> itemData : items) {
                Long itemId = ((Number) itemData.get("item_id")).longValue();
                Integer quantity = (Integer) itemData.get("quantity");
                if (itemId == null || quantity == null || quantity <= 0) {
                    throw new IllegalArgumentException("Invalid item data");
                }

                FoodItem foodItem = foodItemDao.findById(itemId);
                if (foodItem == null) {
                    throw new IllegalArgumentException("Food item not found: " + itemId);
                }

                // Check stock
                if (foodItem.getSupply() < quantity) {
                    throw new IllegalArgumentException("Insufficient stock for item: " + foodItem.getName());
                }

                // Update stock
                foodItem.setSupply(foodItem.getSupply() - quantity);
                foodItemDao.update(foodItem);

                // Add order item
                OrderItem orderItem = new OrderItem(order, foodItem, quantity, restaurant);
                order.getItems().add(orderItem);
            }

            // Calculate total price
            order.calculateTotalPrice();

            // Save order
            orderDao.save(order);

            transaction.commit();
            return order;
        } catch (Exception e) {
            throw e;
        }
    }
    public static List<Order> getOrders(Long customerId) throws NotFoundException {
        OrderDao orderDao = new OrderDao();
        UserDao userDao = new UserDao();
        User user = userDao.findById(customerId);
        if(user == null) {
            throw  new NotFoundException("user not found", 404);
        }
        List<Order> orders = orderDao.findByCustomerId(customerId);
        return orders;
    }
}