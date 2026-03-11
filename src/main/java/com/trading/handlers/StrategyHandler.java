package com.trading.handlers;

import com.lmax.disruptor.EventHandler;
import com.trading.domain.EventType;
import com.trading.infra.event.TradingEvent;
import com.trading.strategy.StrategyEngine;

import java.util.concurrent.atomic.AtomicLong;

// NOTE: This handler mutates the event type in place rather than publishing
// a new ring buffer slot. This works for a single-threaded pipeline but
// limits throughput and prevents multiple fills per tick.
// TODO: Refactor to EventProducer.publishFill() when scaling up.
public class StrategyHandler implements EventHandler<TradingEvent> {
    private final StrategyEngine engine;
    private AtomicLong ORDER_ID_ORIGIN = new AtomicLong();

    public StrategyHandler(StrategyEngine engine) {
        this.engine = engine;
    }

    /**
     * Receives a TradingEvent, checks if it's a MarketTick, passes the Event to the StrategyEngine in order
     * to approve or reject it, once approved it updates the TradingEvent so it can be picked up by the RiskManager.
     *
     * @param event Passed TradingEvent that is evaluated.
     * @param sequence -
     * @param endOfBatch -
     */
    @Override
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) {

        if (event.getType() != EventType.MARKET_TICK) {
            return;
        }

        boolean approved = engine.processTrade(event);

        if(approved) {
            event.setType(EventType.ORDER_PROPOSED);
            event.setOrderId(generateOrderId());
            event.setStrategyId(engine.getStrategyId());
            event.setQuantity(engine.getProposedQuantity());
            event.setSide(engine.getProposedSide());
        }
    }


    private long generateOrderId() {
        return ORDER_ID_ORIGIN.incrementAndGet();
    }
}
