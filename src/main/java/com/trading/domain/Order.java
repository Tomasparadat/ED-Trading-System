package com.trading.domain;

public sealed interface Order permits ValidatedOrder {
    String symbol();
    String orderId();
    double quantity();
    double price();
    long timestamp();
    Side side();
}