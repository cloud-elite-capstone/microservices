package com.cartesian.orderservice.repository;

import com.cartesian.orderservice.model.OrderItems;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemsRepository extends JpaRepository<OrderItems, UUID> {

    List<OrderItems> findByOrderId(UUID orderId);
}
