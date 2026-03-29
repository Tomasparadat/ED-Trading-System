package com.trading.sim;

import java.util.Arrays;
import java.util.SplittableRandom;

public class PriceGenerator {
    private final double[] priceList;
    private final SplittableRandom random = new SplittableRandom();

    private static final double VOLATILITY = 0.20;
    private static final double STARTING_PRICE = 100.00;
    private static final double DRIFT = 0.0;
    private static final double TICKS_PER_SECOND = 1_000_000.0;
    private static final double DT = 1.0 / TICKS_PER_SECOND;

    private static final double DRIFT_COMPONENT = DRIFT * DT;
    private static final double VOL_SQRT_DT = VOLATILITY * Math.sqrt(DT);

    public PriceGenerator(SymbolRegistry registry) {
        this.priceList = new double[registry.size()];
        Arrays.fill(priceList, STARTING_PRICE);
    }

    /**
     * Generates price utilizing Geometric Brownian Motion.
     */
    public double nextPriceGBM(int symbolId) {
        double oldPrice = priceList[symbolId];

        double randomComponent = VOL_SQRT_DT * random.nextGaussian();
        double newPrice = oldPrice * (1.0 + DRIFT_COMPONENT + randomComponent);

        if (newPrice <= 0) newPrice = 0.01;

        priceList[symbolId] = newPrice;
        return newPrice;
    }

}