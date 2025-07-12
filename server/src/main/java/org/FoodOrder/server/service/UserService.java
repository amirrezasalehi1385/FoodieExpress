package org.FoodOrder.server.service;
import dao.VendorDao;
import jakarta.validation.constraints.NotNull;
import jdk.incubator.vector.VectorMask;
import org.FoodOrder.server.DAO.*;
import org.FoodOrder.server.enums.Role;
import org.FoodOrder.server.models.*;


import java.util.*;



public class UserService {
    private static UserService instance;

    private final CustomerDao customerDao = new CustomerDao();
    private final dao.VendorDao ownerDao = new VendorDao();
    private final CourierDao deliverymanDao = new CourierDao();

    private UserService() {
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }


    public Optional<User> addUser(@NotNull User user) {
        try {
            switch (user) {
                case Customer customer -> customerDao.save(customer);
                case Vendor vendor -> ownerDao.save(vendor);
                case Courier courier -> deliverymanDao.save(courier);
                default -> throw new IllegalArgumentException("Unknown user type, cannot add: " + user.getClass().getName());
            }
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) return Optional.empty();
        User user = customerDao.findByPublicId(publicId);
        if (user != null) return Optional.of(user);
        user = ownerDao.findByPublicId(publicId);
        if (user != null) return Optional.of(user);
        user = deliverymanDao.findByPublicId(publicId);
        return Optional.ofNullable(user);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        User user = customerDao.findByEmail(email);
        if (user != null) return Optional.of(user);
        user = ownerDao.findByEmail(email);
        if (user != null) return Optional.of(user);
        user = deliverymanDao.findByEmail(email);
        return Optional.ofNullable(user);
    }


    public Optional<User> findByPhone(String phone) {
        if (phone == null || phone.isBlank()) return Optional.empty();
        User user = customerDao.findByPhone(phone);
        if (user != null) return Optional.of(user);
        user = ownerDao.findByPhone(phone);
        if (user != null) return Optional.of(user);
        user = deliverymanDao.findByPhone(phone);
        return Optional.ofNullable(user);
    }


    public boolean resetPassword(@NotNull User user, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be blank.");
        }
        user.setPassword(newPassword);

        switch (user) {
            case Customer customer -> customerDao.update(customer);
            case Vendor vendor -> ownerDao.update(vendor);
            case Courier courier -> deliverymanDao.update(courier);
            default -> throw new IllegalArgumentException("Unknown user type for password reset: " + user.getClass().getName());
        }
        return true;
    }

    public boolean removeUser(@NotNull User user) {
        switch (user) {
            case Customer customer -> customerDao.delete(customer);
            case Vendor vendor -> ownerDao.delete(vendor);
            case Courier courier-> deliverymanDao.delete(courier);
            default -> throw new IllegalArgumentException("Unknown user type for removal: " + user.getClass().getName());
        }
        return true;
    }


    public boolean updateBasicProfile(@NotNull User user, String fullName, String phoneNumber, String email, String profileImageBase64,String bankName, String accountNumber) {
        boolean changed = false;
        if (fullName != null && !fullName.isBlank() && !fullName.equals(user.getFullName())) {
            user.setFullName(fullName);
            changed = true;
        }
        if (phoneNumber != null && !phoneNumber.isBlank() && !phoneNumber.equals(user.getPhoneNumber())) {
            Optional<User> existingUserWithNewPhone = findByPhone(phoneNumber);
            if (existingUserWithNewPhone.isPresent() && !existingUserWithNewPhone.get().getPublicId().equals(user.getPublicId())) {
                throw new IllegalArgumentException("Phone number '" + phoneNumber + "' is already in use by another account.");
            }
            user.setPhoneNumber(phoneNumber);
            changed = true;
        }
        if (email != null && !email.isBlank() && !email.equals(user.getEmail())) {
            Optional<User> existingUserWithNewEmail = findByEmail(email);
            if (existingUserWithNewEmail.isPresent() && !existingUserWithNewEmail.get().getPublicId().equals(user.getPublicId())) {
                throw new IllegalArgumentException("Email '" + email + "' is already in use by another account.");
            }
            user.setEmail(email);
            changed = true;
        }

        if (bankName != null && !bankName.isBlank() && !bankName.equals(user.getBankInfo().getBank_name())) {
            user.getBankInfo().setBank_name(bankName);
            changed = true;
        }

        if (user.getBankInfo().getAccount_number() != null && !user.getBankInfo().getAccount_number().isBlank() && !accountNumber.equals(user.getBankInfo().getAccount_number())) {
            user.getBankInfo().setAccount_number(accountNumber);
            changed = true;
        }
        if (profileImageBase64 != null && !profileImageBase64.isBlank()) {
            user.setProfileImageBase64(profileImageBase64);
            changed = true;
        }

        if (changed) {
            switch (user) {
                case Customer customer -> customerDao.update(customer);
                case Vendor vendor -> ownerDao.update(vendor);
                case Courier courier -> deliverymanDao.update(courier);
                default -> throw new IllegalArgumentException("Unknown user type for profile update: " + user.getClass().getName());
            }
        }
        return changed;
    }

    public boolean updateCustomerDetails(@NotNull Customer customer, String newAddress) {
        boolean changed = false;
        if (newAddress != null && !newAddress.isBlank() && !newAddress.equals(customer.getAddress())) {
            customer.setAddress(newAddress);
            changed = true;
        }
        if (changed) customerDao.update(customer);
        return changed;
    }

    public boolean updateOwnerDetails(@NotNull Vendor vendor, String newAddress) {
        boolean changed = false;
        if (newAddress != null && !newAddress.isBlank() && !newAddress.equals(vendor.getAddress())) {
            vendor.setAddress(newAddress);
            changed = true;
        }
        if (changed) ownerDao.update(vendor);
        return changed;
    }

    public List<User> getAllUsers() {
        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(customerDao.getAll());
        allUsers.addAll(ownerDao.getAll());
        allUsers.addAll(deliverymanDao.getAll());
        return allUsers;
    }


    public static class UserFactory {
        public static User createUser(
                @NotNull Role role,
                String fullName,
                String phoneNumber,
                String email,
                String password,
                String address,
                String profileImageBase64,
                BankInfo bankInfo
        ) {
            if (fullName == null || fullName.isBlank()) {
                throw new IllegalArgumentException("Full name is required for user creation.");
            }
            if (phoneNumber == null || phoneNumber.isBlank()) {
                throw new IllegalArgumentException("Phone number is required for user creation.");
            }
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Password is required for user creation.");
            }
            if (address == null || address.isBlank()) {
                throw new IllegalArgumentException("Address is required for user creation.");
            }
            if (bankInfo == null) {
                throw new IllegalArgumentException("Bank info is required for user creation.");
            }
            return switch (role) {
                case CUSTOMER -> new Customer(fullName, address, phoneNumber, email, password, profileImageBase64, bankInfo);
                case VENDOR -> new Vendor(fullName, address, phoneNumber, email, password, profileImageBase64, bankInfo);
                case COURIER -> new Courier(fullName, address, phoneNumber, email, password, profileImageBase64, bankInfo);
                default -> throw new IllegalArgumentException("Unsupported role: " + role);
            };
        }
    }
}
//    public Customer(String fullName, String address, String phoneNumber, String email, String password, String profileImageBase64, String bankName, String accountNumber, BankInfo bankInfo) {
//        super(fullName, address, phoneNumber, email, password, profileImageBase64, bankName, accountNumber, bankInfo);
//        setRole(Role.CUSTOMER);
//    }