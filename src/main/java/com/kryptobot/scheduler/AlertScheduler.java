package com.kryptobot.scheduler;

import com.kryptobot.alert.Alert;
import com.kryptobot.alert.AlertService;
import com.kryptobot.bot.MenuBuilder;
import com.kryptobot.price.CoinRegistry;
import com.kryptobot.price.PriceService;
import com.kryptobot.price.dto.PriceDto;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertScheduler {
    private final PriceService priceService;
    private final AlertService alertService;
    private final MenuBuilder menuBuilder;
    private final CoinRegistry coinRegistry;



    @Setter
    private AbsSender absSender;




    @Scheduled(fixedRate = 60000)
    public void checkAlerts() {
        if (absSender == null) {
            return;
        }
        log.info("Checking alerts...");

        //create a list of active alerts
        List<Alert> activeAlerts = alertService.getAllActiveAlerts();
        if (activeAlerts.isEmpty()) {
            return;
        }

        //group active alerts by symbol
        Map<String, List<Alert>> alertsBySymbol = activeAlerts.stream()
                .collect(Collectors.groupingBy(Alert::getSymbol));

        //check each alert
        alertsBySymbol.forEach((symbol, alerts) -> {
            PriceDto priceDto = priceService.getPrice(coinRegistry.getCoinId(symbol), "USD");
            if (priceDto.hasError()) {
                return;
            }

            //check if alert should fire
            for (Alert alert : alerts) {
                try {
                    //send alert if it should fire
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
