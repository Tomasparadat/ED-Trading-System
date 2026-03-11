package com.trading.risk;

import com.trading.domain.Side;
import com.trading.infra.event.TradingEvent;
import com.trading.portfolio.PortfolioTracker;

public class PositionLimitChecker implements RiskRule {
    private double maxPositionSize;

    public PositionLimitChecker(double maxPositionSize) {
        this.maxPositionSize = maxPositionSize;
    }

    /**
     * Validates the order by checking the total amount of the Fill Quantity and the current Position Quantity
     * against the maxPositionSize and returns REJECTED_LIMIT_EXCEEDED if the total Quantity > maxPositionSize and
     * PASSED if total Quantity < maxPositionSize.
     *
     * @param order TradingEvent being evaluated
     * @param pt Portfolio reference to check current size of the underlying asset's Position.
     * @return rejected or approved Trade in the form of RiskResult.
     */
    @Override
    public RiskResult validate(TradingEvent order, PortfolioTracker pt) {
        if (order.getSide() == Side.SELL) return RiskResult.PASSED;

        double totalQuantity = order.getQuantity() + pt.getPositionQuantity(order.getSymbolId());

        if(totalQuantity > maxPositionSize) {
            return RiskResult.REJECTED_LIMIT_EXCEEDED;
        }

        return RiskResult.PASSED;
    }

}
