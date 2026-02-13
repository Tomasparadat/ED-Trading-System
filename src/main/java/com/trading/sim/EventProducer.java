package com.trading.sim;

import com.lmax.disruptor.RingBuffer;
import com.trading.domain.OrderFill;
import com.trading.infra.event.TradingEvent;

public class EventProducer {
    private RingBuffer<TradingEvent> ringBuffer;

    public void publishPrice(String symbol, double price) {}

    public void publishFill(OrderFill fill){}
}
