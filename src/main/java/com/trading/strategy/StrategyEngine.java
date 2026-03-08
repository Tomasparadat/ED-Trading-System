package com.trading.strategy;

import com.trading.domain.Side;
import com.trading.infra.event.TradingEvent;

/**
 * Drives the SMA crossover strategy.
 * Feeds each market tick into the IndicatorCalculator, checks for a signal,
 * and stores the decision for StrategyHandler to read.
 */
public class StrategyEngine {
    private static final int STRATEGY_ID      = 1;
    private static final double DEFAULT_QUANTITY = 10.0;

    private final IndicatorCalculator[] indicators;
    private final SignalGenerator[] signals;

    private Side proposedSide = null;

    /**
     *
     * @param fastPeriod
     * @param slowPeriod
     * @param symbolCount
     */
    public StrategyEngine(int fastPeriod, int slowPeriod, int symbolCount) {
        this.indicators = new IndicatorCalculator[symbolCount];
        this.signals = new SignalGenerator[symbolCount];

        for (int i = 0; i < symbolCount; i++) {
            indicators[i] = new IndicatorCalculator(fastPeriod, slowPeriod);
            signals[i] = new SignalGenerator();
        }
    }

    /**
     *
     * @param event
     * @return
     */
    public boolean processTrade(TradingEvent event) {
        int symbolId = event.getSymbolId();

        indicators[symbolId].update(event.getPrice());
        SignalType type = signals[symbolId].check(indicators[symbolId]);

        switch (type) {
            case BUY -> { proposedSide = Side.BUY;  return true; }
            case SELL -> { proposedSide = Side.SELL; return true; }
            default -> { proposedSide = null;      return false; }
        }
    }

    public int getStrategyId() { return STRATEGY_ID; }

    public double getProposedQuantity() { return DEFAULT_QUANTITY; }

    public Side getProposedSide() { return proposedSide; }
}
