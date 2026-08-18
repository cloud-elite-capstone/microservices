package com.cartesian.orderservice.repository;

import com.cartesian.orderservice.model.Transaction;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}
