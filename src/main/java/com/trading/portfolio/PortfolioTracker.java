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

        Position pos = posManager.getPosition(order.getSymbol());

        if (pos == null) {
            posManager.createNewPosition(order.getSymbol(), order.getQuantity(), order.getPrice());
        } else {
            pos.updateOnFill(order.getPrice(), order.getQuantity());
        }

        pnlCalc.calculate(pos);
        ledger.recordTrade(order, sequence);
    }
}
