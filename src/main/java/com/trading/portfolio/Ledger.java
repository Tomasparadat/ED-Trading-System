package com.trading.portfolio;

import com.trading.infra.event.TradingEvent;

import java.util.ArrayList;
import java.util.List;

public class Ledger {
    private List<LedgerRecord> tradeHistory = new ArrayList<>();

    /**
     * Records a TradingEvent into the Trading history through a LedgerRecord that takes a Snapshot of the trade.
     * This prevents Mutation Bugs since the Disruptor re-uses Memory addresses which can lead to multiple trades
     * having the same address (sequence).
     *
     * @param order Trading Event
     * @param sequence Trading Event ID
     */
    public void recordTrade(TradingEvent order, long sequence){
        tradeHistory.add(new LedgerRecord(sequence, order));
    }
}
