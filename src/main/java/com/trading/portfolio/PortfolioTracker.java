package com.trading.portfolio;

import com.lmax.disruptor.EventHandler;
import com.trading.infra.event.EventType;
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


    //TODO: Create test suite for package.
    //TODO: Update Class diagram for component.
    /**
     * On Event check Fill Type, if sell then call position updateOnFill, add trading event to ledger.
     *
     * @param event Trading Event
     * @param sequence Event ID
     * @param endOfBatch
     */
    @Override
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) {
        if (event.getType() != EventType.ORDER_FILL) {
            return;
        }

        Position pos = posManager.getPosition(event.getSymbol());

        if (pos == null) {
            posManager.createNewPosition(event.getSymbol(), event.getQuantity(), event.getPrice());
        } else {
            pos.updateOnFill(event.getPrice(), event.getQuantity());
        }

        pnlCalc.calculate(pos);
        ledger.recordTrade(event, sequence);
    }
}
