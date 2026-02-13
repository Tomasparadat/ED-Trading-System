package com.trading.infra.db;

import com.trading.handlers.EventHandler;
import com.trading.infra.event.TradingEvent;

public class DbBridge implements EventHandler {
    private SchemaMapper mapper;
    private AsyncWriter writer;

    public void onEvent(TradingEvent event){}


}
