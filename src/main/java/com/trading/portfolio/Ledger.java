package com.trading.portfolio;

import com.trading.domain.EventType;
import com.trading.infra.event.TradingEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maintains an immutable record of all completed fills during a trading session.
 * Each fill is stored as a LedgerRecord snapshot to prevent mutation bugs
 * caused by the Disruptor reusing ring buffer slots.
 */
public class Ledger {
   private final List<LedgerRecord> tradeHistory = new ArrayList<>(1_000_000);


    /**
     * Records a TradingEvent into the trade history as an immutable LedgerRecord.
     * Silently ignores non-fill events.
     * TODO: Add error handling for malformed fill events.
     *
     * @param order The TradingEvent to snapshot — must be ORDER_FILL type.
     * @param sequence Ring buffer sequence number, used as a unique record ID.
     */
    public void recordTrade(TradingEvent order, long sequence) {
        if (order.getType() != EventType.ORDER_FILL) return;
        tradeHistory.add(new LedgerRecord(sequence, order));
    }

    /**
     * Returns an unmodifiable view of the full trade history for this session.
     * Used by SessionReport to print the trade log.
     *
     * @return Unmodifiable list of all LedgerRecords recorded this session.
     */
    public List<LedgerRecord> getTradeHistory() {
        return Collections.unmodifiableList(tradeHistory);
    }

    /**
     * @return Total number of fills recorded this session.
     */
    public int getTotalTrades() {
        return tradeHistory.size();
    }
}