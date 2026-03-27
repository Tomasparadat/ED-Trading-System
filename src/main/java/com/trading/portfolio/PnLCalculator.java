package com.trading.portfolio;

import com.trading.domain.Position;


public class PnLCalculator {
    private double totalRealizedPnL;
    private final double[] pnlBySymbol;


    public PnLCalculator(int symbolCount) {
        this.pnlBySymbol = new double[symbolCount];
    }

    public void calculate(Position pos) {
        double tradePnL = pos.consumeLastTradeRealizedPnL();
        if (tradePnL == 0) return;

        totalRealizedPnL += tradePnL;
        pnlBySymbol[pos.getSymbolId()] += tradePnL;
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
        return pnlBySymbol[symbolId];
    }

}
