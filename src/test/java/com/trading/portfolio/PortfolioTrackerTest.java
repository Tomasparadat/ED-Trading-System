package com.trading.portfolio;

import com.lmax.disruptor.EventHandler;
import com.trading.infra.event.EventType;
import com.trading.infra.event.TradingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("PortfolioTracker Unit Tests")
class PortfolioTrackerTest {

    @Mock
    private PositionManager mockPositionManager;

    @Mock
    private PnLCalculator mockPnlCalculator;

    @Mock
    private Ledger mockLedger;

    @Mock
    private TradingEvent mockTradingEvent;

    @Mock
    private Position mockPosition;

    private PortfolioTracker portfolioTracker;

    private static final String TEST_SYMBOL = "AAPL";
    private static final double TEST_QUANTITY = 100.0;
    private static final double TEST_PRICE = 150.0;
    private static final long TEST_SEQUENCE = 12345L;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Create PortfolioTracker and inject mocks using reflection
        portfolioTracker = new PortfolioTracker();
        injectMocks();
    }

    @Test
    @DisplayName("PortfolioTracker should implement EventHandler interface")
    void portfolioTracker_implementsEventHandler() {
        assertTrue(portfolioTracker instanceof EventHandler);
    }

    @Test
    @DisplayName("onEvent should ignore non-ORDER_FILL events")
    void onEvent_nonOrderFillEvent_ignoresEvent() {
        when(mockTradingEvent.getType()).thenReturn(EventType.MARKET_TICK);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        verify(mockTradingEvent, times(1)).getType();
        verifyNoInteractions(mockPositionManager, mockPnlCalculator, mockLedger);
    }

    @Test
    @DisplayName("onEvent should ignore ORDER_PROPOSED events")
    void onEvent_orderProposedEvent_ignoresEvent() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_PROPOSED);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        verify(mockTradingEvent, times(1)).getType();
        verifyNoInteractions(mockPositionManager, mockPnlCalculator, mockLedger);
    }

    @Test
    @DisplayName("onEvent should ignore ORDER_VALIDATED events")
    void onEvent_orderValidatedEvent_ignoresEvent() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_VALIDATED);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        verify(mockTradingEvent, times(1)).getType();
        verifyNoInteractions(mockPositionManager, mockPnlCalculator, mockLedger);
    }

    @Test
    @DisplayName("onEvent should ignore SYSTEM_HALT events")
    void onEvent_systemHaltEvent_ignoresEvent() {
        when(mockTradingEvent.getType()).thenReturn(EventType.SYSTEM_HALT);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        verify(mockTradingEvent, times(1)).getType();
        verifyNoInteractions(mockPositionManager, mockPnlCalculator, mockLedger);
    }

    @Test
    @DisplayName("onEvent should create new position when position does not exist")
    void onEvent_newPosition_createsPosition() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(TEST_QUANTITY);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition(TEST_SYMBOL)).thenReturn(null);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        verify(mockPositionManager, times(1)).getPosition(TEST_SYMBOL);
        verify(mockPositionManager, times(1)).createNewPosition(TEST_SYMBOL, TEST_QUANTITY, TEST_PRICE);
        verify(mockPnlCalculator, times(1)).calculate(null);
        verify(mockLedger, times(1)).recordTrade(mockTradingEvent, TEST_SEQUENCE);
    }

    @Test
    @DisplayName("onEvent should update existing position when position exists")
    void onEvent_existingPosition_updatesPosition() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(TEST_QUANTITY);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition(TEST_SYMBOL)).thenReturn(mockPosition);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        verify(mockPositionManager, times(1)).getPosition(TEST_SYMBOL);
        verify(mockPosition, times(1)).updateOnFill(TEST_PRICE, TEST_QUANTITY);
        verify(mockPositionManager, never()).createNewPosition(anyString(), anyDouble(), anyDouble());
        verify(mockPnlCalculator, times(1)).calculate(mockPosition);
        verify(mockLedger, times(1)).recordTrade(mockTradingEvent, TEST_SEQUENCE);
    }

    @Test
    @DisplayName("onEvent should always record trade in ledger")
    void onEvent_orderFill_alwaysRecordsInLedger() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(TEST_QUANTITY);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition(TEST_SYMBOL)).thenReturn(mockPosition);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        verify(mockLedger, times(1)).recordTrade(mockTradingEvent, TEST_SEQUENCE);
    }

    @Test
    @DisplayName("onEvent should always calculate PnL")
    void onEvent_orderFill_alwaysCalculatesPnL() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(TEST_QUANTITY);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition(TEST_SYMBOL)).thenReturn(mockPosition);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        verify(mockPnlCalculator, times(1)).calculate(mockPosition);
    }

    @Test
    @DisplayName("onEvent should handle multiple sequential events")
    void onEvent_multipleEvents_processesAll() {
        // First event - new position
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(TEST_QUANTITY);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition(TEST_SYMBOL))
                .thenReturn(null)
                .thenReturn(mockPosition)
                .thenReturn(mockPosition);

        portfolioTracker.onEvent(mockTradingEvent, 1L, false);
        portfolioTracker.onEvent(mockTradingEvent, 2L, false);
        portfolioTracker.onEvent(mockTradingEvent, 3L, false);

        verify(mockPositionManager, times(1)).createNewPosition(TEST_SYMBOL, TEST_QUANTITY, TEST_PRICE);
        verify(mockPosition, times(2)).updateOnFill(TEST_PRICE, TEST_QUANTITY);
        verify(mockLedger, times(3)).recordTrade(eq(mockTradingEvent), anyLong());
    }

    @Test
    @DisplayName("onEvent should handle different symbols")
    void onEvent_differentSymbols_createsMultiplePositions() {
        TradingEvent event1 = mock(TradingEvent.class);
        TradingEvent event2 = mock(TradingEvent.class);

        when(event1.getType()).thenReturn(EventType.ORDER_FILL);
        when(event1.getSymbol()).thenReturn("AAPL");
        when(event1.getQuantity()).thenReturn(100.0);
        when(event1.getPrice()).thenReturn(150.0);

        when(event2.getType()).thenReturn(EventType.ORDER_FILL);
        when(event2.getSymbol()).thenReturn("TSLA");
        when(event2.getQuantity()).thenReturn(50.0);
        when(event2.getPrice()).thenReturn(700.0);

        when(mockPositionManager.getPosition("AAPL")).thenReturn(null);
        when(mockPositionManager.getPosition("TSLA")).thenReturn(null);

        portfolioTracker.onEvent(event1, 1L, false);
        portfolioTracker.onEvent(event2, 2L, false);

        verify(mockPositionManager, times(1)).createNewPosition("AAPL", 100.0, 150.0);
        verify(mockPositionManager, times(1)).createNewPosition("TSLA", 50.0, 700.0);
    }

    @Test
    @DisplayName("onEvent should handle endOfBatch flag true")
    void onEvent_endOfBatchTrue_processesNormally() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(TEST_QUANTITY);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition(TEST_SYMBOL)).thenReturn(null);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, true);

        verify(mockPositionManager, times(1)).createNewPosition(TEST_SYMBOL, TEST_QUANTITY, TEST_PRICE);
        verify(mockLedger, times(1)).recordTrade(mockTradingEvent, TEST_SEQUENCE);
    }

    @Test
    @DisplayName("onEvent should handle endOfBatch flag false")
    void onEvent_endOfBatchFalse_processesNormally() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(TEST_QUANTITY);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition(TEST_SYMBOL)).thenReturn(null);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        verify(mockPositionManager, times(1)).createNewPosition(TEST_SYMBOL, TEST_QUANTITY, TEST_PRICE);
        verify(mockLedger, times(1)).recordTrade(mockTradingEvent, TEST_SEQUENCE);
    }

    @Test
    @DisplayName("onEvent should handle zero sequence number")
    void onEvent_zeroSequence_processesNormally() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(TEST_QUANTITY);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition(TEST_SYMBOL)).thenReturn(null);

        portfolioTracker.onEvent(mockTradingEvent, 0L, false);

        verify(mockLedger, times(1)).recordTrade(mockTradingEvent, 0L);
    }

    @Test
    @DisplayName("onEvent should handle negative sequence number")
    void onEvent_negativeSequence_processesNormally() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(TEST_QUANTITY);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition(TEST_SYMBOL)).thenReturn(null);

        portfolioTracker.onEvent(mockTradingEvent, -1L, false);

        verify(mockLedger, times(1)).recordTrade(mockTradingEvent, -1L);
    }

    @Test
    @DisplayName("onEvent should handle very large sequence numbers")
    void onEvent_largeSequence_processesNormally() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(TEST_QUANTITY);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition(TEST_SYMBOL)).thenReturn(null);

        portfolioTracker.onEvent(mockTradingEvent, Long.MAX_VALUE, false);

        verify(mockLedger, times(1)).recordTrade(mockTradingEvent, Long.MAX_VALUE);
    }

    @Test
    @DisplayName("onEvent should handle negative quantity")
    void onEvent_negativeQuantity_processesNormally() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(-50.0);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition(TEST_SYMBOL)).thenReturn(mockPosition);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        verify(mockPosition, times(1)).updateOnFill(TEST_PRICE, -50.0);
    }

    @Test
    @DisplayName("onEvent should handle zero quantity")
    void onEvent_zeroQuantity_processesNormally() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(0.0);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition(TEST_SYMBOL)).thenReturn(mockPosition);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        verify(mockPosition, times(1)).updateOnFill(TEST_PRICE, 0.0);
    }

    @Test
    @DisplayName("onEvent should handle zero price")
    void onEvent_zeroPrice_processesNormally() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(TEST_QUANTITY);
        when(mockTradingEvent.getPrice()).thenReturn(0.0);
        when(mockPositionManager.getPosition(TEST_SYMBOL)).thenReturn(mockPosition);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        verify(mockPosition, times(1)).updateOnFill(0.0, TEST_QUANTITY);
    }

    @Test
    @DisplayName("onEvent should handle negative price")
    void onEvent_negativePrice_processesNormally() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(TEST_QUANTITY);
        when(mockTradingEvent.getPrice()).thenReturn(-150.0);
        when(mockPositionManager.getPosition(TEST_SYMBOL)).thenReturn(mockPosition);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        verify(mockPosition, times(1)).updateOnFill(-150.0, TEST_QUANTITY);
    }

    @Test
    @DisplayName("onEvent should handle fractional quantities")
    void onEvent_fractionalQuantity_processesNormally() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(0.5);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition(TEST_SYMBOL)).thenReturn(mockPosition);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        verify(mockPosition, times(1)).updateOnFill(TEST_PRICE, 0.5);
    }

    @Test
    @DisplayName("onEvent should handle empty symbol")
    void onEvent_emptySymbol_processesNormally() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn("");
        when(mockTradingEvent.getQuantity()).thenReturn(TEST_QUANTITY);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition("")).thenReturn(null);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        verify(mockPositionManager, times(1)).createNewPosition("", TEST_QUANTITY, TEST_PRICE);
    }

    @Test
    @DisplayName("onEvent should handle null symbol")
    void onEvent_nullSymbol_processesNormally() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(null);
        when(mockTradingEvent.getQuantity()).thenReturn(TEST_QUANTITY);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition(null)).thenReturn(null);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        verify(mockPositionManager, times(1)).createNewPosition(null, TEST_QUANTITY, TEST_PRICE);
    }

    @Test
    @DisplayName("onEvent execution order should be correct")
    void onEvent_executionOrder_followsCorrectSequence() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(TEST_QUANTITY);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition(TEST_SYMBOL)).thenReturn(mockPosition);

        var inOrder = inOrder(mockPositionManager, mockPosition, mockPnlCalculator, mockLedger);

        portfolioTracker.onEvent(mockTradingEvent, TEST_SEQUENCE, false);

        inOrder.verify(mockPositionManager).getPosition(TEST_SYMBOL);
        inOrder.verify(mockPosition).updateOnFill(TEST_PRICE, TEST_QUANTITY);
        inOrder.verify(mockPnlCalculator).calculate(mockPosition);
        inOrder.verify(mockLedger).recordTrade(mockTradingEvent, TEST_SEQUENCE);
    }

    @Test
    @DisplayName("Constructor should initialize all dependencies")
    void constructor_initializesAllComponents() throws Exception {
        PortfolioTracker tracker = new PortfolioTracker();

        assertNotNull(getPositionManager(tracker));
        assertNotNull(getPnlCalculator(tracker));
        assertNotNull(getLedger(tracker));
    }

    @Test
    @DisplayName("onEvent should handle rapid sequential events")
    void onEvent_rapidSequentialEvents_processesAll() {
        when(mockTradingEvent.getType()).thenReturn(EventType.ORDER_FILL);
        when(mockTradingEvent.getSymbol()).thenReturn(TEST_SYMBOL);
        when(mockTradingEvent.getQuantity()).thenReturn(TEST_QUANTITY);
        when(mockTradingEvent.getPrice()).thenReturn(TEST_PRICE);
        when(mockPositionManager.getPosition(TEST_SYMBOL)).thenReturn(mockPosition);

        for (long i = 0; i < 100; i++) {
            portfolioTracker.onEvent(mockTradingEvent, i, false);
        }

        verify(mockPosition, times(100)).updateOnFill(TEST_PRICE, TEST_QUANTITY);
        verify(mockPnlCalculator, times(100)).calculate(mockPosition);
        verify(mockLedger, times(100)).recordTrade(eq(mockTradingEvent), anyLong());
    }

    // Helper methods to inject mocks using reflection
    private void injectMocks() throws Exception {
        java.lang.reflect.Field posManagerField = PortfolioTracker.class.getDeclaredField("posManager");
        posManagerField.setAccessible(true);
        posManagerField.set(portfolioTracker, mockPositionManager);

        java.lang.reflect.Field pnlCalcField = PortfolioTracker.class.getDeclaredField("pnlCalc");
        pnlCalcField.setAccessible(true);
        pnlCalcField.set(portfolioTracker, mockPnlCalculator);

        java.lang.reflect.Field ledgerField = PortfolioTracker.class.getDeclaredField("ledger");
        ledgerField.setAccessible(true);
        ledgerField.set(portfolioTracker, mockLedger);
    }

    // Helper methods to access private fields for verification
    private PositionManager getPositionManager(PortfolioTracker tracker) throws Exception {
        java.lang.reflect.Field field = PortfolioTracker.class.getDeclaredField("posManager");
        field.setAccessible(true);
        return (PositionManager) field.get(tracker);
    }

    private PnLCalculator getPnlCalculator(PortfolioTracker tracker) throws Exception {
        java.lang.reflect.Field field = PortfolioTracker.class.getDeclaredField("pnlCalc");
        field.setAccessible(true);
        return (PnLCalculator) field.get(tracker);
    }

    private Ledger getLedger(PortfolioTracker tracker) throws Exception {
        java.lang.reflect.Field field = PortfolioTracker.class.getDeclaredField("ledger");
        field.setAccessible(true);
        return (Ledger) field.get(tracker);
    }
}