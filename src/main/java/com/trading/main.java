package com.trading;

import com.trading.domain.Side;
import com.trading.handlers.DbLoggerHandler;
import com.trading.handlers.PortfolioHandler;
import com.trading.handlers.RiskHandler;
import com.trading.handlers.StrategyHandler;
import com.trading.infra.engine.DisruptorManager;
import com.trading.portfolio.PortfolioTracker;
import com.trading.risk.RiskManager;
import com.trading.sim.MarketSimulator;
import com.trading.strategy.StrategyEngine;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        // Create domain classes
        StrategyEngine strategyEngine = new StrategyEngine();
        RiskManager riskManager = new RiskManager();
        PortfolioTracker portfolioTracker = new PortfolioTracker();

        // Create handlers, inject domain classes
        StrategyHandler strategyHandler = new StrategyHandler(strategyEngine);
        RiskHandler riskHandler = new RiskHandler(riskManager);
        PortfolioHandler portfolioHandler = new PortfolioHandler(portfolioTracker);
        DbLoggerHandler dbLoggerHandler = new DbLoggerHandler();

        //  Create and start DisruptorManager
        DisruptorManager disruptor = new DisruptorManager(
                strategyHandler,
                riskHandler,
                portfolioHandler,
                dbLoggerHandler
        );
        disruptor.start();

        // Create MarketSimulator, give it the disruptor to publish through
        MarketSimulator simulator = new MarketSimulator(disruptor);

        //Generate some market ticks
        simulator.generateTick("AAPL", 150.0, 100, Side.BUY);
        simulator.generateTick("AAPL", 151.0, 100, Side.BUY);
        simulator.generateTick("AAPL", 149.0, 50, Side.SELL);

        // events drain before shutdown
        Thread.sleep(1000);


        disruptor.shutdown();
    }
}