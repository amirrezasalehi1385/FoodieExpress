package org.FoodOrder.server.controllers;

import org.FoodOrder.server.DAO.CourierDao;
import org.FoodOrder.server.DAO.OrderDao;
import org.FoodOrder.server.enums.OrderStatus;
import org.FoodOrder.server.exceptions.NotFoundException;
import org.FoodOrder.server.exceptions.InvalidInputException;
import org.FoodOrder.server.models.Courier;
import org.FoodOrder.server.models.Order;
import java.util.ArrayList;
import java.util.List;

public class DeliveryController {
    private DeliveryController() {}
    public static List<Order> getAvailableDeliveries() throws NotFoundException {
        List<Order> orders = new ArrayList<>();
        orders.addAll(OrderDao.findByStatus(OrderStatus.FINDING_COURIER));
        orders.addAll(OrderDao.findByStatus(OrderStatus.ON_THE_WAY));
        if (orders.isEmpty()) {
            throw new NotFoundException("No available deliveries found", 404);
        }
        return orders;
    }
    public static void updateDeliveryStatus(Long orderId, String newStatus, Long courierId)
            throws NotFoundException, InvalidInputException, IllegalArgumentException {
        Order order = OrderDao.findById(orderId);
        if (order == null) {
            throw new NotFoundException("Order not found", 404);
        }
        CourierDao courierDao = new CourierDao();
        Courier courier = courierDao.findById(courierId);
        if (courier == null) {
            throw new NotFoundException("Courier not found", 404);
        }

        if ("accepted".equalsIgnoreCase(newStatus)) {
            order.setStatus(OrderStatus.ON_THE_WAY);
            order.setCourier(courier);
            courier.getOrdersAssigned().add(order);
            OrderDao.update(order);
            courierDao.update(courier);
        } else if ("delivered".equalsIgnoreCase(newStatus)) {
            order.setStatus(OrderStatus.COMPLETED);
            OrderDao.update(order);
        } else {
            throw new IllegalArgumentException("Invalid status change");
        }
    }
    public static List<Order> getDeliveryHistory(Long courierId, String search, String vendor, String user)
            throws NotFoundException {
        CourierDao courierDao = new CourierDao();
        Courier courier = courierDao.findById(courierId);
        if (courier == null) {
            throw new NotFoundException("Courier not found", 404);
        }
        List<Order> orders = OrderDao.findByCourierIdWithFilters(courierId, search, vendor, user);
        if (orders.isEmpty()) {
            throw new NotFoundException("No delivery history found", 404);
        }
        return orders;
    }
}