package com.trading.risk;

public enum RiskResult {
    PASSED,
    REJECTED_DRAWDOWN_HALT,
    REJECTED_LIMIT_EXCEEDED,
    REJECTED_PRICE_INVALID
}
