# Event-Driven Trading System

> **For recruiters:** A personal project built to explore low-latency system design in Java. The system implements a fully event-driven trading pipeline using the LMAX Disruptor to process thousands of events per second. It covers the full lifecycle of a trade: market data simulation, signal generation via a moving average crossover strategy, multi-rule risk validation, position tracking, and PnL calculation. The codebase prioritises separation of concerns, low GC usage and zero-allocation on the hot path using primitive array indexing throughout most components. Infrastructure stubs for QuestDB persistence and a live exchange connection are in place for future development.

# Event-Driven Trading System

A low-latency algorithmic trading system built in Java using the LMAX Disruptor pattern for high-throughput event processing.

**Note**: _The purpose of this project is to learn about how HFT Trading Engines, Event-Driven Architecture and Mechanical Sympathy work. As well as to learn about Price Models and Strategy along the way :)._

---
## System Diagram

![Event-Driven Trading System Architecture](docs/architecture.png)

## Architecture

The system uses a tri-buffer Disruptor pipeline. Each handler reads from one dedicated ring buffer and writes to the next, so no handler ever publishes back into the buffer it consumes from making deadlock structurally impossible.

```
MarketSimulator
      │
      ▼
 TickBuffer        (MARKET_TICK events)
      │
      ▼
StrategyHandler --> OrderBuffer       (ORDER_PROPOSED events)
                          │
                          ▼
                    RiskHandler --> FillBuffer        (ORDER_FILL events)
                                         │
                                         ▼
                                   PortfolioHandler
                                   DbLoggerHandler   (simultaneous)
```
_Event Pipeline_


### Event Flow

```
MARKET_TICK  →  StrategyHandler evaluates SMA crossover signal
             →  publishes ORDER_PROPOSED to OrderBuffer if signal fires
             →  RiskHandler validates against all risk rules
             →  publishes ORDER_FILL to FillBuffer if approved
             →  PortfolioHandler updates positions and PnL
             →  DbLoggerHandler records the fill
```

### Price Model Comparison

![Geometric Brownian Motion](docs/BGM1.png)
_Session using Geometric Brownian Motion as Price Generator._

![Gaussian Random Walk](docs/RW1.png)
_Session using Gaussian Random Walk as Price Generator._

- _Note: Shown Data plotted using matplotlib + csv from CsvExporter._
---

## Components

### Simulation (`com.trading.sim`)
- **MarketSimulator**: Generates price ticks for each symbol on a configurable interval and delegates order matching to `OrderMatcher`.
- **PriceGenerator**: Random walk price simulation per symbol using Gaussian noise or Geometric Brownian Motion (depending on configuration) with configurable volatility.
- **OrderMatcher**: Maintains a per-symbol order book using pre-allocated arrays, matches resting orders against incoming ticks.
- **SymbolRegistry**: Maps ticker strings to integer IDs for array-based O(1) lookups, backed by a `HashMap` for O(1) reverse lookup.
- **EventProducer**: Publishes `MARKET_TICK`, `ORDER_PROPOSED`, and `ORDER_FILL` events into their respective ring buffers.

### Strategy (`com.trading.strategy`)
- **StrategyEngine**: Drives the SMA crossover strategy with one independent `IndicatorCalculator` and `SignalGenerator` per symbol, backed by primitive arrays for zero boxing overhead.
- **IndicatorCalculator**: Computes fast and slow SMAs using circular buffers, O(1) per update with no heap allocation.
- **SignalGenerator**: Emits `BUY` on fast-above-slow crossover, `SELL` on fast-below-slow crossover, `HOLD` otherwise. Fires only on the exact tick a crossover occurs


### Risk (`com.trading.risk`)
- **RiskManager**: Runs each order through all registered rules in sequence, fail on first rejection. Thresholds injected from `SystemController`.
- **OrderChecker**: Rejects orders deviating beyond a configurable % from the last known market price, shared via `OrderMatcher.getLastKnownPrices()`.
- **PositionLimitChecker**: Rejects BUY orders that would exceed the max position size per symbol. SELLs always pass.
- **DrawdownGuard**: Halts BUY orders when total realized PnL falls below the max daily loss threshold. SELLs always pass.

### Portfolio (`com.trading.portfolio`)
- **PortfolioTracker**: Entry point for fill events, coordinates position updates, PnL calculation, and ledger recording.
- **PositionManager**: Maintains positions in a primitive array indexed by symbolId for O(1) access.
- **Position**: Tracks quantity, average entry price, and realized PnL per symbol. Uses `consumeLastTradeRealizedPnL()` to prevent double-counting.
- **PnLCalculator**: Accumulates realized PnL per symbol and in total using primitive arrays, no boxing overhead.
- **Ledger**: Append-only trade history stored as immutable `LedgerRecord` snapshots.

