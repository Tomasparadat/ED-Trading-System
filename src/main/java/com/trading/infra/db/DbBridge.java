package com.trading.infra.db;

import com.trading.infra.event.TradingEvent;

public class DbBridge {
    private SchemaMapper mapper;
    private AsyncWriter writer;

    public void onEvent(TradingEvent event){}


}
