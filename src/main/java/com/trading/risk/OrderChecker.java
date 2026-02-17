package com.trading.risk;

import com.trading.infra.event.TradingEvent;
import com.trading.portfolio.PortfolioTracker;

public class OrderChecker implements RiskRule {
    private double maxPriceDeviation;

    @Override
    public RiskResult validate(TradingEvent order, PortfolioTracker pt) {
        return null;
    }
}
