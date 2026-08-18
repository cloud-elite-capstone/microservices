package com.cartesian.userservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cartesian.userservice.model.Seller;

public interface SellerRepository extends JpaRepository<Seller, UUID> {
}
