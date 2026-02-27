package com.trading.sim;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.IntStream;

/**
 * A high-performance, immutable registry that maps Ticker Strings to unique Integer IDs.
 * This allows the Core Engine to use primitive array indexing instead of Map lookups.
 */
public class SymbolRegistry {
    private final String[] symbols;
    private final Map<String, Integer> hashIdMap;


    /**
     * Initializes the registry with a fixed list of symbols.
     * IDs are assigned based on the order of the list (0 to N-1).
     * @param symbolsList The list of tickers from configuration
     */
    public SymbolRegistry(List<String> symbolsList) {
        this.symbols = new String[symbolsList.size()];
        this.hashIdMap = new HashMap<>();

        for (int i = 0; i < symbolsList.size(); i++) {
            String sym = symbolsList.get(i);
            this.symbols[i] = sym;
            this.hashIdMap.put(sym, i);
        }
    }

    /**
     * Translates a Ticker String to its internal Integer ID.
     * Used at the "Edge" when receiving external orders or starting the sim.
     * @return The unique int ID (e.g., "BTC" -> 1)
     */
    // TODO: Usage for DB persistence.
    public int getSymbolId(String symbol) {
        //TODO: Not forget to catch -1 error in MarketSimulator.
        return IntStream.range(0, symbols.length)
                .filter(i -> symbols[i].equals(symbol))
                .findFirst()
                .orElse(-1);
    }

    /**
     * Translates an internal Integer ID back to its Ticker String.
     * Used by the QuestDBBridge or Logger for human-readable output.
     * @return The Ticker String (e.g., 1 -> "BTC")
     */
    public String getSymbolName(int symbolId) {
        // symbolId is trusted since it comes from own, self generated registry, which guarantees an Index.
        return symbols[symbolId];
    }

    /**
     * Returns the total count of registered symbols.
     * Used by PriceGenerator and OrderMatcher to size their primitive arrays.
     */
    public int size() {
        return symbols.length;
    }

    /**
     * Returns an unmodifiable list of all IDs.
     * Useful for the MarketSimulator to loop through and trigger price updates.
     */
    public IntStream getAllIds() {
        return IntStream.range(0, symbols.length);
    }
}
