package com.trading.infra.engine;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.trading.infra.event.TradingEvent;
import com.trading.infra.event.TradingEventFactory;

public class DisruptorManager  implements TradingEventFactory {
    private Disruptor<TradingEvent> disruptor;
    private RingBuffer<TradingEvent> ringBuffer;

    public void start(){}

    public void publish(int data){}

    public void shutdown(){}

    @Override
    public TradingEvent newInstance() {
        return null;
    }
}
