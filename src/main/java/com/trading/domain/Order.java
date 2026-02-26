package com.trading.domain;

public interface Order {
    String symbol();
    String orderId();
    double quantity();
    double price();
    long timestamp();
    Side side();
}