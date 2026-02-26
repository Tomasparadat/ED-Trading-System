package com.trading.portfolio;

import com.trading.infra.event.TradingEvent;

/**
 * Records Snapshot of Trading Event to avoid Mutation Bugs through Ring Buffer in the Disruptor.
 *
 */
public class LedgerRecord {
    private final long sequence;
    private final int symbol;
    private final double quantity;
    private final double price;

    public LedgerRecord(long sequence, TradingEvent order) {
        this.sequence = sequence;
        this.symbol = order.getSymbolId();
        this.quantity = order.getQuantity();
        this.price = order.getPrice();
    }
}
