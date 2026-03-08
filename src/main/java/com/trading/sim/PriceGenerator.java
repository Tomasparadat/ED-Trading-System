package com.trading.sim;

import java.util.Arrays;
import java.util.Random;

public class PriceGenerator {
    private static final double VOLATILITY = 0.005;
    private static final double STARTING_PRICE = 100.00;
    private double[]  priceList;
    private final Random random = new Random();

    public PriceGenerator(SymbolRegistry registry) {
        this.priceList = new double[registry.size()];
        Arrays.fill(priceList, STARTING_PRICE);
    }

    /**
     * Calculates the next price for a specific symbol.
     * @param symbolId The index from the SymbolRegistry.
     * @return The new price
     */
    public double nextPrice(int symbolId) {
        double oldPrice = priceList[symbolId];
        double change = oldPrice * (random.nextGaussian() * VOLATILITY);
        double newPrice = Math.max(0.01, oldPrice + change);
        priceList[symbolId] = newPrice;

        return newPrice;
    }
}
