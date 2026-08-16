package com.retasify.orderservice.dto;

import java.util.UUID;

public class RefundRequestDto {

    private UUID orderId;
    private String paymentGateway;

    public RefundRequestDto() {
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }
}
