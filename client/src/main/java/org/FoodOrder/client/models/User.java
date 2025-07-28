package org.FoodOrder.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.FoodOrder.client.enums.Role;

public class User {
    private Long id;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("password")
    private String password;
    @JsonProperty("email")
    private String email;
    @JsonProperty("role")
    private Role role;
    @JsonProperty("address")
    private String address;
    @JsonProperty("profileImageBase64")
    private String profileImageBase64;
    @JsonProperty("bank_name")
    private String bankName;
    @JsonProperty("account_number")
    private String accountNumber;
    @JsonProperty("status")
    private String status;

    public User() {}

    public User(String fullName, String phone,String password, String address) {
        this.fullName = fullName;
        this.phone = phone;
        this.password = password;
        this.address = address;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProfileImageBase64() { return profileImageBase64; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getApprovalStatus() { return status; }
    public void setApprovalStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", address='" + address + '\'' +
                '}';
    }
}