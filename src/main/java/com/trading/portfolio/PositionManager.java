package com.trading.portfolio;

import java.util.Map;
import com.trading.domain.Position;

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
     *  Retrieve X Position from map
     *
     * @param symbol Ticker symbol of traded asset.
     * @return Position with Ticker-Name of symbol parameter.
     */
    public Position getPosition(String symbol){
        return positions.get(symbol);
    }

    /**
     * Creates a new Position Object at places it int he Positions Map.
     * Checks if Quantity and price are above 0.0 in order to not have empty Fills.
     *
     * @param symbol Ticker Symbol.
     * @param quantity Fill Quantity.
     * @param price Fill Price.
     */
    public void createNewPosition(String symbol, double quantity, double price){
        if(quantity <= 0.0){ return; }
        if(price <= 0.0){ return; }

        positions.put(symbol, new Position(symbol, quantity, price));
    }
}
