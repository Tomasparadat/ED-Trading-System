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
     * @param symbol new Ticker given to the TradingEvent.
     * @param price new Price passed to TradingEvent.
     */
    public void publish(String symbol, double price) {
        int symbolId = symbolRegistry.getSymbolId(symbol);
        long sequence = ringBuffer.next();
        try {
            TradingEvent event = ringBuffer.get(sequence);
            event.clear();
            event.set(EventType.MARKET_TICK, symbolId, price, System.currentTimeMillis());
        } finally {
            ringBuffer.publish(sequence);
        }
    }
}
