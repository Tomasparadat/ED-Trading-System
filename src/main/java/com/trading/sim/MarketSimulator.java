package com.trading.sim;

import com.lmax.disruptor.EventHandler;
import com.trading.api.MarketDataProvider;
import com.trading.infra.engine.DisruptorManager;
import com.trading.infra.event.TradingEvent;

import java.util.List;

public class MarketSimulator implements MarketDataProvider, EventHandler<TradingEvent> {
    private final DisruptorManager disruptor;
    private final List<String> tickerList;
    private final SymbolRegistry symbolRegistry;
    private final PriceGenerator priceGenerator;
    private final OrderMatcher orderMatcher;


    public MarketSimulator(DisruptorManager disruptor, List<String> tickerList) {
        this.disruptor = disruptor;
        this.tickerList = tickerList;
        this.symbolRegistry = new SymbolRegistry(tickerList);
        this.priceGenerator = new PriceGenerator(symbolRegistry);
        this.orderMatcher = new OrderMatcher(symbolRegistry);
    }


    public void onValidateOrder(TradingEvent event) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void onEvent(TradingEvent event, long sequence, boolean endOfBatch) throws Exception {

    }
}
