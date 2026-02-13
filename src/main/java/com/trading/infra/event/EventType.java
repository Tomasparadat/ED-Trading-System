package com.trading.infra.event;

public enum EventType {
    MARKET_TICK,
    ORDER_PROPOSED,
    ORDER_VALIDATED,
    ORDER_FILL,
    SYSTEM_HALT
}
