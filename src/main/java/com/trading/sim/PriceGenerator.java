package com.trading.sim;

public class PriceGenerator {
    private double currentPrice;
    private double volatility;
    private double[]  priceList;

    public PriceGenerator(SymbolRegistry registry) {
        this.priceList = new double[registry.size()];
    }

    public double nextPrice() {
        return 0;
    }
}
