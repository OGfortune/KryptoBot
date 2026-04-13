package com.oghenemalu.kryptobot.alert;

import com.oghenemalu.kryptobot.enums.ConditionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "alert")
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long chatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionType conditionType;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal targetPrice;

    @Column(nullable = false)
    private String symbol;

    private boolean isActive = true;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean shouldFire(BigDecimal currentPrice) {
        return conditionType.matches(currentPrice, targetPrice);
    }
}
