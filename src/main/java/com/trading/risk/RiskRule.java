package com.trading.risk;

import com.trading.infra.event.TradingEvent;
import com.trading.portfolio.PortfolioTracker;

public interface RiskRule{
    public RiskResult validate(TradingEvent order, PortfolioTracker pt);
}
