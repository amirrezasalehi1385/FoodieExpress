package org.FoodOrder.server.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.FoodOrder.server.enums.Role;
import org.FoodOrder.server.enums.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.UUID;
@Getter
@Setter
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String publicId = UUID.randomUUID().toString();
    @JsonProperty("full_name")
    private String fullName;
    private String address;
    @Column(unique = true, nullable = false)
    private String phone;

    @Column(unique = true, nullable = true)
    private String email;

    private String password;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String profileImageBase64;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.WAITING;

    @Embedded
    private BankInfo bankInfo;

    @Column(nullable = false)
    private boolean isVerified = false;

//    @CreationTimestamp
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(nullable = false)
//    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", insertable = false, updatable = false)
    private Role role;

    @Column(nullable = false)
    private BigDecimal walletBalance = BigDecimal.ZERO;

    public User() {}

    public User(String fullName, String address, String phoneNumber, String email, String password, String profileImageBase64, BankInfo bankInfo) {

        this.fullName = fullName;
        this.phone = phoneNumber;
        this.email = email;
        this.password = password;
        this.address = address;
        this.profileImageBase64 = profileImageBase64;
        this.bankInfo = bankInfo;
        this.role = role;
    }

    public User(String fullName, String address, String phoneNumber, String password) {
    }


    public BigDecimal getBalance() {
        return walletBalance;
    }

    public void setBalance(BigDecimal balance) {
        this.walletBalance = balance != null ? balance : BigDecimal.ZERO;
    }

    public void addToWallet(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.walletBalance = this.walletBalance.add(amount);
        }
    }
}