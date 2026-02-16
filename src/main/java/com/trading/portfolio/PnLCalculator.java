package com.trading.portfolio;

import com.trading.domain.Position;


public class PnLCalculator {
    private double totalRealizedPnL;

    /**
     * Calculate realized PnL based off Position's realized PnL on it's last trade.
     *
     * @param pos Position being referenced.
     */
    public void calculate(Position pos) {
        this.totalRealizedPnL +=  pos.getLastTradeRealizedPnL();
    }

    /**
     *
     * @return Total realized PnL.
     */
    public double getTotalRealizedPnL() {
        return totalRealizedPnL;
    }
}
