package com.trading.handlers;

import com.lmax.disruptor.EventHandler;
import com.trading.infra.event.TradingEvent;

public abstract class BaseHandler implements EventHandler<TradingEvent> {
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) {

    }
}
