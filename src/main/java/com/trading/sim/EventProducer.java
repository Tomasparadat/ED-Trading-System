package com.trading.sim;

import com.lmax.disruptor.RingBuffer;
import com.trading.domain.EventType;
import com.trading.domain.Side;
import com.trading.infra.event.TradingEvent;

public class EventProducer {
    private RingBuffer<TradingEvent> ringBuffer;
    private SymbolRegistry symbolRegistry;


    public EventProducer(RingBuffer<TradingEvent> ringBuffer, SymbolRegistry symbolRegistry) {
        this.ringBuffer = ringBuffer;
        this.symbolRegistry = symbolRegistry;
    }


    /**
     * Claims next slot in the RingBuffer, clears it, resets EventType and sets new Ticker, price and TimeStamp.
     *
     * @param symbolId new Ticker given to the TradingEvent.
     * @param price new Price passed to TradingEvent.
     */
    public void publish(int symbolId, double price) {
        long sequence = ringBuffer.next();
        try {
            TradingEvent event = ringBuffer.get(sequence);
            event.set(EventType.MARKET_TICK, symbolId, price, System.currentTimeMillis());
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    /**
     * Publishes an ORDER_PROPOSED event into the ring buffer.
     * Called by StrategyHandler when the strategy engine generates a BUY or SELL signal.
     * In the multi-buffer model this writes to the OrderBuffer, not the TickBuffer, eliminating any risk of deadlock.
     *
     * @param orderId
     * @param strategyId
     * @param symbolId
     * @param price
     * @param quantity
     * @param side
     * @param timestamp
     */
    public void publishProposed(long orderId, int strategyId, int symbolId, double price, double quantity, Side side, long timestamp) {
        long sequence = ringBuffer.next();

        try {
            TradingEvent event = ringBuffer.get(sequence);
            event.setProposed(orderId, strategyId, symbolId, price, quantity, side, timestamp);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    public void publishFill(long orderId, int strategyId, int symbolId, double price, double quantity, Side side, long timestamp) {
        long sequence = ringBuffer.next();

        try {
            TradingEvent event = ringBuffer.get(sequence);
            event.setFill(orderId, strategyId, symbolId, price, quantity, side, timestamp);
        } finally {
            ringBuffer.publish(sequence);
        }
    }
}
