package com.trading.domain;

public class Position {
    private int symbolId;
    private double quantity;
    private double averageEntryPrice;
    private double lastTradeRealizedPnL;

    /**
     * Position Constructor.
     *
     * @param symbolId Ticker Symbol.
     * @param quantity Fill Quantity.
     * @param averageEntryPrice Average Entry Price of Position.
     */
    public Position(int symbolId, double quantity, double averageEntryPrice) {
        this.symbolId = symbolId;
        this.quantity = quantity;
        this.averageEntryPrice = averageEntryPrice;
    }

    /**
     * Return the total market value of the position.
     *
     * @param currentPrice Current Market Price of underlying asset.
     * @return Current market value of position.
     */
    public double getMarketValue(double currentPrice) {
        return quantity * currentPrice;
    }

    /**
     * Update position based of Fill (Buy or sell).
     * The method decides whether to sell or buy based off the fillQty, if the fillQty > 0 this signals a purchase,
     * otherwise a sell is being executed.
     * BUY: Average entry price and position quantity are updated
     * SELL: Prevents Position from going short, if fillQty is greater than Position quantity,
     * then whole Position is sold. PnL and quantity are updated.
     *
     * @param fillQty Quantity of Position being Sold/Bought.
     * @param fillPrice Price of Fill.
     */
    public void updateOnFill(double fillQty, double fillPrice) {
        // Check if fill is a buy or sell.
        boolean isIncreasing = (this.quantity >= 0 && fillQty > 0) || (this.quantity <= 0 && fillQty < 0);

        if (isIncreasing) {
            // BUY: Update position quantity and averageEntryPrice.
            double totalCost = (this.quantity * this.averageEntryPrice) + (fillQty * fillPrice);
            this.quantity += fillQty;
            this.averageEntryPrice = totalCost / this.quantity;
            this.lastTradeRealizedPnL = 0;
        } else {
            // SELL: calculate PnL on sell.
            double qtyToClose = Math.min(Math.abs(this.quantity), Math.abs(fillQty));

            // PnL = (Sell Price - Buy Price) * Quantity sold
            this.lastTradeRealizedPnL = (fillPrice - this.averageEntryPrice) * qtyToClose * Math.signum(this.quantity);

            this.quantity += fillQty;

            if (this.quantity == 0) {
                this.averageEntryPrice = 0;
            }
        }
    }

    /**
     * @return realized PnL from last Trade on Position.
     */
    public double getLastTradeRealizedPnL() {
        return lastTradeRealizedPnL;
    }

   public int getSymbolId() {
        return symbolId;
   }

   public double getQuantity() {
        return quantity;
   }

    /**
     * Returns the realized PnL from the last trade and resets it to 0.
     * Prevents double-counting if calculate() is called on a BUY event.
     */
    public double consumeLastTradeRealizedPnL() {
        double pnl = this.lastTradeRealizedPnL;
        this.lastTradeRealizedPnL = 0;
        return pnl;
    }
}
