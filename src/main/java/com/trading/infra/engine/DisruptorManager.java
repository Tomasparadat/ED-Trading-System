package com.trading.infra.engine;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.trading.domain.Side;
import com.trading.handlers.DbLoggerHandler;
import com.trading.handlers.PortfolioHandler;
import com.trading.handlers.RiskHandler;
import com.trading.handlers.StrategyHandler;
import com.trading.infra.event.TradingEvent;
import com.trading.infra.event.TradingEventFactory;

public class DisruptorManager {
    private static final int BUFFER_SIZE = 1024;


    private Disruptor<TradingEvent> disruptor;
    private RingBuffer<TradingEvent> ringBuffer;

    private final PortfolioHandler portfolioHandler;
    private final StrategyHandler strategyHandler;
    private final RiskHandler riskHandler;
    private final DbLoggerHandler dbLoggerHandler;

    public DisruptorManager(
            StrategyHandler strategyHandler,
            RiskHandler riskHandler,
            PortfolioHandler portfolioHandler,
            DbLoggerHandler dbLoggerHandler
    ) {
        this.strategyHandler = strategyHandler;
        this.riskHandler = riskHandler;
        this.portfolioHandler = portfolioHandler;
        this.dbLoggerHandler = dbLoggerHandler;
    }

    public void start(){
        disruptor = new Disruptor<>(
                // Check if not use factory.
                TradingEvent::new,
                BUFFER_SIZE,
                DaemonThreadFactory.INSTANCE
        );

        disruptor.handleEventsWith(strategyHandler)
                .then(riskHandler)
                .then(portfolioHandler, dbLoggerHandler);

        ringBuffer = disruptor.start();
    }

    public void publish(TradingEvent event, String symbol, double price, double quantity, Side side){
        // Claim sequence number.
        long sequence =  ringBuffer.next();

        try {
            TradingEvent oldEvent = ringBuffer.get(sequence);

            // Clear Object to re-use.
            event.clear();

            //Re-populate Object with new fields.
            event.set(event.getOrderId(), event.getStrategyId(),
                    event.getSide(), event.getSymbol(),
                    event.getPrice(), event.getPrice(),
                    event.getType(), event.getTimestamp());

        } finally {
            // Always publish refurbished Object. finally prevents stalls.
            ringBuffer.publish(sequence);
        }
    }

    public void shutdown(){
        disruptor.shutdown();
    }
}
