package com.trading.infra.db;

import com.trading.infra.event.TradingEvent;

// Use when implementing QuestDB in future version.
public class SchemaMapper {
    public String toLineProtocol(TradingEvent event){
        return "";
    }
    private long convertToMicros(long millis) {
        return 0;
    }
}
