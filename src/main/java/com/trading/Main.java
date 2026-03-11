package com.trading;


import com.trading.infra.controller.SystemController;
import com.trading.infra.reporting.SessionReport;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        SystemController systemController = new SystemController();
        systemController.start();

        Thread.sleep(20_000);

        systemController.stop();
    }
}