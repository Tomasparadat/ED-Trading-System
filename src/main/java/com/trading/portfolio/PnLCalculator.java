package com.trading.portfolio;

import com.trading.domain.Position;

import java.util.HashMap;
import java.util.Map;


public class PnLCalculator {
    private double totalRealizedPnL;
    private final Map<Integer, Double> pnlBySymbol = new HashMap<>();

    /**
     * Consumes the last trade's realized PnL from the position and accumulates
     * it into the total and per-symbol trackers. Skips positions with no PnL
     * to avoid polluting the totals.
     *
     * @param pos Position to read and consume realized PnL from.
     */
    public void calculate(Position pos) {
        double tradePnL = pos.consumeLastTradeRealizedPnL();
        if (tradePnL == 0) return;

        this.totalRealizedPnL += tradePnL;
        pnlBySymbol.merge(pos.getSymbolId(), tradePnL, Double::sum);
    }

    /**
     * Returns the total realized PnL across all symbols since last reset.
     *
     * @return Cumulative realized PnL.
     */
    public double getTotalRealizedPnL() {
        return totalRealizedPnL;
    }

    /**
     * Returns the realized PnL for a specific symbol since last reset.
     *
     * @param symbolId Internal symbol ID from SymbolRegistry.
     * @return Realized PnL for the symbol, or 0.0 if no trades recorded.
     */
    public double getPnLForSymbol(int symbolId) {
        return pnlBySymbol.getOrDefault(symbolId, 0.0);
    }

    /**
     * Resets all PnL accumulators. Should be called at the start of each
     * trading day to ensure DrawdownGuard tracks daily loss correctly.
     */
    public void resetDaily() {
        totalRealizedPnL = 0;
        pnlBySymbol.clear();
    }
}
