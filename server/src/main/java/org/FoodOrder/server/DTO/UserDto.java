
package org.FoodOrder.server.DTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.FoodOrder.server.enums.Role;

public class UserDto {
    private Long id;
    @JsonProperty("full_name")
    private String fullName;
    private String address;
    private String phone;
    private String email;
    private String profileImageBase64;
    private Role role;
    private String status;

    public UserDto() {}

    public UserDto(Long id, String fullName, String address, String phone, String email,
                   String profileImageBase64, Role role, String status) {
        this.id = id;
        this.fullName = fullName;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.profileImageBase64 = profileImageBase64;
        this.role = role;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getProfileImageBase64() { return profileImageBase64; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getApprovalStatus() { return status; }
    public void setApprovalStatus(String status) { this.status = status; }
}