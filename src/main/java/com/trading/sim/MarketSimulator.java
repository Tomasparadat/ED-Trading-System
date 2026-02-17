package com.trading.sim;

import com.lmax.disruptor.dsl.Disruptor;
import com.trading.api.MarketDataProvider;
import com.trading.domain.ValidatedOrder;
import com.trading.infra.engine.DisruptorManager;
import com.trading.infra.event.TradingEvent;

public class MarketSimulator implements MarketDataProvider {
    private final DisruptorManager disruptor;

    public MarketSimulator(DisruptorManager disruptor) {
        this.disruptor = disruptor;
    }


    public void onValidateOrder(ValidatedOrder event) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }



}
