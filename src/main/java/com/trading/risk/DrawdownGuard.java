package com.trading.risk;

import com.trading.infra.event.TradingEvent;
import com.trading.portfolio.PortfolioTracker;

public class DrawdownGuard implements RiskRule{
    private double maxDailyLoss;

    public RiskResult validate() {
        return null;
    }

    @Override
    public RiskResult validate(TradingEvent order, PortfolioTracker pt) {
        return null;
    }
}
