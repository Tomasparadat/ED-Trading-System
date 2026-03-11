package com.trading.risk;

import com.trading.infra.event.TradingEvent;
import com.trading.portfolio.PortfolioTracker;

public class OrderChecker implements RiskRule {
    private final double maxPriceDeviationPercent;
    private final double[] lastKnownPrices;

    public OrderChecker(double[] lastKnownPrices, double maxPriceDeviationPercent) {
        this.lastKnownPrices = lastKnownPrices;
        this.maxPriceDeviationPercent = maxPriceDeviationPercent;
    }

    /**
     * Rejects an order if its price deviates more than maxPriceDeviationPercent
     * from the last known market price for that symbol.
     * If no reference price exists yet (0.0), the order is passed through.
     */
    @Override
    public RiskResult validate(TradingEvent order, PortfolioTracker pt) {
        int symbolId = order.getSymbolId();

        // Guard: symbolId out of bounds
        if (symbolId < 0 || symbolId >= lastKnownPrices.length) {
            return RiskResult.REJECTED_PRICE_INVALID;
        }

        double lastPrice = lastKnownPrices[symbolId];

        // No reference price yet — let it through, can't validate
        if (lastPrice == 0.0) {
            return RiskResult.PASSED;
        }

        double deviation = Math.abs(order.getPrice() - lastPrice) / lastPrice;

        return deviation > maxPriceDeviationPercent
                ? RiskResult.REJECTED_PRICE_INVALID
                : RiskResult.PASSED;
    }
}