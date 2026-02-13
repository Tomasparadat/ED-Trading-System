package com.trading.risk;

import com.trading.domain.ProposedOrder;
import com.trading.portfolio.PortfolioTracker;

public interface RiskRule{
    public RiskResult validate(ProposedOrder order, PortfolioTracker pt);
}
