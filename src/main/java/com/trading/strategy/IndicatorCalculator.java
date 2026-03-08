package com.trading.strategy;

/**
 * Calculates a fast and slow Simple Moving Average (SMA) using circular buffers.
 */
public class IndicatorCalculator {
    private final double[] fastWindow;
    private final double[] slowWindow;

    private int fastIdx = 0;
    private int slowIdx = 0;
    private int fastCount = 0;
    private int slowCount = 0;
    private double fastSum = 0.0;
    private double slowSum = 0.0;

    /**
     * @param fastPeriod Number of ticks for the fast SMA (e.g. 5).
     * @param slowPeriod Number of ticks for the slow SMA (e.g. 20).
     * @throws IllegalArgumentException if fastPeriod >= slowPeriod or either is <= 0.
     */
    public IndicatorCalculator(int fastPeriod, int slowPeriod) {
        if (fastPeriod <= 0 || slowPeriod <= 0) {
            throw new IllegalArgumentException("Periods must be positive.");
        }
        if (fastPeriod >= slowPeriod) {
            throw new IllegalArgumentException("fastPeriod must be less than slowPeriod.");
        }
        this.fastWindow = new double[fastPeriod];
        this.slowWindow = new double[slowPeriod];
    }

    /**
     * Feed a new price into both SMAs.
     * Evicts the oldest value from each circular buffer and adds the new one.
     *
     * @param price Latest market price.
     */
    public void update(double price) {
        // Fast SMA
        fastSum -= fastWindow[fastIdx];
        fastWindow[fastIdx] = price;
        fastSum += price;
        fastIdx = (fastIdx + 1) % fastWindow.length;
        if (fastCount < fastWindow.length) fastCount++;

        // Slow SMA
        slowSum -= slowWindow[slowIdx];
        slowWindow[slowIdx] = price;
        slowSum += price;
        slowIdx = (slowIdx + 1) % slowWindow.length;
        if (slowCount < slowWindow.length) slowCount++;
    }

    /**
     * @return Current fast SMA value.
     */
    public double getFastSMA() {
        return fastCount == 0 ? 0.0 : fastSum / fastCount;
    }

    /**
     * @return Current slow SMA value.
     */
    public double getSlowSMA() {
        return slowCount == 0 ? 0.0 : slowSum / slowCount;
    }

    /**
     * True only once the slow window is fully warmed up.
     * SignalGenerator should not emit signals before this returns true.
     *
     * @return whether both windows have enough data to produce a valid signal.
     */
    public boolean isReady() {
        return slowCount == slowWindow.length;
    }

    /**
     * Difference between fast and slow SMA.
     * Positive = fast above slow (bullish), negative = fast below slow (bearish).
     *
     * @return fast SMA minus slow SMA.
     */
    public double getSpread() {
        return getFastSMA() - getSlowSMA();
    }
}