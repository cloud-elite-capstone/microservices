package com.cartesian.userservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cartesian.userservice.model.Buyer;

public interface BuyerRepository extends JpaRepository<Buyer, UUID> {
}
