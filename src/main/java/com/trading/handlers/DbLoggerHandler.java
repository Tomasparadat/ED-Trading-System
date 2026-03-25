package com.trading.handlers;

import com.lmax.disruptor.EventHandler;
import com.trading.domain.EventType;
import com.trading.infra.event.TradingEvent;
import com.trading.sim.SymbolRegistry;

public class DbLoggerHandler implements EventHandler<TradingEvent> {
    private final SymbolRegistry registry;

    /**
     * Constructs a DbLoggerHandler and starts a dedicated daemon logging thread.
     * The logging thread consumes formatted strings from the queue and prints them,
     * keeping I/O off the Disruptor thread entirely.
     *
     * @param registry SymbolRegistry used to resolve symbolId to a human-readable ticker name.
     */
    public DbLoggerHandler(SymbolRegistry registry) {
        this.registry = registry;
    }

    /**
     * Receives each event from the Disruptor pipeline. Filters for ORDER_FILL events only.
     *
     * NOT IN USE -> BlockingQueue hinders Throughput - Performance even while running on own Thread.
     *
     * @param event TradingEvent received from the ring buffer.
     * @param sequence Ring buffer sequence number (unused).
     * @param endOfBatch Whether this is the last event in the current batch (unused).
     */
    @Override
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) {
        if (event.getType() != EventType.ORDER_FILL) return;
    }
}
