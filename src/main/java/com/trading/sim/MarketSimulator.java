package com.trading.sim;

import com.lmax.disruptor.EventHandler;
import com.trading.api.MarketDataProvider;
import com.trading.infra.event.TradingEvent;

public class MarketSimulator implements MarketDataProvider, EventHandler<TradingEvent> {
    private final EventProducer producer;
    private final SymbolRegistry symbolRegistry;
    private final PriceGenerator priceGenerator;
    private final OrderMatcher orderMatcher;
    private volatile boolean running = false;

    public MarketSimulator(EventProducer producer, SymbolRegistry symbolRegistry) {
        this.producer = producer;
        this.symbolRegistry = symbolRegistry;
        this.priceGenerator = new PriceGenerator(symbolRegistry);
        this.orderMatcher = new OrderMatcher(symbolRegistry);
    }

    @Override
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) {
        orderMatcher.onEvent(event);
    }

    @Override
    public void start() {
        running = true;
        new Thread(this::runSimulation, "PriceTickGenerator").start();
    }

    private void runSimulation() {
        while (running) {
            symbolRegistry.getAllIds().forEach(id -> {
                double price = priceGenerator.nextPrice(id);
                producer.publish(id, price);
            });
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    public void stop() { running = false; }

    public OrderMatcher getOrderMatcher() { return orderMatcher; }
}
