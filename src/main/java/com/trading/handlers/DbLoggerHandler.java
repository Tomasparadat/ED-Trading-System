package com.trading.handlers;

import com.lmax.disruptor.EventHandler;
import com.trading.domain.EventType;
import com.trading.infra.event.TradingEvent;

public class DbLoggerHandler implements EventHandler<TradingEvent> {
//    private final QuestDBWriter writer;


    @Override
    public void onEvent(TradingEvent event, long l, boolean b) throws Exception {
        if(event.getType() != EventType.ORDER_FILL) {
            return;
        }

        // Write to QuestDB
        // For now: just a println so you can see it working
        System.out.println(event.getType() + " " + event.getOrderId() + " " + event.getSymbolId());
        // Replace with writer.write(event) when DB is ready
    }

}
