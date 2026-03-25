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

public class MultiBufferDisruptorManager {
    private static final int BUFFER_SIZE = 65536;

    private Disruptor<TradingEvent> tickDisruptor;
    private Disruptor<TradingEvent> orderDisruptor;
    private Disruptor<TradingEvent> fillDisruptor;

    private RingBuffer<TradingEvent> tickBuffer;
    private RingBuffer<TradingEvent> orderBuffer;
    private RingBuffer<TradingEvent> fillBuffer;

    private final SymbolRegistry symbolRegistry;
    private final StrategyHandler strategyHandler;
    private final RiskHandler riskHandler;
    private final PortfolioHandler portfolioHandler;
    private final DbLoggerHandler dbLoggerHandler;

    public MultiBufferDisruptorManager(
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

    public void init() {
        tickDisruptor = new Disruptor<>(new TradingEventFactory(), BUFFER_SIZE, DaemonThreadFactory.INSTANCE);
        orderDisruptor = new Disruptor<>(new TradingEventFactory(), BUFFER_SIZE, DaemonThreadFactory.INSTANCE);
        fillDisruptor = new Disruptor<>(new TradingEventFactory(), BUFFER_SIZE, DaemonThreadFactory.INSTANCE);

        tickBuffer = tickDisruptor.getRingBuffer();
        orderBuffer = orderDisruptor.getRingBuffer();
        fillBuffer = fillDisruptor.getRingBuffer();
    }

    /**
     * Registers all handlers, wires cross-buffer producers, and starts all Disruptors.
     * Must be called after init() and after all handlers have been constructed
     * with their correct EventProducer references.
     *
     * @param marketSimulator First handler in the pipeline, reads from TickBuffer.
     */
    public void start(MarketSimulator marketSimulator, StrategyHandler strategyHandler, RiskHandler riskHandler) {
        tickDisruptor.handleEventsWith(marketSimulator, strategyHandler);
        orderDisruptor.handleEventsWith(riskHandler);
        fillDisruptor.handleEventsWith(portfolioHandler, dbLoggerHandler);

        fillBuffer = fillDisruptor.start();
        orderBuffer = orderDisruptor.start();
        tickBuffer = tickDisruptor.start();
    }

    /**
     * Returns an EventProducer that publishes MARKET_TICK events into TickBuffer.
     * Used by MarketSimulator to inject price updates into the pipeline.
     *
     * @return EventProducer backed by TickBuffer.
     */
    public EventProducer getTickProducer() {
        return new EventProducer(tickBuffer, symbolRegistry);
    }

    /**
     * Returns an EventProducer that publishes ORDER_FILL events into FillBuffer.
     * Injected into RiskHandler so it can forward approved orders downstream.
     *
     * @return EventProducer backed by FillBuffer.
     */
    public EventProducer getFillProducer() {
        return new EventProducer(fillBuffer, symbolRegistry);
    }

    /**
     * Returns an EventProducer that publishes ORDER_PROPOSED events into OrderBuffer.
     * Injected into StrategyHandler so it can forward approved signals downstream.
     *
     * @return EventProducer backed by OrderBuffer.
     */
    public EventProducer getOrderProducer() {
        return new EventProducer(orderBuffer, symbolRegistry);
    }

    /**
     * Shuts down all three Disruptors cleanly.
     */
    public void shutdown() {
        tickDisruptor.shutdown();
        orderDisruptor.shutdown();
        fillDisruptor.shutdown();
    }
}
