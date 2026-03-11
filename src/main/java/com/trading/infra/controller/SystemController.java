package com.trading.infra.controller;

import com.trading.handlers.*;
import com.trading.infra.engine.DisruptorManager;
import com.trading.infra.reporting.SessionReport;
import com.trading.portfolio.*;
import com.trading.risk.*;
import com.trading.sim.*;
import com.trading.strategy.*;

import java.util.List;

/**
 * Single point of construction, configuration, and lifecycle management
 * for the entire trading system.
 *
 * All tunable parameters are defined as constants at the top of this class.
 * To change symbols, strategy periods, risk limits, or tick speed,
 * this is the only file that needs to be modified.
 *
 * Construction order is strictly linear to resolve dependencies:
 * Registry → Portfolio → Strategy → OrderMatcher → Risk → Handlers
 * → Disruptor (init) → MarketSimulator → Disruptor (start)
 */
public class SystemController {
    private static final List<String> SYMBOLS = List.of("BTC", "ETH", "AAPL", "TSLA", "DB");
    private static final int SMA_FAST_PERIOD = 5;
    private static final int SMA_SLOW_PERIOD = 20;
    private static final double MAX_POSITION_SIZE = 1000.0;
    private static final double MAX_DAILY_LOSS = 10000.0;
    private static final double MAX_PRICE_DEV_PCT = 0.05;
    private static final int TICK_INTERVAL_MS = 10;

    private final DisruptorManager disruptorManager;
    private final MarketSimulator  marketSimulator;
    private final PortfolioTracker portfolio;
    private final SymbolRegistry registry;

    /**
     * Constructs and fully wires the trading system.
     * All components are built in dependency order and injected explicitly —
     * no component constructs its own dependencies internally.
     *
     * Construction sequence:
     * <ol>
     *   <li>SymbolRegistry — assigns integer IDs to ticker symbols.</li>
     *   <li>PortfolioTracker — tracks positions and realized PnL.</li>
     *   <li>StrategyEngine — one SMA indicator pair per symbol.</li>
     *   <li>OrderMatcher — maintains the order book and last known prices.</li>
     *   <li>RiskManager — validates orders against position, drawdown, and price rules.</li>
     *   <li>Handlers — one per pipeline stage, each filtering on its target event type.</li>
     *   <li>DisruptorManager.init() — builds the Disruptor and allocates the ring buffer.</li>
     *   <li>MarketSimulator — receives EventProducer to publish ticks into the ring buffer.</li>
     *   <li>DisruptorManager.start() — registers handlers in pipeline order and starts processing.</li>
     * </ol>
     */
    public SystemController() {
        SymbolRegistry registry = new SymbolRegistry(SYMBOLS);
        PortfolioTracker portfolio = new PortfolioTracker();
        this.registry  = registry;
        this.portfolio = portfolio;

        StrategyEngine strategyEngine = new StrategyEngine(
                SMA_FAST_PERIOD, SMA_SLOW_PERIOD, registry.size()
        );

        OrderMatcher orderMatcher = new OrderMatcher(registry);

        RiskManager riskManager = new RiskManager(
                portfolio,
                orderMatcher.getLastKnownPrices(),
                MAX_POSITION_SIZE,
                MAX_DAILY_LOSS,
                MAX_PRICE_DEV_PCT
        );

        StrategyHandler strategyHandler  = new StrategyHandler(strategyEngine);
        RiskHandler riskHandler = new RiskHandler(riskManager);
        PortfolioHandler portfolioHandler = new PortfolioHandler(portfolio);
        DbLoggerHandler dbLoggerHandler  = new DbLoggerHandler(registry);

        this.disruptorManager = new DisruptorManager(
                registry, strategyHandler, riskHandler, portfolioHandler, dbLoggerHandler
        );
        disruptorManager.init();

        EventProducer producer = disruptorManager.getProducer();
        this.marketSimulator = new MarketSimulator(producer, registry, orderMatcher, TICK_INTERVAL_MS);

        disruptorManager.start(marketSimulator);
    }

    /**
     * Starts the market simulation loop on a dedicated background thread.
     * The simulation begins publishing MARKET_TICK events into the ring buffer
     * at the configured TICK_INTERVAL_MS rate.
     * Must be called after construction is complete.
     */
    public void start() {
        marketSimulator.start();
    }

    /**
     * Stops the market simulation loop and shuts down the Disruptor.
     * The simulation thread will finish its current tick before stopping.
     * Should be called on application shutdown, typically via a shutdown hook.
     */
    public void stop() {
        marketSimulator.stop();
        disruptorManager.shutdown();
        new SessionReport(portfolio, registry).print();
    }
}