package com.cartesian.orderservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "refunds")
@PrimaryKeyJoinColumn(name = "transaction_id")
public class Refund extends Transaction {

    public Refund() {
    }
}
