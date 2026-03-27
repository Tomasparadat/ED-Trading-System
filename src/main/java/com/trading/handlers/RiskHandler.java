package com.trading.handlers;

import com.lmax.disruptor.EventHandler;
import com.trading.infra.event.TradingEvent;
import com.trading.risk.RiskManager;
import com.trading.sim.EventProducer;


public class RiskHandler implements EventHandler<TradingEvent> {
    private final RiskManager riskManager;
    private final EventProducer fillProducer;

    public RiskHandler(RiskManager riskManager, EventProducer fillProducer) {
        this.riskManager = riskManager;
        this.fillProducer = fillProducer;
    }

    @Override
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) {
        boolean approved = riskManager.evaluateOrder(event);
        if (!approved) return;

        fillProducer.publishFill(
                event.getOrderId(),
                event.getStrategyId(),
                event.getSymbolId(),
                event.getPrice(),
                event.getQuantity(),
                event.getSide(),
                event.getTimestamp()
        );
    }
}
