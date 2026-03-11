package com.trading.infra.db;

/*
* Use when implementing QuestDB in future version.
* */
public class AsyncWriter {
    // private QuestDBClient client;
    private StringBuilder buffer;
    private int batchSize;

    public void write(String line) {}

    private void flush() {}
}
