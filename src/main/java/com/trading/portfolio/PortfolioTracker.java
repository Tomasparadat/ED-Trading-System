package com.trading.portfolio;

import com.lmax.disruptor.EventHandler;
import com.trading.domain.OrderFill;
import com.trading.domain.Position;
import com.trading.infra.event.EventType;

public class PortfolioTracker implements EventHandler<OrderFill> {
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
    public void onEvent(OrderFill order, long sequence, boolean endOfBatch) {

        Position pos = posManager.getPosition(order.symbol());

        if (pos == null) {
            posManager.createNewPosition(order.symbol(), order.quantity(), order.price());
        } else {
            pos.updateOnFill(order.price(), order.quantity());
        }

        pnlCalc.calculate(pos);
        ledger.recordTrade(order, sequence);
    }
}
