package com.trading.infra.reporting;

import com.trading.portfolio.Ledger;
import com.trading.portfolio.LedgerRecord;
import com.trading.portfolio.PortfolioTracker;
import com.trading.sim.SymbolRegistry;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Replace with QuestDB queries
 * when the database layer is implemented.
 */
public class SessionReport {
    private final PortfolioTracker portfolio;
    private final SymbolRegistry registry;
    private final long totalTicks;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    /**
     * Constructs a SessionReport for the given portfolio and symbol registry.
     *
     * @param portfolio PortfolioTracker holding final position and PnL state.
     * @param registry  SymbolRegistry used to resolve symbolIds to ticker names.
     */
    public SessionReport(PortfolioTracker portfolio, SymbolRegistry registry, long totalTicks) {
        this.portfolio = portfolio;
        this.registry = registry;
        this.totalTicks = totalTicks;
    }


    public void print() {
        Ledger ledger = portfolio.getLedger();

        System.out.println("\n========================================");
        System.out.println("          SESSION REPORT                ");
        System.out.println("========================================");

        System.out.printf("%n--- Trade Log (showing last 20 of %d fills) ---%n", ledger.getTotalTrades());
        List<LedgerRecord> history = ledger.getTradeHistory();
        int start = Math.max(0, history.size() - 20);

        for (int i = start; i < history.size(); i++) {
            LedgerRecord r = history.get(i);
            System.out.printf("  %-8d %-6s %-5s %-10.2f %-8.1f %s%n",
                    r.getOrderId(),
                    registry.getSymbolName(r.getSymbolId()),
                    r.getSide(),
                    r.getPrice(),
                    r.getQuantity(),
                    FORMATTER.format(Instant.ofEpochMilli(r.getTimestamp()))
            );
        }

        System.out.println("\n--- Final Positions ---");
        for (int i = 0; i < registry.size(); i++) {
            System.out.printf("  %-6s | qty: %8.2f%n",
                    registry.getSymbolName(i),
                    portfolio.getPositionQuantity(i)
            );
        }

        System.out.println("\n--- Realized PnL by Symbol ---");
        for (int i = 0; i < registry.size(); i++) {
            System.out.printf("  %-6s | pnl: %+10.2f%n",
                    registry.getSymbolName(i),
                    portfolio.getPnLForSymbol(i)
            );
        }

        System.out.println("\n--- Total ---");
        System.out.printf("  Total Ticks: %d%n", totalTicks);
        System.out.printf("  Total Fills: %d%n", ledger.getTotalTrades());
        System.out.printf("  Fill Rate: %.4f%%%n", totalTicks == 0 ? 0.0 : (ledger.getTotalTrades() * 100.0 / totalTicks));
        System.out.printf("  Total Realized PnL: %+.2f%n", portfolio.getTotalRealizedPnL());
        System.out.println("========================================\n");
    }
}
