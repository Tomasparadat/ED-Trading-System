package com.trading.portfolio;

import com.lmax.disruptor.EventHandler;
import com.trading.domain.Position;
import com.trading.infra.event.TradingEvent;

public class PortfolioTracker implements EventHandler<TradingEvent> {
    private PositionManager posManager;
    private PnLCalculator pnlCalc;
    private Ledger ledger;

    public PortfolioTracker() {
        this.posManager = new PositionManager();
        this.ledger = new Ledger();
        this.pnlCalc = new PnLCalculator();
    }


    /**
     * On Event check Fill Type, if sell then call position updateOnFill, add trading event to ledger.
     *
     * @param order Trading Event
     * @param sequence Event ID
     * @param endOfBatch ..
     */
    @Override
    public void onEvent(TradingEvent order, long sequence, boolean endOfBatch) {
        Position pos = posManager.getPosition(order.getSymbolId());

        if (pos == null) {
            posManager.createNewPosition(order.getSymbolId(), order.getQuantity(), order.getPrice());
            pos = posManager.getPosition(order.getSymbolId());
        }
        pos.updateOnFill(order.getQuantity(), order.getPrice());
        pnlCalc.calculate(pos);

        ledger.recordTrade(order, sequence);
    }

    /**
     * Return current Quantity being held for Position with symbolId of Parameter.
     *
     * @param symbolId Ticker Symbol of queried Position.
     * @return Quantity being held.
     */
    public double getPositionQuantity(int symbolId) {
        Position pos = posManager.getPosition(symbolId);
        return pos != null ? pos.getQuantity() : 0.0;
    }

    public double getTotalRealizedPnL() {
        return pnlCalc.getTotalRealizedPnL();
    }
}
