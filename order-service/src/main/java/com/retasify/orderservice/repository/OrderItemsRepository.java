package com.retasify.orderservice.repository;

import com.retasify.orderservice.model.OrderItems;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemsRepository extends JpaRepository<OrderItems, UUID> {

    List<OrderItems> findByOrderId(UUID orderId);
}
