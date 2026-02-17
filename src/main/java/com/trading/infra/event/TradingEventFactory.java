package com.trading.infra.event;

import com.lmax.disruptor.EventFactory;

public class TradingEventFactory implements EventFactory<TradingEvent> {
    @Override
    public TradingEvent newInstance() {
        return new TradingEvent();
    }
}
