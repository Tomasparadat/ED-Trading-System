package com.trading.strategy;

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
}
