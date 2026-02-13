package com.trading.handlers;

import com.trading.infra.event.TradingEvent;

public abstract class BaseHandler {
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) {

    }
}
