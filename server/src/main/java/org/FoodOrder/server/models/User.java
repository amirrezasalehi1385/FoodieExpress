package org.FoodOrder.server.models;

import org.FoodOrder.server.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(unique = true, nullable = false, updatable = false)
    private String publicId = UUID.randomUUID().toString();
    private String fullName;
    private String address;
    @Column(unique = true, nullable = false)
    private String phoneNumber;
    @Column(unique = true)
    private String email;
    private String password;
    private String profileImageBase64;
    @Embedded
    private BankInfo bankInfo;
    @Column(nullable = false)
    private boolean isVerified = false;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, insertable = false, updatable = false)
    private Role role;

    public User() {}

    public User(String fullName, String address, String phoneNumber, String email, String password, String profileImageBase64, BankInfo bankInfo) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.address = address;
        this.profileImageBase64 = profileImageBase64;
        this.bankInfo = bankInfo;
    }
}