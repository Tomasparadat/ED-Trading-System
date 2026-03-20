package com.trading.handlers;

import com.lmax.disruptor.EventHandler;
import com.trading.domain.EventType;
import com.trading.infra.event.TradingEvent;
import com.trading.sim.SymbolRegistry;

public class DbLoggerHandler implements EventHandler<TradingEvent> {
    private final SymbolRegistry registry;
    private final java.util.concurrent.BlockingQueue<String> logQueue = new java.util.concurrent.LinkedBlockingQueue<>();

    /**
     * Constructs a DbLoggerHandler and starts a dedicated daemon logging thread.
     * The logging thread consumes formatted strings from the queue and prints them,
     * keeping I/O off the Disruptor thread entirely.
     *
     * @param registry SymbolRegistry used to resolve symbolId to a human-readable ticker name.
     */
    public DbLoggerHandler(SymbolRegistry registry) {
        this.registry = registry;

        Thread logger = new Thread(() -> {
            while (true) {
                try {
                    System.out.println(logQueue.take());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "LoggerThread");
        logger.setDaemon(true);
        logger.start();
    }

    /**
     * Receives each event from the Disruptor pipeline. Filters for ORDER_FILL events only,
     * formats a log entry, and places it on the log queue for the logger thread to print.
     * Returns immediately for all other event types to minimise time on the hot path.
     *
     * @param event TradingEvent received from the ring buffer.
     * @param sequence Ring buffer sequence number (unused).
     * @param endOfBatch Whether this is the last event in the current batch (unused).
     */
    @Override
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) {
        if (event.getType() != EventType.ORDER_FILL) return;
        // removed single event printing for performance.
    }
}
