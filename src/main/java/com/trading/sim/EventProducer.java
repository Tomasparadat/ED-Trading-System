package com.trading.sim;

import com.lmax.disruptor.RingBuffer;
import com.trading.infra.event.TradingEvent;

public class EventProducer {
    private RingBuffer<TradingEvent> ringBuffer;


    public void publishPrice(String symbol, double price) {
        ringBuffer.p
    }

    // TODO: Complete implementation.
    public void publishFill(TradingEvent fill){
        ringBuffer.publish(fill.getOrderId());
    }

}
