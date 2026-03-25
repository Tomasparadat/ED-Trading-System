package com.trading;

import com.trading.infra.controller.SystemController;
import java.util.concurrent.TimeUnit;

/**
 * Simple throughput benchmark. Runs the full trading pipeline for a fixed
 * duration and reports ticks per second and average latency per tick.
 */
public class Benchmark {
    private static final int WARMUP_SECONDS = 5;
    private static final int MEASURE_SECONDS = 30;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Warming up...");
        SystemController warmup = new SystemController();
        warmup.start();
        Thread.sleep(TimeUnit.SECONDS.toMillis(WARMUP_SECONDS));
        warmup.stop();


        System.out.println("Measuring...\n");
        SystemController system = new SystemController();

        long startNanos = System.nanoTime();
        system.start();
        Thread.sleep(TimeUnit.SECONDS.toMillis(MEASURE_SECONDS));
        long endNanos = System.nanoTime();

        system.stop();

        long ticks = system.getTickCount();
        long fills = system.getFillCount();
        double elapsedSeconds = (endNanos - startNanos) / 1_000_000_000.0;
        double ticksPerSec = ticks / elapsedSeconds;
        double microsPerTick = (endNanos - startNanos) / 1_000.0 / ticks;

        System.out.println("==========================================");
        System.out.printf("  Ticks processed:    %,d%n", ticks);
        System.out.printf("  Fills executed:      %,d%n", fills);
        System.out.printf("  Ticks/second:        %,.0f%n", ticksPerSec);
        System.out.printf("  Avg latency/tick:    %.3f µs%n", microsPerTick);
        System.out.println("==========================================");
    }
}