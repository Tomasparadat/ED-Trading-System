package com.trading.handlers;

import com.lmax.disruptor.EventHandler;
import com.trading.infra.event.TradingEvent;
import com.trading.sim.EventProducer;
import com.trading.strategy.StrategyEngine;

import java.util.concurrent.atomic.AtomicLong;

public class StrategyHandler implements EventHandler<TradingEvent> {
    private final EventProducer orderProducer;
    private final StrategyEngine engine;
    private AtomicLong ORDER_ID_ORIGIN = new AtomicLong(0);

    public StrategyHandler(StrategyEngine engine, EventProducer orderProducer) {
        this.engine = engine;
        this.orderProducer = orderProducer;
    }

    /**
     * Receives a TradingEvent, checks if it's a MarketTick, passes the Event to the StrategyEngine.
     *
     * @param event Passed TradingEvent that is evaluated.
     * @param sequence -
     * @param endOfBatch -
     */
    @Override
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) {
        boolean approved = engine.processTrade(event);
        if (!approved) return;

        orderProducer.publishProposed(
                ORDER_ID_ORIGIN.getAndIncrement(),
                engine.getStrategyId(),
                event.getSymbolId(),
                event.getPrice(),
                engine.getProposedQuantity(),
                engine.getProposedSide(),
                event.getTimestamp()
        );
    }
}
