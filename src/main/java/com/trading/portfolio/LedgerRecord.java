package com.trading.portfolio;

import com.trading.infra.event.TradingEvent;

/**
 * Records Snapshot of Trading Event to avoid Mutation Bugs through Ring Buffer in the Disruptor.
 *
 */
public class LedgerRecord {
    private final long sequence;
    private final String symbol;
    private final double quantity;
    private final double price;

    public LedgerRecord(long sequence, TradingEvent event) {
        this.sequence = sequence;
        this.symbol = event.getSymbol();
        this.quantity = event.getQuantity();
        this.price = event.getPrice();
    }
}
