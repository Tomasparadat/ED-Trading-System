package com.trading.risk;

import com.trading.domain.ProposedOrder;
import com.trading.portfolio.PortfolioTracker;

import java.util.*;
import java.util.ArrayList;

import static com.trading.risk.RiskResult.PASSED;

public class RiskManager {
    private List<RiskRule> rules = new ArrayList<>() {{
        add(new OrderChecker());
        add(new PositionLimitChecker());
        add(new DrawdownGuard());
    }};
    private PortfolioTracker portfolioReference;

    public RiskResult checkOrder(ProposedOrder order) {


        for (RiskRule rule : rules) {
            rule.validate(order, portfolioReference);
        }


    }

}
