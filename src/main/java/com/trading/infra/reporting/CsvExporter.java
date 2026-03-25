package com.trading.infra.reporting;

import com.trading.portfolio.Ledger;
import com.trading.portfolio.LedgerRecord;
import com.trading.sim.SymbolRegistry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Exports the full trade history from the Ledger to a CSV file.
 * Each row represents one ORDER_FILL event captured as a LedgerRecord
 */
public class CsvExporter {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    private final Ledger ledger;
    private final SymbolRegistry registry;
    private final String outputPath;


    public CsvExporter(Ledger ledger, SymbolRegistry registry, String outputPath) {
        this.ledger = ledger;
        this.registry = registry;
        this.outputPath = outputPath;
    }


    public void export() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            writer.write("orderId,symbol,side,price,quantity,timestamp");
            writer.newLine();

            for (LedgerRecord r : ledger.getTradeHistory()) {
                writer.write(String.format("%d,%s,%s,%.5f,%.1f,%s",
                        r.getOrderId(),
                        registry.getSymbolName(r.getSymbolId()),
                        r.getSide(),
                        r.getPrice(),
                        r.getQuantity(),
                        FORMATTER.format(Instant.ofEpochMilli(r.getTimestamp()))
                ));
                writer.newLine();
            }

            System.out.printf("Trades exported to %s (%d rows)%n", outputPath, ledger.getTotalTrades());

        } catch (IOException e) {
            System.err.println("Failed to export trades to CSV: " + e.getMessage());
        }
    }
}
