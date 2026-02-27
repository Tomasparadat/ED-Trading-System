package com.trading.sim;

import com.lmax.disruptor.EventHandler;
import com.trading.api.MarketDataProvider;
import com.trading.infra.engine.DisruptorManager;
import com.trading.infra.event.TradingEvent;
import java.util.List;

public class MarketSimulator implements MarketDataProvider, EventHandler<TradingEvent> {
    private final DisruptorManager disruptor;
    private final SymbolRegistry symbolRegistry;
    private final PriceGenerator priceGenerator;
    private final OrderMatcher orderMatcher;
    private volatile boolean running = false;

    public MarketSimulator(DisruptorManager disruptor, List<String> tickerList) {
        this.disruptor = disruptor;
        this.symbolRegistry = new SymbolRegistry(tickerList);
        this.priceGenerator = new PriceGenerator(symbolRegistry);
        this.orderMatcher = new OrderMatcher(symbolRegistry);
    }

    /**
     * This method is called by the Disruptor thread. It handles both MARKET_TICK (update lastPrice) and
     * NEW_ORDER (attempt order match).
     *
     * @param event TradingEvent being handled in current market tick.
     * @param sequence -
     * @param endOfBatch -
     */
    @Override
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) {
        orderMatcher.onEvent(event);
    }

    @Override
    public void start() {
        running = true;
        new Thread(this::runSimulation, "PriceTickGenerator").start();
    }

    /**
     * Executes an instance of a Simulation by calling EventProducer, SymbolRegistry, Generating prices for the ticker
     * Symbols and publishing them to the Ring Buffer.
     *
     * The Method runs on its own thread created by the start() method. The thread can be tuned to sleep X amount of
     * Millis per Tick.
     */
    private void runSimulation() {
        EventProducer producer = new EventProducer(disruptor.getRingBuffer(), symbolRegistry);

        while (running) {
            symbolRegistry.getAllIds().forEach(id -> {
                double price = priceGenerator.nextPrice(id);
                producer.publish(id, price);
            });

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    @Override
    public void stop() { running = false; }
}
