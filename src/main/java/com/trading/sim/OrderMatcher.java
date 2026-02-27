package com.trading.sim;

import com.trading.domain.EventType;
import com.trading.domain.Side;
import com.trading.infra.event.TradingEvent;

import java.util.*;

public class OrderMatcher {
    private final double[] lastKnownPrices;
    private final SymbolRegistry registry;
    private final Map<Integer, List<OpenOrder>> book = new HashMap<>();
    private record OpenOrder(long orderId, int symbolId, double price, double quantity, Side side) {}

    /**
     * OrderMatcher constructor
     *
     * @param registry SymbolRegistry where all Ticker symbols that live in this instance of the market are stored.
     */
    public OrderMatcher(SymbolRegistry registry) {
        this.registry = registry;
        this.lastKnownPrices = new double[registry.size()];
    }

    /**
     * Checks for TradingEvent Type and calls handleTick or handleNewOrder depending on it's value.
     *
     * @param event TradingEvent that's being evaluated.
     */
    public void onEvent(TradingEvent event) {
        switch (event.getType()) {
            case MARKET_TICK -> handleTick(event);
            case NEW_ORDER -> handleNewOrder(event);
        }
    }

    /**
     * Handles MARKET_TICK event types. Method checks if there are any orders or the tick is empty, once the check
     * is passed it iterates through orders Bucket corresponding to the tick id. Where it then checks for an order match
     * which it proceeds to fill if the requirements are met.
     *
     * @param tick TradingEvent being Evaluated, it's EventType get's evaluated in onEvent().
     */
    private void handleTick(TradingEvent tick) {
        int id = tick.getSymbolId();
        double currentPrice = tick.getPrice();
        lastKnownPrices[id] = currentPrice;

        List<OpenOrder> orders = book.get(id);
        if (orders == null || orders.isEmpty()) return;


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
     * Updates TradingEvent attributes. transforming it into a ORDER_FILL and passing the orderId, price and quantity
     * as it's new values.
     *
     * @param event TradingEvent Object being recycled.
     * @param order OpenOrder being matched.
     */
    private void applyFill(TradingEvent event, OpenOrder order) {
        event.setType(EventType.ORDER_FILL);
        event.setOrderId(order.orderId());
        event.setPrice(order.price());
        event.setQuantity(order.quantity());
        event.setSide(order.side());
    }

    /**
     * Checks if MarketPrice equals or is better than ask Price.
     *
     * @param order OpenOrder being evaluated for a fill.
     * @param marketPrice current MarketPrice of the underlying asset.
     * @return true if marketPrice equals or is better than order Price, false if the contrary is the case.
     */
    private boolean isMatch(OpenOrder order, double marketPrice) {
        if (order.side() == Side.BUY) {
            return marketPrice <= order.price();
        } else {
            return marketPrice >= order.price();
        }
    }


    /**
     * Create OpenOrder Record to persist in book map. This prevents losing the data to the Ring-Buffer when it recycles
     * the TradingEvent Object. If the book is absent, the method creates a new ArrayList where it adds the
     * persistentOrder, this avoids
     * NullPointerExceptions.
     *
     * @param event that needs to be copied as a record in order to persist it.
     */
    private void handleNewOrder(TradingEvent event) {
        OpenOrder persistentOrder = new OpenOrder(
                event.getOrderId(),
                event.getSymbolId(),
                event.getPrice(),
                event.getQuantity(),
                event.getSide()
        );

        book.computeIfAbsent(persistentOrder.symbolId(), k -> new ArrayList<>()).add(persistentOrder);
    }
}
