package com.trading.domain;


public record ValidatedOrder(
        String orderId,
        String strategyId,
        String symbol,
        Side side,
        long quantity,
        double price,
        long timestamp
) implements Order {}
