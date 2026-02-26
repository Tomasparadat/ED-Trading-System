package com.trading.strategy;

import com.trading.domain.Side;
import com.trading.infra.event.TradingEvent;

public class StrategyEngine {
    private IndicatorCalculator indicatorCalc;
    private SignalGenerator signalGen;
    private ExecutionClient executionClient;

    public boolean process(TradingEvent event) {
        // Placeholder
        // TODO: Implement method with basic strategy to test functionality.
        return false;
    }

    public int getStrategyId() {
        return 0;
    }

    public double getProposedQuantity() {
        return 0.0;
    }

    public Side getProposedSide() {
        return null;
    }
}


