package com.trading.portfolio;

import com.trading.domain.Side;
import com.trading.infra.event.TradingEvent;


public class LedgerRecord {
    private final long sequence;
    private final int symbolId;
    private final double quantity;
    private final double price;
    private final Side side;
    private final long timestamp;
    private final long orderId;

    /**
     * Constructs a LedgerRecord by snapshotting all relevant fields
     * from the TradingEvent at the moment of fill.
     *
     * @param sequence Ring buffer sequence number, used as a unique record ID.
     * @param order The ORDER_FILL TradingEvent to snapshot.
     */
    public LedgerRecord(long sequence, TradingEvent order) {
        this.sequence = sequence;
        this.symbolId = order.getSymbolId();
        this.quantity = order.getQuantity();
        this.price = order.getPrice();
        this.side = order.getSide();
        this.timestamp = order.getTimestamp();
        this.orderId = order.getOrderId();
    }


    /**
     * @return Internal symbol ID of the filled asset.
     */
    public int getSymbolId() { return symbolId; }

    /**
     * @return Quantity filled.
     */
    public double getQuantity() { return quantity; }

    /**
     * @return Price at which the order was filled.
     */
    public double getPrice() { return price; }

    /**
     * @return Side of the fill — BUY or SELL.
     */
    public Side getSide() { return side; }

    /**
     * @return Timestamp of the tick that triggered the fill.
     */
    public long getTimestamp() { return timestamp; }

    /**
     * @return Order ID of the filled order.
     */
    public long getOrderId() { return orderId; }
}