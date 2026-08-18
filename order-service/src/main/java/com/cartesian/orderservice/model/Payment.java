package com.cartesian.orderservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@PrimaryKeyJoinColumn(name = "transaction_id")
public class Payment extends Transaction {

    @Column(name = "amount", nullable = false)
    private BigDecimal value;

    public Payment() {
    }

    public Payment(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
