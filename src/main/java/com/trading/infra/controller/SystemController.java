package com.trading.infra.controller;

import com.trading.handlers.*;
import com.trading.infra.engine.MultiBufferDisruptorManager;
import com.trading.infra.reporting.CsvExporter;
import com.trading.infra.reporting.SessionReport;
import com.trading.portfolio.*;
import com.trading.risk.*;
import com.trading.sim.*;
import com.trading.strategy.*;

import java.util.List;


public class SystemController {
    private static final List<String> SYMBOLS = List.of("BTC", "ETH", "AAPL", "TSLA", "DB");
    private static final int SMA_FAST_PERIOD = 5;
    private static final int SMA_SLOW_PERIOD = 20;
    private static final double MAX_POSITION_SIZE = 1000.0;
    private static final double MAX_DAILY_LOSS = 10000.0;
    private static final double MAX_PRICE_DEV_PCT = 0.05;
    private static final int TICK_INTERVAL_MS = 0;

    private final MultiBufferDisruptorManager disruptorManager;
    private final MarketSimulator marketSimulator;
    private final PortfolioTracker portfolio;
    private final SymbolRegistry registry;
    private final OrderMatcher orderMatcher;
    private final StrategyEngine strategyEngine;


    public SystemController() {
        this.registry = new SymbolRegistry(SYMBOLS);
        this.portfolio = new PortfolioTracker(registry.size());

        this.strategyEngine = new StrategyEngine(SMA_FAST_PERIOD, SMA_SLOW_PERIOD, registry.size());
        this.orderMatcher = new OrderMatcher(registry);
        RiskManager riskManager = new RiskManager(
                portfolio,
                orderMatcher.getLastKnownPrices(),
                MAX_POSITION_SIZE,
                MAX_DAILY_LOSS,
                MAX_PRICE_DEV_PCT
        );

        this.disruptorManager = new MultiBufferDisruptorManager(registry);
        disruptorManager.init();

        EventProducer tickProducer = disruptorManager.getTickProducer();
        EventProducer orderProducer = disruptorManager.getOrderProducer();
        EventProducer fillProducer = disruptorManager.getFillProducer();

        RiskHandler riskHandler = new RiskHandler(riskManager, fillProducer);
        StrategyHandler strategyHandler = new StrategyHandler(strategyEngine, orderProducer);
        PortfolioHandler portfolioHandler = new PortfolioHandler(portfolio);
        DbLoggerHandler dbLoggerHandler = new DbLoggerHandler(registry);

        this.marketSimulator = new MarketSimulator(tickProducer, registry);

        disruptorManager.start(strategyHandler, riskHandler, orderMatcher ,portfolioHandler, dbLoggerHandler);
    }

    public void start() {
        marketSimulator.start();
    }

    /**
     * Stops the market simulation loop and shuts down the Disruptor.
     * The simulation thread will finish its current tick before stopping.
     */
    public void stop() {
        marketSimulator.stop();
        disruptorManager.shutdown();

        new CsvExporter(portfolio.getLedger(), registry, "trades_" + System.currentTimeMillis() + ".csv").export();
        new SessionReport(portfolio, registry, marketSimulator.getTickCount()).print();
    }

    public long getTickCount() {
        return marketSimulator.getTickCount();
    }

    public long getFillCount() {
        return portfolio.getLedger().getTotalTrades();
    }
}