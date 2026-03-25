package com.trading.sim;

import java.util.Arrays;
import java.util.Random;

public class PriceGenerator {
    private static final double VOLATILITY = 0.20;
    private static final double STARTING_PRICE = 100.00;
    private double[] priceList;
    private final Random random = new Random();

    //BGM Settings
    private static final double DRIFT = 0.0;
    private static final double TICKS_PER_SECOND = 70000.0;
    private static final double DT = 1.0 / TICKS_PER_SECOND;
    private static final double SQRT_DT = Math.sqrt(DT);

    public PriceGenerator(SymbolRegistry registry) {
        this.priceList = new double[registry.size()];
        Arrays.fill(priceList, STARTING_PRICE);
    }

    /**
     * Calculates the next price for a specific symbol.
     * @param symbolId The index from the SymbolRegistry.
     * @return The new price
     */
    //TODO: Add Geometric Brownian Motion & compare price randomization.
    public double nextPrice(int symbolId) {
        double oldPrice = priceList[symbolId];
        double change = oldPrice * (random.nextGaussian() * VOLATILITY);
        double newPrice = Math.max(0.01, oldPrice + change);
        priceList[symbolId] = newPrice;

        return newPrice;
    }

    public double nextPriceGBM(int symbolId) {
        double oldPrice = priceList[symbolId];

        // Calculate the tiny percentage change for this specific micro-tick
        double driftComponent = DRIFT * DT;
        double randomComponent = VOLATILITY * SQRT_DT * random.nextGaussian();

        double change = oldPrice * (driftComponent + randomComponent);
        double newPrice = oldPrice + change;

        // Safety floor just in case of a massive random outlier
        if (newPrice <= 0) newPrice = 0.01;

        priceList[symbolId] = newPrice;
        return newPrice;
    }
}
