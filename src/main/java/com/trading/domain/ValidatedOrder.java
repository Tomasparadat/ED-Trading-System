package com.trading.domain;


public class ValidatedOrder {
    private final String orderId;
    private final ProposedOrder originalOrder;
    private final long riskApprovalTime;

    public ValidatedOrder(String orderId, ProposedOrder originalOrder, long riskApprovalTime) {
        this.orderId = orderId;
        this.originalOrder = originalOrder;
        this.riskApprovalTime = riskApprovalTime;
    }

    public String getOrderId() {
        return orderId;
    }
    public ProposedOrder getOriginalOrder() {
        return originalOrder;
    }

    public long getRiskApprovalTime() {
        return riskApprovalTime;
    }
}
