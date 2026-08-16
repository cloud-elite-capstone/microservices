package com.retasify.userservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "sellers")
@PrimaryKeyJoinColumn(name = "user_id")
public class Seller extends User {

    public Seller() {
    }

    public Seller(String username, String email, String passwordHash, String sex) {
        super(username, email, passwordHash, sex);
    }
}
