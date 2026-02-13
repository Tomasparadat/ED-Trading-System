package com.trading.portfolio;

import com.trading.infra.event.TradingEvent;

import java.util.ArrayList;
import java.util.List;

public class Ledger {
    private List<TradingEvent> tradeHistory = new ArrayList<>();

    /**
     * Records a TradingEvent into the Trading history.
     *
     * @param fill
     */
    public void recordTrade(TradingEvent fill){
        tradeHistory.add(fill);
    }
}
