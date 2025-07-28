package org.FoodOrder.server.controllers;

import org.FoodOrder.server.DAO.UserDao;
import org.FoodOrder.server.enums.Role;
import org.FoodOrder.server.exceptions.InvalidInputException;
import org.FoodOrder.server.exceptions.NotAcceptableException;
import org.FoodOrder.server.exceptions.NotFoundException;
import org.FoodOrder.server.models.BankInfo;
import org.FoodOrder.server.models.Customer;
import org.FoodOrder.server.models.Courier;
import org.FoodOrder.server.models.User;
import org.FoodOrder.server.models.Vendor;
import org.FoodOrder.server.utils.JwtUtil;
import org.FoodOrder.server.utils.PasswordUtil;

public class AuthController {

    private static final UserDao userDao = new UserDao();
    private static final JwtUtil jwtUtil = new JwtUtil();

    private AuthController() {}

    public static User registerUser(String fullName, String phone, String email, String password,
                                    Role role, String address, String profileImageBase64,
                                    BankInfo bankInfo) throws Exception {
        validateRequiredFields(fullName, phone, password, role, address);
        validateUserRole(role);
        User existingUser = userDao.findByPhone(phone);
        if (existingUser != null) throw new NotAcceptableException("Phone number already exists");
        User user;
        switch (role) {
            case COURIER -> user = new Courier();
            case BUYER -> user = new Customer();
            case SELLER -> user = new Vendor();
            default -> throw new InvalidInputException("Invalid role", 400);
        }
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setPassword(password); // Password is already hashed
        user.setRole(role);
        user.setAddress(address);
        user.setProfileImageBase64(profileImageBase64);
        user.setBankInfo(bankInfo);
        user.setVerified(false);
        userDao.save(user);
        return user;
    }

    public static User loginUser(String phone, String password) throws Exception {
        if (phone == null || phone.isEmpty() || password == null || password.isEmpty())
            throw new InvalidInputException("Phone and password are required", 400);
        User user = userDao.findByPhone(phone);
        if (user == null) throw new NotFoundException("User not found", 404);
        if (!PasswordUtil.verifyPassword(password, user.getPassword()))
            throw new NotAcceptableException("Invalid password");
        return user;
    }

    public static User getUserProfile(String userId) throws Exception {
        if (userId == null || userId.isEmpty()) throw new InvalidInputException("User ID is required", 400);
        User user = userDao.findById(Long.valueOf(userId));
        if (user == null) throw new NotFoundException("User not found", 404);
        return user;
    }

    public static void updateUserProfile(String userId, String fullName, String email,
                                         String address, String profileImageBase64,
                                         BankInfo bankInfo) throws Exception {
        User user = getUserProfile(userId);
        if (fullName != null && !fullName.isEmpty()) user.setFullName(fullName);
        if (email != null && !email.isEmpty()) user.setEmail(email);
        if (address != null && !address.isEmpty()) user.setAddress(address);
        if (profileImageBase64 != null) user.setProfileImageBase64(profileImageBase64);
        if (bankInfo != null) user.setBankInfo(bankInfo);
        userDao.update(user);
    }

    public static String generateToken(String userId, String role) {
        return jwtUtil.generateToken(userId, role);
    }

    public static String validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public static String getRoleFromToken(String token) {
        return jwtUtil.getRoleFromToken(token);
    }

    private static void validateRequiredFields(String fullName, String phone, String password,
                                               Role role, String address) throws Exception {
        if (fullName == null || fullName.isEmpty()) throw new InvalidInputException("Full name is required", 400);
        if (phone == null || phone.isEmpty()) throw new InvalidInputException("Phone is required", 400);
        if (password == null || password.isEmpty()) throw new InvalidInputException("Password is required", 400);
        if (role == null) throw new InvalidInputException("Role is required", 400);
        if (address == null || address.isEmpty()) throw new InvalidInputException("Address is required", 400);
    }

    private static void validateUserRole(Role role) throws Exception {
        if (role == null) throw new InvalidInputException("Role is required", 400);
    }
}