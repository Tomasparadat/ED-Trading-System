package com.trading.handlers;

import com.lmax.disruptor.EventHandler;
import com.trading.domain.EventType;
import com.trading.infra.event.TradingEvent;
import com.trading.portfolio.PortfolioTracker;

public class PortfolioHandler implements EventHandler<TradingEvent> {
    private final PortfolioTracker tracker;

    public PortfolioHandler(PortfolioTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) throws Exception {
        if(event.getType() == EventType.ORDER_FILL){
        tracker.onEvent(event, sequence, endOfBatch);
        } else return;
    }
}
