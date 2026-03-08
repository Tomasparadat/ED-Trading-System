package com.trading.sim;

import com.lmax.disruptor.RingBuffer;
import com.trading.domain.EventType;
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
            event.set(EventType.MARKET_TICK, symbolId, price, System.currentTimeMillis()); // clear happens inside
        } finally {
            ringBuffer.publish(sequence);
        }
    }
}
