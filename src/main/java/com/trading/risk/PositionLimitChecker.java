package com.trading.risk;

import com.trading.infra.event.TradingEvent;
import com.trading.portfolio.PortfolioTracker;

public class PositionLimitChecker implements RiskRule {
    private double maxPositionSize;


    @Override
    public RiskResult validate(TradingEvent order, PortfolioTracker pt) {
        return RiskResult.PASSED;
    }

}
