package com.trading.risk;

import com.trading.infra.event.TradingEvent;
import com.trading.portfolio.PortfolioTracker;

public class PositionLimitChecker implements RiskRule {
    // Arbitrary
    //TODO: Implement easy change thru Controller
    private double maxPositionSize = 1000.00;


    /**
     *
     *
     * @param order
     * @param pt
     * @return
     */
    @Override
    public RiskResult validate(TradingEvent order, PortfolioTracker pt) {
        double totalQuantity = order.getQuantity() + pt.getPositionQuantity(order.getSymbolId());

        if(totalQuantity > maxPositionSize) {
            return RiskResult.REJECTED_LIMIT_EXCEEDED;
        }

        return RiskResult.PASSED;
    }

}
