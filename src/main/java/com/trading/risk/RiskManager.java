package com.trading.risk;

import com.trading.infra.event.TradingEvent;
import com.trading.portfolio.PortfolioTracker;

import java.util.*;
import java.util.ArrayList;

public class RiskManager {
    private List<RiskRule> rules = new ArrayList<>() {{
        add(new OrderChecker());
        add(new PositionLimitChecker());
        add(new DrawdownGuard());
    }};
    private PortfolioTracker portfolioReference;

    public boolean evaluateOrder(TradingEvent order) {
        //TODO: Implement correctly to check agains rule list.
        //TODO: Fix method to catch errors
        //TODO: Fix method to return boolean according to approve or reject.

        for (RiskRule rule : rules) {
            rule.validate(order, portfolioReference);
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
