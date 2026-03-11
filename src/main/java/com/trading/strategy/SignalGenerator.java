package com.trading.strategy;

/**
 * Generates trading signals based on SMA crossover from IndicatorCalculator.
 * Emits BUY on fast crossing above slow, SELL on fast crossing below slow.
 * Emits HOLD on all other ticks, including during warmup.
 */
public class SignalGenerator {
    private boolean prevFastAboveSlow = false;
    private boolean warmedUp = false;

    /**
     * Evaluates the current indicator state and returns a signal.
     * Only emits BUY/SELL on the exact tick a crossover occurs.
     *
     * @param calc IndicatorCalculator with updated price data.
     * @return BUY, SELL, or HOLD.
     */
    public SignalType check(IndicatorCalculator calc) {
        if (!calc.isReady()) {
            return SignalType.HOLD;
        }

        boolean fastAboveSlow = calc.getFastSMA() > calc.getSlowSMA();

        if (!warmedUp) {
            prevFastAboveSlow = fastAboveSlow;
            warmedUp = true;
            return SignalType.HOLD;
        }

        SignalType signal = SignalType.HOLD;

        if (fastAboveSlow && !prevFastAboveSlow) {
            // fast crossed above slow
            signal = SignalType.BUY;
        } else if (!fastAboveSlow && prevFastAboveSlow) {
            // fast crossed below slow
            signal = SignalType.SELL;
        }

        prevFastAboveSlow = fastAboveSlow;
        return signal;
    }
}