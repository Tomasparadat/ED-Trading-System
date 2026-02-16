package com.trading.portfolio;

import com.trading.domain.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("PnLCalculator Unit Tests")
class PnLCalculatorTest {

    private PnLCalculator pnlCalculator;

    @Mock
    private Position mockPosition;

    private static final double DELTA = 0.0001;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pnlCalculator = new PnLCalculator();
    }

    @Test
    @DisplayName("getTotalRealizedPnL should return zero initially")
    void getTotalRealizedPnL_newCalculator_returnsZero() {
        assertEquals(0.0, pnlCalculator.getTotalRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("calculate should add positive PnL from position")
    void calculate_positionWithPositivePnL_addsTotalPnL() {
        when(mockPosition.getLastTradeRealizedPnL()).thenReturn(500.0);

        pnlCalculator.calculate(mockPosition);

        assertEquals(500.0, pnlCalculator.getTotalRealizedPnL(), DELTA);
        verify(mockPosition, times(1)).getLastTradeRealizedPnL();
    }

    @Test
    @DisplayName("calculate should add negative PnL from position")
    void calculate_positionWithNegativePnL_subtractsFromTotalPnL() {
        when(mockPosition.getLastTradeRealizedPnL()).thenReturn(-300.0);

        pnlCalculator.calculate(mockPosition);

        assertEquals(-300.0, pnlCalculator.getTotalRealizedPnL(), DELTA);
        verify(mockPosition, times(1)).getLastTradeRealizedPnL();
    }

    @Test
    @DisplayName("calculate should accumulate PnL from multiple positions")
    void calculate_multiplePositions_accumulatesPnL() {
        Position mockPosition1 = mock(Position.class);
        Position mockPosition2 = mock(Position.class);
        Position mockPosition3 = mock(Position.class);

        when(mockPosition1.getLastTradeRealizedPnL()).thenReturn(100.0);
        when(mockPosition2.getLastTradeRealizedPnL()).thenReturn(250.0);
        when(mockPosition3.getLastTradeRealizedPnL()).thenReturn(-50.0);

        pnlCalculator.calculate(mockPosition1);
        pnlCalculator.calculate(mockPosition2);
        pnlCalculator.calculate(mockPosition3);

        assertEquals(300.0, pnlCalculator.getTotalRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("calculate should handle zero PnL")
    void calculate_positionWithZeroPnL_noChangeToTotal() {
        when(mockPosition.getLastTradeRealizedPnL()).thenReturn(0.0);

        pnlCalculator.calculate(mockPosition);

        assertEquals(0.0, pnlCalculator.getTotalRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("calculate should handle same position called multiple times")
    void calculate_samePositionMultipleTimes_accumulatesEachCall() {
        when(mockPosition.getLastTradeRealizedPnL())
                .thenReturn(100.0)
                .thenReturn(50.0)
                .thenReturn(-20.0);

        pnlCalculator.calculate(mockPosition);
        pnlCalculator.calculate(mockPosition);
        pnlCalculator.calculate(mockPosition);

        assertEquals(130.0, pnlCalculator.getTotalRealizedPnL(), DELTA);
        verify(mockPosition, times(3)).getLastTradeRealizedPnL();
    }

    @Test
    @DisplayName("calculate should handle very large PnL values")
    void calculate_largePositivePnL_handlesCorrectly() {
        when(mockPosition.getLastTradeRealizedPnL()).thenReturn(1_000_000.0);

        pnlCalculator.calculate(mockPosition);

        assertEquals(1_000_000.0, pnlCalculator.getTotalRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("calculate should handle very large negative PnL values")
    void calculate_largeNegativePnL_handlesCorrectly() {
        when(mockPosition.getLastTradeRealizedPnL()).thenReturn(-500_000.0);

        pnlCalculator.calculate(mockPosition);

        assertEquals(-500_000.0, pnlCalculator.getTotalRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("calculate should handle fractional PnL values")
    void calculate_fractionalPnL_accumulatesCorrectly() {
        when(mockPosition.getLastTradeRealizedPnL()).thenReturn(12.345);

        pnlCalculator.calculate(mockPosition);

        assertEquals(12.345, pnlCalculator.getTotalRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("calculate should accumulate wins and losses correctly")
    void calculate_mixedPnL_calculatesNetCorrectly() {
        Position pos1 = mock(Position.class);
        Position pos2 = mock(Position.class);
        Position pos3 = mock(Position.class);
        Position pos4 = mock(Position.class);

        when(pos1.getLastTradeRealizedPnL()).thenReturn(1000.0);
        when(pos2.getLastTradeRealizedPnL()).thenReturn(-500.0);
        when(pos3.getLastTradeRealizedPnL()).thenReturn(750.0);
        when(pos4.getLastTradeRealizedPnL()).thenReturn(-250.0);

        pnlCalculator.calculate(pos1);
        pnlCalculator.calculate(pos2);
        pnlCalculator.calculate(pos3);
        pnlCalculator.calculate(pos4);

        assertEquals(1000.0, pnlCalculator.getTotalRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("getTotalRealizedPnL should not modify state")
    void getTotalRealizedPnL_multipleCalls_returnsSameValue() {
        when(mockPosition.getLastTradeRealizedPnL()).thenReturn(100.0);
        pnlCalculator.calculate(mockPosition);

        double pnl1 = pnlCalculator.getTotalRealizedPnL();
        double pnl2 = pnlCalculator.getTotalRealizedPnL();
        double pnl3 = pnlCalculator.getTotalRealizedPnL();

        assertEquals(pnl1, pnl2, DELTA);
        assertEquals(pnl2, pnl3, DELTA);
    }

    @Test
    @DisplayName("calculate should handle alternating positive and negative PnL")
    void calculate_alternatingPnL_tracksCorrectly() {
        Position mockPos = mock(Position.class);

        when(mockPos.getLastTradeRealizedPnL())
                .thenReturn(100.0)
                .thenReturn(-50.0)
                .thenReturn(200.0)
                .thenReturn(-75.0)
                .thenReturn(25.0);

        for (int i = 0; i < 5; i++) {
            pnlCalculator.calculate(mockPos);
        }

        assertEquals(200.0, pnlCalculator.getTotalRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("calculate should maintain precision with many small trades")
    void calculate_manySmallTrades_maintainsPrecision() {
        Position mockPos = mock(Position.class);
        when(mockPos.getLastTradeRealizedPnL()).thenReturn(0.01);

        for (int i = 0; i < 1000; i++) {
            pnlCalculator.calculate(mockPos);
        }

        assertEquals(10.0, pnlCalculator.getTotalRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("calculate should handle very small PnL values")
    void calculate_verySmallPnL_accumulatesCorrectly() {
        when(mockPosition.getLastTradeRealizedPnL()).thenReturn(0.0001);

        pnlCalculator.calculate(mockPosition);

        assertEquals(0.0001, pnlCalculator.getTotalRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("calculate should work correctly after reaching zero")
    void calculate_afterReachingZero_continuesAccumulating() {
        Position pos1 = mock(Position.class);
        Position pos2 = mock(Position.class);
        Position pos3 = mock(Position.class);

        when(pos1.getLastTradeRealizedPnL()).thenReturn(100.0);
        when(pos2.getLastTradeRealizedPnL()).thenReturn(-100.0);
        when(pos3.getLastTradeRealizedPnL()).thenReturn(50.0);

        pnlCalculator.calculate(pos1);
        assertEquals(100.0, pnlCalculator.getTotalRealizedPnL(), DELTA);

        pnlCalculator.calculate(pos2);
        assertEquals(0.0, pnlCalculator.getTotalRealizedPnL(), DELTA);

        pnlCalculator.calculate(pos3);
        assertEquals(50.0, pnlCalculator.getTotalRealizedPnL(), DELTA);
    }

    @Test
    @DisplayName("calculate should handle null position gracefully")
    void calculate_nullPosition_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            pnlCalculator.calculate(null);
        });
    }
}