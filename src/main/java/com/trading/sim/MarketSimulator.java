package com.trading.sim;

import com.trading.infra.event.TradingEvent;
import java.util.concurrent.locks.LockSupport;

/**
 * REFACTORED: MarketSimulator is now a PURE Producer.
 * It is NOT an EventHandler. Order matching should be a separate handler
 * downstream in the Disruptor pipeline.
 */
public class MarketSimulator implements Runnable {
    private final EventProducer producer;
    private final PriceGenerator priceGenerator;
    private final int[] symbolIds;
    private volatile boolean running = false;
    private long tickCount = 0;

    public MarketSimulator(EventProducer producer, SymbolRegistry symbolRegistry) {
        this.producer = producer;
        this.priceGenerator = new PriceGenerator(symbolRegistry);
        this.symbolIds = symbolRegistry.getAllIdsAsArray();
    }

    public void start() {
        running = true;
        Thread thread = new Thread(this, "Market-Sim-Thread");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    @Override
    public void run() {
        final int length = symbolIds.length;

        while (running) {
            for (int i = 0; i < length; i++) {
                int id = symbolIds[i];
                double price = priceGenerator.nextPriceGBM(id);

                producer.publish(id, price);
                tickCount++;
            }
        }
    }

    public void stop() { running = false; }

    public long getTickCount() { return tickCount; }
}