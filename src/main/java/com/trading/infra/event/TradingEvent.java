package com.trading.infra.event;

import com.trading.domain.Side;

public class TradingEvent {
    private long orderId;
    private long timestamp;
    private double price;
    private double quantity;
    private int strategyId;
    private Side side;
    private EventType type;
    private String symbol;

    public void set(long orderId,
                    int strategyId,
                    Side side,
                    String symbol,
                    double price,
                    double quantity,
                    EventType type,
                    long timestamp) {

        this.orderId = orderId;
        this.strategyId = strategyId;
        this.side = side;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.type = type;
        this.timestamp = timestamp;
    }

    public void clear() {
        orderId = 0L;
        strategyId = 0;
        side = null;
        symbol = null;
        price = 0.0;
        quantity = 0.0;
        type = null;
        timestamp = 0L;
    }

    public void setType(EventType type) {this.type = type;}

    public long getOrderId() {return orderId;}

    public int getStrategyId() {return strategyId;}

    public EventType getType() {
        return type;
    }

    public String getSymbol() {return symbol;}

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    public Side getSide() {return side;}

    public long getTimestamp() {return timestamp;}
}
