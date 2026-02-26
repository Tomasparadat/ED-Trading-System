package com.trading.infra.event;

import com.trading.domain.EventType;
import com.trading.domain.Side;

public class TradingEvent {
    private long orderId;
    private long timestamp;
    private double price;
    private double quantity;
    private int strategyId;
    private Side side;
    private EventType type;
    private int symbolId;

    /**
     * Fill TradingEvent with basic information to be published in RingBuffer.
     * Missing attributes like strategyId, orderId and timestamp are added as orders are created & executed.
     *
     * @param type MARKET_TICK, ORDER_PROPOSED, ORDER_FILL, ORDER_VALIDATED
     * @param symbolId Stock ticker symbol
     * @param price asset price
     * @param timestamp
     */
    public void set(EventType type, int symbolId, double price, long timestamp) {
        this.symbolId = symbolId;
        this.price = price;
        this.timestamp = timestamp;
        this.type = type;
    }

    public void clear() {
        orderId = 0L;
        strategyId = 0;
        side = null;
        symbolId = 0;
        price = 0.0;
        quantity = 0.0;
        type = null;
        timestamp = 0L;
    }

    public void setType(EventType type) {this.type = type;}

    public void setSymbolId(int symbol) {this.symbolId = symbol;}

    public void setPrice(double price) {this.price = price;}

    public void setQuantity(double quantity) {this.quantity = quantity;}

    public void setSide(Side side) {this.side = side;}

    public void setOrderId(long orderId) {this.orderId = orderId;}

    public void setStrategyId(int strategyId) {this.strategyId = strategyId;}



    public long getOrderId() {return orderId;}

    public int getStrategyId() {return strategyId;}

    public EventType getType() {return type;}

    public int getSymbolId() {return symbolId;}

    public double getPrice() {return price;}

    public double getQuantity() {return quantity;}

    public Side getSide() {return side;}

    public long getTimestamp() {return timestamp;}
}
