package com.trading.portfolio;

import java.util.Map;
import com.trading.portfolio.Position;

public class PositionManager {
    private static Map<String, Position> positions;

    /**
     * Update the quantity and average Entry-Price of a given position passed
     * as the Ticker Symbol of the Stock.
     *
     * @param symbol
     * @param quantity
     * @param price
     */
    public void updatePositions(String symbol, double quantity, double price){
        positions.get(symbol).updateOnFill(quantity, price);
    }

    /**
     *
     * @param symbol
     * @return Position with Ticker-Name of symbol parameter.
     */
    public Position getPosition(String symbol){
        return positions.get(symbol);
    }
}
