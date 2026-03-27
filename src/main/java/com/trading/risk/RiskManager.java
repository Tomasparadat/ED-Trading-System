package com.trading.risk;

import com.trading.infra.event.TradingEvent;
import com.trading.portfolio.PortfolioTracker;

import java.util.ArrayList;
import java.util.List;

public class RiskManager {
    private final List<RiskRule> rules;
    private final PortfolioTracker portfolioReference;

    /**
     * Constructs a RiskManager with all risk rules initialised.
     * Parameters are passed through from SystemController.
     *
     * @param portfolioTracker PortfolioTracker shared with PortfolioHandler,
     *                         used by PositionLimitChecker and DrawdownGuard.
     * @param lastKnownPrices Shared price array from OrderMatcher, used by OrderChecker for deviation validation.
     * @param maxPositionSize Maximum allowed total quantity per symbol.
     * @param maxDailyLoss Maximum allowed daily loss before trading is halted.
     * @param maxPriceDevPct Maximum allowed price deviation from last known price.
     */
    public RiskManager(PortfolioTracker portfolioTracker, double[] lastKnownPrices,
                       double maxPositionSize, double maxDailyLoss, double maxPriceDevPct) {
        this.portfolioReference = portfolioTracker;
        this.rules = new ArrayList<>();
        this.rules.add(new OrderChecker(lastKnownPrices, maxPriceDevPct));
        this.rules.add(new PositionLimitChecker(maxPositionSize));
        this.rules.add(new DrawdownGuard(maxDailyLoss));
    }

    /**
     * Evaluates an ORDER_PROPOSED event against all registered risk rules in order.
     * Returns false as soon as any rule rejects the order.
     * Returns true only if all rules pass.
     *
     * @param order The ORDER_PROPOSED TradingEvent to evaluate.
     * @return true if the order passes all risk checks, false if any rule rejects it.
     */
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