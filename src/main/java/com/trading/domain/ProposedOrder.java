package com.trading.domain;

public class ProposedOrder {
    private final String strategyId;
    private final String symbol;
    private final String side;
    private final double quantity;
    private final double price;
    private final long timestamp;


    public ProposedOrder(String strategyId, String symbol, String side,
                         double quantity, double price, long timestamp) {
        this.strategyId = strategyId;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
    }

    public String getStrategyId() { return strategyId; }
    public String getSymbol() { return symbol; }
    public String getSide() { return side; }
    public double getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public long getTimestamp() { return timestamp; }


}