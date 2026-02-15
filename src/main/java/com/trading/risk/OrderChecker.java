package com.trading.risk;

import com.trading.domain.ProposedOrder;
import com.trading.portfolio.PortfolioTracker;

public class OrderChecker implements RiskRule {
    private double maxPriceDeviation;

    @Override
    public RiskResult validate(ProposedOrder order, PortfolioTracker pt) {
        return null;
    }
}
