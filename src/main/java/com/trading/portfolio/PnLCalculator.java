package com.trading.portfolio;

import com.trading.domain.Position;

import java.util.HashMap;
import java.util.Map;


public class PnLCalculator {
    private double totalRealizedPnL;
    private final Map<Integer, Double> pnlBySymbol = new HashMap<>();

    /**
     *
     *
     * @param pos
     */
    public void calculate(Position pos) {
        double tradePnL = pos.consumeLastTradeRealizedPnL();
        if (tradePnL == 0) return;

        this.totalRealizedPnL += tradePnL;
        pnlBySymbol.merge(pos.getSymbolId(), tradePnL, Double::sum);
    }

    public double getTotalRealizedPnL() {
        return totalRealizedPnL;
    }

    public double getPnLForSymbol(int symbolId) {
        return pnlBySymbol.getOrDefault(symbolId, 0.0);
    }

    /** Call at start of new trading day */
    public void resetDaily() {
        totalRealizedPnL = 0;
        pnlBySymbol.clear();
    }
}
