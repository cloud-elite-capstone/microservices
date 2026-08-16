package com.retasify.orderservice.controller;

import com.retasify.orderservice.dto.OrderDto;
import com.retasify.orderservice.dto.OrderItemRequestDto;
import com.retasify.orderservice.dto.PaymentRequestDto;
import com.retasify.orderservice.dto.RefundRequestDto;
import com.retasify.orderservice.model.Payment;
import com.retasify.orderservice.model.Refund;
import com.retasify.orderservice.service.OrderService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getOrdersByUserId(@RequestParam(value = "userId", required = false) UUID userId,
                                                           @RequestParam(value = "shopId", required = false) UUID shopId) {
        if (userId != null) {
            return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
        }
        if (shopId != null) {
            return ResponseEntity.ok(orderService.getOrdersByShopId(shopId));
        }
        throw new IllegalArgumentException("Provide either userId or shopId");
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderRequestBody request) {
        OrderDto created = orderService.createOrder(request.getShopId(), request.getBuyerId(), request.getItemsList());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/payments")
    public ResponseEntity<Payment> createPayment(@RequestBody PaymentRequestDto request) {
        return ResponseEntity.ok(orderService.createPayment(request));
    }

    @PostMapping("/refunds")
    public ResponseEntity<Refund> createRefund(@RequestBody RefundRequestDto request) {
        return ResponseEntity.ok(orderService.createRefund(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> updateItemsInOrder(@PathVariable UUID id, @RequestBody List<OrderItemRequestDto> itemsList) {
        return ResponseEntity.ok(orderService.updateItemsInOrder(id, itemsList));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable UUID id, @RequestParam("status") String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}

class OrderRequestBody {
    private UUID shopId;
    private UUID buyerId;
    private List<OrderItemRequestDto> itemsList;

    public UUID getShopId() {
        return shopId;
    }

    public void setShopId(UUID shopId) {
        this.shopId = shopId;
    }

    public UUID getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(UUID buyerId) {
        this.buyerId = buyerId;
    }

    public List<OrderItemRequestDto> getItemsList() {
        return itemsList;
    }

    public void setItemsList(List<OrderItemRequestDto> itemsList) {
        this.itemsList = itemsList;
    }
}
