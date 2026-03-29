package com.trading.sim;

import com.lmax.disruptor.EventHandler;
import com.trading.domain.EventType;
import com.trading.domain.Side;
import com.trading.infra.event.TradingEvent;

import java.util.*;

public class OrderMatcher implements EventHandler<TradingEvent> {
    private final double[] lastKnownPrices;
    private final SymbolRegistry registry;
    private final List<OpenOrder>[] book;

    @Override
    public void onEvent(TradingEvent event, long l, boolean b) throws Exception {
        switch (event.getType()) {
            case MARKET_TICK -> handleTick(event);
            case NEW_ORDER -> handleNewOrder(event);
        }
    }

    private record OpenOrder(long orderId, int symbolId, double price, double quantity, Side side) {}

    @SuppressWarnings("unchecked")
    public OrderMatcher(SymbolRegistry registry) {
        this.registry = registry;
        this.lastKnownPrices = new double[registry.size()];
        this.book = new List[registry.size()];

        for (int i = 0; i < registry.size(); i++) {
            book[i] = new ArrayList<>();
        }
    }


    private void handleTick(TradingEvent tick) {
        int id = tick.getSymbolId();
        double currentPrice = tick.getPrice();
        lastKnownPrices[id] = currentPrice;

        List<OpenOrder> orders = book[id];
        if (orders.isEmpty()) return;

        for(int i = 0; i < orders.size(); i++) {
            OpenOrder order = orders.get(i);
            if (isMatch(order, currentPrice)) {
                applyFill(tick, order);
                orders.remove(order);
                return;
            }
        }
    }

    /**
     * Mutates the TradingEvent in place,
     *
     * @param event TradingEvent.
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
     *
     * @param order The resting OpenOrder to evaluate.
     * @param marketPrice The current market price of the underlying asset.
     * @return true if the order should be filled, false otherwise.
     */
    private boolean isMatch(OpenOrder order, double marketPrice) {
        return order.side() == Side.BUY ? marketPrice <= order.price() : marketPrice >= order.price();
    }

    /**
     * Persists an incoming NEW_ORDER event as an OpenOrder record in the order book.
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
     *
     * @return Array of last known prices indexed by symbolId.
     */
    public double[] getLastKnownPrices() {
        return lastKnownPrices;
    }
}