package com.unicesumar.entities;

import java.util.List;
import java.util.UUID;
import com.unicesumar.paymentMethods.PaymentType;

public class Sale extends Entity {
    private final UUID userId;
    private final List<UUID> productIds;
    private final double total;
    private final PaymentType paymentType;
    private final long timestamp;

    public Sale(UUID userId, List<UUID> productIds, double total, PaymentType paymentType) {
        super();
        this.userId = userId;
        this.productIds = productIds;
        this.total = total;
        this.paymentType = paymentType;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getUserId() {
        return userId;
    }

    public List<UUID> getProductIds() {
        return productIds;
    }

    public double getTotal() {
        return total;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public long getTimestamp() {
        return timestamp;
    }
}