package com.trading.domain;

public class OrderFill {
    private final String orderId;
    private final double filledQty;
    private final double filledPrice;

    /**
     * OrderFill Constructor.
     *
     * @param orderId
     * @param filledQty
     * @param filledPrice
     */
    public OrderFill(String orderId,  double filledQty, double filledPrice) {
        this.orderId = orderId;
        this.filledQty = filledQty;
        this.filledPrice = filledPrice;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getFilledQty() {
        return filledQty;
    }

    public double getFilledPrice() {
        return filledPrice;
    }

}