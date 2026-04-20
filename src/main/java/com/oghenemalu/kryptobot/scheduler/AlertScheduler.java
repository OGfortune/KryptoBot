package com.oghenemalu.kryptobot.scheduler;

import com.oghenemalu.kryptobot.alert.Alert;
import com.oghenemalu.kryptobot.alert.AlertService;
import com.oghenemalu.kryptobot.bot.MenuBuilder;
import com.oghenemalu.kryptobot.price.CoinRegistry;
import com.oghenemalu.kryptobot.price.PriceService;
import com.oghenemalu.kryptobot.price.dto.PriceDto;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AlertScheduler {
    private final PriceService priceService;
    private final AlertService alertService;
    private final MenuBuilder menuBuilder;
    private final CoinRegistry coinRegistry;

    private final Logger log = org.slf4j.LoggerFactory.getLogger(AlertScheduler.class);


    @Setter
    private AbsSender absSender;




    @Scheduled(fixedRate = 60000)
    public void checkAlerts() {
        if (absSender == null) {
            return;
        }
        log.info("Checking alerts...");
        System.out.println("Checking alerts...");

        List<Alert> activeAlerts = alertService.getAllActiveAlerts();
        if (activeAlerts.isEmpty()) {
            return;
        }

        Map<String, List<Alert>> alertsBySymbol = activeAlerts.stream()
                .collect(Collectors.groupingBy(Alert::getSymbol));

        alertsBySymbol.forEach((symbol, alerts) -> {
            PriceDto priceDto = priceService.getPrice(coinRegistry.getCoinId(symbol), "USD");
            if (priceDto.hasError()) {
                return;
            }


            for (Alert alert : alerts) {
                try {
                    if (alert.shouldFire(priceDto.getPrice())) {
                        absSender.execute(menuBuilder.sendMessage(alert.getChatId(), "Alert triggered for " + symbol + ": " + priceDto.getPrice()
                                + " is " + alert.getConditionType() + " or equals your target price of " + alert.getTargetPrice()));
                    }
                } catch (Exception e) {
                    log.error("Error checking alert: {}", e.getMessage(), e);
                }
            }
        });

    }


}
