package com.cartesian.userservice.dto;

import com.cartesian.userservice.model.Buyer;
import com.cartesian.userservice.model.Seller;
import com.cartesian.userservice.model.User;
import java.util.UUID;

public class UserDto {

    private UUID id;
    private String username;
    private String email;
    private String sex;
    private String role;

    public UserDto() {
    }

    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setSex(user.getSex());
        if (user instanceof Buyer) {
            dto.setRole("BUYER");
        } else if (user instanceof Seller) {
            dto.setRole("SELLER");
        } else {
            dto.setRole("USER");
        }
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
