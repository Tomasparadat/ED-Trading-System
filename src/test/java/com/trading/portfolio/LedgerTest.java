package com.trading.portfolio;

import com.trading.domain.OrderFill;
import com.trading.infra.event.TradingEvent;
import com.trading.infra.event.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Ledger Unit Tests")
class LedgerTest {

    private Ledger ledger;

    @Mock
    private OrderFill mockTradingEvent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ledger = new Ledger();
    }

    @Test
    @DisplayName("recordTrade should add trade to history")
    void recordTrade_validEvent_addsToHistory() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn("AAPL");
        when(mockTradingEvent.quantity()).thenReturn(100.0);
        when(mockTradingEvent.price()).thenReturn(150.0);

        ledger.recordTrade(mockTradingEvent, 1L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(1, history.size());

        verify(mockTradingEvent, times(1)).symbol();
        verify(mockTradingEvent, times(1)).quantity();
        verify(mockTradingEvent, times(1)).price();
    }

    @Test
    @DisplayName("recordTrade should handle multiple trades")
    void recordTrade_multipleTrades_addsAllToHistory() throws Exception {
        OrderFill event1 = mock(OrderFill.class);
        OrderFill event2 = mock(OrderFill.class);
        OrderFill event3 = mock(OrderFill.class);

        when(event1.symbol()).thenReturn("AAPL");
        when(event1.quantity()).thenReturn(100.0);
        when(event1.price()).thenReturn(150.0);

        when(event2.symbol()).thenReturn("TSLA");
        when(event2.quantity()).thenReturn(50.0);
        when(event2.price()).thenReturn(700.0);

        when(event3.symbol()).thenReturn("MSFT");
        when(event3.quantity()).thenReturn(200.0);
        when(event3.price()).thenReturn(300.0);

        ledger.recordTrade(event1, 1L);
        ledger.recordTrade(event2, 2L);
        ledger.recordTrade(event3, 3L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(3, history.size());
    }

    @Test
    @DisplayName("recordTrade should create snapshot preventing mutation")
    void recordTrade_sameEventObject_createsIndependentSnapshots() throws Exception {
        when(mockTradingEvent.symbol())
                .thenReturn("AAPL")
                .thenReturn("TSLA");
        when(mockTradingEvent.quantity())
                .thenReturn(100.0)
                .thenReturn(50.0);
        when(mockTradingEvent.price())
                .thenReturn(150.0)
                .thenReturn(700.0);

        // Record the same event object twice (simulating Ring Buffer reuse)
        ledger.recordTrade(mockTradingEvent, 1L);
        ledger.recordTrade(mockTradingEvent, 2L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(2, history.size());

        // Verify each call captured the state at that moment
        verify(mockTradingEvent, times(2)).symbol();
        verify(mockTradingEvent, times(2)).quantity();
        verify(mockTradingEvent, times(2)).price();
    }

    @Test
    @DisplayName("recordTrade should handle sequential sequence numbers")
    void recordTrade_sequentialSequences_recordsCorrectly() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn("AAPL");
        when(mockTradingEvent.quantity()).thenReturn(100.0);
        when(mockTradingEvent.price()).thenReturn(150.0);

        for (long i = 0; i < 10; i++) {
            ledger.recordTrade(mockTradingEvent, i);
        }

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(10, history.size());
    }

    @Test
    @DisplayName("recordTrade should handle non-sequential sequence numbers")
    void recordTrade_nonSequentialSequences_recordsCorrectly() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn("AAPL");
        when(mockTradingEvent.quantity()).thenReturn(100.0);
        when(mockTradingEvent.price()).thenReturn(150.0);

        ledger.recordTrade(mockTradingEvent, 1L);
        ledger.recordTrade(mockTradingEvent, 100L);
        ledger.recordTrade(mockTradingEvent, 5L);
        ledger.recordTrade(mockTradingEvent, 1000L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(4, history.size());
    }

    @Test
    @DisplayName("recordTrade should handle zero sequence number")
    void recordTrade_zeroSequence_recordsCorrectly() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn("AAPL");
        when(mockTradingEvent.quantity()).thenReturn(100.0);
        when(mockTradingEvent.price()).thenReturn(150.0);

        ledger.recordTrade(mockTradingEvent, 0L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(1, history.size());
    }

    @Test
    @DisplayName("recordTrade should handle negative sequence number")
    void recordTrade_negativeSequence_recordsCorrectly() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn("AAPL");
        when(mockTradingEvent.quantity()).thenReturn(100.0);
        when(mockTradingEvent.price()).thenReturn(150.0);

        ledger.recordTrade(mockTradingEvent, -1L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(1, history.size());
    }

    @Test
    @DisplayName("recordTrade should handle very large sequence number")
    void recordTrade_largeSequence_recordsCorrectly() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn("AAPL");
        when(mockTradingEvent.quantity()).thenReturn(100.0);
        when(mockTradingEvent.price()).thenReturn(150.0);

        ledger.recordTrade(mockTradingEvent, Long.MAX_VALUE);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(1, history.size());
    }

    @Test
    @DisplayName("recordTrade should handle negative quantity")
    void recordTrade_negativeQuantity_recordsCorrectly() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn("AAPL");
        when(mockTradingEvent.quantity()).thenReturn(-100.00);
        when(mockTradingEvent.price()).thenReturn(150.0);

        ledger.recordTrade(mockTradingEvent, 1L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(1, history.size());
    }

    @Test
    @DisplayName("recordTrade should handle zero quantity")
    void recordTrade_zeroQuantity_recordsCorrectly() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn("AAPL");
        when(mockTradingEvent.quantity()).thenReturn(0.0);
        when(mockTradingEvent.price()).thenReturn(150.0);

        ledger.recordTrade(mockTradingEvent, 1L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(1, history.size());
    }

    @Test
    @DisplayName("recordTrade should handle zero price")
    void recordTrade_zeroPrice_recordsCorrectly() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn("AAPL");
        when(mockTradingEvent.quantity()).thenReturn(100.0);
        when(mockTradingEvent.price()).thenReturn(0.0);

        ledger.recordTrade(mockTradingEvent, 1L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(1, history.size());
    }

    @Test
    @DisplayName("recordTrade should handle negative price")
    void recordTrade_negativePrice_recordsCorrectly() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn("AAPL");
        when(mockTradingEvent.quantity()).thenReturn(100.0);
        when(mockTradingEvent.price()).thenReturn(-150.0);

        ledger.recordTrade(mockTradingEvent, 1L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(1, history.size());
    }

    @Test
    @DisplayName("recordTrade should handle fractional quantities")
    void recordTrade_fractionalQuantity_recordsCorrectly() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn("BTC");
        when(mockTradingEvent.quantity()).thenReturn(0.5);
        when(mockTradingEvent.price()).thenReturn(50000.0);

        ledger.recordTrade(mockTradingEvent, 1L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(1, history.size());
    }

    @Test
    @DisplayName("recordTrade should handle fractional prices")
    void recordTrade_fractionalPrice_recordsCorrectly() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn("AAPL");
        when(mockTradingEvent.quantity()).thenReturn(100.0);
        when(mockTradingEvent.price()).thenReturn(150.99);

        ledger.recordTrade(mockTradingEvent, 1L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(1, history.size());
    }

    @Test
    @DisplayName("recordTrade should handle empty symbol")
    void recordTrade_emptySymbol_recordsCorrectly() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn("");
        when(mockTradingEvent.quantity()).thenReturn(100.0);
        when(mockTradingEvent.price()).thenReturn(150.0);

        ledger.recordTrade(mockTradingEvent, 1L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(1, history.size());
    }

    @Test
    @DisplayName("recordTrade should handle null symbol")
    void recordTrade_nullSymbol_recordsCorrectly() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn(null);
        when(mockTradingEvent.quantity()).thenReturn(100.0);
        when(mockTradingEvent.price()).thenReturn(150.0);

        ledger.recordTrade(mockTradingEvent, 1L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(1, history.size());
    }

    @Test
    @DisplayName("recordTrade should handle many trades efficiently")
    void recordTrade_manyTrades_performsEfficiently() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn("AAPL");
        when(mockTradingEvent.quantity()).thenReturn(100.0);
        when(mockTradingEvent.price()).thenReturn(150.0);

        int numberOfTrades = 10000;
        for (long i = 0; i < numberOfTrades; i++) {
            ledger.recordTrade(mockTradingEvent, i);
        }

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(numberOfTrades, history.size());
    }

    @Test
    @DisplayName("recordTrade should handle duplicate sequence numbers")
    void recordTrade_duplicateSequence_recordsBothTrades() throws Exception {
        OrderFill event1 = mock(OrderFill.class);
        OrderFill event2 = mock(OrderFill.class);

        when(event1.symbol()).thenReturn("AAPL");
        when(event1.quantity()).thenReturn(100.0);
        when(event1.price()).thenReturn(150.0);

        when(event2.symbol()).thenReturn("TSLA");
        when(event2.quantity()).thenReturn(50.0);
        when(event2.price()).thenReturn(700.0);

        // Same sequence number for both
        ledger.recordTrade(event1, 1L);
        ledger.recordTrade(event2, 1L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(2, history.size());
    }

    @Test
    @DisplayName("recordTrade should handle null event")
    void recordTrade_nullEvent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            ledger.recordTrade(null, 1L);
        });
    }

    @Test
    @DisplayName("recordTrade should handle very large prices")
    void recordTrade_largePrice_recordsCorrectly() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn("BRK.A");
        when(mockTradingEvent.quantity()).thenReturn(1.0);
        when(mockTradingEvent.price()).thenReturn(500000.0);

        ledger.recordTrade(mockTradingEvent, 1L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(1, history.size());
    }

    @Test
    @DisplayName("recordTrade should handle very large quantities")
    void recordTrade_largeQuantity_recordsCorrectly() throws Exception {
        when(mockTradingEvent.symbol()).thenReturn("PENNY");
        when(mockTradingEvent.quantity()).thenReturn(1000000.0);
        when(mockTradingEvent.price()).thenReturn(0.01);

        ledger.recordTrade(mockTradingEvent, 1L);

        List<LedgerRecord> history = getTradeHistory(ledger);
        assertEquals(1, history.size());
    }

    // Helper method to access private tradeHistory field using reflection
    @SuppressWarnings("unchecked")
    private List<LedgerRecord> getTradeHistory(Ledger ledger) throws Exception {
        Field field = Ledger.class.getDeclaredField("tradeHistory");
        field.setAccessible(true);
        return (List<LedgerRecord>) field.get(ledger);
    }
}