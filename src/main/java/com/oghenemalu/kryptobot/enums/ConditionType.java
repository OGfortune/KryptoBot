package com.oghenemalu.kryptobot.enums;

import java.math.BigDecimal;

public enum ConditionType {
    ABOVE {
        @Override
        public boolean matches(BigDecimal currentPrice, BigDecimal targetPrice) {
            return currentPrice.compareTo(targetPrice) >= 0;
        }
    },
    BELOW {
        @Override
        public boolean matches(BigDecimal currentPrice, BigDecimal targetPrice) {
                return currentPrice.compareTo(targetPrice) <= 0;
        }
    };

    public abstract boolean matches(BigDecimal currentPrice, BigDecimal targetPrice);
}
