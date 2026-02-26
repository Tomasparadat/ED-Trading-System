package com.trading.domain;

public enum EventType {
    MARKET_TICK,
    ORDER_PROPOSED,
    ORDER_VALIDATED,
    ORDER_FILL,
    NEW_ORDER, SYSTEM_HALT
}
