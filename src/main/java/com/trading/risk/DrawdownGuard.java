package com.trading.risk;

import com.trading.domain.Side;
import com.trading.infra.event.TradingEvent;
import com.trading.portfolio.PortfolioTracker;

public class DrawdownGuard implements RiskRule{
    private double maxDailyLoss;

    public DrawdownGuard(double maxDailyLoss) {
        this.maxDailyLoss = maxDailyLoss;
    }

    /**
     * Rejects the order if total realized PnL has fallen below the max daily loss threshold.
     *
     * @param order The ORDER_PROPOSED event being evaluated (not used directly, limit is portfolio-level).
     * @param pt PortfolioTracker providing the current total realized PnL.
     * @return REJECTED_DRAWDOWN_HALT if the loss limit is surpassed, PASSED otherwise.
     */
    @Override
    public RiskResult validate(TradingEvent order, PortfolioTracker pt) {
        if (order.getSide() == Side.SELL) return RiskResult.PASSED;
        double totalPnL = pt.getTotalRealizedPnL();

        return totalPnL < -maxDailyLoss
                ? RiskResult.REJECTED_DRAWDOWN_HALT
                : RiskResult.PASSED;
    }
}
