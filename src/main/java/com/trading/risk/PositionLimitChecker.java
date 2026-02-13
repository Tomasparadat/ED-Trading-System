package com.trading.risk;

import com.trading.domain.ProposedOrder;
import com.trading.portfolio.PortfolioTracker;

public class PositionLimitChecker implements RiskRule {
    private double maxPositionSize;


    @Override
    public RiskResult validate(ProposedOrder order, PortfolioTracker pt) {
        return RiskResult.PASSED;
    }

}
