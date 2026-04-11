package com.oghenemalu.kryptobot.scheduler;

import com.oghenemalu.kryptobot.alerts.AlertService;
import com.oghenemalu.kryptobot.alerts.Alerts;
import com.oghenemalu.kryptobot.bot.MenuBuilder;
import com.oghenemalu.kryptobot.price.PriceService;
import com.oghenemalu.kryptobot.price.dto.PriceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AlertScheduler {
    private final PriceService priceService;
    private final AlertService alertService;
    private final MenuBuilder menuBuilder;


    @Scheduled(fixedRate = 60000)
    public void checkAlerts() {

        List<Alerts> activeAlerts = alertService.getAllActiveAlerts();
        if (activeAlerts.isEmpty()) {
            return;
        }

        Map<String, List<Alerts>> alertsBySymbol = activeAlerts.stream()
                .collect(Collectors.groupingBy(Alerts::getSymbol));

        alertsBySymbol.forEach((symbol, alerts) -> {
            PriceDto priceDto = priceService.getPrice(symbol, "USD");
            if (priceDto.hasError()) {
                return;
            }

            for (Alerts alert : alerts) {
                if (alert.shouldFire(priceDto.getPrice())) {
                    menuBuilder.sendMessage(alert.getChatId(), "Alert triggered for " + symbol + ": " + priceDto.getPrice()
                            + " is " + alert.getConditionType() + " or equals your target price of " + alert.getTargetPrice());
                }
            }
        });

    }


}
