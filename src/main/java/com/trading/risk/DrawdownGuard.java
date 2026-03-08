package com.trading.risk;

import com.trading.infra.event.TradingEvent;
import com.trading.portfolio.PortfolioTracker;

public class DrawdownGuard implements RiskRule{
    // Arbitrary number (10K max dail loss)
    //TODO: Implement easy change in a controller.
    private double maxDailyLoss = 10000.00;

    /**
     * Check if total realized PnL is greater than Max Daily loss limit.
     *
     * totalRealizedPnL can be a negative number and encompasses win and loss trades for simplicity.
     *
     * @param order
     * @param pt
     * @return
     */
    //TODO: Check for daily and not total pnl for account lifecycle.
    @Override
    public RiskResult validate(TradingEvent order, PortfolioTracker pt) {
        double totalPnL = pt.getTotalRealizedPnL();

        return pt.getTotalRealizedPnL() < -maxDailyLoss
                ? RiskResult.REJECTED_DRAWDOWN_HALT
                : RiskResult.PASSED;
    }
}
