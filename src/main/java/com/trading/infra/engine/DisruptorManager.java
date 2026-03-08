package com.trading.infra.engine;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.trading.handlers.DbLoggerHandler;
import com.trading.handlers.PortfolioHandler;
import com.trading.handlers.RiskHandler;
import com.trading.handlers.StrategyHandler;
import com.trading.infra.event.TradingEvent;
import com.trading.infra.event.TradingEventFactory;
import com.trading.sim.EventProducer;
import com.trading.sim.MarketSimulator;
import com.trading.sim.SymbolRegistry;

public class DisruptorManager {
    private static final int BUFFER_SIZE = 1024;


    private Disruptor<TradingEvent> disruptor;
    private RingBuffer<TradingEvent> ringBuffer;

    private final PortfolioHandler portfolioHandler;
    private final StrategyHandler strategyHandler;
    private final RiskHandler riskHandler;
    private final DbLoggerHandler dbLoggerHandler;
    private final SymbolRegistry symbolRegistry;

    public DisruptorManager(
            SymbolRegistry symbolRegistry,
            StrategyHandler strategyHandler,
            RiskHandler riskHandler,
            PortfolioHandler portfolioHandler,
            DbLoggerHandler dbLoggerHandler
    ) {
        this.symbolRegistry = symbolRegistry;
        this.strategyHandler = strategyHandler;
        this.riskHandler = riskHandler;
        this.portfolioHandler = portfolioHandler;
        this.dbLoggerHandler = dbLoggerHandler;
    }

    /**
     * Initializes the disruptor using the TradingEventFactory() and sets the order of handling Events:
     * marketSimulator -> strategyHandler -> riskHandler -> portfolioHandler, dbLoggerHandler (simultaneous handling).
     */
    public void start(MarketSimulator marketSimulator) {
        disruptor = new Disruptor<>(
                new TradingEventFactory(),
                BUFFER_SIZE,
                DaemonThreadFactory.INSTANCE
        );

        disruptor.handleEventsWith(marketSimulator)
                .then(strategyHandler)
                .then(riskHandler)
                .then(portfolioHandler, dbLoggerHandler);

        ringBuffer = disruptor.start();
    }

    public void shutdown(){
        disruptor.shutdown();
    }

    public EventProducer getProducer() {
        return new  EventProducer(disruptor.getRingBuffer(), symbolRegistry);
    }

    public RingBuffer<TradingEvent> getRingBuffer() {
        return ringBuffer;
    }
}
