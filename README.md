# Event-Driven Trading System

A low-latency algorithmic trading system built in Java using the LMAX Disruptor pattern for high-throughput event processing.

---

## Architecture

The system is built around a single Disruptor pipeline where each event flows through a chain of handlers in sequence:

```
MarketSimulator → StrategyHandler → RiskHandler → PortfolioHandler
                                                 → DbLoggerHandler
```

Each handler filters on its target event type and ignores all others.

### Event Flow

```
MARKET_TICK  →  StrategyHandler evaluates SMA crossover signal
             →  ORDER_PROPOSED if signal fires
             →  RiskHandler validates against all risk rules
             →  ORDER_FILL if approved
             →  PortfolioHandler updates positions and PnL
             →  DbLoggerHandler logs the fill
```

---

## Components

### Simulation (`com.trading.sim`)
- **MarketSimulator** — generates price ticks for each symbol on a configurable interval and delegates order matching to `OrderMatcher`
- **PriceGenerator** — random walk price simulation per symbol using Gaussian noise
- **OrderMatcher** — maintains a per-symbol order book and matches resting orders against incoming ticks
- **SymbolRegistry** — maps ticker strings to integer IDs for array-based O(1) lookups
- **EventProducer** — publishes events into the Disruptor ring buffer

### Strategy (`com.trading.strategy`)
- **StrategyEngine** — drives the SMA crossover strategy with one independent indicator per symbol
- **IndicatorCalculator** — computes fast and slow SMAs using circular buffers, O(1) per update
- **SignalGenerator** — emits BUY/SELL on crossover events, HOLD otherwise

### Risk (`com.trading.risk`)
- **RiskManager** — runs each order through all registered rules in sequence, fail-fast on first rejection
- **OrderChecker** — rejects orders deviating beyond a configurable % from last known market price
- **PositionLimitChecker** — rejects BUY orders that would exceed the max position size per symbol
- **DrawdownGuard** — halts BUY orders when total realized PnL falls below the max daily loss threshold

### Portfolio (`com.trading.portfolio`)
- **PortfolioTracker** — entry point for fill events, coordinates position updates and PnL calculation
- **PositionManager** — maintains a map of open positions keyed by symbol ID
- **Position** — tracks quantity, average entry price, and realized PnL per symbol
- **PnLCalculator** — accumulates realized PnL per symbol and in total
- **Ledger** — immutable trade history stored as `LedgerRecord` snapshots to prevent ring buffer mutation bugs

### Infrastructure (`com.trading.infra`)
- **DisruptorManager** — builds and manages the LMAX Disruptor and ring buffer lifecycle
- **SystemController** — single point of construction, configuration, and lifecycle management
- **SessionReport** — prints a full session summary on shutdown including trade log, positions, and PnL

---

## Configuration

All tunable parameters are defined as constants at the top of `SystemController.java`:

| Parameter | Default | Description |
|---|---|---|
| `SYMBOLS` | BTC, ETH, AAPL, TSLA, DB | Tracked symbols |
| `SMA_FAST_PERIOD` | 5 | Fast SMA window (ticks) |
| `SMA_SLOW_PERIOD` | 20 | Slow SMA window (ticks) |
| `MAX_POSITION_SIZE` | 1000.0 | Max quantity per symbol |
| `MAX_DAILY_LOSS` | 10,000.0 | Drawdown halt threshold |
| `MAX_PRICE_DEV_PCT` | 0.05 | Max price deviation (5%) |
| `TICK_INTERVAL_MS` | 10 | Milliseconds between ticks |

---

## Running

```java
// Main
SystemController system = new SystemController();
system.start();

Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    system.stop();
}));
```

Press `Ctrl+C` to stop. A session report is printed automatically on shutdown.

### Sample Output

```
[FILL] orderId=1     | symbol=BTC  | side=BUY  | price=  100.17 | qty=  10.0 | strategyId=1
[FILL] orderId=2     | symbol=ETH  | side=SELL | price=   99.23 | qty=  10.0 | strategyId=1

========================================
          SESSION REPORT
========================================

--- Trade Log (42 fills) ---
  orderId  symbol side  price      qty      timestamp
  ----------------------------------------------------------------------
  1        BTC    BUY   100.17     10.0     2026-03-11 11:54:09.123
  2        ETH    SELL  99.23      10.0     2026-03-11 11:54:09.234

--- Final Positions ---
  BTC    | qty:     30.00
  ETH    | qty:     10.00

--- Realized PnL by Symbol ---
  BTC    | pnl:    +142.30
  ETH    | pnl:     -23.10

--- Total ---
  Total Fills:        42
  Total Realized PnL: +119.20
========================================
```

---

## Dependencies

| Library | Version             | Purpose |
|---|---------------------|---|
| LMAX Disruptor | 4.0.0               | Lock-free ring buffer event processing |
| QuestDB | Not yet implemented | Time-series database (not yet connected) |

---

## Notes

> **Known limitations and planned improvements:**
>
> - **Event mutation** — handlers currently mutate the `TradingEvent` in place as it flows through the pipeline rather than publishing new ring buffer slots per stage. This limits throughput to one fill per tick and prevents concurrent order processing. To be refactored to a publish/read model where each handler publishes a new event for the next stage.
>
> - **QuestDB integration** — `DbLoggerHandler` currently logs to stdout. The `AsyncWriter`, `SchemaMapper`, and `DbBridge` stubs are in place and will be wired to a live QuestDB instance for time-series persistence and post-session querying.
>
> - **Exchange API connection** — the system currently runs entirely on simulated market data. A real exchange adapter will replace `MarketSimulator` to consume live market feeds and route orders to an actual exchange via a broker API.
>
> - **Daily PnL reset** — `DrawdownGuard` currently checks total realized PnL since system start rather than daily PnL. `PnLCalculator.resetDaily()` is implemented and ready to be scheduled at session boundaries.