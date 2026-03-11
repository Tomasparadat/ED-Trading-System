package com.trading.infra.db;

import com.trading.infra.event.TradingEvent;

// Use when implementing QuestDB in future version.
public class DbBridge {
    private SchemaMapper mapper;
    private AsyncWriter writer;

    public void onEvent(TradingEvent event){}


}
