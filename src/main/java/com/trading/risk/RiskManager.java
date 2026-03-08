package com.trading.risk;

import com.trading.infra.event.TradingEvent;
import com.trading.portfolio.PortfolioTracker;

import java.util.ArrayList;
import java.util.List;

public class RiskManager {
    private final List<RiskRule> rules;
    private final PortfolioTracker portfolioReference;

    public RiskManager(PortfolioTracker portfolioTracker, double[] lastKnownPrices) {
        this.portfolioReference = portfolioTracker;
        this.rules = new ArrayList<>();
        this.rules.add(new OrderChecker(lastKnownPrices));
        this.rules.add(new PositionLimitChecker());
        this.rules.add(new DrawdownGuard());
    }

    public boolean evaluateOrder(TradingEvent order) {
        for (RiskRule rule : rules) {
            RiskResult result = rule.validate(order, portfolioReference);
            if (result != RiskResult.PASSED) {
                return false;
            }
        }
        return true;
    }
}