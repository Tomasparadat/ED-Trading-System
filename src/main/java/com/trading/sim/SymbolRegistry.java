package com.trading.sim;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.IntStream;

/**
 * High-performance, immutable registry that maps Ticker Strings to unique Integer IDs.
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
     *
     * @param symbol Ticker string (e.g. "BTC").
     * @return The unique int ID, or -1 if not found.
     */
    public int getSymbolId(String symbol) {
        return hashIdMap.getOrDefault(symbol, -1);
    }

    /**
     * Translates an internal Integer ID back to its Ticker String.
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
     *
     * @return Number of registered symbols.
     */
    public int size() {
        return symbols.length;
    }

    /**
     * Returns a stream of all valid symbol IDs (0 to N-1).
     *
     * @return IntStream of all symbol IDs.
     */
    public int[] getAllIdsAsArray() {
        return IntStream.range(0, symbols.length - 1).toArray();
    }

}