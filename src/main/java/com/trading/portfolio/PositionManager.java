package com.trading.portfolio;


import com.trading.domain.Position;

public class PositionManager {
    private final Position[] positions;

    public PositionManager(int symbolCount) {
        this.positions = new Position[symbolCount];
    }

    /**
     * Retrieve X Position from map
     *
     * @param symbolId Ticker symbol of traded asset.
     * @return Position with Ticker-Name of symbol parameter.
     */
    public Position getPosition(int symbolId){
        return positions[symbolId];
    }

    /**
     * Creates a new Position Object at places it int he Positions Map.
     * Checks if Quantity and price are above 0.0 in order to not have empty Fills.
     *
     * @param symbolId Ticker Symbol.
     * @param quantity Fill Quantity.
     * @param price Fill Price.
     */
    public void createNewPosition(int symbolId, double quantity, double price){
        if (quantity <= 0.0 || price <= 0.0) return;
        positions[symbolId] = new Position(symbolId, quantity, price);
    }
}
