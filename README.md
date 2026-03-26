# Event-Driven Trading System

Event-driven trading system built in Java using LMAX Disruptor. Processes ~74k ticks/second with ~13μs latency/tick. Designed with zero allocation hot paths, GC avoidance and strict separation of strategy, risk and portfolio layers.

---
## System Diagram

![Event-Driven Trading System Architecture](docs/architecture.png)

## Architecture

The system uses a tri-buffer Disruptor pipeline. Each handler reads from one dedicated ring buffer and writes to the next, so no handler ever publishes back into the buffer it consumes from. Making deadlock structurally impossible.

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


### Price Model Comparison

![Geometric Brownian Motion](docs/BGM1.png)
_Session using Geometric Brownian Motion as Price Generator._

![Gaussian Random Walk](docs/RW1.png)
_Session using Gaussian Random Walk as Price Generator._

- _Note: Shown Data plotted using matplotlib + csv from CsvExporter._
---

## Components

### Simulation
- Market data generator with configurable stochastic models (GBM/ random walk).
- In-memory order matching using pre-allocated structures.

### Strategy
- SMA crossover engine with O(1) updates via circular buffers
- Signal generation triggered only on crossover events.

### Risk
- Rule-based validation (position limit, price deviation, drawdown guard)
- Fail-fast evaluation pipeline.

### Portfolio
- Real-time position tracking and PnL computation.
- Array based storage.

### Infrastructure
- Multi-buffer Disruptor pipeline orchestration (See Architecture).
- Benchmarking + CSV export for analysis.

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
| QuestDB | Not yet implemented | Time-series database  |

---

## Configuration

All tunable parameters are defined as constants at the top of `SystemController.java`, price generation must be configured in PriceGenerator.

## Notes

> **Planned improvements & Limitations:**
>
> - **QuestDB integration**: `DbLoggerHandler` currently records fills to in-memory `Ledger` and exports to CSV on shutdown. The `AsyncWriter`, `SchemaMapper`, and `DbBridge` stubs are in place and will be wired to a live QuestDB instance for real-time time-series persistence and post-session querying.
>
> - **Exchange API connection**: The system currently runs entirely on simulated market data via `MarketSimulator`. A real exchange adapter will replace it to consume live market feeds and route orders to an actual exchange via a broker API.
>
> - **Daily PnL reset**: `DrawdownGuard` currently checks total realized PnL since system start rather than within a daily window.
>
> - **Additional strategies**: `StrategyEngine` currently implements SMA crossover only. I will implement more strategies in the future to compare them under different market Price Models.