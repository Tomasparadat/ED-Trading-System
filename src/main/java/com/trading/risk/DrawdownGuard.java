package com.trading.risk;

import com.trading.domain.ProposedOrder;
import com.trading.portfolio.PortfolioTracker;

public class DrawdownGuard implements RiskRule{
    private double maxDailyLoss;

    public RiskResult validate() {
        return null;
    }

    @Override
    public RiskResult validate(ProposedOrder order, PortfolioTracker pt) {
        return null;
    }
}
