package com.cartesian.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentRequestDto {

    private UUID orderId;
    private String paymentGateway;
    private BigDecimal value;

    public PaymentRequestDto() {
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

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
