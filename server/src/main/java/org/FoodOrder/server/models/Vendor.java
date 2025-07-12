package org.FoodOrder.server.models;

import org.FoodOrder.server.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("VENDOR")
public class Vendor extends User {
    @OneToOne(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Restaurant restaurant;
    public Vendor() {
        super();
        setRole(Role.VENDOR);
    }
    public Vendor(String fullName, String address, String phoneNumber, String email, String password, String profileImageBase64, BankInfo bankInfo) {
        super(fullName, address, phoneNumber, email, password, profileImageBase64, bankInfo);
        setRole(Role.VENDOR);
    }
}