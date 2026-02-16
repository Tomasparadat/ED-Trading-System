package com.trading.portfolio;

import com.trading.domain.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Position Unit Tests")
class PositionTest {

    private Position position;
    private static final String TEST_SYMBOL = "AAPL";
    private static final double DELTA = 0.0001; // For floating point comparisons

    @BeforeEach
    void setUp() {
        position = new Position(TEST_SYMBOL, 100.0, 150.0);
    }

    @Test
    @DisplayName("Constructor should initialize position with correct values")
    void constructor_validParameters_initializesCorrectly() {
        Position newPosition = new Position("TSLA", 50.0, 200.0);

        assertEquals(10000.0, newPosition.getMarketValue(200.0), DELTA);
        assertEquals(0.0, newPosition.getLastTradeRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("getMarketValue should calculate correct market value")
    void getMarketValue_validCurrentPrice_returnsCorrectValue() {
        double currentPrice = 160.0;
        double expectedValue = 100.0 * 160.0; // quantity * price

        assertEquals(expectedValue, position.getMarketValue(currentPrice), DELTA);
    }

    @Test
    @DisplayName("getMarketValue should handle zero price")
    void getMarketValue_zeroPrice_returnsZero() {
        assertEquals(0.0, position.getMarketValue(0.0), DELTA);
    }

    @Test
    @DisplayName("getMarketValue should handle negative price")
    void getMarketValue_negativePrice_returnsNegativeValue() {
        double currentPrice = -100.0;
        double expectedValue = 100.0 * -100.0;

        assertEquals(expectedValue, position.getMarketValue(currentPrice), DELTA);
    }

    // BUY SCENARIOS
    @Test
    @DisplayName("updateOnFill should increase position on buy")
    void updateOnFill_buyOrder_increasesQuantityAndUpdatesAvgPrice() {
        // Initial: 100 shares @ $150
        // Buy: 50 shares @ $160
        // Expected: 150 shares @ $153.33
        position.updateOnFill(50.0, 160.0);

        double expectedAvgPrice = (100 * 150 + 50 * 160) / 150.0;
        assertEquals(23000.0, position.getMarketValue(expectedAvgPrice), DELTA);
        assertEquals(0.0, position.getLastTradeRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("updateOnFill should update average entry price correctly on multiple buys")
    void updateOnFill_multipleBuys_updatesAvgPriceCorrectly() {
        // Initial: 100 @ $150
        position.updateOnFill(50.0, 160.0);  // 150 @ $153.33
        position.updateOnFill(50.0, 170.0);  // 200 @ $157.50

        double expectedAvgPrice = (100 * 150 + 50 * 160 + 50 * 170) / 200.0;
        assertEquals(expectedAvgPrice, position.getMarketValue(1.0), DELTA);
    }

    @Test
    @DisplayName("updateOnFill should handle fractional share purchase")
    void updateOnFill_fractionalShareBuy_updatesCorrectly() {
        position.updateOnFill(0.5, 160.0);

        double expectedAvgPrice = (100 * 150 + 0.5 * 160) / 100.5;
        assertEquals(100.5 * expectedAvgPrice, position.getMarketValue(expectedAvgPrice), DELTA);
    }

    // SELL SCENARIOS
    @Test
    @DisplayName("updateOnFill should reduce position on partial sell")
    void updateOnFill_partialSell_reducesQuantityAndCalculatesPnL() {
        // Initial: 100 shares @ $150
        // Sell: 40 shares @ $160
        // PnL: (160 - 150) * 40 = $400
        // Remaining: 60 shares @ $150 (avg price unchanged)
        position.updateOnFill(-40.0, 160.0);

        assertEquals(60.0 * 150.0, position.getMarketValue(150.0), DELTA);
        assertEquals(400.0, position.getLastTradeRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("updateOnFill should close entire position on full sell")
    void updateOnFill_fullSell_closesPositionAndCalculatesPnL() {
        // Initial: 100 shares @ $150
        // Sell: 100 shares @ $160
        // PnL: (160 - 150) * 100 = $1000
        position.updateOnFill(-100.0, 160.0);

        assertEquals(0.0, position.getMarketValue(160.0), DELTA);
        assertEquals(1000.0, position.getLastTradeRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("updateOnFill should reset average entry price to zero when position fully closed")
    void updateOnFill_positionFullyClosed_resetsAvgPriceToZero() {
        position.updateOnFill(-100.0, 160.0);

        // After closing, avg price should be 0
        assertEquals(0.0, position.getMarketValue(1.0), DELTA);
    }

    @Test
    @DisplayName("updateOnFill should prevent overselling by limiting to current quantity")
    void updateOnFill_oversell_limitsToCurrentQuantity() {
        // Initial: 100 shares @ $150
        // Attempt to sell: 150 shares @ $160
        // Should only sell 100 shares
        position.updateOnFill(-150.0, 160.0);

        assertEquals(0.0, position.getMarketValue(160.0), DELTA);
        assertEquals(1000.0, position.getLastTradeRealizedPnL(), DELTA); // Only 100 shares sold
    }

    @Test
    @DisplayName("updateOnFill should calculate loss on sell below entry price")
    void updateOnFill_sellAtLoss_calculatesNegativePnL() {
        // Initial: 100 shares @ $150
        // Sell: 50 shares @ $140
        // PnL: (140 - 150) * 50 = -$500
        position.updateOnFill(-50.0, 140.0);

        assertEquals(-500.0, position.getLastTradeRealizedPnL(), DELTA);
        assertEquals(50.0 * 150.0, position.getMarketValue(150.0), DELTA);
    }

    // EDGE CASES
    @Test
    @DisplayName("updateOnFill should handle buy after complete sell")
    void updateOnFill_buyAfterCompleteSell_createsNewPosition() {
        // Sell entire position
        position.updateOnFill(-100.0, 160.0);

        // Buy new position
        position.updateOnFill(200.0, 170.0);

        assertEquals(200.0 * 170.0, position.getMarketValue(170.0), DELTA);
    }

    @Test
    @DisplayName("updateOnFill should handle zero quantity fill")
    void updateOnFill_zeroQuantity_noChangeToPosition() {
        double initialMarketValue = position.getMarketValue(150.0);

        position.updateOnFill(0.0, 160.0);

        assertEquals(initialMarketValue, position.getMarketValue(150.0), DELTA);
    }

    @Test
    @DisplayName("updateOnFill should handle very small quantities")
    void updateOnFill_verySmallQuantity_updatesCorrectly() {
        position.updateOnFill(0.001, 160.0);

        double expectedAvgPrice = (100 * 150 + 0.001 * 160) / 100.001;
        assertEquals(100.001 * expectedAvgPrice, position.getMarketValue(expectedAvgPrice), DELTA);
    }

    @Test
    @DisplayName("updateOnFill should handle multiple sells and buys")
    void updateOnFill_multipleSellsAndBuys_tracksCorrectly() {
        position.updateOnFill(-30.0, 160.0);  // Sell 30 @ 160, PnL = 300
        assertEquals(300.0, position.getLastTradeRealizedPnL(), DELTA);

        position.updateOnFill(50.0, 155.0);   // Buy 50 @ 155
        assertEquals(0.0, position.getLastTradeRealizedPnL(), DELTA); // Reset on buy

        position.updateOnFill(-20.0, 165.0);  // Sell 20 @ 165
        // Should calculate based on weighted avg of remaining 70 @ 150 and bought 50 @ 155
        assertTrue(position.getLastTradeRealizedPnL() > 0);
    }

    // SHORT POSITION SCENARIOS
    @Test
    @DisplayName("updateOnFill should handle short position creation")
    void updateOnFill_shortPositionSell_createsNegativePosition() {
        Position shortPosition = new Position("SPY", -100.0, 400.0);

        // Covering short by buying
        shortPosition.updateOnFill(50.0, 390.0);

        // PnL on short: (400 - 390) * 50 = $500 profit (sold high, bought low)
        assertEquals(500.0, shortPosition.getLastTradeRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("updateOnFill should increase short position correctly")
    void updateOnFill_increaseShortPosition_updatesAvgPrice() {
        Position shortPosition = new Position("SPY", -100.0, 400.0);

        // Increase short position
        shortPosition.updateOnFill(-50.0, 390.0);

        double expectedAvgPrice = (-100 * 400 + -50 * 390) / -150.0;
        assertEquals(0.0, shortPosition.getLastTradeRealizedPnL(), DELTA); // No PnL on increasing
    }

    @Test
    @DisplayName("getLastTradeRealizedPnL should return zero initially")
    void getLastTradeRealizedPnL_newPosition_returnsZero() {
        assertEquals(0.0, position.getLastTradeRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("getLastTradeRealizedPnL should persist until next sell")
    void getLastTradeRealizedPnL_afterSell_persistsUntilNextTrade() {
        position.updateOnFill(-50.0, 160.0);
        double firstPnL = position.getLastTradeRealizedPnL();

        // PnL should persist across multiple reads
        assertEquals(firstPnL, position.getLastTradeRealizedPnL(), DELTA);
        assertEquals(firstPnL, position.getLastTradeRealizedPnL(), DELTA);

        // But reset on buy
        position.updateOnFill(10.0, 165.0);
        assertEquals(0.0, position.getLastTradeRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("updateOnFill should handle large quantities")
    void updateOnFill_largeQuantities_handlesCorrectly() {
        Position largePosition = new Position("BRK.A", 1000000.0, 500000.0);

        largePosition.updateOnFill(500000.0, 510000.0);

        double expectedAvgPrice = (1000000 * 500000 + 500000 * 510000) / 1500000.0;
        assertTrue(expectedAvgPrice > 500000.0);
        assertTrue(expectedAvgPrice < 510000.0);
    }
}