package com.trading.infra.event;

public class TradingEvent {
    private long sequence;
    private String symbol;
    private double price;
    private double quantity;
    private EventType type;
    private long timestamp;

    public void set(){}

    public void clear(){}

    public EventType getType() {
        return type;
    }
}
