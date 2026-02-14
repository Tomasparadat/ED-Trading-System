package com.trading.portfolio;

import java.util.Map;
import com.trading.portfolio.Position;

public class PositionManager {
    private static Map<String, Position> positions;

    /**
     * Update the quantity and average Entry-Price of a given position passed
     * as the Ticker Symbol of the Stock.
     *
     * @param symbol Ticker symbol of traded asset.
     * @param quantity Traded quantity.
     * @param price Asset fill price.
     */
    public void updatePositions(String symbol, double quantity, double price){
        positions.get(symbol).updateOnFill(quantity, price);
    }

    /**
     *
     * @param symbol Ticker symbol of traded asset.
     * @return Position with Ticker-Name of symbol parameter.
     */
    public Position getPosition(String symbol){
        return positions.get(symbol);
    }

    public void createNewPosition(String symbol, double quantity, double price){
        if(quantity < 0.0){ throw new IllegalArgumentException("Quantity must be greater than zero"); }
        if(price < 0.0){ throw new IllegalArgumentException("Price must be greater than zero"); }

        positions.put(symbol, new Position(symbol, quantity, price));
    }
}
