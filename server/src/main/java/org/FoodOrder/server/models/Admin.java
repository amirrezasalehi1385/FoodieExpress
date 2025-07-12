package org.FoodOrder.server.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.FoodOrder.server.enums.Role;

@Getter
@Setter
@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    public Admin() {
        super();
        setRole(Role.ADMIN);
    }

    public Admin(String fullName, String address, String phoneNumber, String email, String password,
                 String profileImageBase64, String bankName, String accountNumber, BankInfo bankInfo) {
        super(fullName, address, phoneNumber, email, password, profileImageBase64, bankName, accountNumber, bankInfo);
        setRole(Role.ADMIN);
    }

    // توابع تایید رستوران می‌تونن در Service قرار بگیرن
}
