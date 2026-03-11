package com.trading.sim;

import com.trading.domain.EventType;
import com.trading.domain.Side;
import com.trading.infra.event.TradingEvent;

import java.util.*;

public class OrderMatcher {
    private final double[] lastKnownPrices;
    private final SymbolRegistry registry;
    private final List<OpenOrder>[] book;

    private record OpenOrder(long orderId, int symbolId, double price, double quantity, Side side) {}

    /**
     * Constructs an OrderMatcher for the given symbol registry.
     * Pre-allocates one order bucket per symbol to avoid runtime allocation.
     *
     * @param registry SymbolRegistry defining all valid symbols and their IDs.
     */
    @SuppressWarnings("unchecked")
    public OrderMatcher(SymbolRegistry registry) {
        this.registry = registry;
        this.lastKnownPrices = new double[registry.size()];
        this.book = new List[registry.size()];

        for (int i = 0; i < registry.size(); i++) {
            book[i] = new ArrayList<>();
        }
    }

    /**
     * Entry point called by MarketSimulator on each event.
     * Routes to the correct handler based on event type.
     * Unrecognised types are ignored.
     *
     * @param event TradingEvent received from the ring buffer.
     */
    public void onEvent(TradingEvent event) {
        switch (event.getType()) {
            case MARKET_TICK -> handleTick(event);
            case NEW_ORDER -> handleNewOrder(event);
        }
    }

    /**
     * Handles a MARKET_TICK event. Updates the last known price for the symbol,
     * then scans the order book for the first resting order that can be filled
     * at the current market price.
     *
     * NOTE: Only one fill is processed per tick. The event is mutated in place
     * to become an ORDER_FILL, which means a second fill would overwrite the first
     * in the same ring buffer slot. This is a known limitation.
     * TODO: Refactor to publish a new ring buffer slot per fill when scaling up.
     *
     * @param tick The incoming MARKET_TICK TradingEvent.
     */
    private void handleTick(TradingEvent tick) {
        int id = tick.getSymbolId();
        double currentPrice = tick.getPrice();
        lastKnownPrices[id] = currentPrice;

        List<OpenOrder> orders = book[id];
        if (orders.isEmpty()) return;

        Iterator<OpenOrder> iterator = orders.iterator();
        while (iterator.hasNext()) {
            OpenOrder order = iterator.next();
            if (isMatch(order, currentPrice)) {
                applyFill(tick, order);
                iterator.remove();
                return;
            }
        }
    }

    /**
     * Mutates the TradingEvent in place, transforming it from a MARKET_TICK
     * into an ORDER_FILL with the matched order's attributes.
     *
     * @param event TradingEvent to mutate.
     * @param order Matched OpenOrder whose values are applied to the event.
     */
    private void applyFill(TradingEvent event, OpenOrder order) {
        event.setType(EventType.ORDER_FILL);
        event.setOrderId(order.orderId());
        event.setPrice(order.price());
        event.setQuantity(order.quantity());
        event.setSide(order.side());
    }

    /**
     * Determines whether a resting order can be filled at the current market price.
     * A BUY order matches when the market price is at or below the order price.
     * A SELL order matches when the market price is at or above the order price.
     *
     * @param order The resting OpenOrder to evaluate.
     * @param marketPrice The current market price of the underlying asset.
     * @return true if the order should be filled, false otherwise.
     */
    private boolean isMatch(OpenOrder order, double marketPrice) {
        return order.side() == Side.BUY
                ? marketPrice <= order.price()
                : marketPrice >= order.price();
    }

    /**
     * Persists an incoming NEW_ORDER event as an OpenOrder record in the order book.
     * Copying into an immutable record prevents data loss when the ring buffer
     * recycles the TradingEvent slot.
     *
     * @param event The NEW_ORDER TradingEvent to persist.
     */
    private void handleNewOrder(TradingEvent event) {
        OpenOrder order = new OpenOrder(
                event.getOrderId(),
                event.getSymbolId(),
                event.getPrice(),
                event.getQuantity(),
                event.getSide()
        );
        book[order.symbolId()].add(order);
    }

    /**
     * Returns the last known market price array, shared with OrderChecker
     * for price deviation validation in the risk layer.
     * Updated on every MARKET_TICK.
     *
     * @return Array of last known prices indexed by symbolId.
     */
    public double[] getLastKnownPrices() {
        return lastKnownPrices;
    }
}