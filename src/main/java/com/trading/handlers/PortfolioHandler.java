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

    /**
     * Receives each event from the Disruptor pipeline and delegates ORDER_FILL
     * events to the PortfolioTracker for position and PnL updates.
     * All other event types are ignored to keep the portfolio state clean —
     * only confirmed fills should affect positions.
     *
     * @param event TradingEvent received from the ring buffer.
     * @param sequence Ring buffer sequence number, passed through to PortfolioTracker for Ledger recording.
     * @param endOfBatch Whether this is the last event in the current batch (unused).
     * @throws Exception if PortfolioTracker.onEvent throws during position update.
     */
    @Override
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) throws Exception {
        if(event.getType() == EventType.ORDER_FILL){
            tracker.onEvent(event, sequence, endOfBatch);
        }
    }
}
