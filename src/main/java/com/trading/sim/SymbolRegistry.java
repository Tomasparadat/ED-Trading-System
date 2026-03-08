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
     *
     * @param symbolsList The list of tickers from configuration.
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
     * Translates a Ticker String to its internal Integer ID using the HashMap.
     * O(1) lookup. Returns -1 if the symbol is not registered.
     *
     * @param symbol Ticker string (e.g. "BTC").
     * @return The unique int ID, or -1 if not found.
     */
    public int getSymbolId(String symbol) {
        return hashIdMap.getOrDefault(symbol, -1);
    }

    /**
     * Translates an internal Integer ID back to its Ticker String.
     * Used by the QuestDBBridge or Logger for human-readable output.
     *
     * @param symbolId Internal integer ID.
     * @return The Ticker String (e.g. "BTC").
     * @throws ArrayIndexOutOfBoundsException if symbolId is out of range.
     */
    public String getSymbolName(int symbolId) {
        return symbols[symbolId];
    }

    /**
     * Returns the total count of registered symbols.
     * Used by PriceGenerator and OrderMatcher to size their primitive arrays.
     *
     * @return Number of registered symbols.
     */
    public int size() {
        return symbols.length;
    }

    /**
     * Returns a stream of all valid symbol IDs (0 to N-1).
     * Used by MarketSimulator to iterate over all symbols each tick.
     *
     * @return IntStream of all symbol IDs.
     */
    public IntStream getAllIds() {
        return IntStream.range(0, symbols.length);
    }

    /**
     * Checks whether a given symbol string is registered.
     *
     * @param symbol Ticker string to check.
     * @return true if registered, false otherwise.
     */
    public boolean contains(String symbol) {
        return hashIdMap.containsKey(symbol);
    }

    /**
     * Checks whether a given symbol ID is within valid range.
     *
     * @param symbolId Integer ID to check.
     * @return true if valid, false otherwise.
     */
    public boolean isValidId(int symbolId) {
        return symbolId >= 0 && symbolId < symbols.length;
    }
}