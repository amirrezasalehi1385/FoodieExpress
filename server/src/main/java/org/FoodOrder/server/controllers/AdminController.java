package org.FoodOrder.server.controllers;

import org.FoodOrder.server.DAO.OrderDao;
import org.FoodOrder.server.DAO.UserDao;
import org.FoodOrder.server.DTO.OrderDto;
import org.FoodOrder.server.DTO.UserDto;
import org.FoodOrder.server.enums.ApprovalStatus;
import org.FoodOrder.server.enums.Role;
import org.FoodOrder.server.models.Order;
import org.FoodOrder.server.models.User;
import org.FoodOrder.server.service.OrderService;
import org.FoodOrder.server.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminController {

    private AdminController() {}

    public static List<UserDto> getAllNonAdminUsers() throws Exception {
        UserDao userDao = new UserDao();
        List<User> users = userDao.getAll();
        if (users == null || users.isEmpty()) {
            throw new Exception("No users found");
        }
        List<User> filteredUsers = users.stream()
                .filter(user -> user.getRole() != Role.ADMIN)
                .collect(Collectors.toList());
        return UserService.convertToDtoList(filteredUsers);
    }

    public static void updateUserStatus(Long userId, String status) throws Exception {
        UserDao userDao = new UserDao();
        User user = userDao.findById(userId);
        if (user == null) {
            throw new Exception("User not found");
        }

        if (!"approved".equals(status) && !"rejected".equals(status)) {
            throw new IllegalArgumentException("Invalid status. Must be 'approved' or 'rejected'");
        }

        ApprovalStatus approvalStatus = status.equals("approved") ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED;
        user.setApprovalStatus(approvalStatus);
        userDao.update(user);
    }

    public static List<OrderDto> getAllOrdersWithFilters(Map<String, String> filters) throws Exception {
        OrderDao orderDao = new OrderDao();
        List<Order> orders = orderDao.findHistoryForAdmin(
                filters.get("search"),
                filters.get("vendor"),
                filters.get("courier"),
                filters.get("customer"),
                filters.get("status")
        );
        if (orders == null || orders.isEmpty()) {
            throw new Exception("No orders found");
        }
        return OrderService.convertToDtoList(orders);
    }
}