### Infrastructure (`com.trading.infra`)
- **MultiBufferDisruptorManager**: Builds and manages three independent Disruptors and their ring buffers.
- **SystemController**: Single point of construction, configuration, and lifecycle management for the entire system.
- **SessionReport**: Prints a session summary on shutdown including final positions, per-symbol PnL, and totals.
- **CsvExporter**: Exports the full trade history to a timestamped CSV file on shutdown. Useful for matplotlib analysis.

### Benchmarking (`com.trading`)
- **Benchmark** — measures end-to-end throughput (ticks/second, fills/second, avg latency per tick) over a configurable duration with JIT warmup phase.

---

## Configuration

All tunable parameters are defined as constants at the top of `SystemController.java`:

_**Note**: Starting Price, volatility, deviation (Brownian Motion), tics per second (Brownian Motion) & Delta Time (Brownian Motion) are only configurable through PriceGenerator constants._

| Parameter           | Default               | Description                          |
|---------------------|-----------------------|--------------------------------------|
| `SYMBOLS`           | BTC, ETH, AAPL, TSLA, DB | Tracked symbols                  |
| `SMA_FAST_PERIOD`   | 5                     | Fast SMA window (ticks)              |
| `SMA_SLOW_PERIOD`   | 20                    | Slow SMA window (ticks)              |
| `MAX_POSITION_SIZE` | 1000.0                | Max quantity per symbol              |
| `MAX_DAILY_LOSS`    | 10,000.0              | Drawdown halt threshold              |
| `MAX_PRICE_DEV_PCT` | 0.05                  | Max order price deviation (5%)       |
| `TICK_INTERVAL_MS`  | 10                    | Milliseconds between tick rounds     |


---

## Running

```java
// Main
    public static void main(String[] args) throws InterruptedException {
    SystemController systemController = new SystemController();
    systemController.start();

    // Trading System execution time.
    Thread.sleep(10_000);

    systemController.stop();
}
```

Press `Ctrl+C` to stop. A session report is printed automatically on shutdown.

### Sample Output

```
========================================
          SESSION REPORT                
========================================

--- Trade Log (showing last 10 of 70692 fills) ---
  70683    TSLA   SELL  78.29      10.0     2026-03-25 18:00:17.311
  70684    BTC    BUY   53.49      10.0     2026-03-25 18:00:17.311
  70685    AAPL   BUY   88.85      10.0     2026-03-25 18:00:17.311
  70686    TSLA   BUY   77.92      10.0     2026-03-25 18:00:17.313
  70687    AAPL   SELL  88.93      10.0     2026-03-25 18:00:17.313
  70688    BTC    SELL  53.57      10.0     2026-03-25 18:00:17.313
  70689    ETH    SELL  74.11      10.0     2026-03-25 18:00:17.313
  70690    DB     SELL  62.33      10.0     2026-03-25 18:00:17.313
  70691    BTC    BUY   53.63      10.0     2026-03-25 18:00:17.314

--- Final Positions ---
  BTC    | qty:    10.00
  ETH    | qty:     0.00
  AAPL   | qty:     0.00
  TSLA   | qty:    10.00
  DB     | qty:     0.00

--- Realized PnL by Symbol ---
  BTC    | pnl:    -209.01
  ETH    | pnl:     -41.82
  AAPL   | pnl:     +85.91
  TSLA   | pnl:     -92.15
  DB     | pnl:    -244.37

--- Total ---
  Total Ticks: 1116605
  Total Fills: 70692
  Fill Rate: 6.3310%
  Total Realized PnL: -501.44
========================================

==========================================
  Ticks processed:    1,116,605
  Fills executed:      70,692
  Ticks/second:        74,439
  Avg latency/tick:    13.434 µs
==========================================
```

---

## Dependencies

| Library | Version             | Purpose |
|---|---------------------|---|
| LMAX Disruptor | 4.0.0               | Lock-free ring buffer event processing |
| QuestDB | Not yet implemented | Time-series database (not yet connected) |

---

## Notes

> **Planned improvements & Limitations:**
>
> - **QuestDB integration**: `DbLoggerHandler` currently records fills to an in-memory `Ledger` and exports to CSV on shutdown. The `AsyncWriter`, `SchemaMapper`, and `DbBridge` stubs are in place and will be wired to a live QuestDB instance for real-time time-series persistence and post-session querying.
>
> - **Exchange API connection**: The system currently runs entirely on simulated market data via `MarketSimulator`. A real exchange adapter will replace it to consume live market feeds and route orders to an actual exchange via a broker API. Connection will be most likely delayed since my main focus right now is experimenting with different Price Models and see how they react with different strategies.
>
> - **Daily PnL reset**: `DrawdownGuard` currently checks total realized PnL since system start rather than within a daily window. `PnLCalculator.resetDaily()` is implemented and ready to be scheduled at session boundaries.
>
> - **Additional strategies**: `StrategyEngine` currently implements SMA crossover only. I will implement more strategies in the future to compare them under different market Price Models.