package com.trading.handlers;

import com.lmax.disruptor.EventHandler;
import com.trading.domain.EventType;
import com.trading.infra.event.TradingEvent;
import com.trading.risk.RiskManager;

// NOTE: This handler mutates the event type in place rather than publishing
// a new ring buffer slot. This works for a single-threaded pipeline but
// limits throughput and prevents multiple fills per tick.
// TODO: Refactor to EventProducer.publishFill() in future version.
public class RiskHandler implements EventHandler<TradingEvent> {
    private final RiskManager riskManager;

    public RiskHandler(RiskManager riskManager) {
        this.riskManager = riskManager;
    }

    /**
     *
     * @param event TradingEvent being evaluated and previously approved by StrategyEngine
     * @param endOfBatch ignore.
     * @param sequence TradingEvent ID, inside RingBuffer.
     */
    @Override
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) {
        if(event.getType() != EventType.ORDER_PROPOSED) {
            return;
        }

        boolean approved = riskManager.evaluateOrder(event);

        if(approved) {
            event.setType(EventType.ORDER_FILL);
        }
    }
}
