package com.cartesian.orderservice.service;

import com.cartesian.orderservice.dto.OrderDto;
import com.cartesian.orderservice.dto.OrderItemRequestDto;
import com.cartesian.orderservice.dto.OrderItemsDto;
import com.cartesian.orderservice.dto.PaymentRequestDto;
import com.cartesian.orderservice.dto.RefundRequestDto;
import com.cartesian.orderservice.exception.InvalidOrderUpdateException;
import com.cartesian.orderservice.exception.InvalidPaymentException;
import com.cartesian.orderservice.exception.OrderNotFoundException;
import com.cartesian.orderservice.model.Order;
import com.cartesian.orderservice.model.OrderItems;
import com.cartesian.orderservice.model.Payment;
import com.cartesian.orderservice.model.Refund;
import com.cartesian.orderservice.repository.OrderItemsRepository;
import com.cartesian.orderservice.repository.OrderRepository;
import com.cartesian.orderservice.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final TransactionRepository transactionRepository;

    public OrderService(OrderRepository orderRepository, OrderItemsRepository orderItemsRepository,
                        TransactionRepository transactionRepository) {
        this.orderRepository = orderRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByUserId(UUID userId) {
        List<Order> orders = orderRepository.findByBuyerId(userId);
        return toDtoList(orders);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByShopId(UUID shopId) {
        List<Order> orders = orderRepository.findByShopId(shopId);
        return toDtoList(orders);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
        List<OrderItems> items = orderItemsRepository.findByOrderId(id);
        return OrderDto.fromEntity(order, items);
    }

    @Transactional
    public OrderDto createOrder(UUID shopId, UUID buyerId, List<OrderItemRequestDto> itemsList) {
        if (shopId == null || buyerId == null) {
            throw new IllegalArgumentException("shopId and buyerId are required");
        }
        if (itemsList == null || itemsList.isEmpty()) {
            throw new IllegalArgumentException("At least one order item is required");
        }

        Order order = new Order();
        order.setShopId(shopId);
        order.setBuyerId(buyerId);
        order.setCreatedOn(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setSubtotal(BigDecimal.ZERO);
        order.setShippingFee(BigDecimal.ZERO);
        order.setTotal(BigDecimal.ZERO);
        Order savedOrder = orderRepository.save(order);

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItems> orderItems = new ArrayList<>();
        for (OrderItemRequestDto itemRequest : itemsList) {
            if (itemRequest == null || itemRequest.getItemId() == null) {
                throw new IllegalArgumentException("Each order item must include an itemId");
            }
            if (itemRequest.getQuantity() <= 0) {
                throw new InvalidOrderUpdateException("Quantity must be greater than zero");
            }

            BigDecimal lineAmount = BigDecimal.valueOf(itemRequest.getQuantity()).multiply(BigDecimal.valueOf(10.0));
            subtotal = subtotal.add(lineAmount);

            OrderItems item = new OrderItems();
            item.setOrderId(savedOrder.getId());
            item.setProductId(itemRequest.getItemId());
            item.setQuantity(itemRequest.getQuantity());
            item.setSubtotal(lineAmount);
            orderItems.add(item);
        }

        savedOrder.setSubtotal(subtotal);
        savedOrder.setTotal(subtotal.add(savedOrder.getShippingFee()));
        orderRepository.save(savedOrder);
        orderItemsRepository.saveAll(orderItems);
        return OrderDto.fromEntity(savedOrder, orderItems);
    }

    @Transactional
    public OrderDto updateItemsInOrder(UUID orderId, List<OrderItemRequestDto> itemsList) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (itemsList == null || itemsList.isEmpty()) {
            throw new IllegalArgumentException("Item list is required");
        }

        List<OrderItems> currentItems = orderItemsRepository.findByOrderId(orderId);
        for (OrderItems current : currentItems) {
            boolean found = false;
            for (OrderItemRequestDto itemDto : itemsList) {
                if (itemDto.getItemId().equals(current.getProductId())) {
                    found = true;
                    if (itemDto.getQuantity() <= 0) {
                        orderItemsRepository.delete(current);
                    } else {
                        current.setQuantity(itemDto.getQuantity());
                        current.setSubtotal(BigDecimal.valueOf(itemDto.getQuantity()).multiply(BigDecimal.valueOf(10.0)));
                        orderItemsRepository.save(current);
                    }
                    break;
                }
            }
            if (!found) {
                orderItemsRepository.delete(current);
            }
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItemRequestDto itemDto : itemsList) {
            if (itemDto.getQuantity() <= 0) {
                continue;
            }
            subtotal = subtotal.add(BigDecimal.valueOf(itemDto.getQuantity()).multiply(BigDecimal.valueOf(10.0)));
        }
        order.setSubtotal(subtotal);
        order.setTotal(subtotal.add(order.getShippingFee()));
        orderRepository.save(order);
        return getOrderById(orderId);
    }

    @Transactional
    public OrderDto updateOrderStatus(UUID id, String status) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
        if (status == null || status.isBlank()) {
            throw new InvalidOrderUpdateException("Status is required");
        }
        order.setStatus(status.trim().toUpperCase());
        Order updated = orderRepository.save(order);
        return getOrderById(updated.getId());
    }

    @Transactional
    public Payment createPayment(PaymentRequestDto paymentRequest) {
        if (paymentRequest == null) {
            throw new InvalidPaymentException("Payment request is required");
        }
        if (paymentRequest.getOrderId() == null) {
            throw new InvalidPaymentException("Order id is required");
        }
        if (paymentRequest.getPaymentGateway() == null || paymentRequest.getPaymentGateway().isBlank()) {
            throw new InvalidPaymentException("Payment gateway is required");
        }
        if (paymentRequest.getValue() == null || paymentRequest.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentException("Payment value must be greater than zero");
        }
        Order order = orderRepository.findById(paymentRequest.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(paymentRequest.getOrderId()));
        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setCreatedOn(LocalDateTime.now());
        payment.setPaymentGateway(paymentRequest.getPaymentGateway());
        payment.setValue(paymentRequest.getValue());
        order.setStatus("PAID");
        orderRepository.save(order);
        return transactionRepository.save(payment);
    }

    @Transactional
    public Refund createRefund(RefundRequestDto request) {
        if (request == null) {
            throw new InvalidPaymentException("Refund request is required");
        }
        if (request.getOrderId() == null) {
            throw new InvalidPaymentException("Order id is required");
        }
        if (request.getPaymentGateway() == null || request.getPaymentGateway().isBlank()) {
            throw new InvalidPaymentException("Payment gateway is required");
        }
        orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(request.getOrderId()));
        Refund refund = new Refund();
        refund.setOrderId(request.getOrderId());
        refund.setCreatedOn(LocalDateTime.now());
        refund.setPaymentGateway(request.getPaymentGateway());
        return transactionRepository.save(refund);
    }

    private List<OrderDto> toDtoList(List<Order> orders) {
        List<OrderDto> result = new ArrayList<>();
        for (Order order : orders) {
            result.add(getOrderById(order.getId()));
        }
        return result;
    }
}
