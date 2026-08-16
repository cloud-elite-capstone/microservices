package com.retasify.orderservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.retasify.orderservice.dto.OrderDto;
import com.retasify.orderservice.dto.OrderItemRequestDto;
import com.retasify.orderservice.exception.OrderNotFoundException;
import com.retasify.orderservice.repository.OrderItemsRepository;
import com.retasify.orderservice.repository.OrderRepository;
import com.retasify.orderservice.repository.TransactionRepository;
import com.retasify.orderservice.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderServiceCrudTests {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        orderItemsRepository.deleteAll();
        transactionRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void orderCrudFlow() {
        UUID shopId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();

        OrderItemRequestDto item = new OrderItemRequestDto();
        item.setItemId(UUID.randomUUID());
        item.setQuantity(2);

        OrderDto created = orderService.createOrder(shopId, buyerId, List.of(item));
        assertNotNull(created.getId());
        assertEquals("PENDING", created.getStatus());

        OrderDto fetched = orderService.getOrderById(created.getId());
        assertEquals(shopId, fetched.getShopId());

        OrderItemRequestDto updatedItem = new OrderItemRequestDto();
        updatedItem.setItemId(item.getItemId());
        updatedItem.setQuantity(3);

        OrderDto updated = orderService.updateItemsInOrder(created.getId(), List.of(updatedItem));
        assertEquals(3, updated.getItems().get(0).getQuantity());

        OrderDto statusUpdated = orderService.updateOrderStatus(created.getId(), "PAID");
        assertEquals("PAID", statusUpdated.getStatus());

        orderRepository.deleteById(created.getId());
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(created.getId()));
    }
}
