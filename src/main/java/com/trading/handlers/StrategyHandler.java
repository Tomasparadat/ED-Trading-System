package com.trading.handlers;

import com.lmax.disruptor.EventHandler;
import com.trading.domain.EventType;
import com.trading.infra.event.TradingEvent;
import com.trading.strategy.StrategyEngine;

public class StrategyHandler implements EventHandler<TradingEvent> {
    private final StrategyEngine engine;
    private static long ORDER_ID_ORIGIN = 0;

    public StrategyHandler(StrategyEngine engine) {
        this.engine = engine;
    }

    /**
     *
     *
     * @param event Passed TradingEvent that is evaluated.
     * @param sequence ignored, it's necessary for EventHandler interface implementation.
     * @param endOfBatch ignored, it's necessary for EventHandler interface implementation.
     */
    @Override
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) {

        // Only accept MARKET_TICK, otherwise reject.
        if (event.getType() != EventType.MARKET_TICK) {
            return;
        }

        // Engine approves strategy internally.
        boolean approved = engine.process(event);

        // If the engine approves the trade, it's type is updated to ORDER_PROPOSED
        // So it can be picked up by the RiskManager off the Buffer.
        if(approved) {
            event.setType(EventType.ORDER_PROPOSED);
            event.setOrderId(generateOrderId());
            event.setStrategyId(engine.getStrategyId());
            event.setQuantity(engine.getProposedQuantity());
            event.setSide(engine.getProposedSide());
        }
    }

    private long generateOrderId() {
        return ORDER_ID_ORIGIN++;
    }
}
