package com.trading.sim;

import com.lmax.disruptor.EventHandler;
import com.trading.api.MarketDataProvider;
import com.trading.infra.event.TradingEvent;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

public class MarketSimulator implements MarketDataProvider, EventHandler<TradingEvent> {
    private final EventProducer producer;
    private final SymbolRegistry symbolRegistry;
    private final PriceGenerator priceGenerator;
    private final OrderMatcher orderMatcher;
    private final int tickIntervalMs;
    private volatile boolean running = false;
    private final AtomicLong tickCount = new AtomicLong(0);

    /**
     * Constructs a MarketSimulator with all dependencies injected.
     * OrderMatcher is injected rather than constructed internally so that its
     * lastKnownPrices array can be shared with RiskManager via SystemController.
     *
     * @param producer EventProducer for publishing tick events into the ring buffer.
     * @param symbolRegistry Registry of all tracked symbols and their IDs.
     * @param orderMatcher Shared OrderMatcher, also referenced by RiskManager.
     * @param tickIntervalMs Milliseconds to sleep between each round of price ticks.
     */
    public MarketSimulator(EventProducer producer, SymbolRegistry symbolRegistry, OrderMatcher orderMatcher, int tickIntervalMs) {
        this.producer = producer;
        this.symbolRegistry = symbolRegistry;
        this.priceGenerator = new PriceGenerator(symbolRegistry);
        this.orderMatcher = orderMatcher;
        this.tickIntervalMs = tickIntervalMs;
    }

    /**
     * Called by the Disruptor thread on every event in the ring buffer.
     * Delegates to OrderMatcher to handle MARKET_TICK and NEW_ORDER events.
     *
     * @param event TradingEvent from the ring buffer.
     * @param sequence Ring buffer sequence number.
     * @param endOfBatch Whether this is the last event in the current batch.
     */
    @Override
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) {
        orderMatcher.onEvent(event);
    }

    /**
     * Starts the price tick generator on a dedicated background thread.
     * The thread publishes a MARKET_TICK for every symbol each interval.
     */
    @Override
    public void start() {
        running = true;
        new Thread(this::runSimulation, "PriceTickGenerator").start();
    }

    /**
     * Main simulation loop. Generates a new price for each registered symbol
     * and publishes it into the ring buffer, then sleeps for tickIntervalMs.
     * Stops cleanly when running is set to false via stop().
     *
     * priceGenerator determines if Arithmetic Random Walk or GBM is being used for price generation.
     */
    private void runSimulation() {
        while (running) {
            symbolRegistry.getAllIds().forEach(id -> {
                double price = priceGenerator.nextPriceGBM(id);
                producer.publish(id, price);
                tickCount.incrementAndGet();
            });

            LockSupport.parkNanos(1_000);
        }
    }

    public long getTickCount(){
        return tickCount.get();
    }

    /**
     * Signals the simulation loop to stop after the current tick completes.
     */
    @Override
    public void stop() { running = false; }

}
