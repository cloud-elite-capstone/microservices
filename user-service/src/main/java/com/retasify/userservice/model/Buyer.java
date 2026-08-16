package com.retasify.userservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "buyers")
@PrimaryKeyJoinColumn(name = "user_id")
public class Buyer extends User {

    public Buyer() {
    }

    public Buyer(String username, String email, String passwordHash, String sex) {
        super(username, email, passwordHash, sex);
    }
}
