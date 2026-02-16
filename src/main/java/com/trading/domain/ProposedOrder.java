package com.trading.domain;

public record ProposedOrder(
        String orderId,
        String strategyId,
        String symbol,
        Side side,
        long quantity,
        double price,
        long timestamp
) implements Order {}
