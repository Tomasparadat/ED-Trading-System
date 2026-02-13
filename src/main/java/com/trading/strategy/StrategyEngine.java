package com.trading.strategy;

import com.trading.handlers.EventHandler;
import com.trading.infra.event.TradingEvent;

public class StrategyEngine implements EventHandler {
    private IndicatorCalculator indicatorCalc;
    private SignalGenerator signalGen;
    private ExecutionClient executionClient;

    public void onEvent(TradingEvent event) {

    }
}
