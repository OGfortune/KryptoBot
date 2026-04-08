package com.oghenemalu.kryptobot.scheduler;

import com.oghenemalu.kryptobot.alerts.AlertService;
import com.oghenemalu.kryptobot.alerts.Alerts;
import com.oghenemalu.kryptobot.price.PriceService;
import com.oghenemalu.kryptobot.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AlertScheduler {
    private final PriceService priceService;
    private final AlertService alertService;
    private final UserService userService;
    private AbsSender senderBot;

    public void setSenderBot(AbsSender senderBot) {
        this.senderBot = senderBot;
    }

    @Scheduled(fixedRate = 60000)
    public void checkAlerts() {
        if (senderBot == null) {
            return;
        }

        List<Alerts> activeAlerts = alertService.getAllActiveAlerts();
        if (activeAlerts.isEmpty()) {
            return;
        }

        Map<String, List<Alerts>> alertsBySymbol = activeAlerts.stream()
                .collect(Collectors.groupingBy(Alerts::getSymbol));

        alertsBySymbol.forEach((symbol, alerts) -> {
            Alerts alert = alerts.getFirst();
            BigDecimal price = new BigDecimal(priceService.getPrice(symbol, "USD"));
        });

    }



}
