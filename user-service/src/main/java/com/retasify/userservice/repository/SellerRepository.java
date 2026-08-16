package com.retasify.userservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.retasify.userservice.model.Seller;

public interface SellerRepository extends JpaRepository<Seller, UUID> {
}
