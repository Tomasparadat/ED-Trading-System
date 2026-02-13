package com.trading.portfolio;

import com.trading.handlers.EventHandler;
import com.trading.infra.event.EventType;
import com.trading.infra.event.TradingEvent;

public class PortfolioTracker implements EventHandler {
    private PositionManager posManager;
    private PnLCalculator pnlCalc;
    private Ledger ledger;

    public PortfolioTracker() {
        this.posManager = new PositionManager();
        this.ledger = new Ledger();
        this.pnlCalc = new PnLCalculator();
    }


    //TODO: Finish implementing method.
    //TODO: Create test suite for package.
    //TODO: Update Class diagram for component.
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) {
        EventType type = event.getType();

//        switch (type) {
//            case ORDER_FILL -> {
//
//            }
//
//        }
    }
}
