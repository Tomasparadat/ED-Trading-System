package com.trading.portfolio;

import com.lmax.disruptor.EventHandler;
import com.trading.domain.Position;
import com.trading.domain.Side;
import com.trading.infra.event.TradingEvent;

public class PortfolioTracker implements EventHandler<TradingEvent> {
    private PositionManager posManager;
    private PnLCalculator pnlCalc;
    private Ledger ledger;

    public PortfolioTracker(int symbolCount) {
        this.posManager = new PositionManager(symbolCount);
        this.ledger = new Ledger();
        this.pnlCalc = new PnLCalculator(symbolCount);
    }


    /**
     * On Event check Fill Type, if sell then call position updateOnFill, add trading event to ledger.
     *
     * @param order Trading Event
     * @param sequence Event ID
     * @param endOfBatch unused
     */
    @Override
    public void onEvent(TradingEvent order, long sequence, boolean endOfBatch) {
        Position pos = posManager.getPosition(order.getSymbolId());

        double fillQty = order.getSide() == Side.SELL ? -order.getQuantity() : order.getQuantity();

        if (pos == null) {
            if (order.getSide() == Side.SELL) {
                return;
            }
            posManager.createNewPosition(order.getSymbolId(), fillQty, order.getPrice());
            pos = posManager.getPosition(order.getSymbolId());
        } else {
            pos.updateOnFill(fillQty, order.getPrice());
        }

        if (pos == null) return;

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

    public Ledger getLedger() { return ledger;}

    /**
     * Returns the realized PnL for a specific symbol since the last reset.
     *
     * @param symbolId Internal symbol ID from SymbolRegistry.
     * @return Realized PnL for the symbol, or 0.0 if no trades recorded.
     */
    public double getPnLForSymbol(int symbolId) {
        return pnlCalc.getPnLForSymbol(symbolId);
    }
}
