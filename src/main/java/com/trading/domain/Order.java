package com.trading.domain;

public sealed interface Order permits ProposedOrder, ValidatedOrder {
    String symbol();
    String orderId();
    long quantity();
    double price();
    long timestamp();
    Side side();
}