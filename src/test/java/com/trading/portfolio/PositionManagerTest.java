package com.trading.portfolio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("PositionManager Unit Tests")
class PositionManagerTest {

    private PositionManager positionManager;

    @Mock
    private Position mockPosition;

    private static final double DELTA = 0.0001;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        positionManager = new PositionManager();

        // Initialize the static positions map
        initializePositionsMap();
    }

    @Test
    @DisplayName("createNewPosition should create position with valid parameters")
    void createNewPosition_validParameters_createsPosition() throws Exception {
        positionManager.createNewPosition("AAPL", 100.0, 150.0);

        Position position = positionManager.getPosition("AAPL");
        assertNotNull(position);
        assertEquals(15000.0, position.getMarketValue(150.0), DELTA);
    }

    @Test
    @DisplayName("createNewPosition should throw exception for zero quantity")
    void createNewPosition_zeroQuantity_throwsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            positionManager.createNewPosition("AAPL", 0.0, 150.0);
        });

        assertEquals("Quantity must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("createNewPosition should throw exception for negative quantity")
    void createNewPosition_negativeQuantity_throwsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            positionManager.createNewPosition("AAPL", -100.0, 150.0);
        });

        assertEquals("Quantity must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("createNewPosition should throw exception for zero price")
    void createNewPosition_zeroPrice_throwsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            positionManager.createNewPosition("AAPL", 100.0, 0.0);
        });

        assertEquals("Price must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("createNewPosition should throw exception for negative price")
    void createNewPosition_negativePrice_throwsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            positionManager.createNewPosition("AAPL", 100.0, -150.0);
        });

        assertEquals("Price must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("createNewPosition should create multiple different positions")
    void createNewPosition_multiplePositions_createsAll() throws Exception {
        positionManager.createNewPosition("AAPL", 100.0, 150.0);
        positionManager.createNewPosition("TSLA", 50.0, 700.0);
        positionManager.createNewPosition("MSFT", 200.0, 300.0);

        assertNotNull(positionManager.getPosition("AAPL"));
        assertNotNull(positionManager.getPosition("TSLA"));
        assertNotNull(positionManager.getPosition("MSFT"));

        Map<String, Position> positions = getPositionsMap();
        assertEquals(3, positions.size());
    }

    @Test
    @DisplayName("createNewPosition should overwrite existing position with same symbol")
    void createNewPosition_duplicateSymbol_overwritesPosition() throws Exception {
        positionManager.createNewPosition("AAPL", 100.0, 150.0);
        Position firstPosition = positionManager.getPosition("AAPL");

        positionManager.createNewPosition("AAPL", 200.0, 160.0);
        Position secondPosition = positionManager.getPosition("AAPL");

        assertNotNull(secondPosition);
        assertEquals(32000.0, secondPosition.getMarketValue(160.0), DELTA);
    }

    @Test
    @DisplayName("createNewPosition should handle fractional quantities")
    void createNewPosition_fractionalQuantity_createsPosition() throws Exception {
        positionManager.createNewPosition("BTC", 0.5, 50000.0);

        Position position = positionManager.getPosition("BTC");
        assertNotNull(position);
        assertEquals(25000.0, position.getMarketValue(50000.0), DELTA);
    }

    @Test
    @DisplayName("createNewPosition should handle very small positive quantity")
    void createNewPosition_verySmallQuantity_createsPosition() throws Exception {
        positionManager.createNewPosition("AAPL", 0.0001, 150.0);

        Position position = positionManager.getPosition("AAPL");
        assertNotNull(position);
    }

    @Test
    @DisplayName("createNewPosition should handle very small positive price")
    void createNewPosition_verySmallPrice_createsPosition() throws Exception {
        positionManager.createNewPosition("PENNY", 1000.0, 0.0001);

        Position position = positionManager.getPosition("PENNY");
        assertNotNull(position);
    }

    @Test
    @DisplayName("createNewPosition should handle large quantities")
    void createNewPosition_largeQuantity_createsPosition() throws Exception {
        positionManager.createNewPosition("AAPL", 1000000.0, 150.0);

        Position position = positionManager.getPosition("AAPL");
        assertNotNull(position);
        assertEquals(150000000.0, position.getMarketValue(150.0), DELTA);
    }

    @Test
    @DisplayName("createNewPosition should handle large prices")
    void createNewPosition_largePrice_createsPosition() throws Exception {
        positionManager.createNewPosition("BRK.A", 1.0, 500000.0);

        Position position = positionManager.getPosition("BRK.A");
        assertNotNull(position);
        assertEquals(500000.0, position.getMarketValue(500000.0), DELTA);
    }

    @Test
    @DisplayName("getPosition should return null for non-existent symbol")
    void getPosition_nonExistentSymbol_returnsNull() {
        Position position = positionManager.getPosition("NONEXISTENT");

        assertNull(position);
    }

    @Test
    @DisplayName("getPosition should return correct position for existing symbol")
    void getPosition_existingSymbol_returnsCorrectPosition() throws Exception {
        positionManager.createNewPosition("AAPL", 100.0, 150.0);

        Position position = positionManager.getPosition("AAPL");

        assertNotNull(position);
        assertEquals(15000.0, position.getMarketValue(150.0), DELTA);
    }

    @Test
    @DisplayName("getPosition should handle empty string symbol")
    void getPosition_emptySymbol_returnsNull() {
        Position position = positionManager.getPosition("");

        assertNull(position);
    }

    @Test
    @DisplayName("getPosition should handle null symbol")
    void getPosition_nullSymbol_returnsNull() {
        Position position = positionManager.getPosition(null);

        assertNull(position);
    }

    @Test
    @DisplayName("getPosition should be case-sensitive")
    void getPosition_differentCase_returnsNull() throws Exception {
        positionManager.createNewPosition("AAPL", 100.0, 150.0);

        Position upperCase = positionManager.getPosition("AAPL");
        Position lowerCase = positionManager.getPosition("aapl");

        assertNotNull(upperCase);
        assertNull(lowerCase);
    }

    @Test
    @DisplayName("updatePositions should update existing position")
    void updatePositions_existingPosition_updatesCorrectly() throws Exception {
        // Create initial position
        positionManager.createNewPosition("AAPL", 100.0, 150.0);

        // Update position (buy more)
        positionManager.updatePositions("AAPL", 50.0, 160.0);

        Position position = positionManager.getPosition("AAPL");
        // Expected avg price: (100*150 + 50*160) / 150 = 153.33
        assertEquals(150 * 153.333, position.getMarketValue(153.333), 1.0);
    }

    @Test
    @DisplayName("updatePositions should handle sell orders")
    void updatePositions_sellOrder_updatesCorrectly() throws Exception {
        positionManager.createNewPosition("AAPL", 100.0, 150.0);

        positionManager.updatePositions("AAPL", -50.0, 160.0);

        Position position = positionManager.getPosition("AAPL");
        assertEquals(50.0 * 150.0, position.getMarketValue(150.0), DELTA);
        assertEquals(500.0, position.getLastTradeRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("updatePositions should handle multiple updates")
    void updatePositions_multipleUpdates_updatesCorrectly() throws Exception {
        positionManager.createNewPosition("AAPL", 100.0, 150.0);

        positionManager.updatePositions("AAPL", 50.0, 160.0);
        positionManager.updatePositions("AAPL", -30.0, 170.0);
        positionManager.updatePositions("AAPL", 20.0, 155.0);

        Position position = positionManager.getPosition("AAPL");
        assertNotNull(position);
    }

    @Test
    @DisplayName("updatePositions should throw NullPointerException for non-existent position")
    void updatePositions_nonExistentPosition_throwsException() {
        assertThrows(NullPointerException.class, () -> {
            positionManager.updatePositions("NONEXISTENT", 100.0, 150.0);
        });
    }

    @Test
    @DisplayName("updatePositions should handle zero quantity update")
    void updatePositions_zeroQuantity_noChange() throws Exception {
        positionManager.createNewPosition("AAPL", 100.0, 150.0);

        positionManager.updatePositions("AAPL", 0.0, 160.0);

        Position position = positionManager.getPosition("AAPL");
        assertEquals(15000.0, position.getMarketValue(150.0), DELTA);
    }

    @Test
    @DisplayName("updatePositions should handle negative prices")
    void updatePositions_negativePrice_updates() throws Exception {
        positionManager.createNewPosition("AAPL", 100.0, 150.0);

        // This is allowed by Position.updateOnFill
        positionManager.updatePositions("AAPL", 50.0, -160.0);

        Position position = positionManager.getPosition("AAPL");
        assertNotNull(position);
    }

    @Test
    @DisplayName("updatePositions should handle fractional quantities")
    void updatePositions_fractionalQuantity_updates() throws Exception {
        positionManager.createNewPosition("AAPL", 100.0, 150.0);

        positionManager.updatePositions("AAPL", 0.5, 160.0);

        Position position = positionManager.getPosition("AAPL");
        assertNotNull(position);
    }

    @Test
    @DisplayName("createNewPosition should handle symbols with special characters")
    void createNewPosition_specialCharactersInSymbol_createsPosition() throws Exception {
        positionManager.createNewPosition("BRK.A", 10.0, 500000.0);
        positionManager.createNewPosition("BTC-USD", 0.5, 50000.0);

        assertNotNull(positionManager.getPosition("BRK.A"));
        assertNotNull(positionManager.getPosition("BTC-USD"));
    }

    @Test
    @DisplayName("createNewPosition should handle very long symbol names")
    void createNewPosition_longSymbol_createsPosition() throws Exception {
        String longSymbol = "VERYLONGSYMBOLNAME123456789";
        positionManager.createNewPosition(longSymbol, 100.0, 150.0);

        assertNotNull(positionManager.getPosition(longSymbol));
    }

    @Test
    @DisplayName("Multiple position managers should share same positions map")
    void multipleManagers_sameStaticMap_sharePositions() throws Exception {
        PositionManager manager1 = new PositionManager();
        PositionManager manager2 = new PositionManager();

        // Initialize for both
        initializePositionsMap();

        manager1.createNewPosition("AAPL", 100.0, 150.0);

        // Manager2 should see the same position
        Position position = manager2.getPosition("AAPL");
        assertNotNull(position);
    }

    @Test
    @DisplayName("createNewPosition then getPosition should return consistent object")
    void createAndGet_sameSymbol_returnsSameObject() throws Exception {
        positionManager.createNewPosition("AAPL", 100.0, 150.0);

        Position position1 = positionManager.getPosition("AAPL");
        Position position2 = positionManager.getPosition("AAPL");

        assertSame(position1, position2);
    }

    // Helper method to initialize the static positions map using reflection
    private void initializePositionsMap() throws Exception {
        Field field = PositionManager.class.getDeclaredField("positions");
        field.setAccessible(true);
        field.set(null, new HashMap<String, Position>());
    }

    // Helper method to get the positions map using reflection
    @SuppressWarnings("unchecked")
    private Map<String, Position> getPositionsMap() throws Exception {
        Field field = PositionManager.class.getDeclaredField("positions");
        field.setAccessible(true);
        return (Map<String, Position>) field.get(null);
    }
}