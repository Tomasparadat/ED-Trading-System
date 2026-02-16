package com.trading.portfolio;

import com.trading.domain.OrderFill;

/**
 * Records Snapshot of Trading Event to avoid Mutation Bugs through Ring Buffer in the Disruptor.
 *
 */
public class LedgerRecord {
    private final long sequence;
    private final String symbol;
    private final double quantity;
    private final double price;

    public LedgerRecord(long sequence, OrderFill order) {
        this.sequence = sequence;
        this.symbol = order.symbol();
        this.quantity = order.quantity();
        this.price = order.price();
    }
}
