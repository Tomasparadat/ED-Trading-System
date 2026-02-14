package com.trading.infra.event;

public class TradingEvent {
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

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }
}
