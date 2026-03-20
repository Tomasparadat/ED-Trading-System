package com.trading;


import com.trading.infra.controller.SystemController;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        SystemController systemController = new SystemController();
        systemController.start();

        // Trading System execution time.
        Thread.sleep(10_000);

        systemController.stop();
    }
}