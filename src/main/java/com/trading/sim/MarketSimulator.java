package com.trading.sim;

import com.trading.api.MarketDataProvider;
import com.trading.domain.ValidatedOrder;
import com.trading.infra.event.TradingEvent;

public class MarketSimulator implements MarketDataProvider {
    private PriceGenerator priceGen;
    private OrderMatcher matcher;
    private EventProducer producer;

    public void onValidateOrder(ValidatedOrder event) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }



}
